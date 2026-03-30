package com.javalab;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javalab.core.events.ExternalAuthEvent;
import com.javalab.core.events.UserLoggedInEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jboss.logmanager.Level;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class KafkaLoginEventListener implements EventListenerProvider
{
    private final KafkaConfig config;

    private final KeycloakSession session;

    private final KafkaProducer<String, String> kafkaProducer;

    private final ObjectMapper objectMapper;

    private static final Set<String> EXTERNAL_PROVIDERS = Set.of("google", "github", "yandex", "vk");

    private final Logger logger = Logger.getLogger(KafkaLoginEventListener.class.getName());

    public KafkaLoginEventListener(KeycloakSession session)
    {
        this.session = session;
        this.objectMapper = new ObjectMapper();
        this.config = new KafkaConfig(StringSerializer.class.getName(), StringSerializer.class.getName());

        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getKeySerializer());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getValueSerializer());

        this.kafkaProducer = new KafkaProducer<>(properties);
    }

    @Override
    public void onEvent(Event event)
    {
        if (event.getError() != null)
            return;

        if (isOAuth2UserRegisterEvent(event))
            sendUpdateOAuth2User(event);

        if (EventType.LOGIN.equals(event.getType()))
            sendUpdateTimestampLogin(event);
    }

    private void sendUpdateTimestampLogin(Event event)
    {
        try
        {
            UserLoggedInEvent userEvent = new UserLoggedInEvent(
                    UUID.randomUUID(),
                    UUID.fromString(event.getUserId()),
                    event.getRealmId(),
                    event.getTime()
            );

            String message = objectMapper.writeValueAsString(userEvent);
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    config.getLoggedInTopicName(),
                    event.getUserId(), message
            );

            kafkaProducer.send(record, (metadata, exception) -> {
                if (exception != null)
                    logger.log(Level.WARNING, "Unable to send event to %s"
                            .formatted(config.getLoggedInTopicName()), exception);
            });

        } catch (JsonProcessingException e)
        {
            logger.log(Level.ERROR,
                    "Error when sending a message to %s"
                            .formatted(config.getLoggedInTopicName()), e);
        }
    }

    private void sendUpdateOAuth2User(Event event)
    {
        try
        {
            UserModel user = session.users().getUserById(
                    session.getContext().getRealm(),
                    event.getUserId()
            );

            if (user == null)
            {
                logger.warning("User not found: " + event.getUserId());
                return;
            }

            ExternalAuthEvent authEvent = new ExternalAuthEvent(
                    UUID.randomUUID(),
                    UUID.fromString(event.getUserId()),
                    user.getUsername(),
                    user.getFirstAttribute("full_name"),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getFirstAttribute("avatar_url"),
                    event.getTime()
            );

            String message = objectMapper.writeValueAsString(authEvent);

            ProducerRecord<String, String> record = new ProducerRecord<>(
                    config.getOauth2LoggedInTopicName(),
                    user.getId(),
                    message
            );

            kafkaProducer.send(record);

        } catch (Exception e)
        {
            logger.log(Level.ERROR,
                    "Unexpected error processing event for user with k-id=%s"
                            .formatted(event.getUserId()), e);
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {}

    @Override
    public void close()
    {
        if (kafkaProducer != null)
        {
            kafkaProducer.flush();
            kafkaProducer.close();
        }
    }

    private boolean isOAuth2UserRegisterEvent(Event event)
    {
        String identityProvider = event.getDetails().getOrDefault("identity_provider", "");
        return EventType.REGISTER.equals(event.getType())
                && EXTERNAL_PROVIDERS.contains(identityProvider);
    }
}
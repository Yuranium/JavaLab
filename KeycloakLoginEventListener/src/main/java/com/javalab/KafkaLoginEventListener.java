package com.javalab;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class KafkaLoginEventListener implements EventListenerProvider
{
    private final KafkaConfig config;

    private final KeycloakSession session;

    private final KafkaProducer<String, String> kafkaProducer;

    private final ObjectMapper objectMapper;

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
        if (EventType.LOGIN.equals(event.getType()) && event.getError() == null)
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
                    config.getTopicName(),
                    event.getUserId(), message
            );

            kafkaProducer.send(record, (metadata, exception) -> {
                if (exception != null)
                    logger.log(Level.WARNING, "Unable to send event to %s"
                            .formatted(config.getTopicName()), exception);
            });

        } catch (JsonProcessingException e)
        {
            logger.log(Level.ERROR,
                    "Error when sending a message to %s"
                            .formatted(config.getTopicName()), e);
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
}
package com.javalab;

public class KafkaConfig
{
    private String bootstrapServers;

    private String loggedInTopicName;

    private String oauth2LoggedInTopicName;

    private String keySerializer;

    private String valueSerializer;

    private int retries;

    private String acks;

    public KafkaConfig(String keySerializer, String valueSerializer)
    {
        this.bootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
        if (bootstrapServers == null || bootstrapServers.isEmpty())
            this.bootstrapServers = "localhost:9092";

        this.loggedInTopicName = System.getenv("KAFKA_LOGIN_TOPIC");
        if (loggedInTopicName == null || loggedInTopicName.isEmpty())
            this.loggedInTopicName = "user-logged-in-topic";

        this.oauth2LoggedInTopicName = System.getenv("KAFKA_OAUTH2_LOGIN_TOPIC");
        if (oauth2LoggedInTopicName == null || oauth2LoggedInTopicName.isEmpty())
            this.oauth2LoggedInTopicName = "oauth2-user-updated-topic";

        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        this.retries = 3;
        this.acks = "all";
    }

    public String getAcks()
    {
        return acks;
    }

    public String getBootstrapServers()
    {
        return bootstrapServers;
    }

    public String getKeySerializer()
    {
        return keySerializer;
    }

    public int getRetries()
    {
        return retries;
    }

    public String getLoggedInTopicName()
    {
        return loggedInTopicName;
    }

    public String getOauth2LoggedInTopicName()
    {
        return oauth2LoggedInTopicName;
    }

    public String getValueSerializer()
    {
        return valueSerializer;
    }
}
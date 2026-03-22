package com.javalab;

public class KafkaConfig
{
    private String bootstrapServers;

    private String topicName;

    private String keySerializer;

    private String valueSerializer;

    private int retries;

    private String acks;

    public KafkaConfig(String keySerializer, String valueSerializer)
    {
        this.bootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
        if (bootstrapServers == null || bootstrapServers.isEmpty())
            this.bootstrapServers = "localhost:9092";

        this.topicName = System.getenv("KAFKA_LOGIN_TOPIC");
        if (topicName == null || topicName.isEmpty())
            this.topicName = "login-timestamp-updated-topic";

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

    public String getTopicName()
    {
        return topicName;
    }

    public String getValueSerializer()
    {
        return valueSerializer;
    }
}
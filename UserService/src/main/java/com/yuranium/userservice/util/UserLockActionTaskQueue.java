package com.yuranium.userservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuranium.userservice.config.RedisConfig;
import com.yuranium.userservice.enums.LockAction;
import com.yuranium.userservice.models.dto.RedisFailedTask;
import com.yuranium.userservice.models.dto.userlock.UserLockTask;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserLockActionTaskQueue
{
    private final StringRedisTemplate redisTemplate;

    private final RedisConfig redisConfig;

    private final ObjectMapper objectMapper;

    private static final String POP_SCRIPT = """
                local items = redis.call('ZRANGEBYSCORE', KEYS[1], 0, ARGV[1])
                if #items > 0 then
                    redis.call('ZREM', KEYS[1], unpack(items))
                    return items
                end
                return {}
            """;

    @SneakyThrows
    public void scheduleTask(Long userId, LockAction action, Instant executeAt)
    {
        UserLockTask task = new UserLockTask(UUID.randomUUID(), userId, action);
        String taskJson = objectMapper.writeValueAsString(task);

        redisTemplate.opsForZSet().add(redisConfig.getQueueKey(),
                taskJson, executeAt.toEpochMilli());
    }

    public List<String> popDueTasks()
    {
        long now = System.currentTimeMillis();
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(POP_SCRIPT);
        script.setResultType(List.class);
        return redisTemplate.execute(
                script,
                Collections.singletonList(redisConfig.getQueueKey()),
                String.valueOf(now)
        );
    }

    public boolean isProcessed(UUID taskId)
    {
        return redisTemplate.hasKey(redisConfig.getProcessedKey(taskId));
    }

    public void markProcessed(UUID taskId)
    {
        redisTemplate.opsForValue().set(
                redisConfig.getProcessedKey(taskId),
                "1",
                redisConfig.getProcessedTtl()
        );
    }

    public void sendToDlq(String taskJson, String error)
    {
        var failed = new RedisFailedTask(taskJson, error, Instant.now());
        redisTemplate.opsForList().rightPush(redisConfig.getDlqKey(), failed.toString());
        redisTemplate.opsForList().trim(redisConfig.getDlqKey(), 0, 1000);
    }
}
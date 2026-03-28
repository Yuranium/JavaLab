package com.yuranium.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuranium.userservice.models.dto.userlock.UserLockTask;
import com.yuranium.userservice.util.UserLockActionTaskQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserLockTaskScheduler
{
    private final UserLockActionTaskQueue taskQueue;

    private final UserLockService lockService;

    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 10000)
    public void processDueTasks()
    {
        List<String> taskJsons = taskQueue.popDueTasks();
        if (taskJsons == null || taskJsons.isEmpty())
            return;

        for (String taskJson : taskJsons)
            processTask(taskJson);
    }

    private void processTask(String taskJson)
    {
        try
        {
            UserLockTask task = objectMapper.readValue(taskJson, UserLockTask.class);
            if (taskQueue.isProcessed(task.taskId()))
                return;

            lockService.executeLockAction(task);
            taskQueue.markProcessed(task.taskId());
        } catch (Exception e)
        {
            taskQueue.sendToDlq(taskJson, e.getMessage());
        }
    }
}
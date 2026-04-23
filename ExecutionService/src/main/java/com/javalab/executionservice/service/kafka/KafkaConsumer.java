package com.javalab.executionservice.service.kafka;

import com.javalab.core.events.TestCaseEvent;
import com.javalab.executionservice.dao.TestCaseDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer
{
    private final TestCaseDao testCaseDao;

    @KafkaListener(topics = "${spring.kafka.topic-names.test-case-events}")
    public void handle(TestCaseEvent event)
    {
        try
        {
            log.info("Received test case event with type {}", event.type());
            switch (event.type())
            {
                case TEST_CASE_CREATED ->
                        testCaseDao.saveAllTestCases(event.taskId(), event.payload());
                case TEST_CASE_UPDATED ->
                        testCaseDao.updateAllTestCases(event.taskId(), event.payload());
                case TEST_CASE_DELETED ->
                        testCaseDao.deleteAllTestCases(event.taskId());
            }
        } catch (Exception e)
        {
            log.error("Failed to process test case event with taskId={}, message:{}",
                    event.taskId(), e.getMessage());
        }
    }
}
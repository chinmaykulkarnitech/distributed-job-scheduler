package com.chinmaytech.job_scheduler_backend.mapper;


import com.chinmaytech.job_scheduler_backend.dto.queues.QueueResponse;
import com.chinmaytech.job_scheduler_backend.entity.Queue;
import org.springframework.stereotype.Component;

@Component
public class QueueMapper {

    public QueueResponse mapQueueToQueueResponse(Queue queue) {

        return QueueResponse.builder()
                .id(queue.getId())
                .projectId(queue.getProjectId())
                .retryPolicyId(queue.getRetryPolicyId())
                .name(queue.getName())
                .priority(queue.getPriority())
                .concurrencyLimit(queue.getConcurrencyLimit())
                .status(queue.getStatus())
                .createdAt(queue.getCreatedAt())
                .updatedAt(queue.getUpdatedAt())
                .build();
    }
}

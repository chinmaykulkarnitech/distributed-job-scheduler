package com.chinmaytech.job_scheduler_backend.service;


import com.chinmaytech.job_scheduler_backend.dto.queues.CreateQueueRequest;
import com.chinmaytech.job_scheduler_backend.dto.queues.QueueResponse;
import com.chinmaytech.job_scheduler_backend.entity.Queue;
import com.chinmaytech.job_scheduler_backend.exception.ConflictException;
import com.chinmaytech.job_scheduler_backend.exception.ResourceNotFoundException;
import com.chinmaytech.job_scheduler_backend.mapper.QueueMapper;
import com.chinmaytech.job_scheduler_backend.repository.ProjectRepository;
import com.chinmaytech.job_scheduler_backend.repository.QueueRepository;
import com.chinmaytech.job_scheduler_backend.repository.RetryPolicyRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueRepository queueRepository;
    private final ProjectRepository projectRepository;
    private final QueueMapper queueMapper;
    private final RetryPolicyRepository retryPolicyRepository;

    @Transactional
    public QueueResponse createQueue(CreateQueueRequest request) {

        // 1. Check project exists
        projectRepository.findById(request.getProjectId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found")

                );

        // 2. Prevent duplicate queue name in same project
        if (queueRepository.existsByProjectIdAndName(
                request.getProjectId(),
                request.getName()
        )) {
            throw new ConflictException(
                    "Queue with this name already exists in this project"
            );
        }

        // 3. Create queue
        Queue queue = Queue.builder()
                .projectId(request.getProjectId())
                .retryPolicyId(request.getRetryPolicyId())
                .name(request.getName())
                .priority(
                        request.getPriority() != null
                                ? request.getPriority()
                                : 0
                )
                .concurrencyLimit(
                        request.getConcurrencyLimit() != null
                                ? request.getConcurrencyLimit()
                                : 1
                )
                .status("ACTIVE")
                .build();

        // 4. Save
        queue = queueRepository.save(queue);

        // 5. Return response
        return queueMapper.mapQueueToQueueResponse(queue);
    }

    public List<QueueResponse> getQueuesByProject(String projectId) {

        List<Queue> queues =
                queueRepository.findByProjectId(projectId);

        List<QueueResponse> responses =
                new java.util.ArrayList<>();

        for (Queue queue : queues) {

            responses.add(
                    queueMapper.mapQueueToQueueResponse(queue)
            );
        }

        return responses;
    }

    public QueueResponse getQueue(String queueId) {

        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Queue not found")

                );

        return queueMapper.mapQueueToQueueResponse(queue);
    }



    @Transactional public QueueResponse assignRetryPolicy( String queueId, String retryPolicyId )
    { // 1. Find queue
         Queue queue = queueRepository.findById(queueId) .orElseThrow(() ->         new ResourceNotFoundException("Queue not found")
         ); // 2. Check retry policy exists
         retryPolicyRepository.findById(retryPolicyId) .orElseThrow(() -> new ResourceNotFoundException("Retry policy not found") ); // 3. Assign retry policy
         queue.setRetryPolicyId(retryPolicyId); // 4. Save queue
         queue = queueRepository.save(queue); // 5. Return updated
          return queueMapper.mapQueueToQueueResponse(queue);
    }

    @Transactional
    public QueueResponse pauseQueue(String queueId) {
        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() ->         new ResourceNotFoundException("Queue not found")
                );

        queue.setStatus("PAUSED");
        queue = queueRepository.save(queue);

        return queueMapper.mapQueueToQueueResponse(queue);
    }

    @Transactional
    public QueueResponse resumeQueue(String queueId) {
        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() ->         new ResourceNotFoundException("Queue not found")
                );

        queue.setStatus("ACTIVE");
        queue = queueRepository.save(queue);

        return queueMapper.mapQueueToQueueResponse(queue);
    }

    }
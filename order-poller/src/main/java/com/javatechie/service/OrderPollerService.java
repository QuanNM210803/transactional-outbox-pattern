package com.javatechie.service;

import com.javatechie.entity.Outbox;
import com.javatechie.publisher.MessagePublisher;
import com.javatechie.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class OrderPollerService {

    private final OutboxRepository repository;
    private final MessagePublisher messagePublisher;

    @Scheduled(fixedRate = 20000)
    public void pollOutboxMessagesAndPublish() {
        List<Outbox> unprocessedRecords = repository.findByProcessedFalse();

        unprocessedRecords.forEach(outbox -> {
            try {
                messagePublisher.publish(outbox.getPayload());
                outbox.setProcessed(true);
                repository.save(outbox);
            } catch (Exception ignored) {
                log.error(ignored.getMessage());
            }
        });
    }
}

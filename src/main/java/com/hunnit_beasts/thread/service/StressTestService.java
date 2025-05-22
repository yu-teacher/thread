package com.hunnit_beasts.thread.service;

import com.hunnit_beasts.thread.util.ExecutionResult;
import com.hunnit_beasts.thread.util.ThreadUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class StressTestService {

    /**
     * 가상 스레드를 사용한 간단한 요청 처리
     */
    @Async("virtualThreadExecutor")
    public CompletableFuture<ExecutionResult<String>> processSimpleRequestWithVirtualThread(int id) {
        return ThreadUtils.executeWithMetrics(
                "간단한 요청 (가상) - " + id,
                () -> "요청 " + id + " 처리 완료"
        );
    }

    /**
     * 플랫폼 스레드를 사용한 간단한 요청 처리
     */
    @Async("platformThreadExecutor")
    public CompletableFuture<ExecutionResult<String>> processSimpleRequestWithPlatformThread(int id) {
        return ThreadUtils.executeWithMetrics(
                "간단한 요청 (플랫폼) - " + id,
                () -> "요청 " + id + " 처리 완료"
        );
    }

    /**
     * 가상 스레드를 사용한 지연 요청 처리
     */
    @Async("virtualThreadExecutor")
    public CompletableFuture<ExecutionResult<String>> processDelayedRequestWithVirtualThread(
            int id, long delayMs) {
        return ThreadUtils.executeWithMetrics(
                "지연 요청 (가상) - " + id,
                () -> {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("스레드가 중단되었습니다", e);
                    }
                    return "지연 요청 " + id + " 처리 완료 (지연: " + delayMs + "ms)";
                }
        );
    }

    /**
     * 플랫폼 스레드를 사용한 지연 요청 처리
     */
    @Async("platformThreadExecutor")
    public CompletableFuture<ExecutionResult<String>> processDelayedRequestWithPlatformThread(
            int id, long delayMs) {
        return ThreadUtils.executeWithMetrics(
                "지연 요청 (플랫폼) - " + id,
                () -> {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("스레드가 중단되었습니다", e);
                    }
                    return "지연 요청 " + id + " 처리 완료 (지연: " + delayMs + "ms)";
                }
        );
    }

    /**
     * 가상 스레드로 다중 요청 처리
     */
    public List<ExecutionResult<String>> processMultipleRequestsWithVirtualThread(int count) {
        List<CompletableFuture<ExecutionResult<String>>> futures = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            futures.add(processSimpleRequestWithVirtualThread(i));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * 플랫폼 스레드로 다중 요청 처리
     */
    public List<ExecutionResult<String>> processMultipleRequestsWithPlatformThread(int count) {
        List<CompletableFuture<ExecutionResult<String>>> futures = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            futures.add(processSimpleRequestWithPlatformThread(i));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * 가상 스레드로 지연 요청 처리
     */
    public List<ExecutionResult<String>> processDelayedRequestsWithVirtualThread(int count, long delayMs) {
        List<CompletableFuture<ExecutionResult<String>>> futures = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            futures.add(processDelayedRequestWithVirtualThread(i, delayMs));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * 플랫폼 스레드로 지연 요청 처리
     */
    public List<ExecutionResult<String>> processDelayedRequestsWithPlatformThread(int count, long delayMs) {
        List<CompletableFuture<ExecutionResult<String>>> futures = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            futures.add(processDelayedRequestWithPlatformThread(i, delayMs));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }
}
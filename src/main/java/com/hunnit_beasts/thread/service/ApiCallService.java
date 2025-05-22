package com.hunnit_beasts.thread.service;

import com.hunnit_beasts.thread.model.ApiResponse;
import com.hunnit_beasts.thread.util.ExecutionResult;
import com.hunnit_beasts.thread.util.ThreadUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApiCallService {

    private final WebClient.Builder webClientBuilder;

    /**
     * 단일 API 호출 수행
     */
    public ApiResponse callSingleApi(int id) {
        return ThreadUtils.measureExecutionTime(
                "Single API Call",
                () -> webClientBuilder.build()
                        .get()
                        .uri("https://jsonplaceholder.typicode.com/posts/" + id)
                        .retrieve()
                        .bodyToMono(ApiResponse.class)
                        .block()
        );
    }

    /**
     * 가상 스레드를 사용하여 다중 API 호출
     */
    @Async("virtualThreadExecutor")
    public CompletableFuture<ExecutionResult<ApiResponse>> callApiWithVirtualThread(int id) {
        return ThreadUtils.executeWithMetrics(
                "API Call (Virtual) - " + id,
                () -> webClientBuilder.build()
                        .get()
                        .uri("https://jsonplaceholder.typicode.com/posts/" + id)
                        .retrieve()
                        .bodyToMono(ApiResponse.class)
                        .block()
        );
    }

    /**
     * 플랫폼 스레드를 사용하여 다중 API 호출
     */
    @Async("platformThreadExecutor")
    public CompletableFuture<ExecutionResult<ApiResponse>> callApiWithPlatformThread(int id) {
        return ThreadUtils.executeWithMetrics(
                "API Call (Platform) - " + id,
                () -> webClientBuilder.build()
                        .get()
                        .uri("https://jsonplaceholder.typicode.com/posts/" + id)
                        .retrieve()
                        .bodyToMono(ApiResponse.class)
                        .block()
        );
    }

    /**
     * 다중 API 호출 실행 (가상 스레드)
     */
    public List<ExecutionResult<ApiResponse>> callMultipleApisWithVirtualThread(int count) {
        List<CompletableFuture<ExecutionResult<ApiResponse>>> futures = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            futures.add(callApiWithVirtualThread(i));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * 다중 API 호출 실행 (플랫폼 스레드)
     */
    public List<ExecutionResult<ApiResponse>> callMultipleApisWithPlatformThread(int count) {
        List<CompletableFuture<ExecutionResult<ApiResponse>>> futures = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            futures.add(callApiWithPlatformThread(i));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }
}
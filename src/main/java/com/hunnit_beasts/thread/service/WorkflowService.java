package com.hunnit_beasts.thread.service;

import com.hunnit_beasts.thread.model.ApiResponse;
import com.hunnit_beasts.thread.model.Product;
import com.hunnit_beasts.thread.model.User;
import com.hunnit_beasts.thread.repository.ProductRepository;
import com.hunnit_beasts.thread.repository.UserRepository;
import com.hunnit_beasts.thread.util.ExecutionResult;
import com.hunnit_beasts.thread.util.ThreadUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowService {

    private final WebClient.Builder webClientBuilder;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private static final Path BASE_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "virtual-threads-test");

    /**
     * 가상 스레드를 사용한 간단한 워크플로우
     */
    @Async("virtualThreadExecutor")
    public CompletableFuture<ExecutionResult<String>> runSimpleWorkflowWithVirtualThread(int id) {
        return ThreadUtils.executeWithMetrics(
                "간단한 워크플로우 (가상) - " + id,
                () -> {
                    // 1. API 호출
                    log.info("워크플로우 {}: API 호출 시작", id);
                    ApiResponse apiResponse = webClientBuilder.build()
                            .get()
                            .uri("https://jsonplaceholder.typicode.com/posts/" + id)
                            .retrieve()
                            .bodyToMono(ApiResponse.class)
                            .block();
                    log.info("워크플로우 {}: API 호출 완료", id);

                    // 2. DB 저장
                    log.info("워크플로우 {}: DB 저장 시작", id);
                    User user = new User("워크플로우사용자" + id, "workflow" + id + "@example.com");
                    userRepository.save(user);
                    log.info("워크플로우 {}: DB 저장 완료", id);

                    // 3. 파일 기록
                    log.info("워크플로우 {}: 파일 기록 시작", id);
                    String fileName = "workflow-" + id + "-" + UUID.randomUUID() + ".txt";
                    Path filePath = BASE_DIR.resolve(fileName);
                    String content = "워크플로우 ID: " + id + "\n" +
                            "시간: " + LocalDateTime.now() + "\n" +
                            "API 응답: " + apiResponse.getTitle() + "\n" +
                            "사용자 ID: " + user.getId();
                    Files.writeString(filePath, content, StandardCharsets.UTF_8);
                    log.info("워크플로우 {}: 파일 기록 완료", id);

                    return "간단한 워크플로우 " + id + " 완료";
                }
        );
    }

    /**
     * 플랫폼 스레드를 사용한 간단한 워크플로우
     */
    @Async("platformThreadExecutor")
    public CompletableFuture<ExecutionResult<String>> runSimpleWorkflowWithPlatformThread(int id) {
        return ThreadUtils.executeWithMetrics(
                "간단한 워크플로우 (플랫폼) - " + id,
                () -> {
                    // 1. API 호출
                    log.info("워크플로우 {}: API 호출 시작", id);
                    ApiResponse apiResponse = webClientBuilder.build()
                            .get()
                            .uri("https://jsonplaceholder.typicode.com/posts/" + id)
                            .retrieve()
                            .bodyToMono(ApiResponse.class)
                            .block();
                    log.info("워크플로우 {}: API 호출 완료", id);

                    // 2. DB 저장
                    log.info("워크플로우 {}: DB 저장 시작", id);
                    User user = new User("워크플로우사용자" + id, "workflow" + id + "@example.com");
                    userRepository.save(user);
                    log.info("워크플로우 {}: DB 저장 완료", id);

                    // 3. 파일 기록
                    log.info("워크플로우 {}: 파일 기록 시작", id);
                    String fileName = "workflow-" + id + "-" + UUID.randomUUID() + ".txt";
                    Path filePath = BASE_DIR.resolve(fileName);
                    String content = "워크플로우 ID: " + id + "\n" +
                            "시간: " + LocalDateTime.now() + "\n" +
                            "API 응답: " + apiResponse.getTitle() + "\n" +
                            "사용자 ID: " + user.getId();
                    Files.writeString(filePath, content, StandardCharsets.UTF_8);
                    log.info("워크플로우 {}: 파일 기록 완료", id);

                    return "간단한 워크플로우 " + id + " 완료";
                }
        );
    }

    /**
     * 가상 스레드를 사용한 복잡한 워크플로우
     */
    @Async("virtualThreadExecutor")
    public CompletableFuture<ExecutionResult<String>> runComplexWorkflowWithVirtualThread(int id) {
        return ThreadUtils.executeWithMetrics(
                "복잡한 워크플로우 (가상) - " + id,
                () -> {
                    // 1. 다중 API 호출
                    log.info("복잡한 워크플로우 {}: 다중 API 호출 시작", id);
                    List<CompletableFuture<ApiResponse>> apiFutures = new ArrayList<>();
                    for (int i = 1; i <= 3; i++) {
                        final int apiId = id * 10 + i;
                        apiFutures.add(CompletableFuture.supplyAsync(() ->
                                webClientBuilder.build()
                                        .get()
                                        .uri("https://jsonplaceholder.typicode.com/posts/" + apiId)
                                        .retrieve()
                                        .bodyToMono(ApiResponse.class)
                                        .block()
                        ));
                    }
                    List<ApiResponse> apiResponses = apiFutures.stream()
                            .map(CompletableFuture::join)
                            .toList();
                    log.info("복잡한 워크플로우 {}: 다중 API 호출 완료", id);

                    // 2. DB 조회 및 저장
                    log.info("복잡한 워크플로우 {}: DB 작업 시작", id);
                    // 기존 사용자 조회
                    User existingUser = null;
                    try {
                        existingUser = userRepository.findById((long)id).orElse(null);
                    } catch (Exception e) {
                        log.warn("사용자 조회 실패, 새 사용자 생성: {}", e.getMessage());
                    }

                    // 새 사용자 저장
                    User newUser = new User("복잡워크플로우사용자" + id, "complex" + id + "@example.com");
                    userRepository.save(newUser);

                    // 새 제품 저장
                    Product newProduct = new Product("복잡워크플로우제품" + id, (double)(1000 * id));
                    productRepository.save(newProduct);
                    log.info("복잡한 워크플로우 {}: DB 작업 완료", id);

                    // 3. 파일 작업
                    log.info("복잡한 워크플로우 {}: 파일 작업 시작", id);
                    String fileName = "complex-workflow-" + id + "-" + UUID.randomUUID() + ".txt";
                    Path filePath = BASE_DIR.resolve(fileName);

                    StringBuilder sb = new StringBuilder();
                    sb.append("복잡한 워크플로우 ID: ").append(id).append("\n");
                    sb.append("시간: ").append(LocalDateTime.now()).append("\n\n");

                    sb.append("API 응답:\n");
                    for (int i = 0; i < apiResponses.size(); i++) {
                        ApiResponse resp = apiResponses.get(i);
                        sb.append("  ").append(i + 1).append(". ID: ").append(resp.getId())
                                .append(", 제목: ").append(resp.getTitle()).append("\n");
                    }

                    sb.append("\n기존 사용자: ").append(existingUser != null ? existingUser.getUsername() : "없음").append("\n");
                    sb.append("새 사용자 ID: ").append(newUser.getId()).append("\n");
                    sb.append("새 제품 ID: ").append(newProduct.getId()).append("\n");

                    Files.writeString(filePath, sb.toString(), StandardCharsets.UTF_8);
                    log.info("복잡한 워크플로우 {}: 파일 작업 완료", id);

                    return "복잡한 워크플로우 " + id + " 완료, 파일: " + fileName;
                }
        );
    }

    /**
     * 플랫폼 스레드를 사용한 복잡한 워크플로우
     */
    @Async("platformThreadExecutor")
    public CompletableFuture<ExecutionResult<String>> runComplexWorkflowWithPlatformThread(int id) {
        return ThreadUtils.executeWithMetrics(
                "복잡한 워크플로우 (플랫폼) - " + id,
                () -> {
                    // 1. 다중 API 호출
                    log.info("복잡한 워크플로우 {}: 다중 API 호출 시작", id);
                    List<CompletableFuture<ApiResponse>> apiFutures = new ArrayList<>();
                    for (int i = 1; i <= 3; i++) {
                        final int apiId = id * 10 + i;
                        apiFutures.add(CompletableFuture.supplyAsync(() ->
                                webClientBuilder.build()
                                        .get()
                                        .uri("https://jsonplaceholder.typicode.com/posts/" + apiId)
                                        .retrieve()
                                        .bodyToMono(ApiResponse.class)
                                        .block()
                        ));
                    }
                    List<ApiResponse> apiResponses = apiFutures.stream()
                            .map(CompletableFuture::join)
                            .toList();
                    log.info("복잡한 워크플로우 {}: 다중 API 호출 완료", id);

                    // 2. DB 조회 및 저장
                    log.info("복잡한 워크플로우 {}: DB 작업 시작", id);
                    // 기존 사용자 조회
                    User existingUser = null;
                    try {
                        existingUser = userRepository.findById((long)id).orElse(null);
                    } catch (Exception e) {
                        log.warn("사용자 조회 실패, 새 사용자 생성: {}", e.getMessage());
                    }

                    // 새 사용자 저장
                    User newUser = new User("복잡워크플로우사용자" + id, "complex" + id + "@example.com");
                    userRepository.save(newUser);

                    // 새 제품 저장
                    Product newProduct = new Product("복잡워크플로우제품" + id, (double)(1000 * id));
                    productRepository.save(newProduct);
                    log.info("복잡한 워크플로우 {}: DB 작업 완료", id);

                    // 3. 파일 작업
                    log.info("복잡한 워크플로우 {}: 파일 작업 시작", id);
                    String fileName = "complex-workflow-" + id + "-" + UUID.randomUUID() + ".txt";
                    Path filePath = BASE_DIR.resolve(fileName);

                    StringBuilder sb = new StringBuilder();
                    sb.append("복잡한 워크플로우 ID: ").append(id).append("\n");
                    sb.append("시간: ").append(LocalDateTime.now()).append("\n\n");

                    sb.append("API 응답:\n");
                    for (int i = 0; i < apiResponses.size(); i++) {
                        ApiResponse resp = apiResponses.get(i);
                        sb.append("  ").append(i + 1).append(". ID: ").append(resp.getId())
                                .append(", 제목: ").append(resp.getTitle()).append("\n");
                    }

                    sb.append("\n기존 사용자: ").append(existingUser != null ? existingUser.getUsername() : "없음").append("\n");
                    sb.append("새 사용자 ID: ").append(newUser.getId()).append("\n");
                    sb.append("새 제품 ID: ").append(newProduct.getId()).append("\n");

                    Files.writeString(filePath, sb.toString(), StandardCharsets.UTF_8);
                    log.info("복잡한 워크플로우 {}: 파일 작업 완료", id);

                    return "복잡한 워크플로우 " + id + " 완료, 파일: " + fileName;
                }
        );
    }

    /**
     * 여러 간단한 워크플로우 실행 (가상 스레드)
     */
    public List<ExecutionResult<String>> runMultipleSimpleWorkflowsWithVirtualThread(int count) {
        List<CompletableFuture<ExecutionResult<String>>> futures = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            futures.add(runSimpleWorkflowWithVirtualThread(i));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * 여러 간단한 워크플로우 실행 (플랫폼 스레드)
     */
    public List<ExecutionResult<String>> runMultipleSimpleWorkflowsWithPlatformThread(int count) {
        List<CompletableFuture<ExecutionResult<String>>> futures = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            futures.add(runSimpleWorkflowWithPlatformThread(i));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * 여러 복잡한 워크플로우 실행 (가상 스레드)
     */
    public List<ExecutionResult<String>> runMultipleComplexWorkflowsWithVirtualThread(int count) {
        List<CompletableFuture<ExecutionResult<String>>> futures = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            futures.add(runComplexWorkflowWithVirtualThread(i));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * 여러 복잡한 워크플로우 실행 (플랫폼 스레드)
     */
    public List<ExecutionResult<String>> runMultipleComplexWorkflowsWithPlatformThread(int count) {
        List<CompletableFuture<ExecutionResult<String>>> futures = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            futures.add(runComplexWorkflowWithPlatformThread(i));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }
}
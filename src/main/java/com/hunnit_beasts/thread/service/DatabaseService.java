package com.hunnit_beasts.thread.service;

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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class DatabaseService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private static final int QUERY_DELAY_MS = 500;

    /**
     * 테스트용 데이터 초기화
     */
    @Transactional
    public void initializeTestData() {
        log.info("테스트 데이터 초기화 중...");

        // 기존 데이터 삭제
        userRepository.deleteAll();
        productRepository.deleteAll();

        // 사용자 데이터 생성
        List<User> users = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> new User("사용자" + i, "user" + i + "@example.com"))
                .toList();
        userRepository.saveAll(users);

        // 상품 데이터 생성
        List<Product> products = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> new Product("상품" + i, (double) (1000 * i)))
                .toList();
        productRepository.saveAll(products);

        log.info("테스트 데이터 초기화 완료: 사용자 {} 개, 상품 {} 개", users.size(), products.size());
    }

    /**
     * 단일 사용자 조회
     */
    public User getSingleUser(Long id) {
        return ThreadUtils.measureExecutionTime(
                "단일 사용자 조회",
                () -> userRepository.findByIdWithDelay(id, QUERY_DELAY_MS)
        );
    }

    /**
     * 가상 스레드로 사용자 조회
     */
    @Async("virtualThreadExecutor")
    public CompletableFuture<ExecutionResult<User>> getUserWithVirtualThread(Long id) {
        return ThreadUtils.executeWithMetrics(
                "사용자 조회 (가상) - " + id,
                () -> userRepository.findByIdWithDelay(id, QUERY_DELAY_MS)
        );
    }

    /**
     * 플랫폼 스레드로 사용자 조회
     */
    @Async("platformThreadExecutor")
    public CompletableFuture<ExecutionResult<User>> getUserWithPlatformThread(Long id) {
        return ThreadUtils.executeWithMetrics(
                "사용자 조회 (플랫폼) - " + id,
                () -> userRepository.findByIdWithDelay(id, QUERY_DELAY_MS)
        );
    }

    /**
     * 여러 사용자를 가상 스레드로 동시 조회
     */
    public List<ExecutionResult<User>> batchGetUsersWithVirtualThread(int count) {
        List<CompletableFuture<ExecutionResult<User>>> futures = new ArrayList<>();

        for (long i = 1; i <= count; i++) {
            futures.add(getUserWithVirtualThread(i));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * 여러 사용자를 플랫폼 스레드로 동시 조회
     */
    public List<ExecutionResult<User>> batchGetUsersWithPlatformThread(int count) {
        List<CompletableFuture<ExecutionResult<User>>> futures = new ArrayList<>();

        for (long i = 1; i <= count; i++) {
            futures.add(getUserWithPlatformThread(i));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }
}
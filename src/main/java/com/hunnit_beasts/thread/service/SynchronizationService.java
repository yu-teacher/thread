package com.hunnit_beasts.thread.service;

import com.hunnit_beasts.thread.util.ExecutionResult;
import com.hunnit_beasts.thread.util.ThreadUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class SynchronizationService {

    private final Object lock = new Object();
    private final ReentrantLock reentrantLock = new ReentrantLock();

    /**
     * synchronized 블록을 사용한 메서드 (가상 스레드)
     */
    @Async("virtualThreadExecutor")
    public CompletableFuture<ExecutionResult<String>> processSynchronizedBlockWithVirtualThread(
            int id, long sleepTimeMs) {
        return ThreadUtils.executeWithMetrics(
                "동기화 블록 (가상) - " + id,
                () -> {
                    synchronized (lock) {
                        log.info("스레드 {}가 동기화 블록 진입 (가상: {})",
                                Thread.currentThread().getName(),
                                Thread.currentThread().isVirtual());

                        // I/O 작업이나 긴 대기 시간을 시뮬레이션
                        try {
                            Thread.sleep(sleepTimeMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("스레드가 중단되었습니다", e);
                        }

                        log.info("스레드 {}가 동기화 블록 종료 (가상: {})",
                                Thread.currentThread().getName(),
                                Thread.currentThread().isVirtual());

                        return "synchronized 블록 처리 완료 - " + id;
                    }
                }
        );
    }

    /**
     * ReentrantLock을 사용한 메서드 (가상 스레드)
     */
    @Async("virtualThreadExecutor")
    public CompletableFuture<ExecutionResult<String>> processReentrantLockWithVirtualThread(
            int id, long sleepTimeMs) {
        return ThreadUtils.executeWithMetrics(
                "리엔트런트 락 (가상) - " + id,
                () -> {
                    try {
                        reentrantLock.lock();
                        log.info("스레드 {}가 리엔트런트 락 획득 (가상: {})",
                                Thread.currentThread().getName(),
                                Thread.currentThread().isVirtual());

                        // I/O 작업이나 긴 대기 시간을 시뮬레이션
                        try {
                            Thread.sleep(sleepTimeMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("스레드가 중단되었습니다", e);
                        }

                        log.info("스레드 {}가 리엔트런트 락 해제 (가상: {})",
                                Thread.currentThread().getName(),
                                Thread.currentThread().isVirtual());

                        return "ReentrantLock 처리 완료 - " + id;
                    } finally {
                        reentrantLock.unlock();
                    }
                }
        );
    }

    /**
     * synchronized 블록을 사용한 메서드 (플랫폼 스레드)
     */
    @Async("platformThreadExecutor")
    public CompletableFuture<ExecutionResult<String>> processSynchronizedBlockWithPlatformThread(
            int id, long sleepTimeMs) {
        return ThreadUtils.executeWithMetrics(
                "동기화 블록 (플랫폼) - " + id,
                () -> {
                    synchronized (lock) {
                        log.info("스레드 {}가 동기화 블록 진입 (가상: {})",
                                Thread.currentThread().getName(),
                                Thread.currentThread().isVirtual());

                        // I/O 작업이나 긴 대기 시간을 시뮬레이션
                        try {
                            Thread.sleep(sleepTimeMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("스레드가 중단되었습니다", e);
                        }

                        log.info("스레드 {}가 동기화 블록 종료 (가상: {})",
                                Thread.currentThread().getName(),
                                Thread.currentThread().isVirtual());

                        return "synchronized 블록 처리 완료 - " + id;
                    }
                }
        );
    }

    /**
     * ReentrantLock을 사용한 메서드 (플랫폼 스레드)
     */
    @Async("platformThreadExecutor")
    public CompletableFuture<ExecutionResult<String>> processReentrantLockWithPlatformThread(
            int id, long sleepTimeMs) {
        return ThreadUtils.executeWithMetrics(
                "리엔트런트 락 (플랫폼) - " + id,
                () -> {
                    try {
                        reentrantLock.lock();
                        log.info("스레드 {}가 리엔트런트 락 획득 (가상: {})",
                                Thread.currentThread().getName(),
                                Thread.currentThread().isVirtual());

                        // I/O 작업이나 긴 대기 시간을 시뮬레이션
                        try {
                            Thread.sleep(sleepTimeMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("스레드가 중단되었습니다", e);
                        }

                        log.info("스레드 {}가 리엔트런트 락 해제 (가상: {})",
                                Thread.currentThread().getName(),
                                Thread.currentThread().isVirtual());

                        return "ReentrantLock 처리 완료 - " + id;
                    } finally {
                        reentrantLock.unlock();
                    }
                }
        );
    }

    /**
     * 여러 작업을 synchronized 블록으로 처리 (가상 스레드)
     */
    public List<ExecutionResult<String>> processSynchronizedBlocksWithVirtualThread(int count, long sleepTimeMs) {
        List<CompletableFuture<ExecutionResult<String>>> futures = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            futures.add(processSynchronizedBlockWithVirtualThread(i, sleepTimeMs));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * 여러 작업을 ReentrantLock으로 처리 (가상 스레드)
     */
    public List<ExecutionResult<String>> processReentrantLocksWithVirtualThread(int count, long sleepTimeMs) {
        List<CompletableFuture<ExecutionResult<String>>> futures = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            futures.add(processReentrantLockWithVirtualThread(i, sleepTimeMs));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * 여러 작업을 synchronized 블록으로 처리 (플랫폼 스레드)
     */
    public List<ExecutionResult<String>> processSynchronizedBlocksWithPlatformThread(int count, long sleepTimeMs) {
        List<CompletableFuture<ExecutionResult<String>>> futures = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            futures.add(processSynchronizedBlockWithPlatformThread(i, sleepTimeMs));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * 여러 작업을 ReentrantLock으로 처리 (플랫폼 스레드)
     */
    public List<ExecutionResult<String>> processReentrantLocksWithPlatformThread(int count, long sleepTimeMs) {
        List<CompletableFuture<ExecutionResult<String>>> futures = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            futures.add(processReentrantLockWithPlatformThread(i, sleepTimeMs));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }
}
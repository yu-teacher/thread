package com.hunnit_beasts.thread.util;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Slf4j
public class ThreadUtils {

    /**
     * 작업 실행 시간을 측정하고 로깅하는 메서드
     */
    public static <T> T measureExecutionTime(String taskName, Supplier<T> task) {
        long startTime = System.currentTimeMillis();
        T result = task.get();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("{} 작업이 {}ms 동안 스레드: {} (가상: {})에서 완료되었습니다.",
                taskName, duration, Thread.currentThread().getName(),
                Thread.currentThread().isVirtual());

        return result;
    }

    /**
     * CompletableFuture에 대한 실행 정보를 포함하는 래퍼 메서드
     */
    public static <T> CompletableFuture<ExecutionResult<T>> executeWithMetrics(
            String taskName, Callable<T> task) {

        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            ExecutionResult<T> result = new ExecutionResult<>();
            result.setTaskName(taskName);
            result.setThreadName(Thread.currentThread().getName());
            result.setThreadId(Thread.currentThread().threadId());
            result.setVirtualThread(Thread.currentThread().isVirtual());

            try {
                T taskResult = task.call();
                result.setSuccess(true);
                result.setResult(taskResult);
            } catch (Exception e) {
                result.setSuccess(false);
                result.setErrorMessage(e.getMessage());
                log.error("작업 {} 실행 중 오류 발생: {}", taskName, e.getMessage(), e);
            }

            long endTime = System.currentTimeMillis();
            result.setExecutionTimeMs(endTime - startTime);

            log.info("작업 '{}'이(가) {}ms 동안 스레드: {} (가상: {})에서 완료되었습니다.",
                    taskName, result.getExecutionTimeMs(), result.getThreadName(),
                    result.isVirtualThread());

            return result;
        });
    }
}
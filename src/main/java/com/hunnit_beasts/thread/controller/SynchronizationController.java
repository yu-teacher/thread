package com.hunnit_beasts.thread.controller;

import com.hunnit_beasts.thread.service.SynchronizationService;
import com.hunnit_beasts.thread.util.ComparisonResult;
import com.hunnit_beasts.thread.util.ExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sync")
@Slf4j
@RequiredArgsConstructor
public class SynchronizationController {

    private final SynchronizationService synchronizationService;

    @GetMapping("/synchronized")
    public List<ExecutionResult<String>> testSynchronizedBlock(
            @RequestParam(defaultValue = "20") int count,
            @RequestParam(defaultValue = "100") long sleepTimeMs) {
        log.info("synchronized 블록 테스트 요청, 개수: {}, 대기 시간: {}ms", count, sleepTimeMs);
        return synchronizationService.processSynchronizedBlocksWithVirtualThread(count, sleepTimeMs);
    }

    @GetMapping("/reentrant-lock")
    public List<ExecutionResult<String>> testReentrantLock(
            @RequestParam(defaultValue = "20") int count,
            @RequestParam(defaultValue = "100") long sleepTimeMs) {
        log.info("ReentrantLock 테스트 요청, 개수: {}, 대기 시간: {}ms", count, sleepTimeMs);
        return synchronizationService.processReentrantLocksWithVirtualThread(count, sleepTimeMs);
    }

    @GetMapping("/compare")
    public ComparisonResult<String> compareSynchronizationMethods(
            @RequestParam(defaultValue = "20") int count,
            @RequestParam(defaultValue = "100") long sleepTimeMs) {
        log.info("동기화 방법 비교 요청, 개수: {}, 대기 시간: {}ms", count, sleepTimeMs);

        // 가상 스레드 + synchronized
        long startTimeVSynchronized = System.currentTimeMillis();
        List<ExecutionResult<String>> vSynchronizedResults =
                synchronizationService.processSynchronizedBlocksWithVirtualThread(count, sleepTimeMs);
        long totalTimeVSynchronized = System.currentTimeMillis() - startTimeVSynchronized;

        // 가상 스레드 + ReentrantLock
        long startTimeVReentrant = System.currentTimeMillis();
        List<ExecutionResult<String>> vReentrantResults =
                synchronizationService.processReentrantLocksWithVirtualThread(count, sleepTimeMs);
        long totalTimeVReentrant = System.currentTimeMillis() - startTimeVReentrant;

        // 플랫폼 스레드 + synchronized
        long startTimePSynchronized = System.currentTimeMillis();
        List<ExecutionResult<String>> pSynchronizedResults =
                synchronizationService.processSynchronizedBlocksWithPlatformThread(count, sleepTimeMs);
        long totalTimePSynchronized = System.currentTimeMillis() - startTimePSynchronized;

        // 플랫폼 스레드 + ReentrantLock
        long startTimePReentrant = System.currentTimeMillis();
        List<ExecutionResult<String>> pReentrantResults =
                synchronizationService.processReentrantLocksWithPlatformThread(count, sleepTimeMs);
        long totalTimePReentrant = System.currentTimeMillis() - startTimePReentrant;

        // 결과 매핑
        ComparisonResult<String> result = new ComparisonResult<>("동기화 방법 비교", count);
        result.setVirtualThreadResults(vSynchronizedResults);
        result.setPlatformThreadResults(pSynchronizedResults);
        result.setVirtualThreadTotalTimeMs(totalTimeVSynchronized);
        result.setPlatformThreadTotalTimeMs(totalTimePSynchronized);
        result.calculateSpeedup();

        // 추가 정보 로깅
        log.info("동기화 비교 결과:");
        log.info("가상 스레드 + synchronized: {}ms", totalTimeVSynchronized);
        log.info("가상 스레드 + ReentrantLock: {}ms", totalTimeVReentrant);
        log.info("플랫폼 스레드 + synchronized: {}ms", totalTimePSynchronized);
        log.info("플랫폼 스레드 + ReentrantLock: {}ms", totalTimePReentrant);
        log.info("핀닝 효과 (가상): {}ms", totalTimeVSynchronized - totalTimeVReentrant);

        // 추가 정보를 위한 핀닝 감지 안내
        log.info("핀닝 감지를 위해 -Djdk.tracePinnedThreads=full JVM 옵션을 사용하세요.");

        return result;
    }
}
package com.hunnit_beasts.thread.controller;

import com.hunnit_beasts.thread.service.StressTestService;
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
@RequestMapping("/stress")
@Slf4j
@RequiredArgsConstructor
public class StressTestController {

    private final StressTestService stressTestService;

    @GetMapping("/simple")
    public List<ExecutionResult<String>> processSimpleRequests(
            @RequestParam(defaultValue = "1000") int count) {
        log.info("간단한 요청 처리 중, 요청 수: {}", count);
        return stressTestService.processMultipleRequestsWithVirtualThread(count);
    }

    @GetMapping("/delayed")
    public List<ExecutionResult<String>> processDelayedRequests(
            @RequestParam(defaultValue = "1000") int count,
            @RequestParam(defaultValue = "100") long delayMs) {
        log.info("지연 요청 처리 중, 요청 수: {}, 지연: {}ms", count, delayMs);
        return stressTestService.processDelayedRequestsWithVirtualThread(count, delayMs);
    }

    @GetMapping("/compare")
    public ComparisonResult<String> compareRequestProcessing(
            @RequestParam(defaultValue = "1000") int count,
            @RequestParam(defaultValue = "50") long delayMs) {
        log.info("요청 처리 성능 비교 중, 요청 수: {}, 지연: {}ms", count, delayMs);

        // 가상 스레드 테스트
        long startTimeVirtual = System.currentTimeMillis();
        List<ExecutionResult<String>> virtualResults =
                stressTestService.processDelayedRequestsWithVirtualThread(count, delayMs);
        long totalTimeVirtual = System.currentTimeMillis() - startTimeVirtual;

        // 플랫폼 스레드 테스트
        long startTimePlatform = System.currentTimeMillis();
        List<ExecutionResult<String>> platformResults =
                stressTestService.processDelayedRequestsWithPlatformThread(count, delayMs);
        long totalTimePlatform = System.currentTimeMillis() - startTimePlatform;

        // 결과 생성
        ComparisonResult<String> result = new ComparisonResult<>("대용량 요청 처리 비교", count);
        result.setVirtualThreadResults(virtualResults);
        result.setPlatformThreadResults(platformResults);
        result.setVirtualThreadTotalTimeMs(totalTimeVirtual);
        result.setPlatformThreadTotalTimeMs(totalTimePlatform);
        result.calculateSpeedup();

        log.info("요청 처리 비교 결과 - 가상: {}ms, 플랫폼: {}ms, 속도 향상: {}배",
                totalTimeVirtual, totalTimePlatform, result.getSpeedupFactor());

        return result;
    }
}
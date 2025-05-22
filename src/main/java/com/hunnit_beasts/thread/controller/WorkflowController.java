package com.hunnit_beasts.thread.controller;

import com.hunnit_beasts.thread.service.WorkflowService;
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
@RequestMapping("/workflow")
@Slf4j
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping("/simple")
    public List<ExecutionResult<String>> runSimpleWorkflows(
            @RequestParam(defaultValue = "5") int count) {
        log.info("간단한 워크플로우 실행 요청, 개수: {}", count);
        return workflowService.runMultipleSimpleWorkflowsWithVirtualThread(count);
    }

    @GetMapping("/complex")
    public List<ExecutionResult<String>> runComplexWorkflows(
            @RequestParam(defaultValue = "3") int count) {
        log.info("복잡한 워크플로우 실행 요청, 개수: {}", count);
        return workflowService.runMultipleComplexWorkflowsWithVirtualThread(count);
    }

    @GetMapping("/compare")
    public ComparisonResult<String> compareWorkflows(
            @RequestParam(defaultValue = "simple") String type,
            @RequestParam(defaultValue = "5") int count) {
        log.info("워크플로우 비교 요청, 유형: {}, 개수: {}", type, count);

        long startTimeVirtual = System.currentTimeMillis();
        List<ExecutionResult<String>> virtualResults;
        List<ExecutionResult<String>> platformResults;

        if ("complex".equalsIgnoreCase(type)) {
            // 복잡한 워크플로우 비교
            virtualResults = workflowService.runMultipleComplexWorkflowsWithVirtualThread(count);
            long totalTimeVirtual = System.currentTimeMillis() - startTimeVirtual;

            long startTimePlatform = System.currentTimeMillis();
            platformResults = workflowService.runMultipleComplexWorkflowsWithPlatformThread(count);
            long totalTimePlatform = System.currentTimeMillis() - startTimePlatform;

            ComparisonResult<String> result = new ComparisonResult<>("복잡한 워크플로우 비교", count);
            result.setVirtualThreadResults(virtualResults);
            result.setPlatformThreadResults(platformResults);
            result.setVirtualThreadTotalTimeMs(totalTimeVirtual);
            result.setPlatformThreadTotalTimeMs(totalTimePlatform);
            result.calculateSpeedup();

            log.info("복잡한 워크플로우 비교 결과 - 가상: {}ms, 플랫폼: {}ms, 속도 향상: {}배",
                    totalTimeVirtual, totalTimePlatform, result.getSpeedupFactor());

            return result;
        } else {
            // 간단한 워크플로우 비교
            virtualResults = workflowService.runMultipleSimpleWorkflowsWithVirtualThread(count);
            long totalTimeVirtual = System.currentTimeMillis() - startTimeVirtual;

            long startTimePlatform = System.currentTimeMillis();
            platformResults = workflowService.runMultipleSimpleWorkflowsWithPlatformThread(count);
            long totalTimePlatform = System.currentTimeMillis() - startTimePlatform;

            ComparisonResult<String> result = new ComparisonResult<>("간단한 워크플로우 비교", count);
            result.setVirtualThreadResults(virtualResults);
            result.setPlatformThreadResults(platformResults);
            result.setVirtualThreadTotalTimeMs(totalTimeVirtual);
            result.setPlatformThreadTotalTimeMs(totalTimePlatform);
            result.calculateSpeedup();

            log.info("간단한 워크플로우 비교 결과 - 가상: {}ms, 플랫폼: {}ms, 속도 향상: {}배",
                    totalTimeVirtual, totalTimePlatform, result.getSpeedupFactor());

            return result;
        }
    }
}
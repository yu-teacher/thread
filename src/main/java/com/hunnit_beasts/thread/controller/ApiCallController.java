package com.hunnit_beasts.thread.controller;

import com.hunnit_beasts.thread.model.ApiResponse;
import com.hunnit_beasts.thread.service.ApiCallService;
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
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class ApiCallController {

    private final ApiCallService apiCallService;

    @GetMapping("/single")
    public ApiResponse singleApiCall() {
        log.info("단일 API 호출 요청 처리 중");
        return apiCallService.callSingleApi(1);
    }

    @GetMapping("/multiple")
    public List<ExecutionResult<ApiResponse>> multipleApiCalls(
            @RequestParam(defaultValue = "10") int count) {
        log.info("다중 API 호출 요청 처리 중, 개수: {}", count);
        return apiCallService.callMultipleApisWithVirtualThread(count);
    }

    @GetMapping("/compare")
    public ComparisonResult<ApiResponse> compareApiCalls(
            @RequestParam(defaultValue = "50") int count) {
        log.info("API 호출 성능 비교 중, 개수: {}", count);

        long startTimeVirtual = System.currentTimeMillis();
        List<ExecutionResult<ApiResponse>> virtualResults =
                apiCallService.callMultipleApisWithVirtualThread(count);
        long totalTimeVirtual = System.currentTimeMillis() - startTimeVirtual;

        long startTimePlatform = System.currentTimeMillis();
        List<ExecutionResult<ApiResponse>> platformResults =
                apiCallService.callMultipleApisWithPlatformThread(count);
        long totalTimePlatform = System.currentTimeMillis() - startTimePlatform;

        ComparisonResult<ApiResponse> result = new ComparisonResult<>("API 호출 비교", count);
        result.setVirtualThreadResults(virtualResults);
        result.setPlatformThreadResults(platformResults);
        result.setVirtualThreadTotalTimeMs(totalTimeVirtual);
        result.setPlatformThreadTotalTimeMs(totalTimePlatform);
        result.calculateSpeedup();

        return result;
    }
}
package com.hunnit_beasts.thread.controller;

import com.hunnit_beasts.thread.model.User;
import com.hunnit_beasts.thread.service.DatabaseService;
import com.hunnit_beasts.thread.util.ComparisonResult;
import com.hunnit_beasts.thread.util.ExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/database")
@Slf4j
@RequiredArgsConstructor
public class DatabaseController {

    private final DatabaseService databaseService;

    @PostMapping("/init")
    public String initializeData() {
        log.info("데이터베이스 초기화 요청 수신");
        databaseService.initializeTestData();
        return "테스트 데이터가 성공적으로 초기화되었습니다.";
    }

    @GetMapping("/single/{id}")
    public User getSingleUser(@PathVariable Long id) {
        log.info("단일 사용자 조회 요청 수신, ID: {}", id);
        return databaseService.getSingleUser(id);
    }

    @GetMapping("/batch")
    public List<ExecutionResult<User>> batchGetUsers(
            @RequestParam(defaultValue = "10") int count) {
        log.info("배치 사용자 조회 요청 수신, 개수: {}", count);
        return databaseService.batchGetUsersWithVirtualThread(count);
    }

    @GetMapping("/compare")
    public ComparisonResult<User> compareDbQueries(
            @RequestParam(defaultValue = "50") int count) {
        log.info("데이터베이스 쿼리 성능 비교 중, 개수: {}", count);

        long startTimeVirtual = System.currentTimeMillis();
        List<ExecutionResult<User>> virtualResults =
                databaseService.batchGetUsersWithVirtualThread(count);
        long totalTimeVirtual = System.currentTimeMillis() - startTimeVirtual;

        long startTimePlatform = System.currentTimeMillis();
        List<ExecutionResult<User>> platformResults =
                databaseService.batchGetUsersWithPlatformThread(count);
        long totalTimePlatform = System.currentTimeMillis() - startTimePlatform;

        ComparisonResult<User> result = new ComparisonResult<>("데이터베이스 쿼리 비교", count);
        result.setVirtualThreadResults(virtualResults);
        result.setPlatformThreadResults(platformResults);
        result.setVirtualThreadTotalTimeMs(totalTimeVirtual);
        result.setPlatformThreadTotalTimeMs(totalTimePlatform);
        result.calculateSpeedup();

        return result;
    }
}
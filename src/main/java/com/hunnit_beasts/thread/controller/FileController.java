package com.hunnit_beasts.thread.controller;

import com.hunnit_beasts.thread.service.FileService;
import com.hunnit_beasts.thread.util.ComparisonResult;
import com.hunnit_beasts.thread.util.ExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/files")
@Slf4j
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private static final int DEFAULT_FILE_SIZE_KB = 100;

    @PostMapping("/write")
    public String writeFile(
            @RequestParam(defaultValue = "test.txt") String fileName,
            @RequestParam(defaultValue = "100") int sizeKb) {
        log.info("파일 쓰기 요청 수신, 이름: {}, 크기: {}KB", fileName, sizeKb);
        return fileService.writeFile(fileName, sizeKb);
    }

    @GetMapping("/read/{fileName}")
    public String readFile(@PathVariable String fileName) {
        log.info("파일 읽기 요청 수신, 이름: {}", fileName);
        return fileService.readFile(fileName);
    }

    @GetMapping("/batch")
    public List<ExecutionResult<String>> batchProcessFiles(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(defaultValue = "100") int sizeKb) {
        log.info("배치 파일 처리 요청 수신, 개수: {}, 크기: {}KB", count, sizeKb);
        return fileService.batchProcessFilesWithVirtualThread(count, sizeKb);
    }

    @GetMapping("/compare")
    public ComparisonResult<String> compareFileProcessing(
            @RequestParam(defaultValue = "20") int count,
            @RequestParam(defaultValue = "100") int sizeKb) {
        log.info("파일 처리 성능 비교 중, 개수: {}, 크기: {}KB", count, sizeKb);

        long startTimeVirtual = System.currentTimeMillis();
        List<ExecutionResult<String>> virtualResults =
                fileService.batchProcessFilesWithVirtualThread(count, sizeKb);
        long totalTimeVirtual = System.currentTimeMillis() - startTimeVirtual;

        long startTimePlatform = System.currentTimeMillis();
        List<ExecutionResult<String>> platformResults =
                fileService.batchProcessFilesWithPlatformThread(count, sizeKb);
        long totalTimePlatform = System.currentTimeMillis() - startTimePlatform;

        ComparisonResult<String> result = new ComparisonResult<>("파일 처리 비교", count);
        result.setVirtualThreadResults(virtualResults);
        result.setPlatformThreadResults(platformResults);
        result.setVirtualThreadTotalTimeMs(totalTimeVirtual);
        result.setPlatformThreadTotalTimeMs(totalTimePlatform);
        result.calculateSpeedup();

        return result;
    }
}
package com.hunnit_beasts.thread.service;

import com.hunnit_beasts.thread.util.ExecutionResult;
import com.hunnit_beasts.thread.util.ThreadUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class FileService {

    private static final Path BASE_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "virtual-threads-test");
    private static final int DEFAULT_FILE_SIZE_KB = 100;

    public FileService() {
        try {
            if (!Files.exists(BASE_DIR)) {
                Files.createDirectories(BASE_DIR);
            }
            log.info("파일 서비스 초기화 완료. 기본 디렉토리: {}", BASE_DIR);
        } catch (IOException e) {
            log.error("기본 디렉토리 생성 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 테스트 데이터로 파일 생성
     */
    public String writeFile(String fileName, int sizeKb) {
        Path filePath = BASE_DIR.resolve(fileName);

        try {
            String content = generateContent(sizeKb);
            Files.writeString(filePath, content, StandardCharsets.UTF_8);
            log.info("파일이 생성되었습니다: {}, 크기: {}KB", filePath, sizeKb);
            return filePath.toString();
        } catch (IOException e) {
            log.error("파일 쓰기 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("파일 쓰기 오류: " + e.getMessage(), e);
        }
    }

    /**
     * 파일 읽기
     */
    public String readFile(String fileName) {
        Path filePath = BASE_DIR.resolve(fileName);

        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            log.info("파일을 읽었습니다: {}, 크기: {}KB", filePath, content.length() / 1024);
            return content;
        } catch (IOException e) {
            log.error("파일 읽기 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("파일 읽기 오류: " + e.getMessage(), e);
        }
    }

    /**
     * 가상 스레드로 파일 읽기
     */
    @Async("virtualThreadExecutor")
    public CompletableFuture<ExecutionResult<String>> readFileWithVirtualThread(String fileName) {
        return ThreadUtils.executeWithMetrics(
                "파일 읽기 (가상) - " + fileName,
                () -> {
                    Path filePath = BASE_DIR.resolve(fileName);
                    return Files.readString(filePath, StandardCharsets.UTF_8);
                }
        );
    }

    /**
     * 플랫폼 스레드로 파일 읽기
     */
    @Async("platformThreadExecutor")
    public CompletableFuture<ExecutionResult<String>> readFileWithPlatformThread(String fileName) {
        return ThreadUtils.executeWithMetrics(
                "파일 읽기 (플랫폼) - " + fileName,
                () -> {
                    Path filePath = BASE_DIR.resolve(fileName);
                    return Files.readString(filePath, StandardCharsets.UTF_8);
                }
        );
    }

    /**
     * 가상 스레드로 파일 쓰기
     */
    @Async("virtualThreadExecutor")
    public CompletableFuture<ExecutionResult<String>> writeFileWithVirtualThread(int sizeKb) {
        return ThreadUtils.executeWithMetrics(
                "파일 쓰기 (가상) - " + sizeKb + "KB",
                () -> {
                    String fileName = "virtual-" + UUID.randomUUID() + ".txt";
                    Path filePath = BASE_DIR.resolve(fileName);
                    String content = generateContent(sizeKb);
                    Files.writeString(filePath, content, StandardCharsets.UTF_8);
                    return fileName;
                }
        );
    }

    /**
     * 플랫폼 스레드로 파일 쓰기
     */
    @Async("platformThreadExecutor")
    public CompletableFuture<ExecutionResult<String>> writeFileWithPlatformThread(int sizeKb) {
        return ThreadUtils.executeWithMetrics(
                "파일 쓰기 (플랫폼) - " + sizeKb + "KB",
                () -> {
                    String fileName = "platform-" + UUID.randomUUID() + ".txt";
                    Path filePath = BASE_DIR.resolve(fileName);
                    String content = generateContent(sizeKb);
                    Files.writeString(filePath, content, StandardCharsets.UTF_8);
                    return fileName;
                }
        );
    }

    /**
     * 여러 파일을 가상 스레드로 처리
     */
    public List<ExecutionResult<String>> batchProcessFilesWithVirtualThread(int count, int sizeKb) {
        List<CompletableFuture<ExecutionResult<String>>> futures = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            futures.add(writeFileWithVirtualThread(sizeKb));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * 여러 파일을 플랫폼 스레드로 처리
     */
    public List<ExecutionResult<String>> batchProcessFilesWithPlatformThread(int count, int sizeKb) {
        List<CompletableFuture<ExecutionResult<String>>> futures = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            futures.add(writeFileWithPlatformThread(sizeKb));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * 테스트용 파일 내용 생성
     */
    private String generateContent(int sizeKb) {
        int lineLength = 100; // 한 줄의 문자 수
        int linesCount = sizeKb * 10; // 각 라인이 약 100 바이트라고 가정

        return IntStream.range(0, linesCount)
                .mapToObj(i -> "라인 " + i + ": " +
                        IntStream.range(0, lineLength - 10)
                                .mapToObj(j -> String.valueOf((char) ('가' + (j % 44))))
                                .collect(Collectors.joining()))
                .collect(Collectors.joining("\n"));
    }
}
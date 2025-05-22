package com.hunnit_beasts.thread.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunnit_beasts.thread.service.FileService;
import com.hunnit_beasts.thread.util.ComparisonResult;
import com.hunnit_beasts.thread.util.ExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("📁 파일 I/O 컨트롤러 테스트")
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @Autowired
    private ObjectMapper objectMapper;

    private ExecutionResult<String> mockExecutionResult;
    private ComparisonResult<String> mockComparisonResult;

    @BeforeEach
    void setUp() {
        // Mock 객체 설정
        mockExecutionResult = new ExecutionResult<>();
        mockExecutionResult.setTaskName("Test File Processing");
        mockExecutionResult.setThreadName("test-thread");
        mockExecutionResult.setThreadId(1L);
        mockExecutionResult.setVirtualThread(true);
        mockExecutionResult.setExecutionTimeMs(150L);
        mockExecutionResult.setSuccess(true);
        mockExecutionResult.setResult("test-file.txt");

        mockComparisonResult = new ComparisonResult<>("파일 처리 비교", 20);
        mockComparisonResult.setVirtualThreadResults(Arrays.asList(mockExecutionResult));
        mockComparisonResult.setPlatformThreadResults(Arrays.asList(mockExecutionResult));
        mockComparisonResult.setVirtualThreadTotalTimeMs(600L);
        mockComparisonResult.setPlatformThreadTotalTimeMs(1200L);
        mockComparisonResult.calculateSpeedup();
    }

    @Test
    @DisplayName("💾 파일 생성 - 50KB 크기의 테스트 파일 생성")
    void testWriteFile() throws Exception {
        // Given
        when(fileService.writeFile(anyString(), anyInt())).thenReturn("/tmp/test.txt");

        // When & Then
        mockMvc.perform(post("/files/write")
                        .param("fileName", "test.txt")
                        .param("sizeKb", "50"))
                .andExpect(status().isOk())
                .andExpect(content().string("/tmp/test.txt"));
    }

    @Test
    @DisplayName("💾📋 파일 생성 기본값 - 기본 100KB 파일 생성")
    void testWriteFileWithDefaultParams() throws Exception {
        // Given
        when(fileService.writeFile("test.txt", 100)).thenReturn("/tmp/test.txt");

        // When & Then
        mockMvc.perform(post("/files/write"))
                .andExpect(status().isOk())
                .andExpect(content().string("/tmp/test.txt"));
    }

    @Test
    @DisplayName("📖 파일 읽기 - 기존 파일 내용 읽기")
    void testReadFile() throws Exception {
        // Given
        String mockFileContent = "This is test file content";
        when(fileService.readFile(anyString())).thenReturn(mockFileContent);

        // When & Then
        mockMvc.perform(get("/files/read/test.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string(mockFileContent));
    }

    @Test
    @DisplayName("📁🔢 배치 파일 처리 - 5개 파일(200KB 각각)을 가상 스레드로 동시 처리")
    void testBatchProcessFiles() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(fileService.batchProcessFilesWithVirtualThread(anyInt(), anyInt())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/files/batch")
                        .param("count", "5")
                        .param("sizeKb", "200"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].taskName").value("Test File Processing"));
    }

    @Test
    @DisplayName("📁🔢📋 배치 파일 처리 기본값 - 기본 10개 파일(100KB 각각) 처리")
    void testBatchProcessFilesWithDefaultParams() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(fileService.batchProcessFilesWithVirtualThread(10, 100)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/files/batch"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("⚔️ 파일 처리 성능 비교 - 15개 파일(300KB 각각) 처리로 가상 vs 플랫폼 스레드 성능 측정")
    void testCompareFileProcessing() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(fileService.batchProcessFilesWithVirtualThread(anyInt(), anyInt())).thenReturn(mockResults);
        when(fileService.batchProcessFilesWithPlatformThread(anyInt(), anyInt())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/files/compare")
                        .param("count", "15")
                        .param("sizeKb", "300"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scenarioName").value("파일 처리 비교"))
                .andExpect(jsonPath("$.taskCount").value(15))
                .andExpect(jsonPath("$.speedupFactor").exists());
    }

    @Test
    @DisplayName("⚔️📋 파일 처리 성능 비교 기본값 - 기본 20개 파일(100KB 각각)로 벤치마크")
    void testCompareFileProcessingWithDefaultParams() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(fileService.batchProcessFilesWithVirtualThread(20, 100)).thenReturn(mockResults);
        when(fileService.batchProcessFilesWithPlatformThread(20, 100)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/files/compare"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskCount").value(20));
    }
}
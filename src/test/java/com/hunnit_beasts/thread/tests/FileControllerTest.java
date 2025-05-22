package com.hunnit_beasts.thread.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunnit_beasts.thread.service.FileService;
import com.hunnit_beasts.thread.util.ComparisonResult;
import com.hunnit_beasts.thread.util.ExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
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
        // Mock ExecutionResult 생성
        mockExecutionResult = new ExecutionResult<>();
        mockExecutionResult.setTaskName("Test File Processing");
        mockExecutionResult.setThreadName("test-thread");
        mockExecutionResult.setThreadId(1L);
        mockExecutionResult.setVirtualThread(true);
        mockExecutionResult.setExecutionTimeMs(150L);
        mockExecutionResult.setSuccess(true);
        mockExecutionResult.setResult("test-file.txt");

        // Mock ComparisonResult 생성
        mockComparisonResult = new ComparisonResult<>("파일 처리 비교", 20);
        mockComparisonResult.setVirtualThreadResults(Arrays.asList(mockExecutionResult));
        mockComparisonResult.setPlatformThreadResults(Arrays.asList(mockExecutionResult));
        mockComparisonResult.setVirtualThreadTotalTimeMs(600L);
        mockComparisonResult.setPlatformThreadTotalTimeMs(1200L);
        mockComparisonResult.calculateSpeedup();
    }

    @Test
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
    void testWriteFileWithDefaultParams() throws Exception {
        // Given
        when(fileService.writeFile("test.txt", 100)).thenReturn("/tmp/test.txt");

        // When & Then
        mockMvc.perform(post("/files/write"))
                .andExpect(status().isOk())
                .andExpect(content().string("/tmp/test.txt"));
    }

    @Test
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

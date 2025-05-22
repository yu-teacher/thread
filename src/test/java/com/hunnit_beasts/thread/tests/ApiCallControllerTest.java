package com.hunnit_beasts.thread.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunnit_beasts.thread.controller.ApiCallController;
import com.hunnit_beasts.thread.model.ApiResponse;
import com.hunnit_beasts.thread.service.ApiCallService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("🌐 API 호출 컨트롤러 테스트")
class ApiCallControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApiCallService apiCallService;

    @Autowired
    private ObjectMapper objectMapper;

    private ApiResponse mockApiResponse;
    private ExecutionResult<ApiResponse> mockExecutionResult;
    private ComparisonResult<ApiResponse> mockComparisonResult;

    @BeforeEach
    void setUp() {
        // Mock 객체 설정
        mockApiResponse = new ApiResponse();

        mockExecutionResult = new ExecutionResult<>();
        mockExecutionResult.setTaskName("Test API Call");
        mockExecutionResult.setThreadName("test-thread");
        mockExecutionResult.setThreadId(1L);
        mockExecutionResult.setVirtualThread(true);
        mockExecutionResult.setExecutionTimeMs(100L);
        mockExecutionResult.setSuccess(true);
        mockExecutionResult.setResult(mockApiResponse);

        mockComparisonResult = new ComparisonResult<>("API 호출 비교", 10);
        mockComparisonResult.setVirtualThreadResults(Arrays.asList(mockExecutionResult));
        mockComparisonResult.setPlatformThreadResults(Arrays.asList(mockExecutionResult));
        mockComparisonResult.setVirtualThreadTotalTimeMs(500L);
        mockComparisonResult.setPlatformThreadTotalTimeMs(1000L);
        mockComparisonResult.calculateSpeedup();
    }

    @Test
    @DisplayName("📡 단일 API 호출 - JSONPlaceholder에서 단일 포스트 조회")
    void testSingleApiCall() throws Exception {
        // Given
        when(apiCallService.callSingleApi(1)).thenReturn(mockApiResponse);

        // When & Then
        mockMvc.perform(get("/api/single"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("📡🔢 다중 API 호출 - 5개의 동시 API 호출 처리 (가상 스레드)")
    void testMultipleApiCalls() throws Exception {
        // Given
        List<ExecutionResult<ApiResponse>> mockResults = Arrays.asList(mockExecutionResult);
        when(apiCallService.callMultipleApisWithVirtualThread(anyInt())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/api/multiple")
                        .param("count", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].taskName").value("Test API Call"));
    }

    @Test
    @DisplayName("📡🔢📋 다중 API 호출 기본값 - 기본 10개 요청으로 가상 스레드 동작 확인")
    void testMultipleApiCallsWithDefaultCount() throws Exception {
        // Given
        List<ExecutionResult<ApiResponse>> mockResults = Arrays.asList(mockExecutionResult);
        when(apiCallService.callMultipleApisWithVirtualThread(10)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/api/multiple"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("⚔️ API 호출 성능 비교 - 20개 요청으로 가상 vs 플랫폼 스레드 성능 측정")
    void testCompareApiCalls() throws Exception {
        // Given
        List<ExecutionResult<ApiResponse>> mockResults = Arrays.asList(mockExecutionResult);
        when(apiCallService.callMultipleApisWithVirtualThread(anyInt())).thenReturn(mockResults);
        when(apiCallService.callMultipleApisWithPlatformThread(anyInt())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/api/compare")
                        .param("count", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scenarioName").value("API 호출 비교"))
                .andExpect(jsonPath("$.taskCount").value(20))
                .andExpect(jsonPath("$.speedupFactor").exists());
    }

    @Test
    @DisplayName("⚔️📋 API 호출 성능 비교 기본값 - 기본 50개 요청으로 성능 벤치마크")
    void testCompareApiCallsWithDefaultCount() throws Exception {
        // Given
        List<ExecutionResult<ApiResponse>> mockResults = Arrays.asList(mockExecutionResult);
        when(apiCallService.callMultipleApisWithVirtualThread(50)).thenReturn(mockResults);
        when(apiCallService.callMultipleApisWithPlatformThread(50)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/api/compare"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskCount").value(50));
    }
}
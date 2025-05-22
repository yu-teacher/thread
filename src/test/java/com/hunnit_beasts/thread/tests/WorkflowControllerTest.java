package com.hunnit_beasts.thread.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunnit_beasts.thread.service.WorkflowService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WorkflowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkflowService workflowService;

    @Autowired
    private ObjectMapper objectMapper;

    private ExecutionResult<String> mockSimpleWorkflowResult;
    private ExecutionResult<String> mockComplexWorkflowResult;
    private ComparisonResult<String> mockComparisonResult;

    @BeforeEach
    void setUp() {
        // Mock Simple Workflow Result 생성
        mockSimpleWorkflowResult = new ExecutionResult<>();
        mockSimpleWorkflowResult.setTaskName("간단한 워크플로우 (가상) - 1");
        mockSimpleWorkflowResult.setThreadName("virtual-thread-1");
        mockSimpleWorkflowResult.setThreadId(1L);
        mockSimpleWorkflowResult.setVirtualThread(true);
        mockSimpleWorkflowResult.setExecutionTimeMs(800L);
        mockSimpleWorkflowResult.setSuccess(true);
        mockSimpleWorkflowResult.setResult("간단한 워크플로우 1 완료");

        // Mock Complex Workflow Result 생성
        mockComplexWorkflowResult = new ExecutionResult<>();
        mockComplexWorkflowResult.setTaskName("복잡한 워크플로우 (가상) - 1");
        mockComplexWorkflowResult.setThreadName("virtual-thread-2");
        mockComplexWorkflowResult.setThreadId(2L);
        mockComplexWorkflowResult.setVirtualThread(true);
        mockComplexWorkflowResult.setExecutionTimeMs(2500L);
        mockComplexWorkflowResult.setSuccess(true);
        mockComplexWorkflowResult.setResult("복잡한 워크플로우 1 완료, 파일: complex-workflow-1-uuid.txt");

        // Mock ComparisonResult 생성
        mockComparisonResult = new ComparisonResult<>("간단한 워크플로우 비교", 5);
        mockComparisonResult.setVirtualThreadResults(Arrays.asList(mockSimpleWorkflowResult));
        mockComparisonResult.setPlatformThreadResults(Arrays.asList(mockSimpleWorkflowResult));
        mockComparisonResult.setVirtualThreadTotalTimeMs(3000L);
        mockComparisonResult.setPlatformThreadTotalTimeMs(4500L);
        mockComparisonResult.calculateSpeedup();
    }

    @Test
    void testRunSimpleWorkflows() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockSimpleWorkflowResult);
        when(workflowService.runMultipleSimpleWorkflowsWithVirtualThread(anyInt())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/workflow/simple")
                        .param("count", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].taskName").value("간단한 워크플로우 (가상) - 1"))
                .andExpect(jsonPath("$[0].result").value("간단한 워크플로우 1 완료"));
    }

    @Test
    void testRunSimpleWorkflowsWithDefaultCount() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockSimpleWorkflowResult);
        when(workflowService.runMultipleSimpleWorkflowsWithVirtualThread(5)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/workflow/simple"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testRunComplexWorkflows() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockComplexWorkflowResult);
        when(workflowService.runMultipleComplexWorkflowsWithVirtualThread(anyInt())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/workflow/complex")
                        .param("count", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].taskName").value("복잡한 워크플로우 (가상) - 1"))
                .andExpect(jsonPath("$[0].result").value("복잡한 워크플로우 1 완료, 파일: complex-workflow-1-uuid.txt"));
    }

    @Test
    void testRunComplexWorkflowsWithDefaultCount() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockComplexWorkflowResult);
        when(workflowService.runMultipleComplexWorkflowsWithVirtualThread(3)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/workflow/complex"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCompareSimpleWorkflows() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockSimpleWorkflowResult);
        when(workflowService.runMultipleSimpleWorkflowsWithVirtualThread(anyInt())).thenReturn(mockResults);
        when(workflowService.runMultipleSimpleWorkflowsWithPlatformThread(anyInt())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/workflow/compare")
                        .param("type", "simple")
                        .param("count", "4"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scenarioName").value("간단한 워크플로우 비교"))
                .andExpect(jsonPath("$.taskCount").value(4))
                .andExpect(jsonPath("$.speedupFactor").exists());
    }

    @Test
    void testCompareComplexWorkflows() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockComplexWorkflowResult);
        when(workflowService.runMultipleComplexWorkflowsWithVirtualThread(anyInt())).thenReturn(mockResults);
        when(workflowService.runMultipleComplexWorkflowsWithPlatformThread(anyInt())).thenReturn(mockResults);

        ComparisonResult<String> complexComparisonResult = new ComparisonResult<>("복잡한 워크플로우 비교", 3);
        complexComparisonResult.setVirtualThreadResults(mockResults);
        complexComparisonResult.setPlatformThreadResults(mockResults);
        complexComparisonResult.setVirtualThreadTotalTimeMs(6000L);
        complexComparisonResult.setPlatformThreadTotalTimeMs(9000L);
        complexComparisonResult.calculateSpeedup();

        // When & Then
        mockMvc.perform(get("/workflow/compare")
                        .param("type", "complex")
                        .param("count", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scenarioName").value("복잡한 워크플로우 비교"))
                .andExpect(jsonPath("$.taskCount").value(3))
                .andExpect(jsonPath("$.speedupFactor").exists());
    }

    @Test
    void testCompareWorkflowsWithDefaultParams() throws Exception {
        // Given - 기본값: simple workflow, count=5
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockSimpleWorkflowResult);
        when(workflowService.runMultipleSimpleWorkflowsWithVirtualThread(5)).thenReturn(mockResults);
        when(workflowService.runMultipleSimpleWorkflowsWithPlatformThread(5)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/workflow/compare"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scenarioName").value("간단한 워크플로우 비교"))
                .andExpect(jsonPath("$.taskCount").value(5));
    }

    @Test
    void testCompareInvalidWorkflowType() throws Exception {
        // Given - invalid type, should default to simple
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockSimpleWorkflowResult);
        when(workflowService.runMultipleSimpleWorkflowsWithVirtualThread(anyInt())).thenReturn(mockResults);
        when(workflowService.runMultipleSimpleWorkflowsWithPlatformThread(anyInt())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/workflow/compare")
                        .param("type", "invalid")
                        .param("count", "7"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scenarioName").value("간단한 워크플로우 비교"))
                .andExpect(jsonPath("$.taskCount").value(7));
    }

    @Test
    void testWorkflowWithLargeCount() throws Exception {
        // Given - 큰 개수로 테스트
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockSimpleWorkflowResult);
        when(workflowService.runMultipleSimpleWorkflowsWithVirtualThread(50)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/workflow/simple")
                        .param("count", "50"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testWorkflowPerformanceMetrics() throws Exception {
        // Given - 성능 메트릭 검증을 위한 테스트
        ExecutionResult<String> slowResult = new ExecutionResult<>();
        slowResult.setTaskName("느린 워크플로우");
        slowResult.setExecutionTimeMs(5000L);
        slowResult.setSuccess(true);
        slowResult.setResult("느린 워크플로우 완료");

        List<ExecutionResult<String>> slowResults = Arrays.asList(slowResult);
        when(workflowService.runMultipleSimpleWorkflowsWithVirtualThread(anyInt())).thenReturn(slowResults);

        // When & Then
        mockMvc.perform(get("/workflow/simple")
                        .param("count", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].executionTimeMs").value(5000));
    }

    @Test
    void testWorkflowFailureHandling() throws Exception {
        // Given - 실패한 워크플로우 결과
        ExecutionResult<String> failedResult = new ExecutionResult<>();
        failedResult.setTaskName("실패한 워크플로우");
        failedResult.setExecutionTimeMs(100L);
        failedResult.setSuccess(false);
        failedResult.setErrorMessage("워크플로우 실행 중 오류 발생");

        List<ExecutionResult<String>> failedResults = Arrays.asList(failedResult);
        when(workflowService.runMultipleSimpleWorkflowsWithVirtualThread(anyInt())).thenReturn(failedResults);

        // When & Then
        mockMvc.perform(get("/workflow/simple")
                        .param("count", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].success").value(false))
                .andExpect(jsonPath("$[0].errorMessage").value("워크플로우 실행 중 오류 발생"));
    }
}

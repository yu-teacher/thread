package com.hunnit_beasts.thread.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunnit_beasts.thread.service.SynchronizationService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SynchronizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SynchronizationService synchronizationService;

    @Autowired
    private ObjectMapper objectMapper;

    private ExecutionResult<String> mockExecutionResult;
    private ComparisonResult<String> mockComparisonResult;

    @BeforeEach
    void setUp() {
        // Mock ExecutionResult 생성
        mockExecutionResult = new ExecutionResult<>();
        mockExecutionResult.setTaskName("Test Synchronization");
        mockExecutionResult.setThreadName("test-thread");
        mockExecutionResult.setThreadId(1L);
        mockExecutionResult.setVirtualThread(true);
        mockExecutionResult.setExecutionTimeMs(300L);
        mockExecutionResult.setSuccess(true);
        mockExecutionResult.setResult("synchronized 블록 처리 완료 - 1");

        // Mock ComparisonResult 생성
        mockComparisonResult = new ComparisonResult<>("동기화 방법 비교", 20);
        mockComparisonResult.setVirtualThreadResults(Arrays.asList(mockExecutionResult));
        mockComparisonResult.setPlatformThreadResults(Arrays.asList(mockExecutionResult));
        mockComparisonResult.setVirtualThreadTotalTimeMs(1000L);
        mockComparisonResult.setPlatformThreadTotalTimeMs(800L); // 핀닝으로 인해 가상 스레드가 더 느릴 수 있음
        mockComparisonResult.calculateSpeedup();
    }

    @Test
    void testSynchronizedBlock() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(synchronizationService.processSynchronizedBlocksWithVirtualThread(anyInt(), anyLong())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/sync/synchronized")
                        .param("count", "10")
                        .param("sleepTimeMs", "200"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].taskName").value("Test Synchronization"));
    }

    @Test
    void testSynchronizedBlockWithDefaultParams() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(synchronizationService.processSynchronizedBlocksWithVirtualThread(20, 100L)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/sync/synchronized"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testReentrantLock() throws Exception {
        // Given
        ExecutionResult<String> lockResult = new ExecutionResult<>();
        lockResult.setTaskName("Test ReentrantLock");
        lockResult.setResult("ReentrantLock 처리 완료 - 1");
        lockResult.setSuccess(true);

        List<ExecutionResult<String>> mockResults = Arrays.asList(lockResult);
        when(synchronizationService.processReentrantLocksWithVirtualThread(anyInt(), anyLong())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/sync/reentrant-lock")
                        .param("count", "15")
                        .param("sleepTimeMs", "150"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].taskName").value("Test ReentrantLock"));
    }

    @Test
    void testReentrantLockWithDefaultParams() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(synchronizationService.processReentrantLocksWithVirtualThread(20, 100L)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/sync/reentrant-lock"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCompareSynchronizationMethods() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(synchronizationService.processSynchronizedBlocksWithVirtualThread(anyInt(), anyLong())).thenReturn(mockResults);
        when(synchronizationService.processReentrantLocksWithVirtualThread(anyInt(), anyLong())).thenReturn(mockResults);
        when(synchronizationService.processSynchronizedBlocksWithPlatformThread(anyInt(), anyLong())).thenReturn(mockResults);
        when(synchronizationService.processReentrantLocksWithPlatformThread(anyInt(), anyLong())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/sync/compare")
                        .param("count", "25")
                        .param("sleepTimeMs", "120"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scenarioName").value("동기화 방법 비교"))
                .andExpect(jsonPath("$.taskCount").value(25))
                .andExpect(jsonPath("$.speedupFactor").exists());
    }

    @Test
    void testCompareSynchronizationMethodsWithDefaultParams() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(synchronizationService.processSynchronizedBlocksWithVirtualThread(20, 100L)).thenReturn(mockResults);
        when(synchronizationService.processReentrantLocksWithVirtualThread(20, 100L)).thenReturn(mockResults);
        when(synchronizationService.processSynchronizedBlocksWithPlatformThread(20, 100L)).thenReturn(mockResults);
        when(synchronizationService.processReentrantLocksWithPlatformThread(20, 100L)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/sync/compare"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskCount").value(20));
    }

    @Test
    void testPinningDetection() throws Exception {
        // Given - 핀닝 현상 시뮬레이션
        ExecutionResult<String> pinnedResult = new ExecutionResult<>();
        pinnedResult.setTaskName("Pinned Thread Test");
        pinnedResult.setVirtualThread(true);
        pinnedResult.setExecutionTimeMs(500L); // 더 긴 실행 시간으로 핀닝 효과 시뮬레이션
        pinnedResult.setSuccess(true);
        pinnedResult.setResult("synchronized 블록 처리 완료 - 핀닝됨");

        List<ExecutionResult<String>> pinnedResults = Arrays.asList(pinnedResult);
        when(synchronizationService.processSynchronizedBlocksWithVirtualThread(anyInt(), anyLong())).thenReturn(pinnedResults);

        // When & Then
        mockMvc.perform(get("/sync/synchronized")
                        .param("count", "5")
                        .param("sleepTimeMs", "300"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testLongRunningSync() throws Exception {
        // Given - 긴 실행 시간의 동기화 테스트
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(synchronizationService.processSynchronizedBlocksWithVirtualThread(5, 1000L)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/sync/synchronized")
                        .param("count", "5")
                        .param("sleepTimeMs", "1000"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testZeroSleepTime() throws Exception {
        // Given - 대기 시간이 없는 경우
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(synchronizationService.processSynchronizedBlocksWithVirtualThread(10, 0L)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/sync/synchronized")
                        .param("count", "10")
                        .param("sleepTimeMs", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}

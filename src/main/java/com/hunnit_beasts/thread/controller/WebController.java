package com.hunnit_beasts.thread.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 웹 페이지 라우팅을 처리하는 컨트롤러
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class WebController {

    /**
     * 메인 페이지
     */
    @GetMapping("/")
    public String index(Model model) {
        log.info("메인 페이지 요청");
        model.addAttribute("title", "가상 스레드 테스트 대시보드");
        return "index";
    }

    /**
     * 결과 요약 페이지
     */
    @GetMapping("/results")
    public String results(Model model) {
        log.info("결과 요약 페이지 요청");
        model.addAttribute("title", "테스트 결과 요약");
        return "results";
    }

    /**
     * 정보 페이지
     */
    @GetMapping("/about")
    public String about(Model model) {
        log.info("정보 페이지 요청");
        model.addAttribute("title", "가상 스레드 정보");
        return "about";
    }

    /**
     * API 호출 시나리오 페이지
     */
    @GetMapping("/scenarios/api-call")
    public String apiCallScenario(Model model) {
        log.info("API 호출 시나리오 페이지 요청");
        model.addAttribute("title", "API 호출 시나리오");
        model.addAttribute("scenarioName", "API 호출");
        model.addAttribute("scenarioDescription", "외부 API 호출 시 가상 스레드와 플랫폼 스레드의 성능을 비교합니다.");
        model.addAttribute("endpoint", "/api/compare");
        return "scenarios/api-call";
    }

    /**
     * 데이터베이스 시나리오 페이지
     */
    @GetMapping("/scenarios/database")
    public String databaseScenario(Model model) {
        log.info("데이터베이스 시나리오 페이지 요청");
        model.addAttribute("title", "데이터베이스 시나리오");
        model.addAttribute("scenarioName", "데이터베이스 쿼리");
        model.addAttribute("scenarioDescription", "데이터베이스 쿼리 시 가상 스레드와 플랫폼 스레드의 성능을 비교합니다.");
        model.addAttribute("endpoint", "/database/compare");
        return "scenarios/database";
    }

    /**
     * 파일 I/O 시나리오 페이지
     */
    @GetMapping("/scenarios/file-io")
    public String fileIoScenario(Model model) {
        log.info("파일 I/O 시나리오 페이지 요청");
        model.addAttribute("title", "파일 I/O 시나리오");
        model.addAttribute("scenarioName", "파일 I/O 작업");
        model.addAttribute("scenarioDescription", "파일 읽기/쓰기 작업 시 가상 스레드와 플랫폼 스레드의 성능을 비교합니다.");
        model.addAttribute("endpoint", "/files/compare");
        return "scenarios/file-io";
    }

    /**
     * 대용량 동시 요청 시나리오 페이지
     */
    @GetMapping("/scenarios/stress-test")
    public String stressTestScenario(Model model) {
        log.info("대용량 동시 요청 시나리오 페이지 요청");
        model.addAttribute("title", "대용량 동시 요청 시나리오");
        model.addAttribute("scenarioName", "대용량 요청 처리");
        model.addAttribute("scenarioDescription", "수천 개의 동시 HTTP 요청 처리 시 가상 스레드와 플랫폼 스레드의 성능을 비교합니다.");
        model.addAttribute("endpoint", "/stress/compare");
        return "scenarios/stress-test";
    }

    /**
     * Synchronized 블록 테스트 시나리오 페이지
     */
    @GetMapping("/scenarios/synchronization")
    public String synchronizationScenario(Model model) {
        log.info("Synchronized 블록 테스트 시나리오 페이지 요청");
        model.addAttribute("title", "Synchronized 블록 테스트");
        model.addAttribute("scenarioName", "동기화 블록 테스트");
        model.addAttribute("scenarioDescription", "synchronized 블록과 ReentrantLock 사용 시 가상 스레드의 핀닝 현상을 테스트합니다.");
        model.addAttribute("endpoint", "/sync/compare");
        return "scenarios/synchronization";
    }

    /**
     * 복합 작업 시나리오 페이지
     */
    @GetMapping("/scenarios/workflow")
    public String workflowScenario(Model model) {
        log.info("복합 작업 시나리오 페이지 요청");
        model.addAttribute("title", "복합 작업 시나리오");
        model.addAttribute("scenarioName", "복합 워크플로우");
        model.addAttribute("scenarioDescription", "여러 종류의 작업(API 호출, DB 쿼리, 파일 I/O)을 혼합한 워크플로우에서 가상 스레드와 플랫폼 스레드의 성능을 비교합니다.");
        model.addAttribute("endpoint", "/workflow/compare");
        return "scenarios/workflow";
    }

    /**
     * 특정 테스트 결과 상세 페이지
     */
    @GetMapping("/results/{id}")
    public String resultDetail(@PathVariable String id, Model model) {
        log.info("테스트 결과 상세 페이지 요청 - ID: {}", id);
        model.addAttribute("title", "테스트 결과 상세");
        model.addAttribute("resultId", id);
        // 여기서 실제 결과 데이터를 가져와서 모델에 추가할 수 있습니다
        return "result-detail";
    }

    /**
     * 시스템 정보 페이지
     */
    @GetMapping("/system-info")
    public String systemInfo(Model model) {
        log.info("시스템 정보 페이지 요청");
        model.addAttribute("title", "시스템 정보");
        return "system-info";
    }

    /**
     * 도움말 페이지
     */
    @GetMapping("/help")
    public String help(Model model) {
        log.info("도움말 페이지 요청");
        model.addAttribute("title", "도움말");
        return "help";
    }

    /**
     * 벤치마크 실행 페이지
     */
    @GetMapping("/run-benchmark")
    public String runBenchmark(Model model) {
        log.info("벤치마크 실행 페이지 요청");
        model.addAttribute("title", "벤치마크 실행");
        return "run-benchmark";
    }

    /**
     * 가상 스레드 모니터링 페이지
     */
    @GetMapping("/monitor")
    public String monitor(Model model) {
        log.info("가상 스레드 모니터링 페이지 요청");
        model.addAttribute("title", "스레드 모니터링");
        return "monitor";
    }

    /**
     * 오류 페이지 (404)
     */
    @GetMapping("/error/404")
    public String error404(Model model) {
        log.info("404 오류 페이지 요청");
        model.addAttribute("title", "페이지를 찾을 수 없음");
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorMessage", "요청하신 페이지를 찾을 수 없습니다.");
        return "error";
    }

    /**
     * 오류 페이지 (500)
     */
    @GetMapping("/error/500")
    public String error500(Model model) {
        log.info("500 오류 페이지 요청");
        model.addAttribute("title", "서버 오류");
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorMessage", "서버에서 오류가 발생했습니다. 나중에 다시 시도해 주세요.");
        return "error";
    }
}
package com.aivle08.big_project_api.service;

import com.aivle08.big_project_api.dto.api.request.PdfInfoListRequestDTO;
import com.aivle08.big_project_api.dto.api.request.QuestionRequestDTO;
import com.aivle08.big_project_api.dto.api.request.RecruitmentInputDTO;
import com.aivle08.big_project_api.dto.api.request.ScoreRequestDTO;
import com.aivle08.big_project_api.dto.api.response.ApiResponseDTO;
import com.aivle08.big_project_api.dto.api.response.ExtractionResponseDTO;
import com.aivle08.big_project_api.dto.api.response.QuestionResponseDTO;
import com.aivle08.big_project_api.dto.api.response.ScoreResponseDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class ApiService {
    private final WebClient webClient;

    public ApiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://ai-api:8000").build();
    }

    public ApiResponseDTO<Void> callInsertResumeApi(PdfInfoListRequestDTO requestDTO) {
        Mono<ApiResponseDTO<Void>> responseMono = webClient.post()
                .uri("/zilliz/insertResume")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDTO<Void>>() {
                });
        return responseMono.block();
    }

    public ApiResponseDTO<ExtractionResponseDTO> callSummaryExtractionApi(Long applicantId) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("applicant_id", applicantId);

        Mono<ApiResponseDTO<ExtractionResponseDTO>> responseMono = webClient.post()
                .uri("/summary/extraction")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDTO<ExtractionResponseDTO>>() {
                });

        return responseMono.block();
    }

    public ApiResponseDTO<QuestionResponseDTO> callQuestionApi(QuestionRequestDTO requestDTO, String param) {
        Mono<ApiResponseDTO<QuestionResponseDTO>> responseMono = webClient.post()
                .uri("/question/" + param)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDTO<QuestionResponseDTO>>() {
                });
        return responseMono.block();
    }

    public ApiResponseDTO<ScoreResponseDTO> callScoreApi(ScoreRequestDTO requestDTO) {
        Mono<ApiResponseDTO<ScoreResponseDTO>> responseMono = webClient.post()
                .uri("/score")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDTO<ScoreResponseDTO>>() {
                });
        return responseMono.block();
    }

    public ApiResponseDTO<Void> callInserDetail(RecruitmentInputDTO requestDTO) {
        Mono<ApiResponseDTO<Void>> responseMono = webClient.post()
                .uri("/zilliz/insertDetail")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDTO<Void>>() {
                });
        return responseMono.block();
    }

}

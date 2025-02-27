package com.aivle08.big_project_api.service;

import com.aivle08.big_project_api.constants.ProcessingStatus;
import com.aivle08.big_project_api.dto.request.EvaluationRequestDTO;
import com.aivle08.big_project_api.dto.request.RecruitmentRequestDTO;
import com.aivle08.big_project_api.dto.response.RecruitmentResponseDTO;
import com.aivle08.big_project_api.model.Department;
import com.aivle08.big_project_api.model.Evaluation;
import com.aivle08.big_project_api.model.Recruitment;
import com.aivle08.big_project_api.repository.EvaluationRepository;
import com.aivle08.big_project_api.repository.RecruitmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecruitmentService {
    private final RecruitmentRepository recruitmentRepository;
    private final EvaluationRepository evaluationRepository;
    private final UsersService usersService;

    public RecruitmentService(RecruitmentRepository recruitmentRepository, EvaluationRepository evaluationRepository, UsersService usersService) {
        this.recruitmentRepository = recruitmentRepository;
        this.evaluationRepository = evaluationRepository;
        this.usersService = usersService;
    }

    public List<RecruitmentResponseDTO> getRecruitmentList() {
        return recruitmentRepository.findAllByDepartment(usersService.getCurrentUser().getDepartment())
                .stream().map(RecruitmentResponseDTO::fromEntity).toList();
    }

    @Transactional
    public RecruitmentResponseDTO createRecruitment(RecruitmentRequestDTO recruitmentRequestDTO) {
        List<Evaluation> evaluations = recruitmentRequestDTO.getEvaluationRequestDTOList()
                .stream().map(EvaluationRequestDTO::toEntity).toList();

        Department department = usersService.getCurrentUser().getDepartment();

        Recruitment recruitment = Recruitment.builder()
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .title(recruitmentRequestDTO.getTitle())
                .job(recruitmentRequestDTO.getJob())
                .evaluationList(evaluations)
                .processingStatus(ProcessingStatus.NOT_STARTED)
                .scoreProcessingStatus(ProcessingStatus.NOT_STARTED)
                .department(department)
                .build();

        Recruitment savedRecruitment = recruitmentRepository.save(recruitment);

        List<Evaluation> evaluationList = evaluations.stream()
                .map(evaluation -> Evaluation.builder()
                        .item(evaluation.getItem())
                        .detail(evaluation.getDetail())
                        .recruitment(savedRecruitment)
                        .build())
                .toList();
        List<Evaluation> savedEvaluations = evaluationRepository.saveAll(evaluationList);

        RecruitmentResponseDTO savedRecruitmentResponseDTO = RecruitmentResponseDTO.fromEntity(savedRecruitment);
        return savedRecruitmentResponseDTO;
    }
}

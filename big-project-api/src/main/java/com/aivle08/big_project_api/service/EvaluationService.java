package com.aivle08.big_project_api.service;

import com.aivle08.big_project_api.dto.request.ApplicantRequestDTO;
import com.aivle08.big_project_api.dto.response.EvaluationDetailResponseDTO;
import com.aivle08.big_project_api.dto.response.EvaluationResponseDTO;
import com.aivle08.big_project_api.model.Applicant;
import com.aivle08.big_project_api.model.EvaluationScore;
import com.aivle08.big_project_api.repository.ApplicantRepository;
import com.aivle08.big_project_api.repository.EvaluationScoreRepository;
import com.aivle08.big_project_api.repository.RecruitmentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvaluationService {

    private final EvaluationScoreRepository evaluationScoreRepository;
    private final ApplicantService applicantService;
    private final RecruitmentRepository recruitmentRepository;
    private final ApplicantRepository applicantRepository;

    public EvaluationService(EvaluationScoreRepository evaluationScoreRepository, ApplicantService applicantService, RecruitmentRepository recruitmentRepository, ApplicantRepository applicantRepository) {
        this.evaluationScoreRepository = evaluationScoreRepository;
        this.applicantService = applicantService;
        this.recruitmentRepository = recruitmentRepository;
        this.applicantRepository = applicantRepository;
    }

    public EvaluationResponseDTO getScoreListByApplicantIdAndRecruitmentId(Long recruitmentId, Long applicantId) {

        String recruitmentTitle = recruitmentRepository.findById(recruitmentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid recruitmentId")).getTitle();

        List<Long> applicantIds = applicantService.getApplicantIdListByRecruitmentId(recruitmentId);

        if (!applicantIds.contains(applicantId)) {
            throw new IllegalArgumentException("해당 지원자는 채용 공고에 포함되지 않습니다. applicantId: " + applicantId);
        }

        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 지원자를 찾을 수 없습니다. ID: " + applicantId));


        List<EvaluationDetailResponseDTO> scoreDetails = evaluationScoreRepository.findByApplicantId(applicantId)
                .stream()
                .map(evaluationScore -> {
                    String summary = evaluationScore.getEvaluationDetail().getSummary();
                    return EvaluationDetailResponseDTO.builder()
                            .score(evaluationScore.getScore())
                            .summary(summary)
                            .title(evaluationScore.getEvaluation().getItem())
                            .build();
                })
                .toList();

        return EvaluationResponseDTO.builder()
                .applicantId(applicantId)
                .recruitmentTitle(recruitmentTitle)
                .applicantName(applicant.getName())
                .resumeSummary(applicant.getResumeSummary())
                .scoreDetails(scoreDetails)
                .build();
    }

    public List<EvaluationResponseDTO> getPassedApplicantList(Long recruitmentId) {

        String recruitmentTitle = recruitmentRepository.findById(recruitmentId).orElseThrow(() -> new IllegalArgumentException("Invalid recruitmentId")).getTitle();


        List<Applicant> passedApplicants = applicantRepository.findAllByResumeResultAndRecruitmentId(true, recruitmentId);

        List<EvaluationResponseDTO> passList = new ArrayList<>();
        for (Applicant applicant : passedApplicants) {
            List<EvaluationDetailResponseDTO> scoreDetails = new ArrayList<>();

            for (EvaluationScore evaluationScore : applicant.getEvaluationScoreList()) {

                EvaluationDetailResponseDTO evaluationDetailResponseDTO = EvaluationDetailResponseDTO.builder()
                        .score(evaluationScore.getScore())
                        .summary(evaluationScore.getEvaluationDetail().getSummary())
                        .title(evaluationScore.getEvaluation().getItem())
                        .build();

                scoreDetails.add(evaluationDetailResponseDTO);
            }

            EvaluationResponseDTO evaluationResponseDTO = EvaluationResponseDTO.builder()
                    .recruitmentTitle(recruitmentTitle)
                    .applicantName(applicant.getName())
                    .scoreDetails(scoreDetails)
                    .resumeSummary(applicant.getResumeSummary())
                    .applicantId(applicant.getId())
                    .build();

            passList.add(evaluationResponseDTO);
        }
        return passList;
    }

    public List<EvaluationResponseDTO> getApplicantEvaluationList(Long recruitmentId) {

        String recruitmentTitle = recruitmentRepository.findById(recruitmentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid recruitmentId")).getTitle();

        List<Applicant> allApplicants = applicantRepository.findByRecruitmentId(recruitmentId);

        if (allApplicants.isEmpty()) {
            throw new EntityNotFoundException("해당 채용 공고(ID: " + recruitmentId + ")에 지원한 지원자가 없습니다.");
        }


        List<EvaluationResponseDTO> allList = new ArrayList<>();
        for (Applicant applicant : allApplicants) {
            List<EvaluationDetailResponseDTO> scoreDetails = new ArrayList<>();

            for (EvaluationScore evaluationScore : applicant.getEvaluationScoreList()) {
                scoreDetails.add(EvaluationDetailResponseDTO.builder()
                        .score(evaluationScore.getScore())
                        .summary(evaluationScore.getEvaluationDetail().getSummary())
                        .title(evaluationScore.getEvaluation().getItem())
                        .build());
            }

            allList.add(EvaluationResponseDTO.builder()
                    .recruitmentTitle(recruitmentTitle)
                    .applicantName(applicant.getName())
                    .scoreDetails(scoreDetails)
                    .applicantId(applicant.getId())
                    .resumeSummary(applicant.getResumeSummary())
                    .build());
        }

        return allList;
    }

    @Transactional
    public List<ApplicantRequestDTO> getPassApplicantById(List<Long> applicantIdList) {

        for (Long applicantId : applicantIdList) {
            int updatedCount = applicantRepository.updateResumeResultToPassed(applicantId);

            if (updatedCount == 0) {
                throw new IllegalArgumentException("지원자 ID " + applicantId + "가 존재하지 않거나 이미 합격 처리되었습니다.");
            }
        }

        List<Applicant> updatedApplicants = applicantRepository.findAllById(applicantIdList);

        return updatedApplicants.stream()
                .map(ApplicantRequestDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
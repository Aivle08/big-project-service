package com.aivle08.big_project_api.repository;

import com.aivle08.big_project_api.model.Department;
import com.aivle08.big_project_api.model.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {
    List<Recruitment> findAllByDepartment(Department department);
}

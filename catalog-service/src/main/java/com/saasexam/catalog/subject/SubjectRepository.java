package com.saasexam.catalog.subject;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, String> {

    List<Subject> findByClassIdAndStatusNotOrderByName(String classId, String status);

    List<Subject> findByOrgIdAndStatusNotOrderByName(String orgId, String status);
}

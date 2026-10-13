package com.saasexam.chapter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, String> {

    List<Chapter> findBySubjectIdAndStatusNotOrderByName(String subjectId, String status);

    List<Chapter> findByOrgIdAndStatusNotOrderByName(String orgId, String status);
}

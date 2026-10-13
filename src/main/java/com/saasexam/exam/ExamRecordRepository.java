package com.saasexam.exam;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRecordRepository extends JpaRepository<ExamRecord, String> {

    List<ExamRecord> findBySessionIdOrderById(String sessionId);

    List<ExamRecord> findBySubjectIdOrderByTakenOnDesc(String subjectId);

    List<ExamRecord> findBySubjectIdAndTakenByOrderByTakenOnDesc(String subjectId, String takenBy);
}

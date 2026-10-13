package com.saasexam.question;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, String> {

    Page<Question> findBySubjectIdAndStatusNot(String subjectId, String status, Pageable pageable);

    Page<Question> findByChapterIdAndStatusNot(String chapterId, String status, Pageable pageable);

    /** Live question counts of an organisation per (subject, chapter); chapterId is null for unfiled questions. */
    @Query("""
            select new com.saasexam.question.QuestionCount(q.subjectId, q.chapterId, count(q))
            from Question q
            where q.orgId = :orgId and q.status <> 'D'
            group by q.subjectId, q.chapterId""")
    List<QuestionCount> countByOrg(@Param("orgId") String orgId);
}

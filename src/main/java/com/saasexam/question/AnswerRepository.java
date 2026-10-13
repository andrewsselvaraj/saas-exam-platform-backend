package com.saasexam.question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, String> {

    List<Answer> findByQuestionIdInAndStatusNotOrderById(Collection<String> questionIds, String status);

    @Modifying
    @Query("update Answer a set a.chapterId = :chapterId where a.questionId = :questionId")
    void moveToChapter(@Param("questionId") String questionId, @Param("chapterId") String chapterId);
}

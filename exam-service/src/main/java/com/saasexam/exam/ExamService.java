package com.saasexam.exam;

import com.saasexam.common.BadRequestException;
import com.saasexam.common.IdGenerator;
import com.saasexam.common.NotFoundException;
import com.saasexam.exam.ExamDtos.Choice;
import com.saasexam.exam.ExamDtos.ChoiceResult;
import com.saasexam.exam.ExamDtos.ExamResult;
import com.saasexam.exam.ExamDtos.ExamSubmission;
import com.saasexam.exam.ExamDtos.ExamSummary;
import com.saasexam.exam.client.CatalogClient;
import com.saasexam.exam.client.CatalogClient.SubjectRef;
import com.saasexam.exam.client.QuestionBankClient;
import com.saasexam.exam.client.QuestionBankClient.AnswerRef;
import com.saasexam.exam.client.QuestionBankClient.QuestionRef;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExamService {

    private final ExamRecordRepository records;
    private final CatalogClient catalog;
    private final QuestionBankClient questionBank;

    public ExamService(ExamRecordRepository records, CatalogClient catalog, QuestionBankClient questionBank) {
        this.records = records;
        this.catalog = catalog;
        this.questionBank = questionBank;
    }

    public ExamResult submit(ExamSubmission submission) {
        SubjectRef subject = catalog.getSubject(submission.subjectId());
        Set<String> seen = new HashSet<>();
        for (Choice choice : submission.choices()) {
            if (!seen.add(choice.questionId())) {
                throw new BadRequestException("Question answered more than once: " + choice.questionId());
            }
        }
        Map<String, QuestionRef> questionsById = questionBank.findLive(seen).stream()
                .collect(Collectors.toMap(QuestionRef::id, Function.identity()));

        String sessionId = IdGenerator.next();
        LocalDateTime takenOn = LocalDateTime.now();
        List<ExamRecord> saved = new ArrayList<>();
        for (Choice choice : submission.choices()) {
            QuestionRef question = questionsById.get(choice.questionId());
            if (question == null) throw new NotFoundException("Question", choice.questionId());
            if (!question.subjectId().equals(subject.id())) {
                throw new BadRequestException("Question " + question.id() + " is not part of subject " + subject.id());
            }
            AnswerRef answer = question.answers().stream()
                    .filter(a -> a.id().equals(choice.answerId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException(
                            "Answer " + choice.answerId() + " is not an option of question " + question.id()));
            ExamRecord record = new ExamRecord();
            record.setId(IdGenerator.next());
            record.setSessionId(sessionId);
            record.setOrgId(subject.orgId());
            record.setClassId(subject.classId());
            record.setSubjectId(subject.id());
            record.setQuestionId(question.id());
            record.setSelectedAnswerId(answer.id());
            record.setSelectedCorrect(answer.correct());
            record.setTakenBy(submission.takenBy().trim());
            record.setTakenOn(takenOn);
            saved.add(records.save(record));
        }
        return result(saved);
    }

    @Transactional(readOnly = true)
    public ExamResult get(String sessionId) {
        List<ExamRecord> session = records.findBySessionIdOrderById(sessionId);
        if (session.isEmpty()) throw new NotFoundException("Exam session", sessionId);
        return result(session);
    }

    /** Attempts on a subject, newest first, optionally limited to one person. */
    @Transactional(readOnly = true)
    public List<ExamSummary> list(String subjectId, String takenBy) {
        catalog.getSubject(subjectId);
        List<ExamRecord> all = takenBy == null || takenBy.isBlank()
                ? records.findBySubjectIdOrderByTakenOnDesc(subjectId)
                : records.findBySubjectIdAndTakenByOrderByTakenOnDesc(subjectId, takenBy.trim());
        Map<String, List<ExamRecord>> bySession = all.stream()
                .collect(Collectors.groupingBy(ExamRecord::getSessionId, LinkedHashMap::new, Collectors.toList()));
        return bySession.values().stream().map(session -> {
            ExamResult r = result(session);
            return new ExamSummary(r.sessionId(), r.subjectId(), r.takenBy(), r.takenOn(),
                    r.totalQuestions(), r.correctCount(), r.scorePercent());
        }).toList();
    }

    private static ExamResult result(List<ExamRecord> session) {
        ExamRecord first = session.get(0);
        List<ChoiceResult> choices = session.stream()
                .map(r -> new ChoiceResult(r.getQuestionId(), r.getSelectedAnswerId(), r.isSelectedCorrect()))
                .toList();
        int correct = (int) choices.stream().filter(ChoiceResult::correct).count();
        double percent = Math.round(correct * 10000.0 / choices.size()) / 100.0;
        return new ExamResult(first.getSessionId(), first.getOrgId(), first.getClassId(), first.getSubjectId(),
                first.getTakenBy(), first.getTakenOn(), choices.size(), correct, percent, choices);
    }
}

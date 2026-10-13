package com.saasexam.exam;

import com.saasexam.common.BadRequestException;
import com.saasexam.common.IdGenerator;
import com.saasexam.common.NotFoundException;
import com.saasexam.exam.ExamDtos.Choice;
import com.saasexam.exam.ExamDtos.ChoiceResult;
import com.saasexam.exam.ExamDtos.ExamResult;
import com.saasexam.exam.ExamDtos.ExamSubmission;
import com.saasexam.exam.ExamDtos.ExamSummary;
import com.saasexam.question.Answer;
import com.saasexam.question.Question;
import com.saasexam.question.QuestionService;
import com.saasexam.subject.Subject;
import com.saasexam.subject.SubjectService;
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
    private final SubjectService subjects;
    private final QuestionService questions;

    public ExamService(ExamRecordRepository records, SubjectService subjects, QuestionService questions) {
        this.records = records;
        this.subjects = subjects;
        this.questions = questions;
    }

    public ExamResult submit(ExamSubmission submission) {
        Subject subject = subjects.get(submission.subjectId());
        Set<String> seen = new HashSet<>();
        for (Choice choice : submission.choices()) {
            if (!seen.add(choice.questionId())) {
                throw new BadRequestException("Question answered more than once: " + choice.questionId());
            }
        }
        Map<String, Answer> answersById = questions.liveAnswers(seen).stream()
                .collect(Collectors.toMap(Answer::getId, Function.identity()));

        String sessionId = IdGenerator.next();
        LocalDateTime takenOn = LocalDateTime.now();
        List<ExamRecord> saved = new ArrayList<>();
        for (Choice choice : submission.choices()) {
            Question question = questions.get(choice.questionId());
            if (!question.getSubjectId().equals(subject.getId())) {
                throw new BadRequestException("Question " + question.getId() + " is not part of subject " + subject.getId());
            }
            Answer answer = answersById.get(choice.answerId());
            if (answer == null || !answer.getQuestionId().equals(question.getId())) {
                throw new BadRequestException("Answer " + choice.answerId() + " is not an option of question " + question.getId());
            }
            ExamRecord record = new ExamRecord();
            record.setId(IdGenerator.next());
            record.setSessionId(sessionId);
            record.setOrgId(subject.getOrgId());
            record.setClassId(subject.getClassId());
            record.setSubjectId(subject.getId());
            record.setQuestionId(question.getId());
            record.setSelectedAnswerId(answer.getId());
            record.setTakenBy(submission.takenBy().trim());
            record.setTakenOn(takenOn);
            saved.add(records.save(record));
        }
        return result(saved, answersById);
    }

    @Transactional(readOnly = true)
    public ExamResult get(String sessionId) {
        List<ExamRecord> session = records.findBySessionIdOrderById(sessionId);
        if (session.isEmpty()) throw new NotFoundException("Exam session", sessionId);
        return result(session, answersFor(session));
    }

    /** Attempts on a subject, newest first, optionally limited to one person. */
    @Transactional(readOnly = true)
    public List<ExamSummary> list(String subjectId, String takenBy) {
        subjects.get(subjectId);
        List<ExamRecord> all = takenBy == null || takenBy.isBlank()
                ? records.findBySubjectIdOrderByTakenOnDesc(subjectId)
                : records.findBySubjectIdAndTakenByOrderByTakenOnDesc(subjectId, takenBy.trim());
        Map<String, Answer> answers = answersFor(all);
        Map<String, List<ExamRecord>> bySession = all.stream()
                .collect(Collectors.groupingBy(ExamRecord::getSessionId, LinkedHashMap::new, Collectors.toList()));
        return bySession.values().stream().map(session -> {
            ExamResult r = result(session, answers);
            return new ExamSummary(r.sessionId(), r.subjectId(), r.takenBy(), r.takenOn(),
                    r.totalQuestions(), r.correctCount(), r.scorePercent());
        }).toList();
    }

    /** Includes deleted answers so that historical attempts are still scored against what was chosen. */
    private Map<String, Answer> answersFor(List<ExamRecord> session) {
        Set<String> ids = session.stream().map(ExamRecord::getSelectedAnswerId).collect(Collectors.toSet());
        return questions.answersById(ids);
    }

    private static ExamResult result(List<ExamRecord> session, Map<String, Answer> answersById) {
        ExamRecord first = session.get(0);
        List<ChoiceResult> choices = session.stream()
                .map(r -> {
                    Answer a = answersById.get(r.getSelectedAnswerId());
                    return new ChoiceResult(r.getQuestionId(), r.getSelectedAnswerId(), a != null && a.isCorrect());
                })
                .toList();
        int correct = (int) choices.stream().filter(ChoiceResult::correct).count();
        double percent = Math.round(correct * 10000.0 / choices.size()) / 100.0;
        return new ExamResult(first.getSessionId(), first.getOrgId(), first.getClassId(), first.getSubjectId(),
                first.getTakenBy(), first.getTakenOn(), choices.size(), correct, percent, choices);
    }
}

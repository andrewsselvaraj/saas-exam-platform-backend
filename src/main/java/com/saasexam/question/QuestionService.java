package com.saasexam.question;

import com.saasexam.chapter.Chapter;
import com.saasexam.chapter.ChapterService;
import com.saasexam.common.BadRequestException;
import com.saasexam.common.IdGenerator;
import com.saasexam.common.NotFoundException;
import com.saasexam.common.SoftDeleter;
import com.saasexam.common.Status;
import com.saasexam.common.UpdatedBy;
import com.saasexam.question.QuestionDtos.AnswerRequest;
import com.saasexam.question.QuestionDtos.QuestionRequest;
import com.saasexam.question.QuestionDtos.QuestionResponse;
import com.saasexam.subject.Subject;
import com.saasexam.subject.SubjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuestionService {

    private final QuestionRepository questions;
    private final AnswerRepository answers;
    private final SubjectService subjects;
    private final ChapterService chapters;
    private final SoftDeleter softDeleter;

    public QuestionService(QuestionRepository questions, AnswerRepository answers, SubjectService subjects,
                           ChapterService chapters, SoftDeleter softDeleter) {
        this.questions = questions;
        this.answers = answers;
        this.subjects = subjects;
        this.chapters = chapters;
        this.softDeleter = softDeleter;
    }

    // ---- questions -------------------------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<QuestionResponse> listBySubject(String subjectId, Pageable pageable) {
        subjects.get(subjectId);
        return withAnswers(questions.findBySubjectIdAndStatusNot(subjectId, Status.DELETED, pageable));
    }

    @Transactional(readOnly = true)
    public Page<QuestionResponse> listByChapter(String chapterId, Pageable pageable) {
        chapters.get(chapterId);
        return withAnswers(questions.findByChapterIdAndStatusNot(chapterId, Status.DELETED, pageable));
    }

    @Transactional(readOnly = true)
    public Question get(String id) {
        return questions.findById(id)
                .filter(q -> !Status.DELETED.equals(q.getStatus()))
                .orElseThrow(() -> new NotFoundException("Question", id));
    }

    @Transactional(readOnly = true)
    public QuestionResponse getWithAnswers(String id) {
        Question question = get(id);
        return QuestionResponse.of(question, liveAnswers(List.of(id)));
    }

    public QuestionResponse create(String subjectId, QuestionRequest request) {
        Subject subject = subjects.get(subjectId);
        Question question = new Question();
        question.setId(IdGenerator.next());
        question.setOrgId(subject.getOrgId());
        question.setClassId(subject.getClassId());
        question.setSubjectId(subject.getId());
        apply(question, request);
        questions.save(question);
        List<Answer> created = request.answers() == null ? List.of() : addAnswers(question, request.answers());
        return QuestionResponse.of(question, created);
    }

    public QuestionResponse update(String id, QuestionRequest request) {
        Question question = get(id);
        String previousChapter = question.getChapterId();
        apply(question, request);
        if (!Objects.equals(previousChapter, question.getChapterId())) {
            questions.flush();
            answers.moveToChapter(id, question.getChapterId());
        }
        if (request.answers() != null) {
            liveAnswers(List.of(id)).forEach(a -> a.setStatus(Status.DELETED));
            addAnswers(question, request.answers());
        }
        return QuestionResponse.of(question, liveAnswers(List.of(id)));
    }

    public void delete(String id) {
        get(id);
        softDeleter.delete(SoftDeleter.Level.QUESTION, id);
    }

    // ---- answers ---------------------------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Answer> listAnswers(String questionId) {
        get(questionId);
        return liveAnswers(List.of(questionId));
    }

    @Transactional(readOnly = true)
    public Answer getAnswer(String id) {
        return answers.findById(id)
                .filter(a -> !Status.DELETED.equals(a.getStatus()))
                .orElseThrow(() -> new NotFoundException("Answer", id));
    }

    public Answer addAnswer(String questionId, AnswerRequest request) {
        return addAnswers(get(questionId), List.of(request)).get(0);
    }

    public Answer updateAnswer(String id, AnswerRequest request) {
        Answer answer = getAnswer(id);
        apply(answer, request);
        return answer;
    }

    public void deleteAnswer(String id) {
        getAnswer(id).setStatus(Status.DELETED);
    }

    /** Live (not deleted) answers of the given questions, oldest first. */
    @Transactional(readOnly = true)
    public List<Answer> liveAnswers(Collection<String> questionIds) {
        if (questionIds.isEmpty()) return List.of();
        return answers.findByQuestionIdInAndStatusNotOrderById(questionIds, Status.DELETED);
    }

    /** Answers by id regardless of status (exam history must still resolve deleted options). */
    @Transactional(readOnly = true)
    public Map<String, Answer> answersById(Collection<String> ids) {
        return answers.findAllById(ids).stream().collect(Collectors.toMap(Answer::getId, a -> a));
    }

    // ---- helpers ---------------------------------------------------------------------------------------------

    private Page<QuestionResponse> withAnswers(Page<Question> page) {
        Map<String, List<Answer>> byQuestion = liveAnswers(page.map(Question::getId).getContent()).stream()
                .collect(Collectors.groupingBy(Answer::getQuestionId));
        return page.map(q -> QuestionResponse.of(q, byQuestion.getOrDefault(q.getId(), List.of())));
    }

    private List<Answer> addAnswers(Question question, List<AnswerRequest> requests) {
        return requests.stream().map(request -> {
            Answer answer = new Answer();
            answer.setId(IdGenerator.next());
            answer.attachTo(question);
            apply(answer, request);
            return answers.save(answer);
        }).toList();
    }

    private void apply(Question question, QuestionRequest request) {
        question.setChapterId(resolveChapter(question.getSubjectId(), request.chapterId()));
        question.setText(request.text().trim());
        question.setType(request.type());
        question.setUpdatedBy(UpdatedBy.orSystem(request.updatedBy()));
        if (request.status() != null) question.setStatus(request.status());
    }

    private static void apply(Answer answer, AnswerRequest request) {
        answer.setText(request.text().trim());
        answer.setCorrect(request.correct());
        answer.setUpdatedBy(UpdatedBy.orSystem(request.updatedBy()));
        if (request.status() != null) answer.setStatus(request.status());
    }

    private String resolveChapter(String subjectId, String chapterId) {
        if (chapterId == null || chapterId.isBlank()) return null;
        Chapter chapter = chapters.get(chapterId);
        if (!chapter.getSubjectId().equals(subjectId)) {
            throw new BadRequestException("Chapter " + chapterId + " does not belong to subject " + subjectId);
        }
        return chapter.getId();
    }
}

package com.saasexam.catalog.hierarchy;

import com.saasexam.catalog.chapter.Chapter;
import com.saasexam.catalog.chapter.ChapterRepository;
import com.saasexam.catalog.client.QuestionBankClient;
import com.saasexam.catalog.client.QuestionBankClient.QuestionCount;
import com.saasexam.catalog.organization.Organization;
import com.saasexam.catalog.organization.OrganizationService;
import com.saasexam.catalog.schoolclass.SchoolClass;
import com.saasexam.catalog.schoolclass.SchoolClassRepository;
import com.saasexam.catalog.subject.Subject;
import com.saasexam.catalog.subject.SubjectRepository;
import com.saasexam.common.Status;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Read-only view of an organisation's whole tree, e.g. for navigation menus. Answers are not included. Question
 * counts come from the question service.
 */
@RestController
public class HierarchyController {

    public record OrgTree(String id, String name, String status, List<ClassNode> classes) {
    }

    public record ClassNode(String id, String name, String status, List<SubjectNode> subjects) {
    }

    /** {@code questionCount} covers all of the subject's questions; {@code unfiledQuestionCount} those without a chapter. */
    public record SubjectNode(String id, String name, String status, long questionCount, long unfiledQuestionCount,
                              List<ChapterNode> chapters) {
    }

    public record ChapterNode(String id, String name, String status, long questionCount) {
    }

    private final OrganizationService organizations;
    private final SchoolClassRepository classes;
    private final SubjectRepository subjects;
    private final ChapterRepository chapters;
    private final QuestionBankClient questions;

    public HierarchyController(OrganizationService organizations, SchoolClassRepository classes,
                               SubjectRepository subjects, ChapterRepository chapters, QuestionBankClient questions) {
        this.organizations = organizations;
        this.classes = classes;
        this.subjects = subjects;
        this.chapters = chapters;
        this.questions = questions;
    }

    @GetMapping("/api/orgs/{orgId}/tree")
    @Transactional(readOnly = true)
    public OrgTree tree(@PathVariable String orgId) {
        Organization org = organizations.get(orgId);

        Map<String, Long> bySubject = new HashMap<>();
        Map<String, Long> unfiledBySubject = new HashMap<>();
        Map<String, Long> byChapter = new HashMap<>();
        for (QuestionCount c : questions.countByOrg(orgId)) {
            bySubject.merge(c.subjectId(), c.count(), Long::sum);
            if (c.chapterId() == null) unfiledBySubject.merge(c.subjectId(), c.count(), Long::sum);
            else byChapter.merge(c.chapterId(), c.count(), Long::sum);
        }

        Map<String, List<Chapter>> chaptersBySubject = chapters.findByOrgIdAndStatusNotOrderByName(orgId, Status.DELETED)
                .stream().collect(Collectors.groupingBy(Chapter::getSubjectId));
        Map<String, List<Subject>> subjectsByClass = subjects.findByOrgIdAndStatusNotOrderByName(orgId, Status.DELETED)
                .stream().collect(Collectors.groupingBy(Subject::getClassId));

        List<ClassNode> classNodes = classes.findByOrgIdAndStatusNotOrderByName(orgId, Status.DELETED).stream()
                .map((SchoolClass c) -> new ClassNode(c.getId(), c.getName(), c.getStatus(),
                        subjectsByClass.getOrDefault(c.getId(), List.of()).stream()
                                .map(s -> new SubjectNode(s.getId(), s.getName(), s.getStatus(),
                                        bySubject.getOrDefault(s.getId(), 0L),
                                        unfiledBySubject.getOrDefault(s.getId(), 0L),
                                        chaptersBySubject.getOrDefault(s.getId(), List.of()).stream()
                                                .map(ch -> new ChapterNode(ch.getId(), ch.getName(), ch.getStatus(),
                                                        byChapter.getOrDefault(ch.getId(), 0L)))
                                                .toList()))
                                .toList()))
                .toList();
        return new OrgTree(org.getId(), org.getName(), org.getStatus(), classNodes);
    }
}

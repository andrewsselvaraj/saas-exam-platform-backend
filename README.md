# saas-exam-platform-backend

Backend API for SaaS exam platform with school hierarchy (school/org → class → subject → chapter → question → answer).

Spring Boot 3.5 · Java 17 · H2 (file-based, MySQL mode) · Flyway · Spring Data JPA · springdoc OpenAPI.

## Run

```bash
./mvnw spring-boot:run
```

- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/examdb`, user `sa`, no password)

The database lives in `./data`. Delete that folder to start again from the seed data.

## Database

Built from the legacy MySQL dump in `sql/m2itssql_complete.sql`:

- `V1__create_schema.sql` – H2 schema for the hierarchy tables and `exam_info`. The dump's `question_info_v_two` /
  `answer_info_v_two` are the live tables and are named `question_info` / `answer_info` here.
- `V2__seed_data.sql` – generated from the dump by `java tools/MysqlDumpToH2.java` (rows with missing parents are dropped).

The user/admin/role tables are not included yet (user module comes later). `org_info.org_password` is kept in the
schema for that module but is not exposed by the API, and `*_updated_by` / `takenBy` are plain strings for now.

Every child row stores the ids of all its ancestors (`fk_org_id`, `fk_class_id`, ...). The API fills these from the
parent, so clients only ever pass the direct parent. A question's chapter is optional: most legacy questions sit
directly under a subject.

Statuses: `A` active, `I` inactive, `D` deleted. `DELETE` endpoints soft-delete the node and everything below it;
exam history is kept.

## API

| Level    | Collection (list / create)                    | Item (get / update / delete) |
|----------|-----------------------------------------------|------------------------------|
| Org      | `/api/orgs`                                   | `/api/orgs/{orgId}`          |
| Class    | `/api/orgs/{orgId}/classes`                   | `/api/classes/{classId}`     |
| Subject  | `/api/classes/{classId}/subjects`             | `/api/subjects/{subjectId}`  |
| Chapter  | `/api/subjects/{subjectId}/chapters`          | `/api/chapters/{chapterId}`  |
| Question | `/api/subjects/{subjectId}/questions` (create; paged list of all) · `/api/chapters/{chapterId}/questions` (paged list) | `/api/questions/{questionId}` |
| Answer   | `/api/questions/{questionId}/answers`         | `/api/answers/{answerId}`    |

Other endpoints:

- `GET /api/orgs/{orgId}/tree` – classes → subjects → chapters with question counts.
- `POST /api/exams` – submit an attempt `{subjectId, takenBy, choices:[{questionId, answerId}]}`; returns the score.
- `GET /api/exams/{sessionId}` – result of one attempt.
- `GET /api/exams?subjectId=...&takenBy=...` – attempts on a subject.

Example – create a question with its options:

```json
POST /api/subjects/{subjectId}/questions
{
  "chapterId": null,
  "text": "Who is the inventor of the computer?",
  "type": "text",
  "answers": [
    { "text": "Charles Babbage", "correct": true },
    { "text": "Einstein", "correct": false }
  ]
}
```

On `PUT /api/questions/{id}`, omitting `answers` keeps the existing options; sending a list replaces them.

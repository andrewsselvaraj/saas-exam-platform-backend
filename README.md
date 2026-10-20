# saas-exam-platform-backend

Backend API for SaaS exam platform with school hierarchy (school/org → class → subject → chapter → question → answer),
split into microservices.

Spring Boot 3.5 · Java 17 · Spring Cloud Gateway · H2 (file-based, MySQL mode, one database per service) · Flyway ·
Spring Data JPA · springdoc OpenAPI.

## Services

| Module             | Port | Owns                                             | Calls                     |
|--------------------|------|--------------------------------------------------|---------------------------|
| `api-gateway`      | 8080 | Nothing: routes `/api/**` to the owning service  | all three services        |
| `catalog-service`  | 8081 | Orgs, classes, subjects, chapters, the org tree  | question-service          |
| `question-service` | 8082 | Questions and their answer options               | catalog-service           |
| `exam-service`     | 8083 | Exam attempts and scores                         | catalog, question-service |
| `common-lib`       | –    | Ids, statuses and error handling (no entities)   | –                         |

Each service has its own database and Flyway migrations, and no service reads another's tables. Ids that point into
another service (`fk_subject_id` on a question, `fk_question_id` on an exam row, ...) are plain columns, not
foreign keys. Service-to-service calls use REST:

- question-service → catalog `GET /api/subjects/{id}`, `GET /api/chapters/{id}` to validate parents and copy their ids.
- exam-service → catalog `GET /api/subjects/{id}`, and question-service `GET /internal/questions?ids=...` to score a
  submission. Whether each chosen option was correct is stored on the exam row, so history stays scoreable after
  questions change or are deleted.
- catalog-service → question-service `GET /internal/question-counts?orgId=...` for the tree view, and
  `POST /internal/hierarchy-deletions` when an org, class, subject or chapter is deleted. If that call fails the
  catalog's delete is rolled back and the client gets `502`.

`/internal/**` endpoints are not routed by the gateway. The base URLs of other services come from
`CATALOG_SERVICE_URL`, `QUESTION_SERVICE_URL` and `EXAM_SERVICE_URL` (defaults: `http://localhost:8081`, `8082`,
`8083`).

## Run

```bash
./mvnw -DskipTests package
java -jar catalog-service/target/catalog-service-0.0.1-SNAPSHOT.jar &
java -jar question-service/target/question-service-0.0.1-SNAPSHOT.jar &
java -jar exam-service/target/exam-service-0.0.1-SNAPSHOT.jar &
java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar &
```

Or with Docker: `./mvnw -DskipTests package && docker compose up --build`.

- API (through the gateway): http://localhost:8080/api
- Swagger UI per service: http://localhost:8081/swagger-ui.html (catalog), `:8082` (questions), `:8083` (exams)
- H2 console per service: `http://localhost:<port>/h2-console`, JDBC URL `jdbc:h2:file:./data/<catalogdb|questiondb|examdb>`,
  user `sa`, no password

Each service keeps its database in `./data` of its working directory; delete it to start again from the seed data.
`PORT` and `DB_URL` override a service's port and database.

## Test

- `./mvnw test` runs each service's tests, with the other services mocked.
- `scripts/e2e-test.sh` (needs `curl` and `jq`, after `./mvnw -DskipTests package`) starts all four services on
  in-memory databases and runs the full lifecycle through the gateway.

## Deploy

The OpenShift manifests, the image Dockerfile and the deploy scripts live in [`deploy/`](deploy/) and
[`scripts/`](scripts/). See [deploy/README.md](deploy/README.md); after `oc login`, `./scripts/deploy-backend.sh` deploys
all four services and prints the public API URL.

## Database

Built from the legacy MySQL dump in `sql/m2itssql_complete.sql`. Each service has:

- `V1__create_schema.sql` – its own tables. The dump's `question_info_v_two` / `answer_info_v_two` are the live
  tables and are named `question_info` / `answer_info` in question-service. exam-service's `exam_info` adds
  `exam_selected_correct`.
- `V2__seed_data.sql` – generated from the dump by `java tools/MysqlDumpToH2.java sql/m2itssql_complete.sql .`, which
  writes all three seed files (rows with missing parents are dropped).

The user/admin/role tables are not included yet (user module comes later). `org_info.org_password` is kept in the
schema for that module but is not exposed by the API, and `*_updated_by` / `takenBy` are plain strings for now.

Every child row stores the ids of all its ancestors (`fk_org_id`, `fk_class_id`, ...). The services fill these from
the parent, so clients only ever pass the direct parent. A question's chapter is optional: most legacy questions sit
directly under a subject.

Statuses: `A` active, `I` inactive, `D` deleted. `DELETE` endpoints soft-delete the node and everything below it,
across services; exam history is kept.

## API

All paths are served by the gateway on port 8080.

| Level    | Collection (list / create)                    | Item (get / update / delete) | Service  |
|----------|-----------------------------------------------|------------------------------|----------|
| Org      | `/api/orgs`                                   | `/api/orgs/{orgId}`          | catalog  |
| Class    | `/api/orgs/{orgId}/classes`                   | `/api/classes/{classId}`     | catalog  |
| Subject  | `/api/classes/{classId}/subjects`             | `/api/subjects/{subjectId}`  | catalog  |
| Chapter  | `/api/subjects/{subjectId}/chapters`          | `/api/chapters/{chapterId}`  | catalog  |
| Question | `/api/subjects/{subjectId}/questions` (create; paged list of all) · `/api/chapters/{chapterId}/questions` (paged list) | `/api/questions/{questionId}` | question |
| Answer   | `/api/questions/{questionId}/answers`         | `/api/answers/{answerId}`    | question |

Other endpoints:

- `GET /api/orgs/{orgId}/tree` – classes → subjects → chapters with question counts (catalog).
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

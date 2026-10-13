#!/usr/bin/env bash
# End-to-end check: starts all four services on in-memory databases, then drives the full hierarchy lifecycle
# through the gateway (create org → class → subject → chapter → questions, take an exam, delete the class).
# Needs curl and jq. Run from the project root after `./mvnw -DskipTests package`.
set -euo pipefail

GATEWAY=http://localhost:8080
PIDS=()
cleanup() { for pid in "${PIDS[@]}"; do kill "$pid" 2>/dev/null || true; done; }
trap cleanup EXIT

start() { # module port
  local mem="jdbc:h2:mem:$1-e2e;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1"
  DB_URL="$mem" PORT="$2" java -jar "$1"/target/"$1"-0.0.1-SNAPSHOT.jar > "target-e2e-$1.log" 2>&1 &
  PIDS+=($!)
}
wait_up() { # port
  for _ in $(seq 1 90); do
    curl -sf "http://localhost:$1/actuator/health" > /dev/null && return 0
    sleep 1
  done
  echo "service on port $1 did not start" >&2; exit 1
}

start catalog-service 8081; start question-service 8082; start exam-service 8083; start api-gateway 8080
for port in 8081 8082 8083 8080; do wait_up "$port"; done

post() { curl -sf -X POST -H 'Content-Type: application/json' -d "$2" "$GATEWAY$1"; }
status() { curl -s -o /dev/null -w '%{http_code}' -X "$1" -H 'Content-Type: application/json' ${3:+-d "$3"} "$GATEWAY$2"; }
expect() { [ "$2" == "$3" ] || { echo "FAIL: $1: expected $3, got $2" >&2; exit 1; }; echo "ok: $1"; }

expect "seed question" "$(curl -sf $GATEWAY/api/questions/6490999ab704c | jq -r .text)" "Who is the inventor of the computer?"
expect "seed exam" "$(curl -sf $GATEWAY/api/exams/66c9933e02e56 | jq .totalQuestions)" "6"

ORG=$(post /api/orgs '{"name":"Test School","email":"t@school.com"}' | jq -r .id)
CLASS=$(post /api/orgs/$ORG/classes '{"name":"Class 10"}' | jq -r .id)
SUBJECT=$(post /api/classes/$CLASS/subjects '{"name":"Maths"}' | jq -r .id)
CHAPTER=$(post /api/subjects/$SUBJECT/chapters '{"name":"Algebra"}' | jq -r .id)

Q1=$(post /api/subjects/$SUBJECT/questions "{\"chapterId\":\"$CHAPTER\",\"text\":\"2 + 2 = ?\",\"type\":\"text\",
  \"answers\":[{\"text\":\"3\",\"correct\":false},{\"text\":\"4\",\"correct\":true}]}")
Q1_ID=$(jq -r .id <<< "$Q1"); Q1_WRONG=$(jq -r '.answers[0].id' <<< "$Q1"); Q1_RIGHT=$(jq -r '.answers[1].id' <<< "$Q1")
Q2=$(post /api/subjects/$SUBJECT/questions '{"text":"Is 7 prime?","type":"text",
  "answers":[{"text":"Yes","correct":true},{"text":"No","correct":false}]}')
Q2_ID=$(jq -r .id <<< "$Q2"); Q2_WRONG=$(jq -r '.answers[1].id' <<< "$Q2")
expect "question copies catalog ids" "$(jq -r .orgId <<< "$Q1")" "$ORG"

TREE=$(curl -sf $GATEWAY/api/orgs/$ORG/tree)
expect "tree question count" "$(jq '.classes[0].subjects[0].questionCount' <<< "$TREE")" "2"
expect "tree unfiled count" "$(jq '.classes[0].subjects[0].unfiledQuestionCount' <<< "$TREE")" "1"
expect "tree chapter count" "$(jq '.classes[0].subjects[0].chapters[0].questionCount' <<< "$TREE")" "1"
expect "chapter questions" "$(curl -sf $GATEWAY/api/chapters/$CHAPTER/questions | jq '.content | length')" "1"
expect "chapter of another subject" "$(status PUT /api/questions/$Q1_ID '{"chapterId":"688514c8cc3a4","text":"x","type":"text"}')" "400"

SESSION=$(post /api/exams "{\"subjectId\":\"$SUBJECT\",\"takenBy\":\"student@school.com\",\"choices\":[
  {\"questionId\":\"$Q1_ID\",\"answerId\":\"$Q1_RIGHT\"},{\"questionId\":\"$Q2_ID\",\"answerId\":\"$Q2_WRONG\"}]}" | jq -r .sessionId)
expect "exam score" "$(curl -sf $GATEWAY/api/exams/$SESSION | jq .scorePercent)" "50.0"
expect "exam list" "$(curl -sf "$GATEWAY/api/exams?subjectId=$SUBJECT" | jq length)" "1"
expect "answer of another question" "$(status POST /api/exams "{\"subjectId\":\"$SUBJECT\",\"takenBy\":\"s\",
  \"choices\":[{\"questionId\":\"$Q2_ID\",\"answerId\":\"$Q1_WRONG\"}]}")" "400"

expect "delete class" "$(status DELETE /api/classes/$CLASS)" "204"
expect "subject gone" "$(status GET /api/subjects/$SUBJECT)" "404"
expect "question gone" "$(status GET /api/questions/$Q1_ID)" "404"
expect "answer gone" "$(status GET /api/answers/$Q1_RIGHT)" "404"
expect "exam history kept" "$(curl -sf $GATEWAY/api/exams/$SESSION | jq .correctCount)" "1"
expect "internal API not exposed" "$(status GET "/internal/question-counts?orgId=$ORG")" "404"

echo "All end-to-end checks passed."

#!/usr/bin/env bash
# Deploys the backend microservices to the OpenShift project you are logged in to (`oc login`, `oc project`).
# The data services use the existing MySQL schema ocp_connect; their login comes from the saas-exam-mysql Secret.
#   ./scripts/deploy-backend.sh
# Set NAMESPACE to deploy into another existing project, OVERLAY to use another overlay (default: sandbox).
set -euo pipefail

OVERLAY="${OVERLAY:-sandbox}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SERVICES=(catalog-service question-service exam-service api-gateway)

[ -d "$ROOT/deploy/openshift/overlays/$OVERLAY" ] || { echo "Unknown overlay: $OVERLAY" >&2; exit 1; }
NAMESPACE="${NAMESPACE:-$(oc project -q)}"
oc project "$NAMESPACE" >/dev/null

# The MySQL login is never committed and never generated: create it by hand once.
if ! oc get secret saas-exam-mysql >/dev/null 2>&1; then
  echo "Secret saas-exam-mysql is missing. Create it with the MySQL login for ocp_connect:" >&2
  echo "  oc create secret generic saas-exam-mysql --from-literal=SPRING_DATASOURCE_USERNAME=<user> --from-literal=SPRING_DATASOURCE_PASSWORD=<password>" >&2
  exit 1
fi

oc apply -k "$ROOT/deploy/openshift/overlays/$OVERLAY"

# Build every service from the latest backend commit in parallel; each Deployment
# rolls out when its new image lands.
builds=()
for svc in "${SERVICES[@]}"; do
  builds+=("$(oc start-build "$svc" -o name)")
done
for build in "${builds[@]}"; do
  echo "Waiting for $build..."
  if ! oc wait "$build" --for=jsonpath='{.status.phase}'=Complete --timeout=20m; then
    oc logs "$build" --tail=50 || true
    echo "$build did not complete" >&2
    exit 1
  fi
done

for svc in "${SERVICES[@]}"; do
  oc rollout status "deployment/$svc" --timeout=10m
done

HOST="$(oc get route api-gateway -o jsonpath='{.spec.host}')"
echo "API (through the gateway): https://$HOST/api/orgs"

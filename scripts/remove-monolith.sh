#!/usr/bin/env bash
# Removes the old single-backend deployment (exam-backend) after the microservices are up.
# This deletes the monolith's data volume. The microservices keep their data in MySQL.
set -euo pipefail

NAMESPACE="${NAMESPACE:-$(oc project -q)}"
oc project "$NAMESPACE" >/dev/null

read -r -p "Delete exam-backend and its data volume in $NAMESPACE? [y/N] " answer
[ "$answer" = "y" ] || { echo "Nothing deleted."; exit 0; }

oc delete route,service,deployment,buildconfig,imagestream,configmap,secret,pvc \
  -l app.kubernetes.io/name=exam-backend --ignore-not-found

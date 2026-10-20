#!/usr/bin/env bash
# Checks the MySQL SSH tunnel from inside OpenShift: starts a throwaway pod with the tunnel image and the
# mysql-ssh-tunnel Secret, and reports whether 127.0.0.1:3306 in the pod reaches MySQL on the server.
#   ./scripts/test-tunnel.sh
# Prints "TUNNEL: UP" with MySQL's greeting, or "TUNNEL: DOWN" with the SSH error.
set -euo pipefail

cfg() { oc get configmap mysql-tunnel-config -o "jsonpath={.data.$1}"; }
IMG="$(oc get istag mysql-tunnel:latest -o jsonpath='{.image.dockerImageReference}')"
HOST="$(cfg SSH_HOST)"; USER_="$(cfg SSH_USER)"; PORT="$(cfg SSH_PORT)"

CHECK='/usr/local/bin/mysql-tunnel > /tmp/ssh.log 2>&1 &
for i in $(seq 1 15); do
  sleep 1
  if (exec 3<>/dev/tcp/127.0.0.1/3306) 2>/dev/null; then
    exec 3<>/dev/tcp/127.0.0.1/3306
    greeting=$(timeout 5 head -c 80 <&3 | tr -dc "[:print:]")
    echo "TUNNEL: UP (MySQL says: ${greeting:0:40})"
    exit 0
  fi
done
echo "TUNNEL: DOWN"
cat /tmp/ssh.log
exit 1'

OVERRIDES=$(cat <<EOF
{"spec":{"containers":[{"name":"tunnel-test","image":"$IMG","command":["bash","-c",$(printf '%s' "$CHECK" | sed 's/\\/\\\\/g; s/"/\\"/g' | awk 'BEGIN{printf "\""} {printf "%s\\n", $0} END{printf "\""}')],
  "env":[{"name":"SSH_HOST","value":"$HOST"},{"name":"SSH_USER","value":"$USER_"},{"name":"SSH_PORT","value":"$PORT"}],
  "volumeMounts":[{"name":"key","mountPath":"/etc/tunnel","readOnly":true}],
  "resources":{"requests":{"cpu":"10m","memory":"16Mi"},"limits":{"memory":"64Mi"}}}],
  "volumes":[{"name":"key","secret":{"secretName":"mysql-ssh-tunnel","defaultMode":288}}]}}
EOF
)

oc run "tunnel-test-$$" --rm -i --restart=Never --quiet --image="$IMG" --overrides="$OVERRIDES" 2>&1 \
  | grep -vE "^Warning|couldn't attach|^pod .* terminated"

#!/usr/bin/env bash
set -uo pipefail

RUN_DIR="build/server-smoke"
BOOT_TIMEOUT_SECONDS="${BOOT_TIMEOUT_SECONDS:-300}"
SETTLE_SECONDS="${SETTLE_SECONDS:-10}"
STOP_TIMEOUT_SECONDS="${STOP_TIMEOUT_SECONDS:-60}"
FAILURE_PATTERN='Crash Report|Exception in thread|Encountered an unexpected exception|^[[:space:]]+at [A-Za-z0-9_.$]+\('

rm -rf "$RUN_DIR"
mkdir -p "$RUN_DIR"
echo "eula=true" > "$RUN_DIR/eula.txt"
touch "$RUN_DIR/server.properties"
log="$RUN_DIR/smoke.log"
console="$RUN_DIR/console"
mkfifo "$console"

./gradlew runServerSmoke --no-daemon --stacktrace < "$console" > "$log" 2>&1 &
server_pid=$!
exec 3> "$console"

fail() {
  echo "::error::$1"
  cat "$log"
  kill "$server_pid" 2> /dev/null
  exit 1
}

deadline=$((SECONDS + BOOT_TIMEOUT_SECONDS))
until grep -q 'Done (' "$log"; do
  kill -0 "$server_pid" 2> /dev/null || fail "The server exited before it finished starting."
  [ "$SECONDS" -lt "$deadline" ] || fail "The server did not finish starting within ${BOOT_TIMEOUT_SECONDS}s."
  sleep 1
done

sleep "$SETTLE_SECONDS"
kill -0 "$server_pid" 2> /dev/null || fail "The server exited while it should have been running."
echo "stop" >&3

deadline=$((SECONDS + STOP_TIMEOUT_SECONDS))
while kill -0 "$server_pid" 2> /dev/null; do
  [ "$SECONDS" -lt "$deadline" ] || fail "The server did not stop within ${STOP_TIMEOUT_SECONDS}s."
  sleep 1
done
wait "$server_pid"
exit_code=$?
exec 3>&-

[ "$exit_code" -eq 0 ] || fail "The server exited with code ${exit_code}."
grep -q 'Stopping server' "$log" || fail "The server never shut down cleanly."
if grep -E "$FAILURE_PATTERN" "$log"; then
  fail "The server log shows an error or crash."
fi
echo "Dedicated server started, ticked for ${SETTLE_SECONDS}s, and stopped cleanly."

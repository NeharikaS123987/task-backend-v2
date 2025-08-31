set -euo pipefail
: "${BASE:=http://localhost:8080}"
: "${AUTH_A:?missing AUTH_A}"
: "${BOARD_ID:?missing BOARD_ID}"
curl -s -o /dev/null -w "cold: %{time_total}\n"  -H "$AUTH_A" "$BASE/api/boards/$BOARD_ID"
curl -s -o /dev/null -w "warm1: %{time_total}\n" -H "$AUTH_A" "$BASE/api/boards/$BOARD_ID"
curl -s -o /dev/null -w "warm2: %{time_total}\n" -H "$AUTH_A" "$BASE/api/boards/$BOARD_ID"

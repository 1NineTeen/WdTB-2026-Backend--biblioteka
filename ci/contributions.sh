#!/bin/sh
# Raport wkładu członków zespołu od poprzedniego tagu lab-N.
# Nie jest oceną automatyczną, tylko punktem wyjścia do rozmowy na ocenie.
set -eu

PREVIOUS=$(git describe --tags --abbrev=0 --match 'lab-*' HEAD^ 2>/dev/null || true)
RANGE=${PREVIOUS:+$PREVIOUS..}HEAD

echo "== Commity per autor (${PREVIOUS:-od początku} → HEAD) =="
git shortlog -sne --no-merges "$RANGE"

echo
echo "== Zmienione linie per autor (bez merge) =="
git log --no-merges --numstat --format='@%aN' "$RANGE" | awk '
    /^@/ { author = substr($0, 2); next }
    NF == 3 && $1 != "-" { added[author] += $1; removed[author] += $2 }
    END { for (a in added) printf "%-32s +%-6d -%d\n", a, added[a], removed[a] }'

echo
echo "== Pull requesty zmergowane do main =="
git log --merges --first-parent --format='%h %aN: %s' "$RANGE" | grep -iE "merge (pull request|branch)" || echo "(brak)"

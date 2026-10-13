#!/usr/bin/env bash
set -e
command -v python3 >/dev/null 2>&1 || { echo 'Python 3 not found.' >&2; exit 1; }
exec python3 -m tool "$@"

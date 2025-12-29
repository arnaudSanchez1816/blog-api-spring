#!/usr/bin/env bash

DIR="$(cd "$(dirname "$0")" && pwd)"
source $DIR/set-env.sh
docker compose stop db
docker compose up -d
echo '🟡 - Waiting for database to be ready...'
$DIR/wait-for-it.sh  "docker compose exec -T db psql -U postgres -c 'select 1'"
echo '🟢 - Database is ready!'
npx prisma migrate dev --name init
if [ "$#" -eq  "0" ]
  then
    vitest run -c ./vitest.config.integration.ts
else
    vitest run -c ./vitest.config.integration.ts --ui
fi
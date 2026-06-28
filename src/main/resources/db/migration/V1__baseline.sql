-- V1 baseline migration.
-- Shared database setup; module tables are introduced in their own migrations.

-- [jooq ignore start]
create extension if not exists pgcrypto;
-- [jooq ignore stop]

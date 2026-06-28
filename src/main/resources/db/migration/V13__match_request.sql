-- Queue of users waiting to be matched into an anonymous chat (in DB, not in-memory: fixes v1 #6).
create table match_request (
    user_id    uuid        primary key references app_user(id) on delete cascade,
    created_at timestamptz not null
);

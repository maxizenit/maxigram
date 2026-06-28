-- One-time tokens for email verification and password reset.
create table one_time_token (
    token       uuid         primary key,
    user_id     uuid         not null references app_user(id),
    purpose     varchar(40)  not null,
    expires_at  timestamptz  not null,
    consumed_at timestamptz
);

create index idx_one_time_token_user on one_time_token (user_id);

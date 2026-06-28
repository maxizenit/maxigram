-- Identity module: user accounts owned by the application's Authorization Server.
create table app_user (
    id             uuid         primary key,
    email          varchar(255) not null unique,
    password_hash  varchar(255) not null,
    email_verified boolean      not null default false,
    created_at     timestamptz  not null
);

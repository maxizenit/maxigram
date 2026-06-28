-- Chat module: one-to-one chats and their messages. Anonymous-chat fields are added in Etap 6.
create table chat (
    id                    bigint      generated always as identity primary key,
    first_participant_id  uuid        not null references app_user(id) on delete cascade,
    second_participant_id uuid        not null references app_user(id) on delete cascade,
    created_at            timestamptz not null
);

create table message (
    id         bigint      generated always as identity primary key,
    chat_id    bigint      not null references chat(id) on delete cascade,
    sender_id  uuid        not null references app_user(id) on delete cascade,
    text       text        not null,
    created_at timestamptz not null,
    read       boolean     not null default false
);

create index idx_chat_first on chat (first_participant_id);
create index idx_chat_second on chat (second_participant_id);
create index idx_message_chat on message (chat_id);

-- Anonymous-chat support: masking flags, mutual de-anonymization agreement, and the
-- link to the regular chat created once both participants agree.
alter table chat
    add column anonymous     boolean not null default false,
    add column first_agreed  boolean not null default false,
    add column second_agreed boolean not null default false,
    add column closed        boolean not null default false,
    add column new_chat_id   bigint references chat(id);

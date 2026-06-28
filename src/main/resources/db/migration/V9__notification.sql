-- Notification module: in-app notifications, delivered live over STOMP and listed via REST.
create table notification (
    id           bigint       generated always as identity primary key,
    recipient_id uuid         not null references app_user(id) on delete cascade,
    type         varchar(40)  not null,
    actor_id     uuid,
    text         varchar(500) not null,
    read         boolean      not null default false,
    created_at   timestamptz  not null
);

create index idx_notification_recipient on notification (recipient_id, created_at desc);

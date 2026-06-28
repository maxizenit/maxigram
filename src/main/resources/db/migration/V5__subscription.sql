-- Social graph: who subscribes to whom. Account-level (references app_user, not profile).
create table subscription (
    subscriber_id uuid        not null references app_user(id) on delete cascade,
    author_id     uuid        not null references app_user(id) on delete cascade,
    created_at    timestamptz not null,
    primary key (subscriber_id, author_id)
);

create index idx_subscription_author on subscription (author_id);

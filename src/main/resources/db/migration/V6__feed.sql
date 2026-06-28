-- Feed module: posts and their comments.
create table post (
    id         bigint      generated always as identity primary key,
    author_id  uuid        not null references app_user(id) on delete cascade,
    text       text        not null,
    created_at timestamptz not null
);

create table comment (
    id         bigint      generated always as identity primary key,
    post_id    bigint      not null references post(id) on delete cascade,
    author_id  uuid        not null references app_user(id) on delete cascade,
    text       text        not null,
    created_at timestamptz not null
);

create index idx_post_author on post (author_id);
create index idx_comment_post on comment (post_id);

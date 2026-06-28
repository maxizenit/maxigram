-- Likes for posts and comments. Separate explicit tables (v1 used one nullable-FK table).
create table post_like (
    post_id   bigint not null references post(id) on delete cascade,
    author_id uuid   not null references app_user(id) on delete cascade,
    primary key (post_id, author_id)
);

create table comment_like (
    comment_id bigint not null references comment(id) on delete cascade,
    author_id  uuid   not null references app_user(id) on delete cascade,
    primary key (comment_id, author_id)
);

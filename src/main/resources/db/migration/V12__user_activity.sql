-- Histogram of a user's activity by local hour (0-23), accumulated at the moment of activity
-- so it stays correct even if the user later changes timezone. Best-effort analytics (no FK).
create table user_activity_hour (
    user_id uuid     not null,
    hour    smallint not null,
    count   integer  not null default 0,
    primary key (user_id, hour)
);

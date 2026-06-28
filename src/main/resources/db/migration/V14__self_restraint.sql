-- Digital wellbeing: a self-imposed window during which the app is blocked for the user.
-- v1 had this table without time columns and the gRPC methods were stubs (#2).
create table self_restraint (
    user_id    uuid        primary key references app_user(id) on delete cascade,
    start_time timestamptz not null,
    end_time   timestamptz not null
);

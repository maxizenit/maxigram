-- Timezone is needed to compute time-of-activity similarity in local hours.
alter table user_profile add column timezone varchar(64) not null default 'UTC';

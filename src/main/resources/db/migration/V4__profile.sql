-- Profile module: user profiles, interests catalogue, and the many-to-many link.
create table user_profile (
    id         uuid         primary key references app_user(id),
    first_name varchar(100) not null,
    last_name  varchar(100) not null,
    birthdate  date         not null
);

create table interest (
    id   bigint       generated always as identity primary key,
    name varchar(100) not null unique
);

create table user_interest (
    user_id     uuid   not null references user_profile(id) on delete cascade,
    interest_id bigint not null references interest(id),
    primary key (user_id, interest_id)
);

-- [jooq ignore start]
insert into interest (name) values
    ('Футбол'),
    ('Программирование'),
    ('Музыка'),
    ('Литература'),
    ('Путешествия');
-- [jooq ignore stop]

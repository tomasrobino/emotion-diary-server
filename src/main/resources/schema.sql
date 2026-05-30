create table if not exists moodboard
(
    id             bigint auto_increment
        primary key,
    content        longtext     null,
    is_public      bit          not null,
    owner_username varchar(255) null,
    name           varchar(100) null,
    thumbnail      longblob     null
);

create table if not exists moodboard_likes
(
    id             bigint auto_increment
        primary key,
    liker_username varchar(255) not null,
    moodboard_id   bigint       not null,
    constraint UKdxp746oxrm3qaotsqby7ic1nw
        unique (moodboard_id, liker_username)
);

create table if not exists moodboard_permissions
(
    id                 bigint auto_increment
        primary key,
    moodboard_id       bigint       not null,
    owner_username     varchar(255) not null,
    permitted_username varchar(255) not null,
    constraint UK3f0q8b8a47tah2dv9lh9618jj
        unique (moodboard_id, permitted_username)
);

create table if not exists moodboard_media
(
    id                bigint auto_increment
        primary key,
    moodboard_id      bigint       not null,
    content_type      varchar(255) not null,
    original_filename varchar(255) null,
    data              longblob     not null,
    size_bytes        bigint       not null,
    created_at        datetime(6)  not null
);

create table if not exists revoked_tokens
(
    jti        varchar(255) not null
        primary key,
    expires_at datetime(6)  null
);

create table if not exists user_seq
(
    next_not_cached_value bigint(21)          not null,
    minimum_value         bigint(21)          not null,
    maximum_value         bigint(21)          not null,
    start_value           bigint(21)          not null comment 'start value when sequences is created or value if RESTART is used',
    increment             bigint(21)          not null comment 'increment value',
    cache_size            bigint(21) unsigned not null,
    cycle_option          tinyint(1) unsigned not null comment '0 if no cycles are allowed, 1 if the sequence should begin a new cycle when maximum_value is passed',
    cycle_count           bigint(21)          not null comment 'How many cycles have been done'
);

create table if not exists users
(
    id       bigint       not null
        primary key,
    password varchar(255) null,
    username varchar(255) null,
    constraint UKsb8bbouer5wak8vyiiy4pf2bx
        unique (username)
);

create table if not exists diary_entry
(
    id                   bigint auto_increment
        primary key,
    owner_username       varchar(255) not null,
    entry_date           date         not null,
    mood_score           int          not null,
    text_note            text         null,
    linked_moodboard_id  bigint       null,
    reminder_at          datetime(6)  null,
    created_at           datetime(6)  not null,
    updated_at           datetime(6)  not null,
    constraint UK_diary_entry_user_date
        unique (owner_username, entry_date)
);

create table if not exists quiz_template
(
    id       bigint auto_increment
        primary key,
    question varchar(500) not null,
    type     varchar(20)  not null,
    options  longtext     null,
    sort_order int        not null
);

create table if not exists quiz_response
(
    id              bigint auto_increment
        primary key,
    owner_username  varchar(255) not null,
    response_date   date         not null,
    answers         longtext     not null,
    created_at      datetime(6)  not null,
    constraint UK_quiz_response_user_date
        unique (owner_username, response_date)
);

drop table if exists users cascade;
drop table if exists grup cascade;
drop table if exists schedule cascade;
drop table if exists analytics cascade;

create table schedule (
    schedule_id varchar(20) primary key,
    schedule_name varchar(255),
    data json
);

create table grup (
    group_id varchar(20) primary key,
    schedule_id varchar(20),
    group_name varchar(255),
    description text,
    data json,
    constraint fk_schedule
        foreign key (schedule_id)
        references schedule (schedule_id)
        on delete cascade
        on update cascade
);

create table users ( 
    user_id text primary key, 
    full_name text,  
    password varchar(255), 
    email varchar(255), 
    group_id varchar(20), 
    role varchar(20),
    created_at timestamp default current_timestamp,
    personal_permissions text,
    avatar varchar(255),
    constraint fk_group
        foreign key (group_id)
        references grup (group_id)
        on delete cascade
        on update cascade
);

create table analytics (
prompt text,
answer text
)

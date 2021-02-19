create table user
(
    id          int auto_increment primary key,
    username    varchar(20)                        null,
    password    varchar(20)                        null,
    sex         tinyint  default 0                 not null,
    create_time datetime default CURRENT_TIMESTAMP not null,
    update_time datetime default CURRENT_TIMESTAMP not null,
    deleted     tinyint  default 0                 not null
);
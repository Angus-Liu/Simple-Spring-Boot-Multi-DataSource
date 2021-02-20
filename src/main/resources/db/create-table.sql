# DDL
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

# DATA
INSERT INTO multi_datasource_master.user (id, username, password, sex) VALUES (1, 'master_username', 'master_password', 1);
INSERT INTO multi_datasource_slave.user (id, username, password, sex) VALUES (1, 'slave_username', 'slave_password', 1);
INSERT INTO multi_datasource_slave_2.user (id, username, password, sex) VALUES (1, 'slave_2_username', 'slave_2_password', 1);
create table `sample_db`.`todo` (
  `todo_id` int unsigned not null auto_increment,
  `todo_title` varchar(255),
  `todo_status` enum('waiting', 'doing', 'done') not null default 'waiting',
  `created_at` timestamp not null default current_timestamp,
  `updated_at` timestamp not null default current_timestamp on update current_timestamp,
  primary key (`todo_id`)
) default charset=utf8;

insert into `sample_db`.`todo` (todo_id, todo_title, todo_status) values
(1, 'title1', 'waiting'),
(2, 'title2', 'doing'),
(3, 'title3', 'done')
;

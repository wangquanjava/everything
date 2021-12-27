CREATE TABLE `input` (
  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `db_name` varchar(100) NOT NULL default '',
  `table_name` varchar(100) NOT NULL default '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='输入源配置';

insert into input(db_name, table_name) values('poi', 'poi');

drop table if exists `output`;
drop table if exists `flow`;
CREATE TABLE `flow` (
  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `input_id` int NOT NULL default 0,
  `table_name` varchar(100) NOT NULL default '',
  `cols` varchar(400) NOT NULL default '',
  `unique_cols` varchar(400) NOT NULL default '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='输出绑定关系';
insert into flow(input_Id,table_name, cols, unique_cols) values(1,'poi_valid', 'id,name,valid', 'id');
insert into flow(input_Id,table_name, cols, unique_cols) values(1,'poi_status', 'id,name,valid', 'id');
insert into flow(input_Id,table_name, cols, unique_cols) values(1,'poi_222', 'id,name,valid', 'id');


delete from flow where id = 2;



CREATE TABLE `poi` (
  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` varchar(100) NOT NULL default '',
  `valid` int NOT NULL default 0,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店表';

CREATE TABLE `poi_valid` (
  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` varchar(100) NOT NULL default '',
  `valid` int NOT NULL default 0,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='有效门店表';

insert into poi(name, valid) value ('麦当劳', 1);
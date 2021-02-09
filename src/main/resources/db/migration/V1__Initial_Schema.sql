CREATE TABLE tb_todotask (
  id bigint auto_increment,
  itemName varchar(50) not null,
  description varchar(255) not null,
  date timestamp,
  primary key (id));

INSERT INTO tb_todotask (id ,itemName ,description,date) VALUES (1,'java','java task 1','1991-01-20'),(2,'hibernate','hibernate task 1','1991-01-20')
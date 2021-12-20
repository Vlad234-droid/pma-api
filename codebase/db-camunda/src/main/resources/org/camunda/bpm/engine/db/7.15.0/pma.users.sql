INSERT INTO camunda.act_id_group (id_,rev_,name_,type_)
VALUES ('camunda-admin',1,'camunda BPM Administrators','SYSTEM');

INSERT INTO camunda.act_id_user (id_,rev_,first_,last_,email_,pwd_,salt_,lock_exp_time_,attempts_,picture_id_)
VALUES ('admin',1,'Admin','Admin','admin@localhost','{SHA-512}JMbfwbF6rgvahJ8oZP31giMooM3Mw8M6COHYa/cEUyDWCaxAzBNRfrsPM2zf5JoHo9f/07zqCozCgEyTv1KuGw==','hobqq6TYDqmIfWK79r+xBA==',NULL,NULL,NULL);

INSERT INTO camunda.act_id_membership (user_id_,group_id_)
VALUES ('admin','camunda-admin');

INSERT INTO camunda.act_ru_authorization (id_,rev_,type_,group_id_,user_id_,resource_type_,resource_id_,perms_,removal_time_,root_proc_inst_id_)
VALUES
 ('91676d11-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,1,'*',2147483647,NULL,NULL),
 ('91645fd0-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,0,'*',2147483647,NULL,NULL),
 ('9168f3b2-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,2,'*',2147483647,NULL,NULL),
 ('916a5343-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,3,'*',2147483647,NULL,NULL),
 ('916bd9e4-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,4,'*',2147483647,NULL,NULL),
 ('916d3975-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,5,'*',2147483647,NULL,NULL),
 ('916e9906-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,6,'*',2147483647,NULL,NULL),
 ('91701fa7-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,7,'*',2147483647,NULL,NULL),
 ('91717f38-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,8,'*',2147483647,NULL,NULL),
 ('917305d9-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,9,'*',2147483647,NULL,NULL);
INSERT INTO camunda.act_ru_authorization (id_,rev_,type_,group_id_,user_id_,resource_type_,resource_id_,perms_,removal_time_,root_proc_inst_id_)
VALUES
 ('91743e5a-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,10,'*',2147483647,NULL,NULL),
 ('917576db-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,11,'*',2147483647,NULL,NULL),
 ('9176af5c-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,12,'*',2147483647,NULL,NULL),
 ('91780eed-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,13,'*',2147483647,NULL,NULL),
 ('9179476e-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,14,'*',2147483647,NULL,NULL),
 ('917aa6ff-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,15,'*',2147483647,NULL,NULL),
 ('917c54b0-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,16,'*',2147483647,NULL,NULL),
 ('917d8d31-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,17,'*',2147483647,NULL,NULL),
 ('917ec5b2-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,18,'*',2147483647,NULL,NULL),
 ('917ffe33-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,19,'*',2147483647,NULL,NULL);
INSERT INTO camunda.act_ru_authorization (id_,rev_,type_,group_id_,user_id_,resource_type_,resource_id_,perms_,removal_time_,root_proc_inst_id_)
VALUES 
 ('91815dc4-58d7-11ec-be4c-002b67d4b67e',1,1,'camunda-admin',NULL,20,'*',2147483647,NULL,NULL);
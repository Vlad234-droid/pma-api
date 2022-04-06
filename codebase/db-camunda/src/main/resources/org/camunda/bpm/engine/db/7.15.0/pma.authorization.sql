--liquibase formatted sql
/*
-- Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
-- under one or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information regarding copyright
-- ownership. Camunda licenses this file to you under the Apache License,
-- Version 2.0; you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
*/
--changeset akuzmin:insert-to-engine-tables-identity-2 splitStatements:true endDelimiter:;

INSERT INTO act_id_group (id_, rev_, name_, type_)
VALUES ('tesco-admin', 1, 'Tesco Admin', 'SYSTEM'),
       ('tesco-process-manager', 1, 'Tesco Process Manager', 'SYSTEM');

INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_,
                                  removal_time_, root_proc_inst_id_)
VALUES ('a18f53ca-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-admin', NULL, 2, '*', 2147483647, NULL, NULL),
       ('e4866242-b3fe-11ec-b304-002b67d4b67e', 2, 1, 'tesco-admin', NULL, 0, '*', 2147483647, NULL, NULL),
       ('09acbaca-b3ff-11ec-b304-002b67d4b67e', 4, 1, 'tesco-process-manager', NULL, 0, 'cockpit', 2147483647, NULL, NULL),
       ('133d1407-b4db-11ec-a315-002b67d4b67e', 2, 1, 'tesco-process-manager', NULL, 0, 'tasklist', 2147483647, NULL, NULL),
       ('fe7e7c5a-b3fe-11ec-b304-002b67d4b67e', 2, 1, 'tesco-admin', NULL, 4, '*', 2147483647, NULL, NULL),
       ('8a2f4a92-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-admin', NULL, 13, '*', 2147483647, NULL, NULL),
       ('8ebbfeaa-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-admin', NULL, 10, '*', 2147483647, NULL, NULL),
       ('1aa70b12-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-process-manager', NULL, 10, '*', 2147483647, NULL, NULL),
       ('936b7d02-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-admin', NULL, 14, '*', 2147483647, NULL, NULL);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_,
                                  removal_time_, root_proc_inst_id_)
VALUES ('261e3962-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-process-manager', NULL, 14, '*', 2147483647, NULL, NULL),
       ('97c75d1a-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-admin', NULL, 9, '*', 2147483647, NULL, NULL),
       ('9cab57f2-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-admin', NULL, 5, '*', 2147483647, NULL, NULL),
       ('304f0502-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-process-manager', NULL, 5, '*', 2147483647, NULL, NULL),
       ('a5ced242-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-admin', NULL, 3, '*', 2147483647, NULL, NULL),
       ('ab3173fa-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-admin', NULL, 20, '*', 2147483647, NULL, NULL),
       ('40c1399a-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-process-manager', NULL, 20, '*', 2147483647, NULL, NULL),
       ('af4b4202-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-admin', NULL, 19, '*', 2147483647, NULL, NULL),
       ('464aeb52-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-process-manager', NULL, 19, '*', 2147483647, NULL, NULL),
       ('b390dafa-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-admin', NULL, 17, '*', 2147483647, NULL, NULL);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_,
                                  removal_time_, root_proc_inst_id_)
VALUES ('b84a1d52-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-admin', NULL, 6, '*', 2147483647, NULL, NULL),
       ('4fc73402-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-process-manager', NULL, 6, '*', 2147483647, NULL, NULL),
       ('bc540cda-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-admin', NULL, 8, '*', 2147483647, NULL, NULL),
       ('54ef8bda-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-process-manager', NULL, 8, '*', 2147483647, NULL, NULL),
       ('c089a042-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-admin', NULL, 7, '*', 2147483647, NULL, NULL),
       ('59919cb2-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-process-manager', NULL, 7, '*', 2147483647, NULL, NULL),
       ('c4de064a-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-admin', NULL, 11, '*', 2147483647, NULL, NULL),
       ('c9466982-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-admin', NULL, 12, '*', 2147483647, NULL, NULL),
       ('ce98de9a-b3ff-11ec-b304-002b67d4b67e', 2, 1, 'tesco-admin', NULL, 1, '*', 2147483647, NULL, NULL);

--rollback DELETE FROM act_ru_authorization WHERE group_id_ = 'tesco-admin' OR group_id_ = 'tesco-process-manager';

--rollback DELETE FROM act_id_group WHERE id_ = 'tesco-admin' OR id_ = 'tesco-process-manager';


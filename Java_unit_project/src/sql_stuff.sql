
CREATE TABLE person(
                       id            BIGSERIAL NOT NULL PRIMARY KEY,
                       username      VARCHAR(50) NOT NULL UNIQUE,
                       master_ivspec VARCHAR(100) NOT NULL ,
                       master_key    VARCHAR(100) NOT NULL ,
                       master_image  BYTEA
);

CREATE TABLE password(
                         id BIGSERIAL NOT NULL PRIMARY KEY,
                         title TEXT,
                         ivspec TEXT,
                         key TEXT,
                         image BYTEA,
                         person_id BIGINT REFERENCES person(id)
);

insert into person (id, username, master_ivspec, master_key, master_image) values (1, 'mcordingly0', 'Ut at dolor quis odio consequat varius.', 'Morbi vestibulum, velit id pretium iaculis, diam erat fermentum justo, nec condimentum neque sapien placerat ante.', null);
insert into person (id, username, master_ivspec, master_key, master_image) values (2, 'sbeneix1', 'Maecenas leo odio, condimentum id, luctus nec, molestie sed, justo.', 'Nulla tellus. In sagittis dui vel nisl.', null);
insert into person (id, username, master_ivspec, master_key, master_image) values (3, 'rbuie2', 'Mauris sit amet eros. Suspendisse accumsan tortor quis turpis.', 'Aenean fermentum. Donec ut mauris eget massa tempor convallis.', null);
insert into person (id, username, master_ivspec, master_key, master_image) values (4, 'bpedroni3', 'Donec semper sapien a libero. Nam dui.', 'Sed sagittis. Nam congue, risus semper porta volutpat, quam pede lobortis ligula, sit amet eleifend pede libero quis orci.', null);
insert into person (id, username, master_ivspec, master_key, master_image) values (5, 'bdunckley4', 'Etiam pretium iaculis justo.', 'Nam tristique tortor eu pede.', null);
insert into person (id, username, master_ivspec, master_key, master_image) values (6, 'lreinbach5', 'Lorem ipsum dolor sit amet, consectetuer adipiscing elit.', 'Donec ut dolor.', null);
insert into person (id, username, master_ivspec, master_key, master_image) values (7, 'kedsall6', 'Duis at velit eu est congue elementum.', 'Mauris ullamcorper purus sit amet nulla. Quisque arcu libero, rutrum ac, lobortis vel, dapibus at, diam.', null);
insert into person (id, username, master_ivspec, master_key, master_image) values (8, 'ashenfish7', 'Nulla ac enim. In tempor, turpis nec euismod scelerisque, quam turpis adipiscing lorem, vitae mattis nibh ligula nec sem.', 'Integer ac leo.', null);
insert into person (id, username, master_ivspec, master_key, master_image) values (9, 'clangstrath8', 'Proin interdum mauris non ligula pellentesque ultrices.', 'Fusce posuere felis sed lacus.', null);
insert into person (id, username, master_ivspec, master_key, master_image) values (10, 'dborley9', 'In hac habitasse platea dictumst. Maecenas ut massa quis augue luctus tincidunt.', 'Pellentesque viverra pede ac diam. Cras pellentesque volutpat dui.', null);
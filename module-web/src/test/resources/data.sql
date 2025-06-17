INSERT INTO authors(id, name, create_date, last_update_date)
values (1, 'Author1', now(), now());
INSERT INTO authors(id, name, create_date, last_update_date)
values (2, 'Author2', now(), now());
INSERT INTO authors(id, name, create_date, last_update_date)
values (3, 'Author3', now(), now());
INSERT INTO authors(id, name, create_date, last_update_date)
values (4, 'Author4', now(), now());
INSERT INTO authors(id, name, create_date, last_update_date)
values (5, 'Author5', now(), now());
INSERT INTO authors(id, name, create_date, last_update_date)
values (6, 'Author6', now(), now());
ALTER TABLE authors
    ALTER COLUMN id RESTART WITH 7;

INSERT INTO security_users(id, firstname, lastname, password, role, username)
values (1, 'test', 'test', '$2a$12$.X68yudtJQGDWOrK5XamoeSNgmRpcxdFsrt66WinJYnAcekuFlN9W', 'USER', 'test');
INSERT INTO security_users(id, firstname, lastname, password, role, username)
values (2, 'admin', 'admin', '$2a$12$VSXMyJ5bjoNmZJCC0hDgDeIdVwdk8ZRXRXL9s67XzZZKquVLKfg4m', 'ADMIN', 'admin');
ALTER TABLE security_users
    ALTER COLUMN id RESTART WITH 3;

INSERT INTO news(id, content, create_date, last_update_date, title, author_id)
values (1, 'content1', now(), now(), 'title1', 1);
INSERT INTO news(id, content, create_date, last_update_date, title, author_id)
values (2, 'content2', now(), now(), 'title2', 2);
INSERT INTO news(id, content, create_date, last_update_date, title, author_id)
values (3, 'content3', now(), now(), 'title3', 3);
INSERT INTO news(id, content, create_date, last_update_date, title, author_id)
values (4, 'content4', now(), now(), 'title4', 4);
INSERT INTO news(id, content, create_date, last_update_date, title, author_id)
values (5, 'content5', now(), now(), 'title5', 5);
ALTER TABLE news
    ALTER COLUMN id RESTART WITH 6;

INSERT INTO comments(id, content, create_date, last_update_date, news_id)
values (1, 'content1', now(), now(), 1);
INSERT INTO comments(id, content, create_date, last_update_date, news_id)
values (2, 'content2', now(), now(), 2);
INSERT INTO comments(id, content, create_date, last_update_date, news_id)
values (3, 'content3', now(), now(), 3);
INSERT INTO comments(id, content, create_date, last_update_date, news_id)
values (4, 'content4', now(), now(), 4);
INSERT INTO comments(id, content, create_date, last_update_date, news_id)
values (5, 'content5', now(), now(), 5);
INSERT INTO comments(id, content, create_date, last_update_date, news_id)
values (6, 'content6', now(), now(), 3);
ALTER TABLE comments
    ALTER COLUMN id RESTART WITH 7;

INSERT INTO tags(id, name)
values (1, 'name1');
INSERT INTO tags(id, name)
values (2, 'name2');
INSERT INTO tags(id, name)
values (3, 'name3');
INSERT INTO tags(id, name)
values (4, 'name4');
INSERT INTO tags(id, name)
values (5, 'name5');
ALTER TABLE tags
    ALTER COLUMN id RESTART WITH 6;

INSERT INTO news_tags(news_id, tag_id)
values (1, 1);
INSERT INTO news_tags(news_id, tag_id)
values (2, 2);
INSERT INTO news_tags(news_id, tag_id)
values (3, 3);
INSERT INTO news_tags(news_id, tag_id)
values (4, 4);
INSERT INTO news_tags(news_id, tag_id)
values (5, 5);

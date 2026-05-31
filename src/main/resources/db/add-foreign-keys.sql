-- One-off migration: add foreign keys to an existing MariaDB database.
--
-- Prerequisites:
--   1. Backup the database.
--   2. Stop the application (or ensure no concurrent writes).
--   3. Every username referenced in moodboard / likes / permissions / diary_entry
--      must exist in users.username (register users via the app or INSERT).
--      Seed data (insert-moodboards.sql) uses owners like 'aaaa' — create that user first.
--
-- Run once:
--   mariadb -u USER -p DATABASE_NAME < add-foreign-keys.sql
--
-- If a constraint already exists, skip or drop the conflicting constraint before re-running.
-- Hibernate ddl-auto=update does not add these FKs on existing tables; this script is required.

-- Step A: moodboard_id orphans
delete from moodboard_likes
where moodboard_id not in (select id from moodboard);

delete from moodboard_permissions
where moodboard_id not in (select id from moodboard);

delete from moodboard_media
where moodboard_id not in (select id from moodboard);

update diary_entry
set linked_moodboard_id = null
where linked_moodboard_id is not null
  and linked_moodboard_id not in (select id from moodboard);

-- Step B: username orphans
update moodboard
set owner_username = null
where owner_username is not null
  and owner_username not in (select username from users);

delete from moodboard_likes
where liker_username not in (select username from users);

delete from moodboard_permissions
where owner_username not in (select username from users)
   or permitted_username not in (select username from users);

delete from diary_entry
where owner_username not in (select username from users);

-- Step C: moodboard foreign keys
alter table moodboard_likes
    add constraint fk_likes_moodboard
        foreign key (moodboard_id) references moodboard (id) on delete cascade;

alter table moodboard_permissions
    add constraint fk_permissions_moodboard
        foreign key (moodboard_id) references moodboard (id) on delete cascade;

alter table moodboard_media
    add constraint fk_media_moodboard
        foreign key (moodboard_id) references moodboard (id) on delete cascade;

alter table diary_entry
    add constraint fk_diary_linked_moodboard
        foreign key (linked_moodboard_id) references moodboard (id) on delete set null;

-- Step D: user foreign keys
alter table moodboard
    add constraint fk_moodboard_owner
        foreign key (owner_username) references users (username) on delete restrict;

alter table moodboard_likes
    add constraint fk_likes_liker
        foreign key (liker_username) references users (username) on delete restrict;

alter table moodboard_permissions
    add constraint fk_permissions_owner
        foreign key (owner_username) references users (username) on delete restrict;

alter table moodboard_permissions
    add constraint fk_permissions_permitted
        foreign key (permitted_username) references users (username) on delete restrict;

alter table diary_entry
    add constraint fk_diary_owner
        foreign key (owner_username) references users (username) on delete restrict;

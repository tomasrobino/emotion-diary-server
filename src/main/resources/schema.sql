CREATE TABLE IF NOT EXISTS moodboard_likes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    moodboard_id BIGINT NOT NULL,
    liker_username VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_moodboard_liker UNIQUE (moodboard_id, liker_username)
);

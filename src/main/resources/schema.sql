CREATE TABLE IF NOT EXISTS moodboard_likes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    moodboard_id BIGINT NOT NULL,
    liker_username VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_moodboard_liker UNIQUE (moodboard_id, liker_username)
);

CREATE TABLE IF NOT EXISTS moodboard_media (
    id BIGINT NOT NULL AUTO_INCREMENT,
    moodboard_id BIGINT NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255),
    data LONGBLOB NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id)
);

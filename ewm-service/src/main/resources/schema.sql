CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS locations (
    id BIGSERIAL PRIMARY KEY,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS events (
    id BIGSERIAL PRIMARY KEY,
    annotation TEXT NOT NULL CHECK (char_length(annotation) >= 20 AND char_length(annotation) <= 2000),
    created_on TIMESTAMP NOT NULL,
    description TEXT CHECK (char_length(description) >= 20 AND char_length(description) <= 7000),
    category_id BIGINT NOT NULL REFERENCES categories(id),
    confirmed_requests INT DEFAULT 0,
    location_id BIGINT REFERENCES locations(id),
    event_date TIMESTAMP NOT NULL,
    initiator_id BIGINT NOT NULL REFERENCES users(id),
    paid BOOLEAN NOT NULL,
    participant_limit INT DEFAULT 0 CHECK (participant_limit >= 0),
    published_on TIMESTAMP,
    request_moderation BOOLEAN NOT NULL,
    state VARCHAR(50),
    title TEXT NOT NULL CHECK (char_length(title) >= 3 AND char_length(title) <= 120),
    views BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS requests (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL REFERENCES users(id),
    event_id BIGINT NOT NULL REFERENCES events(id),
    created TIMESTAMP,
    status VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS compilations (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    pinned BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS compilation_events (
    compilation_id BIGINT NOT NULL REFERENCES compilations(id) ON DELETE CASCADE,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    PRIMARY KEY (compilation_id, event_id)
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    created TIMESTAMP NOT NULL,
    creator_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE
)

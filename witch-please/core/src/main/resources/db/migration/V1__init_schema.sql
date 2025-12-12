CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE spreads (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    card_count INT NOT NULL,
    description TEXT NOT NULL,
    spread_status TEXT NOT NULL CHECK (spread_status IN ('Draft', 'Scheduled', 'PreviewPublished', 'Published', 'Archived')),
    photo_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    scheduled_at TIMESTAMPTZ,
    card_of_day_at TIMESTAMPTZ,
    published_at TIMESTAMPTZ,

    CONSTRAINT chk_spread_status_times CHECK (
        (spread_status = 'Draft' AND scheduled_at IS NULL AND card_of_day_at IS NULL AND published_at IS NULL) OR
        (spread_status = 'Scheduled' AND scheduled_at IS NOT NULL AND card_of_day_at IS NOT NULL AND published_at IS NULL) OR
        (spread_status = 'PreviewPublished' AND scheduled_at IS NOT NULL AND card_of_day_at IS NOT NULL AND published_at IS NULL) OR
        (spread_status = 'Published' AND scheduled_at IS NOT NULL AND card_of_day_at IS NOT NULL AND published_at IS NOT NULL) OR
        (spread_status = 'Archived')
    )
);

CREATE INDEX idx_spreads_project_id ON spreads(project_id);
CREATE INDEX idx_spreads_scheduled_at
    ON spreads(scheduled_at)
    WHERE spread_status = 'Scheduled';
CREATE INDEX idx_spreads_preview_published_at
    ON spreads(card_of_day_at)
    WHERE spread_status = 'PreviewPublished';
CREATE INDEX idx_spreads_published_at
    ON spreads(published_at)
    WHERE spread_status = 'Published';

CREATE TABLE cards (
    id UUID PRIMARY KEY,
    position INT NOT NULL,
    spread_id UUID NOT NULL REFERENCES spreads(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    photo_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_cards_spread_position UNIQUE (spread_id, position)
);

CREATE INDEX idx_cards_spread_id ON cards(spread_id);

CREATE TABLE photos (
    id UUID PRIMARY KEY,
    file_id UUID NOT NULL,
    owner_type TEXT NOT NULL CHECK (owner_type IN ('Spread', 'Card')),
    owner_id UUID NOT NULL,
    storage_type TEXT NOT NULL CHECK (storage_type IN ('Local', 'S3')),
    source_type TEXT NOT NULL CHECK (source_type IN ('Telegram', 'S3')),
    source_id TEXT NOT NULL,
    path TEXT,
    bucket TEXT,
    key TEXT
);

CREATE INDEX idx_spreads_photo_id ON spreads(photo_id);
CREATE INDEX idx_cards_photo_id ON cards(photo_id);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    client_type TEXT NOT NULL CHECK (client_type IN ('Telegram', 'Web', 'Mobile')),
    client_id TEXT NOT NULL,
    secret_hash TEXT NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE user_projects (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    role TEXT NOT NULL CHECK (role IN ('PreProject', 'Admin', 'User')),
    PRIMARY KEY (user_id, project_id)
);

CREATE INDEX idx_user_projects_user_id ON user_projects(user_id);
CREATE INDEX idx_user_projects_project_id ON user_projects(project_id);
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
    spread_status TEXT NOT NULL CHECK (spread_status IN ('Draft', 'Ready', 'Published', 'Archived')),
    cover_photo_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    scheduled_at TIMESTAMPTZ,
    published_at TIMESTAMPTZ
);

CREATE INDEX idx_spreads_project_id ON spreads(project_id);

CREATE TABLE photos (
    id UUID PRIMARY KEY,
    storage_type TEXT NOT NULL CHECK (storage_type IN ('Local', 'S3')),
    owner_type TEXT NOT NULL CHECK (owner_type IN ('Spread', 'Card')),
    owner_id UUID NOT NULL,
    path TEXT,
    bucket TEXT,
    key TEXT
);

CREATE TABLE cards (
    id UUID PRIMARY KEY,
    spread_id UUID NOT NULL REFERENCES spreads(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    cover_photo_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

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
CREATE INDEX idx_user_projects_role ON user_projects(role);
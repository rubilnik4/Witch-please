CREATE TABLE spreads (
    id UUID PRIMARY KEY,
    title TEXT NOT NULL,
    card_count INT NOT NULL,
    spread_status TEXT NOT NULL CHECK (spread_status IN ('Draft', 'Ready', 'Published', 'Archived')),
    cover_photo_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    scheduled_at TIMESTAMPTZ,
    published_at TIMESTAMPTZ
);

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
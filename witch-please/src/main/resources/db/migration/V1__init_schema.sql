CREATE TABLE photos (
    id UUID PRIMARY KEY,
    storage_type TEXT NOT NULL CHECK (storage_type IN ('Local', 'S3')),
    data TEXT NOT NULL
);

CREATE TABLE spreads (
    id UUID PRIMARY KEY,
    title TEXT NOT NULL,
    card_count INT NOT NULL,
    spread_status TEXT NOT NULL CHECK (spread_status IN ('Draft', 'Published', 'Archived')),
    cover_photo_id UUID NOT NULL REFERENCES photos(id) ON DELETE CASCADE,
    time TIMESTAMP NOT NULL
);
CREATE TABLE IF NOT EXISTS prices (
    id UUID PRIMARY KEY,
    asset_id TEXT NOT NULL,
    value NUMERIC NOT NULL,
    time TIMESTAMPTZ NOT NULL,
    UNIQUE (asset_id, time)
    );

CREATE INDEX IF NOT EXISTS idx_prices_asset_time ON prices (asset_id, time DESC);

CREATE TABLE IF NOT EXISTS spreads (
    id UUID PRIMARY KEY,
    price_a_id UUID NOT NULL REFERENCES prices(id),
    price_b_id UUID NOT NULL REFERENCES prices(id),
    asset_spread_id TEXT NOT NULL,
    value NUMERIC NOT NULL,
    time TIMESTAMPTZ NOT NULL,
    UNIQUE (asset_spread_id, time)
    );

CREATE INDEX IF NOT EXISTS idx_spreads_unique ON spreads (asset_spread_id, time);
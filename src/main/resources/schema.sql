CREATE TABLE IF NOT EXISTS partner_events (
    id SERIAL PRIMARY KEY,
    partner_id VARCHAR(50),
    event_json TEXT,
    normalized_text TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
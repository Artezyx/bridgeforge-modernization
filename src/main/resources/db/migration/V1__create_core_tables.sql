CREATE TABLE customers (
    id BIGINT PRIMARY KEY,
    full_name VARCHAR(140) NOT NULL,
    email VARCHAR(240) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id BIGINT NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    payload VARCHAR(2048) NOT NULL,
    published BOOLEAN NOT NULL,
    attempts INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_outbox_customer FOREIGN KEY (aggregate_id) REFERENCES customers(id)
);
CREATE INDEX ix_outbox_delivery ON outbox_events (published, attempts, created_at);

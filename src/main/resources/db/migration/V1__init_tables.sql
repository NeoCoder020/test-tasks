CREATE TABLE products
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    type        VARCHAR(50),
    description TEXT,
    image_url   TEXT
);

CREATE TABLE variants
(
    id         BIGSERIAL PRIMARY KEY,
    product_id BIGINT       NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    title      VARCHAR(100) NOT NULL,
    position   SMALLINT,
    price      NUMERIC(10, 2)  NOT NULL
);
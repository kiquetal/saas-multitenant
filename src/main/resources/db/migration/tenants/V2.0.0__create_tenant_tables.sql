CREATE SEQUENCE IF NOT EXISTS my_entity_seq
    INCREMENT 50
    START 1;
CREATE TABLE my_entity (
    id BIGINT PRIMARY KEY DEFAULT  nextval('my_entity_seq'),
    name VARCHAR(255)
);


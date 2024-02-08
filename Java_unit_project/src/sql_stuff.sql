
CREATE TABLE password(
                         id BIGSERIAL NOT NULL PRIMARY KEY,
                         url TEXT,
                         description TEXT,
                         hash BYTEA,
                         salt BYTEA,
                         image VARCHAR(50),
                         person_id BIGINT REFERENCES person(id)
);

CREATE TABLE person(
                       id BIGSERIAL NOT NULL PRIMARY KEY,
                       f_name VARCHAR(25),
                       l_name VARCHAR(25),
                       master_salt VARCHAR(50),
                       master_pass VARCHAR(50)
)
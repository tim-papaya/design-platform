CREATE TABLE users (
	unique_id int8 NOT NULL,
	user_id int8 NOT NULL,
	user_prompt varchar(255) NULL,
	user_state varchar(255) NULL,
	generations int4 NULL,
	CONSTRAINT users_pkey PRIMARY KEY (unique_id)
);

CREATE TABLE photos (
	unique_id int8 NOT NULL,
	user_unique_id int8 NOT NULL,
	file_id varchar(255) NULL,
	file_unique_id varchar(255) NULL,
	CONSTRAINT photos_pkey PRIMARY KEY (unique_id)
);


-- image_bot.photos foreign keys

ALTER TABLE photos ADD CONSTRAINT fkdc51qcxbccqeqaj69mly3vbra FOREIGN KEY (user_unique_id) REFERENCES users(unique_id);

CREATE SEQUENCE photos_seq
	INCREMENT BY 50
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

CREATE SEQUENCE users_seq
	INCREMENT BY 50
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

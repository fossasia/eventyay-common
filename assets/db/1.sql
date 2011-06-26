CREATE TABLE events (
	_id  TEXT NOT NULL PRIMARY KEY,
	starts INT NOT NULL,
	ends INT NOT NULL,
	title TEXT,
	description TEXT,
	location TEXT,
	speaker TEXT
);

CREATE TABLE favorites (
	_id  TEXT NOT NULL PRIMARY KEY,
	favorite INT
);

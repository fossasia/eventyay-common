CREATE TABLE events (
	event  TEXT NOT NULL,
	id     TEXT NOT NULL,
	starts INT NOT NULL,
	ends INT NOT NULL,
	title TEXT,
	description TEXT,
	location TEXT,
	speaker TEXT,
	
	PRIMARY KEY (event, id)
);
	
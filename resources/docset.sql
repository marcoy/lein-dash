-- name: create-table!
CREATE TABLE searchIndex(id INTEGER PRIMARY KEY, name TEXT, type TEXT, path TEXT);

-- name: create-index!
CREATE UNIQUE INDEX anchor ON searchIndex (name, type, path);

-- name: insert-info!
INSERT OR IGNORE INTO searchIndex(name, type, path) VALUES (:name, :type, :path);

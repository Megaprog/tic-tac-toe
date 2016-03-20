CREATE TABLE IF NOT EXISTS pending (
  key text,
  time timestamp,
  name text,
  PRIMARY KEY (key, time, name)
)
WITH CLUSTERING ORDER BY (time DESC);
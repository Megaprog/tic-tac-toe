CREATE TABLE IF NOT EXISTS claim (
  key text,
  time timestamp,
  name text,
  PRIMARY KEY (key, time, name)
)
WITH CLUSTERING ORDER BY (time DESC);
CREATE TABLE IF NOT EXISTS player (
  name text,
  lock uuid,
  game uuid,
  PRIMARY KEY (name)
);
-- =========================
-- Tag dictionary
-- =========================
CREATE TABLE IF NOT EXISTS tag
(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- =========================
-- Recipe ↔ Tag link
-- =========================
CREATE TABLE IF NOT EXISTS recipe_tag
(
    recipe_id BIGINT NOT NULL REFERENCES recipe(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (recipe_id, tag_id)  -- ein Tag pro Rezept nur einmal
);

-- Für "gib mir alle Rezepte mit Tag X" (dein Filter-Use-Case)
CREATE INDEX IF NOT EXISTS idx_recipe_tag_tag_id ON recipe_tag(tag_id);
-- =========================
-- Recipe metadata
-- =========================
CREATE TABLE recipe
(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    portions INTEGER NOT NULL,
    duration INTEGER NOT NULL,
    user_id VARCHAR NOT NULL,
    CONSTRAINT unique_user_recipe UNIQUE (name, user_id)
);

-- =========================
-- Master ingredient dictionary
-- =========================
CREATE TABLE ingredient
(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    category VARCHAR(255) NOT NULL
);

-- =========================
-- Recipe ↔ Ingredient link (with quantities)
-- =========================
CREATE TABLE recipe_ingredient
(
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL REFERENCES recipe(id) ON DELETE CASCADE,
    ingredient_id BIGINT NOT NULL REFERENCES ingredient(id) ON DELETE CASCADE,
    total_quantity DOUBLE PRECISION NOT NULL,
    unit VARCHAR(50) NOT NULL,
    UNIQUE (recipe_id, ingredient_id) -- avoid duplicates
);

-- =========================
-- Cooking instructions (steps)
-- =========================
CREATE TABLE cooking_instruction
(
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL REFERENCES recipe(id) ON DELETE CASCADE,
    step_number INTEGER NOT NULL,
    instruction TEXT NOT NULL,
    UNIQUE (recipe_id, step_number) -- enforce ordered steps
);

-- =========================
-- Ingredient usage per instruction step
-- =========================
CREATE TABLE instruction_ingredient
(
    id BIGSERIAL PRIMARY KEY,
    cooking_instruction_id BIGINT NOT NULL REFERENCES cooking_instruction(id) ON DELETE CASCADE,
    recipe_ingredient_id BIGINT NOT NULL REFERENCES recipe_ingredient(id) ON DELETE CASCADE,
    quantity DOUBLE PRECISION NOT NULL
);

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
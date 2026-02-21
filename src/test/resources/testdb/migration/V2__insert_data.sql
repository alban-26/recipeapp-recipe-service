-- ============================
-- Ingredients dictionary (base set)
-- ============================
INSERT INTO ingredient (name, category) VALUES
('Flour', 'BAKERY'),
('Eggs', 'DAIRY'),
('Sugar', 'BAKERY'),
('Butter', 'DAIRY'),
('Vanilla Extract', 'BAKERY'),
('Pasta', 'DRY_GOODS'),
('Tomatoes', 'VEGETABLES'),
('Bell Pepper', 'VEGETABLES'),
('Garlic', 'VEGETABLES'),
('Olive Oil', 'OTHER'),
('Cocoa Powder', 'BAKERY'),
('Baking Powder', 'BAKERY'),
('Chocolate Chips', 'SWEETS'),
('Chicken Breast', 'MEAT'),
('Lettuce', 'VEGETABLES'),
('Cucumber', 'VEGETABLES'),
('Lemon Juice', 'OTHER'),
('Tofu', 'VEGAN'),
('Broccoli', 'VEGETABLES'),
('Carrots', 'VEGETABLES'),
('Soy Sauce', 'OTHER'),
('Ginger', 'SPICES');

-- ============================
-- Recipes
-- ============================
INSERT INTO recipe (name, portions, duration, user_id) VALUES
('Chocolate Cake', 8, 60, 1),
('Pasta Primavera', 4, 30, 1),
('Chicken Stir-Fry', 4, 25, 1);

-- ============================
-- Recipe Ingredients
-- ============================
-- Chocolate Cake
INSERT INTO recipe_ingredient (recipe_id, ingredient_id, total_quantity, unit)
SELECT r.id, i.id, q.qty, q.unit
FROM recipe r
JOIN (
  VALUES
    ('Flour', 200, 'g'),
    ('Sugar', 150, 'g'),
    ('Butter', 100, 'g'),
    ('Eggs', 3, 'Stück'),
    ('Cocoa Powder', 50, 'g'),
    ('Baking Powder', 10, 'g'),
    ('Vanilla Extract', 5, 'ml'),
    ('Chocolate Chips', 100, 'g')
) AS q(name, qty, unit) ON TRUE
JOIN ingredient i ON i.name = q.name
WHERE r.name = 'Chocolate Cake';

-- Pasta Primavera
INSERT INTO recipe_ingredient (recipe_id, ingredient_id, total_quantity, unit)
SELECT r.id, i.id, q.qty, q.unit
FROM recipe r
JOIN (
  VALUES
    ('Pasta', 250, 'g'),
    ('Olive Oil', 30, 'ml'),
    ('Garlic', 2, 'clove'),
    ('Tomatoes', 200, 'g'),
    ('Bell Pepper', 150, 'g'),
    ('Broccoli', 150, 'g'),
    ('Carrots', 100, 'g')
) AS q(name, qty, unit) ON TRUE
JOIN ingredient i ON i.name = q.name
WHERE r.name = 'Pasta Primavera';

-- Chicken Stir-Fry
INSERT INTO recipe_ingredient (recipe_id, ingredient_id, total_quantity, unit)
SELECT r.id, i.id, q.qty, q.unit
FROM recipe r
JOIN (
  VALUES
    ('Chicken Breast', 400, 'g'),
    ('Soy Sauce', 30, 'ml'),
    ('Olive Oil', 20, 'ml'),
    ('Garlic', 3, 'clove'),
    ('Ginger', 10, 'g'),
    ('Broccoli', 150, 'g'),
    ('Carrots', 100, 'g'),
    ('Bell Pepper', 150, 'g')
) AS q(name, qty, unit) ON TRUE
JOIN ingredient i ON i.name = q.name
WHERE r.name = 'Chicken Stir-Fry';

-- ============================
-- Cooking Instructions
-- ============================
-- Chocolate Cake
INSERT INTO cooking_instruction (recipe_id, step_number, instruction)
SELECT r.id, s.step, s.text
FROM recipe r
JOIN (
  VALUES
    (1, 'Preheat oven to 180°C.'),
    (2, 'Mix flour, sugar, cocoa powder, and baking powder.'),
    (3, 'Whisk eggs, melted butter, and vanilla extract.'),
    (4, 'Combine wet and dry ingredients, fold in chocolate chips.'),
    (5, 'Pour batter into pan and bake for 35 minutes.')
) AS s(step, text) ON TRUE
WHERE r.name = 'Chocolate Cake';

-- Pasta Primavera
INSERT INTO cooking_instruction (recipe_id, step_number, instruction)
SELECT r.id, s.step, s.text
FROM recipe r
JOIN (
  VALUES
    (1, 'Cook pasta according to package instructions.'),
    (2, 'Sauté garlic in olive oil.'),
    (3, 'Add chopped vegetables and cook until tender.'),
    (4, 'Toss vegetables with pasta and serve.')
) AS s(step, text) ON TRUE
WHERE r.name = 'Pasta Primavera';

-- Chicken Stir-Fry
INSERT INTO cooking_instruction (recipe_id, step_number, instruction)
SELECT r.id, s.step, s.text
FROM recipe r
JOIN (
  VALUES
    (1, 'Slice chicken and vegetables.'),
    (2, 'Heat oil in wok, add garlic and ginger.'),
    (3, 'Stir-fry chicken until golden.'),
    (4, 'Add vegetables and cook for 5 minutes.'),
    (5, 'Add soy sauce and stir well. Serve hot.')
) AS s(step, text) ON TRUE
WHERE r.name = 'Chicken Stir-Fry';

-- ============================
-- Instruction Ingredients (quantities used per step)
-- ============================
-- Chocolate Cake
INSERT INTO instruction_ingredient (cooking_instruction_id, recipe_ingredient_id, quantity)
SELECT ci.id, ri.id, q.qty
FROM recipe r
JOIN (
  VALUES
    (2, 'Flour', 200),
    (2, 'Sugar', 150),
    (2, 'Cocoa Powder', 50),
    (2, 'Baking Powder', 10),
    (3, 'Eggs', 3),
    (3, 'Butter', 100),
    (3, 'Vanilla Extract', 5),
    (4, 'Chocolate Chips', 100)
) AS q(step, name, qty) ON TRUE
JOIN cooking_instruction ci ON ci.recipe_id = r.id AND ci.step_number = q.step
JOIN ingredient i ON i.name = q.name
JOIN recipe_ingredient ri ON ri.recipe_id = r.id AND ri.ingredient_id = i.id
WHERE r.name = 'Chocolate Cake';

-- Pasta Primavera
INSERT INTO instruction_ingredient (cooking_instruction_id, recipe_ingredient_id, quantity)
SELECT ci.id, ri.id, q.qty
FROM recipe r
JOIN (
  VALUES
    (1, 'Pasta', 250),
    (2, 'Olive Oil', 30),
    (2, 'Garlic', 2),
    (3, 'Tomatoes', 200),
    (3, 'Bell Pepper', 150),
    (3, 'Broccoli', 150),
    (3, 'Carrots', 100)
) AS q(step, name, qty) ON TRUE
JOIN cooking_instruction ci ON ci.recipe_id = r.id AND ci.step_number = q.step
JOIN ingredient i ON i.name = q.name
JOIN recipe_ingredient ri ON ri.recipe_id = r.id AND ri.ingredient_id = i.id
WHERE r.name = 'Pasta Primavera';

-- Chicken Stir-Fry
INSERT INTO instruction_ingredient (cooking_instruction_id, recipe_ingredient_id, quantity)
SELECT ci.id, ri.id, q.qty
FROM recipe r
JOIN (
  VALUES
    (2, 'Olive Oil', 20),
    (2, 'Garlic', 3),
    (2, 'Ginger', 10),
    (3, 'Chicken Breast', 400),
    (4, 'Broccoli', 150),
    (4, 'Carrots', 100),
    (4, 'Bell Pepper', 150),
    (5, 'Soy Sauce', 30)
) AS q(step, name, qty) ON TRUE
JOIN cooking_instruction ci ON ci.recipe_id = r.id AND ci.step_number = q.step
JOIN ingredient i ON i.name = q.name
JOIN recipe_ingredient ri ON ri.recipe_id = r.id AND ri.ingredient_id = i.id
WHERE r.name = 'Chicken Stir-Fry';

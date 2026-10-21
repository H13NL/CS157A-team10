-- WhatsOnTheMenu sample data (MySQL)
--
-- Run after db/schema.sql, in Workbench or from a terminal:
--   mysql -u root -p < db/seed.sql
--
-- Expects freshly created (empty) tables, since the ids below are hard-coded.

USE testproject;

INSERT INTO restaurant (restaurant_id, restaurant_name) VALUES
    (1, 'Team 10 Kitchen'),
    (2, 'Spartan Bistro');

-- Passwords are plain text until password hashing is added
INSERT INTO users (user_id, name, email, password) VALUES
    (1, 'Test Chef',   'chef@test.com',  'pass123'),
    (2, 'Second Chef', 'chef2@test.com', 'pass123'),
    (3, 'Bistro Chef', 'bistro@test.com', 'pass123');

-- Chefs 1 and 2 share Team 10 Kitchen's inventory; chef 3 works at Spartan Bistro
INSERT INTO chef (user_id, restaurant_id) VALUES
    (1, 1),
    (2, 1),
    (3, 2);

INSERT INTO ingredient (ing_id, name, unit) VALUES
    (1, 'Rice', 'kg'),
    (2, 'Eggs', 'pcs'),
    (3, 'Onion', 'pcs'),
    (4, 'Garlic', 'cloves'),
    (5, 'Soy Sauce', 'ml'),
    (6, 'Ramen Noodles', 'packs'),
    (7, 'Lettuce', 'heads'),
    (8, 'Tomato', 'pcs'),
    (9, 'Olive Oil', 'ml'),
    (10, 'Chicken Breast', 'kg');

INSERT INTO stock (restaurant_id, ing_id, qty_avai, unit) VALUES
    (1, 1, 5.00, 'kg'),
    (1, 2, 24.00, 'pcs'),
    (1, 3, 10.00, 'pcs'),
    (1, 4, 0.00, 'cloves'),
    (1, 5, 500.00, 'ml'),
    (1, 6, 12.00, 'packs'),
    (2, 7, 4.00, 'heads'),
    (2, 8, 15.00, 'pcs'),
    (2, 9, 750.00, 'ml');

-- Public recipes are browseable by every chef. A chef must save a private copy before editing.
INSERT INTO recipe (recipe_id, name, description, is_public, created_by) VALUES
    (1, 'Garden Vegetable Bowl', 'A bright bowl built around crisp vegetables and a simple olive-oil finish.', TRUE, 1),
    (2, 'Quick Ramen', 'A flexible ramen bowl for using pantry staples on a busy service night.', TRUE, 2);

INSERT INTO requires (recipe_id, ing_id, qty, unit) VALUES
    (1, 2, 2.00, 'pcs'),
    (1, 3, 1.00, 'pcs'),
    (1, 7, 1.00, 'heads'),
    (1, 9, 30.00, 'ml'),
    (2, 6, 1.00, 'packs'),
    (2, 3, 1.00, 'pcs'),
    (2, 4, 2.00, 'cloves'),
    (2, 5, 50.00, 'ml');

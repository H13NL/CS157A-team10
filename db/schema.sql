-- WhatsOnTheMenu database schema (MySQL)
--
-- Run in MySQL Workbench (File > Open SQL Script, then Execute) or from a terminal:
--   mysql -u root -p < db/schema.sql
--
-- WARNING: re-running this script drops and recreates every table, deleting their data.

-- change testproject into your database name (must match DBConnection.java / UserDAO.java)
CREATE DATABASE IF NOT EXISTS testproject
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
USE testproject;

-- Drop in reverse dependency order so foreign keys don't block it
DROP TABLE IF EXISTS stock;
DROP TABLE IF EXISTS requires;
DROP TABLE IF EXISTS contain;
DROP TABLE IF EXISTS supplies;
DROP TABLE IF EXISTS includes;
DROP TABLE IF EXISTS keeps;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS restock_request;
DROP TABLE IF EXISTS ogs_order;
DROP TABLE IF EXISTS recipe;
DROP TABLE IF EXISTS ingredient;
DROP TABLE IF EXISTS chef;
DROP TABLE IF EXISTS restaurant;
DROP TABLE IF EXISTS restocker;
DROP TABLE IF EXISTS admin;
DROP TABLE IF EXISTS consumer;
DROP TABLE IF EXISTS users;

-- ---------------------------------------------------------------
-- Entity tables
-- ---------------------------------------------------------------

-- User superclass ("users" because USER is a reserved word)
CREATE TABLE users (
    user_id     INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'active',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ISA subclasses: each shares its primary key with users
CREATE TABLE consumer (
    user_id INT PRIMARY KEY,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE admin (
    user_id INT PRIMARY KEY,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- approved_by is NULL while the restocker is pending approval (Approve relationship)
CREATE TABLE restocker (
    user_id      INT PRIMARY KEY,
    company_name VARCHAR(150),
    approved_by  INT NULL,
    approved_at  TIMESTAMP NULL,
    FOREIGN KEY (user_id)     REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES admin(user_id)
);

CREATE TABLE restaurant (
    restaurant_id   INT PRIMARY KEY AUTO_INCREMENT,
    restaurant_name VARCHAR(150) NOT NULL
);

-- restaurant_id implements the Work at relationship (NOT NULL: every chef belongs to exactly 1 restaurant)
CREATE TABLE chef (
    user_id       INT PRIMARY KEY,
    restaurant_id INT NOT NULL,
    FOREIGN KEY (user_id)       REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (restaurant_id) REFERENCES restaurant(restaurant_id)
);

CREATE TABLE ingredient (
    ing_id     INT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL UNIQUE,
    unit       VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- created_by implements the Create relationship
CREATE TABLE recipe (
    recipe_id   INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    is_public   BOOLEAN DEFAULT FALSE,
    created_by  INT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- consumer_id implements the Places relationship
CREATE TABLE ogs_order (
    order_id    INT PRIMARY KEY AUTO_INCREMENT,
    consumer_id INT NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (consumer_id) REFERENCES consumer(user_id)
);

-- chef_id = Submit, restocker_id = Receives, recipe_id = BasedOn
CREATE TABLE restock_request (
    request_id   INT PRIMARY KEY AUTO_INCREMENT,
    chef_id      INT NOT NULL,
    restocker_id INT NOT NULL,
    recipe_id    INT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP NULL,
    FOREIGN KEY (chef_id)      REFERENCES chef(user_id),
    FOREIGN KEY (restocker_id) REFERENCES restocker(user_id),
    FOREIGN KEY (recipe_id)    REFERENCES recipe(recipe_id)
);

-- user_id implements the Perform relationship.
-- entity_id points at a different table depending on entity_type, so it is not a foreign key.
CREATE TABLE audit_log (
    log_id      INT PRIMARY KEY AUTO_INCREMENT,
    user_id     INT NOT NULL,
    action      VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id   INT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- ---------------------------------------------------------------
-- Many-to-many relationship tables
-- ---------------------------------------------------------------

-- Keeps: a consumer's pantry
CREATE TABLE keeps (
    consumer_id INT,
    ing_id      INT,
    qty_avai    DECIMAL(10,2) NOT NULL DEFAULT 0,
    unit        VARCHAR(20),
    last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (consumer_id, ing_id),
    FOREIGN KEY (consumer_id) REFERENCES consumer(user_id) ON DELETE CASCADE,
    FOREIGN KEY (ing_id)      REFERENCES ingredient(ing_id)
);

-- Includes: ingredients in a grocery order
CREATE TABLE includes (
    order_id    INT,
    ing_id      INT,
    qty_ordered DECIMAL(10,2) NOT NULL CHECK (qty_ordered > 0),
    PRIMARY KEY (order_id, ing_id),
    FOREIGN KEY (order_id) REFERENCES ogs_order(order_id) ON DELETE CASCADE,
    FOREIGN KEY (ing_id)   REFERENCES ingredient(ing_id)
);

-- Supplies: a restocker's catalog and pricing
CREATE TABLE supplies (
    restocker_id INT,
    ing_id       INT,
    price        DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    qty          DECIMAL(10,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (restocker_id, ing_id),
    FOREIGN KEY (restocker_id) REFERENCES restocker(user_id) ON DELETE CASCADE,
    FOREIGN KEY (ing_id)       REFERENCES ingredient(ing_id)
);

-- Contain: ingredients in a restock request
CREATE TABLE contain (
    request_id INT,
    ing_id     INT,
    qty_req    DECIMAL(10,2) NOT NULL CHECK (qty_req > 0),
    PRIMARY KEY (request_id, ing_id),
    FOREIGN KEY (request_id) REFERENCES restock_request(request_id) ON DELETE CASCADE,
    FOREIGN KEY (ing_id)     REFERENCES ingredient(ing_id)
);

-- Require: ingredients a recipe needs ("requires" because REQUIRE is a reserved word)
CREATE TABLE requires (
    recipe_id INT,
    ing_id    INT,
    qty       DECIMAL(10,2) NOT NULL,
    unit      VARCHAR(20),
    PRIMARY KEY (recipe_id, ing_id),
    FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id) ON DELETE CASCADE,
    FOREIGN KEY (ing_id)    REFERENCES ingredient(ing_id)
);

-- Stock: a restaurant's inventory
CREATE TABLE stock (
    restaurant_id INT,
    ing_id        INT,
    qty_avai      DECIMAL(10,2) NOT NULL DEFAULT 0,
    unit          VARCHAR(20),
    last_update   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (restaurant_id, ing_id),
    FOREIGN KEY (restaurant_id) REFERENCES restaurant(restaurant_id) ON DELETE CASCADE,
    FOREIGN KEY (ing_id)        REFERENCES ingredient(ing_id)
);

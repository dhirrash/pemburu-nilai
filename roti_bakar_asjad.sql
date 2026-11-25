-- =====================================================================
-- Roti Bakar Asjad - Skrip Database
-- Target  : MariaDB 10.2.1+ / MySQL 8.0.16+ (engine InnoDB)
-- Charset : utf8mb4 / utf8mb4_unicode_ci
-- Catatan : ini pake DROP TABLE jadi kalo ada table sebelumnya bakal di apus dan diganti ini
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `roti_bakar_asjad`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `roti_bakar_asjad`;

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- DROP (urutan: tabel anak dulu, baru tabel induk)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `stock_movements`;
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `users`;

-- ---------------------------------------------------------------------
-- 1. users
-- ---------------------------------------------------------------------
CREATE TABLE `users` (
  `id`            INT NOT NULL AUTO_INCREMENT,
  `username`      VARCHAR(50)  NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `full_name`     VARCHAR(100) NOT NULL,
  `role`          ENUM('ADMIN','KASIR') NOT NULL DEFAULT 'KASIR',
  `created_at`    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_users_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 2. categories
-- ---------------------------------------------------------------------
CREATE TABLE `categories` (
  `id`   INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_categories_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 3. products
-- ---------------------------------------------------------------------
CREATE TABLE `products` (
  `id`          INT NOT NULL AUTO_INCREMENT,
  `category_id` INT NOT NULL,
  `name`        VARCHAR(150) NOT NULL,
  `description` VARCHAR(500) NULL,
  `price`       DECIMAL(12,2) NOT NULL,
  `stock`       INT NOT NULL DEFAULT 0,
  `is_active`   TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `idx_products_category_active` (`category_id`, `is_active`),
  CONSTRAINT `fk_products_category`
    FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_products_price` CHECK (`price` >= 0),
  CONSTRAINT `chk_products_stock` CHECK (`stock` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 4. orders
-- ---------------------------------------------------------------------
CREATE TABLE `orders` (
  `id`             INT NOT NULL AUTO_INCREMENT,
  `order_code`     VARCHAR(30) NOT NULL,
  `user_id`        INT NOT NULL,
  `total_amount`   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `paid_amount`    DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `change_amount`  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `payment_method` ENUM('TUNAI','QRIS','DEBIT') NULL,
  `status`         ENUM('OPEN','PAID','CANCELLED') NOT NULL DEFAULT 'OPEN',
  `created_at`     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `paid_at`        TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_orders_order_code` (`order_code`),
  KEY `idx_orders_user_status` (`user_id`, `status`),
  KEY `idx_orders_status_paid_at` (`status`, `paid_at`),
  CONSTRAINT `fk_orders_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_orders_paid` CHECK (
    `status` <> 'PAID'
    OR (`payment_method` IS NOT NULL
        AND `paid_at` IS NOT NULL
        AND `paid_amount` >= `total_amount`)
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 5. order_items
-- ---------------------------------------------------------------------
CREATE TABLE `order_items` (
  `id`           INT NOT NULL AUTO_INCREMENT,
  `order_id`     INT NOT NULL,
  `product_id`   INT NOT NULL,
  `product_name` VARCHAR(150) NOT NULL,
  `unit_price`   DECIMAL(12,2) NOT NULL,
  `quantity`     INT NOT NULL,
  `subtotal`     DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_order_product` (`order_id`, `product_id`),
  KEY `idx_order_items_product` (`product_id`),
  CONSTRAINT `fk_order_items_order`
    FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
    ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_order_items_product`
    FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_order_items_qty`      CHECK (`quantity` > 0),
  CONSTRAINT `chk_order_items_subtotal` CHECK (`subtotal` = `unit_price` * `quantity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 6. stock_movements
-- ---------------------------------------------------------------------
CREATE TABLE `stock_movements` (
  `id`         INT NOT NULL AUTO_INCREMENT,
  `product_id` INT NOT NULL,
  `order_id`   INT NULL,
  `change_qty` INT NOT NULL,
  `reason`     ENUM('RESTOCK','SALE','ADJUSTMENT') NOT NULL,
  `note`       VARCHAR(255) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_stock_movements_product_created` (`product_id`, `created_at`),
  KEY `idx_stock_movements_order` (`order_id`),
  CONSTRAINT `fk_stock_movements_product`
    FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_stock_movements_order`
    FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_stock_movements_qty`  CHECK (`change_qty` <> 0),
  CONSTRAINT `chk_stock_movements_sale` CHECK (`reason` <> 'SALE' OR `order_id` IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =====================================================================
-- SEED DATA
-- =====================================================================

-- ---------------------------------------------------------------------
-- Users (hash BCrypt $2a$10, kompatibel dengan jBCrypt)
--   admin / admin123
--   kasir1 / kasir123
-- ---------------------------------------------------------------------
INSERT INTO `users` (`id`, `username`, `password_hash`, `full_name`, `role`) VALUES
(1, 'admin',  '$2a$10$Q7xk2LmN9pRsT4vWy6Zb.eZw2D9WsTNjNGoGfUBrM8HCeKQYUGur2', 'Administrator', 'ADMIN'),
(2, 'kasir1', '$2a$10$Hd3Fg5JkL8nPq1StUw9XyeCSbIPG9xej5y9rsbIOQUa/43VQ2n6SK', 'Kasir Satu',     'KASIR');

-- ---------------------------------------------------------------------
-- Categories (sesuai  di  menu)
-- ---------------------------------------------------------------------
INSERT INTO `categories` (`id`, `name`) VALUES
(1, 'Roti Bakar Manis'),
(2, 'Roti Bakar Asin'),
(3, 'Menu Mix Best Seller'),
(4, 'Topping Premium');

-- ---------------------------------------------------------------------
-- Products (nama dan harga sesuai daftar menu Asjad)
-- Stok TIDAK ada di daftar menu, jadi stok awal hanya contoh:
-- roti bakar 50, topping 100. Ubah sesuai kebutuhan.
-- Kolom stock di bawah adalah stok SETELAH contoh transaksi.
-- ---------------------------------------------------------------------
INSERT INTO `products` (`id`, `category_id`, `name`, `description`, `price`, `stock`, `is_active`) VALUES
(1, 1, 'Choco Crunchy', NULL, 15000.00, 48, 1),
(2, 1, 'Strawberry Pocky', NULL, 15000.00, 50, 1),
(3, 1, 'Green Tea', NULL, 15000.00, 50, 1),
(4, 1, 'Cream Cheese', NULL, 15000.00, 50, 1),
(5, 1, 'Dark Chocolate', NULL, 15000.00, 50, 1),
(6, 1, 'Taro', NULL, 15000.00, 50, 1),
(7, 1, 'Tiramisu', NULL, 15000.00, 50, 1),
(8, 1, 'Vanila', NULL, 15000.00, 50, 1),
(9, 1, 'Capucino', NULL, 15000.00, 50, 1),
(10, 1, 'Manggo', NULL, 15000.00, 50, 1),
(11, 1, 'Blue Berry Jam', NULL, 15000.00, 50, 1),
(12, 1, 'Hazelnut Crunchy', NULL, 20000.00, 50, 1),
(13, 1, 'Mozarella Keju Susu', NULL, 20000.00, 50, 1),
(14, 1, 'Full Cheese', NULL, 20000.00, 50, 1),
(15, 1, 'Nutella', NULL, 23000.00, 49, 1),
(16, 1, 'Lotus Biscoff', NULL, 25000.00, 49, 1),
(17, 1, 'Skippy', NULL, 25000.00, 50, 1),
(18, 2, 'Egg Mayo', NULL, 15000.00, 50, 1),
(19, 2, 'Egg Mayo Smoked Beef', NULL, 18000.00, 49, 1),
(20, 2, 'Egg Mayo Sosis', NULL, 18000.00, 50, 1),
(21, 2, 'Egg Mayo Smoked Keju', NULL, 20000.00, 50, 1),
(22, 2, 'Egg Mayo Crabstik Keju', NULL, 20000.00, 50, 1),
(23, 2, 'Egg Mayo Beef Patties', NULL, 20000.00, 50, 1),
(24, 3, 'Chocco Cheese', NULL, 18000.00, 50, 1),
(25, 3, 'Cream Cheese Red Velvet', NULL, 18000.00, 50, 1),
(26, 3, 'Vanila Milo', NULL, 18000.00, 50, 1),
(27, 3, 'Vanila Oreo', NULL, 18000.00, 49, 1),
(28, 3, 'Choco Vanila Regal', NULL, 18000.00, 50, 1),
(29, 3, 'Smoked Beef Cheese', NULL, 20000.00, 48, 1),
(30, 3, 'Mozarella Sugar Cheese', NULL, 20000.00, 50, 1),
(31, 4, 'Keju', NULL, 3000.00, 99, 1),
(32, 4, 'Regal Biscuit', NULL, 3000.00, 100, 1),
(33, 4, 'Milo', NULL, 3000.00, 100, 1),
(34, 4, 'Red Velvet', NULL, 3000.00, 100, 1),
(35, 4, 'Double Pastry', NULL, 3000.00, 100, 1),
(36, 4, 'Kacang', NULL, 3000.00, 100, 1),
(37, 4, 'Beng Beng', NULL, 3000.00, 100, 1),
(38, 4, 'Smoked Beef/Patties', NULL, 6000.00, 100, 1);

-- ---------------------------------------------------------------------
-- Orders (3 PAID + 1 CANCELLED) untuk menguji Riwayat dan filter tanggal
-- ---------------------------------------------------------------------
INSERT INTO `orders`
  (`id`, `order_code`, `user_id`, `total_amount`, `paid_amount`, `change_amount`, `payment_method`, `status`, `created_at`, `paid_at`)
VALUES
(1, 'RBA-20261110-0001', 2, 56000.00, 60000.00, 4000.00, 'TUNAI', 'PAID', '2026-11-10 10:15:00', '2026-11-10 10:17:00'),
(2, 'RBA-20261110-0002', 2, 36000.00, 36000.00, 0.00, 'QRIS', 'PAID', '2026-11-10 14:30:00', '2026-11-10 14:32:00'),
(3, 'RBA-20261111-0001', 2, 65000.00, 65000.00, 0.00, 'DEBIT', 'PAID', '2026-11-11 09:05:00', '2026-11-11 09:08:00'),
(4, 'RBA-20261111-0002', 2, 15000.00, 0.00, 0.00, NULL, 'CANCELLED', '2026-11-11 09:40:00', NULL);

-- ---------------------------------------------------------------------
-- Order items
-- ---------------------------------------------------------------------
INSERT INTO `order_items` (`order_id`, `product_id`, `product_name`, `unit_price`, `quantity`, `subtotal`) VALUES
(1, 1, 'Choco Crunchy', 15000.00, 2, 30000.00),
(1, 15, 'Nutella', 23000.00, 1, 23000.00),
(1, 31, 'Keju', 3000.00, 1, 3000.00),
(2, 19, 'Egg Mayo Smoked Beef', 18000.00, 1, 18000.00),
(2, 27, 'Vanila Oreo', 18000.00, 1, 18000.00),
(3, 16, 'Lotus Biscoff', 25000.00, 1, 25000.00),
(3, 29, 'Smoked Beef Cheese', 20000.00, 2, 40000.00),
(4, 3, 'Green Tea', 15000.00, 1, 15000.00);

-- ---------------------------------------------------------------------
-- Stock movements: stok awal (RESTOCK) lalu penjualan (SALE)
-- Pesanan CANCELLED tidak mengurangi stok, jadi tidak ada baris SALE.
-- ---------------------------------------------------------------------
INSERT INTO `stock_movements` (`product_id`, `order_id`, `change_qty`, `reason`, `note`, `created_at`) VALUES
(1, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(2, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(3, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(4, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(5, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(6, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(7, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(8, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(9, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(10, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(11, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(12, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(13, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(14, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(15, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(16, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(17, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(18, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(19, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(20, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(21, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(22, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(23, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(24, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(25, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(26, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(27, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(28, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(29, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(30, NULL, 50, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(31, NULL, 100, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(32, NULL, 100, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(33, NULL, 100, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(34, NULL, 100, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(35, NULL, 100, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(36, NULL, 100, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(37, NULL, 100, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),
(38, NULL, 100, 'RESTOCK', 'Stok awal', '2026-11-09 08:00:00'),

(1, 1, -2, 'SALE', 'RBA-20261110-0001', '2026-11-10 10:17:00'),
(15, 1, -1, 'SALE', 'RBA-20261110-0001', '2026-11-10 10:17:00'),
(31, 1, -1, 'SALE', 'RBA-20261110-0001', '2026-11-10 10:17:00'),
(19, 2, -1, 'SALE', 'RBA-20261110-0002', '2026-11-10 14:32:00'),
(27, 2, -1, 'SALE', 'RBA-20261110-0002', '2026-11-10 14:32:00'),
(16, 3, -1, 'SALE', 'RBA-20261111-0001', '2026-11-11 09:08:00'),
(29, 3, -2, 'SALE', 'RBA-20261111-0001', '2026-11-11 09:08:00');



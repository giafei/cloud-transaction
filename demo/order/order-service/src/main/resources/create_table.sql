CREATE SCHEMA `pay_order` DEFAULT CHARACTER SET utf8mb4 ;

CREATE TABLE `pay_order`.`order_head` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `amount` DOUBLE NULL,
  `create_time` DATETIME NULL,
  PRIMARY KEY (`id`));

CREATE TABLE `pay_order`.`order_data` (
 `id` INT NOT NULL AUTO_INCREMENT,
 `order_head_id` INT NULL,
 `solid_item_id` INT NULL,
 `create_time` DATETIME NULL,
 PRIMARY KEY (`id`));

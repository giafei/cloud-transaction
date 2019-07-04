CREATE SCHEMA `storage_data` DEFAULT CHARACTER SET utf8mb4 ;


CREATE TABLE `storage_data`.`sold_item` (
 `id` INT NOT NULL AUTO_INCREMENT,
 `item_id` INT NULL,
 `number` INT NULL,
 `create_time` DATETIME NULL,
 PRIMARY KEY (`id`));

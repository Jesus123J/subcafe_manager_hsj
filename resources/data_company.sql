-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: financialtracker1
-- ------------------------------------------------------
-- Server version	5.5.5-10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `service_concept`
--

DROP TABLE IF EXISTS `service_concept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_concept` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `description` text DEFAULT NULL,
  `sale_price` decimal(10,2) DEFAULT NULL,
  `cost_price` decimal(10,2) DEFAULT NULL,
  `priority` int(11) DEFAULT NULL,
  `unid` int(11) DEFAULT NULL,
  `priority_concept` varchar(45) DEFAULT NULL,
  `createdBy` int(11) DEFAULT NULL,
  `createdAt` varchar(45) DEFAULT NULL,
  `modifiedBy` int(11) DEFAULT NULL,
  `modifiedAt` varchar(45) DEFAULT NULL,
  `codigo` varchar(8) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `codigo` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_concept`
--

LOCK TABLES `service_concept` WRITE;
/*!40000 ALTER TABLE `service_concept` DISABLE KEYS */;
INSERT INTO `service_concept` VALUES (1,'CANASTA',0.00,0.00,0,0,'Primero',1,'2025-04-16',0,NULL,'84409463');
/*!40000 ALTER TABLE `service_concept` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ZERO_IN_DATE,NO_ZERO_DATE,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER before_insert_service_concept
BEFORE INSERT ON service_concept
FOR EACH ROW
BEGIN
    DECLARE random_code VARCHAR(8);
    
    -- Generar un código único
    REPEAT
        SET random_code = LPAD(FLOOR(RAND() * 100000000), 8, '0');
    UNTIL NOT EXISTS (SELECT 1 FROM service_concept WHERE codigo = random_code)
    END REPEAT;

    -- Asignar el código generado al nuevo registro
    SET NEW.codigo = random_code;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Dumping routines for database 'financialtracker1'
--
/*!50003 DROP PROCEDURE IF EXISTS `InsertarRegistro` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ZERO_IN_DATE,NO_ZERO_DATE,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `InsertarRegistro`(
    IN p_empleado_id INT,
    IN p_amount DOUBLE(10,2)
)
BEGIN
    DECLARE v_codigo_existente VARCHAR(20);
    DECLARE v_nuevo_codigo VARCHAR(20);
    DECLARE v_prefijo INT;
    DECLARE v_numero INT;

    -- Obtener el prefijo máximo o iniciar desde 1 si no existe
    SELECT 
        IFNULL(MAX(CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(codigo, '/', -1), '-', 1) AS UNSIGNED)), 1) 
    INTO v_prefijo
    FROM registro;

    -- Obtener el número dentro del prefijo
    SELECT 
        IFNULL(MAX(CAST(SUBSTRING_INDEX(codigo, '-' , -1) AS UNSIGNED)), 0) 
    INTO v_numero
    FROM registro
    WHERE codigo LIKE CONCAT('P/', LPAD(v_prefijo, 4, '0'), '-%');

    -- Incrementar el prefijo o el número según el límite
    IF v_numero >= 99999999 THEN
        SET v_prefijo = v_prefijo + 1;
        SET v_numero = 1;
    ELSE
        SET v_numero = v_numero + 1;
    END IF;

    -- Generar el nuevo código
    SET v_nuevo_codigo = CONCAT('P/', LPAD(v_prefijo, 4, '0'), '-', LPAD(v_numero, 8, '0'));

    -- Insertar el nuevo registro
    INSERT INTO registro (codigo, empleado_id, amount)
    VALUES (v_nuevo_codigo, p_empleado_id, p_amount);

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-04-16 11:48:51

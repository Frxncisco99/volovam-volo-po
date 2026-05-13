-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 13, 2026 at 08:55 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `pospanaderia`
--

-- --------------------------------------------------------

--
-- Table structure for table `auditoria`
--

CREATE TABLE `auditoria` (
  `id_log` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `accion` varchar(50) NOT NULL,
  `tabla_afectada` varchar(30) DEFAULT NULL,
  `id_registro` int(11) DEFAULT NULL,
  `detalle` text DEFAULT NULL,
  `fecha` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Dumping data for table `auditoria`
--

INSERT INTO `auditoria` (`id_log`, `id_usuario`, `accion`, `tabla_afectada`, `id_registro`, `detalle`, `fecha`) VALUES
(1, 1, 'VENTA', 'ventas', 27, 'Venta VTA-000027 — Total: $59.00 — Método: TRANSFERENCIA — Cliente ID: 1', '2026-05-10 18:50:43'),
(2, 1, 'CANCELACION', 'ventas', 27, 'Venta VTA-000027 cancelada. Motivo: queja de cleinte', '2026-05-10 18:52:07'),
(3, 1, 'VENTA', 'ventas', 28, 'Venta VTA-000028 — Total: $1000.00 — Método: DOLARES — Cliente ID: 1', '2026-05-10 18:53:05'),
(4, 1, 'CANCELACION', 'ventas', 28, 'Venta VTA-000028 cancelada. Motivo: no', '2026-05-10 18:53:14'),
(5, 1, 'VENTA', 'ventas', 29, 'Venta VTA-000029 — Total: $45.00 — Método: EFECTIVO — Cliente ID: 1', '2026-05-10 19:29:17'),
(6, 1, 'VENTA', 'ventas', 30, 'Venta VTA-000030 — Total: $1540.00 — Método: TARJETA — Cliente ID: 1', '2026-05-10 19:30:29'),
(7, 1, 'VENTA', 'ventas', 31, 'Venta VTA-000031 — Total: $1540.00 — Método: EFECTIVO — Cliente ID: 1', '2026-05-10 19:30:37'),
(8, 1, 'VENTA', 'ventas', 32, 'Venta VTA-000032 — Total: $2100.00 — Método: TARJETA — Cliente ID: 1', '2026-05-10 19:31:38'),
(9, 1, 'VENTA', 'ventas', 33, 'Venta VTA-000033 — Total: $2100.00 — Método: TARJETA — Cliente ID: 1', '2026-05-10 19:31:56'),
(10, 1, 'VENTA', 'ventas', 34, 'Venta VTA-000034 — Total: $2100.00 — Método: EFECTIVO — Cliente ID: 1', '2026-05-10 19:32:53'),
(11, 1, 'VENTA', 'ventas', 35, 'Venta VTA-000035 — Total: $15.00 — Método: EFECTIVO — Cliente ID: 1', '2026-05-10 19:33:10'),
(12, 1, 'VENTA', 'ventas', 36, 'Venta VTA-000036 — Total: $15.00 — Método: EFECTIVO — Cliente ID: 1', '2026-05-10 19:33:17'),
(13, 1, 'VENTA', 'ventas', 37, 'Venta VTA-000037 — Total: $200.00 — Método: TARJETA — Cliente ID: 1', '2026-05-10 19:40:00'),
(14, 1, 'AJUSTE_STOCK', 'productos', 2, 'Producto: Concha — Entrada 4 unidades — Stock resultante: 4', '2026-05-10 19:45:17'),
(15, 1, 'AJUSTE_STOCK', 'productos', 1, 'Producto: Volovan de Camaron — Entrada 8 unidades — Stock resultante: 8', '2026-05-10 19:45:21'),
(16, 1, 'AJUSTE_STOCK', 'productos', 2, 'Producto: Concha — Entrada 2 unidades — Stock resultante: 6', '2026-05-10 19:45:23'),
(17, 1, 'VENTA', 'ventas', 38, 'Venta VTA-000038 — Total: $60.00 — Método: EFECTIVO — Cliente ID: 1', '2026-05-10 19:46:06'),
(18, 1, 'DEVOLUCION', 'devoluciones', 12, 'Devolución DEV-000012 de venta VTA-000038 — Monto: $30.00 — Tipo: EFECTIVO', '2026-05-10 19:48:09'),
(19, 1, 'LOGIN', 'usuarios', 1, 'Inicio de sesión: Administrador desde 192.168.56.1', '2026-05-11 01:25:54'),
(20, 1, 'VENTA', 'ventas', 39, 'Venta VTA-000039 — Total: $291.00 — Método: EFECTIVO — Cliente ID: 1', '2026-05-11 01:28:00'),
(21, 1, 'LOGIN', 'usuarios', 1, 'Inicio de sesión: Administrador desde 192.168.56.1', '2026-05-11 01:28:59'),
(22, 1, 'LOGIN', 'usuarios', 1, 'Inicio de sesión: Administrador desde 192.168.56.1', '2026-05-11 01:29:38'),
(23, 3, 'LOGIN', 'usuarios', 3, 'Inicio de sesión: Francisco Hernandez desde 192.168.56.1', '2026-05-11 01:29:56'),
(24, 3, 'VENTA', 'ventas', 40, 'Venta VTA-000040 — Total: $61.00 — Método: EFECTIVO — Cliente ID: 1', '2026-05-11 01:30:11'),
(25, 3, 'VENTA', 'ventas', 41, 'Venta VTA-000041 — Total: $313.00 — Método: EFECTIVO — Cliente ID: 1', '2026-05-11 01:30:20'),
(26, 1, 'LOGIN', 'usuarios', 1, 'Inicio de sesión: Administrador desde 192.168.56.1', '2026-05-11 01:32:04'),
(27, 1, 'LOGIN', 'usuarios', 1, 'Inicio de sesión: Administrador desde 192.168.56.1', '2026-05-11 07:13:40'),
(28, 1, 'LOGOUT', 'usuarios', 1, 'Cierre de sesión: Administrador', '2026-05-11 07:18:20'),
(29, 1, 'LOGIN', 'usuarios', 1, 'Inicio de sesión: Administrador desde 192.168.56.1', '2026-05-11 10:05:29'),
(30, 1, 'VENTA', 'ventas', 42, 'Venta VTA-000042 — Total: $90.00 — Método: TARJETA — Cliente ID: 1', '2026-05-11 10:06:47'),
(31, 1, 'LOGOUT', 'usuarios', 1, 'Cierre de sesión: Administrador', '2026-05-11 10:07:11'),
(32, 3, 'LOGIN', 'usuarios', 3, 'Inicio de sesión: Francisco Hernandez desde 192.168.56.1', '2026-05-11 10:07:23'),
(33, 3, 'LOGOUT', 'usuarios', 3, 'Cierre de sesión: Francisco Hernandez', '2026-05-11 10:07:36'),
(34, 1, 'LOGIN', 'usuarios', 1, 'Inicio de sesión: Administrador desde 192.168.56.1', '2026-05-11 10:07:48'),
(35, 1, 'LOGIN', 'usuarios', 1, 'Inicio de sesión: Administrador desde 192.168.56.1', '2026-05-11 10:10:27'),
(36, 1, 'LOGIN', 'usuarios', 1, 'Inicio de sesión: Administrador desde 192.168.56.1', '2026-05-11 10:11:14'),
(37, 1, 'LOGIN', 'usuarios', 1, 'Inicio de sesión: Administrador desde 192.168.56.1', '2026-05-11 10:11:37'),
(38, 1, 'VENTA', 'ventas', 43, 'Venta VTA-000043 — Total: $90.00 — Método: DOLARES — Cliente ID: 1', '2026-05-11 10:18:34'),
(39, 1, 'VENTA', 'ventas', 44, 'Venta VTA-000044 — Total: $108.00 — Método: FIADO — Cliente ID: 8', '2026-05-11 10:25:14'),
(40, 1, 'VENTA', 'ventas', 45, 'Venta VTA-000045 — Total: $1995.00 — Método: TARJETA — Cliente ID: 1', '2026-05-11 10:26:35'),
(41, 1, 'CANCELACION', 'ventas', 45, 'Venta VTA-000045 cancelada. Motivo: me equivque:/', '2026-05-11 10:28:37'),
(42, 1, 'LOGOUT', 'usuarios', 1, 'Cierre de sesión: Administrador', '2026-05-11 10:38:44');

-- --------------------------------------------------------

--
-- Table structure for table `caja`
--

CREATE TABLE `caja` (
  `id_caja` int(11) NOT NULL,
  `fecha_apertura` datetime NOT NULL DEFAULT current_timestamp(),
  `fecha_cierre` datetime DEFAULT NULL,
  `monto_inicial` decimal(10,2) NOT NULL DEFAULT 0.00,
  `monto_final` decimal(10,2) DEFAULT NULL,
  `estado` enum('abierta','cerrada') NOT NULL DEFAULT 'abierta',
  `id_usuario` int(11) NOT NULL,
  `tipo_cambio_dolar` decimal(10,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Dumping data for table `caja`
--

INSERT INTO `caja` (`id_caja`, `fecha_apertura`, `fecha_cierre`, `monto_inicial`, `monto_final`, `estado`, `id_usuario`, `tipo_cambio_dolar`) VALUES
(1, '2026-04-21 10:17:18', '2026-05-01 03:02:51', 2000.00, 5000.00, 'cerrada', 1, 17.00),
(2, '2026-05-02 16:16:46', NULL, 1000.00, NULL, 'abierta', 1, 17.00);

-- --------------------------------------------------------

--
-- Table structure for table `cancelaciones`
--

CREATE TABLE `cancelaciones` (
  `id_cancelacion` int(11) NOT NULL,
  `id_venta` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `fecha` datetime DEFAULT current_timestamp(),
  `motivo` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Dumping data for table `cancelaciones`
--

INSERT INTO `cancelaciones` (`id_cancelacion`, `id_venta`, `id_usuario`, `fecha`, `motivo`) VALUES
(1, 27, 1, '2026-05-10 18:52:07', 'queja de cleinte'),
(2, 28, 1, '2026-05-10 18:53:14', 'no'),
(3, 45, 1, '2026-05-11 10:28:37', 'me equivque:/');

-- --------------------------------------------------------

--
-- Table structure for table `categorias`
--

CREATE TABLE `categorias` (
  `id_categoria` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Dumping data for table `categorias`
--

INSERT INTO `categorias` (`id_categoria`, `nombre`) VALUES
(1, 'Pan dulce'),
(2, 'Pan salado'),
(3, 'Pasteles'),
(4, 'Bebidas'),
(5, 'Sin categoría'),
(6, 'Volovan'),
(11, 'Galletas'),
(12, 'Fresas con crema');

-- --------------------------------------------------------

--
-- Table structure for table `clientes`
--

CREATE TABLE `clientes` (
  `id_cliente` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `telefono` varchar(15) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `limite_credito` decimal(10,2) NOT NULL DEFAULT 0.00,
  `saldo_actual` decimal(10,2) NOT NULL DEFAULT 0.00,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `fecha_registro` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Dumping data for table `clientes`
--

INSERT INTO `clientes` (`id_cliente`, `nombre`, `telefono`, `direccion`, `limite_credito`, `saldo_actual`, `activo`, `fecha_registro`) VALUES
(1, 'Publico General', NULL, NULL, 0.00, 0.00, 1, '2026-04-21 08:41:54'),
(2, 'Juan Martinez', '8781234567', 'Col. Centro', 1000.00, 0.00, 1, '2026-04-21 22:08:55'),
(3, 'Luis Garcia', '8787654321', 'Col. Roma', 1500.00, 500.00, 1, '2026-04-21 22:08:55'),
(4, 'Pedro Sanchez', '8781112233', 'Col. Villas', 800.00, 0.00, 1, '2026-04-21 22:08:55'),
(6, 'Alexa', '56789122', NULL, 900.00, 0.00, 1, '2026-05-02 16:18:24'),
(7, 'Francisco Hdz', '8771009090', NULL, 1000.00, 0.00, 1, '2026-05-10 16:03:14'),
(8, 'Uzziel Garcia Ramos', '8771360626', NULL, 2000.00, 108.00, 1, '2026-05-11 10:24:16');

-- --------------------------------------------------------

--
-- Table structure for table `corte_caja`
--

CREATE TABLE `corte_caja` (
  `id_corte` int(11) NOT NULL,
  `id_caja` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `fecha_apertura` datetime NOT NULL,
  `fecha_cierre` datetime NOT NULL DEFAULT current_timestamp(),
  `fondo_inicial` decimal(10,2) NOT NULL DEFAULT 0.00,
  `total_ventas` decimal(10,2) NOT NULL DEFAULT 0.00,
  `num_tickets` int(11) NOT NULL DEFAULT 0,
  `total_entradas` decimal(10,2) NOT NULL DEFAULT 0.00,
  `total_salidas` decimal(10,2) NOT NULL DEFAULT 0.00,
  `dinero_esperado` decimal(10,2) NOT NULL DEFAULT 0.00,
  `dinero_real` decimal(10,2) NOT NULL DEFAULT 0.00,
  `diferencia` decimal(10,2) NOT NULL DEFAULT 0.00,
  `observaciones` varchar(500) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Dumping data for table `corte_caja`
--

INSERT INTO `corte_caja` (`id_corte`, `id_caja`, `id_usuario`, `fecha_apertura`, `fecha_cierre`, `fondo_inicial`, `total_ventas`, `num_tickets`, `total_entradas`, `total_salidas`, `dinero_esperado`, `dinero_real`, `diferencia`, `observaciones`) VALUES
(1, 1, 1, '2026-04-21 10:17:18', '2026-05-01 03:02:51', 2000.00, 2555.00, 0, 0.00, 0.00, 4555.00, 5000.00, 445.00, 'sabe');

-- --------------------------------------------------------

--
-- Table structure for table `detalle_devolucion`
--

CREATE TABLE `detalle_devolucion` (
  `id_detalle_dev` int(11) NOT NULL,
  `id_devolucion` int(11) NOT NULL,
  `id_detalle_venta` int(11) NOT NULL,
  `id_producto` int(11) NOT NULL,
  `cantidad` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Dumping data for table `detalle_devolucion`
--

INSERT INTO `detalle_devolucion` (`id_detalle_dev`, `id_devolucion`, `id_detalle_venta`, `id_producto`, `cantidad`) VALUES
(1, 10, 87, 1, 1),
(2, 11, 88, 1, 8),
(3, 12, 104, 7, 1);

-- --------------------------------------------------------

--
-- Table structure for table `detalle_venta`
--

CREATE TABLE `detalle_venta` (
  `id_detalle` int(11) NOT NULL,
  `id_venta` int(11) NOT NULL,
  `id_producto` int(11) NOT NULL,
  `cantidad` int(11) NOT NULL,
  `precio_unitario` decimal(10,2) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Dumping data for table `detalle_venta`
--

INSERT INTO `detalle_venta` (`id_detalle`, `id_venta`, `id_producto`, `cantidad`, `precio_unitario`, `subtotal`) VALUES
(1, 1, 1, 1, 25.00, 25.00),
(2, 2, 1, 9, 25.00, 225.00),
(3, 3, 1, 1, 25.00, 25.00),
(4, 3, 2, 1, 12.00, 12.00),
(5, 3, 4, 2, 3.00, 6.00),
(6, 3, 5, 1, 4.00, 4.00),
(7, 3, 6, 1, 20.00, 20.00),
(8, 3, 7, 2, 30.00, 60.00),
(9, 3, 9, 1, 220.00, 220.00),
(10, 4, 1, 1, 25.00, 25.00),
(11, 4, 2, 1, 12.00, 12.00),
(12, 4, 3, 1, 15.00, 15.00),
(13, 4, 6, 1, 20.00, 20.00),
(14, 4, 7, 1, 30.00, 30.00),
(15, 4, 9, 1, 220.00, 220.00),
(16, 5, 1, 1, 25.00, 25.00),
(17, 5, 2, 1, 12.00, 12.00),
(18, 5, 3, 2, 15.00, 30.00),
(19, 5, 5, 1, 4.00, 4.00),
(20, 5, 6, 1, 20.00, 20.00),
(21, 5, 7, 1, 30.00, 30.00),
(22, 5, 9, 1, 220.00, 220.00),
(23, 6, 1, 1, 25.00, 25.00),
(24, 6, 2, 1, 12.00, 12.00),
(25, 7, 1, 1, 25.00, 25.00),
(26, 7, 2, 1, 12.00, 12.00),
(27, 7, 3, 1, 15.00, 15.00),
(28, 7, 4, 1, 3.00, 3.00),
(29, 7, 6, 2, 20.00, 40.00),
(30, 7, 7, 1, 30.00, 30.00),
(31, 8, 1, 1, 25.00, 25.00),
(32, 8, 2, 2, 12.00, 24.00),
(33, 8, 3, 2, 15.00, 30.00),
(34, 9, 1, 1, 25.00, 25.00),
(35, 9, 2, 1, 12.00, 12.00),
(36, 9, 3, 1, 15.00, 15.00),
(37, 10, 1, 1, 25.00, 25.00),
(38, 11, 1, 1, 25.00, 25.00),
(39, 11, 2, 1, 12.00, 12.00),
(40, 11, 3, 1, 15.00, 15.00),
(41, 11, 4, 1, 3.00, 3.00),
(42, 11, 5, 1, 4.00, 4.00),
(43, 11, 6, 1, 20.00, 20.00),
(44, 11, 7, 1, 30.00, 30.00),
(45, 12, 2, 2, 12.00, 24.00),
(46, 12, 4, 1, 3.00, 3.00),
(47, 13, 1, 1, 25.00, 25.00),
(48, 13, 2, 1, 12.00, 12.00),
(49, 13, 4, 2, 3.00, 6.00),
(50, 13, 7, 1, 30.00, 30.00),
(51, 13, 9, 2, 220.00, 440.00),
(52, 13, 10, 1, 10.00, 10.00),
(53, 13, 11, 2, 9.00, 18.00),
(54, 14, 2, 25, 12.00, 300.00),
(55, 15, 12, 50, 100.00, 5000.00),
(56, 16, 1, 9, 25.00, 225.00),
(57, 17, 1, 1, 25.00, 25.00),
(58, 17, 2, 1, 12.00, 12.00),
(59, 17, 4, 1, 3.00, 3.00),
(60, 17, 5, 1, 4.00, 4.00),
(61, 18, 1, 1, 25.00, 25.00),
(62, 18, 2, 1, 12.00, 12.00),
(63, 18, 5, 1, 4.00, 4.00),
(64, 18, 7, 1, 30.00, 30.00),
(65, 18, 8, 1, 250.00, 250.00),
(66, 18, 11, 2, 9.00, 18.00),
(67, 19, 1, 1, 25.00, 25.00),
(68, 19, 3, 1, 15.00, 15.00),
(69, 19, 4, 1, 3.00, 3.00),
(70, 19, 5, 1, 4.00, 4.00),
(71, 20, 1, 1, 25.00, 25.00),
(72, 20, 3, 2, 15.00, 30.00),
(73, 20, 5, 2, 4.00, 8.00),
(74, 20, 6, 1, 20.00, 20.00),
(75, 20, 7, 2, 30.00, 60.00),
(76, 20, 9, 1, 220.00, 220.00),
(77, 20, 10, 1, 10.00, 10.00),
(78, 20, 11, 1, 9.00, 9.00),
(79, 20, 12, 1, 100.00, 100.00),
(80, 21, 1, 8, 25.00, 200.00),
(81, 22, 1, 1, 25.00, 25.00),
(82, 23, 3, 1, 15.00, 15.00),
(83, 23, 5, 1, 4.00, 4.00),
(84, 23, 6, 1, 20.00, 20.00),
(85, 23, 10, 1, 10.00, 10.00),
(86, 24, 4, 2, 3.00, 6.00),
(87, 25, 1, 1, 25.00, 25.00),
(88, 26, 1, 8, 25.00, 200.00),
(89, 27, 1, 1, 25.00, 25.00),
(90, 27, 5, 1, 4.00, 4.00),
(91, 27, 7, 1, 30.00, 30.00),
(92, 28, 8, 4, 250.00, 1000.00),
(93, 29, 3, 1, 15.00, 15.00),
(94, 29, 6, 1, 20.00, 20.00),
(95, 29, 10, 1, 10.00, 10.00),
(96, 30, 9, 7, 220.00, 1540.00),
(97, 31, 9, 7, 220.00, 1540.00),
(98, 32, 7, 70, 30.00, 2100.00),
(99, 33, 7, 70, 30.00, 2100.00),
(100, 34, 7, 70, 30.00, 2100.00),
(101, 35, 3, 1, 15.00, 15.00),
(102, 36, 3, 1, 15.00, 15.00),
(103, 37, 1, 8, 25.00, 200.00),
(104, 38, 7, 2, 30.00, 60.00),
(105, 39, 1, 1, 25.00, 25.00),
(106, 39, 2, 1, 12.00, 12.00),
(107, 39, 5, 1, 4.00, 4.00),
(108, 39, 8, 1, 250.00, 250.00),
(109, 40, 1, 1, 25.00, 25.00),
(110, 40, 2, 1, 12.00, 12.00),
(111, 40, 5, 1, 4.00, 4.00),
(112, 40, 6, 1, 20.00, 20.00),
(113, 41, 2, 4, 12.00, 48.00),
(114, 41, 3, 1, 15.00, 15.00),
(115, 41, 8, 1, 250.00, 250.00),
(116, 42, 1, 3, 25.00, 75.00),
(117, 42, 3, 1, 15.00, 15.00),
(118, 43, 1, 3, 25.00, 75.00),
(119, 43, 3, 1, 15.00, 15.00),
(120, 44, 5, 2, 4.00, 8.00),
(121, 44, 12, 1, 100.00, 100.00),
(122, 45, 3, 133, 15.00, 1995.00);

-- --------------------------------------------------------

--
-- Table structure for table `devoluciones`
--

CREATE TABLE `devoluciones` (
  `id_devolucion` int(11) NOT NULL,
  `id_venta` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `fecha` datetime DEFAULT current_timestamp(),
  `monto_devuelto` decimal(10,2) NOT NULL,
  `tipo_reembolso` enum('EFECTIVO','NOTA_CREDITO') NOT NULL DEFAULT 'EFECTIVO',
  `notas` text DEFAULT NULL,
  `motivo` enum('PRODUCTO_DAÑADO','ERROR_COBRO','PRODUCTO_INCORRECTO','CLIENTE_ARREPENTIDO','CADUCADO','OTRO') DEFAULT 'OTRO'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Dumping data for table `devoluciones`
--

INSERT INTO `devoluciones` (`id_devolucion`, `id_venta`, `id_usuario`, `fecha`, `monto_devuelto`, `tipo_reembolso`, `notas`, `motivo`) VALUES
(1, 4, 1, '2026-04-22 01:55:18', 322.00, 'EFECTIVO', NULL, 'OTRO'),
(2, 1, 1, '2026-04-27 10:03:16', 25.00, 'EFECTIVO', NULL, 'OTRO'),
(3, 7, 1, '2026-04-27 10:09:08', 65.00, 'EFECTIVO', NULL, 'OTRO'),
(4, 18, 1, '2026-05-10 17:07:52', 25.00, 'EFECTIVO', NULL, 'OTRO'),
(5, 15, 1, '2026-05-10 17:08:14', 5000.00, 'EFECTIVO', NULL, 'OTRO'),
(6, 21, 1, '2026-05-10 17:13:04', 25.00, 'EFECTIVO', NULL, 'OTRO'),
(7, 21, 1, '2026-05-10 17:13:17', 200.00, 'EFECTIVO', NULL, 'OTRO'),
(10, 25, 1, '2026-05-10 18:01:16', 25.00, 'EFECTIVO', '', 'OTRO'),
(11, 26, 1, '2026-05-10 18:01:48', 200.00, 'EFECTIVO', '', 'OTRO'),
(12, 38, 1, '2026-05-10 19:48:09', 30.00, 'EFECTIVO', '', 'OTRO');

-- --------------------------------------------------------

--
-- Table structure for table `movimientos_caja`
--

CREATE TABLE `movimientos_caja` (
  `id_movimiento` int(11) NOT NULL,
  `id_caja` int(11) NOT NULL,
  `tipo` enum('INGRESO','RETIRO') NOT NULL,
  `monto` decimal(10,2) NOT NULL,
  `motivo` varchar(255) DEFAULT NULL,
  `fecha` datetime NOT NULL DEFAULT current_timestamp(),
  `id_usuario` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- --------------------------------------------------------

--
-- Table structure for table `movimientos_inventario`
--

CREATE TABLE `movimientos_inventario` (
  `id_movimiento` int(11) NOT NULL,
  `id_producto` int(11) NOT NULL,
  `tipo` enum('VENTA','DEVOLUCION','AJUSTE_ENTRADA','AJUSTE_SALIDA','MERMA','CANCELACION') NOT NULL,
  `cantidad` int(11) NOT NULL,
  `stock_anterior` int(11) NOT NULL,
  `stock_nuevo` int(11) NOT NULL,
  `referencia_id` int(11) DEFAULT NULL,
  `referencia_tipo` varchar(30) DEFAULT NULL,
  `id_usuario` int(11) NOT NULL,
  `fecha` datetime DEFAULT current_timestamp(),
  `notas` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Dumping data for table `movimientos_inventario`
--

INSERT INTO `movimientos_inventario` (`id_movimiento`, `id_producto`, `tipo`, `cantidad`, `stock_anterior`, `stock_nuevo`, `referencia_id`, `referencia_tipo`, `id_usuario`, `fecha`, `notas`) VALUES
(1, 2, 'AJUSTE_ENTRADA', 4, 4, 8, 0, 'AJUSTE_MANUAL', 1, '2026-05-10 19:45:17', 'Ajuste manual de stock por Administrador'),
(2, 1, 'AJUSTE_ENTRADA', 8, 8, 16, 0, 'AJUSTE_MANUAL', 1, '2026-05-10 19:45:21', 'Ajuste manual de stock por Administrador'),
(3, 2, 'AJUSTE_ENTRADA', 2, 6, 8, 0, 'AJUSTE_MANUAL', 1, '2026-05-10 19:45:23', 'Ajuste manual de stock por Administrador'),
(4, 7, 'VENTA', 2, 10, 8, 38, 'VENTA', 1, '2026-05-10 19:46:06', 'Venta VTA-000038'),
(5, 7, 'DEVOLUCION', 1, 9, 10, 12, 'DEVOLUCION', 1, '2026-05-10 19:48:09', 'Devolución DEV-000012'),
(6, 1, 'VENTA', 1, 8, 7, 39, 'VENTA', 1, '2026-05-11 01:28:00', 'Venta VTA-000039'),
(7, 2, 'VENTA', 1, 6, 5, 39, 'VENTA', 1, '2026-05-11 01:28:00', 'Venta VTA-000039'),
(8, 5, 'VENTA', 1, 241, 240, 39, 'VENTA', 1, '2026-05-11 01:28:00', 'Venta VTA-000039'),
(9, 8, 'VENTA', 1, 19, 18, 39, 'VENTA', 1, '2026-05-11 01:28:00', 'Venta VTA-000039'),
(10, 1, 'VENTA', 1, 7, 6, 40, 'VENTA', 3, '2026-05-11 01:30:11', 'Venta VTA-000040'),
(11, 2, 'VENTA', 1, 5, 4, 40, 'VENTA', 3, '2026-05-11 01:30:11', 'Venta VTA-000040'),
(12, 5, 'VENTA', 1, 240, 239, 40, 'VENTA', 3, '2026-05-11 01:30:11', 'Venta VTA-000040'),
(13, 6, 'VENTA', 1, 94, 93, 40, 'VENTA', 3, '2026-05-11 01:30:11', 'Venta VTA-000040'),
(14, 2, 'VENTA', 4, 4, 0, 41, 'VENTA', 3, '2026-05-11 01:30:20', 'Venta VTA-000041'),
(15, 3, 'VENTA', 1, 136, 135, 41, 'VENTA', 3, '2026-05-11 01:30:20', 'Venta VTA-000041'),
(16, 8, 'VENTA', 1, 18, 17, 41, 'VENTA', 3, '2026-05-11 01:30:20', 'Venta VTA-000041'),
(17, 1, 'VENTA', 3, 6, 3, 42, 'VENTA', 1, '2026-05-11 10:06:47', 'Venta VTA-000042'),
(18, 3, 'VENTA', 1, 135, 134, 42, 'VENTA', 1, '2026-05-11 10:06:47', 'Venta VTA-000042'),
(19, 1, 'VENTA', 3, 3, 0, 43, 'VENTA', 1, '2026-05-11 10:18:34', 'Venta VTA-000043'),
(20, 3, 'VENTA', 1, 134, 133, 43, 'VENTA', 1, '2026-05-11 10:18:34', 'Venta VTA-000043'),
(21, 5, 'VENTA', 2, 239, 237, 44, 'VENTA', 1, '2026-05-11 10:25:14', 'Venta VTA-000044'),
(22, 12, 'VENTA', 1, 99, 98, 44, 'VENTA', 1, '2026-05-11 10:25:14', 'Venta VTA-000044'),
(23, 3, 'VENTA', 133, 133, 0, 45, 'VENTA', 1, '2026-05-11 10:26:35', 'Venta VTA-000045'),
(24, 3, 'CANCELACION', 133, 133, 266, 45, 'VENTA', 1, '2026-05-11 10:28:37', 'Cancelación venta VTA-000045');

-- --------------------------------------------------------

--
-- Table structure for table `pagos`
--

CREATE TABLE `pagos` (
  `id_pago` int(11) NOT NULL,
  `id_venta` int(11) NOT NULL,
  `tipo_pago` enum('EFECTIVO','TARJETA','TRANSFERENCIA','MIXTO','FIADO','DOLARES','MIXTO_USD') NOT NULL,
  `monto_recibido` decimal(10,2) NOT NULL DEFAULT 0.00,
  `cambio` decimal(10,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Dumping data for table `pagos`
--

INSERT INTO `pagos` (`id_pago`, `id_venta`, `tipo_pago`, `monto_recibido`, `cambio`) VALUES
(1, 1, 'EFECTIVO', 50.00, 25.00),
(2, 2, 'EFECTIVO', 900.00, 675.00),
(3, 3, 'MIXTO', 347.00, 0.00),
(4, 4, 'EFECTIVO', 900.00, 578.00),
(5, 5, 'EFECTIVO', 900.00, 559.00),
(6, 6, 'EFECTIVO', 90.00, 53.00),
(7, 7, 'EFECTIVO', 125.00, 0.00),
(8, 8, 'EFECTIVO', 100.00, 21.00),
(9, 9, 'EFECTIVO', 1000.00, 948.00),
(10, 10, 'EFECTIVO', 25.00, 0.00),
(11, 11, 'EFECTIVO', 120.00, 11.00),
(12, 12, 'EFECTIVO', 111.00, 84.00),
(13, 13, 'EFECTIVO', 600.00, 59.00),
(14, 14, 'EFECTIVO', 300.00, 0.00),
(15, 15, 'EFECTIVO', 10000.00, 5000.00),
(16, 16, 'EFECTIVO', 225.00, 0.00),
(17, 17, 'EFECTIVO', 900.00, 856.00),
(18, 18, 'EFECTIVO', 339.00, 0.00),
(19, 19, 'EFECTIVO', 100.00, 53.00),
(20, 20, 'EFECTIVO', 490.00, 8.00),
(21, 21, 'TARJETA', 200.00, 0.00),
(22, 22, 'EFECTIVO', 50.00, 25.00),
(23, 23, 'TARJETA', 49.00, 0.00),
(24, 24, 'EFECTIVO', 50.00, 44.00),
(25, 25, 'EFECTIVO', 50.00, 25.00),
(26, 26, 'EFECTIVO', 200.00, 0.00),
(27, 27, 'TRANSFERENCIA', 59.00, 0.00),
(28, 28, 'DOLARES', 1700.00, 700.00),
(29, 29, 'EFECTIVO', 50.00, 5.00),
(30, 30, 'TARJETA', 1540.00, 0.00),
(31, 31, 'EFECTIVO', 1540.00, 0.00),
(32, 32, 'TARJETA', 2100.00, 0.00),
(33, 33, 'TARJETA', 2100.00, 0.00),
(34, 34, 'EFECTIVO', 2100.00, 0.00),
(35, 35, 'EFECTIVO', 50.00, 35.00),
(36, 36, 'EFECTIVO', 50.00, 35.00),
(37, 37, 'TARJETA', 200.00, 0.00),
(38, 38, 'EFECTIVO', 60.00, 0.00),
(39, 39, 'EFECTIVO', 300.00, 9.00),
(40, 40, 'EFECTIVO', 70.00, 9.00),
(41, 41, 'EFECTIVO', 400.00, 87.00),
(42, 42, 'TARJETA', 90.00, 0.00),
(43, 43, 'DOLARES', 340.00, 250.00),
(44, 45, 'TARJETA', 1995.00, 0.00);

-- --------------------------------------------------------

--
-- Table structure for table `pagos_cliente`
--

CREATE TABLE `pagos_cliente` (
  `id_pago` int(11) NOT NULL,
  `id_cliente` int(11) NOT NULL,
  `monto` decimal(10,2) NOT NULL,
  `fecha` datetime NOT NULL DEFAULT current_timestamp(),
  `tipo` enum('CARGO','ABONO') NOT NULL,
  `id_venta` int(11) DEFAULT NULL,
  `notas` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Dumping data for table `pagos_cliente`
--

INSERT INTO `pagos_cliente` (`id_pago`, `id_cliente`, `monto`, `fecha`, `tipo`, `id_venta`, `notas`) VALUES
(1, 2, 200.00, '2026-05-10 18:07:49', 'ABONO', NULL, 'Abono manual'),
(2, 8, 108.00, '2026-05-11 10:25:14', 'CARGO', 44, 'Venta a credito');

-- --------------------------------------------------------

--
-- Table structure for table `productos`
--

CREATE TABLE `productos` (
  `id_producto` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `codigo_barras` varchar(50) DEFAULT NULL,
  `precio` decimal(10,2) NOT NULL,
  `costo` decimal(10,2) NOT NULL,
  `stock` int(11) NOT NULL DEFAULT 0,
  `id_categoria` int(11) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `stock_minimo` int(11) NOT NULL DEFAULT 5
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Dumping data for table `productos`
--

INSERT INTO `productos` (`id_producto`, `nombre`, `codigo_barras`, `precio`, `costo`, `stock`, `id_categoria`, `activo`, `stock_minimo`) VALUES
(1, 'Volovan de Camaron', NULL, 25.00, 12.00, 0, 6, 1, 0),
(2, 'Concha', NULL, 12.00, 5.00, 0, 1, 1, 20),
(3, 'Cuernito', NULL, 15.00, 6.00, 133, 1, 1, 20),
(4, 'Bolillo', NULL, 3.00, 1.00, 289, 2, 1, 50),
(5, 'Telera', NULL, 4.00, 2.00, 237, 2, 1, 50),
(6, 'Cafe chico', NULL, 20.00, 8.00, 93, 3, 1, 10),
(7, 'Cafe grande', NULL, 30.00, 12.00, 9, 3, 1, 1),
(8, 'Pastel chocolate', NULL, 250.00, 120.00, 17, 4, 1, 5),
(9, 'Pastel vainilla', NULL, 220.00, 110.00, 100, 4, 1, 5),
(10, 'Galleta chocochip', NULL, 10.00, 4.00, 296, 11, 1, 30),
(11, 'Galleta avena', NULL, 9.00, 3.00, 275, 11, 1, 30),
(12, 'Fresas con crema', NULL, 100.00, 70.00, 98, 12, 1, 0);

-- --------------------------------------------------------

--
-- Table structure for table `roles`
--

CREATE TABLE `roles` (
  `id_rol` int(11) NOT NULL,
  `nombre` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Dumping data for table `roles`
--

INSERT INTO `roles` (`id_rol`, `nombre`) VALUES
(1, 'admin'),
(2, 'cajero');

-- --------------------------------------------------------

--
-- Table structure for table `usuarios`
--

CREATE TABLE `usuarios` (
  `id_usuario` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `usuario` varchar(50) NOT NULL,
  `contrasena` varchar(255) NOT NULL,
  `id_rol` int(11) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Dumping data for table `usuarios`
--

INSERT INTO `usuarios` (`id_usuario`, `nombre`, `usuario`, `contrasena`, `id_rol`, `activo`) VALUES
(1, 'Administrador', 'admin', 'admin123', 1, 1),
(2, 'Joaquin', 'Joto', '123', 2, 1),
(3, 'Francisco Hernandez', 'user', '123', 2, 1);

-- --------------------------------------------------------

--
-- Table structure for table `ventas`
--

CREATE TABLE `ventas` (
  `id_venta` int(11) NOT NULL,
  `fecha` datetime NOT NULL DEFAULT current_timestamp(),
  `total` decimal(10,2) NOT NULL DEFAULT 0.00,
  `metodo_pago` varchar(50) DEFAULT 'Efectivo',
  `id_usuario` int(11) NOT NULL,
  `id_caja` int(11) NOT NULL,
  `id_cliente` int(11) NOT NULL DEFAULT 1,
  `estado` enum('COMPLETADA','CANCELADA','PARCIALMENTE_DEVUELTA','DEVUELTA') DEFAULT 'COMPLETADA'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Dumping data for table `ventas`
--

INSERT INTO `ventas` (`id_venta`, `fecha`, `total`, `metodo_pago`, `id_usuario`, `id_caja`, `id_cliente`, `estado`) VALUES
(1, '2026-04-21 22:04:47', 25.00, 'Efectivo', 1, 1, 1, 'COMPLETADA'),
(2, '2026-04-22 00:25:22', 225.00, 'Efectivo', 1, 1, 1, 'COMPLETADA'),
(3, '2026-04-22 00:34:22', 347.00, 'Efectivo', 1, 1, 1, 'COMPLETADA'),
(4, '2026-04-22 01:28:15', 322.00, 'Efectivo', 1, 1, 1, 'COMPLETADA'),
(5, '2026-04-22 01:30:47', 341.00, 'Efectivo', 1, 1, 1, 'COMPLETADA'),
(6, '2026-04-22 01:53:41', 37.00, 'Efectivo', 1, 1, 1, 'COMPLETADA'),
(7, '2026-04-27 10:04:56', 125.00, 'Efectivo', 1, 1, 1, 'COMPLETADA'),
(8, '2026-04-27 10:08:09', 79.00, 'Efectivo', 1, 1, 1, 'COMPLETADA'),
(9, '2026-04-27 10:08:35', 52.00, 'Efectivo', 1, 1, 1, 'COMPLETADA'),
(10, '2026-05-01 02:06:07', 25.00, 'Efectivo', 1, 1, 1, 'COMPLETADA'),
(11, '2026-05-01 02:06:39', 109.00, 'Efectivo', 1, 1, 3, 'COMPLETADA'),
(12, '2026-05-01 02:22:26', 27.00, 'Efectivo', 1, 1, 1, 'COMPLETADA'),
(13, '2026-05-01 02:45:33', 541.00, 'Efectivo', 1, 1, 1, 'COMPLETADA'),
(14, '2026-05-01 02:50:12', 300.00, 'Efectivo', 1, 1, 1, 'COMPLETADA'),
(15, '2026-05-02 16:17:53', 5000.00, 'Efectivo', 1, 2, 1, 'COMPLETADA'),
(16, '2026-05-04 00:50:17', 225.00, 'Efectivo', 1, 2, 1, 'COMPLETADA'),
(17, '2026-05-06 02:01:35', 44.00, 'Efectivo', 1, 2, 1, 'COMPLETADA'),
(18, '2026-05-10 02:17:54', 339.00, 'Efectivo', 1, 2, 1, 'COMPLETADA'),
(19, '2026-05-10 03:55:25', 47.00, 'Efectivo', 1, 2, 1, 'COMPLETADA'),
(20, '2026-05-10 15:29:22', 482.00, 'Efectivo', 1, 2, 1, 'COMPLETADA'),
(21, '2026-05-10 17:12:41', 200.00, 'Efectivo', 1, 2, 1, 'COMPLETADA'),
(22, '2026-05-10 17:23:25', 25.00, 'Efectivo', 1, 2, 1, 'COMPLETADA'),
(23, '2026-05-10 17:25:46', 49.00, 'Efectivo', 1, 2, 1, 'COMPLETADA'),
(24, '2026-05-10 17:32:00', 6.00, 'Efectivo', 1, 2, 1, 'COMPLETADA'),
(25, '2026-05-10 18:01:08', 25.00, 'Efectivo', 1, 2, 1, 'DEVUELTA'),
(26, '2026-05-10 18:01:31', 200.00, 'Efectivo', 1, 2, 1, 'DEVUELTA'),
(27, '2026-05-10 18:50:43', 59.00, 'TRANSFERENCIA', 1, 2, 1, 'CANCELADA'),
(28, '2026-05-10 18:53:05', 1000.00, 'DOLARES', 1, 2, 1, 'CANCELADA'),
(29, '2026-05-10 19:29:17', 45.00, 'EFECTIVO', 1, 2, 1, 'COMPLETADA'),
(30, '2026-05-10 19:30:29', 1540.00, 'TARJETA', 1, 2, 1, 'COMPLETADA'),
(31, '2026-05-10 19:30:37', 1540.00, 'EFECTIVO', 1, 2, 1, 'COMPLETADA'),
(32, '2026-05-10 19:31:38', 2100.00, 'TARJETA', 1, 2, 1, 'COMPLETADA'),
(33, '2026-05-10 19:31:56', 2100.00, 'TARJETA', 1, 2, 1, 'COMPLETADA'),
(34, '2026-05-10 19:32:53', 2100.00, 'EFECTIVO', 1, 2, 1, 'COMPLETADA'),
(35, '2026-05-10 19:33:10', 15.00, 'EFECTIVO', 1, 2, 1, 'COMPLETADA'),
(36, '2026-05-10 19:33:17', 15.00, 'EFECTIVO', 1, 2, 1, 'COMPLETADA'),
(37, '2026-05-10 19:40:00', 200.00, 'TARJETA', 1, 2, 1, 'COMPLETADA'),
(38, '2026-05-10 19:46:06', 60.00, 'EFECTIVO', 1, 2, 1, 'PARCIALMENTE_DEVUELTA'),
(39, '2026-05-11 01:28:00', 291.00, 'EFECTIVO', 1, 2, 1, 'COMPLETADA'),
(40, '2026-05-11 01:30:11', 61.00, 'EFECTIVO', 3, 2, 1, 'COMPLETADA'),
(41, '2026-05-11 01:30:20', 313.00, 'EFECTIVO', 3, 2, 1, 'COMPLETADA'),
(42, '2026-05-11 10:06:47', 90.00, 'TARJETA', 1, 2, 1, 'COMPLETADA'),
(43, '2026-05-11 10:18:34', 90.00, 'DOLARES', 1, 2, 1, 'COMPLETADA'),
(44, '2026-05-11 10:25:14', 108.00, 'FIADO', 1, 2, 8, 'COMPLETADA'),
(45, '2026-05-11 10:26:35', 1995.00, 'TARJETA', 1, 2, 1, 'CANCELADA');

-- --------------------------------------------------------

--
-- Stand-in structure for view `vista_ventas_netas`
-- (See below for the actual view)
--
CREATE TABLE `vista_ventas_netas` (
`id_venta` int(11)
,`fecha` datetime
,`venta_bruta` decimal(10,2)
,`total_devuelto` decimal(32,2)
,`venta_neta` decimal(33,2)
,`estado` varchar(21)
,`cajero` varchar(100)
,`cliente` varchar(100)
,`metodo_pago` varchar(13)
,`hora_venta` int(2)
);

-- --------------------------------------------------------

--
-- Structure for view `vista_ventas_netas`
--
DROP TABLE IF EXISTS `vista_ventas_netas`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vista_ventas_netas`  AS SELECT `v`.`id_venta` AS `id_venta`, `v`.`fecha` AS `fecha`, `v`.`total` AS `venta_bruta`, coalesce(sum(`dev`.`monto_devuelto`),0) AS `total_devuelto`, `v`.`total`- coalesce(sum(`dev`.`monto_devuelto`),0) AS `venta_neta`, coalesce(`v`.`estado`,'COMPLETADA') AS `estado`, `u`.`nombre` AS `cajero`, coalesce(`c`.`nombre`,'Publico General') AS `cliente`, coalesce(`p`.`tipo_pago`,'FIADO') AS `metodo_pago`, hour(`v`.`fecha`) AS `hora_venta` FROM ((((`ventas` `v` join `usuarios` `u` on(`v`.`id_usuario` = `u`.`id_usuario`)) left join `clientes` `c` on(`v`.`id_cliente` = `c`.`id_cliente`)) left join `pagos` `p` on(`p`.`id_venta` = `v`.`id_venta`)) left join `devoluciones` `dev` on(`dev`.`id_venta` = `v`.`id_venta`)) GROUP BY `v`.`id_venta`, `v`.`fecha`, `v`.`total`, `v`.`estado`, `u`.`nombre`, `c`.`nombre`, `p`.`tipo_pago` ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `auditoria`
--
ALTER TABLE `auditoria`
  ADD PRIMARY KEY (`id_log`),
  ADD KEY `idx_audit_usuario` (`id_usuario`),
  ADD KEY `idx_audit_fecha` (`fecha`);

--
-- Indexes for table `caja`
--
ALTER TABLE `caja`
  ADD PRIMARY KEY (`id_caja`),
  ADD KEY `fk_caja_usuario` (`id_usuario`);

--
-- Indexes for table `cancelaciones`
--
ALTER TABLE `cancelaciones`
  ADD PRIMARY KEY (`id_cancelacion`),
  ADD KEY `id_venta` (`id_venta`),
  ADD KEY `id_usuario` (`id_usuario`);

--
-- Indexes for table `categorias`
--
ALTER TABLE `categorias`
  ADD PRIMARY KEY (`id_categoria`);

--
-- Indexes for table `clientes`
--
ALTER TABLE `clientes`
  ADD PRIMARY KEY (`id_cliente`);

--
-- Indexes for table `corte_caja`
--
ALTER TABLE `corte_caja`
  ADD PRIMARY KEY (`id_corte`),
  ADD KEY `fk_corte_caja` (`id_caja`),
  ADD KEY `fk_corte_usuario` (`id_usuario`);

--
-- Indexes for table `detalle_devolucion`
--
ALTER TABLE `detalle_devolucion`
  ADD PRIMARY KEY (`id_detalle_dev`),
  ADD KEY `id_devolucion` (`id_devolucion`),
  ADD KEY `id_producto` (`id_producto`),
  ADD KEY `idx_det_dev` (`id_detalle_venta`);

--
-- Indexes for table `detalle_venta`
--
ALTER TABLE `detalle_venta`
  ADD PRIMARY KEY (`id_detalle`),
  ADD KEY `idx_detalle_venta` (`id_venta`),
  ADD KEY `idx_detalle_prod` (`id_producto`);

--
-- Indexes for table `devoluciones`
--
ALTER TABLE `devoluciones`
  ADD PRIMARY KEY (`id_devolucion`),
  ADD KEY `id_usuario` (`id_usuario`),
  ADD KEY `idx_dev_venta` (`id_venta`);

--
-- Indexes for table `movimientos_caja`
--
ALTER TABLE `movimientos_caja`
  ADD PRIMARY KEY (`id_movimiento`),
  ADD KEY `fk_mov_caja` (`id_caja`),
  ADD KEY `fk_mov_usuario` (`id_usuario`);

--
-- Indexes for table `movimientos_inventario`
--
ALTER TABLE `movimientos_inventario`
  ADD PRIMARY KEY (`id_movimiento`),
  ADD KEY `id_usuario` (`id_usuario`),
  ADD KEY `idx_mov_producto` (`id_producto`),
  ADD KEY `idx_mov_fecha` (`fecha`);

--
-- Indexes for table `pagos`
--
ALTER TABLE `pagos`
  ADD PRIMARY KEY (`id_pago`),
  ADD KEY `fk_pagos_venta` (`id_venta`);

--
-- Indexes for table `pagos_cliente`
--
ALTER TABLE `pagos_cliente`
  ADD PRIMARY KEY (`id_pago`),
  ADD KEY `fk_pagcliente_cliente` (`id_cliente`),
  ADD KEY `fk_pagcliente_venta` (`id_venta`);

--
-- Indexes for table `productos`
--
ALTER TABLE `productos`
  ADD PRIMARY KEY (`id_producto`),
  ADD UNIQUE KEY `codigo_barras` (`codigo_barras`),
  ADD KEY `fk_productos_categoria` (`id_categoria`);

--
-- Indexes for table `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`id_rol`);

--
-- Indexes for table `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id_usuario`),
  ADD UNIQUE KEY `usuario` (`usuario`),
  ADD KEY `fk_usuarios_rol` (`id_rol`);

--
-- Indexes for table `ventas`
--
ALTER TABLE `ventas`
  ADD PRIMARY KEY (`id_venta`),
  ADD KEY `fk_ventas_usuario` (`id_usuario`),
  ADD KEY `fk_ventas_cliente` (`id_cliente`),
  ADD KEY `idx_ventas_fecha` (`fecha`),
  ADD KEY `idx_ventas_estado` (`estado`),
  ADD KEY `idx_ventas_caja` (`id_caja`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `auditoria`
--
ALTER TABLE `auditoria`
  MODIFY `id_log` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=43;

--
-- AUTO_INCREMENT for table `caja`
--
ALTER TABLE `caja`
  MODIFY `id_caja` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `cancelaciones`
--
ALTER TABLE `cancelaciones`
  MODIFY `id_cancelacion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `categorias`
--
ALTER TABLE `categorias`
  MODIFY `id_categoria` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `clientes`
--
ALTER TABLE `clientes`
  MODIFY `id_cliente` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `corte_caja`
--
ALTER TABLE `corte_caja`
  MODIFY `id_corte` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `detalle_devolucion`
--
ALTER TABLE `detalle_devolucion`
  MODIFY `id_detalle_dev` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `detalle_venta`
--
ALTER TABLE `detalle_venta`
  MODIFY `id_detalle` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=123;

--
-- AUTO_INCREMENT for table `devoluciones`
--
ALTER TABLE `devoluciones`
  MODIFY `id_devolucion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `movimientos_caja`
--
ALTER TABLE `movimientos_caja`
  MODIFY `id_movimiento` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `movimientos_inventario`
--
ALTER TABLE `movimientos_inventario`
  MODIFY `id_movimiento` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;

--
-- AUTO_INCREMENT for table `pagos`
--
ALTER TABLE `pagos`
  MODIFY `id_pago` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=45;

--
-- AUTO_INCREMENT for table `pagos_cliente`
--
ALTER TABLE `pagos_cliente`
  MODIFY `id_pago` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `productos`
--
ALTER TABLE `productos`
  MODIFY `id_producto` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `roles`
--
ALTER TABLE `roles`
  MODIFY `id_rol` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `ventas`
--
ALTER TABLE `ventas`
  MODIFY `id_venta` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=46;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `auditoria`
--
ALTER TABLE `auditoria`
  ADD CONSTRAINT `auditoria_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`);

--
-- Constraints for table `caja`
--
ALTER TABLE `caja`
  ADD CONSTRAINT `fk_caja_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`);

--
-- Constraints for table `cancelaciones`
--
ALTER TABLE `cancelaciones`
  ADD CONSTRAINT `cancelaciones_ibfk_1` FOREIGN KEY (`id_venta`) REFERENCES `ventas` (`id_venta`),
  ADD CONSTRAINT `cancelaciones_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`);

--
-- Constraints for table `corte_caja`
--
ALTER TABLE `corte_caja`
  ADD CONSTRAINT `fk_corte_caja` FOREIGN KEY (`id_caja`) REFERENCES `caja` (`id_caja`),
  ADD CONSTRAINT `fk_corte_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`);

--
-- Constraints for table `detalle_devolucion`
--
ALTER TABLE `detalle_devolucion`
  ADD CONSTRAINT `detalle_devolucion_ibfk_1` FOREIGN KEY (`id_devolucion`) REFERENCES `devoluciones` (`id_devolucion`),
  ADD CONSTRAINT `detalle_devolucion_ibfk_2` FOREIGN KEY (`id_producto`) REFERENCES `productos` (`id_producto`),
  ADD CONSTRAINT `detalle_devolucion_ibfk_3` FOREIGN KEY (`id_detalle_venta`) REFERENCES `detalle_venta` (`id_detalle`),
  ADD CONSTRAINT `detalle_devolucion_ibfk_4` FOREIGN KEY (`id_detalle_venta`) REFERENCES `detalle_venta` (`id_detalle`);

--
-- Constraints for table `detalle_venta`
--
ALTER TABLE `detalle_venta`
  ADD CONSTRAINT `fk_detalle_producto` FOREIGN KEY (`id_producto`) REFERENCES `productos` (`id_producto`),
  ADD CONSTRAINT `fk_detalle_venta` FOREIGN KEY (`id_venta`) REFERENCES `ventas` (`id_venta`);

--
-- Constraints for table `devoluciones`
--
ALTER TABLE `devoluciones`
  ADD CONSTRAINT `devoluciones_ibfk_1` FOREIGN KEY (`id_venta`) REFERENCES `ventas` (`id_venta`),
  ADD CONSTRAINT `devoluciones_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`);

--
-- Constraints for table `movimientos_caja`
--
ALTER TABLE `movimientos_caja`
  ADD CONSTRAINT `fk_mov_caja` FOREIGN KEY (`id_caja`) REFERENCES `caja` (`id_caja`),
  ADD CONSTRAINT `fk_mov_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`);

--
-- Constraints for table `movimientos_inventario`
--
ALTER TABLE `movimientos_inventario`
  ADD CONSTRAINT `movimientos_inventario_ibfk_1` FOREIGN KEY (`id_producto`) REFERENCES `productos` (`id_producto`),
  ADD CONSTRAINT `movimientos_inventario_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`);

--
-- Constraints for table `pagos`
--
ALTER TABLE `pagos`
  ADD CONSTRAINT `fk_pagos_venta` FOREIGN KEY (`id_venta`) REFERENCES `ventas` (`id_venta`);

--
-- Constraints for table `pagos_cliente`
--
ALTER TABLE `pagos_cliente`
  ADD CONSTRAINT `fk_pagcliente_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `clientes` (`id_cliente`),
  ADD CONSTRAINT `fk_pagcliente_venta` FOREIGN KEY (`id_venta`) REFERENCES `ventas` (`id_venta`);

--
-- Constraints for table `productos`
--
ALTER TABLE `productos`
  ADD CONSTRAINT `fk_productos_categoria` FOREIGN KEY (`id_categoria`) REFERENCES `categorias` (`id_categoria`);

--
-- Constraints for table `usuarios`
--
ALTER TABLE `usuarios`
  ADD CONSTRAINT `fk_usuarios_rol` FOREIGN KEY (`id_rol`) REFERENCES `roles` (`id_rol`);

--
-- Constraints for table `ventas`
--
ALTER TABLE `ventas`
  ADD CONSTRAINT `fk_ventas_caja` FOREIGN KEY (`id_caja`) REFERENCES `caja` (`id_caja`),
  ADD CONSTRAINT `fk_ventas_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `clientes` (`id_cliente`),
  ADD CONSTRAINT `fk_ventas_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

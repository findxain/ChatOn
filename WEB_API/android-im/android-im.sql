-- phpMyAdmin SQL Dump
-- version 3.4.10.1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Feb 22, 2017 at 05:58 AM
-- Server version: 5.5.20
-- PHP Version: 5.3.10

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `android-im`
--

-- --------------------------------------------------------

--
-- Table structure for table `friends`
--

CREATE TABLE IF NOT EXISTS `friends` (
  `Id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `providerId` int(10) unsigned NOT NULL DEFAULT '0',
  `requestId` int(10) unsigned NOT NULL DEFAULT '0',
  `status` binary(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Id`),
  UNIQUE KEY `Index_3` (`providerId`,`requestId`),
  KEY `Index_2` (`providerId`,`requestId`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='providerId is the Id of the users who wish to be friend with' AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `messages`
--

CREATE TABLE IF NOT EXISTS `messages` (
  `id` int(255) NOT NULL AUTO_INCREMENT,
  `fromuid` int(255) NOT NULL,
  `touid` int(255) NOT NULL,
  `sentdt` datetime NOT NULL,
  `read` tinyint(1) NOT NULL DEFAULT '0',
  `readdt` datetime DEFAULT NULL,
  `messagetext` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `type` varchar(255) NOT NULL,
  `file` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `Id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(45) NOT NULL DEFAULT '',
  `password` varchar(32) NOT NULL DEFAULT '',
  `email` varchar(45) NOT NULL DEFAULT '',
  `date` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `status` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `authenticationTime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `userKey` varchar(32) NOT NULL DEFAULT '',
  `IP` varchar(45) NOT NULL DEFAULT '',
  `port` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`Id`),
  UNIQUE KEY `Index_2` (`username`),
  KEY `Index_3` (`authenticationTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

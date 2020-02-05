-- Best practice MySQL as of 5.7.6
--
-- The Aquarium database must exist before running this script
-- and before running the fx_desktop_standard_project because the unit
-- test runs this script. Only the root user can create a MySQL database
-- but you do not want to use the root user and password in your code.
--
-- This script needs to run only once

DROP DATABASE IF EXISTS AQUARIUM;
CREATE DATABASE AQUARIUM;

USE AQUARIUM;

DROP USER IF EXISTS fish@localhost;
CREATE USER fish@'localhost' IDENTIFIED WITH mysql_native_password BY 'kfstandard' REQUIRE NONE;
GRANT ALL ON AQUARIUM.* TO fish@'localhost';

-- This creates a user with access from any IP number except localhost
-- Use only if your MyQL database is on a different host from localhost
-- DROP USER IF EXISTS fish;
-- CREATE USER fish IDENTIFIED WITH mysql_native_password BY 'kfstandard' REQUIRE NONE;
-- GRANT ALL ON AQUARIUM TO fish;

-- This creates a user with access from a specific IP number
-- Preferable to '%'
-- DROP USER IF EXISTS fish@'192.168.0.194';
-- CREATE USER fish@'192.168.0.194' IDENTIFIED WITH mysql_native_password BY 'kfstandard' REQUIRE NONE;
-- GRANT ALL ON AQUARIUM TO fish@'192.168.0.194';

FLUSH PRIVILEGES;

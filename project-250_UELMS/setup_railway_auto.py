#!/usr/bin/env python3
"""
Railway Database Auto-Setup Script
Reads configuration from railway_config.py
"""

import mysql.connector
import sys

def setup_railway_database():
    print("Railway Database Auto-Setup")
    print("=" * 30)
    print()
    
    try:
        # Import configuration
        from railway_config import RAILWAY_CONFIG
        
        # Validate configuration
        required_keys = ['host', 'port', 'database', 'user', 'password']
        for key in required_keys:
            if key not in RAILWAY_CONFIG or RAILWAY_CONFIG[key] == f'YOUR_{key.upper()}_HERE':
                print(f"ERROR: Please edit railway_config.py and set your {key}")
                return False
        
        print("Configuration loaded successfully!")
        print(f"Host: {RAILWAY_CONFIG['host']}")
        print(f"Port: {RAILWAY_CONFIG['port']}")
        print(f"Database: {RAILWAY_CONFIG['database']}")
        print(f"User: {RAILWAY_CONFIG['user']}")
        print()
        
        # Connect to database
        print("Connecting to Railway database...")
        conn = mysql.connector.connect(
            host=RAILWAY_CONFIG['host'],
            port=int(RAILWAY_CONFIG['port']),
            database=RAILWAY_CONFIG['database'],
            user=RAILWAY_CONFIG['user'],
            password=RAILWAY_CONFIG['password']
        )
        cursor = conn.cursor()
        
        # Create users table
        print("Creating users table...")
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INT NOT NULL AUTO_INCREMENT,
                username VARCHAR(50) DEFAULT NULL,
                registration_no VARCHAR(20) DEFAULT NULL,
                password VARCHAR(100) NOT NULL,
                role VARCHAR(10) NOT NULL,
                is_active TINYINT(1) NOT NULL DEFAULT 1,
                PRIMARY KEY (id),
                UNIQUE KEY uk_username (username),
                UNIQUE KEY uk_registration_no (registration_no)
            )
        """)
        
        # Create forget_pass table
        print("Creating forget_pass table...")
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS forget_pass (
                email VARCHAR(100) DEFAULT NULL,
                username VARCHAR(100) DEFAULT NULL,
                password VARCHAR(100) DEFAULT NULL
            )
        """)
        
        # Insert admin user
        print("Creating default admin user...")
        cursor.execute("""
            INSERT IGNORE INTO users (username, password, role, is_active) 
            VALUES ('admin', 'admin123', 'ADMIN', 1)
        """)
        
        # Commit changes
        conn.commit()
        
        # Verify setup
        cursor.execute("SELECT * FROM users WHERE role = 'ADMIN'")
        admin_users = cursor.fetchall()
        
        print()
        print("SUCCESS! Database setup completed.")
        print(f"Found {len(admin_users)} admin user(s)")
        print()
        print("Default login credentials:")
        print("Username: admin")
        print("Password: admin123")
        print()
        print("You can now run your JAR file!")
        
        return True
        
    except ImportError:
        print("ERROR: railway_config.py not found!")
        print("Please make sure railway_config.py exists and is properly configured.")
        return False
    except mysql.connector.Error as err:
        print(f"DATABASE ERROR: {err}")
        print("Please check your Railway connection details.")
        return False
    except Exception as e:
        print(f"UNEXPECTED ERROR: {e}")
        return False
    finally:
        if 'conn' in locals() and conn.is_connected():
            cursor.close()
            conn.close()

if __name__ == "__main__":
    success = setup_railway_database()
    if not success:
        print()
        print("Setup failed. Please:")
        print("1. Edit railway_config.py with your Railway database details")
        print("2. Run this script again")
    print()
    input("Press Enter to exit...")

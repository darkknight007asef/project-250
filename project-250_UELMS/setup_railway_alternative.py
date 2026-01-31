#!/usr/bin/env python3
"""
Railway Database Setup Script
Alternative to MySQL Workbench - sets up database via Python
"""

import mysql.connector
import sys

def setup_railway_database():
    print("Railway Database Auto-Setup")
    print("=" * 30)
    print()
    
    # Get connection details
    host = input("Enter Railway Host: ")
    port = input("Enter Railway Port: ")
    database = input("Enter Database Name (usually 'railway'): ")
    user = input("Enter Username (usually 'root'): ")
    password = input("Enter Password: ")
    
    try:
        # Connect to database
        print("\nConnecting to Railway database...")
        conn = mysql.connector.connect(
            host=host,
            port=int(port),
            database=database,
            user=user,
            password=password
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
        
        print("\n✅ SUCCESS! Database setup completed.")
        print(f"Found {len(admin_users)} admin user(s)")
        print("\nDefault login credentials:")
        print("Username: admin")
        print("Password: admin123")
        print("\nYou can now run your JAR file!")
        
    except mysql.connector.Error as err:
        print(f"\n❌ ERROR: {err}")
        print("Please check your connection details and try again.")
        return False
    except Exception as e:
        print(f"\n❌ UNEXPECTED ERROR: {e}")
        return False
    finally:
        if 'conn' in locals() and conn.is_connected():
            cursor.close()
            conn.close()
    
    return True

if __name__ == "__main__":
    setup_railway_database()
    input("\nPress Enter to exit...")

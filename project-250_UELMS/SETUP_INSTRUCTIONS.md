# University Management System - Railway Setup Instructions

## Step 1: Setup Railway Database

1. **Go to your Railway project dashboard**
2. **Open your MySQL database service**
3. **Click on "Query" or "Console" tab**
4. **Copy and paste the entire content from `railway_setup.sql`**
5. **Execute the SQL script**

This will create:
- `users` table for login/registration
- `forget_pass` table for password recovery
- Default admin user (username: `admin`, password: `admin123`)

## Step 2: Configure Database Connection

1. **Get your Railway database credentials:**
   - Host (e.g., `containers-us-west-123.railway.app`)
   - Port (e.g., `6543`)
   - Database name (usually `railway`)
   - Username (usually `root`)
   - Password (from Railway dashboard)

2. **Update the JAR's database config:**
   - Navigate to: `University Management System/dist/`
   - Edit `db.properties` file
   - Use the template from `railway_db_config_template.properties`
   - Replace placeholder values with your actual Railway credentials

## Step 3: Run the Application

1. **Open terminal/command prompt**
2. **Navigate to the dist folder:**
   ```cmd
   cd "c:\Users\MY PC\Desktop\uni save\project-250_UELMS\University Management System\dist"
   ```

3. **Run the JAR:**
   ```cmd
   java -jar University_Management_System.jar
   ```

## Step 4: Login

- **Username:** `admin`
- **Password:** `admin123`

## Troubleshooting

If you still get "table doesn't exist" errors:
1. Verify the SQL script ran successfully in Railway
2. Check that `db.properties` has the correct Railway credentials
3. Ensure the Railway database user has CREATE/ALTER permissions

## Adding Student Data

To register students, you'll need student records in the `student` table. You can:
1. Import your existing SQL dump (`cloud_compatible.sql` or `freedb_ready.sql`) into Railway
2. Or manually add student records via Railway's SQL console

The JAR is now ready to run with Railway!

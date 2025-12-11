#  Almoviland – Java Web Application
Developed by: Almog Siton 


Almoviland is a modular, database-driven web application designed to manage the rental, review, and administration of movies in a digital library setting.
The system supports both regular users and administrators with clearly separated interfaces and permissions. Users can register, subscribe, search or browse movies by category, borrow and return movies, report losses, and write or manage their own reviews.
Administrators have full control over movie inventory, user roles, categories, loss approvals, and review moderation.
The system includes dynamic pagination, AJAX-based live search, responsive UI components, and REST API endpoints for future expansion — all deployed over a secure, local GlassFish environment using JSF and Apache Derby.

---

 Technologies Used:

- Java EE (Jakarta)
- JSF (JavaServer Faces)
- JDBC (Apache Derby DB)
- XHTML + CSS + JavaScript
- RESTful Web Services (JAX-RS)
- GlassFish Server 7
- Apache Derby (Java DB)

---

## ⚙️ How to Run the Project

### Step 1 – Open the Project

1. Open Apache NetBeans (version 23 or newer).
2. Go to `File > Open Project`.
3. Select the folder called `Almoviland`.

---

### Step 2 – Run the Project

1. Make sure GlassFish Server 7 is installed and configured in NetBeans.
2. Right-click the project name and choose `Run`.
3. The system will open in your browser at:  
   http://localhost:8080/Almoviland/

---

### Step 3 – Make Sure the Database Is Working

1. Go to the `Services` tab in NetBeans.
2. Under `Databases`, make sure there is a Derby database called `almoviland`.
3. If it doesn't exist:
   - Right-click `Java DB` and choose `Create Database`.
   - Use the name `almoviland`, with username and password: `almoviland`.

---

###  Default Admin User (for testing)

When the system runs for the first time, it automatically creates a default admin user.

Use the following credentials to log in as admin:

- **Email**: almoviland@gmail.com  
- **Password**: almoviland  
- **Role**: ADMIN

You can access the admin dashboard from the top navigation bar after login.

Now the system is ready. You can:
- Register as a new user
- Use the admin panel (default admin is created automatically)
- Test borrowing, reviews, and all features

If needed, the REST API is available at:  
http://localhost:8080/Almoviland/api/movies

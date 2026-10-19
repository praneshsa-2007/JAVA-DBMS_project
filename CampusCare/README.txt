CAMPUSCARE - Java + JDBC + MySQL
================================

PROJECT INTERFACES
1. UserApp.java   - student interface; no login; submit a complaint only.
2. AdminApp.java  - separate admin entry point; login required.

ADMIN LOGIN
Username: user
Password: 1234

REQUIREMENTS
- Java JDK 8 or newer
- MySQL Server
- MySQL Connector/J (JDBC driver JAR)

STEP 1: CREATE THE DATABASE
1. Open MySQL Workbench or the MySQL command line.
2. Run the complete contents of setup.sql.
   This creates the campuscare database and complaints table.

STEP 2: CONFIGURE THE DATABASE PASSWORD
Open DBConnection.java and set PASSWORD to your MySQL root password.
If your MySQL username is not root, update USER too.
The default connection is:
jdbc:mysql://localhost:3306/campuscare

STEP 3: ADD MYSQL JDBC DRIVER
Download MySQL Connector/J from the official MySQL website and note the JAR path.
The JDBC JAR must be included in the classpath when compiling and running.

WINDOWS COMMAND PROMPT EXAMPLE
Assume the Connector/J file is named mysql-connector-j-9.x.x.jar and is in this folder.
Replace that example filename with the exact filename you downloaded.

Compile:
javac -cp ".;mysql-connector-j-9.x.x.jar" DBConnection.java UserApp.java AdminApp.java

Run student interface:
java -cp ".;mysql-connector-j-9.x.x.jar" UserApp

Run admin interface:
java -cp ".;mysql-connector-j-9.x.x.jar" AdminApp

LINUX / macOS
Use a colon instead of a semicolon in the classpath:
javac -cp ".:mysql-connector-j-9.x.x.jar" DBConnection.java UserApp.java AdminApp.java
java -cp ".:mysql-connector-j-9.x.x.jar" UserApp
java -cp ".:mysql-connector-j-9.x.x.jar" AdminApp

HOW TO USE
USER:
- Run UserApp.
- Enter student name, category, campus location, and complaint description.
- The program saves the complaint and prints its generated ID.
- There is no student login and no user-side status tracking.

ADMIN:
- Run AdminApp separately.
- Log in with username user and password 1234.
- Use the menu to view, search, update status, or delete complaints.
- Type YES in uppercase to confirm deletion.
- Choose Logout to exit.

NOTES
- Both entry points connect to the same MySQL database.
- Complaint queries use PreparedStatement where user input is involved.
- This is a beginner-friendly academic example. The admin credentials are hardcoded
  as requested, which is suitable only for a local academic demo, not production use.
- If you get "Access denied", check DBConnection.java username/password.
- If you get "No suitable driver", make sure the Connector/J JAR is on the classpath.
- If you get "Unknown database", run setup.sql first.

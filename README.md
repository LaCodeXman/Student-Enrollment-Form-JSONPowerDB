# Student Enrollment Form — Java Swing + MySQL

## Features
- MySQL database `SCHOOL_DB` and table `STUDENT_TABLE`.
- Roll-No is the primary key.
- On launch/reset, only Roll-No is enabled; all other fields and buttons are disabled.
- Enter a Roll-No and press Enter, or move focus out of the field, to check it:
  - Existing Roll-No: existing data is loaded; Roll-No is locked; Update and Reset are enabled.
  - New Roll-No: detail fields are enabled; Save and Reset are enabled.
- Save inserts a new student.
- Update modifies the existing student.
- Reset returns to the initial empty state and focuses Roll-No.
- All fields are mandatory; dates are checked using `yyyy-MM-dd`.
- SQL uses prepared statements.

## Requirements
- JDK 17 or later
- MySQL Server 8.x (or compatible)
- Apache Maven 3.8+

## 1. Create the database
1. Start MySQL Server.
2. Open MySQL Workbench or the MySQL command-line client.
3. Run the contents of `database.sql`.

The script creates `SCHOOL_DB` and `STUDENT_TABLE`. It also inserts sample Roll-No `101` for testing the update workflow.

## 2. Configure database credentials
Open:
`src/main/java/com/microproject/StudentEnrollmentForm.java`

Update these constants to match your MySQL setup:

```java
private static final String DB_URL =
    "jdbc:mysql://localhost:3306/SCHOOL_DB?useSSL=false&serverTimezone=UTC";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "YOUR_MYSQL_PASSWORD";
```

Replace `YOUR_MYSQL_PASSWORD` with your MySQL password. If your MySQL root account has no password, use an empty string: `""`.

## 3. Run the project
Open a terminal in the project folder (the folder containing `pom.xml`) and run:

```bash
mvn clean compile
mvn exec:java
```

Maven downloads MySQL Connector/J automatically from the Maven repository. The first build needs internet access.

You can also open the folder in IntelliJ IDEA or Eclipse as an existing Maven project, wait for dependencies to download, then run `StudentEnrollmentForm.main()`.

## 4. Test the form
### Test A — New student
1. Start the application.
2. Enter an unused Roll-No, such as `202`, and press Enter (or move to another field).
3. The remaining fields become enabled. Enter all details.
4. Dates must use `yyyy-MM-dd`, e.g. `2005-08-21`.
5. Click Save. A success message appears and the form resets.

### Test B — Existing student
1. Enter `101` and press Enter.
2. The sample student details should load.
3. Roll-No remains disabled. Edit any other field.
4. Click Update. A success message appears and the form resets.

### Test C — Reset
1. Enter a new Roll-No and begin entering details.
2. Click Reset.
3. The form returns to the initial state.

## Field rules
| Field | Rule |
|---|---|
| Roll-No | Required, positive whole number, unique primary key |
| Full Name | Required, max 100 characters |
| Class | Required, max 30 characters |
| Birth Date | Required, valid date in `yyyy-MM-dd` format |
| Address | Required, max 255 characters |
| Enrollment Date | Required, valid date in `yyyy-MM-dd` format |

## Common problems
- **Communications link failure:** Ensure MySQL Server is running and the host/port in `DB_URL` are correct.
- **Access denied for user:** Check `DB_USER` and `DB_PASSWORD`.
- **Unknown database SCHOOL_DB:** Run `database.sql` first.
- **No suitable driver:** Run through Maven and allow it to download dependencies; confirm the Maven dependency is resolved.
- **Roll-No lookup does not run:** Press Enter after entering Roll-No, or move focus out of the field.

## Project structure
```text
StudentEnrollmentForm/
├── pom.xml
├── database.sql
├── README.md
└── src/
    └── main/
        └── java/
            └── com/
                └── microproject/
                    └── StudentEnrollmentForm.java
```

# ShopWave - SE4801 Assignment 1

**Name:** HAILEMICHALE LIJALEM AYTENEW  
**Student ID:** ATE/1051/14  
**Section:** 1  
**Course:** SE4801 - Enterprise Application Development  

This is a Spring Boot 3.x REST API application for managing products, categories, and orders built as part of the Enterprise Application Development practical assignment.

---

## 🚀 Necessary Steps for Running the App

### Prerequisites
- JDK 17 or higher installed
- An IDE (IntelliJ IDEA, VS Code, or Eclipse)
- Git (if cloning from a repository)

### 🔨 How to Build

To compile the application, download dependencies, and package it into an executable JAR file, run:

*On Windows:*
```powershell
.\mvnw.cmd clean package
```

*On macOS/Linux:*
```bash
./mvnw clean package
```

This will generate a `.jar` file inside the `target/` directory.

### 🏃‍♂️ How to Run (Running the Application Structure)

The application uses an **H2 in-memory database**, so you don't need to manually configure any external database (MySQL/PostgreSQL) prior to running it. The database schema is automatically generated on startup based on the JPA entities.

**1. Clone the repository:**
```bash
git clone https://github.com/Hailemichale/se4801-assignment1--ATE-1051-14-.git
cd se4801-assignment1--ATE-1051-14-
```

**2. Run the application using the Maven Wrapper:**
*On Windows (PowerShell/Command Prompt):*
```powershell
.\mvnw.cmd spring-boot:run
```
*On macOS/Linux:*
```bash
./mvnw spring-boot:run
```

**3. Access the APIs:**
The application starts by default on `http://localhost:8080`.
You can interact with the REST endpoints (e.g., GET `/api/products`).

**4. Access the Database Console:**
- **URL:** `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:mem:shopwavedb`
- **Username:** `sa`
- **Password:** *(Leave blank)*

---

## 🧪 How to Run Tests

This project includes a suite of unit and integration tests covering mapping, controllers, services, bindings, and repositories.

To execute the tests and ensure everything is functioning as expected, run the following command:

*On Windows:*
```powershell
.\mvnw.cmd test
```

*On macOS/Linux:*
```bash
./mvnw test
```

The tests use an independent, freshly booted in-memory database to prevent test contamination.

---

## 💡 Acknowledgements

Developed with the assistance of **Cloud AI** for accelerated structural boilerplate setup, testing scaffolding, and architectural implementation. 

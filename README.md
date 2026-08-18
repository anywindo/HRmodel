# HR Management Backend (HRmodel)

A robust, enterprise-grade Human Resources management backend built with Java and Spring Boot. This project leverages **Domain-Driven Design (DDD)** principles to encapsulate complex business logic and provide a clear, scalable architecture.

## Overview

This API manages the core organizational structure of a company, handling entities such as Employees, Departments, and Positions. By utilizing DDD, the domain models (e.g., `Employee`, `Department`, `Position`) contain rich business logic rather than acting as simple data containers (anemic models). 

### Key Features
* **Employee Management:** Complete lifecycle management for employees, including hiring, status changes, and personal details.
* **Department & Organizational Structure:** Manage departments, their descriptions, and assign head positions.
* **Position Management:** Define roles within the company, tying together employees and their respective departments.
* **Domain-Driven Design (DDD):** Built with a strong ubiquitous language, bounded contexts, and rich domain models.

## Tech Stack
* **Java** (JDK 17+)
* **Spring Boot** (Web, Data JPA)
* **Maven** for dependency management and build automation

## Project Structure

The codebase is organized to reflect the domain, separating concerns and enforcing architectural boundaries:

* **`src/main/java/model/`**: The core domain layer. Contains entities (`Employee`, `Department`, `Position`), value objects, and business rules.
* **`src/main/java/repository/`**: Data access interfaces.
* **`src/main/java/service/`**: Application services coordinating domain objects to execute use cases.
* **`src/main/java/com/hr/controller/`**: The presentation layer (REST Controllers) exposing the API endpoints.
* **`src/main/java/com/hr/dto/`**: Data Transfer Objects for API requests and responses.
* **`src/main/java/com/hr/config/`**: Configuration classes (e.g., Data Seeders).

## Getting Started

### Prerequisites
* Java 17 or higher
* Maven 3.6+
* A database (configured in `application.properties` or `.env`)

### Setup and Running

1. **Clone the repository (if applicable)**
   ```bash
   git clone <repository-url>
   cd HRmodel/HR_backend
   ```

2. **Configure the environment**
   Ensure your `.env` file or `src/main/resources/application.properties` has the correct database credentials.

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The application will start by default on `http://localhost:8080`.

## API Endpoints Overview

* **`/api/employees`**: Endpoints for creating, retrieving, updating, and managing employees.
* **`/api/departments`**: Endpoints for managing departments and their head positions.
* **`/api/positions`**: Endpoints for defining and retrieving organizational positions.

## Contributing

When contributing to this project, please adhere to the established Domain-Driven Design patterns:
- Place core business rules in the `model` packages.
- Keep the `service` layer thin, acting as an orchestrator.
- Use DTOs for all incoming and outgoing REST API data.

## License
This project is licensed under the MIT License - see the LICENSE file for details.

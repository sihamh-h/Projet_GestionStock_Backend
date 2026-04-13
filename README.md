GestionStock - Backend API

This is the Spring Boot REST API for the Inventory Management System. It handles authentication, security, and core business logic.

## 🚀 Technologies
* **Java 17** & **Spring Boot 3**
* **Spring Security** (JWT Authentication)
* **Spring Data JPA** (MySQL)
* **Lombok** (Boilerplate reduction)
* **Mail Starter** (Email activation & Reset)

## 🔑 Key Features
- [x] **User Management**: Registration with email activation.
- [x] **Security**: JWT-based authentication with custom filters.
- [x] **Email Strategy**: Pattern Strategy for Activation, Forgot Password, and Notifications.
- [x] **Database**: Relationship mapping for Products, Categories, and Users.

## ⚙️ Setup
1. Clone the repository.
2. Create a `application-local.properties` (ignored by Git) for your secrets:
   ```properties
   spring.datasource.username=your_user
   spring.datasource.password=your_password
   spring.mail.password=your_app_password

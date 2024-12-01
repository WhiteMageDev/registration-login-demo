# User Registration and Email Confirmation Backend (Java Spring Boot)

User registration and email verefication backend.

## Description

This project implements a user registration functionality with email confirmation using tokens. After registration, a unique token is sent to the user's email to confirm their account. Once the email is confirmed, the user is granted access to protected resources. Login and authentication are managed using the default Spring Security login form.

## Technologies

- **Spring Boot** — the main framework for developing the backend application.
- **Spring Security** — for authentication and authorization.
- **Spring Data JPA** — for database access and ORM.
- **Spring Boot Starter Mail** — for sending email messages.
- **PostgreSQL** — the database used to store user information.

## Setup and Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/yourusername/your-repository.git
   cd your-repository
2. Install dependencies:
   ```bash
   mvn install
3. Configure database connection settings in application.properties:
   ```bash
   spring.datasource.url=jdbc:postgresql://localhost:5432/your_db_name
   spring.datasource.username=your_db_user
   spring.datasource.password=your_db_password
4. Configure email sending or use default ones for [maildev](https://hub.docker.com/r/maildev/maildev)
   ```bash
   spring.mail.host=smtp.yourmailserver.com
   spring.mail.port=587
   spring.mail.username=your_email@example.com
   spring.mail.password=your_email_password

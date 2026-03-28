# Email Service

A Spring Boot microservice for secure password reset via OTP (One-Time Password) email verification. The service generates a time-limited 6-digit OTP, delivers it to the user's email address using Gmail SMTP, and allows the user to verify the OTP and reset their password.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture Overview](#architecture-overview)
- [API Endpoints](#api-endpoints)
- [Password Reset Flow](#password-reset-flow)
- [Configuration](#configuration)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Running Locally](#running-locally)
  - [Running with Docker](#running-with-docker)
  - [Running with Docker Compose](#running-with-docker-compose)
- [Project Structure](#project-structure)
- [CI/CD Pipeline](#cicd-pipeline)
- [Security](#security)

---

## Features

- Send a 6-digit OTP to a registered user's email address
- Verify OTP validity (format, existence, expiration)
- Reset user password with OTP confirmation
- Asynchronous, non-blocking email delivery via `@Async`
- BCrypt password hashing
- Standardised JSON API responses
- Dockerised deployment with multi-platform images (amd64 / arm64)
- Automated CI/CD pipeline via GitHub Actions

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.4 |
| ORM | Spring Data JPA (Hibernate) |
| Database | MySQL |
| Email | Spring Mail (Gmail SMTP) |
| Security | Spring Security Crypto (BCrypt) |
| Containerisation | Docker, Docker Compose |
| Build Tool | Gradle |
| Code Generation | Lombok |
| CI/CD | GitHub Actions |

---

## Architecture Overview

```
┌─────────────┐       ┌────────────────────────┐       ┌─────────────────┐
│   Client    │──────▶│  EmailSenderController  │──────▶│   OtpService    │
└─────────────┘       │  HealthCheckController  │       │  UserService    │
                      └────────────────────────┘       │  EmailSender    │
                                                        │   Service       │
                                                        └────────┬────────┘
                                                                 │
                              ┌──────────────────────────────────┤
                              │                                  │
                       ┌──────▼──────┐                  ┌───────▼───────┐
                       │  MySQL DB   │                  │  Gmail SMTP   │
                       │ (UserRepo)  │                  │  (Port 587)   │
                       └─────────────┘                  └───────────────┘
```

OTP state is held in an in-memory `ConcurrentHashMap` keyed by email address. Each entry stores the generated OTP value and its expiration time (30 minutes from creation).

---

## API Endpoints

### Base URL: `http://localhost:8080`

#### Health Check

```
GET /ping
```

**Response**
```json
{
  "success": true,
  "message": "pong",
  "timeStamp": "2024-01-01T12:00:00Z",
  "data": null
}
```

---

#### Send OTP

Generates a 6-digit OTP and sends it to the provided email address. The email must belong to a registered user.

```
POST /
Content-Type: application/json
```

**Request Body**
```json
{
  "email": "user@example.com"
}
```

**Success Response (200)**
```json
{
  "success": true,
  "message": "email send initiated",
  "timeStamp": "2024-01-01T12:00:00Z",
  "data": null
}
```

**Error Response – User not found (404)**
```json
{
  "success": false,
  "message": "User not found",
  "timeStamp": "2024-01-01T12:00:00Z",
  "data": null
}
```

---

#### Verify OTP

Validates the OTP sent to the user's email. The OTP expires 30 minutes after generation.

```
POST /verifyOtp
Content-Type: application/json
```

**Request Body**
```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```

**Success Response (200)**
```json
{
  "success": true,
  "message": "OTP is valid",
  "timeStamp": "2024-01-01T12:00:00Z",
  "data": null
}
```

**Error Responses**

| Scenario | HTTP Status | Message |
|---|---|---|
| Invalid OTP format | 400 | "Invalid OTP" |
| OTP not found | 404 | "OTP not found" |
| OTP expired | 400 | "OTP has expired" |
| OTP incorrect | 400 | "Invalid OTP" |

---

#### Reset Password

Resets the user's password. The OTP is validated a second time before the password is changed.

```
POST /resetPassword
Content-Type: application/json
```

**Request Body**
```json
{
  "email": "user@example.com",
  "otp": "123456",
  "password": "newSecurePassword"
}
```

**Success Response (200)**
```json
{
  "success": true,
  "message": "Password Reset Successfully",
  "timeStamp": "2024-01-01T12:00:00Z",
  "data": null
}
```

**Error Response – User not found (404)**
```json
{
  "success": false,
  "message": "User not found",
  "timeStamp": "2024-01-01T12:00:00Z",
  "data": null
}
```

---

## Password Reset Flow

```
1. Client  ──POST /──────────────────────────▶  Service checks user exists
                                                 Generates 6-digit OTP (SecureRandom)
                                                 Stores OTP with 30-min expiry
                                                 Sends OTP email asynchronously
                                                        │
                                               ◀── 200 OK ──
2. User receives OTP in email

3. Client  ──POST /verifyOtp─────────────────▶  Validates OTP (format, exists, expiry, value)
                                               ◀── 200 OK ──

4. Client  ──POST /resetPassword─────────────▶  Re-validates OTP
                                                 BCrypt-encodes new password
                                                 Updates user record in DB
                                                 Clears OTP from memory
                                               ◀── 200 OK ──
```

---

## Configuration

The application uses a `prod` Spring profile. All sensitive values are supplied via environment variables.

| Environment Variable | Description | Example |
|---|---|---|
| `MYSQL_URL` | JDBC connection URL | `jdbc:mysql://localhost:3306/mydb` |
| `MYSQL_USERNAME` | Database username | `root` |
| `MYSQL_PASSWORD` | Database password | `secret` |
| `MAIL_USERNAME` | Gmail address used to send emails | `yourapp@gmail.com` |
| `MAIL_PASSWORD` | Gmail App Password (not your account password) | `abcd efgh ijkl mnop` |

> **Note:** For Gmail, you must use an [App Password](https://support.google.com/accounts/answer/185833). Standard account passwords will not work when 2-Step Verification is enabled.

### Gmail SMTP Settings (pre-configured)

| Property | Value |
|---|---|
| Host | `smtp.gmail.com` |
| Port | `587` |
| Auth | `true` |
| STARTTLS | `required` |

---

## Getting Started

### Prerequisites

- Java 17
- MySQL instance accessible from your machine
- Gmail account with an App Password configured
- Docker (optional, for containerised deployment)

---

### Running Locally

1. **Clone the repository**

   ```bash
   git clone https://github.com/tasneem1010/email_service.git
   cd email_service
   ```

2. **Create the database**

   Connect to your MySQL instance and create the database:

   ```sql
   CREATE DATABASE your_database_name;
   ```

3. **Set environment variables**

   ```bash
   export MYSQL_URL=jdbc:mysql://localhost:3306/your_database_name
   export MYSQL_USERNAME=root
   export MYSQL_PASSWORD=your_mysql_password
   export MAIL_USERNAME=your_gmail@gmail.com
   export MAIL_PASSWORD=your_gmail_app_password
   ```

4. **Build and run**

   ```bash
   ./gradlew clean bootJar
   java -jar build/libs/myapp.jar --spring.profiles.active=prod
   ```

   The application starts on `http://localhost:8080`.

---

### Running with Docker

1. **Build the JAR first**

   ```bash
   ./gradlew clean bootJar
   ```

2. **Build the Docker image**

   ```bash
   docker build -t email_service .
   ```

3. **Run the container**

   ```bash
   docker run -p 8080:8080 \
     -e MYSQL_URL=jdbc:mysql://<MYSQL_HOST>:3306/<DB_NAME> \
     -e MYSQL_USERNAME=root \
     -e MYSQL_PASSWORD=your_mysql_password \
     -e MAIL_USERNAME=your_gmail@gmail.com \
     -e MAIL_PASSWORD=your_gmail_app_password \
     email_service
   ```

---

### Running with Docker Compose

The `docker-compose.yml` uses the pre-built image from Docker Hub. Create a `.env` file in the project root:

```env
MYSQL_SERVER_IP=your_mysql_host
MYSQL_DATABASE=your_database_name
MYSQL_ROOT_PASSWORD=your_mysql_password
MAIL_USERNAME=your_gmail@gmail.com
MAIL_PASSWORD=your_gmail_app_password
```

Then start the service:

```bash
docker-compose up -d
```

The application will be available at `http://localhost:8080`.

---

## Project Structure

```
email_service/
├── src/
│   └── main/
│       ├── java/com/example/
│       │   ├── EmailServiceApplication.java   # Application entry point, enables @Async
│       │   ├── controller/
│       │   │   ├── EmailSenderController.java # OTP and password-reset endpoints
│       │   │   └── HealthCheckController.java # GET /ping health check
│       │   ├── service/
│       │   │   ├── EmailSenderService.java    # Async email sending via JavaMailSender
│       │   │   ├── OtpService.java            # OTP generation, validation, and storage
│       │   │   └── UserService.java           # User lookup and password reset logic
│       │   ├── model/
│       │   │   ├── User.java                  # JPA entity mapped to `user` table
│       │   │   └── OtpData.java               # In-memory OTP data model with expiry check
│       │   ├── repository/
│       │   │   └── UserRepository.java        # Spring Data JPA repository; findByEmail()
│       │   └── dto/
│       │       └── ApiResponse.java           # Generic API response wrapper
│       └── resources/
│           └── application-prod.properties    # Production profile configuration
├── Dockerfile                                 # Multi-stage Docker build definition
├── docker-compose.yml                         # Compose file for containerised deployment
├── build.gradle                               # Gradle build configuration
├── settings.gradle                            # Gradle project name
└── .github/
    └── workflows/
        └── main.yml                           # GitHub Actions CI/CD pipeline
```

### Key Classes

| Class | Responsibility |
|---|---|
| `EmailSenderController` | Receives and validates HTTP requests; delegates to services |
| `HealthCheckController` | Provides a `/ping` liveness endpoint |
| `OtpService` | Generates secure OTPs, manages their lifecycle in memory |
| `EmailSenderService` | Composes and dispatches the OTP email asynchronously |
| `UserService` | Orchestrates OTP re-validation and BCrypt password update |
| `UserRepository` | Database access layer; custom `findByEmail` query |
| `ApiResponse<T>` | Standardised response envelope with status, message, and payload |
| `User` | JPA entity for the `user` table |
| `OtpData` | Holds the OTP value and expiration instant |

---

## CI/CD Pipeline

The GitHub Actions workflow (`.github/workflows/main.yml`) runs on every push to `main` and on manual dispatch:

1. **Build** – Compiles the project and packages it as `myapp.jar` via `./gradlew clean bootJar -x test`.
2. **Docker Build & Push** – Builds a multi-platform image (`linux/amd64`, `linux/arm64`) and pushes it to Docker Hub as `taneem101/project:mail`.
3. **Deploy** – Copies `docker-compose.yml` to the deployment server over SSH and runs `docker-compose up -d`.
4. **Health Check** – Polls `GET /ping` to confirm the new container is healthy.

### Required GitHub Secrets

| Secret | Purpose |
|---|---|
| `DOCKER_USERNAME` | Docker Hub username |
| `DOCKER_PASSWORD` | Docker Hub password or token |
| `SERVER_HOST` | Deployment server hostname / IP |
| `SERVER_USER` | SSH username for the deployment server |
| `SERVER_SSH_KEY` | Private SSH key for the deployment server |
| `MYSQL_SERVER_IP` | MySQL host (injected into docker-compose) |
| `MYSQL_DATABASE` | Database name |
| `MYSQL_ROOT_PASSWORD` | MySQL root password |
| `MAIL_USERNAME` | Gmail address |
| `MAIL_PASSWORD` | Gmail App Password |

---

## Security

- **Password hashing** – All passwords are stored as BCrypt hashes. Plain-text passwords are never persisted.
- **Cryptographically secure OTP** – OTPs are generated using `java.security.SecureRandom`, ensuring unpredictability.
- **OTP expiration** – OTPs are valid for 30 minutes only and are invalidated immediately after a successful password reset.
- **Double OTP validation** – The OTP is verified at `/verifyOtp` and re-verified at `/resetPassword`, preventing replay attacks between the two steps.
- **Non-root container** – The Docker image creates and runs as a dedicated `spring` system user rather than root.
- **Environment-based secrets** – No credentials are baked into the image or source code; all sensitive values are injected at runtime via environment variables.
- **TLS-only email** – STARTTLS is required for all SMTP connections; plaintext delivery is not permitted.

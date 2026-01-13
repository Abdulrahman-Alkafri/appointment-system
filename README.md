# Appointment Management System

This is a comprehensive **Appointment Management System** built with **Java Spring Boot**, designed to facilitate appointment scheduling for various services. It supports multiple user roles (Customer, Staff, Admin), manages working schedules, and handles holiday configurations.

## 👥 Team Work Distribution

The successful delivery of this project was a result of collaborative effort with clear division of responsibilities:

### 🛠️ SRE, Infrastructure & Security

**Team Members:** Abdulrahman Al-kafri & Abdulrahman Al-Dammad

* **Observability Stack:** Implemented a robust monitoring solution using **Prometheus** (metrics), **Grafana** (dashboards/visualization), and **Loki** (log aggregation).
* **Security:** Engineered the authentication system using **JWT** (JSON Web Tokens) for secure access and refresh token flows. Implemented **Redis** for token blacklisting to manage secure logouts effectively.
* **Infrastructure & Orchestration:** Managed containerization of infrastructure using **Docker** and orchestrated the ecosystem using **Docker Compose**.
* **Production Readiness:** Configured **Nginx** as a reverse proxy with **SSL termination** to ensure all application traffic is encrypted via **HTTPS**, preparing the system for production environments.

### 💻 Development, Database & Core Logic

**Team Members:** Aeham Alhaeak & Jalal Kakhi

* **Backend Development:** Developed the core application architecture, including API endpoints, controllers, and services using Spring Boot.
* **Database Management:** Designed the **PostgreSQL** database schema and managed version control and schema evolution through **Flyway** migrations.
* **Business Logic:** Implemented the complex business rules governing appointment scheduling, slot availability, and service configurations.
* **Performance Optimization:** Conducted performance tuning of endpoints and database queries to ensure system efficiency and responsiveness.

## 🚀 How to Run the Project

The project uses Docker for infrastructure and Maven for the application itself.

### Prerequisites

* **Java 21** (or compatible version)
* **Maven** installed
* **Docker** and **Docker Compose** installed

### Steps to Start

1. **Clone the repository:**

    ```bash
    git clone <repository-url>
    cd appointment
    ```

2. **Start the Infrastructure:**
    Run the following command to start the Database, Redis, Nginx, and the Monitoring stack:

    ```bash
    docker-compose up -d
    ```

3. **Run the Spring Boot Application:**
    Once the infrastructure is up, run the application using Maven:

    ```bash
    mvn spring-boot:run
    ```

4. **Access the Application:**
    * **API Base URL (HTTPS):** `https://localhost/api`
    * **Grafana:** `http://localhost:3000` (Default login: `admin` / `admin`)
    * **Prometheus:** `http://localhost:9090`
    * **Loki:** `http://localhost:3100`

## 🔌 Available APIs

### Authentication (`/api/auth`)

* `POST /register` - Register a new user.
* `POST /login` - Login to receive Access and Refresh tokens.
* `POST /refresh` - Refresh an expired access token.
* `POST /logout` - Logout user (invalidates token via Redis blacklist).
* `POST /logout-all` - Logout all sessions for the user.

### Customer (`/api/customer`)

* `GET /appointments/show_active_appointments` - View upcoming active appointments.
* `GET /appointments/available_slots` - Check available time slots for a specific service and date.
* `DELETE /appointments/cancel_appointment/{id}` - Cancel a scheduled appointment.

### Admin (`/api/admin`)

* **User Management:** Create, update, delete, and view users (`/users/...`).
* **Service Management:** Create services, link services to staff, and manage details (`/services/...`).
* **Schedule Management:** Define and manage working schedules for the organization (`/working-schedules/...`).
* **Holiday Management:** Configure holidays and days off (`/holidays/...`).

### Working Schedules (`/api/working-schedules`)

* Public endpoints to view general working hours and schedules.

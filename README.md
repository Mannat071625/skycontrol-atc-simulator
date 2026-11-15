# SKYCONTROL – Air Traffic Control Simulator

A web-based Air Traffic Control Simulator built using Java, Spring Boot, React and MySQL to simulate and manage essential airport operations.

## Overview

SKYCONTROL provides a centralized platform for managing aircraft, flights, runways, gates, emergencies and weather conditions through an interactive dashboard.

## Key Features

- **Aircraft Management:** Register, search and track passenger, cargo and emergency aircraft.
- **Flight Management:** Schedule flights and manage flight statuses.
- **Runway and Gate Management:** Monitor availability and manage resource allocation.
- **Queue Management:** Handle aircraft waiting for runway and gate resources.
- **Emergency Handling:** Prioritize and process emergency requests.
- **Weather Monitoring:** Apply landing and takeoff restrictions based on weather conditions.
- **Flight History:** Maintain and retrieve records of operational events.
- **Aircraft Simulation:** Simulate aircraft movement through different flight stages using multithreading.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Core simulation and business logic |
| Spring Boot 3.5.7 | Backend and REST APIs |
| React 19.3.1 | Frontend |
| Vite 8.3.6 | Frontend development and build |
| MySQL 8.0 | Database |
| Spring Data JPA | Database integration |
| Maven 3.10.0 | Build and dependency management |
| Axios | API communication |

## Project Structure

```text
SKYCONTROL/
├── backend/
│   ├── src/
│   └── pom.xml
├── frontend/
│   ├── src/
│   ├── public/
│   └── package.json
└── .gitignore
```

## Getting Started

### Prerequisites

- JDK 17 or compatible Java runtime
- Apache Maven
- Node.js and npm
- MySQL Server

### 1. Clone the Repository

```bash
git clone https://github.com/Mannat071625/skycontrol-atc-simulator.git
cd skycontrol-atc-simulator
```

### 2. Configure the Database

Start MySQL Server and create the database:

```sql
CREATE DATABASE atc_simulator;
```

Configure your local database credentials using environment variables.

**Windows PowerShell:**

```powershell
$env:DB_USERNAME="your_mysql_username"
$env:DB_PASSWORD="your_mysql_password"
```

Use your own MySQL username and password. If your MySQL account has no password, you can leave `DB_PASSWORD` unset.

The required tables are generated automatically by Hibernate when the backend starts.

### 3. Run the Backend

Open a terminal:

```bash
cd backend
mvn spring-boot:run
```

Backend URL: `http://localhost:8080`

### 4. Run the Frontend

Open a separate terminal:

```bash
cd frontend
npm install
npm run dev
```

Frontend URL: `http://localhost:5173`

## API Modules

| Module | Base Endpoint |
|---|---|
| Aircraft Management | `/api/aircraft` |
| Flight Management | `/api/flights` |
| Runway Management | `/api/runways` |
| Gate Management | `/api/gates` |
| Emergency Handling | `/api/emergencies` |
| Weather Monitoring | `/api/weather` |
| Flight History | `/api/history` |
| Queue Management | `/api/queues` |
| Airport Information | `/api/airport` |
| Flight Operations | `/api/operations` |

# Cricbook Backend – Local Cricket Scoring Web App

The backend service for **Cricbook**, built using **Spring Boot**. It manages leagues, teams, players, matches, scores, and ball-by-ball commentary. Provides REST APIs for the React frontend and uses MongoDB Atlas for data storage.

---

## Features

- JWT-based authentication and email verification.
- CRUD operations for leagues, teams, players, and matches.
- Real-time score updates.
- Ball-by-ball commentary.
- Role-based access for admins.

---

## Tech Stack

- **Backend:** Java, Spring Boot  
- **Database:** MongoDB Atlas  
- **Security:** Spring Security, JWT  
- **Authentication:** Email OTP verification  

---

## API Endpoints

### Authentication (`/api/auth`)
1. **POST /api/auth/signup** – Register new user and send OTP.  
2. **POST /api/auth/verify-otp** – Verify OTP for email.  
3. **POST /api/auth/login** – Login and receive JWT token.  
4. **GET /api/auth/me** – Get current authenticated user.  
5. **DELETE /api/auth** – Delete user account.

### Match & Tournament Management (`/api`)
- CRUD operations for leagues, teams, players, matches, and scores.
- Real-time score updates and ball-by-ball commentary.

---

## Installation & Setup

### Prerequisites
- Java 21
- Maven 3+
- MongoDB Atlas account or local MongoDB
- Git

### Steps
1. **Clone the repository**
```bash
git clone https://github.com/anujyadav2244/Cricbook-Local-Cricket-Scoring-Web-App.git
cd cricbook/server
````

2. **Configure application.properties**

* MongoDB URI
* JWT secret
* Mail SMTP config (if using email OTP)

3. **Run the backend**

```bash
./mvnw spring-boot:run
```

The backend will run at `http://localhost:8080`.

---

## Folder Structure

```
server/
├── src/main/java/com/cricbook/cricbook/
│   ├── config/          # Security and app configuration
│   ├── controller/      # REST API controllers
│   ├── model/           # Entities and DTOs
│   ├── repository/      # MongoDB repositories
│   └── service/         # Business logic and services
└── src/main/resources/
    ├── application.properties
    └── application-sample.properties
```

---

## Contributing

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/YourFeature`).
3. Commit your changes (`git commit -m 'Add new feature'`).
4. Push to the branch (`git push origin feature/YourFeature`).
5. Open a pull request.

---

## License

MIT License © 2025 Anuj Yadav


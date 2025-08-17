# Cricbook – Local Cricket Scoring Web App (Backend API Documentation)

Cricbook is a web application designed to help users manage local cricket tournaments efficiently. It allows admins to create leagues, add teams and players, manage matches, update scores in real-time, and provide ball-by-ball commentary.

---

## Features

### Tournament & Team Management
- Create leagues and tournaments.
- Add and manage teams.
- Add players with roles and assign batting orders.

### Match Management
- Set up matches with overs, squads, and teams.
- Real-time score updates (runs, wickets, overs, player stats).
- Ball-by-ball commentary support.

### Admin Dashboard
- View ongoing matches.
- Update scores and commentary instantly.
- Manage tournament schedules and teams.

### Security & Authentication
- JWT-based authentication.
- Email verification during signup.
- Role-based access for admins.

---

## Tech Stack

- **Backend:** Java, Spring Boot  
- **Frontend:** React, CSS  
- **Database:** MongoDB Atlas  
- **Authentication & Security:** JWT, Spring Security  
- **Hosting & Deployment:** GitHub, Local Server  

---

## API Endpoints

### Authentication (`/api/auth`)
1. **POST /api/auth/register** – Register new user with email verification.
2. **POST /api/auth/verify-otp** – Verify OTP sent to email.
3. **POST /api/auth/login** – Login and receive JWT token.
4. **GET /api/auth/me** – Fetch current logged-in user.
5. **DELETE /api/auth** – Delete user account.

### Match & Tournament Management (`/api`)
- CRUD operations for leagues, teams, players, matches, and scores.
- Real-time score updates and commentary.

---

## Installation & Setup

### Prerequisites
- Java 21
- Node.js & npm
- MongoDB Atlas account or local MongoDB
- Git

### Steps
1. **Clone the repository**
```bash
git clone https://github.com/anujyadav2244/Cricbook-Local-Cricket-Scoring-Web-App.git
cd cricbook
````

2. **Setup Backend**

```bash
cd server
# Configure application.properties with MongoDB URI and JWT secret
./mvnw spring-boot:run
```

3. **Setup Frontend**

```bash
cd ../client
npm install
npm start
```

4. Open your browser at `http://localhost:3000` to access the app.

---

## Folder Structure

```
cricbook/
├── server/                  # Spring Boot backend
│   ├── src/main/java/com/cricbook/cricbook/
│   │   ├── config/          # Security & app configuration
│   │   ├── controller/      # REST API controllers
│   │   ├── model/           # Entities & DTOs
│   │   ├── repository/      # MongoDB repositories
│   │   └── service/         # Business logic & services
│   └── src/main/resources/
│       ├── application.properties
│       └── application-sample.properties
├── client/                  # React frontend
│   ├── src/
│   ├── public/
│   └── package.json
└── README.md
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

---

## About

Cricbook is a project aimed at helping local cricket tournament organizers simplify match management and provide real-time scoring updates. It is built with Java Spring Boot for the backend and React for the frontend.


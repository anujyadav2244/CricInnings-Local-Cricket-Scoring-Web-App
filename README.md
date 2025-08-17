# Cricbook – Local Cricket Scoring Backend

Cricbook is the backend service for a local cricket tournament scoring web app. It provides APIs to manage leagues, teams, players, matches, scores, and real-time commentary.

---

## 🚧 Status
Backend is under active development. Core features like authentication, match management, and score updates are implemented.  

---

## 📌 MVP Scope
- User signup/login with JWT-based authentication
- Email verification via OTP
- CRUD operations for leagues, teams, players, and matches
- Real-time score updates
- Ball-by-ball commentary
- Admin controls for managing tournaments, teams, and players

---

## 🧰 Tech Stack
- **Backend:** Java, Spring Boot  
- **Database:** MongoDB Atlas  
- **Security:** Spring Security, JWT  
- **Email Verification:** SMTP-based OTP  

---

## 📂 Setup Instructions

### Clone the repository
```bash
git clone https://github.com/anujyadav2244/Cricbook-Local-Cricket-Scoring-Web-App.git
cd cricbook/server
````

### Configure environment

* Update `src/main/resources/application.properties` with:

  * MongoDB URI
  * JWT secret
  * SMTP mail config (for OTP)

### Run the backend

```bash
./mvnw spring-boot:run
```

Backend will be available at `http://localhost:8080`.

---

## 📄 License

This project is licensed under the MIT License. You are free to use, modify, and distribute this software with proper attribution.

---

## About

Cricbook backend powers a local cricket scoring platform, enabling admins to manage leagues, matches, teams, and provide real-time scores and commentary for local tournaments.

```

---

If you want, I can also make a **super short version** specifically optimized for GitHub **homepage preview**, which highlights the project and tech stack at a glance.  

Do you want me to do that?
```

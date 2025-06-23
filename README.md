# 🛠️ Job Scraper Application

This is a Spring Boot application that scrapes job listings from [jobs.techstars.com](https://jobs.techstars.com) based on a selected job function. It allows users to fetch and store job data into a SQL database, filter by location, and sort by posting date.

## ✅ Features

- Scrape jobs by specific job function
- Parse and store job details: title, company, description, tags, location, date
- Filter results by location
- Sort results by posting date (ascending/descending)
- Expose a simple REST API for external usage

## 🔧 Tech Stack

- Java 17
- Spring Boot
- Selenium + Jsoup for web scraping
- PostgreSQL
- Hibernate (JPA)
- Maven

## 📦 REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET`  | `/api/jobs/functions` | Returns a list of available job functions |
| `POST` | `/api/jobs/scrape?function=Software%20Engineering` | Triggers scraping jobs by a specific function |
| `GET`  | `/api/jobs?location=Remote&sortOrder=asc` | Returns jobs filtered by location and sorted by posted date |

## 📂 Project Structure
├── src/
│ ├── controller/
│ ├── model/
│ ├── repository/
│ ├── service/
│ └── ...
├── application.properties
├── README.md
├── INSTALL.md
└── dump.sql

## 🛠️ Setup

See [INSTALL.md](INSTALL.md) for instructions on building and running the application.

## 🗃️ SQL Dump

The SQL dump file containing all "Software Engineering" jobs is available in `dump.sql`.

You can also download it from Google Drive:  
📎 [Download link here]

## 🧼 Code Practices

- Clean Code and OOP principles
- Proper separation of concerns (Service, Controller, Repository layers)
- Error handling for scraping process

---

## 🌟 Extras (Optional)

- [ ] Docker support
- [ ] Multithreaded scraping
- [ ] Upload results to Google Sheets via Google Sheets API  

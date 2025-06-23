# INSTALL.md

## System Requirements

To run the project, you need:

* Java 17 or higher
* Maven 3.8 or higher
* PostgreSQL (locally installed or via Docker)
* Google Chrome (for Selenium)
* WebDriver (automatically managed by WebDriverManager)
* Docker (optional)

## Database Setup

### Option 1: Local PostgreSQL

1. Create a database named `jobsdb` (e.g., using psql):

```sql
CREATE DATABASE jobsdb;
````

2. Edit the file `src/main/resources/application.yml` and add:

```application.yml
    spring:
      datasource:
        url: jdbc:postgresql://localhost:5432/jobsdb
        username: ${DB_USERNAME}
        password: ${DB_PASSWORD}
      jpa:
        hibernate:
          ddl-auto: update
        show-sql: true
        properties:
          hibernate:
            format_sql: true
      sql:
        init:
          mode: always
```

3. Create environment variables or set your username and password directly, for example:

```bash
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
```

> Replace `your_password` with your actual PostgreSQL password.

### Option 2: PostgreSQL via Docker

If you don't have PostgreSQL installed locally, you can run it in Docker:

```bash
docker run --name jobsdb -e POSTGRES_PASSWORD=your_password -e POSTGRES_DB=jobsdb -p 5432:5432 -d postgres
```

After running, the database will be accessible at `localhost:5432`.

---

## Running the Application

Open a terminal in the project root and run:

```bash
mvn spring-boot:run
```

The server will start at [http://localhost:8080](http://localhost:8080).

---

## Starting Job Scraping

To start scraping jobs by a specific function, send a POST request:

```
POST http://localhost:8080/api/jobs/scrape?function=Software%20Engineering
```

---

## Getting List of Available Job Functions

```
GET http://localhost:8080/api/jobs/functions
```

---

## Getting Jobs with Filtering and Sorting

```
GET http://localhost:8080/api/jobs?location=USA&sortOrder=asc
```

* `location` — optional location filter
* `sortOrder` — `asc` or `desc` to sort by posting date

---

## Exporting Database Dump

To export the database to a SQL file, run:

```bash
pg_dump -U postgres -d jobsdb -f job_dump.sql
```

> Replace `postgres` and `jobsdb` with your own credentials if necessary.

---

## Building the JAR File (Optional)

To build and run the standalone JAR:

```bash
mvn clean package
java -jar target/jobscraper-0.0.1-SNAPSHOT.jar
```

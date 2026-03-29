# Chatapp

![vert.x](https://img.shields.io/badge/vert.x-4.5.25-purple.svg)

This application was generated using http://start.vertx.io

## Database setup

1. Install PostgreSQL and then run in terminal:

```bash
psql postgres
```

2. Then create a user and a database:

```sql
CREATE ROLE "user" WITH LOGIN PASSWORD 'secret';
CREATE DATABASE "the-db" OWNER "user";
```

3. Type `\q` to leave and then run in terminal:

```bash
psql -d the-db -U user
```

## Alternative: Database setup with Docker

Instead of installing PostgreSQL locally, you can use Docker.

### 1. Build and run the container

```bash
docker build -t my-postgres .
docker run -d -p 5432:5432 --name my-postgres my-postgres
```

### 2. Inspect the database with pgAdmin

Run pgAdmin as a Docker container:

```bash
docker run -d -p 8080:80 -e PGADMIN_DEFAULT_EMAIL=admin@admin.com -e PGADMIN_DEFAULT_PASSWORD=admin dpage/pgadmin4
```

Then open http://localhost:8080 in your browser and create a new PostgreSQL connection with:

- **Host:** `localhost`
- **Port:** `5432`
- **Database:** `db`
- **Username:** `user`
- **Password:** `secret`

## Building

To launch your tests:
```bash
./mvnw clean test
```

To package your application:
```bash
./mvnw clean package
```

To run your application:
```bash
./mvnw clean compile exec:java
```

## Help

- [Vert.x Documentation](https://vertx.io/docs/)
- [Vert.x Stack Overflow](https://stackoverflow.com/questions/tagged/vert.x?sort=newest&pageSize=15)
- [Vert.x User Group](https://groups.google.com/forum/?fromgroups#!forum/vertx)
- [Vert.x Discord](https://discord.gg/6ry7aqPWXy)

# Auth & Profile Service

Service untuk Authentication dan Profile management pengguna platform JSON (JaStip Online Nasional).

## Tech Stack
- Java 21 + Spring Boot
- Spring Security + BCrypt
- MySQL
- Gradle

## Cara Menjalankan

### Prasyarat
- Java 21
- MySQL

### Setup Database
```sql
CREATE DATABASE auth_profile;
```

### Jalankan Aplikasi
```bash
./gradlew bootRun
```

Aplikasi berjalan di `http://localhost:8080`

## Cara Mencoba

### Opsi 1: Frontend (Lebih Mudah)
Buka browser dan akses `http://localhost:8080` — sudah tersedia halaman login dan register sederhana.

### Opsi 2: API Langsung

**Register**
- `POST /auth/register`
```json
{
  "email": "user@example.com",
  "password": "password123",
  "username": "username"
}
```

**Login**
- `POST /auth/login`
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
# Auth/Profile Service

Authentication and profile service for Milestone `25%` and `50%`.

## Deployed URL

- `https://auth-profile-api-383620816191.us-central1.run.app`

## Implemented Scope

- `POST /auth/register`
- `POST /auth/login`
- `GET /auth/me`
- `GET /profile/{id}`
- `PUT /profile`

JWTs issued here are used directly by the Inventory, Wallet, and Order services.

## Local Run

Prerequisites:
- Java `21`

Run:

```bash
./gradlew bootRun
```

PowerShell:

```powershell
.\gradlew.bat bootRun
```

Default local URL:
- `http://localhost:8080`

## Environment Variables

- `PORT`
- `DB_URL`
- `DB_DRIVER`
- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_CORS_ALLOWED_ORIGINS`
- `JWT_SECRET`
- `JWT_EXPIRATION_SECONDS`

Defaults are configured for an H2 file database under `/tmp`, which is enough for the milestone demo.

## Test

```bash
./gradlew test
```

Includes:
- auth flow integration test for register -> login -> `/auth/me`

## Cloud Run Deploy

```bash
gcloud run deploy auth-profile-api --source . --region us-central1 --allow-unauthenticated --max-instances=1 \
  --set-env-vars APP_CORS_ALLOWED_ORIGINS=https://advprog-frontend-m25-m50-383620816191.us-central1.run.app \
  --set-env-vars JWT_SECRET=<shared-jwt-secret>
```

## Notes

- The service is intentionally limited to the milestone auth/profile flow.
- Data is stored in a service-local H2 file for demo purposes.

# Auth/Profile Service

Authentication and profile service for JSON Milestone `75%`.

## Scope

- register
- login
- current-session lookup
- profile lookup and update
- role-bearing JWTs for buyer, jastiper, and admin flows
- optional demo account seeding for local or deployed demo environments

## Demo Accounts

Demo seeding is now explicit.

Set `APP_DEMO_ACCOUNTS_ENABLED=true` only for local/demo deployments that need the seeded buyer, jastiper, and admin users:

- `demo@json.app`
- `jastiper1@json.app`
- `jastiper2@json.app`
- `jastiper3@json.app`
- `admin@json.app`

Password for those demo accounts: `Demo123!`

Default behavior is `false` so production-like environments do not silently seed public demo users.

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
- `APP_DEMO_ACCOUNTS_ENABLED`

Defaults are configured for an H2 file database under `/tmp`.

## Test

```bash
./gradlew test
```

Coverage includes:

- register -> login -> `/auth/me`
- seeded demo account role and login checks

## Deployment

Target platform: Google Cloud Run.

Basic deploy:

```bash
gcloud run deploy auth-profile-api --source . --region us-central1 --allow-unauthenticated --max-instances=1 \
  --update-env-vars APP_DEMO_ACCOUNTS_ENABLED=true
```

The demo deployment should keep the existing shared `JWT_SECRET` and CORS settings already configured in Cloud Run.

## Risks

- Enabling demo seeds on a public deployment exposes known demo credentials by design.
- Disabling demo seeds means the frontend admin and jastiper views will need alternative accounts.

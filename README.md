# CineBook (Frontend + Java backend + Supabase Postgres)

Browser (index.html) -> Spring Boot REST API (Java) -> Supabase Postgres

## Setup
1. Supabase: SQL Editor -> paste `schema.sql` -> Run.
2. Supabase: Connect -> Session pooler -> copy host, user (postgres.<ref>), password.
3. Set env vars, then run:

   export SUPABASE_DB_URL="jdbc:postgresql://<pooler-host>:5432/postgres"
   export SUPABASE_DB_USER="postgres.<project-ref>"
   export SUPABASE_DB_PASSWORD="<db password>"
   cd backend && mvn spring-boot:run

4. Open http://localhost:8080

## API
GET  /api/shows
GET  /api/bookings
POST /api/bookings   {showId, seats[], coupon, payment, paymentConfirmed}
POST /api/bookings/{id}/cancel

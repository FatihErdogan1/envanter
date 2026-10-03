# inventory.io — Desktop Inventory & Asset Management (Java Swing)

A Java Swing desktop application for tracking product stock across multiple warehouses and managing company fixed assets (assignment, transfer, maintenance), backed by MySQL over plain JDBC.
This is the desktop edition of the *envanter* project; the later web edition is split into [envanter-api](https://github.com/FatihErdogan1/envanter-api) (Spring Boot) and [envanter-ui](https://github.com/FatihErdogan1/envanter-ui) (React).

## Features

- **Authentication** — login and registration with SHA-256 password hashing and an in-memory user session
- **Role-based navigation** — `ADMIN`, `MANAGER`, `STAFF`; staff only see the dashboard, products, stock movements and warehouse stock, managers see everything except user management
- **Dashboard** — total products, low-stock products and user counts, with shortcuts to the related screens
- **Master data** — create / update / delete products, categories, suppliers, warehouses and users
- **Stock movements** — `IN`, `OUT` and `TRANSFER` (between warehouses) transactions with a per-warehouse stock report
- **Fixed-asset lifecycle** — add, update and retire assets; assign to / return from a user; transfer between warehouses; start and finish maintenance; view assignment, transfer and maintenance history
- **Retro pixel theme** — custom dark theme on top of FlatLaf; uses Press Start 2P / VT323 fonts from `src/main/resources/fonts/` when present and falls back to a monospaced font otherwise
- **Tests** — 119 JUnit 5 + Mockito unit tests for the service and controller layers (DAOs are mocked, so no database is needed)
- **Design docs** — PlantUML use-case and class diagrams in `docs/diagrams/`

## Architecture

Layered MVC: Swing **views** → **controllers** → **services** (business rules, validation) → **DAOs** (generic `IGenericDao` + JDBC implementations) → MySQL, with a singleton `DatabaseConnection`.

```
src/main/java/org/example/
├── Main.java        # entry point: applies the theme, opens the login window
├── view/            # frames (Login, Register, Main) and management panels
├── controller/      # UI-facing controllers
├── service/         # business logic
├── dao/             # IGenericDao + JDBC DAO implementations
├── model/           # entities and enums (Role, AssetStatus, TransactionType)
└── util/            # DB connection, theme, password hashing, session
src/test/java/...    # service and controller tests
docs/diagrams/       # PlantUML diagrams
```

## Tech Stack

Java 17 · Swing + FlatLaf 3.5 · MySQL (Connector/J 8.3, JDBC) · Maven · JUnit 5 · Mockito 5

## Running

Prerequisites: JDK 17+, Maven, and a local MySQL server.

1. Create a MySQL database named `envanter` with the tables used by the DAOs (`Users`, `Products`, `Categories`, `Suppliers`, `Warehouses`, `Inventory_Transactions`, `Assets`, `Asset_Assignments`, `Asset_Transfers`, `Asset_Maintenance`). A schema script is not included in this repository.
2. Configure the database connection (credentials are not stored in the code). `DatabaseConnection` reads, in this order:
   - environment variables `DB_URL`, `DB_USER`, `DB_PASSWORD`;
   - otherwise a `db.properties` file in the working directory (or on the classpath) with the keys `db.url`, `db.user`, `db.password` — copy `db.properties.example` to `db.properties` and fill it in (it is git-ignored);
   - otherwise the defaults `jdbc:mysql://localhost:3306/envanter?useSSL=false&serverTimezone=UTC`, user `root` and an empty password.
3. Build and start the app:

   ```bash
   mvn compile exec:java
   ```

Run the tests with (on JDK 17 — the pinned Mockito version cannot mock classes on much newer JDKs such as 25):

```bash
mvn test
```

---

**Author:** Fatih Erdoğan

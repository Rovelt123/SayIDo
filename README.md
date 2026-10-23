# Say I Do

Say I Do is a wedding planner. A user creates a wedding, gets a board of categories, adds tasks with prices and deadlines, and follows the budget and the time left until the day.

Fourth semester systems development project at EK.

## Structure

backend is the API, written in Java with Javalin and Hibernate.

frontend is the React client.

documents holds the test documentation: test strategy, test case design and testability.

## Built with

Java 21, Javalin, Hibernate, PostgreSQL in production and H2 in tests, JWT for authentication, bcrypt for passwords, JUnit 5 and JaCoCo for tests, Maven for the build.

## Running the backend

Configuration is read from backend/src/main/resources/config.properties, so that file has to be filled in before the API will start. The entry point is app.Main, and mvn clean package produces a shaded jar in backend/target that can be run on its own.

## Tests

Run everything from the backend folder with mvn clean verify. Surefire runs the unit tests, Failsafe runs the integration tests against an in memory H2 database, and the JaCoCo check fails the build if line coverage drops below the minimum we have set.

## Branches

Work is done on a branch and merged through a pull request to Review. Nothing goes straight to Main. Pull requests against Review, Development and Main run the build, so a failing test blocks the merge.

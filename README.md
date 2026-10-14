# STARTER-team01

Instructions: <https://ucsb-cs156.github.io/s26/lab/team01.html>

TODO: change heading above to your repo name, e.g. `# team01-s26-17`

TODO: Add a link to the deployed Dokku app for your team here, e.g.

Deployments:

* Prod: <https://team01-dev-vcnardi524.dokku-00.cs.ucsb.edu>
* QA: <https://team01-qa.dokku-17.cs.ucsb.edu>

TODO: Fill in this table with correct information. 

| Table                     | Name         | Github Id |
|---------------------------|--------------|-----------|
| UCSBDiningCommonsMenuItem |              |           |
| UCSBOrganization          |              |           |
| RecommendationRequest     |              |           |
| MenuItemReview            |              |           |
| HelpRequest               |              |           |
| Articles                  |              |           |

Remember though, that in spite of these initial  assignments, it is still
a team project.  Please help other team members to finish their work
after completing your own.

# Versions
* Java: 21
* node: 20.17.0
See [docs/versions.md](docs/versions.md) for more information on upgrading versions.

# Brief overview of starter code 

TODO: remove this header and content of this section before submitting.
However leave the section `# Overview of application` and its content 
intact.

The starter code here starts with a base similar to `team01`, but with 
some extra frontend code on top of the of backend CRUD operations
that were present in `team01`.

You can use this code as a basis to:
* Add the backend code from team01 *in stages* as suggested in the issues (doing that in "one giant pull request" is *not recommended) 
* Add a frontend on top of the backend CRUD features you added in team01, using the existing
  code as examples.

# Overview of application

When complete, this application will have the following features:

* An endpoint to POST each entity type to the database
* An endpoint to GET each entity type from the database
* An endpoint to PUT each entity type in the database
* An endpoint to DELETE each entity type from the database
* An endpoint to GET a list of all entity types from the database
# Setup before running application

Before running the application for the first time,
you need to do the steps documented in [`docs/oauth.md`](docs/oauth.md).

Otherwise, when you try to login for the first time, you 
will likely see an error such as:

<img src="https://user-images.githubusercontent.com/1119017/149858436-c9baa238-a4f7-4c52-b995-0ed8bee97487.png" alt="Authorization Error; Error 401: invalid_client; The OAuth client was not found." width="400"/>

# Getting Started on localhost
* Start up the backend with:
  ``` 
  mvn spring-boot:run
  ```

Then, the app should be available on <http://localhost:8080>

If it doesn't work at first, e.g. you have a blank page on  <http://localhost:8080>, give it a minute and a few page refreshes.  Sometimes it takes a moment for everything to settle in.

If you see the following on localhost, make sure that you also have the frontend code running in a separate window.


# Getting Started on Dokku

See: [/docs/dokku.md](/docs/dokku.md)

# Accessing swagger

To access the swagger API endpoints, use:

* <http://localhost:8080/swagger-ui/index.html>

Or add `/swagger-ui/index.html` to the URL of your dokku deployment.

# SQL Database access

On localhost:
* The SQL database is an H2 database and the data is stored in a file under `target`
* Each time you do `mvn clean` the database is completely rebuilt from scratch
* You can access the database console via a special route, <http://localhost:8080/h2-console>
* For more info, see [docs/h2-database.md](/docs/h2-database.md)

On Dokku, follow instructions for Dokku databases:
* <https://ucsb-cs156.github.io/topics/dokku/postgres_database.html>

# Testing

## Unit Tests

* To run all unit tests, use: `mvn test`
* To run only the tests from `FooTests.java` use: `mvn test -Dtest=FooTests`

Unit tests are any methods labelled with the `@Test` annotation that are under the `/src/test/java` hierarchy, and have file names that end in `Test` or `Tests`

## Integration Tests

To run only the integration tests, use:
```
INTEGRATION=true mvn test-compile failsafe:integration-test
```

To run only the integration tests *and* see the tests run as you run them,
use:

```
INTEGRATION=true HEADLESS=false mvn test-compile failsafe:integration-test
```

To run a particular integration test (e.g. only `HomePageWebIT.java`) use `-Dit.test=ClassName`, for example:

```
INTEGRATION=true mvn test-compile failsafe:integration-test -Dit.test=HomePageWebIT
```

or to see it run live:
```
INTEGRATION=true HEADLESS=false mvn test-compile failsafe:integration-test -Dit.test=HomePageWebIT
```

Integration tests are any methods labelled with `@Test` annotation, that are under the `/src/test/java` hierarchy, and have names starting with `IT` (specifically capital I, capital T).

By convention, we are putting Integration tests (the ones that run with Playwright) under the package `src/test/java/edu/ucsb/cs156/example/web`.

Unless you want a particular integration test to *also* be run when you type `mvn test`, do *not* use the suffixes `Test` or `Tests` for the filename.

Note that while `mvn test` is typically sufficient to run tests, we have found that if you haven't compiled the test code yet, running `mvn failsafe:integration-test` may not actually run any of the tests.


## Partial pitest runs

This repo has support for partial pitest runs

For example, to run pitest on just one class, use:

```
mvn pitest:mutationCoverage -DtargetClasses=edu.ucsb.cs156.example.controllers.RestaurantsController
```

To run pitest on just one package, use:

```
mvn pitest:mutationCoverage -DtargetClasses=edu.ucsb.cs156.example.controllers.\*
```

To run full mutation test coverage, as usual, use:

```
mvn pitest:mutationCoverage
```

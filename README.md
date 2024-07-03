# Notes API

The Notes API is designed as a RESTful service that enables users to manage their daily notes. 
It provides essential CRUD operations and includes an endpoint for obtaining word frequency statistics based on the text of each note.

## Table of Contents

- [Overview](#overview)
- [Technologies](#technologies)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Building the Project](#building-the-project)
    - [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Docker Instructions](#docker-instructions)
- [Continuous Integration](#continuous-integration)
- [Contact](#contact)

## Overview

This project is a simple Notes API built with Spring Boot and Gradle. It allows users to manage notes and retrieve statistics on the word frequency in each note's text.

## Technologies

- Java 17 
- SpringBoot 3
- MongoDB
- Gradle
- Docker
- MapStruct (Used for object mapping, for more info please refer https://mapstruct.org/)
- TestContainer (Used for integration test, please refer https://java.testcontainers.org/)

## Getting Started

### Prerequisites

Before running this application, ensure you have the following installed:

- Java 17
- Gradle
- Docker (for containerization)
- MongoDB Instance

Make sure MongoDB is installed and running locally as docker container or service.

This setup ensures all necessary dependencies are in place to build, test, and run the application seamlessly.

### Building the Project

Clone the repository:

```bash
git clone https://github.com/yourusername/notes-api.git
cd notes-api
````

To build the project, run the following command:
```bash
./gradlew clean build
```

The above build command will compile the project, run tests, and package the application into a JAR file located in build/libs.

Note: test task will execute the integration tests by default, which require Docker to be running.

### Running the Application

To run the application locally, use the following command:

Using bootRun Gradle task to run the application directly from the source code:

```bash
./gradlew bootRun
```

Alternatively, you can use the packaged JAR file as follows:

```bash
java -jar build/libs/notes-api-<version>.jar
```

Replace <version> with the actual version number of your application JAR file.

## API Endpoints

The following endpoints are available in the Notes API:

- POST /notes: Create a new note.
- GET /notes: Fetch a list of note summaries.
- GET /notes/{id}: Get a note by id
- PUT /notes/{id} : Update a note using id
- DELETE /notes/{id}: Delete a note using id
- GET /notes/{id}/stats: Get note text stats by word occurrence

### Example Requests

POST /notes

Create a new note with a specified title, text, and tag. Supported tag value are BUSINESS, PERSONAL and IMPORTANT.

```bash
curl -X POST \
     -H "Content-Type: application/json" \
     -d '{"title":"Example Note","text":"note is just a note.","tag":"PERSONAL"}' \
     http://localhost:8080/notes

```

GET /notes/{id}/stats

Get text statistics for a given note.

```bash
curl -X GET http://localhost:8080/notes/7369602a-a297-40cb-9b85-ddf4b932c5af/stats
```
Replace with your note id

## Docker Instructions

### Building the Docker Image
Ensure you installed docker in your local machine

```bash
docker build -t notes-api:latest .
```

The above docker command will generate new docker image with the latest tag. 

### Running the Docker Container

You can use above generated docker image to spin a container.
```bash
docker run -p 8080:8080 notes-api:latest
```

## Continuous Integration

This project utilizes GitHub Actions() for automating continuous integration. 
The defined workflow in .github/workflows/ci.yml ensures that specific jobs 
are executed on every commit and pull request made to the main or master branches.

### Workflow Overview

The workflow includes two main jobs:
- Build and Test
  - This job checks out the repository, sets up JDK 17, and builds the project using Gradle. 
- Dockerize 
  - This job runs after the build_and_test job is completed successfully. It handles the containerization of the application into a Docker image

Code sinppet from ci.yml file
```yaml
jobs:
  build_and_test:
    runs-on: ubuntu-latest
    name: Build and Test

    steps:
      ...
      - name: Build with Gradle
        run: ./gradlew clean build --no-daemon
      ...
  dockerize:
    runs-on: ubuntu-latest
    needs: build_and_test
    name: Dockerize

    steps:
      ...
      - name: Build Docker Image
        run: docker build -t notes-api:latest .
```

After setting up Docker Build, this job builds a Docker image named notes-api:latest from the compiled artifacts of the project. 

### Integration with Deployment
The Docker image built in the dockerize job can be further integrated with continuous deployment pipelines, ensuring that updates to the application are seamlessly propagated across environments.

For more information refer [GitHub Actions](https://docs.github.com/en/actions/automating-builds-and-tests/building-and-testing-java-with-gradle)

## Contact

For any questions or issues, please reach out to Notes API Service team.




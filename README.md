[![Codacy Badge](https://api.codacy.com/project/badge/Grade/68211df3f35e44fbb3fe01da5151a8b9)](https://www.codacy.com/app/sdcplatform/rm-collection-exercise-service?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ONSdigital/rm-collection-exercise-service&amp;utm_campaign=Badge_Grade) [![Docker Pulls](https://img.shields.io/docker/pulls/sdcplatform/collectionexercisesvc.svg)]()
[![Build Status](https://travis-ci.org/ONSdigital/rm-collection-exercise-service.svg?branch=main)](https://travis-ci.org/ONSdigital/rm-collection-exercise-service)
[![codecov](https://codecov.io/gh/ONSdigital/rm-collection-exercise-service/branch/main/graph/badge.svg)](https://codecov.io/gh/ONSdigital/rm-collection-exercise-service)

# Collection Exercise Service
The Collection Exercise service will be responsible for the orchestration of the processes necessary to begin the data collection for a particular field period for a particular survey.

## Running

There are two ways of running this service

* The easiest way is via docker (https://github.com/ONSdigital/ras-rm-docker-dev)
* Alternatively running the service up in isolation
    ```bash
    cp .maven.settings.xml ~/.m2/settings.xml  # This only needs to be done once to set up mavens settings file
    mvn clean install
    mvn spring-boot:run
    ```

# API
See [API.md](https://github.com/ONSdigital/rm-collection-exercise-service/blob/main/API.md) for API documentation.

# Swagger Specification
To view the swagger Specification for the Collection Exercise service, run the service and navigate to http://localhost:8145/swagger-ui.html.

# Code Styler
To use the code styler please goto this url (https://github.com/google/google-java-format) and follow the Intellij instructions or Eclipse depending on what you use

## Copyright
Copyright (C) 2017 Crown Copyright (Office for National Statistics)

# Collection Exercise Service API
This page documents the Collection Exercise service API endpoints. Apart from the Service Information endpoint, all these endpoints are secured using HTTP basic authentication. All endpoints return an `HTTP 200 OK` status code except where noted otherwise.

## Service Information
* `GET /info` will return information about this service, collated from when it was last built.

### Example JSON Response
```json
{
  "name": "collectionexercisesvc",
  "version": "10.42.0",
  "origin": "git@github.com:ONSdigital/rm-collection-exercise-service.git",
  "commit": "045d26a8bf0663d8e255d3efe9892f65b0cbb9ce",
  "branch": "master",
  "built": "2017-07-12T13:40:41Z"
}
```

## Get Collection Exercises for Survey
* `GET /collectionexercises/survey/{survey_id}` will return a list of known collection exercises for the survey id.

### Example JSON Response
```json
[
  {
    "id": "c6467711-21eb-4e78-804c-1db8392f93fb",
    "surveyId": "cb8accda-6118-4d3b-85a3-149e28960c54",
    "name": "Monthly Survey of Building Materials Bricks",
    "actualExecutionDateTime": null,
    "scheduledExecutionDateTime": null,
    "scheduledStartDateTime": null,
    "actualPublishDateTime": null,
    "periodStartDateTime": null,
    "periodEndDateTime": null,
    "scheduledReturnDateTime": null,
    "scheduledEndDateTime": null,
    "executedBy": null,
    "state": "INIT",
    "caseTypes": null,
    "exerciseRef": "201801",
    "userDescription": "January 2018",
    "created": "2018-01-09T12:56:09.652Z",
    "updated": null,
    "deleted": null,
    "validationErrors": []
  },
  {
    "id": "b447e134-5e5d-46fb-b4fc-15efdcbe5ca7",
    "surveyId": "cb8accda-6118-4d3b-85a3-149e28960c54",
    "name": "Monthly Survey of Building materials Bricks",
    "actualExecutionDateTime": null,
    "scheduledExecutionDateTime": null,
    "scheduledStartDateTime": null,
    "actualPublishDateTime": null,
    "periodStartDateTime": null,
    "periodEndDateTime": null,
    "scheduledReturnDateTime": null,
    "scheduledEndDateTime": null,
    "executedBy": null,
    "state": "INIT",
    "caseTypes": null,
    "exerciseRef": "201802",
    "userDescription": "February 2018",
    "created": "2018-01-09T12:56:09.709Z",
    "updated": null,
    "deleted": null,
    "validationErrors": []
  }
]
```



## Get Collection Exercises for Survey and Exercise
* `GET /collectionexercises/{exerciseRef}/survey/{survey_id}` will return a list of known collection exercises for the survey id and exerciseRef.

### Example JSON Response
```json
[
  {
    "id": "b447e134-5e5d-46fb-b4fc-15efdcbe5ca7",
    "surveyId": "cb8accda-6118-4d3b-85a3-149e28960c54",
    "name": "Monthly Survey of Building materials Bricks",
    "actualExecutionDateTime": null,
    "scheduledExecutionDateTime": null,
    "scheduledStartDateTime": null,
    "actualPublishDateTime": null,
    "periodStartDateTime": null,
    "periodEndDateTime": null,
    "scheduledReturnDateTime": null,
    "scheduledEndDateTime": null,
    "executedBy": null,
    "state": "INIT",
    "caseTypes": null,
    "exerciseRef": "201802",
    "userDescription": "February 2018",
    "created": "2018-01-09T12:56:09.709Z",
    "updated": null,
    "deleted": null,
    "validationErrors": []
  },
]
```



An `HTTP 404 Not Found` status code is returned if the survey with the specified ID could not be found. An `HTTP 204 No Content` status code is returned if there are no known collection exercises for the specified survey.

## List Collection Exercises
* `GET /collectionexercises` will return a list of all collection exercises.

### Example JSON Response
```json
[
  {
        "id": "14fb3e68-4dca-46db-bf49-04b84e07e77c",
        "surveyId": "cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87",
        "name": "BRES_2017",
        "actualExecutionDateTime": "2017-11-06T16:06:19.966Z",
        "scheduledExecutionDateTime": "2017-09-10T23:00:00.000Z",
        "scheduledStartDateTime": "2017-09-11T23:00:00.000Z",
        "actualPublishDateTime": "2017-11-06T16:08:26.100Z",
        "periodStartDateTime": "2017-09-14T23:00:00.000Z",
        "periodEndDateTime": "2017-09-15T22:59:59.000Z",
        "scheduledReturnDateTime": "2017-10-06T00:00:00.000Z",
        "scheduledEndDateTime": "2018-06-29T23:00:00.000Z",
        "executedBy": null,
        "state": "PUBLISHED",
        "caseTypes": [
            {
                "actionPlanId": "e71002ac-3575-47eb-b87f-cd9db92bf9a7",
                "sampleUnitType": "B"
            },
            {
                "actionPlanId": "0009e978-0932-463b-a2a1-b45cb3ffcb2a",
                "sampleUnitType": "BI"
            }
        ],
        "exerciseRef": "221_201712",
        "userDescription": null,
        "created": null,
        "updated": null,
        "deleted": false,
        "validationErrors": []
    },
    {
        "id": "88e18a80-bc77-48bf-8eff-db351024be2b",
        "surveyId": "75b19ea0-69a4-4c58-8d7f-4458c8f43f5c",
        "name": "Monthly Business Sur",
        "actualExecutionDateTime": null,
        "scheduledExecutionDateTime": null,
        "scheduledStartDateTime": null,
        "actualPublishDateTime": null,
        "periodStartDateTime": null,
        "periodEndDateTime": null,
        "scheduledReturnDateTime": null,
        "scheduledEndDateTime": null,
        "executedBy": null,
        "state": "INIT",
        "caseTypes": [],
        "exerciseRef": "1802",
        "userDescription": "February 2018",
        "created": "2018-01-09T12:56:07.262Z",
        "updated": null,
        "deleted": null,
        "validationErrors": []
    },
    {
        "id": "6af19036-f69b-4d2e-abf1-ce442debb51c",
        "surveyId": "75b19ea0-69a4-4c58-8d7f-4458c8f43f5c",
        "name": "Monthly Business Sur",
        "actualExecutionDateTime": null,
        "scheduledExecutionDateTime": null,
        "scheduledStartDateTime": null,
        "actualPublishDateTime": null,
        "periodStartDateTime": null,
        "periodEndDateTime": null,
        "scheduledReturnDateTime": null,
        "scheduledEndDateTime": null,
        "executedBy": null,
        "state": "INIT",
        "caseTypes": [],
        "exerciseRef": "1803",
        "userDescription": "March 2018",
        "created": "2018-01-09T12:56:07.485Z",
        "updated": null,
        "deleted": null,
        "validationErrors": []
    },
]
```

## Get Collection Exercise
* `GET /collectionexercises/{collection_exercise_id}` will return the details of the collection exercise with the given id.

### Example JSON Response
```json
{
    "id": "14fb3e68-4dca-46db-bf49-04b84e07e77c",
    "surveyId": "cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87",
    "name": "BRES_2017",
    "actualExecutionDateTime": "2017-11-06T16:06:19.966Z",
    "scheduledExecutionDateTime": "2017-09-10T23:00:00.000Z",
    "scheduledStartDateTime": "2017-09-11T23:00:00.000Z",
    "actualPublishDateTime": "2017-11-06T16:08:26.100Z",
    "periodStartDateTime": "2017-09-14T23:00:00.000Z",
    "periodEndDateTime": "2017-09-15T22:59:59.000Z",
    "scheduledReturnDateTime": "2017-10-06T00:00:00.000Z",
    "scheduledEndDateTime": "2018-06-29T23:00:00.000Z",
    "executedBy": null,
    "state": "PUBLISHED",
    "caseTypes": [
        {
            "actionPlanId": "e71002ac-3575-47eb-b87f-cd9db92bf9a7",
            "sampleUnitType": "B"
        },
        {
            "actionPlanId": "0009e978-0932-463b-a2a1-b45cb3ffcb2a",
            "sampleUnitType": "BI"
        }
    ],
    "exerciseRef": "221_201712",
    "userDescription": null,
    "created": null,
    "updated": null,
    "deleted": false,
    "validationErrors": []
}
```

## Validation Errors

In the event that errors have occurred validating the collection exercise, there will be an array of validation errors in the resources returned for that collection exercise.  Please note that validation errors are only shown if the collection exercise is in the FAILEDVALIDATION state.

### Example Collection Exercise Representation including validation errors
```json
{
    "id": "14fb3e68-4dca-46db-bf49-04b84e07e77c",
    "surveyId": "cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87",
    "name": "BRES_2017",
    "actualExecutionDateTime": "2017-11-06T16:06:19.966Z",
    "scheduledExecutionDateTime": "2017-09-10T23:00:00.000Z",
    "scheduledStartDateTime": "2017-09-11T23:00:00.000Z",
    "actualPublishDateTime": "2017-11-06T16:08:26.100Z",
    "periodStartDateTime": "2017-09-14T23:00:00.000Z",
    "periodEndDateTime": "2017-09-15T22:59:59.000Z",
    "scheduledReturnDateTime": "2017-10-06T00:00:00.000Z",
    "scheduledEndDateTime": "2018-06-29T23:00:00.000Z",
    "executedBy": null,
    "state": "PUBLISHED",
    "caseTypes": [
        {
            "actionPlanId": "e71002ac-3575-47eb-b87f-cd9db92bf9a7",
            "sampleUnitType": "B"
        },
        {
            "actionPlanId": "0009e978-0932-463b-a2a1-b45cb3ffcb2a",
            "sampleUnitType": "BI"
        }
    ],
    "exerciseRef": "221_201712",
    "userDescription": null,
    "created": null,
    "updated": null,
    "deleted": false,
    "validationErrors": [
        {
            "sampleUnitRef": "49900000037",
            "errors": [
                "MISSING_PARTY"
            ]
        },
        {
            "sampleUnitRef": "49900000038",
            "errors": [
                "MISSING_COLLECTION_INSTRUMENT",
                "MISSING_PARTY"
            ]
        },
        {
            "sampleUnitRef": "49900000006",
            "errors": [
                "MISSING_COLLECTION_INSTRUMENT"
            ]
        }
    ]
}
```

## Create Collection Exercise
* `POST /collectionexercises` will create a new collection exercise
* Returns 201 Created if the resource is created
* Returns 400 Bad Request
* Returns 409 Conflict

### Example JSON Request Body
```json
{
    "surveyId": "cb0711c3-0ac8-41d3-ae0e-567e5ea1ef77",
    "name": "SurveyName",
    "exerciseRef": "201715",
    "userDescription": "August 2017"
}
```

## Update Collection Exercise
* `PUT /collectionexercises/{collection_exercise_id}` will update the collection exercise with the given id.
* Returns 200 OK if the resource is updated
* Returns 400 Bad Request
* Returns 409 conflict

### Example JSON Request Body

```json
{
    "name": "UpdatedSurveyName",
    "exerciseRef": "201715",
    "userDescription": "Updated August 2017",
    "surveyId": "cb0711c3-0ac8-41d3-ae0e-567e5ea1ef77"
}
```

## Update Collection Exercise exerciseRef (period)
* `PUT /collectionexercises/{collection_exercise_id}/exerciseRef` will update the exerciseRef for collection exercise with given id.
* Returns 200 OK if the resource is updated.
* Returns 400 Bad Request, resource not updated.
* Returns 409 conflict, resource not updated.

### Example Request Body
```text/plain
201803
```

## Update Collection Exercise userDescription (user visible name)
* `PUT /collectionexercises/{collection_exercise_id}/userDescription` will update the user visible name for collection exercise with given id.
* Returns 200 OK if the resource is updated
* Returns 400 Bad Request, not updated resource.
* Returns 409 conflict, not updated resource.

### Example Request Body
```text/plain
August 2018
```

## Update Collection Exercise exerciseRef (name)
* `PUT /collectionexercises/{collection_exercise_id}/name` will update the name of collection exercise with given id.
* Returns 200 OK if the resource is updated
* Returns 400 Bad Request, resource not updated
* Returns 409 Conflict, resource not updated

### Example Request Body
```text\plain
Collex name
```

## Update Collection Exercise start
* `PUT /collectionexercises/{collection_exercise_id}/scheduledStart` will update the scheduled start of collection exercise with given id.
* Returns 200 OK if the resource is updated
* Returns 400 Bad Request, resource not updated
* Returns 409 Conflict, resource not updated


## Update Collection Exercise survey
* `PUT /collectionexercises/{collection_exercise_id}/surveyId` will update the survey for collection exercise with given id.
* Returns 200 OK if the resource is updated
* Returns 400 Bad Request, resource not updated
* Returns 409 Conflict, resource not updated

### Example Request Body
```
cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87
```

## Delete Collection Exercise
* `DELETE /collectionexercises/{collection_exercise_id}` will mark the collection exercise with given id for deletion.  This operation is included for future use and currently serves no purpose (also included for completeness).
* Returns collection exercise to be deleted.


## Execute Collection Exercise
* `POST /collectionexerciseexecution/{collection_exercise_id}` will execute the collection exercise with the given id.

### Example JSON Response
```json
{
  "sampleUnitsTotal": "670"
}
```

An `HTTP 404 Not Found` status code is returned if the collection exercise with the specified ID could not be found.

## Link Sample Summary To Collection Exercise
* `PUT /collectionexercises/link/{collection_exercise_id}` will link the Sample Summaries specified in the json request to the collection exercise with the given id.

The endpoint will also delete any entries currently in the samplelink table for the specified collection exercise before linking to the sample summary IDs in the json request.

### Example JSON Request
```json
{
  "sampleSummaryIds": [
    "8fa33fd9-486a-41ca-baf2-2a833cfa031c",
    "08c191b8-e8b8-4920-b8de-87f85e536463"
  ]
}
```

### Example JSON Response
```json
{
  "collectionExerciseId": "c6467711-21eb-4e78-804c-1db8392f93fb",
  "sampleSummaryIds": [
    "8fa33fd9-486a-41ca-baf2-2a833cfa031c",
    "08c191b8-e8b8-4920-b8de-87f85e536463"
  ]
}
```

An `HTTP 404 Not Found` status code is returned if the collection exercise with the specified ID could not be found.

## Unlink Sample Summary To Collection Exercise
* `PUT /collectionexercises/unlink/{collection_exercise_id}/sample/{sample_summary_id}` will unlink the Sample Summary specified from the collection exercise with the given id.
* Returns 200 OK if successfully unlinked

An `HTTP 404 Not Found` status code is returned if the collection exercise with the specified ID could not be found.

## Get Sample Summaries Linked To Collection Exercise
* `GET /collectionexercises/link/{collection_exercise_id}` will return a list of Sample Summary IDs linked to a collection exercise.

### Example JSON Response
```json
[
  "8fa33fd9-486a-41ca-baf2-2a833cfa031c",
  "08c191b8-e8b8-4920-b8de-87f85e536463"
]
```

An `HTTP 404 Not Found` status code is returned if the collection exercise with the specified ID could not be found. An `HTTP 204 No Content` status code is returned if there are no sample summaries linked to the specified collection exercise.



# Collection Exercise Event Service API
This part of the page  documents the Collection Exercise event service API endpoints. Apart from the Service Information endpoint, all these endpoints are secured using HTTP basic authentication. All endpoints return an `HTTP 200 OK` status code except where noted otherwise.

## Service Information

## Update Collection Event Exercise
* `PUT /collectionexercises/{collection_exercise_id}/events/{event-tag}` will update the collection exercise event timestamp with the given id.
* Returns 200 OK if the resource is updated


### Example text Request Body

```
2017-10-09T00:00:00.000+0000
```

## Get Event Linked To Collection Exercise event
* `GET /collectionexercises/{collection_exercie_id}/events/{event-tag}` will return an event linked to a collection exercise.


### Example JSON Response
```
json
{
    "id": "87ea52d1-745b-45fb-b1f7-148d33be09ed",
    "eventPK": 3,
    "tag": "End",
    "timestamp": "2017-10-09T00:00:00.000+0000",
    "collectionExercise": {
        "id": "14fb3e68-4dca-46db-bf49-04b84e07e97c",
        "exercisePK": 3,
        "name": "SOCIAL",
        "actualExecutionDateTime": null,
        "scheduledExecutionDateTime": null,
        "scheduledStartDateTime": "2001-12-31T12:00:00.000+0000",
        "actualPublishDateTime": null,
        "periodStartDateTime": "2017-09-08T00:00:00.000+0000",
        "periodEndDateTime": "2017-09-08T00:00:00.000+0000",
        "scheduledReturnDateTime": "2017-10-06T00:00:00.000+0000",
        "scheduledEndDateTime": "2099-01-01T00:00:00.000+0000",
        "executedBy": null,
        "state": "INIT",
        "sampleSize": null,
        "exerciseRef": "SOCIAL_201712",
        "userDescription": null,
        "created": null,
        "updated": null,
        "deleted": false,
        "surveyUuid": "cb0711c3-0ac8-41d3-ae0e-567e5ea1ef67"
    }
```

An `HTTP 404 Not Found` status code is returned if the collection exercise event with the specified ID could not be found.



## Delete Collection Exercise event
* `DELETE /collectionexercises/{collection_exercise_id}/events/{event-tag}` will delete the collection exercise event with the given id.

An `HTTP 202 ` status code is returned if the collection exercise event with the specified ID deleted.

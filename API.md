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
* `GET /collectionexercises/survey/cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87` will return a list of known collection exercises for the survey with an ID of `cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87`.

### Example JSON Response
```json
[
  {
    "id": "c6467711-21eb-4e78-804c-1db8392f93fb",
    "name": "201601",
    "scheduledExecutionDateTime": "2017-05-15T00:00:00Z"
  },
  {
    "id": "e33daf0e-6a27-40cd-98dc-c6231f50e84a",
    "name": "201602",
    "scheduledExecutionDateTime": "2017-08-12T00:00:00Z"
  }
]
```

An `HTTP 404 Not Found` status code is returned if the survey with the specified ID could not be found. An `HTTP 204 No Content` status code is returned if there are no known collection exercises for the specified survey.

## List Collection Exercises
* `GET /collectionexercises` will returns a list of all collection exercises

### Example JSON Response
```json
[
  {
    "id": "c6467711-21eb-4e78-804c-1db8392f93fb",
    "surveyId": "cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87",
    "name": "201601",
    "exerciseRef": "221_201712",
    "actualExecutionDateTime": "2017-05-15T14:20:24Z",
    "scheduledExecutionDateTime": "2017-05-15T00:00:00Z",
    "scheduledStartDateTime": "2017-06-01T00:00:00Z",
    "actualPublishDateTime": null,
    "periodStartDateTime": null,
    "periodEndDateTime": null,
    "scheduledReturnDateTime": "2017-06-30T00:00:00Z",
    "scheduledEndDateTime": null,
    "executedBy": "Fred Bloggs",
    "state": "EXECUTED",
    "caseTypes": [
      {
        "sampleUnitType": "B",
        "actionPlanId": "60df56d9-f491-4ac8-b256-a10154290a8b"
      },
      {
        "sampleUnitType": "BI",
        "actionPlanId": "b1f46e33-a3ef-4e50-939d-c18f8a9f11bb"
      }
    ]
  }
]
```

## Get Collection Exercise
* `GET /collectionexercises/c6467711-21eb-4e78-804c-1db8392f93fb` will returns the details of the collection exercise with an ID of `c6467711-21eb-4e78-804c-1db8392f93fb`.

### Example JSON Response
```json
{
  "id": "c6467711-21eb-4e78-804c-1db8392f93fb",
  "surveyId": "cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87",
  "name": "201601",
  "exerciseRef": "221_201712",
  "actualExecutionDateTime": "2017-05-15T14:20:24Z",
  "scheduledExecutionDateTime": "2017-05-15T00:00:00Z",
  "scheduledStartDateTime": "2017-06-01T00:00:00Z",
  "actualPublishDateTime": null,
  "periodStartDateTime": null,
  "periodEndDateTime": null,
  "scheduledReturnDateTime": "2017-06-30T00:00:00Z",
  "scheduledEndDateTime": null,
  "executedBy": "Fred Bloggs",
  "state": "EXECUTED",
  "caseTypes": [
    {
      "sampleUnitType": "B",
      "actionPlanId": "60df56d9-f491-4ac8-b256-a10154290a8b"
    },
    {
      "sampleUnitType": "BI",
      "actionPlanId": "b1f46e33-a3ef-4e50-939d-c18f8a9f11bb"
    }
  ]
}
```

An `HTTP 404 Not Found` status code is returned if the collection exercise with the specified ID could not be found.

## Create Collection Exercise
* `POST /collectionexercises` will create a new collection exercise
* Returns 201 Created if the resource is created

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
* `PUT /collectionexercises/c6467711-21eb-4e78-804c-1db8392f93fb` will update the collection exercise with an ID of `c6467711-21eb-4e78-804c-1db8392f93fb`.
* Returns 200 OK if the resource is updated

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
* `PUT /collectionexercises/c6467711-21eb-4e78-804c-1db8392f93fb/exerciseRef` will update the exerciseRef for collection exercise with an ID of `c6467711-21eb-4e78-804c-1db8392f93fb`.
* Returns 200 OK if the resource is updated

### Example Request Body
```
201803
```

## Update Collection Exercise userDescription (user visible name)
* `PUT /collectionexercises/c6467711-21eb-4e78-804c-1db8392f93fb/userDescription` will update the user visible name for collection exercise with an ID of `c6467711-21eb-4e78-804c-1db8392f93fb`.
* Returns 200 OK if the resource is updated

### Example Request Body
```
August 2018
```

## Update Collection Exercise exerciseRef (name)
* `PUT /collectionexercises/c6467711-21eb-4e78-804c-1db8392f93fb/name` will update the name of collection exercise with an ID of `c6467711-21eb-4e78-804c-1db8392f93fb`.
* Returns 200 OK if the resource is updated

### Example Request Body
```
Collex name
```

## Update Collection Exercise survey
* `PUT /collectionexercises/c6467711-21eb-4e78-804c-1db8392f93fb/surveyId` will update the survey for collection exercise with an ID of `c6467711-21eb-4e78-804c-1db8392f93fb`.
* Returns 200 OK if the resource is updated

### Example Request Body
```
cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87
```

## Delete Collection Exercise
* `DELETE /collectionexercises/c6467711-21eb-4e78-804c-1db8392f93fb` will mark the collection exercise with an ID of `c6467711-21eb-4e78-804c-1db8392f93fb` for deletion.  This operation is included for future use and currently serves no purpose (also included for completeness).


## Execute Collection Exercise
* `POST /collectionexerciseexecute/c6467711-21eb-4e78-804c-1db8392f93fb` will execute the collection exercise with an ID of `c6467711-21eb-4e78-804c-1db8392f93fb`.

### Example JSON Response
```json
{
  "sampleUnitsTotal": "670"
}
```

An `HTTP 404 Not Found` status code is returned if the collection exercise with the specified ID could not be found.

## Link Sample Summary To Collection Exercise
* `PUT /collectionexercises/link/c6467711-21eb-4e78-804c-1db8392f93fb` will link the Sample Summaries specified in the json request to the collection exercise with an ID of `c6467711-21eb-4e78-804c-1db8392f93fb`.

The endpoint will also delete any entries currently in the samplelink table for `c6467711-21eb-4e78-804c-1db8392f93fb` before linking to the sample summary IDs in the json request.

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

## Get Sample Summaries Linked To Collection Exercise
* `GET /collectionexercises/link/c6467711-21eb-4e78-804c-1db8392f93fb` will return a list of Sample Summary IDs linked to a collection exercise.

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
* `PUT /collectionexercises/14fb3e68-4dca-46db-bf49-04b84e07e97c/events/End` will update the collection exercise event timestamp with an ID of `14fb3e68-4dca-46db-bf49-04b84e07e97c`.
* Returns 200 OK if the resource is updated


### Example JSON Request Body

```
json
{
    "date": "2017-10-09T00:00:00.000+0000"
}
```

## Get Event Linked To Collection Exercise event
* `GET /collectionexercises/14fb3e68-4dca-46db-bf49-04b84e07e97c/events/End` will return an event linked to a collection exercise.


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
* `DELETE /collectionexercises/9139f443-b44d-4aaf-a5bb-942a7916e49e/events/End` will delete the collection exercise event with an ID of `9139f443-b44d-4aaf-a5bb-942a7916e49e`.

An `HTTP 202 ` status code is returned if the collection exercise event with the specified ID deleted.

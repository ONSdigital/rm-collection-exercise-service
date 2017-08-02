# Collection Exercise Service API
This page documents the Collection Exercise service API endpoints. These endpoints will be secured using HTTP basic authentication initially. All endpoints return an `HTTP 200 OK` status code except where noted otherwise.

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

## Execute Collection Exercise
* `PUT /collectionexercises/c6467711-21eb-4e78-804c-1db8392f93fb` will execute the collection exercise with an ID of `c6467711-21eb-4e78-804c-1db8392f93fb`.

### Example JSON Response
```json
{
  "sampleUnitsTotal": "670"
}
```

An `HTTP 404 Not Found` status code is returned if the collection exercise with the specified ID could not be found.

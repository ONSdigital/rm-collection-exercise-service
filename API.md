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

## Get Collection Exercises for Surveys
* `GET /collectionexercises/surveys?surveyIds=cb8accda-6118-4d3b-85a3-149e28960c54,b447e134-5e5d-46fb-b4fc-15efdcbe5ca7` 
Will return a dictionary of collection exercises for each survey id in the supplied list of survey ids. In the form of a dictionary with a key of survey id, and value of a list of Collection Exercises.
* `GET /collectionexercises/survey?surveyIds=cb8accda-6118-4d3b-85a3-149e28960c54,b447e134-5e5d-46fb-b4fc-15efdcbe5ca7&liveOnly=true` 
As above , but only LIVE collexes returned

### Example JSON Response
```json
{
  "cb8accda-6118-4d3b-85a3-149e28960c54": 
  [
      {
        "id": "c6467711-21eb-4e78-804c-1db8392f93fb",
        "surveyId": "cb8accda-6118-4d3b-85a3-149e28960c54",
        "actualExecutionDateTime": null,
        "scheduledExecutionDateTime": null,
        "scheduledStartDateTime": null,
        "actualPublishDateTime": null,
        "periodStartDateTime": null,
        "periodEndDateTime": null,
        "scheduledReturnDateTime": null,
        "scheduledEndDateTime": null,
        "executedBy": null,
        "state": "LIVE",
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
        "actualExecutionDateTime": null,
        "scheduledExecutionDateTime": null,
        "scheduledStartDateTime": null,
        "actualPublishDateTime": null,
        "periodStartDateTime": null,
        "periodEndDateTime": null,
        "scheduledReturnDateTime": null,
        "scheduledEndDateTime": null,
        "executedBy": null,
        "state": "LIVE",
        "caseTypes": null,
        "exerciseRef": "201802",
        "userDescription": "February 2018",
        "created": "2018-01-09T12:56:09.709Z",
        "updated": null,
        "deleted": null,
        "validationErrors": []
      }
    ]
}
```

## Get Collection Exercises for Survey
* `GET /collectionexercises/survey/{survey_id}` will return a list of known collection exercises for the survey id.
* `GET /collectionexercises/survey/{survey_id}?liveOnly=true` will return a list of LIVE collection exercises for the survey id.

### Example JSON Response
```json
[
  {
    "id": "c6467711-21eb-4e78-804c-1db8392f93fb",
    "surveyId": "cb8accda-6118-4d3b-85a3-149e28960c54",
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
* `GET /collectionexercises/{id}` will return the details of the collection exercise with the given id.

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
    "exerciseRef": "201715",
    "userDescription": "August 2017"
}
```

## Update Collection Exercise
* `PUT /collectionexercises/{id}` will update the collection exercise with the given id.
* Returns 200 OK if the resource is updated
* Returns 400 Bad Request
* Returns 409 conflict

### Example JSON Request Body

```json
{
    "exerciseRef": "201715",
    "userDescription": "Updated August 2017",
    "surveyId": "cb0711c3-0ac8-41d3-ae0e-567e5ea1ef77"
}
```

## Update Collection Exercise exerciseRef (period)
* `PUT /collectionexercises/{id}/exerciseRef` will update the exerciseRef for collection exercise with given id.
* Returns 200 OK if the resource is updated.
* Returns 400 Bad Request, resource not updated.
* Returns 409 conflict, resource not updated.

### Example Request Body
```text/plain
201803
```

## Update Collection Exercise Name
* `PUT /collectionexercise/{id}/name` will update the name for the collection exercise with given id.
* Returns 200 OK if the resource is updated
* Returns 400 Bad Request, not updated resource.
* Returns 409 conflict, not updated resource.

## Update Collection Exercise userDescription (user visible name)
* `PUT /collectionexercises/{id}/userDescription` will update the user description for collection exercise with given id.
* Returns 200 OK if the resource is updated
* Returns 400 Bad Request, not updated resource.
* Returns 409 conflict, not updated resource.

### Example Request Body
```text/plain
August 2018
```

### Example Request Body
```text\plain
Collex name
```

## Update Collection Exercise start
* `PUT /collectionexercises/{id}/scheduledStart` will update the scheduled start of collection exercise with given id.
* Returns 200 OK if the resource is updated
* Returns 400 Bad Request, resource not updated
* Returns 409 Conflict, resource not updated


## Update Collection Exercise survey
* `PUT /collectionexercises/{id}/surveyId` will update the survey for collection exercise with given id.
* Returns 200 OK if the resource is updated
* Returns 400 Bad Request, resource not updated
* Returns 409 Conflict, resource not updated

### Example Request Body
```
cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87
```

## Delete Collection Exercise
* `DELETE /collectionexercises/{id}` will mark the collection exercise with given id for deletion.  This operation is included for future use and currently serves no purpose (also included for completeness).
* Returns collection exercise to be deleted.


## Execute Collection Exercise
* `POST /collectionexerciseexecution/{id}` will execute the collection exercise with the given id.

### Example JSON Response
```json
{
  "sampleUnitsTotal": "670"
}
```

An `HTTP 404 Not Found` status code is returned if the collection exercise with the specified ID could not be found.

## Link Sample Summary To Collection Exercise
* `PUT /collectionexercises/link/{collectionExerciseId}` will link the Sample Summaries specified in the json request to the collection exercise with the given id.

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
* `DELETE /collectionexercises/unlink/{collectionExerciseId}/sample/{sampleSummaryId}` will unlink the Sample Summary specified from the collection exercise with the given id.
* Returns 204 noContent if successfully unlinked

An `HTTP 404 Not Found` status code is returned if the collection exercise with the specified ID could not be found.

## Get Sample Summaries Linked To Collection Exercise
* `GET /collectionexercises/link/{collectionExerciseId}` will return a list of Sample Summary IDs linked to a collection exercise.

### Example JSON Response
```json
[
  "8fa33fd9-486a-41ca-baf2-2a833cfa031c",
  "08c191b8-e8b8-4920-b8de-87f85e536463"
]
```

An `HTTP 404 Not Found` status code is returned if the collection exercise with the specified ID could not be found. An `HTTP 204 No Content` status code is returned if there are no sample summaries linked to the specified collection exercise.

NOTE: For some reason, in the code, there is another endpoint with an almost identical name ("link/{collectionExerciseId}") that accomplishes the exact same thing, except that this endpoint doesn't work properly because it doesn't have a "/" to separate it from the "/collectionexercises" part. As such, it is redundant, and should be deleted.

# Collection Exercise Event Service API
This part of the page  documents the Collection Exercise event service API endpoints. Apart from the Service Information endpoint, all these endpoints are secured using HTTP basic authentication. All endpoints return an `HTTP 200 OK` status code except where noted otherwise.

## Service Information

## Create Collection Exercise Event
* `POST /collectionexercises/{id}/events` will create a new collection exercise event timestamp with the given id.
* Returns 201 OK if the resource is updated

### Example Request Body

```
    {
        "tag": "exercise_end_2",
        "timestamp": "2018-04-01T00:00:00.000Z"
    }
```

## Get Collection Exercise Events
* `GET /collectionexercises/{id}/events` will display the events for a given collection ID.

### Example JSON Response
```
json
[
    {"id":"f924633a-573e-4bb8-9a44-bd571d517fa9",
    "collectionExerciseId":"df61f560-bff0-491b-b6e4-9dd17c824247",
    "tag":"employment",
    "timestamp":"2018-06-15T00:00:00.000Z"},
    {"id":"6b5f9a2f-7b5c-40c5-818f-07c8904418c5",
    "collectionExerciseId":"df61f560-bff0-491b-b6e4-9dd17c824247",
    "tag":"exercise_end",
    "timestamp":"2020-08-31T00:00:00.000Z"},
    {"id":"7f530007-6213-44d7-99f7-08fda2792946",
    "collectionExerciseId":"df61f560-bff0-491b-b6e4-9dd17c824247",
    "tag":"go_live",
    "timestamp":"2018-06-25T00:00:00.000Z"},
    {"id":"bed8307d-df6a-403c-b2f0-04c219bc88b4",
    "collectionExerciseId":"df61f560-bff0-491b-b6e4-9dd17c824247",
    "tag":"mps",
    "timestamp":"2018-06-19T00:00:00.000Z"},
    {"id":"8b5c3766-5658-46ea-ba48-0187b54f997a",
    "collectionExerciseId":"df61f560-bff0-491b-b6e4-9dd17c824247",
    "tag":"ref_period_end",
    "timestamp":"2018-06-30T00:00:00.000Z"},
    {"id":"ca3ae637-0195-4423-bc75-aa11ab7c86d5",
    "collectionExerciseId":"df61f560-bff0-491b-b6e4-9dd17c824247",
    "tag":"ref_period_start",
    "timestamp":"2018-06-01T00:00:00.000Z"},
    {"id":"ad5836b0-7159-47f1-8273-a247e3e07032",
    "collectionExerciseId":"df61f560-bff0-491b-b6e4-9dd17c824247",
    "tag":"reminder",
    "timestamp":"2018-07-10T00:00:00.000Z"},
    {"id":"de82fff7-39ad-4355-be45-608f4d03b54a",
    "collectionExerciseId":"df61f560-bff0-491b-b6e4-9dd17c824247",
    "tag":"return_by",
    "timestamp":"2018-07-07T00:00:00.000Z"}
]
```

An `HTTP 404 Not Found` status code is returned if the collection exercise event with the specified ID could not be found.

## Update Collection Exercise Event
* `PUT /collectionexercises/{id}/events/{tag}` will update the collection exercise event timestamp with the given id.
* Returns 200 OK if the resource is updated


### Example text Request Body

```
2017-10-09T00:00:00.000+0000
```

## Get Event Linked To Collection Exercise event
* `GET /collectionexercises/{id}/events/{tag}` will return an event linked to a collection exercise.


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
* `DELETE /collectionexercises/{id}/events/{tag}` will delete the collection exercise event with the given id.

An `HTTP 202 ` status code is returned if the collection exercise event with the specified ID deleted.

## Get All Scheduled Events
* `GET /collectionexercises/events/scheduled` will return a list of all scheduled events.

### Example JSON Response
```
json
[
    {"id":"a39371be-9df1-4ab4-a1bd-27222331b79b","collectionExerciseId":"d4445b0c-7e61-4b5d-afbc-4eafa6b7d9de","tag":"ref_period_start",
    "timestamp":"2019-11-01T00:00:00.000Z"},{"id":"ee59b380-ab45-4905-9ebe-748f128b1862",
    "collectionExerciseId":"aae2efaa-7070-4a7b-ac3d-3d932669528a","tag":"exercise_end","timestamp":"2020-02-28T00:00:00.000Z"},
    {"id":"dc72a65d-f693-4ab3-abc3-fd0c6a2f4b58","collectionExerciseId":"259d929f-2dba-48d0-8476-bce2577c0e96","tag":"exercise_end",
    "timestamp":"2021-01-31T00:00:00.000Z"},{"id":"1b0c88c5-7170-40a7-aad6-09234bf61768",
    "collectionExerciseId":"631383db-5076-4de9-ae5a-96a30ad6ea5a","tag":"ref_period_start","timestamp":"2019-10-01T00:00:00.000Z"},
    {"id":"7152bb6a-6d7e-4049-ada3-aeae970efb11","collectionExerciseId":"b486af41-2277-408a-8d41-99aaa09c3e51","tag":"exercise_end",
    "timestamp":"2020-12-31T00:00:00.000Z"},{"id":"3364185f-3fdf-4dab-aef9-e3682405eab7",
    "collectionExerciseId":"b3145fb7-3eaa-42ef-8873-983dfafd4e54","tag":"return_by","timestamp":"2019-06-08T00:00:00.000Z"},
    {"id":"d0889b70-8310-4337-9ff9-e098a95a9a09","collectionExerciseId":"9ae470fa-4e22-490a-add4-83b04ce9d6c2","tag":"go_live",
    "timestamp":"2019-11-26T00:00:00.000Z"},{"id":"a9d4edcc-2847-4fbb-b7f0-951f58e268fd",
    "collectionExerciseId":"b89ddbfb-884c-4de6-8324-2b55e51cf70b","tag":"ref_period_start","timestamp":"2019-06-01T00:00:00.000Z"},
    {"id":"41bce284-aa09-463d-b085-6986966c82db","collectionExerciseId":"85ee3ac9-4523-4e75-aedf-aa5fd7c7a7c9","tag":"exercise_end",
    "timestamp":"2021-11-30T00:00:00.000Z"},{"id":"6a59944a-acba-4da1-bc04-c499bbaba7ab",
    "collectionExerciseId":"bb8c729a-6f64-4af4-b760-eb742fd5ccec","tag":"exercise_end","timestamp":"2020-08-31T00:00:00.000Z"}
]
```

An `HTTP 404 Not Found` status code is returned if an event could not be found.

## Get Events linked to list of Collection Exercises
* `GET /collectionexercises/events?ids={a,b}` will return the events linked to the specified collection exercise IDs.
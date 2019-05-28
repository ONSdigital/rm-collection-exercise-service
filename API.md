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
[{"id":"a39371be-9df1-4ab4-a1bd-27222331b79b","collectionExerciseId":"d4445b0c-7e61-4b5d-afbc-4eafa6b7d9de","tag":"ref_period_start","timestamp":"2019-11-01T00:00:00.000Z"},{"id":"ee59b380-ab45-4905-9ebe-748f128b1862","collectionExerciseId":"aae2efaa-7070-4a7b-ac3d-3d932669528a","tag":"exercise_end","timestamp":"2020-02-28T00:00:00.000Z"},{"id":"dc72a65d-f693-4ab3-abc3-fd0c6a2f4b58","collectionExerciseId":"259d929f-2dba-48d0-8476-bce2577c0e96","tag":"exercise_end","timestamp":"2021-01-31T00:00:00.000Z"},{"id":"1b0c88c5-7170-40a7-aad6-09234bf61768","collectionExerciseId":"631383db-5076-4de9-ae5a-96a30ad6ea5a","tag":"ref_period_start","timestamp":"2019-10-01T00:00:00.000Z"},{"id":"7152bb6a-6d7e-4049-ada3-aeae970efb11","collectionExerciseId":"b486af41-2277-408a-8d41-99aaa09c3e51","tag":"exercise_end","timestamp":"2020-12-31T00:00:00.000Z"},{"id":"3364185f-3fdf-4dab-aef9-e3682405eab7","collectionExerciseId":"b3145fb7-3eaa-42ef-8873-983dfafd4e54","tag":"return_by","timestamp":"2019-06-08T00:00:00.000Z"},{"id":"d0889b70-8310-4337-9ff9-e098a95a9a09","collectionExerciseId":"9ae470fa-4e22-490a-add4-83b04ce9d6c2","tag":"go_live","timestamp":"2019-11-26T00:00:00.000Z"},{"id":"a9d4edcc-2847-4fbb-b7f0-951f58e268fd","collectionExerciseId":"b89ddbfb-884c-4de6-8324-2b55e51cf70b","tag":"ref_period_start","timestamp":"2019-06-01T00:00:00.000Z"},{"id":"591bbc17-9fdc-49d7-b236-bcce08b85ea0","collectionExerciseId":"9d2d97cf-edf6-407c-9e20-fa980bab3a7d","tag":"mps","timestamp":"2019-10-18T00:00:00.000Z"},{"id":"e7d580b2-ed16-4e60-84d0-ff3b4c3615f5","collectionExerciseId":"326672bc-0d14-434a-af7e-4dc161356090","tag":"exercise_end","timestamp":"2021-02-28T00:00:00.000Z"},{"id":"69a7eb14-84f7-41c8-865f-088942aaaa0e","collectionExerciseId":"3f6337d8-8eb6-4a02-a6a3-f29d2d6e8510","tag":"ref_period_start","timestamp":"2019-10-01T00:00:00.000Z"},{"id":"6b45b747-99ca-4525-9627-68a53c250c54","collectionExerciseId":"570eed21-2171-4484-9dfc-c3909cf5127c","tag":"exercise_end","timestamp":"2020-07-31T00:00:00.000Z"},{"id":"c7a35caa-44a2-4d04-bfb1-733e882b1cfc","collectionExerciseId":"625def2f-2111-49e6-87d7-eeb1709ab664","tag":"employment","timestamp":"2019-06-15T00:00:00.000Z"},{"id":"87ae04d4-11ee-45aa-8fa0-fb83e79286e4","collectionExerciseId":"98ae1930-1e14-4267-8c0b-1bee9715b0cd","tag":"go_live","timestamp":"2019-09-24T00:00:00.000Z"},{"id":"3b4da7f9-0836-4067-8964-e2934c4c42e8","collectionExerciseId":"c75bb593-db4b-4dbf-92eb-55d0d085a7e2","tag":"return_by","timestamp":"2020-01-04T00:00:00.000Z"},{"id":"465f841b-b3f3-4d36-b6e6-8dba05417826","collectionExerciseId":"fc757d76-f100-412b-83c7-8955ffa3a810","tag":"exercise_end","timestamp":"2020-06-30T00:00:00.000Z"},{"id":"ebc87470-a3aa-4970-b370-c5fb3852202b","collectionExerciseId":"d93a5bc6-edd1-4ed1-b2fc-e1e417f4b493","tag":"ref_period_end","timestamp":"2019-06-30T00:00:00.000Z"},{"id":"ac06b3fd-2917-4420-92a4-be533c23f99e","collectionExerciseId":"87612d73-49a2-46ac-b939-cac63a33ca92","tag":"return_by","timestamp":"2019-08-08T00:00:00.000Z"},{"id":"c07ea160-b66c-4f72-8e2c-5744a1b4c4a4","collectionExerciseId":"f5fe1edb-c927-4ad6-b2f4-abca7cc146f4","tag":"reminder","timestamp":"2019-09-12T00:00:00.000Z"},{"id":"9c777b6e-9074-4274-b8d7-0bc736415fb7","collectionExerciseId":"710399cd-7758-419f-8210-8cae845884bf","tag":"return_by","timestamp":"2019-11-09T00:00:00.000Z"},{"id":"1c682ba4-2cf3-4d74-9df8-154ea356bd67","collectionExerciseId":"6ab11e56-9b06-4793-906d-83750cc035c4","tag":"exercise_end","timestamp":"2021-06-30T00:00:00.000Z"},{"id":"949899b7-ba79-4981-90fc-f36970ffaf84","collectionExerciseId":"cfc81f0c-ce96-48ce-8eef-fbeacd46ae48","tag":"go_live","timestamp":"2019-06-01T00:00:00.000Z"},{"id":"313d94db-3d5f-4a8e-a112-52925a8cce15","collectionExerciseId":"c34bae50-4b61-4f22-9bf0-00245d739c26","tag":"return_by","timestamp":"2020-01-07T00:00:00.000Z"},{"id":"ccf63818-d36d-4950-b9ca-b3960d871bd0","collectionExerciseId":"0529a56c-d852-4935-8a1e-316db9d928fe","tag":"reminder2","timestamp":"2019-11-07T00:00:00.000Z"},{"id":"a71807ff-8d1b-4a15-b4f0-0073426a77bb","collectionExerciseId":"4a3360ce-9a0e-4ed2-834c-dbde17640957","tag":"exercise_end","timestamp":"2021-09-30T00:00:00.000Z"},{"id":"b8301ee2-4628-414b-b33e-b22adec257a8","collectionExerciseId":"b89ddbfb-884c-4de6-8324-2b55e51cf70b","tag":"employment","timestamp":"2019-06-15T00:00:00.000Z"},{"id":"83143e19-c799-4d5c-bdc8-a9f116e2ce57","collectionExerciseId":"47876bac-84e6-46ac-bf09-f1221cb71848","tag":"exercise_end","timestamp":"2020-12-31T00:00:00.000Z"},{"id":"d38965fd-110b-4141-b5a9-bdca151230cb","collectionExerciseId":"68154a7c-3185-4590-a615-e0fa0b647708","tag":"exercise_end","timestamp":"2021-03-31T00:00:00.000Z"},{"id":"3b826f72-b6c9-4d85-9bae-9d3affeb9f9e","collectionExerciseId":"4a3360ce-9a0e-4ed2-834c-dbde17640957","tag":"ref_period_end","timestamp":"2019-08-25T00:00:00.000Z"},{"id":"84cbc2f8-7860-494f-a420-d78438ec620e","collectionExerciseId":"4388e326-3225-49c8-889f-1a18c58da3d6","tag":"return_by","timestamp":"2019-09-10T00:00:00.000Z"},{"id":"465ce7e4-e964-4db4-ace5-b0c80a95ce70","collectionExerciseId":"ca2e2227-0823-4aef-a2ac-b0a47d8b288d","tag":"exercise_end","timestamp":"2020-07-31T00:00:00.000Z"},{"id":"c79e5ffb-d460-42d8-b5b0-367b2a373188","collectionExerciseId":"625def2f-2111-49e6-87d7-eeb1709ab664","tag":"ref_period_start","timestamp":"2019-06-15T00:00:00.000Z"},{"id":"2eea0533-9d8a-47bd-add8-b54900bdcfd9","collectionExerciseId":"4a3360ce-9a0e-4ed2-834c-dbde17640957","tag":"mps","timestamp":"2019-08-16T00:00:00.000Z"},{"id":"3b6e2aee-e66a-47d6-ab35-58d57911dadf","collectionExerciseId":"ebef3ac8-58d4-45e1-8ff9-9060e2b326c5","tag":"ref_period_end","timestamp":"2019-09-30T00:00:00.000Z"},{"id":"984ac327-62b3-499c-9984-cf2c3af0b787","collectionExerciseId":"032a8b37-6cb4-4fba-adce-afff5bb1b4dc","tag":"ref_period_end","timestamp":"2019-06-30T00:00:00.000Z"},{"id":"acb9e768-4254-4047-aa1d-828db033090b","collectionExerciseId":"625def2f-2111-49e6-87d7-eeb1709ab664","tag":"reminder2","timestamp":"2019-07-23T00:00:00.000Z"},{"id":"224ccfc7-6a91-408d-bea7-2ccb9067ce7d","collectionExerciseId":"d274a802-83aa-492b-bbaa-7d3ad11932a4","tag":"exercise_end","timestamp":"2021-04-30T00:00:00.000Z"},{"id":"927c7f83-5fd3-4eec-8194-3c086b203c4e","collectionExerciseId":"6e466abd-3c6e-4093-8b68-8159778b02ec","tag":"employment","timestamp":"2019-06-15T00:00:00.000Z"},{"id":"6f5aa2b7-b028-4084-9ec9-c4981db109d1","collectionExerciseId":"f206b6e5-8b5f-4d5a-b5e3-8be3cbaef8a0","tag":"exercise_end","timestamp":"2020-07-31T00:00:00.000Z"},{"id":"4ddacd89-bd08-4e55-bc85-43503f0c36f7","collectionExerciseId":"f4195ba4-f0fd-4c21-9da7-bd222016104f","tag":"mps","timestamp":"2019-07-25T00:00:00.000Z"},{"id":"48116e1c-cfb4-41fc-ad8b-769bbe3a2e65","collectionExerciseId":"1cb4ec16-bde4-4d8f-9237-7614e04d22ae","tag":"return_by","timestamp":"2019-10-07T00:00:00.000Z"},{"id":"69f67fe1-7a31-459e-bbda-70e1f2d2846f","collectionExerciseId":"ebef3ac8-58d4-45e1-8ff9-9060e2b326c5","tag":"mps","timestamp":"2019-09-14T00:00:00.000Z"},{"id":"b1ff5abe-046a-47c4-8e32-bfeeb7c48ebd","collectionExerciseId":"032a8b37-6cb4-4fba-adce-afff5bb1b4dc","tag":"go_live","timestamp":"2019-06-21T00:00:00.000Z"},{"id":"c1b93cda-36bf-4f4e-a5f8-6d9c90647b8f","collectionExerciseId":"04384f50-e1bc-4ce3-8eab-9949283224b1","tag":"exercise_end","timestamp":"2021-08-31T00:00:00.000Z"},{"id":"5c91a6b0-55dc-4f12-a8f9-c586995dbf77","collectionExerciseId":"0ae2a344-9048-482b-abb2-d56eec545e3e","tag":"exercise_end","timestamp":"2021-10-31T00:00:00.000Z"},{"id":"d396683d-1cce-41ed-9cb4-132c5421c006","collectionExerciseId":"d93a5bc6-edd1-4ed1-b2fc-e1e417f4b493","tag":"exercise_end","timestamp":"2021-08-31T00:00:00.000Z"},{"id":"0a45923b-39f6-4792-bd85-1b62858d5284","collectionExerciseId":"1505c106-5d88-4976-b8c1-85c15c545465","tag":"go_live","timestamp":"2019-11-20T00:00:00.000Z"},{"id":"3164a788-92ac-4539-9414-d400fc99e72a","collectionExerciseId":"bb8e8f22-d26f-44f8-b70c-7d646768b78b","tag":"return_by","timestamp":"2019-11-07T00:00:00.000Z"},{"id":"e0cc908a-eaa5-49ae-9e65-c4cdf6d8b9f7","collectionExerciseId":"abee6545-15ee-4c85-9878-a0b4273db2e6","tag":"exercise_end","timestamp":"2020-03-31T00:00:00.000Z"},{"id":"8f182e3e-01b9-4060-bb2e-8ac5e1f1bd73","collectionExerciseId":"4a3360ce-9a0e-4ed2-834c-dbde17640957","tag":"go_live","timestamp":"2019-08-21T00:00:00.000Z"},{"id":"98548785-a34a-4e66-923e-785c60ae0c58","collectionExerciseId":"9ae470fa-4e22-490a-add4-83b04ce9d6c2","tag":"reminder","timestamp":"2019-12-11T00:00:00.000Z"},{"id":"f219ceaa-21c0-4c2d-9c19-272ff326fcde","collectionExerciseId":"d93a5bc6-edd1-4ed1-b2fc-e1e417f4b493","tag":"reminder","timestamp":"2019-07-12T00:00:00.000Z"},{"id":"262e16cb-17af-44e6-9e00-c2292949d85a","collectionExerciseId":"f5fe1edb-c927-4ad6-b2f4-abca7cc146f4","tag":"go_live","timestamp":"2019-08-24T00:00:00.000Z"},{"id":"87553ff7-faad-4392-96b8-63244679b1b2","collectionExerciseId":"9fd9158a-e47b-4c96-bc91-c62d4bce1947","tag":"return_by","timestamp":"2020-01-08T00:00:00.000Z"},{"id":"e1bfca9b-bd97-40df-a898-2e13a2b92852","collectionExerciseId":"98ae1930-1e14-4267-8c0b-1bee9715b0cd","tag":"ref_period_end","timestamp":"2019-09-30T00:00:00.000Z"},{"id":"e27699b1-f467-4a4b-89b8-f2c93a97ee27","collectionExerciseId":"d93a5bc6-edd1-4ed1-b2fc-e1e417f4b493","tag":"go_live","timestamp":"2019-06-26T00:00:00.000Z"},{"id":"22c95e23-d369-40b7-9004-f2d271bb5161","collectionExerciseId":"93979cc5-593d-4229-bd5f-aaa123f0ca0e","tag":"exercise_end","timestamp":"2020-04-30T00:00:00.000Z"},{"id":"b1d098ac-7647-4570-a876-ac73ed394dc9","collectionExerciseId":"bba33485-d677-4e74-9593-9798a095f6eb","tag":"return_by","timestamp":"2020-01-09T00:00:00.000Z"},{"id":"c7edcdd3-270d-46fb-bcd2-e090e3ff0709","collectionExerciseId":"032a8b37-6cb4-4fba-adce-afff5bb1b4dc","tag":"exercise_end","timestamp":"2021-07-31T00:00:00.000Z"},{"id":"f3d9242d-65bb-4ef2-aafe-4c8caa9053be","collectionExerciseId":"bb9cc90c-b20e-42b9-85a9-2cbf6be390be","tag":"exercise_end","timestamp":"2020-10-31T00:00:00.000Z"},{"id":"1938f4af-a60b-4564-97d0-75ee98c7eb27","collectionExerciseId":"1505c106-5d88-4976-b8c1-85c15c545465","tag":"reminder","timestamp":"2019-12-03T00:00:00.000Z"},{"id":"50015c74-059a-4dbb-a48d-02f1cc23d3df","collectionExerciseId":"f06a657b-996d-43b5-bd0a-d0e6ecf6b7d7","tag":"exercise_end","timestamp":"2020-05-31T00:00:00.000Z"},{"id":"b1a00cd7-63db-4224-9194-3916c8520985","collectionExerciseId":"9ae470fa-4e22-490a-add4-83b04ce9d6c2","tag":"ref_period_end","timestamp":"2019-11-30T00:00:00.000Z"},{"id":"f93067b7-1628-4c34-9b49-60643431788a","collectionExerciseId":"8ad14503-49bd-4d30-9c74-604212429788","tag":"ref_period_start","timestamp":"2019-07-01T00:00:00.000Z"},{"id":"0dad97c0-1499-4851-9658-b7ccaab2dd68","collectionExerciseId":"631383db-5076-4de9-ae5a-96a30ad6ea5a","tag":"reminder2","timestamp":"2020-02-06T00:00:00.000Z"},{"id":"6cef061f-25a0-4af8-a059-6f8799a66a1e","collectionExerciseId":"e48905aa-0c24-48b0-bef1-257b80e154eb","tag":"exercise_end","timestamp":"2021-01-31T00:00:00.000Z"},{"id":"0d938f6f-e840-419c-8599-5e8f490fe744","collectionExerciseId":"81498954-b626-4aa7-b329-2bdb2749fd59","tag":"mps","timestamp":"2019-10-25T00:00:00.000Z"},{"id":"6b5f9a2f-7b5c-40c5-818f-07c8904418c5","collectionExerciseId":"df61f560-bff0-491b-b6e4-9dd17c824247","tag":"exercise_end","timestamp":"2020-08-31T00:00:00.000Z"},{"id":"9c43d222-7eee-4368-b4ea-6d3a911b620e","collectionExerciseId":"d5879834-c71f-4ff7-86b1-ba77bed732bf","tag":"exercise_end","timestamp":"2020-06-30T00:00:00.000Z"},{"id":"47daa6a2-dc5f-4f44-8934-91249cf255e1","collectionExerciseId":"00ad3483-afb8-46b0-9aa4-8d374d8480fc","tag":"exercise_end","timestamp":"2020-08-31T00:00:00.000Z"},{"id":"f2c2d716-1ff5-4534-b633-4b2bb22d03c8","collectionExerciseId":"687cf71f-4ad3-4bd2-b054-5cfafddb109d","tag":"return_by","timestamp":"2019-12-21T00:00:00.000Z"},{"id":"7cda5e0f-3bc0-4dcc-9ccf-95c740d83d3a","collectionExerciseId":"9d2d97cf-edf6-407c-9e20-fa980bab3a7d","tag":"exercise_end","timestamp":"2021-11-30T00:00:00.000Z"},{"id":"5aeb19e6-ca3c-43a9-b5d9-c91164704e49","collectionExerciseId":"06b30955-6a9c-4a0c-b86f-8a94778a943c","tag":"return_by","timestamp":"2019-12-10T00:00:00.000Z"},{"id":"ab499f9e-367d-45a6-9f64-f504011c212d","collectionExerciseId":"54e31437-c6ae-41c1-893b-ae7224c8e9f9","tag":"return_by","timestamp":"2019-06-08T00:00:00.000Z"},{"id":"117f1b6b-86cd-4408-99f6-e7da45c963bc","collectionExerciseId":"cc9afa9c-6d49-4bdc-8d05-e5b31a55fb36","tag":"go_live","timestamp":"2019-09-25T00:00:00.000Z"},{"id":"e2054329-93ed-49bd-8d68-84fb2f933f5a","collectionExerciseId":"393fee7b-249c-408e-8bb6-0093fb6c54ca","tag":"exercise_end","timestamp":"2020-06-30T00:00:00.000Z"},{"id":"bdfd2ffd-c62c-451a-b2f6-1f85d43cfc14","collectionExerciseId":"66e98d19-4a60-4a3d-a4d0-951f0020dd65","tag":"return_by","timestamp":"2019-08-08T00:00:00.000Z"},{"id":"88f39822-46e7-4c66-ae05-c14f66dbdcd3","collectionExerciseId":"fe10fe34-cd08-4116-a89b-d5ec66dec04c","tag":"exercise_end","timestamp":"2020-11-30T00:00:00.000Z"},{"id":"f8c5e61c-5a72-463e-8bf7-151a06c0efe0","collectionExerciseId":"9ae470fa-4e22-490a-add4-83b04ce9d6c2","tag":"mps","timestamp":"2019-11-21T00:00:00.000Z"},{"id":"67af1ba3-a2d1-4c15-a88a-55e170d38f18","collectionExerciseId":"1b97b77f-8a4e-4d4c-9b87-89efe404693d","tag":"exercise_end","timestamp":"2020-09-30T00:00:00.000Z"},{"id":"f555b72c-8c51-41dc-a502-d5fe1de3fccb","collectionExerciseId":"b90f0a3f-ffeb-47af-a18f-bdf15ba1d489","tag":"exercise_end","timestamp":"2021-02-28T00:00:00.000Z"},{"id":"343ded7a-d574-445c-ad1a-50b049b3b556","collectionExerciseId":"e5f41aa9-38e1-4ffd-9f27-d07e27dc21f6","tag":"ref_period_start","timestamp":"2019-07-01T00:00:00.000Z"},{"id":"ceaba81e-edda-43ed-9ad3-b16e353f4549","collectionExerciseId":"ebc6a21d-d7b6-4f80-b414-b05ad6f34617","tag":"exercise_end","timestamp":"2020-11-30T00:00:00.000Z"},{"id":"a7ab5a0d-1de0-4a3b-ac85-167d4dd7f9fb","collectionExerciseId":"a5ee6624-cdd0-4d47-8bee-1f4a548887d7","tag":"exercise_end","timestamp":"2020-04-30T00:00:00.000Z"},{"id":"39eae1c0-6210-4667-9402-8aaf723b1b6b","collectionExerciseId":"addebb2d-5632-4d9d-95f4-d3e5f9acb0e9","tag":"return_by","timestamp":"2019-11-08T00:00:00.000Z"},{"id":"5880adc6-71cf-440a-b212-a1e00222aeb0","collectionExerciseId":"81498954-b626-4aa7-b329-2bdb2749fd59","tag":"go_live","timestamp":"2019-11-01T00:00:00.000Z"},{"id":"e1fa2d39-bc34-44e2-ac53-c2c93ab7b819","collectionExerciseId":"e037a9d6-62a8-459b-8252-1625de2eda34","tag":"exercise_end","timestamp":"2021-05-31T00:00:00.000Z"},{"id":"acdf9321-a7e5-4c30-a49b-a4bafc11927e","collectionExerciseId":"3b7811ed-1dee-41f6-92ba-6d95855df13f","tag":"exercise_end","timestamp":"2020-09-30T00:00:00.000Z"},{"id":"9c8d39a9-9d1f-45cf-94a6-b97bec6ec201","collectionExerciseId":"fcc7b1af-715b-4bd2-8892-05cfc2825aa0","tag":"exercise_end","timestamp":"2020-03-31T00:00:00.000Z"},{"id":"8787b825-f5ee-4393-aef2-d35460c1edf4","collectionExerciseId":"f5fe1edb-c927-4ad6-b2f4-abca7cc146f4","tag":"ref_period_end","timestamp":"2019-08-31T00:00:00.000Z"},{"id":"0cdcee50-fa45-4e91-8cf7-c857ba756530","collectionExerciseId":"81d7b772-eff3-47b3-9029-2c0383657f65","tag":"return_by","timestamp":"2019-12-10T00:00:00.000Z"},{"id":"3cc0ff06-430b-42ba-8644-cde0bba59db2","collectionExerciseId":"98ae1930-1e14-4267-8c0b-1bee9715b0cd","tag":"exercise_end","timestamp":"2021-10-31T00:00:00.000Z"},{"id":"72dff532-7b6a-4946-93be-a2609ef56b81","collectionExerciseId":"395c9c00-010d-4d7e-aa52-56bb53639215","tag":"exercise_end","timestamp":"2020-09-30T00:00:00.000Z"},{"id":"a5aa0d60-8c06-41e6-bf88-e73fedd706cd","collectionExerciseId":"f5fe1edb-c927-4ad6-b2f4-abca7cc146f4","tag":"exercise_end","timestamp":"2021-10-31T00:00:00.000Z"},{"id":"09a5ea04-bfcb-4a16-bc31-b4b1cd251ae9","collectionExerciseId":"625def2f-2111-49e6-87d7-eeb1709ab664","tag":"reminder3","timestamp":"2019-08-06T00:00:00.000Z"},{"id":"57d76250-03ef-4183-a0cb-d92987a1fcdc","collectionExerciseId":"907b6d4a-d5bf-42b8-ab16-8ec901bd0a72","tag":"exercise_end","timestamp":"2020-10-31T00:00:00.000Z"},{"id":"85d281eb-5fcc-4ba6-af39-212be48d0f62","collectionExerciseId":"9d2d97cf-edf6-407c-9e20-fa980bab3a7d","tag":"go_live","timestamp":"2019-10-23T00:00:00.000Z"},{"id":"4a5f617e-50d7-43dd-9ede-794b42ff0d10","collectionExerciseId":"1141b6f1-6f8e-4920-af0d-88a6875eb832","tag":"return_by","timestamp":"2020-01-09T00:00:00.000Z"},{"id":"7693afc1-047f-4bcf-ac13-da690e98a746","collectionExerciseId":"cc9afa9c-6d49-4bdc-8d05-e5b31a55fb36","tag":"reminder","timestamp":"2019-10-08T00:00:00.000Z"},{"id":"3d0355d9-79f5-4e04-b370-807feda7cbcf","collectionExerciseId":"fe70c082-ec8c-4a64-82dc-7d85e1c18364","tag":"employment","timestamp":"2019-12-14T00:00:00.000Z"},{"id":"6b5658c8-e319-4ed0-afbd-bada6d065247","collectionExerciseId":"d471c85d-59ff-469c-a263-5c9951207cb3","tag":"return_by","timestamp":"2020-01-15T00:00:00.000Z"},{"id":"6bb39abc-1500-4ab1-b1ab-764422da036d","collectionExerciseId":"060f666e-9ee7-40ea-a966-9ec905348472","tag":"exercise_end","timestamp":"2020-02-28T00:00:00.000Z"},{"id":"6363047e-94cb-4a57-a5b3-8ba0842d3ecf","collectionExerciseId":"1505c106-5d88-4976-b8c1-85c15c545465","tag":"mps","timestamp":"2019-11-15T00:00:00.000Z"},{"id":"e7115569-2efd-44da-ae66-9180f9d46b8f","collectionExerciseId":"85ee3ac9-4523-4e75-aedf-aa5fd7c7a7c9","tag":"ref_period_start","timestamp":"2019-09-01T00:00:00.000Z"},{"id":"fa713584-6e7f-4a96-8069-2d9e136a3550","collectionExerciseId":"9dcbb243-bf1f-4b63-b852-aa59a4c1bbb8","tag":"exercise_end","timestamp":"2021-11-30T00:00:00.000Z"},{"id":"179b24f1-032b-4235-a7b3-ca4dd4efbdfd","collectionExerciseId":"8ad14503-49bd-4d30-9c74-604212429788","tag":"ref_period_end","timestamp":"2019-07-31T00:00:00.000Z"},{"id":"300aaa57-8998-4cb5-80ed-2b9e5474c13e","collectionExerciseId":"631383db-5076-4de9-ae5a-96a30ad6ea5a","tag":"mps","timestamp":"2019-12-07T00:00:00.000Z"},{"id":"b8801f06-79a9-41c7-b072-f1e011a8e8e8","collectionExerciseId":"32939223-f0db-4883-96f5-1b59aec81bdd","tag":"exercise_end","timestamp":"2021-05-31T00:00:00.000Z"},{"id":"99346ec8-0788-4ba9-be51-cd4d7af8451f","collectionExerciseId":"b7160465-4b49-4dc5-87cf-1f3e1cdd7cf1","tag":"go_live","timestamp":"2019-06-21T00:00:00.000Z"},{"id":"5597e42e-3290-4af1-91d4-d1e6ee9d9ef8","collectionExerciseId":"5cf8bc26-1ead-4495-b5fc-a5c89ce5e4e9","tag":"exercise_end","timestamp":"2020-11-30T00:00:00.000Z"},{"id":"eca19738-2bcf-4195-a056-a92425185513","collectionExerciseId":"9d2d97cf-edf6-407c-9e20-fa980bab3a7d","tag":"ref_period_end","timestamp":"2019-10-27T00:00:00.000Z"},{"id":"f5d62af8-3fe9-484d-8a1a-8c87ac5317b5","collectionExerciseId":"f6d849d7-8f14-43f1-9291-0acf32477539","tag":"return_by","timestamp":"2019-09-10T00:00:00.000Z"},{"id":"4f351e7b-d265-4b6b-a130-9d5c48d94e31","collectionExerciseId":"04384f50-e1bc-4ce3-8eab-9949283224b1","tag":"mps","timestamp":"2019-06-15T00:00:00.000Z"},{"id":"73398128-bb10-42c6-9739-b6c613ccf107","collectionExerciseId":"8ad14503-49bd-4d30-9c74-604212429788","tag":"go_live","timestamp":"2019-07-26T00:00:00.000Z"},{"id":"7080750a-0a4b-4bdd-92d6-92f0e001e3a4","collectionExerciseId":"56592cb2-a8b3-4b51-9dda-619c952c46a7","tag":"exercise_end","timestamp":"2021-10-30T00:00:00.000Z"},{"id":"3f178500-aa3d-4b5f-91e0-855eeda49a3a","collectionExerciseId":"1ad78cb0-b4ff-454b-b807-c03351963df9","tag":"return_by","timestamp":"2019-07-06T00:00:00.000Z"},{"id":"f44d3a8a-a4f9-4c63-9d88-44ea2bc22dc6","collectionExerciseId":"ebef3ac8-58d4-45e1-8ff9-9060e2b326c5","tag":"exercise_end","timestamp":"2021-11-30T00:00:00.000Z"},{"id":"b6b68e4a-2dca-4b35-baa7-17e2ed117233","collectionExerciseId":"13848243-ff1e-4f8e-9edc-5771be22953a","tag":"return_by","timestamp":"2019-12-10T00:00:00.000Z"},{"id":"f3d19bcb-d7fd-4532-9932-534463c42325","collectionExerciseId":"cc9afa9c-6d49-4bdc-8d05-e5b31a55fb36","tag":"ref_period_end","timestamp":"2019-09-29T00:00:00.000Z"},{"id":"8eda2c7a-bf01-46fd-8375-df5844fae63c","collectionExerciseId":"5c3afe3c-5504-4d51-91b5-9ff7fabf7d19","tag":"exercise_end","timestamp":"2021-08-31T00:00:00.000Z"},{"id":"d8a3000b-83e8-4040-a65d-0765fe31a8d6","collectionExerciseId":"1a86c3cd-cb6e-4ef3-8ec9-8448438f4937","tag":"exercise_end","timestamp":"2021-06-30T00:00:00.000Z"},{"id":"f7ea9d65-4b4c-4422-ad24-a58549d33321","collectionExerciseId":"9fd9158a-e47b-4c96-bc91-c62d4bce1947","tag":"reminder","timestamp":"2020-01-14T00:00:00.000Z"},{"id":"5b76f12b-9271-4966-a001-9593cf23a65a","collectionExerciseId":"0ae2a344-9048-482b-abb2-d56eec545e3e","tag":"mps","timestamp":"2019-09-18T00:00:00.000Z"},{"id":"0cf3fcbf-5fde-424b-a2dc-7346e47d17a1","collectionExerciseId":"6ab11e56-9b06-4793-906d-83750cc035c4","tag":"ref_period_end","timestamp":"2019-05-31T00:00:00.000Z"},{"id":"4675e4d9-5bee-48fd-9675-c01fbfa7969c","collectionExerciseId":"6e466abd-3c6e-4093-8b68-8159778b02ec","tag":"ref_period_start","timestamp":"2019-05-27T00:00:00.000Z"},{"id":"7ac6da1e-81a0-4eb1-a5dd-f4fef69b22b2","collectionExerciseId":"bfd2da8a-d189-47e4-a556-bf1afec7efce","tag":"exercise_end","timestamp":"2020-12-31T00:00:00.000Z"},{"id":"e491d07f-42ac-4dc3-bd75-e6400d168c3c","collectionExerciseId":"74cc3c63-d4c7-42ca-b79a-3897f134dbe2","tag":"exercise_end","timestamp":"2020-03-31T00:00:00.000Z"},{"id":"c6439517-625b-4b0d-ad5f-ccfd7165302c","collectionExerciseId":"cfc81f0c-ce96-48ce-8eef-fbeacd46ae48","tag":"exercise_end","timestamp":"2021-06-30T00:00:00.000Z"},{"id":"8f407982-e227-43a8-91a8-7ca3516b5e8b","collectionExerciseId":"f9091290-d22f-4c1f-b80a-5bf225d00b5e","tag":"reminder","timestamp":"2019-06-12T00:00:00.000Z"},{"id":"76bb64d8-6b8a-4570-b8ee-3138261e4702","collectionExerciseId":"6044b444-92e6-4e40-9fbc-e0cb718a077b","tag":"exercise_end","timestamp":"2020-11-30T00:00:00.000Z"},{"id":"5763ac71-b7f9-49c2-9cc9-54fc10a733fa","collectionExerciseId":"f6d849d7-8f14-43f1-9291-0acf32477539","tag":"go_live","timestamp":"2019-09-03T00:00:00.000Z"},{"id":"4cefc22c-67a5-488e-816f-3029aeb502be","collectionExerciseId":"9fd9158a-e47b-4c96-bc91-c62d4bce1947","tag":"go_live","timestamp":"2019-12-17T00:00:00.000Z"},{"id":"730d8400-de06-4af8-983a-a218df6fb81a","collectionExerciseId":"1ad78cb0-b4ff-454b-b807-c03351963df9","tag":"go_live","timestamp":"2019-06-19T00:00:00.000Z"},{"id":"bc42f28c-13b6-47a4-8227-40a2944011f9","collectionExerciseId":"b3145fb7-3eaa-42ef-8873-983dfafd4e54","tag":"reminder","timestamp":"2019-06-12T00:00:00.000Z"},{"id":"5d4913ec-c539-4036-a26a-558b8d0c92c0","collectionExerciseId":"0529a56c-d852-4935-8a1e-316db9d928fe","tag":"mps","timestamp":"2019-09-14T00:00:00.000Z"},{"id":"da368c15-3781-4c5e-b02e-a1ad02d8843b","collectionExerciseId":"ebef3ac8-58d4-45e1-8ff9-9060e2b326c5","tag":"ref_period_start","timestamp":"2019-07-01T00:00:00.000Z"},{"id":"5068d752-da06-43a0-818d-a368a7778537","collectionExerciseId":"e518281c-7e45-4993-834e-c012d26b6db8","tag":"exercise_end","timestamp":"2020-05-31T00:00:00.000Z"},{"id":"9334b4e9-fc57-4d19-ba66-ca1c27f8a14f","collectionExerciseId":"dc08941f-7cb1-4049-bf09-92ab23d10784","tag":"mps","timestamp":"2019-06-26T00:00:00.000Z"},{"id":"f091996d-9a19-44dd-912f-a4482c7af50d","collectionExerciseId":"cfc81f0c-ce96-48ce-8eef-fbeacd46ae48","tag":"return_by","timestamp":"2019-06-08T00:00:00.000Z"},{"id":"d4fd8d51-a5d8-44e2-b3a3-944fab41fa1f","collectionExerciseId":"584b807a-9354-4341-8944-450ff2215eda","tag":"exercise_end","timestamp":"2021-05-31T00:00:00.000Z"},{"id":"08afafd3-13f3-4f9a-9dc3-faa78d617edb","collectionExerciseId":"b9295c0a-fdd8-46ee-9fcb-b9a61f1068c0","tag":"exercise_end","timestamp":"2020-02-28T00:00:00.000Z"},{"id":"f1339b05-f716-4873-83c8-6d0d2aad02e4","collectionExerciseId":"6e466abd-3c6e-4093-8b68-8159778b02ec","tag":"go_live","timestamp":"2019-06-26T00:00:00.000Z"},{"id":"11c131ed-d929-4c2c-9130-50bc3a406e27","collectionExerciseId":"54e31437-c6ae-41c1-893b-ae7224c8e9f9","tag":"exercise_end","timestamp":"2021-06-30T00:00:00.000Z"},{"id":"5c732a6e-c456-4f22-98b5-464bb1772ed0","collectionExerciseId":"c34bae50-4b61-4f22-9bf0-00245d739c26","tag":"reminder","timestamp":"2020-01-08T00:00:00.000Z"},{"id":"d7abd852-70be-4e2b-8223-4b6643f08174","collectionExerciseId":"ebef3ac8-58d4-45e1-8ff9-9060e2b326c5","tag":"reminder2","timestamp":"2019-11-07T00:00:00.000Z"},{"id":"ceff61f0-9ff4-467a-a561-c97df4e55c2b","collectionExerciseId":"5e054da8-6a7f-45ac-b84e-1a53ed574e82","tag":"exercise_end","timestamp":"2021-05-31T00:00:00.000Z"},{"id":"a96a4fc7-ac7f-4ba4-8c40-bc3ba096572b","collectionExerciseId":"1505c106-5d88-4976-b8c1-85c15c545465","tag":"return_by","timestamp":"2019-12-01T00:00:00.000Z"},{"id":"176fd79f-a525-4c45-87c8-2d7e1435238e","collectionExerciseId":"d471c85d-59ff-469c-a263-5c9951207cb3","tag":"exercise_end","timestamp":"2022-02-28T00:00:00.000Z"},{"id":"10d1f8fe-bfa5-442b-a1bf-d5c26530784f","collectionExerciseId":"544cc93b-2a69-4b40-aab4-b8e2a6ee4871","tag":"exercise_end","timestamp":"2021-02-28T00:00:00.000Z"},{"id":"d11bbaa0-a7f7-4540-acf0-52ba79b79d38","collectionExerciseId":"c75bb593-db4b-4dbf-92eb-55d0d085a7e2","tag":"reminder","timestamp":"2020-01-15T00:00:00.000Z"},{"id":"4eb25990-edc3-485a-b732-992c593f1fc7","collectionExerciseId":"4a3360ce-9a0e-4ed2-834c-dbde17640957","tag":"ref_period_start","timestamp":"2019-07-29T00:00:00.000Z"},{"id":"82818018-e0ab-4140-bebc-68fa7a927b4f","collectionExerciseId":"625def2f-2111-49e6-87d7-eeb1709ab664","tag":"exercise_end","timestamp":"2021-07-31T00:00:00.000Z"},{"id":"3b27df6b-b8d2-47b0-97ea-f34fbf276692","collectionExerciseId":"710399cd-7758-419f-8210-8cae845884bf","tag":"go_live","timestamp":"2019-10-22T00:00:00.000Z"},{"id":"e364f55d-139f-4fe2-990b-a77d805e06ec","collectionExerciseId":"bba33485-d677-4e74-9593-9798a095f6eb","tag":"go_live","timestamp":"2019-12-14T00:00:00.000Z"},{"id":"c5af7008-a798-4d16-96eb-b9a557aca995","collectionExerciseId":"d93a5bc6-edd1-4ed1-b2fc-e1e417f4b493","tag":"ref_period_start","timestamp":"2019-06-01T00:00:00.000Z"},{"id":"7ff90d80-3749-4d46-bcc9-813db794e9a1","collectionExerciseId":"5c2c80b1-7631-4a53-a023-d4529f1af620","tag":"exercise_end","timestamp":"2021-05-31T00:00:00.000Z"},{"id":"912683c2-54d4-4461-baf0-2531298d94d3","collectionExerciseId":"98ae1930-1e14-4267-8c0b-1bee9715b0cd","tag":"return_by","timestamp":"2019-10-09T00:00:00.000Z"},{"id":"81233ed2-b73e-4d32-b34a-ab3e46483088","collectionExerciseId":"56592cb2-a8b3-4b51-9dda-619c952c46a7","tag":"reminder3","timestamp":"2019-12-31T00:00:00.000Z"},{"id":"85b660b2-5231-4a14-9924-27089f0d506a","collectionExerciseId":"321105b1-39c1-4965-b0e5-4f39e8634bb4","tag":"exercise_end","timestamp":"2021-04-30T00:00:00.000Z"},{"id":"08c4b970-3d95-4eb0-b732-6ed55728b764","collectionExerciseId":"d4445b0c-7e61-4b5d-afbc-4eafa6b7d9de","tag":"go_live","timestamp":"2019-11-26T00:00:00.000Z"},{"id":"3d420b4a-c628-4fdd-8478-68b76c094974","collectionExerciseId":"b89ddbfb-884c-4de6-8324-2b55e51cf70b","tag":"mps","timestamp":"2019-06-19T00:00:00.000Z"},{"id":"739e9487-179c-43af-9609-3c0e9f9a9e1b","collectionExerciseId":"fa4a2d27-6c9c-4b20-8489-2706a4a6d6ff","tag":"reminder","timestamp":"2019-06-04T00:00:00.000Z"},{"id":"f901e53d-a32f-4f2f-9466-3648b7f71e9a","collectionExerciseId":"7e12ac56-e3ee-4e2a-8693-be7bf2e009a1","tag":"return_by","timestamp":"2019-06-07T00:00:00.000Z"},{"id":"2302af9a-5706-4663-81b5-14c7e5407f59","collectionExerciseId":"b89ddbfb-884c-4de6-8324-2b55e51cf70b","tag":"go_live","timestamp":"2019-06-25T00:00:00.000Z"},{"id":"f19e7f53-32f5-473b-ac53-c72a507d8eee","collectionExerciseId":"687cf71f-4ad3-4bd2-b054-5cfafddb109d","tag":"go_live","timestamp":"2019-12-11T00:00:00.000Z"},{"id":"b575c02d-98af-4886-9b10-c75042eff09e","collectionExerciseId":"9d2d97cf-edf6-407c-9e20-fa980bab3a7d","tag":"ref_period_start","timestamp":"2019-09-30T00:00:00.000Z"},{"id":"f4cae8b8-b5ef-445f-bb3a-32ef98fd7c45","collectionExerciseId":"66e98d19-4a60-4a3d-a4d0-951f0020dd65","tag":"ref_period_end","timestamp":"2019-07-31T00:00:00.000Z"},{"id":"ea38009d-2c30-44f6-8283-8d8a14760173","collectionExerciseId":"9ae470fa-4e22-490a-add4-83b04ce9d6c2","tag":"return_by","timestamp":"2019-12-07T00:00:00.000Z"},{"id":"3058daa4-a45d-47af-a2b6-26e8ef027f93","collectionExerciseId":"1505c106-5d88-4976-b8c1-85c15c545465","tag":"exercise_end","timestamp":"2021-12-31T00:00:00.000Z"},{"id":"49ee4d0d-3423-4bb7-b826-225770e99c06","collectionExerciseId":"b7160465-4b49-4dc5-87cf-1f3e1cdd7cf1","tag":"mps","timestamp":"2019-06-15T00:00:00.000Z"},{"id":"739c56dd-9f8d-40ff-abb0-84984d55ac8c","collectionExerciseId":"04384f50-e1bc-4ce3-8eab-9949283224b1","tag":"reminder2","timestamp":"2019-08-08T00:00:00.000Z"},{"id":"0cd6ab9e-649f-4721-a89c-5c842f90efb9","collectionExerciseId":"ecb21698-cbf6-443f-a628-daeb777e653c","tag":"exercise_end","timestamp":"2021-01-31T00:00:00.000Z"},{"id":"f02d55a9-fef6-4871-839e-95ea07968278","collectionExerciseId":"75dbcd71-a526-455b-9fbc-3a05a4bdfa5e","tag":"return_by","timestamp":"2019-10-08T00:00:00.000Z"},{"id":"d8c24ce5-70c5-4c66-af24-4bb373611aba","collectionExerciseId":"addebb2d-5632-4d9d-95f4-d3e5f9acb0e9","tag":"exercise_end","timestamp":"2021-12-31T00:00:00.000Z"},{"id":"03c4d021-7ecf-4993-9d8d-7f3f75b7cb34","collectionExerciseId":"9d2d97cf-edf6-407c-9e20-fa980bab3a7d","tag":"reminder","timestamp":"2019-11-05T00:00:00.000Z"},{"id":"47a79223-4492-4d72-943f-632de8756b4e","collectionExerciseId":"d6b40c2e-dddc-4805-a337-7b4b7b0e1870","tag":"exercise_end","timestamp":"2020-11-30T00:00:00.000Z"},{"id":"5f76bdbc-92d2-4308-ade3-060947792200","collectionExerciseId":"c75bb593-db4b-4dbf-92eb-55d0d085a7e2","tag":"ref_period_end","timestamp":"2019-12-31T00:00:00.000Z"},{"id":"8b3308f4-17df-47a6-8f9f-a305cc7f968a","collectionExerciseId":"f5cbc68f-3c19-4d9f-b8d1-84c266ccfbd2","tag":"exercise_end","timestamp":"2022-01-31T00:00:00.000Z"},{"id":"e8ba43fd-181a-4e49-8852-92e7e086dcd3","collectionExerciseId":"24d5177e-d523-4405-aa9d-0084889dd9d2","tag":"exercise_end","timestamp":"2020-12-31T00:00:00.000Z"},{"id":"6bcfa364-cee0-4546-bdc8-9847fd1e6422","collectionExerciseId":"6e466abd-3c6e-4093-8b68-8159778b02ec","tag":"mps","timestamp":"2019-06-21T00:00:00.000Z"},{"id":"637f779e-c9aa-4866-9470-4baebabdf551","collectionExerciseId":"c903ef97-e28b-44b2-8414-b01a77857b26","tag":"exercise_end","timestamp":"2021-02-28T00:00:00.000Z"},{"id":"11d8efd5-201a-4a15-a370-9d75ce1adf07","collectionExerciseId":"c34bae50-4b61-4f22-9bf0-00245d739c26","tag":"ref_period_end","timestamp":"2019-12-31T00:00:00.000Z"},{"id":"036c5034-8b88-4ba1-be09-1d2dea0d815e","collectionExerciseId":"13a231b2-ad29-47f8-b4fc-99b17742a161","tag":"exercise_end","timestamp":"2020-11-30T00:00:00.000Z"},{"id":"a0cb3b5b-3b2b-468c-9a7f-51d9f8b09a0f","collectionExerciseId":"1ad78cb0-b4ff-454b-b807-c03351963df9","tag":"exercise_end","timestamp":"2021-08-31T00:00:00.000Z"},{"id":"8000dd03-1b38-4918-aa0d-ae8ac5c5aa5e","collectionExerciseId":"cfc81f0c-ce96-48ce-8eef-fbeacd46ae48","tag":"mps","timestamp":"2019-05-25T00:00:00.000Z"},{"id":"20eb0144-7f3a-470c-a728-ef2243d3c2b9","collectionExerciseId":"89e64d0b-cca0-459d-92db-6ce77aa9aa6e","tag":"exercise_end","timestamp":"2020-06-30T00:00:00.000Z"},{"id":"b48d5e1f-f617-4b13-9390-0ef1e1930f0a","collectionExerciseId":"26720fb9-2164-48b6-8a8e-0fbfbcbfffa6","tag":"exercise_end","timestamp":"2020-11-30T00:00:00.000Z"},{"id":"2108b8c1-1572-4657-83b5-d3b15933efde","collectionExerciseId":"04384f50-e1bc-4ce3-8eab-9949283224b1","tag":"reminder","timestamp":"2019-07-19T00:00:00.000Z"},{"id":"076c4f24-1faa-4d7f-8148-dd5987fac527","collectionExerciseId":"2e9bf8a7-2eda-48e2-8150-cfb64a99992a","tag":"return_by","timestamp":"2019-09-07T00:00:00.000Z"},{"id":"243dcce7-110c-4684-8b91-0c595db5da62","collectionExerciseId":"2e9bf8a7-2eda-48e2-8150-cfb64a99992a","tag":"mps","timestamp":"2019-08-17T00:00:00.000Z"},{"id":"d4fb0965-da6f-4858-9c9a-e32866248cc1","collectionExerciseId":"631383db-5076-4de9-ae5a-96a30ad6ea5a","tag":"exercise_end","timestamp":"2022-02-28T00:00:00.000Z"},{"id":"9e50e15e-4e70-486b-b7d9-f91077e76053","collectionExerciseId":"fe70c082-ec8c-4a64-82dc-7d85e1c18364","tag":"ref_period_start","timestamp":"2019-11-25T00:00:00.000Z"},{"id":"a8934c82-de8f-4f43-b52d-0236030742db","collectionExerciseId":"4a3360ce-9a0e-4ed2-834c-dbde17640957","tag":"return_by","timestamp":"2019-09-01T00:00:00.000Z"},{"id":"1050aa2c-137f-494b-a13d-ec4d2676642b","collectionExerciseId":"33ea4efa-6d7d-4592-84a5-792b279cc0a3","tag":"exercise_end","timestamp":"2020-12-31T00:00:00.000Z"},{"id":"6bea8ec3-7163-4712-89fe-10a56ba21920","collectionExerciseId":"fe70c082-ec8c-4a64-82dc-7d85e1c18364","tag":"exercise_end","timestamp":"2022-01-31T00:00:00.000Z"},{"id":"d1824d1b-2c13-4980-9470-3a516cebb892","collectionExerciseId":"c75bb593-db4b-4dbf-92eb-55d0d085a7e2","tag":"ref_period_start","timestamp":"2019-10-01T00:00:00.000Z"},{"id":"7c65f74b-d95d-4707-8be6-b8de406f5055","collectionExerciseId":"b7160465-4b49-4dc5-87cf-1f3e1cdd7cf1","tag":"return_by","timestamp":"2019-07-09T00:00:00.000Z"},{"id":"9b5cf854-8007-46db-8a75-7f662df7679b","collectionExerciseId":"87612d73-49a2-46ac-b939-cac63a33ca92","tag":"exercise_end","timestamp":"2021-08-31T00:00:00.000Z"},{"id":"8fcd0b0b-ad68-4c50-8211-a8b7a7821656","collectionExerciseId":"4a3360ce-9a0e-4ed2-834c-dbde17640957","tag":"reminder","timestamp":"2019-09-03T00:00:00.000Z"},{"id":"d12e93eb-6d73-4bfd-ac81-a202aad8eb5e","collectionExerciseId":"9fd9158a-e47b-4c96-bc91-c62d4bce1947","tag":"mps","timestamp":"2019-12-12T00:00:00.000Z"},{"id":"cf7107b7-f14d-4a7d-b2b5-bcf34c11c4f4","collectionExerciseId":"d471c85d-59ff-469c-a263-5c9951207cb3","tag":"go_live","timestamp":"2019-12-14T00:00:00.000Z"},{"id":"82be8539-e2b5-4ea4-bddd-a8e6ac53e061","collectionExerciseId":"f5cbc68f-3c19-4d9f-b8d1-84c266ccfbd2","tag":"mps","timestamp":"2019-12-27T00:00:00.000Z"},{"id":"ba65f990-86d6-40c7-b21f-670bbdc4d302","collectionExerciseId":"5c3afe3c-5504-4d51-91b5-9ff7fabf7d19","tag":"go_live","timestamp":"2019-07-24T00:00:00.000Z"},{"id":"30675144-8823-4243-8919-ba5a1d0fabb0","collectionExerciseId":"04384f50-e1bc-4ce3-8eab-9949283224b1","tag":"ref_period_end","timestamp":"2019-06-30T00:00:00.000Z"},{"id":"8c7446e8-1e18-459b-b6ec-23bcb8408f4a","collectionExerciseId":"43c51695-4a01-47e1-afb3-bb7ec1dbe4ea","tag":"exercise_end","timestamp":"2020-08-31T00:00:00.000Z"},{"id":"7c43560b-32fb-4e4a-abe5-8949008b5f1b","collectionExerciseId":"06b30955-6a9c-4a0c-b86f-8a94778a943c","tag":"go_live","timestamp":"2019-12-03T00:00:00.000Z"},{"id":"4f6f2e96-8914-48d1-9486-1ed0e21a4967","collectionExerciseId":"87612d73-49a2-46ac-b939-cac63a33ca92","tag":"mps","timestamp":"2019-07-18T00:00:00.000Z"},{"id":"a7996f60-8bdc-4633-8ef4-37cf3c0a287c","collectionExerciseId":"6ec325ba-6787-442e-8e7c-7b85a231aebb","tag":"reminder2","timestamp":"2019-08-08T00:00:00.000Z"},{"id":"0d70b6c1-b4db-4c73-8531-8ba78be5a370","collectionExerciseId":"7e12ac56-e3ee-4e2a-8693-be7bf2e009a1","tag":"ref_period_end","timestamp":"2019-05-31T00:00:00.000Z"},{"id":"a4c803f5-fa66-43a2-84e0-3a7606abebbe","collectionExerciseId":"bb8e8f22-d26f-44f8-b70c-7d646768b78b","tag":"go_live","timestamp":"2019-10-25T00:00:00.000Z"},{"id":"986cdeb1-46bd-4428-9f76-c7896892736d","collectionExerciseId":"f9091290-d22f-4c1f-b80a-5bf225d00b5e","tag":"reminder2","timestamp":"2019-07-03T00:00:00.000Z"},{"id":"6903e7e7-b282-477e-808f-7c8a04839f78","collectionExerciseId":"470488db-b65a-45f0-a3c1-34e4f2edef9a","tag":"exercise_end","timestamp":"2021-04-30T00:00:00.000Z"},{"id":"433f3bad-f198-4977-91ad-485ffdf8c139","collectionExerciseId":"0ae2a344-9048-482b-abb2-d56eec545e3e","tag":"go_live","timestamp":"2019-09-24T00:00:00.000Z"},{"id":"4f42361d-2cc3-4031-8615-d4b3d2e43c51","collectionExerciseId":"56592cb2-a8b3-4b51-9dda-619c952c46a7","tag":"go_live","timestamp":"2019-12-17T00:00:00.000Z"},{"id":"c91a7059-66fa-4b9f-a6c7-e90c4a1dab7e","collectionExerciseId":"d93a5bc6-edd1-4ed1-b2fc-e1e417f4b493","tag":"return_by","timestamp":"2019-07-09T00:00:00.000Z"},{"id":"37f43e47-bf35-4cf4-aedf-0dbb5483d089","collectionExerciseId":"d93a5bc6-edd1-4ed1-b2fc-e1e417f4b493","tag":"mps","timestamp":"2019-06-21T00:00:00.000Z"},{"id":"a4876c9a-8c6d-495f-a00c-4f3447136bee","collectionExerciseId":"0529a56c-d852-4935-8a1e-316db9d928fe","tag":"ref_period_end","timestamp":"2019-09-30T00:00:00.000Z"},{"id":"64d9ac28-ef7e-4014-a49e-1bb8d9df5980","collectionExerciseId":"b3145fb7-3eaa-42ef-8873-983dfafd4e54","tag":"exercise_end","timestamp":"2021-07-31T00:00:00.000Z"},{"id":"6debef95-6155-4a52-b570-5434d365174f","collectionExerciseId":"4388e326-3225-49c8-889f-1a18c58da3d6","tag":"mps","timestamp":"2019-08-16T00:00:00.000Z"},{"id":"549b4c26-436c-49a0-9108-013ca3df9e88","collectionExerciseId":"9dcbb243-bf1f-4b63-b852-aa59a4c1bbb8","tag":"go_live","timestamp":"2019-09-18T00:00:00.000Z"},{"id":"feeef190-ba27-4321-9601-c628bd17f85c","collectionExerciseId":"4388e326-3225-49c8-889f-1a18c58da3d6","tag":"ref_period_start","timestamp":"2019-08-01T00:00:00.000Z"},{"id":"4f7b5b94-89a0-46a8-9aa2-7f6913ce39e1","collectionExerciseId":"6ec325ba-6787-442e-8e7c-7b85a231aebb","tag":"ref_period_end","timestamp":"2019-06-30T00:00:00.000Z"},{"id":"9a474ebb-4339-4af0-8b1c-7b78f1c8c6f6","collectionExerciseId":"9ae470fa-4e22-490a-add4-83b04ce9d6c2","tag":"exercise_end","timestamp":"2022-01-31T00:00:00.000Z"},{"id":"444860d5-5f5e-4766-b8e0-c33f585d0574","collectionExerciseId":"ee90fce9-afb1-4f41-9419-5bc1c52902c2","tag":"exercise_end","timestamp":"2020-04-30T00:00:00.000Z"},{"id":"10e692a3-76c2-40fd-850b-8537392ec912","collectionExerciseId":"98ae1930-1e14-4267-8c0b-1bee9715b0cd","tag":"mps","timestamp":"2019-09-18T00:00:00.000Z"},{"id":"000cbefa-6dbe-4112-8c48-9863fb7e9ff9","collectionExerciseId":"fa4a2d27-6c9c-4b20-8489-2706a4a6d6ff","tag":"return_by","timestamp":"2019-06-02T00:00:00.000Z"},{"id":"db323fac-0d61-4ec0-a55d-2e79c2ea527b","collectionExerciseId":"631383db-5076-4de9-ae5a-96a30ad6ea5a","tag":"go_live","timestamp":"2019-12-14T00:00:00.000Z"},{"id":"098b067e-3600-423d-abc3-77170760b9f1","collectionExerciseId":"addebb2d-5632-4d9d-95f4-d3e5f9acb0e9","tag":"go_live","timestamp":"2019-10-25T00:00:00.000Z"},{"id":"b6ec408a-8e4c-48c8-b6f6-774ac3dee6df","collectionExerciseId":"98ae1930-1e14-4267-8c0b-1bee9715b0cd","tag":"ref_period_start","timestamp":"2019-09-01T00:00:00.000Z"},{"id":"3b15f6a1-0af0-4ada-a0ab-317c587fdbcc","collectionExerciseId":"81d7b772-eff3-47b3-9029-2c0383657f65","tag":"go_live","timestamp":"2019-11-22T00:00:00.000Z"},{"id":"5f4d03bc-7b6d-47b9-b7a4-7e242b6082cd","collectionExerciseId":"0529a56c-d852-4935-8a1e-316db9d928fe","tag":"ref_period_start","timestamp":"2019-07-01T00:00:00.000Z"},{"id":"e54e74dd-1eee-48e3-bd6e-7f1a5236464f","collectionExerciseId":"b5d3f983-1ce7-4c08-a4b1-1a83b645e831","tag":"exercise_end","timestamp":"2020-08-31T00:00:00.000Z"},{"id":"4e326b5a-2314-444e-a0fe-0abd27d4d29d","collectionExerciseId":"1cb4ec16-bde4-4d8f-9237-7614e04d22ae","tag":"exercise_end","timestamp":"2021-11-30T00:00:00.000Z"},{"id":"cbc7cbba-ed32-4ace-bda9-f48faae7d2fd","collectionExerciseId":"b89ddbfb-884c-4de6-8324-2b55e51cf70b","tag":"exercise_end","timestamp":"2021-08-31T00:00:00.000Z"},{"id":"120f6b73-156f-440e-b3c2-d0411b556216","collectionExerciseId":"6e466abd-3c6e-4093-8b68-8159778b02ec","tag":"exercise_end","timestamp":"2021-07-31T00:00:00.000Z"},{"id":"3c2f057e-7627-4e58-91df-0a864613bcee","collectionExerciseId":"f5cbc68f-3c19-4d9f-b8d1-84c266ccfbd2","tag":"return_by","timestamp":"2020-01-09T00:00:00.000Z"},{"id":"8ded4eee-c244-4eb2-bdc6-7cdd1a2c0323","collectionExerciseId":"7e12ac56-e3ee-4e2a-8693-be7bf2e009a1","tag":"reminder","timestamp":"2019-06-12T00:00:00.000Z"},{"id":"ff9ece4b-8ccc-43c4-a9f6-e85ea046d04a","collectionExerciseId":"83e7e9a2-8d51-4068-9574-5b88a8b09df3","tag":"exercise_end","timestamp":"2021-03-31T00:00:00.000Z"},{"id":"c218267f-f923-4964-95b7-cb9210f22915","collectionExerciseId":"1141b6f1-6f8e-4920-af0d-88a6875eb832","tag":"go_live","timestamp":"2019-12-14T00:00:00.000Z"},{"id":"3c105c2e-726a-4a79-8177-29e9d7a8e790","collectionExerciseId":"9d2d97cf-edf6-407c-9e20-fa980bab3a7d","tag":"return_by","timestamp":"2019-11-03T00:00:00.000Z"},{"id":"d4e8e5e7-1e76-4112-bc51-f25050984048","collectionExerciseId":"0529a56c-d852-4935-8a1e-316db9d928fe","tag":"reminder","timestamp":"2019-10-18T00:00:00.000Z"},{"id":"c7937bb1-0d97-4af9-b113-8595a2ca2fd0","collectionExerciseId":"b7160465-4b49-4dc5-87cf-1f3e1cdd7cf1","tag":"exercise_end","timestamp":"2021-07-31T00:00:00.000Z"},{"id":"8945bb5e-e6f9-42a6-b959-9171e57e76a9","collectionExerciseId":"ebef3ac8-58d4-45e1-8ff9-9060e2b326c5","tag":"reminder","timestamp":"2019-10-18T00:00:00.000Z"},{"id":"559ae8f6-0f05-4370-81b1-b80f3ac5d1c3","collectionExerciseId":"9fd9158a-e47b-4c96-bc91-c62d4bce1947","tag":"exercise_end","timestamp":"2022-02-28T00:00:00.000Z"},{"id":"a5cd2914-d991-47d7-bd9f-ccdf3bf3381f","collectionExerciseId":"625def2f-2111-49e6-87d7-eeb1709ab664","tag":"go_live","timestamp":"2019-06-18T00:00:00.000Z"},{"id":"030e2550-6ae1-4709-8159-8ddd18e3e65b","collectionExerciseId":"6910d17b-c515-447a-bdd4-7a6f1a982019","tag":"exercise_end","timestamp":"2021-05-31T00:00:00.000Z"},{"id":"b02bedbe-7f7b-4b43-84df-aa22d15eabdd","collectionExerciseId":"81498954-b626-4aa7-b329-2bdb2749fd59","tag":"exercise_end","timestamp":"2021-11-30T00:00:00.000Z"},{"id":"6aec7ed6-df3e-4d5f-80ce-08b87c96c8c7","collectionExerciseId":"13848243-ff1e-4f8e-9edc-5771be22953a","tag":"ref_period_end","timestamp":"2019-11-30T00:00:00.000Z"},{"id":"623b35aa-1574-4c8d-8aaf-4ebc49db671d","collectionExerciseId":"fe70c082-ec8c-4a64-82dc-7d85e1c18364","tag":"return_by","timestamp":"2020-01-05T00:00:00.000Z"},{"id":"fe1c5ee3-232b-433e-84ef-3cf10489301e","collectionExerciseId":"fe70c082-ec8c-4a64-82dc-7d85e1c18364","tag":"mps","timestamp":"2019-12-07T00:00:00.000Z"},{"id":"9745f7d2-2896-44d8-91c2-2b29db0a03e0","collectionExerciseId":"ad5b7c2b-abb3-4b91-b616-2412f118e7ab","tag":"exercise_end","timestamp":"2020-10-31T00:00:00.000Z"},{"id":"0522faa9-d3e8-4ea4-bf7b-2972180c3973","collectionExerciseId":"2e9bf8a7-2eda-48e2-8150-cfb64a99992a","tag":"ref_period_start","timestamp":"2019-08-01T00:00:00.000Z"},{"id":"83bc21ef-f74d-42c7-a840-c32f9396d26e","collectionExerciseId":"1141b6f1-6f8e-4920-af0d-88a6875eb832","tag":"ref_period_end","timestamp":"2019-12-31T00:00:00.000Z"},{"id":"8a5466d0-3d78-4f69-a78d-47468aade4a8","collectionExerciseId":"6ec325ba-6787-442e-8e7c-7b85a231aebb","tag":"reminder","timestamp":"2019-07-19T00:00:00.000Z"},{"id":"802db947-9d04-45f5-93a5-7668b2ea74e4","collectionExerciseId":"06b30955-6a9c-4a0c-b86f-8a94778a943c","tag":"exercise_end","timestamp":"2021-12-31T00:00:00.000Z"},{"id":"6008dc02-9aa8-4f15-869b-7485f8065452","collectionExerciseId":"e5f41aa9-38e1-4ffd-9f27-d07e27dc21f6","tag":"reminder","timestamp":"2019-08-09T00:00:00.000Z"},{"id":"ed1b6709-b3b6-4191-af08-05093d4e336c","collectionExerciseId":"8ad14503-49bd-4d30-9c74-604212429788","tag":"return_by","timestamp":"2019-08-08T00:00:00.000Z"},{"id":"61727ab6-bd04-4d44-afe4-38559b1b62e8","collectionExerciseId":"81d7b772-eff3-47b3-9029-2c0383657f65","tag":"ref_period_start","timestamp":"2019-11-01T00:00:00.000Z"},{"id":"5d1223e6-7ec2-4cd6-bf8d-e3fa933c8d44","collectionExerciseId":"addebb2d-5632-4d9d-95f4-d3e5f9acb0e9","tag":"ref_period_start","timestamp":"2019-10-01T00:00:00.000Z"},{"id":"c9386446-b050-41fa-bb7e-e4e602e4bb62","collectionExerciseId":"2e9bf8a7-2eda-48e2-8150-cfb64a99992a","tag":"reminder","timestamp":"2019-09-11T00:00:00.000Z"},{"id":"d57e502a-ddae-4d12-a796-ee0d7521469a","collectionExerciseId":"c3128c22-6133-4bef-a5aa-acd0acbd6b49","tag":"exercise_end","timestamp":"2021-05-31T00:00:00.000Z"},{"id":"04544cec-ab79-4328-a63c-3e58f08ca1a9","collectionExerciseId":"3584a6f5-0ca1-423d-8373-1156ed13e76c","tag":"exercise_end","timestamp":"2020-10-31T00:00:00.000Z"},{"id":"4366ad6f-4222-4e34-b783-1114b1f9dcf1","collectionExerciseId":"b7160465-4b49-4dc5-87cf-1f3e1cdd7cf1","tag":"ref_period_start","timestamp":"2019-06-01T00:00:00.000Z"},{"id":"d346507d-499a-4fe9-a3cc-4dcf2717d316","collectionExerciseId":"fe70c082-ec8c-4a64-82dc-7d85e1c18364","tag":"reminder","timestamp":"2020-01-07T00:00:00.000Z"},{"id":"fe973ae4-9d90-4ce5-b46e-75dec1b46fe3","collectionExerciseId":"1cb4ec16-bde4-4d8f-9237-7614e04d22ae","tag":"mps","timestamp":"2019-09-19T00:00:00.000Z"},{"id":"e868ce98-0684-4f65-a58d-231b2308242f","collectionExerciseId":"2e9bf8a7-2eda-48e2-8150-cfb64a99992a","tag":"go_live","timestamp":"2019-08-24T00:00:00.000Z"},{"id":"2c895aff-4dcd-49fa-8dae-47b34202c63f","collectionExerciseId":"f9091290-d22f-4c1f-b80a-5bf225d00b5e","tag":"return_by","timestamp":"2019-06-11T00:00:00.000Z"},{"id":"e18d23dd-48ad-4f55-a365-37afba6043b3","collectionExerciseId":"fe70c082-ec8c-4a64-82dc-7d85e1c18364","tag":"go_live","timestamp":"2019-12-14T00:00:00.000Z"},{"id":"0a1aafc3-f1df-44db-9d4b-51bb62fa41cb","collectionExerciseId":"ac1ca442-79d0-49bc-94c1-08ca8e008747","tag":"exercise_end","timestamp":"2020-06-30T00:00:00.000Z"},{"id":"0cfee54c-bd17-49c2-bdd2-6e23d931b6c8","collectionExerciseId":"85ee3ac9-4523-4e75-aedf-aa5fd7c7a7c9","tag":"ref_period_end","timestamp":"2019-09-30T00:00:00.000Z"},{"id":"76a09f53-0491-467d-b620-388bf0d0f88f","collectionExerciseId":"f4195ba4-f0fd-4c21-9da7-bd222016104f","tag":"return_by","timestamp":"2019-08-08T00:00:00.000Z"},{"id":"db097f15-f996-439a-9e05-80514fbd1e67","collectionExerciseId":"021f6b2d-0e43-4228-b3a7-f02372a8fbd0","tag":"exercise_end","timestamp":"2020-06-30T00:00:00.000Z"},{"id":"7f5142fe-fed4-424c-8887-18bca5ca250f","collectionExerciseId":"ab149dd6-f567-4a40-8cde-2392fc1618c2","tag":"exercise_end","timestamp":"2021-02-28T00:00:00.000Z"},{"id":"9eb7ad6d-0a64-4a5f-8736-548dca7d7dbe","collectionExerciseId":"6ec325ba-6787-442e-8e7c-7b85a231aebb","tag":"go_live","timestamp":"2019-06-21T00:00:00.000Z"},{"id":"606d3cdc-4b00-4e9c-86b7-7e3c33288414","collectionExerciseId":"d471c85d-59ff-469c-a263-5c9951207cb3","tag":"reminder2","timestamp":"2020-02-06T00:00:00.000Z"},{"id":"a6722eff-65fd-481a-b610-a6a64179f95c","collectionExerciseId":"57f79187-99d2-46ca-bc57-933e7b3da36a","tag":"exercise_end","timestamp":"2020-05-31T00:00:00.000Z"},{"id":"ff7b5eec-3775-4ac2-bdcc-b8e17b532c14","collectionExerciseId":"85ee3ac9-4523-4e75-aedf-aa5fd7c7a7c9","tag":"go_live","timestamp":"2019-09-25T00:00:00.000Z"},{"id":"4594bc3c-3c89-42ce-88bd-6202c5725dbb","collectionExerciseId":"6e466abd-3c6e-4093-8b68-8159778b02ec","tag":"return_by","timestamp":"2019-07-07T00:00:00.000Z"},{"id":"8a3a77ca-8c58-4c8f-a9ac-ee075bc9d3b1","collectionExerciseId":"0f5d1cf9-244c-493d-89e0-581847006a53","tag":"exercise_end","timestamp":"2020-11-30T00:00:00.000Z"},{"id":"66f7994c-964c-4054-8c9b-3cc4cc5a278a","collectionExerciseId":"d471c85d-59ff-469c-a263-5c9951207cb3","tag":"ref_period_start","timestamp":"2019-10-01T00:00:00.000Z"},{"id":"5baf0021-4223-47d7-ac83-c7a7826e1765","collectionExerciseId":"77fb2ec2-c4b4-4eff-abfb-1c13823b5fbc","tag":"exercise_end","timestamp":"2020-08-31T00:00:00.000Z"},{"id":"6289a1c7-ac81-44f8-a261-ee6627cc62ce","collectionExerciseId":"2e9bf8a7-2eda-48e2-8150-cfb64a99992a","tag":"ref_period_end","timestamp":"2019-08-31T00:00:00.000Z"},{"id":"1cf2d658-f8a2-44bd-8a5d-e377ac8ef1be","collectionExerciseId":"3d9a5a27-ccb6-4100-bd66-2336bc879840","tag":"exercise_end","timestamp":"2021-03-31T00:00:00.000Z"},{"id":"1a566d77-801d-4a2d-a190-449cfd24fd77","collectionExerciseId":"60af48d9-6a3e-4ed0-893d-b7b5bca71ca2","tag":"exercise_end","timestamp":"2021-01-31T00:00:00.000Z"},{"id":"851d54c8-6882-4e8b-9d81-80ccba72486c","collectionExerciseId":"c75bb593-db4b-4dbf-92eb-55d0d085a7e2","tag":"mps","timestamp":"2019-12-07T00:00:00.000Z"},{"id":"e99f1bd0-c4dc-44a4-926e-7fddec157588","collectionExerciseId":"c34bae50-4b61-4f22-9bf0-00245d739c26","tag":"employment","timestamp":"2019-12-14T00:00:00.000Z"},{"id":"bb00e824-c964-443c-b31b-a064516a30a5","collectionExerciseId":"710399cd-7758-419f-8210-8cae845884bf","tag":"mps","timestamp":"2019-10-17T00:00:00.000Z"},{"id":"48cba6fa-366d-4ce9-b677-4f0ee65181a4","collectionExerciseId":"75dbcd71-a526-455b-9fbc-3a05a4bdfa5e","tag":"mps","timestamp":"2019-09-25T00:00:00.000Z"},{"id":"1d30fa9f-3f89-4aa2-87cc-023f4de0fde6","collectionExerciseId":"8202e7ee-1456-455c-9c03-341a7d5a47b9","tag":"exercise_end","timestamp":"2020-07-31T00:00:00.000Z"},{"id":"360e5f60-6758-43ac-82b1-c5a1f7c1aab3","collectionExerciseId":"cc9afa9c-6d49-4bdc-8d05-e5b31a55fb36","tag":"ref_period_start","timestamp":"2019-08-26T00:00:00.000Z"},{"id":"ba5b4f7c-ca70-4d5d-891f-1f199c0277d3","collectionExerciseId":"1ad78cb0-b4ff-454b-b807-c03351963df9","tag":"reminder2","timestamp":"2019-07-30T00:00:00.000Z"},{"id":"1df93e09-0452-4415-87d8-83fa4297cd03","collectionExerciseId":"87612d73-49a2-46ac-b939-cac63a33ca92","tag":"ref_period_start","timestamp":"2019-07-01T00:00:00.000Z"},{"id":"91c35f04-760d-4281-802d-dbb491d9b4a7","collectionExerciseId":"02ce7ad0-699e-4b23-a3f2-168e80836dd4","tag":"exercise_end","timestamp":"2020-05-31T00:00:00.000Z"},{"id":"e65fe833-4299-4ef8-ba1f-35ef44b42330","collectionExerciseId":"75dbcd71-a526-455b-9fbc-3a05a4bdfa5e","tag":"go_live","timestamp":"2019-10-01T00:00:00.000Z"},{"id":"c74c130a-9799-431d-bf4b-bb4cb627dcad","collectionExerciseId":"bb8e8f22-d26f-44f8-b70c-7d646768b78b","tag":"ref_period_end","timestamp":"2019-10-31T00:00:00.000Z"},{"id":"8be15fe4-1d34-41c5-b549-a8fa60a2a82e","collectionExerciseId":"6698e298-733b-49ee-b300-5be22c3ac94f","tag":"exercise_end","timestamp":"2021-01-31T00:00:00.000Z"},{"id":"a67dff1b-1508-434d-8405-2e6e858fb63c","collectionExerciseId":"fa4a2d27-6c9c-4b20-8489-2706a4a6d6ff","tag":"exercise_end","timestamp":"2021-06-30T00:00:00.000Z"},{"id":"111fd3de-ed37-4b5b-b9fb-632c9aa47cc4","collectionExerciseId":"bb8e8f22-d26f-44f8-b70c-7d646768b78b","tag":"mps","timestamp":"2019-10-19T00:00:00.000Z"},{"id":"1ffe1c25-1dde-42e6-a24d-2d6940276c13","collectionExerciseId":"42642209-9d1b-4a4c-b0c6-c0fcbe64d95b","tag":"exercise_end","timestamp":"2021-05-31T00:00:00.000Z"},{"id":"123c3d9e-7721-4bac-84b3-a65633d56ee8","collectionExerciseId":"13848243-ff1e-4f8e-9edc-5771be22953a","tag":"ref_period_start","timestamp":"2019-11-01T00:00:00.000Z"},{"id":"f27d3cc6-10f5-46a3-a97a-24f5cfc330b4","collectionExerciseId":"687cf71f-4ad3-4bd2-b054-5cfafddb109d","tag":"mps","timestamp":"2019-12-06T00:00:00.000Z"},{"id":"89b0b4d4-f229-40cb-9166-d3fb73ec65a2","collectionExerciseId":"06b30955-6a9c-4a0c-b86f-8a94778a943c","tag":"mps","timestamp":"2019-11-23T00:00:00.000Z"},{"id":"83fc2bf2-cc5f-4c24-be10-b718ff07d21d","collectionExerciseId":"81498954-b626-4aa7-b329-2bdb2749fd59","tag":"return_by","timestamp":"2019-11-08T00:00:00.000Z"},{"id":"ede4d4f3-f000-4691-9a5b-9bb583d99696","collectionExerciseId":"2e9bf8a7-2eda-48e2-8150-cfb64a99992a","tag":"exercise_end","timestamp":"2021-10-31T00:00:00.000Z"},{"id":"1de4ce1b-1e1f-4e9a-be42-143d4f4151e5","collectionExerciseId":"feaaf1fe-cca9-40a9-bd98-54bc6fd6608b","tag":"go_live","timestamp":"2019-08-21T00:00:00.000Z"},{"id":"f3b022b1-288d-4415-b222-b75e6da5cfe0","collectionExerciseId":"dc942cfb-cfc7-466d-b0dd-3dd5091c3f7a","tag":"exercise_end","timestamp":"2021-04-30T00:00:00.000Z"},{"id":"dd6fe632-ff02-4267-b0e6-ab28e58ef6ee","collectionExerciseId":"1ad78cb0-b4ff-454b-b807-c03351963df9","tag":"mps","timestamp":"2019-06-14T00:00:00.000Z"},{"id":"4e530712-bdf2-4753-b44f-a0e59a00858e","collectionExerciseId":"bcb60b03-3dda-4a83-8a23-d4b731cc5081","tag":"exercise_end","timestamp":"2020-08-31T00:00:00.000Z"},{"id":"01a3bcdc-2a48-4978-a564-fd03b7322f08","collectionExerciseId":"f6d849d7-8f14-43f1-9291-0acf32477539","tag":"mps","timestamp":"2019-08-24T00:00:00.000Z"},{"id":"3b6bf506-cc58-4db0-b116-566b4ae19ca8","collectionExerciseId":"1505c106-5d88-4976-b8c1-85c15c545465","tag":"ref_period_start","timestamp":"2019-10-28T00:00:00.000Z"},{"id":"49ea02b6-5fa0-4a26-9a08-eb5ae8df4cff","collectionExerciseId":"c34bae50-4b61-4f22-9bf0-00245d739c26","tag":"mps","timestamp":"2019-12-12T00:00:00.000Z"},{"id":"cd8014eb-3899-49ca-b8f6-482c7f4907ab","collectionExerciseId":"02453152-72b0-4b24-98bc-86410e763aed","tag":"exercise_end","timestamp":"2020-06-30T00:00:00.000Z"},{"id":"37af7da7-41f0-4ae2-b23c-9f85d70d773a","collectionExerciseId":"bba33485-d677-4e74-9593-9798a095f6eb","tag":"exercise_end","timestamp":"2022-01-31T00:00:00.000Z"},{"id":"bfdcdf6b-73f6-4b59-b7f7-611cd7a87b85","collectionExerciseId":"cb2474ca-003d-47d2-922e-1b838ab5c129","tag":"exercise_end","timestamp":"2021-05-31T00:00:00.000Z"},{"id":"dc45fdfc-d01a-426b-b5ba-d07069920c3b","collectionExerciseId":"b89ddbfb-884c-4de6-8324-2b55e51cf70b","tag":"return_by","timestamp":"2019-07-07T00:00:00.000Z"},{"id":"b6990236-c88f-42cf-96b6-51c228c1b509","collectionExerciseId":"bba33485-d677-4e74-9593-9798a095f6eb","tag":"mps","timestamp":"2019-12-07T00:00:00.000Z"},{"id":"491c3e3a-307b-4b88-b736-010222cf9432","collectionExerciseId":"f6d849d7-8f14-43f1-9291-0acf32477539","tag":"exercise_end","timestamp":"2021-09-30T00:00:00.000Z"},{"id":"07136785-0e22-4204-89b3-0fadaea81c8c","collectionExerciseId":"9ddc735d-5046-428f-8510-0598265a4804","tag":"exercise_end","timestamp":"2020-04-30T00:00:00.000Z"},{"id":"fe2196e4-4c3f-4a04-8fec-d8e67b331014","collectionExerciseId":"4388e326-3225-49c8-889f-1a18c58da3d6","tag":"ref_period_end","timestamp":"2019-08-31T00:00:00.000Z"},{"id":"5f540d5b-43f5-499a-ad96-d155cf72d3e6","collectionExerciseId":"4388e326-3225-49c8-889f-1a18c58da3d6","tag":"exercise_end","timestamp":"2021-09-30T00:00:00.000Z"},{"id":"f6b9f47c-fef5-4798-933b-ca6824ff1162","collectionExerciseId":"1cb4ec16-bde4-4d8f-9237-7614e04d22ae","tag":"go_live","timestamp":"2019-09-24T00:00:00.000Z"},{"id":"ee434239-b417-43c1-9059-629d07d53ce4","collectionExerciseId":"ebef3ac8-58d4-45e1-8ff9-9060e2b326c5","tag":"return_by","timestamp":"2019-10-16T00:00:00.000Z"},{"id":"d530a420-5229-48cf-85a6-5c17aa813b32","collectionExerciseId":"0529a56c-d852-4935-8a1e-316db9d928fe","tag":"return_by","timestamp":"2019-10-16T00:00:00.000Z"},{"id":"285bba1a-b6cf-4b48-8635-e524c9e74226","collectionExerciseId":"fa4a2d27-6c9c-4b20-8489-2706a4a6d6ff","tag":"ref_period_end","timestamp":"2019-05-26T00:00:00.000Z"},{"id":"e9216a7b-a004-4196-8b27-0c1ce0e10e2f","collectionExerciseId":"1cb4ec16-bde4-4d8f-9237-7614e04d22ae","tag":"ref_period_end","timestamp":"2019-09-30T00:00:00.000Z"},{"id":"bbd639f3-7725-484c-a4bb-f52c7ba02603","collectionExerciseId":"dc08941f-7cb1-4049-bf09-92ab23d10784","tag":"return_by","timestamp":"2019-07-09T00:00:00.000Z"},{"id":"24d5d985-c303-41cc-80b3-5856456582d9","collectionExerciseId":"35fdc730-bb79-4bec-ae49-0c3f34cd819a","tag":"exercise_end","timestamp":"2021-02-28T00:00:00.000Z"},{"id":"44d2fd94-67e6-4995-98f8-15feb3be87b1","collectionExerciseId":"625def2f-2111-49e6-87d7-eeb1709ab664","tag":"return_by","timestamp":"2019-06-27T00:00:00.000Z"},{"id":"bf8b4f41-8c8e-4be1-b48d-b1df697559ea","collectionExerciseId":"b89ddbfb-884c-4de6-8324-2b55e51cf70b","tag":"reminder","timestamp":"2019-07-10T00:00:00.000Z"},{"id":"6a751e72-1b64-4e2b-9a8e-18ee15f77164","collectionExerciseId":"1141b6f1-6f8e-4920-af0d-88a6875eb832","tag":"mps","timestamp":"2019-12-07T00:00:00.000Z"},{"id":"02e66a1e-8acf-4428-aff6-0bca8d13e277","collectionExerciseId":"87612d73-49a2-46ac-b939-cac63a33ca92","tag":"ref_period_end","timestamp":"2019-07-31T00:00:00.000Z"},{"id":"75b3e3b6-986a-4fec-9117-e58296cdfc86","collectionExerciseId":"fe70c082-ec8c-4a64-82dc-7d85e1c18364","tag":"ref_period_end","timestamp":"2019-12-29T00:00:00.000Z"},{"id":"d9027b2b-afb8-433a-954a-8837613c0d6b","collectionExerciseId":"f70354f5-a85f-4287-afa1-f446d66aede1","tag":"exercise_end","timestamp":"2020-10-31T00:00:00.000Z"},{"id":"1ffbed4a-e6f0-4c77-90bf-3b4c7c229948","collectionExerciseId":"13848243-ff1e-4f8e-9edc-5771be22953a","tag":"mps","timestamp":"2019-11-16T00:00:00.000Z"},{"id":"63be5b1a-09bb-4d12-8c64-6b8db13bc33b","collectionExerciseId":"1141b6f1-6f8e-4920-af0d-88a6875eb832","tag":"exercise_end","timestamp":"2022-01-31T00:00:00.000Z"},{"id":"01cabce8-d209-40a3-8102-130f64259b2d","collectionExerciseId":"addebb2d-5632-4d9d-95f4-d3e5f9acb0e9","tag":"mps","timestamp":"2019-10-19T00:00:00.000Z"},{"id":"627a406e-855c-4fab-b698-f61070b98a78","collectionExerciseId":"1cb4ec16-bde4-4d8f-9237-7614e04d22ae","tag":"employment","timestamp":"2019-09-14T00:00:00.000Z"},{"id":"e8497c67-36e7-485e-94a5-2af514aee8c7","collectionExerciseId":"6e466abd-3c6e-4093-8b68-8159778b02ec","tag":"reminder","timestamp":"2019-07-09T00:00:00.000Z"},{"id":"4f860968-48b8-4a42-8d9b-8398426925c3","collectionExerciseId":"9ae470fa-4e22-490a-add4-83b04ce9d6c2","tag":"ref_period_start","timestamp":"2019-11-01T00:00:00.000Z"},{"id":"defed336-fc0f-40d4-bdfa-b1d8f6aef705","collectionExerciseId":"1dcb3e89-02d5-49b9-ad02-2aa9cd98aa0a","tag":"exercise_end","timestamp":"2021-04-30T00:00:00.000Z"},{"id":"917c92c9-dc34-49e5-93e5-4cfc395219e6","collectionExerciseId":"3f6337d8-8eb6-4a02-a6a3-f29d2d6e8510","tag":"go_live","timestamp":"2019-10-22T00:00:00.000Z"},{"id":"dbff6746-3a09-4f46-8be9-a5d6bc9ebfb4","collectionExerciseId":"75dbcd71-a526-455b-9fbc-3a05a4bdfa5e","tag":"exercise_end","timestamp":"2021-10-31T00:00:00.000Z"},{"id":"b972e36c-ab90-4ced-a2fa-a65fd787d4e5","collectionExerciseId":"56592cb2-a8b3-4b51-9dda-619c952c46a7","tag":"reminder2","timestamp":"2019-12-30T00:00:00.000Z"},{"id":"4dac2098-753d-4b78-bab2-d7aff8ec3209","collectionExerciseId":"e5f41aa9-38e1-4ffd-9f27-d07e27dc21f6","tag":"ref_period_end","timestamp":"2019-07-31T00:00:00.000Z"},{"id":"70810bd5-64b9-4dfd-80cb-0260dcce9c84","collectionExerciseId":"cc9afa9c-6d49-4bdc-8d05-e5b31a55fb36","tag":"employment","timestamp":"2019-09-14T00:00:00.000Z"},{"id":"bb92d221-083b-4ce4-87c6-97f8891edbae","collectionExerciseId":"9dcbb243-bf1f-4b63-b852-aa59a4c1bbb8","tag":"reminder2","timestamp":"2019-10-29T00:00:00.000Z"},{"id":"8eca3eeb-6627-4e25-92e8-2a584b01f364","collectionExerciseId":"1cb4ec16-bde4-4d8f-9237-7614e04d22ae","tag":"ref_period_start","timestamp":"2019-09-01T00:00:00.000Z"},{"id":"b555ffff-d73c-4007-9241-cdd5b5d7a422","collectionExerciseId":"c34bae50-4b61-4f22-9bf0-00245d739c26","tag":"ref_period_start","timestamp":"2019-12-01T00:00:00.000Z"},{"id":"e738dd86-5126-4817-a365-4ddb5dfde9d3","collectionExerciseId":"0ae2a344-9048-482b-abb2-d56eec545e3e","tag":"return_by","timestamp":"2019-10-09T00:00:00.000Z"},{"id":"3f3588f4-9a1a-4b8f-a22f-5e68cf2ec88a","collectionExerciseId":"6ec325ba-6787-442e-8e7c-7b85a231aebb","tag":"mps","timestamp":"2019-06-15T00:00:00.000Z"},{"id":"1a6d0fb2-e58e-4591-a2bd-c949ce114abd","collectionExerciseId":"f8c12028-93b3-4a63-bdaf-5664f6e27888","tag":"exercise_end","timestamp":"2021-01-31T00:00:00.000Z"},{"id":"4ddc0fda-ff4c-4a69-89bf-eab6b7bddb7c","collectionExerciseId":"81d7b772-eff3-47b3-9029-2c0383657f65","tag":"exercise_end","timestamp":"2021-12-31T00:00:00.000Z"},{"id":"30029aa7-0c00-403e-b842-8970c154c0b4","collectionExerciseId":"8b6d0159-ab0b-4831-93a0-209981b5a0d5","tag":"exercise_end","timestamp":"2020-08-31T00:00:00.000Z"},{"id":"67b3dee0-101b-40eb-850a-c0c1d42bdd5e","collectionExerciseId":"b3145fb7-3eaa-42ef-8873-983dfafd4e54","tag":"ref_period_end","timestamp":"2019-05-31T00:00:00.000Z"},{"id":"b9ba67e6-41bb-4ce4-a945-353d55437ef6","collectionExerciseId":"9fd9158a-e47b-4c96-bc91-c62d4bce1947","tag":"ref_period_start","timestamp":"2019-12-01T00:00:00.000Z"},{"id":"afb786a9-a94e-4744-921b-be67d0fd8578","collectionExerciseId":"c75bb593-db4b-4dbf-92eb-55d0d085a7e2","tag":"exercise_end","timestamp":"2022-02-28T00:00:00.000Z"},{"id":"bb6b11a2-7bc3-42ff-8ae8-1fe0c509832e","collectionExerciseId":"bb8e8f22-d26f-44f8-b70c-7d646768b78b","tag":"exercise_end","timestamp":"2021-12-31T00:00:00.000Z"},{"id":"0353c19f-1ca6-47e3-8294-e07702f95ee6","collectionExerciseId":"4469dff1-ae52-4313-90de-70fed1e6d334","tag":"exercise_end","timestamp":"2020-07-31T00:00:00.000Z"},{"id":"7945bdf2-16e8-46e4-8196-a4481c55b171","collectionExerciseId":"7e12ac56-e3ee-4e2a-8693-be7bf2e009a1","tag":"exercise_end","timestamp":"2021-07-31T00:00:00.000Z"},{"id":"c0aabb60-1a64-4e77-85d0-8b84fe22215f","collectionExerciseId":"04384f50-e1bc-4ce3-8eab-9949283224b1","tag":"return_by","timestamp":"2019-07-17T00:00:00.000Z"},{"id":"2791d8d3-9bb0-4619-b8ca-9a3f172a8a2c","collectionExerciseId":"9c8febc5-bfb0-44af-95b8-8567439088b4","tag":"exercise_end","timestamp":"2020-12-31T00:00:00.000Z"},{"id":"b79b9fb4-f6bc-4f4a-8932-33663a6438a6","collectionExerciseId":"c75bb593-db4b-4dbf-92eb-55d0d085a7e2","tag":"reminder2","timestamp":"2020-01-28T00:00:00.000Z"},{"id":"67d3c335-40f8-4b7e-8c9b-c2a0f88cb0b7","collectionExerciseId":"87612d73-49a2-46ac-b939-cac63a33ca92","tag":"go_live","timestamp":"2019-07-23T00:00:00.000Z"},{"id":"b1c59de5-c7eb-4e26-8fd7-9ab030289c2e","collectionExerciseId":"631383db-5076-4de9-ae5a-96a30ad6ea5a","tag":"return_by","timestamp":"2020-01-15T00:00:00.000Z"},{"id":"4225fd83-1535-4cf1-bc14-c2e5ae871ecb","collectionExerciseId":"710399cd-7758-419f-8210-8cae845884bf","tag":"exercise_end","timestamp":"2021-11-30T00:00:00.000Z"},{"id":"a848fa2d-241d-42af-90ff-8474409a4df3","collectionExerciseId":"85ee3ac9-4523-4e75-aedf-aa5fd7c7a7c9","tag":"reminder","timestamp":"2019-10-11T00:00:00.000Z"},{"id":"ed9297e2-2ab8-46d8-bb24-26f1ed9e8b56","collectionExerciseId":"687cf71f-4ad3-4bd2-b054-5cfafddb109d","tag":"exercise_end","timestamp":"2022-01-31T00:00:00.000Z"},{"id":"02dc5567-d905-4ed5-8b3f-ddff56c70bb0","collectionExerciseId":"66e98d19-4a60-4a3d-a4d0-951f0020dd65","tag":"ref_period_start","timestamp":"2019-07-01T00:00:00.000Z"},{"id":"7c10feee-52d2-4daa-a365-28d8f00bdd16","collectionExerciseId":"de64ab7c-ded6-4e9b-8580-e59607d3c867","tag":"exercise_end","timestamp":"2020-05-31T00:00:00.000Z"},{"id":"950ec51d-a740-417d-be72-e2b8fd513703","collectionExerciseId":"3f6337d8-8eb6-4a02-a6a3-f29d2d6e8510","tag":"ref_period_end","timestamp":"2019-10-31T00:00:00.000Z"},{"id":"9fb7ef72-6d79-4d57-9a51-e1b8fc5bc7a0","collectionExerciseId":"1cb4ec16-bde4-4d8f-9237-7614e04d22ae","tag":"reminder","timestamp":"2019-10-09T00:00:00.000Z"},{"id":"c3c4cee5-2735-4e20-9cb7-8941ec6efc61","collectionExerciseId":"e5f41aa9-38e1-4ffd-9f27-d07e27dc21f6","tag":"go_live","timestamp":"2019-07-25T00:00:00.000Z"},{"id":"92c3b677-53b7-4d07-8366-7dd31ada4af8","collectionExerciseId":"e5f41aa9-38e1-4ffd-9f27-d07e27dc21f6","tag":"return_by","timestamp":"2019-08-07T00:00:00.000Z"},{"id":"091a5146-db78-484f-af46-055b29d90df1","collectionExerciseId":"cc9afa9c-6d49-4bdc-8d05-e5b31a55fb36","tag":"mps","timestamp":"2019-09-20T00:00:00.000Z"},{"id":"66ad7848-18b1-4fc0-8ccf-da0b108b4f73","collectionExerciseId":"6ec325ba-6787-442e-8e7c-7b85a231aebb","tag":"return_by","timestamp":"2019-07-17T00:00:00.000Z"},{"id":"aa0a3888-b260-4cc8-8594-7c30805baa80","collectionExerciseId":"29d5eb7e-0b98-4e1c-9566-5f193036992f","tag":"exercise_end","timestamp":"2021-02-28T00:00:00.000Z"},{"id":"bb166eeb-9339-4f91-94a9-da9dd7f27798","collectionExerciseId":"6694f81e-fab8-40cc-a0f5-a99cc40b9a29","tag":"exercise_end","timestamp":"2020-09-30T00:00:00.000Z"},{"id":"3c6c34d4-950e-4ef3-9b12-eecd8795c42c","collectionExerciseId":"032a8b37-6cb4-4fba-adce-afff5bb1b4dc","tag":"ref_period_start","timestamp":"2019-06-01T00:00:00.000Z"},{"id":"195c9379-faf5-499c-ac92-229b2ba9902f","collectionExerciseId":"feaaf1fe-cca9-40a9-bd98-54bc6fd6608b","tag":"mps","timestamp":"2019-08-16T00:00:00.000Z"},{"id":"4e716948-cd02-4b9c-b9bb-7e4c3f5474f7","collectionExerciseId":"8ad14503-49bd-4d30-9c74-604212429788","tag":"reminder","timestamp":"2019-08-13T00:00:00.000Z"},{"id":"40bc7051-1ea6-414a-889e-342d8948223c","collectionExerciseId":"5c3afe3c-5504-4d51-91b5-9ff7fabf7d19","tag":"mps","timestamp":"2019-07-19T00:00:00.000Z"},{"id":"b7d3672d-20d4-4d91-a9e6-f02d473162fd","collectionExerciseId":"51657690-7397-4da1-b48c-5a2f1071b6ae","tag":"exercise_end","timestamp":"2021-04-30T00:00:00.000Z"},{"id":"1861f5d2-a853-45c2-96ad-dacfc459392d","collectionExerciseId":"66e98d19-4a60-4a3d-a4d0-951f0020dd65","tag":"exercise_end","timestamp":"2021-08-31T00:00:00.000Z"},{"id":"e7ead5e6-8be2-4bfe-97c0-d1137608c2c4","collectionExerciseId":"dc08941f-7cb1-4049-bf09-92ab23d10784","tag":"exercise_end","timestamp":"2021-07-31T00:00:00.000Z"},{"id":"d3a61bbf-6753-4c62-ad73-a99be2890544","collectionExerciseId":"0529a56c-d852-4935-8a1e-316db9d928fe","tag":"exercise_end","timestamp":"2021-11-30T00:00:00.000Z"},{"id":"6f1ccffe-39fd-41c4-ae33-5428044b4c9e","collectionExerciseId":"1141b6f1-6f8e-4920-af0d-88a6875eb832","tag":"ref_period_start","timestamp":"2019-12-01T00:00:00.000Z"},{"id":"e5e8ef1d-e500-4a1c-9b88-add8852e95f6","collectionExerciseId":"fcb53bc0-946f-4842-a957-1d6c8fcadcb5","tag":"exercise_end","timestamp":"2020-09-30T00:00:00.000Z"},{"id":"9c361a5e-2a01-4719-a920-259000b5c2e3","collectionExerciseId":"1ad78cb0-b4ff-454b-b807-c03351963df9","tag":"ref_period_end","timestamp":"2019-06-30T00:00:00.000Z"},{"id":"76a76aba-31f8-4184-9e4d-c4c3e6255f05","collectionExerciseId":"d4445b0c-7e61-4b5d-afbc-4eafa6b7d9de","tag":"ref_period_end","timestamp":"2019-11-30T00:00:00.000Z"},{"id":"c9db8f2a-877f-4110-8b3a-5d5a5a1568b4","collectionExerciseId":"631383db-5076-4de9-ae5a-96a30ad6ea5a","tag":"ref_period_end","timestamp":"2019-12-31T00:00:00.000Z"},{"id":"cc79fb97-44af-4e88-9efc-93f70ccf37c0","collectionExerciseId":"d4445b0c-7e61-4b5d-afbc-4eafa6b7d9de","tag":"exercise_end","timestamp":"2022-01-31T00:00:00.000Z"},{"id":"f39581ca-06ff-46d4-bcf5-feb26ace48cb","collectionExerciseId":"cc9afa9c-6d49-4bdc-8d05-e5b31a55fb36","tag":"exercise_end","timestamp":"2021-10-31T00:00:00.000Z"},{"id":"15074ce5-ba12-481a-9025-52a851b9ba27","collectionExerciseId":"9fd9158a-e47b-4c96-bc91-c62d4bce1947","tag":"ref_period_end","timestamp":"2019-12-31T00:00:00.000Z"},{"id":"1b3daf76-4131-45be-8438-84c6812f6ced","collectionExerciseId":"56592cb2-a8b3-4b51-9dda-619c952c46a7","tag":"mps","timestamp":"2019-09-12T00:00:00.000Z"},{"id":"694f4b26-59b6-4f40-9cd3-3cf943423dfe","collectionExerciseId":"bba33485-d677-4e74-9593-9798a095f6eb","tag":"ref_period_end","timestamp":"2019-12-31T00:00:00.000Z"},{"id":"85e40bfe-2a8c-49ce-902c-b1342ceb3788","collectionExerciseId":"56592cb2-a8b3-4b51-9dda-619c952c46a7","tag":"return_by","timestamp":"2019-12-28T00:00:00.000Z"},{"id":"28043af8-d0fb-4dbd-9173-6d5340f966b4","collectionExerciseId":"54e31437-c6ae-41c1-893b-ae7224c8e9f9","tag":"ref_period_end","timestamp":"2019-05-31T00:00:00.000Z"},{"id":"f9f41134-dde4-4f38-a21d-915660d7ea7a","collectionExerciseId":"d471c85d-59ff-469c-a263-5c9951207cb3","tag":"mps","timestamp":"2019-12-07T00:00:00.000Z"},{"id":"10321193-213f-4f43-b9de-c248ed5fb052","collectionExerciseId":"cc728a87-10ce-48b4-aa46-b56db4263995","tag":"exercise_end","timestamp":"2021-03-30T00:00:00.000Z"},{"id":"eb83fc2f-50e2-4f14-9511-da2e9cf11ead","collectionExerciseId":"feaaf1fe-cca9-40a9-bd98-54bc6fd6608b","tag":"exercise_end","timestamp":"2021-09-30T00:00:00.000Z"},{"id":"06319645-bdac-4233-b2a5-202c1e2c075c","collectionExerciseId":"bc67046b-7767-4b0a-9c40-bbe129ef724e","tag":"exercise_end","timestamp":"2020-04-30T00:00:00.000Z"},{"id":"b5053c88-f105-427e-a9e9-a6f5c10c7cd5","collectionExerciseId":"81d7b772-eff3-47b3-9029-2c0383657f65","tag":"ref_period_end","timestamp":"2019-11-30T00:00:00.000Z"},{"id":"173784e9-d858-4b05-be0e-7c6466b03fb6","collectionExerciseId":"c685eda9-db5b-44d8-89bd-8f04f3c9a809","tag":"exercise_end","timestamp":"2020-06-30T00:00:00.000Z"},{"id":"f4fdf081-3432-498c-9e15-151f0fd70aba","collectionExerciseId":"e33f4d39-8f9c-4cb0-90a9-3bce18036c2b","tag":"exercise_end","timestamp":"2020-03-31T00:00:00.000Z"},{"id":"10b713d8-affe-4cfd-a97d-676ff9891808","collectionExerciseId":"b7160465-4b49-4dc5-87cf-1f3e1cdd7cf1","tag":"ref_period_end","timestamp":"2019-06-30T00:00:00.000Z"},{"id":"81e9959a-6b3c-449a-9548-bb11c7af787d","collectionExerciseId":"f5fe1edb-c927-4ad6-b2f4-abca7cc146f4","tag":"return_by","timestamp":"2019-09-07T00:00:00.000Z"},{"id":"e9e00549-df8b-4204-932f-9e242330e07a","collectionExerciseId":"d4445b0c-7e61-4b5d-afbc-4eafa6b7d9de","tag":"mps","timestamp":"2019-11-21T00:00:00.000Z"},{"id":"ec7095f4-6b41-4cac-9f57-374f9d47dca7","collectionExerciseId":"f5cbc68f-3c19-4d9f-b8d1-84c266ccfbd2","tag":"go_live","timestamp":"2020-01-02T00:00:00.000Z"},{"id":"46b5fd13-d286-4420-9851-881f52e5b139","collectionExerciseId":"625def2f-2111-49e6-87d7-eeb1709ab664","tag":"mps","timestamp":"2019-06-13T00:00:00.000Z"},{"id":"ec3b5830-5dbe-46c9-b5ac-5380c341e221","collectionExerciseId":"2ae80284-441b-43cb-a54f-3cbae8df880d","tag":"exercise_end","timestamp":"2021-06-30T00:00:00.000Z"},{"id":"e1891174-66d2-49a8-8332-b9d347c89b27","collectionExerciseId":"81d7b772-eff3-47b3-9029-2c0383657f65","tag":"mps","timestamp":"2019-11-16T00:00:00.000Z"},{"id":"cbcfef0c-c445-40dd-9b39-7aba826912c2","collectionExerciseId":"4388e326-3225-49c8-889f-1a18c58da3d6","tag":"go_live","timestamp":"2019-08-21T00:00:00.000Z"},{"id":"4c11623a-8494-412f-902b-334c65477719","collectionExerciseId":"5c3afe3c-5504-4d51-91b5-9ff7fabf7d19","tag":"return_by","timestamp":"2019-08-04T00:00:00.000Z"},{"id":"c345d7b3-fea7-435d-ba98-a52e087208a8","collectionExerciseId":"f4195ba4-f0fd-4c21-9da7-bd222016104f","tag":"go_live","timestamp":"2019-08-01T00:00:00.000Z"},{"id":"06d13c74-1e34-4c75-a77f-c552b4e8fb4a","collectionExerciseId":"bf8c1890-29d0-4d23-a558-83ca05e2d028","tag":"exercise_end","timestamp":"2020-04-30T00:00:00.000Z"},{"id":"9f86311a-bd13-4610-85ab-61db1bca8434","collectionExerciseId":"9dcbb243-bf1f-4b63-b852-aa59a4c1bbb8","tag":"mps","timestamp":"2019-09-13T00:00:00.000Z"},{"id":"b78fc62c-fabd-441c-8771-83cd4df79574","collectionExerciseId":"feaaf1fe-cca9-40a9-bd98-54bc6fd6608b","tag":"return_by","timestamp":"2019-09-10T00:00:00.000Z"},{"id":"9c986428-516f-430f-b0d1-07b0e8a0a49c","collectionExerciseId":"13848243-ff1e-4f8e-9edc-5771be22953a","tag":"exercise_end","timestamp":"2021-12-31T00:00:00.000Z"},{"id":"d3f5290e-1b7e-471a-ba7e-0c0b32a254d8","collectionExerciseId":"3f6337d8-8eb6-4a02-a6a3-f29d2d6e8510","tag":"exercise_end","timestamp":"2021-11-30T00:00:00.000Z"},{"id":"a194db63-4aea-4e5d-b100-9f80b8eca969","collectionExerciseId":"c75bb593-db4b-4dbf-92eb-55d0d085a7e2","tag":"go_live","timestamp":"2019-12-13T00:00:00.000Z"},{"id":"76ae5ea1-5f84-4546-bfa4-13a116ecefa6","collectionExerciseId":"66e98d19-4a60-4a3d-a4d0-951f0020dd65","tag":"go_live","timestamp":"2019-07-23T00:00:00.000Z"},{"id":"ef18e3db-a05f-47f5-a6af-9ed82f1628ea","collectionExerciseId":"feaaf1fe-cca9-40a9-bd98-54bc6fd6608b","tag":"ref_period_end","timestamp":"2019-08-31T00:00:00.000Z"},{"id":"00b60dba-2c90-4a6f-b044-cb293a86f0c3","collectionExerciseId":"0ae2a344-9048-482b-abb2-d56eec545e3e","tag":"ref_period_start","timestamp":"2019-09-01T00:00:00.000Z"},{"id":"94a82ef4-5df2-4058-86e5-4817fed3b067","collectionExerciseId":"9dcbb243-bf1f-4b63-b852-aa59a4c1bbb8","tag":"return_by","timestamp":"2019-10-05T00:00:00.000Z"},{"id":"e7d4c781-d4bd-4e25-9f59-c065708d82e8","collectionExerciseId":"631383db-5076-4de9-ae5a-96a30ad6ea5a","tag":"reminder","timestamp":"2020-01-17T00:00:00.000Z"},{"id":"a593cde1-e2c9-4d50-b07b-a101ef93381e","collectionExerciseId":"84342013-3905-49e1-8b5b-f1cf350517d1","tag":"exercise_end","timestamp":"2020-05-31T00:00:00.000Z"},{"id":"5f6ba0ca-98d1-48f9-b04f-7bb7da4565b8","collectionExerciseId":"66e98d19-4a60-4a3d-a4d0-951f0020dd65","tag":"mps","timestamp":"2019-07-18T00:00:00.000Z"},{"id":"263531ac-f392-4e86-888c-38d03a1373c6","collectionExerciseId":"85ee3ac9-4523-4e75-aedf-aa5fd7c7a7c9","tag":"return_by","timestamp":"2019-10-08T00:00:00.000Z"},{"id":"d5d5b532-7a7e-4d44-9bb2-faf7a97739aa","collectionExerciseId":"e7c4525c-7fae-45d3-a630-b9a559d659c0","tag":"exercise_end","timestamp":"2020-10-31T00:00:00.000Z"},{"id":"9e34e234-a306-45aa-98de-e4af582b769d","collectionExerciseId":"6e466abd-3c6e-4093-8b68-8159778b02ec","tag":"ref_period_end","timestamp":"2019-06-30T00:00:00.000Z"},{"id":"691ab068-3c41-4315-ac1f-bdd58a936e15","collectionExerciseId":"b89ddbfb-884c-4de6-8324-2b55e51cf70b","tag":"ref_period_end","timestamp":"2019-06-30T00:00:00.000Z"},{"id":"db711bee-9086-487f-adf4-7ae94f6e1de1","collectionExerciseId":"1ad78cb0-b4ff-454b-b807-c03351963df9","tag":"reminder","timestamp":"2019-07-17T00:00:00.000Z"},{"id":"1177cf09-3973-4b14-ace0-16e329c4b706","collectionExerciseId":"c34bae50-4b61-4f22-9bf0-00245d739c26","tag":"go_live","timestamp":"2019-12-13T00:00:00.000Z"},{"id":"085707b3-1dcd-41dd-a8b7-41d712a7b578","collectionExerciseId":"7493d38a-f8a0-42a6-9849-834a5f1de84c","tag":"exercise_end","timestamp":"2020-07-31T00:00:00.000Z"},{"id":"dbf679bf-0f9f-4a61-802f-3b64b367731e","collectionExerciseId":"625def2f-2111-49e6-87d7-eeb1709ab664","tag":"reminder","timestamp":"2019-07-03T00:00:00.000Z"},{"id":"f90e2335-1a42-43ef-858b-a02858e038d7","collectionExerciseId":"56592cb2-a8b3-4b51-9dda-619c952c46a7","tag":"ref_period_end","timestamp":"2019-12-30T00:00:00.000Z"},{"id":"d4e727cd-a8ce-4df2-935f-bf9c1ea32b7a","collectionExerciseId":"dc08941f-7cb1-4049-bf09-92ab23d10784","tag":"go_live","timestamp":"2019-07-02T00:00:00.000Z"},{"id":"0724f9fc-b68e-449d-ba74-f1bae3a358f0","collectionExerciseId":"ad77e776-cbe6-428a-a43d-1d306cdef484","tag":"exercise_end","timestamp":"2020-11-30T00:00:00.000Z"},{"id":"8991d3d1-e879-4035-bffc-6f0986d565e2","collectionExerciseId":"9dcbb243-bf1f-4b63-b852-aa59a4c1bbb8","tag":"ref_period_end","timestamp":"2019-09-30T00:00:00.000Z"},{"id":"44116641-f42b-4b38-aa82-d3a68086abc6","collectionExerciseId":"85ee3ac9-4523-4e75-aedf-aa5fd7c7a7c9","tag":"mps","timestamp":"2019-09-19T00:00:00.000Z"},{"id":"29dd1d7a-c32f-4a01-a441-4e87e01ef093","collectionExerciseId":"0ae2a344-9048-482b-abb2-d56eec545e3e","tag":"ref_period_end","timestamp":"2019-09-30T00:00:00.000Z"},{"id":"c77f0a09-f5da-466a-9023-70155a2a6bdc","collectionExerciseId":"8ad14503-49bd-4d30-9c74-604212429788","tag":"exercise_end","timestamp":"2021-09-30T00:00:00.000Z"},{"id":"c2bf048f-a0c8-4287-a206-d545050958cc","collectionExerciseId":"f9091290-d22f-4c1f-b80a-5bf225d00b5e","tag":"exercise_end","timestamp":"2021-07-31T00:00:00.000Z"},{"id":"7fe7fa34-6735-49cf-908d-9dca61a9592a","collectionExerciseId":"0529a56c-d852-4935-8a1e-316db9d928fe","tag":"go_live","timestamp":"2019-09-20T00:00:00.000Z"},{"id":"c4ab4fbe-afc1-402c-9dab-703880a4c29f","collectionExerciseId":"9dcbb243-bf1f-4b63-b852-aa59a4c1bbb8","tag":"ref_period_start","timestamp":"2019-07-01T00:00:00.000Z"},{"id":"68236af4-a5e4-441e-9f96-954697222ef6","collectionExerciseId":"6ec325ba-6787-442e-8e7c-7b85a231aebb","tag":"exercise_end","timestamp":"2021-08-31T00:00:00.000Z"},{"id":"a063dc07-4a54-4642-8d1e-c916b44e41f2","collectionExerciseId":"3f6337d8-8eb6-4a02-a6a3-f29d2d6e8510","tag":"return_by","timestamp":"2019-11-09T00:00:00.000Z"},{"id":"a4bc5db9-f744-4f45-b7f9-63f3b1fee4b4","collectionExerciseId":"d4445b0c-7e61-4b5d-afbc-4eafa6b7d9de","tag":"reminder","timestamp":"2019-12-11T00:00:00.000Z"},{"id":"69b54da7-9dcd-49e7-9e71-5c26eafa19d2","collectionExerciseId":"d471c85d-59ff-469c-a263-5c9951207cb3","tag":"ref_period_end","timestamp":"2019-12-31T00:00:00.000Z"},{"id":"5abbaa1d-33bb-4fa6-bbb8-58ab7b5fbd29","collectionExerciseId":"687cf71f-4ad3-4bd2-b054-5cfafddb109d","tag":"ref_period_end","timestamp":"2019-12-31T00:00:00.000Z"},{"id":"8965bae1-89f3-410f-bad2-d487bcfeda65","collectionExerciseId":"710399cd-7758-419f-8210-8cae845884bf","tag":"ref_period_start","timestamp":"2019-10-01T00:00:00.000Z"},{"id":"5c487c6c-1691-4dd9-910e-c54dcd409f8e","collectionExerciseId":"56592cb2-a8b3-4b51-9dda-619c952c46a7","tag":"ref_period_start","timestamp":"2019-12-14T00:00:00.000Z"},{"id":"e9421ee4-b402-48ff-bff4-3ce031a2abc0","collectionExerciseId":"e5f41aa9-38e1-4ffd-9f27-d07e27dc21f6","tag":"mps","timestamp":"2019-07-20T00:00:00.000Z"},{"id":"84533137-fdfa-4c3f-a284-a55b56b1f8bb","collectionExerciseId":"687cf71f-4ad3-4bd2-b054-5cfafddb109d","tag":"employment","timestamp":"2019-12-14T00:00:00.000Z"},{"id":"42cec3be-c60e-4ab4-8370-756640180230","collectionExerciseId":"032a8b37-6cb4-4fba-adce-afff5bb1b4dc","tag":"return_by","timestamp":"2019-07-09T00:00:00.000Z"},{"id":"73e61def-a5fc-4189-be9d-e268d4ecce94","collectionExerciseId":"f5fe1edb-c927-4ad6-b2f4-abca7cc146f4","tag":"mps","timestamp":"2019-08-17T00:00:00.000Z"},{"id":"d898fc4f-03fe-42b8-9310-ed3ec4eea39e","collectionExerciseId":"8f0f805e-2af4-4571-9fd3-407317e6cf02","tag":"exercise_end","timestamp":"2020-08-31T00:00:00.000Z"},{"id":"4388cb47-a666-4a10-a6d2-27bab62f7fe1","collectionExerciseId":"710399cd-7758-419f-8210-8cae845884bf","tag":"ref_period_end","timestamp":"2019-10-31T00:00:00.000Z"},{"id":"d7cc6261-1692-4d1d-abc5-2c4a83dc0f4b","collectionExerciseId":"addebb2d-5632-4d9d-95f4-d3e5f9acb0e9","tag":"reminder","timestamp":"2019-11-14T00:00:00.000Z"},{"id":"c437c578-7cf0-4669-9110-6496e9576ed1","collectionExerciseId":"e5f41aa9-38e1-4ffd-9f27-d07e27dc21f6","tag":"exercise_end","timestamp":"2021-09-30T00:00:00.000Z"},{"id":"69f0866e-b8a7-4eaa-aabb-95a0b27547db","collectionExerciseId":"c34bae50-4b61-4f22-9bf0-00245d739c26","tag":"exercise_end","timestamp":"2022-02-28T00:00:00.000Z"},{"id":"c0e59d29-af70-4f9e-a003-e3f9f4657e6c","collectionExerciseId":"04384f50-e1bc-4ce3-8eab-9949283224b1","tag":"go_live","timestamp":"2019-06-21T00:00:00.000Z"},{"id":"806f2817-ac78-417a-a1b6-335ef5ee700b","collectionExerciseId":"71520d87-e091-4062-b228-93abf52dcc04","tag":"exercise_end","timestamp":"2020-09-30T00:00:00.000Z"},{"id":"ebcdbfce-5f4b-4d6e-94a7-e98e29ddd561","collectionExerciseId":"f4195ba4-f0fd-4c21-9da7-bd222016104f","tag":"exercise_end","timestamp":"2021-08-31T00:00:00.000Z"},{"id":"b1ddabe4-f312-4a38-9585-382f818ea712","collectionExerciseId":"5c3afe3c-5504-4d51-91b5-9ff7fabf7d19","tag":"ref_period_end","timestamp":"2019-07-28T00:00:00.000Z"},{"id":"358108f7-bc8d-4568-b99a-a7478d0378f5","collectionExerciseId":"d4445b0c-7e61-4b5d-afbc-4eafa6b7d9de","tag":"return_by","timestamp":"2019-12-07T00:00:00.000Z"},{"id":"7203dd36-22f8-47f2-b036-98e9a4251e39","collectionExerciseId":"1505c106-5d88-4976-b8c1-85c15c545465","tag":"ref_period_end","timestamp":"2019-11-24T00:00:00.000Z"},{"id":"721d4fab-3215-45b3-a139-b1e186493e99","collectionExerciseId":"7ebdb17b-80a0-4de8-b360-025cff3774e7","tag":"exercise_end","timestamp":"2020-05-31T00:00:00.000Z"},{"id":"5107c5bf-30b0-47fb-927c-3e07de35af56","collectionExerciseId":"f5fe1edb-c927-4ad6-b2f4-abca7cc146f4","tag":"ref_period_start","timestamp":"2019-08-01T00:00:00.000Z"},{"id":"6a6b83b1-7c02-4070-a614-4eb780163c5c","collectionExerciseId":"f5a4d25d-4852-4ffc-82fb-e71321bd62ed","tag":"exercise_end","timestamp":"2020-05-31T00:00:00.000Z"},{"id":"74afa1a1-b6c0-42a7-9fb9-31ef7b70eb1b","collectionExerciseId":"feaaf1fe-cca9-40a9-bd98-54bc6fd6608b","tag":"ref_period_start","timestamp":"2019-08-01T00:00:00.000Z"},{"id":"b937ac3c-d57e-4e82-b50b-0517b7c9bf5b","collectionExerciseId":"3f6337d8-8eb6-4a02-a6a3-f29d2d6e8510","tag":"mps","timestamp":"2019-10-17T00:00:00.000Z"},{"id":"8cbdd50c-0cac-4f38-8992-8af2f0a5cd67","collectionExerciseId":"032a8b37-6cb4-4fba-adce-afff5bb1b4dc","tag":"mps","timestamp":"2019-06-15T00:00:00.000Z"},{"id":"680cd981-5a52-4a88-b506-edf11d93f9ce","collectionExerciseId":"ebef3ac8-58d4-45e1-8ff9-9060e2b326c5","tag":"go_live","timestamp":"2019-09-20T00:00:00.000Z"},{"id":"a3e2dfa7-5bba-4e41-9afd-bcc48407ba70","collectionExerciseId":"d20ef6ed-de7f-418a-81d8-9ba6b83eda3c","tag":"exercise_end","timestamp":"2020-07-31T00:00:00.000Z"},{"id":"da7003fc-770f-4692-aa12-cfb3d11e1a10","collectionExerciseId":"56592cb2-a8b3-4b51-9dda-619c952c46a7","tag":"reminder","timestamp":"2019-12-29T00:00:00.000Z"},{"id":"482ab4b1-164b-442c-acc6-6c6734410f28","collectionExerciseId":"bb8e8f22-d26f-44f8-b70c-7d646768b78b","tag":"reminder","timestamp":"2019-11-13T00:00:00.000Z"},{"id":"d3d550f6-1796-44db-964e-5fbddd0f22ab","collectionExerciseId":"d471c85d-59ff-469c-a263-5c9951207cb3","tag":"reminder","timestamp":"2020-01-17T00:00:00.000Z"},{"id":"0c3dea2a-438b-43e3-a969-d296a690d800","collectionExerciseId":"d15a1e9c-feca-48b3-9ab1-19f7a6c0e806","tag":"exercise_end","timestamp":"2021-02-28T00:00:00.000Z"},{"id":"d74ed24d-ac3d-425e-a8a2-f959ed50545e","collectionExerciseId":"cc9afa9c-6d49-4bdc-8d05-e5b31a55fb36","tag":"return_by","timestamp":"2019-10-06T00:00:00.000Z"},{"id":"4953df7c-2375-4623-903e-3011faaecbd5","collectionExerciseId":"5c3afe3c-5504-4d51-91b5-9ff7fabf7d19","tag":"ref_period_start","timestamp":"2019-07-01T00:00:00.000Z"},{"id":"1a09aa83-d8b0-4f76-b916-8dda3b470a71","collectionExerciseId":"625def2f-2111-49e6-87d7-eeb1709ab664","tag":"ref_period_end","timestamp":"2019-06-30T00:00:00.000Z"},{"id":"3755d15f-126a-4025-abe2-886b61244721","collectionExerciseId":"6ab11e56-9b06-4793-906d-83750cc035c4","tag":"return_by","timestamp":"2019-06-08T00:00:00.000Z"},{"id":"82d4b60d-a7f3-492d-8282-b4763bf47809","collectionExerciseId":"32b702cc-ecfd-4180-bf81-31397a29a0b5","tag":"exercise_end","timestamp":"2020-07-31T00:00:00.000Z"},{"id":"707fde79-096d-43c0-8a23-4ce2a7ee7cb4","collectionExerciseId":"28637c66-e5d3-44a4-86b4-5c2a06984c9e","tag":"exercise_end","timestamp":"2021-01-31T00:00:00.000Z"},{"id":"31820343-a1f2-4845-9786-e6a87da20868","collectionExerciseId":"d8d67522-b6de-42b8-9310-e09839ba0bf8","tag":"exercise_end","timestamp":"2020-10-30T00:00:00.000Z"},{"id":"55fa995e-95c1-49ba-a1a0-c0ff1d7f4d4d","collectionExerciseId":"13848243-ff1e-4f8e-9edc-5771be22953a","tag":"go_live","timestamp":"2019-11-22T00:00:00.000Z"},{"id":"5599cc74-29e6-4e2c-ade6-0942cd443193","collectionExerciseId":"bba33485-d677-4e74-9593-9798a095f6eb","tag":"ref_period_start","timestamp":"2019-12-01T00:00:00.000Z"},{"id":"eb00600a-73f5-4c69-8f0a-2f9fa26705a0","collectionExerciseId":"687cf71f-4ad3-4bd2-b054-5cfafddb109d","tag":"reminder","timestamp":"2020-01-03T00:00:00.000Z"},{"id":"1fba9417-b893-4efb-a882-4f6166cab4d1","collectionExerciseId":"addebb2d-5632-4d9d-95f4-d3e5f9acb0e9","tag":"ref_period_end","timestamp":"2019-10-31T00:00:00.000Z"},{"id":"1bbd9e81-a927-44cf-8260-17a3969a8786","collectionExerciseId":"9dcbb243-bf1f-4b63-b852-aa59a4c1bbb8","tag":"reminder","timestamp":"2019-10-16T00:00:00.000Z"},{"id":"b0bf6114-f1b3-4493-8dd3-a95eabeeb14b","collectionExerciseId":"8ad14503-49bd-4d30-9c74-604212429788","tag":"mps","timestamp":"2019-07-20T00:00:00.000Z"},{"id":"6954849f-d03a-44f2-9d96-4ecd42b1ef7d","collectionExerciseId":"bb8e8f22-d26f-44f8-b70c-7d646768b78b","tag":"ref_period_start","timestamp":"2019-10-01T00:00:00.000Z"},{"id":"5dc32639-7aaf-4867-ac9d-22caa0d65325","collectionExerciseId":"56592cb2-a8b3-4b51-9dda-619c952c46a7","tag":"employment","timestamp":"2019-12-14T00:00:00.000Z"},{"id":"4061c052-f5f7-4a8d-bce6-c676c3d77d12","collectionExerciseId":"687cf71f-4ad3-4bd2-b054-5cfafddb109d","tag":"ref_period_start","timestamp":"2019-12-14T00:00:00.000Z"},{"id":"d35bc270-e710-485b-8fbe-153c2d87ac97","collectionExerciseId":"5c3afe3c-5504-4d51-91b5-9ff7fabf7d19","tag":"reminder","timestamp":"2019-08-06T00:00:00.000Z"},{"id":"41bce284-aa09-463d-b085-6986966c82db","collectionExerciseId":"85ee3ac9-4523-4e75-aedf-aa5fd7c7a7c9","tag":"exercise_end","timestamp":"2021-11-30T00:00:00.000Z"},{"id":"6a59944a-acba-4da1-bc04-c499bbaba7ab","collectionExerciseId":"bb8c729a-6f64-4af4-b760-eb742fd5ccec","tag":"exercise_end","timestamp":"2020-08-31T00:00:00.000Z"}]
```

An `HTTP 404 Not Found` status code is returned if an event could not be found.
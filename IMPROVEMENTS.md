# rm-collection-exercise-service
## evaluation
   This microservice is a web service implemented using [Spring Boot](http://projects.spring.io/spring-boot/). This 
   service intends to provide the orchestration of the processes necessary to begin the data collection for a 
   particular field period for a particular survey.
   
   This is a busy service interacting with multiple microservices to carry out its functions. Collection Exercise 
   (rm-collection-exercise-service) interacts with the following microservices :
   * action-svc
   * collection-instrument-svc
   * sample-svc
   * survey-svc
   * party-svc
   
   There are multiple functions carried out by this microservice, and the resources is exposed via rest endpoints. 
   This rest service mainly deals with collection exercises and its association. The service follows a defined
   controlled state transitions of CollectionExercises and SampleUnitGroups managed by spring integration as follows:
   * CREATED
   * SCHEDULED
   * READY_FOR_REVIEW
   * PENDING/EXECUTION_STARTED
   * EXECUTED
   * VALIDATED
   * FAILEDVALIDATION
   * READY_FOR_LIVE
   
   It receives data from response operation UI to create a new collection exercise record over the endpoint
   `/collectionexercises`. At this point a service gives a call to survey-svc to retrieve existing survey based on 
   `surveyId` or `surveyref` present in the reqest body, if the survery exists the service looks for an existing 
   collection exercise and if not present it creates a record to `collectionexercise` table. 
   
   It receives data from response operation UI to create an event against an existing collection exercise record over 
   the endpoint `/collectionexercises/{id}/events`. At this point eventservice looks for an existing collection exercise
   if it exists and if the event with the same tag does not exist, a validation is performed for respective event tag.
   Once the validation passes, it gives a call to the action service to add action rule for the event tag against the 
   action plan created for collection exercise and creates a record to `event` table. At this point
   a request to event change request handler is given, which publishes the event to the message queue. 
   After which the schedular is called to schedule a collection exercise event. Events also follow defined state i.e.
   `CREADED`, `UPDATED`, `DELETED`, `EventElapsed`.
   
   The service exposes following endpoints to retrieve data against collection-exercises:
   * `/collectionexercises` - to retrieve all the existing collection exercises in the system.
   * `/collectionexercises/{id}` - to retrieve an existing collection exercise against given collection exercise id.
   * `/collectionexercises/survey/{id}` - to retrieve existing collection exercises against a given survey id.
   * `/collectionexercises/{exerciseRef}/survey/{surveyRef}` - to retrieve existing collection exercises against given 
   exerciseRef and SurveyRef.
   * `/collectionexercises/link/{collectionExerciseId}` - to retrieve list of UUIDs for the sample summary linked to 
   given collection exercise.
   * `/collectionexercises/surveys` - to retrieve all existing collection exercises for given list of survey ids.
   * `/collectionexercises/{id}/events/{tag}` - to retrieve collection exercise event against given collection exercise
   id and event tag.
   * `/collectionexercises/{id}/events` - to retrieve all events against a collection exercise id.
   * `/collectionexercises/events` - to retrieve collection exercise events.
   
   The service exposes following endpoints to update data against collection-exercises:
   * `/collectionexercises/{id}` - to update existing collection exercise against the collection exercise id.
   * `/collectionexercises/link/{collectionExerciseId}` - to update existing collection exercise by linking sample
   summary.
   * `/collectionexercises/{id}/events/{tag}` - to update existing event date against collection exercise id and event 
   tag.
   * `/collectionexercises/{id}/scheduledStart` - to update existing collection exercise schedule start date time.
   * `/collectionexercises/{id}/exerciseRef` - to update existing collection exercise exercise reference.
   * `/collectionexercises/{id}/name` - to update existing collection exercise name.
   * `/collectionexercises/{id}/userDescription` - to update a collection exercise userDescription.
   * `/collectionexercises/{id}/surveyId` - to update survey id against the collection exercise id.
   
   The service exposes following endpoint to remove data against collection-exercises:
   * `/collectionexercises/{id}/events/{tag}` - to remove existing event and action rules against given collection 
   exercise id and event tag.
   * `/collectionexercises/{id}` - to remove existing collection exercise.
   * `/collectionexercises/unlink/{collectionExerciseId}/sample/{sampleSummaryId}` - to unlink sample summary from a
   collection exercise.
   
   #### Rabbit queues
   The collection-exercise-service utilises rabbit queues and spring integration to process uploaded collection 
   instrument and samples via response operations UI also for processing scheduled collection exercise events.
   
   * `cemInMessage` - channel with event message to transition collection exercise state to LIVE if the event is 
   a go_live event, and it's EventElapsed state.
   * `ciMessageDto` - channel with collection instrument to transition collection exercise state to `READY FOR REVIEW` 
   * `sampleUploadedSampleSummaryInMessage` - channel with sample to be processed. Once the sample gets uploaded to 
      the `collex.sample.uploaded.inbound` via response operation UI. It is picked, the service tries to retrieve 
      records in `samplelink` table  against `samplelinksummayId` for distinct
      `collectionExerciseId`. The transition state for all collection-exercise retrieved are changed to 'ReadyToReview'.
   * `sampleUnitTransformed` - channel responsible for receipt of sample units. At this point the records are populated
   in `sampleunit` and `sampleunitgroup` tables and sampleUnit message is published to the case service.
   
   ## [data base](rm-collection-exercise-svc-db.png)
   #### `collectionexercisestate`
   This table holds allowed collection exercise transition states.
   #### `collectionexercise`
   This is the main table used by collection exercise where it stores collection exercise data with `statefk` as 
   collection exercise state.
   #### `event`
   This table holds the information of collection exercise events with `collexfk` as collection exercise parent key.
   #### `sampleunitgroupstate`
   This table holds allowed sample unit group transition states.
   #### `sampleunitgroup`
   This table holds sample unit group data with `statefk` as sample unit groups state.
   #### `sampleunittype`
   This table holds Business/Social sample unit type as `sampleunittypepk`
   #### `sampleunit`
   This table holds sample units with `sampleunitgroupfk` as `sampleunitgrouppk` and `sampleunittypefk` as `sampleunit.sampleunittypepk`
   #### `samplelink`
   Holds collection-exercise and sample summary id
   
   
   ## improvements
   The collection-exercise-svc deals with collection exercise and looks ok compared to some other services. Though the 
   extensive use of scheduler and rabbit queue along with spring integration make it challenging understanding different
   functions collection exercise is performing. This service does not seem to have a lot of duplication of the data, 
   but it does interact with multiple services to carry out its function.
   This service can be simplified by improving existing service, a rewrite might not be necessary.
   * DB structure need to be improved to provide more viable link and relationship between tables.
   * Remove `sampleunittype` as it can easily be removed by a static value. 
   * Remove the use of XML/XSD and replace with JSON.
   * Improving service to propagate the changes to action for any changes. Few of the function does not seem to propagate
   correctly to action svc.
   * we can reduce the complexity of the service by redesigning the service and delegating few of the 
   functions to another microservice.
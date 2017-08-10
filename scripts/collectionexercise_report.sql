SELECT  c.name
      , c.scheduledstartdatetime
      , c.scheduledexecutiondatetime
      , c.scheduledreturndatetime
      , c.scheduledenddatetime
      , c.periodstartdatetime
      , c.periodenddatetime
      , c.actualexecutiondatetime
      , c.actualpublishdatetime
      , c.executedby
      , c.stateFK
      , c.samplesize 
FROM collectionexercise.collectionexercise c;
-- Add not null constraint to event.collexfk
ALTER TABLE collectionexercise.event ALTER COLUMN collexfk SET NOT NULL;

-- Add a boolean column to indicate whether a message has been sent for an event when it occurs
ALTER TABLE collectionexercise.event ADD COLUMN message_sent TIMESTAMP;
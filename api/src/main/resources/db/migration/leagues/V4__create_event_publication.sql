CREATE TABLE IF NOT EXISTS event_publication
(
    id                     UUID NOT NULL,
    event_type             VARCHAR(512) NOT NULL,
    listener_id            VARCHAR(512) NOT NULL,
    publication_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    serialized_event       VARCHAR(4000) NOT NULL,
    status                 VARCHAR(20) NOT NULL,
    completion_attempts    INTEGER NOT NULL,
    last_resubmission_date TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id)
);

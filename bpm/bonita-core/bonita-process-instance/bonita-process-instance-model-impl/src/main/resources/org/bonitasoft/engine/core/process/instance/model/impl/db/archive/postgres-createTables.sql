CREATE TABLE arch_process_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(75) NOT NULL,
  processDefinitionId INT8 NOT NULL,
  description VARCHAR(255),
  startDate INT8 NOT NULL,
  startedBy INT8 NOT NULL,
  startedByDelegate INT8 NOT NULL,
  endDate INT8 NOT NULL,
  archiveDate INT8 NOT NULL,
  stateId INT NOT NULL,
  lastUpdate INT8 NOT NULL,
  rootProcessInstanceId INT8,
  callerId INT8,
  migration_plan INT8,
  sourceObjectId INT8 NOT NULL,
  stringIndex1 VARCHAR(50),
  stringIndex2 VARCHAR(50),
  stringIndex3 VARCHAR(50),
  stringIndex4 VARCHAR(50),
  stringIndex5 VARCHAR(50),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE arch_flownode_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  flownodeDefinitionId INT8 NOT NULL,
  kind VARCHAR(25) NOT NULL,
  sourceObjectId INT8,
  archiveDate INT8 NOT NULL,
  rootContainerId INT8 NOT NULL,
  parentContainerId INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(75),
  displayDescription VARCHAR(255),
  stateId INT NOT NULL,
  stateName VARCHAR(50),
  terminal BOOLEAN NOT NULL,
  stable BOOLEAN ,
  actorId INT8 NULL,
  assigneeId INT8 DEFAULT 0 NOT NULL,
  reachedStateDate INT8,
  lastUpdateDate INT8,
  expectedEndDate INT8,
  claimedDate INT8,
  priority SMALLINT,
  gatewayType VARCHAR(50),
  hitBys VARCHAR(255),
  logicalGroup1 INT8 NOT NULL,
  logicalGroup2 INT8 NOT NULL,
  logicalGroup3 INT8,
  logicalGroup4 INT8 NOT NULL,
  loop_counter INT,
  loop_max INT,
  loopCardinality INT,
  loopDataInputRef VARCHAR(255),
  loopDataOutputRef VARCHAR(255),
  description VARCHAR(255),
  sequential BOOLEAN,
  dataInputItemRef VARCHAR(255),
  dataOutputItemRef VARCHAR(255),
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy INT8,
  executedByDelegate INT8,
  activityInstanceId INT8,
  aborting BOOLEAN NOT NULL,
  triggeredByEvent BOOLEAN,
  interrupting BOOLEAN,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE arch_transition_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  rootContainerId INT8 NOT NULL,
  parentContainerId INT8 NOT NULL,
  name VARCHAR(255) NOT NULL,
  source INT8,
  target INT8,
  state VARCHAR(50),
  terminal BOOLEAN NOT NULL,
  stable BOOLEAN ,
  stateCategory VARCHAR(50) NOT NULL,
  logicalGroup1 INT8 NOT NULL,
  logicalGroup2 INT8 NOT NULL,
  logicalGroup3 INT8,
  logicalGroup4 INT8 NOT NULL,
  description VARCHAR(255),
  sourceObjectId INT8,
  archiveDate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);



CREATE TABLE arch_connector_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  containerId INT8 NOT NULL,
  containerType VARCHAR(10) NOT NULL,
  connectorId VARCHAR(255) NOT NULL,
  version VARCHAR(10) NOT NULL,
  name VARCHAR(255) NOT NULL,
  activationEvent VARCHAR(30),
  state VARCHAR(50),
  sourceObjectId INT8,
  archiveDate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

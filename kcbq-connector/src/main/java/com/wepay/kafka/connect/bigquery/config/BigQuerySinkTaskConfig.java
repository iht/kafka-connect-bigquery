package com.wepay.kafka.connect.bigquery.config;

/*
 * Copyright 2016 WePay, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.util.List;
import java.util.Optional;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Class for task-specific configuration properties.
 */
public class BigQuerySinkTaskConfig extends BigQuerySinkConfig {
  private static final ConfigDef config;
  private static final Logger logger = LoggerFactory.getLogger(BigQuerySinkTaskConfig.class);

  public static final String THREAD_POOL_SIZE_CONFIG =                  "threadPoolSize";
  private static final ConfigDef.Type THREAD_POOL_SIZE_TYPE =           ConfigDef.Type.INT;
  public static final Integer THREAD_POOL_SIZE_DEFAULT =                10;
  private static final ConfigDef.Validator THREAD_POOL_SIZE_VALIDATOR = ConfigDef.Range.atLeast(1);
  private static final ConfigDef.Importance THREAD_POOL_SIZE_IMPORTANCE =
      ConfigDef.Importance.MEDIUM;
  private static final String THREAD_POOL_SIZE_DOC =
      "The size of the BigQuery write thread pool. This establishes the maximum number of "
      + "concurrent writes to BigQuery.";

  public static final String QUEUE_SIZE_CONFIG =                    "queueSize";
  private static final ConfigDef.Type QUEUE_SIZE_TYPE =             ConfigDef.Type.LONG;
  // should this even have a default?
  public static final Long QUEUE_SIZE_DEFAULT =                     -1L;
  private static final ConfigDef.Validator QUEUE_SIZE_VALIDATOR =   ConfigDef.Range.atLeast(-1);
  private static final ConfigDef.Importance QUEUE_SIZE_IMPORTANCE = ConfigDef.Importance.HIGH;
  private static final String QUEUE_SIZE_DOC =
      "The maximum size (or -1 for no maximum size) of the worker queue for bigQuery write "
      + "requests before all topics are paused. This is a soft limit; the size of the queue can "
      + "go over this before topics are paused. All topics will be resumed once a flush is "
      + "requested or the size of the queue drops under half of the maximum size.";

  public static final String BIGQUERY_RETRY_CONFIG =                    "bigQueryRetry";
  private static final ConfigDef.Type BIGQUERY_RETRY_TYPE =             ConfigDef.Type.INT;
  public static final Integer BIGQUERY_RETRY_DEFAULT =                  0;
  private static final ConfigDef.Validator BIGQUERY_RETRY_VALIDATOR =   ConfigDef.Range.atLeast(0);
  private static final ConfigDef.Importance BIGQUERY_RETRY_IMPORTANCE =
      ConfigDef.Importance.MEDIUM;
  private static final String BIGQUERY_RETRY_DOC =
      "The number of retry attempts that will be made per BigQuery request that fails with a "
      + "backend error or a quota exceeded error";

  public static final String BIGQUERY_RETRY_WAIT_CONFIG =               "bigQueryRetryWait";
  private static final ConfigDef.Type BIGQUERY_RETRY_WAIT_CONFIG_TYPE = ConfigDef.Type.LONG;
  public static final Long BIGQUERY_RETRY_WAIT_DEFAULT =                1000L;
  private static final ConfigDef.Validator BIGQUERY_RETRY_WAIT_VALIDATOR =
      ConfigDef.Range.atLeast(0);
  private static final ConfigDef.Importance BIGQUERY_RETRY_WAIT_IMPORTANCE =
      ConfigDef.Importance.MEDIUM;
  private static final String BIGQUERY_RETRY_WAIT_DOC =
      "The minimum amount of time, in milliseconds, to wait between BigQuery backend or quota "
      +  "exceeded error retry attempts.";

  public static final String BIGQUERY_MESSAGE_TIME_PARTITIONING_CONFIG =
      "bigQueryMessageTimePartitioning";
  private static final ConfigDef.Type BIGQUERY_MESSAGE_TIME_PARTITIONING_CONFIG_TYPE =
      ConfigDef.Type.BOOLEAN;
  public static final Boolean BIGQUERY_MESSAGE_TIME_PARTITIONING_DEFAULT =                   false;
  private static final ConfigDef.Importance BIGQUERY_MESSAGE_TIME_PARTITIONING_IMPORTANCE =
      ConfigDef.Importance.HIGH;
  private static final String BIGQUERY_MESSAGE_TIME_PARTITIONING_DOC =
      "Whether or not to use the message time when inserting records. "
      + "Default uses the connector processing time.";
  
  public static final String BIGQUERY_PARTITION_DECORATOR_CONFIG =
          "bigQueryPartitionDecorator";
  private static final ConfigDef.Type BIGQUERY_PARTITION_DECORATOR_CONFIG_TYPE =
      ConfigDef.Type.BOOLEAN;
  //This has been set to true to preserve the existing behavior. However, we can set it to false if field based partitioning is used in BigQuery
  public static final Boolean BIGQUERY_PARTITION_DECORATOR_DEFAULT =                 true; 
  private static final ConfigDef.Importance BIGQUERY_PARTITION_DECORATOR_IMPORTANCE =
      ConfigDef.Importance.HIGH;
  private static final String BIGQUERY_PARTITION_DECORATOR_DOC =
      "Whether or not to append partition decorator to BigQuery table name when inserting records. "
      + "Default is true. Setting this to true appends partition decorator to table name (e.g. table$yyyyMMdd depending on the configuration set for bigQueryPartitionDecorator). "
      + "Setting this to false bypasses the logic to append the partition decorator and uses raw table name for inserts.";

  public static final String BIGQUERY_TIMESTAMP_PARTITION_FIELD_NAME_CONFIG = "timestampPartitionFieldName";
  private static final ConfigDef.Type BIGQUERY_TIMESTAMP_PARTITION_FIELD_NAME_TYPE = ConfigDef.Type.STRING;
  private static final String BIGQUERY_TIMESTAMP_PARTITION_FIELD_NAME_DEFAULT = null;
  private static final ConfigDef.Importance BIGQUERY_TIMESTAMP_PARTITION_FIELD_NAME_IMPORTANCE =
      ConfigDef.Importance.LOW;
  private static final String BIGQUERY_TIMESTAMP_PARTITION_FIELD_NAME_DOC =
      "The name of the field in the value that contains the timestamp to partition by in BigQuery"
          + " and enable timestamp partitioning for each table. Leave this configuration blank,"
          + " to enable ingestion time partitioning for each table.";

  public static final String BIGQUERY_CLUSTERING_FIELD_NAMES_CONFIG = "clusteringPartitionFieldNames";
  private static final ConfigDef.Type BIGQUERY_CLUSTERING_FIELD_NAMES_TYPE = ConfigDef.Type.LIST;
  private static final List<String> BIGQUERY_CLUSTERING_FIELD_NAMES_DEFAULT = null;
  private static final ConfigDef.Importance BIGQUERY_CLUSTERING_FIELD_NAMES_IMPORTANCE =
      ConfigDef.Importance.LOW;
  private static final String BIGQUERY_CLUSTERING_FIELD_NAMES_DOC =
      "List of fields on which data should be clustered by in BigQuery, separated by commas";

  static {
    config = BigQuerySinkConfig.getConfig()
        .define(
            THREAD_POOL_SIZE_CONFIG,
            THREAD_POOL_SIZE_TYPE,
            THREAD_POOL_SIZE_DEFAULT,
            THREAD_POOL_SIZE_VALIDATOR,
            THREAD_POOL_SIZE_IMPORTANCE,
            THREAD_POOL_SIZE_DOC
        ).define(
            QUEUE_SIZE_CONFIG,
            QUEUE_SIZE_TYPE,
            QUEUE_SIZE_DEFAULT,
            QUEUE_SIZE_VALIDATOR,
            QUEUE_SIZE_IMPORTANCE,
            QUEUE_SIZE_DOC
        ).define(
            BIGQUERY_RETRY_CONFIG,
            BIGQUERY_RETRY_TYPE,
            BIGQUERY_RETRY_DEFAULT,
            BIGQUERY_RETRY_VALIDATOR,
            BIGQUERY_RETRY_IMPORTANCE,
            BIGQUERY_RETRY_DOC
        ).define(
            BIGQUERY_RETRY_WAIT_CONFIG,
            BIGQUERY_RETRY_WAIT_CONFIG_TYPE,
            BIGQUERY_RETRY_WAIT_DEFAULT,
            BIGQUERY_RETRY_WAIT_VALIDATOR,
            BIGQUERY_RETRY_WAIT_IMPORTANCE,
            BIGQUERY_RETRY_WAIT_DOC
        ).define(
            BIGQUERY_MESSAGE_TIME_PARTITIONING_CONFIG,
            BIGQUERY_MESSAGE_TIME_PARTITIONING_CONFIG_TYPE,
            BIGQUERY_MESSAGE_TIME_PARTITIONING_DEFAULT,
            BIGQUERY_MESSAGE_TIME_PARTITIONING_IMPORTANCE,
            BIGQUERY_MESSAGE_TIME_PARTITIONING_DOC
        ).define(
            BIGQUERY_PARTITION_DECORATOR_CONFIG,
            BIGQUERY_PARTITION_DECORATOR_CONFIG_TYPE,
            BIGQUERY_PARTITION_DECORATOR_DEFAULT,
            BIGQUERY_PARTITION_DECORATOR_IMPORTANCE,
            BIGQUERY_PARTITION_DECORATOR_DOC
        ).define(
            BIGQUERY_TIMESTAMP_PARTITION_FIELD_NAME_CONFIG,
            BIGQUERY_TIMESTAMP_PARTITION_FIELD_NAME_TYPE,
            BIGQUERY_TIMESTAMP_PARTITION_FIELD_NAME_DEFAULT,
            BIGQUERY_TIMESTAMP_PARTITION_FIELD_NAME_IMPORTANCE,
            BIGQUERY_TIMESTAMP_PARTITION_FIELD_NAME_DOC
        ).define(
            BIGQUERY_CLUSTERING_FIELD_NAMES_CONFIG,
            BIGQUERY_CLUSTERING_FIELD_NAMES_TYPE,
            BIGQUERY_CLUSTERING_FIELD_NAMES_DEFAULT,
            BIGQUERY_CLUSTERING_FIELD_NAMES_IMPORTANCE,
            BIGQUERY_CLUSTERING_FIELD_NAMES_DOC
        );
  }

  private void checkSchemaUpdates() {
    Class<?> schemaRetriever = getClass(BigQuerySinkConfig.SCHEMA_RETRIEVER_CONFIG);

    boolean allowNewBigQueryFields = getBoolean(BigQuerySinkConfig.ALLOW_NEW_BIGQUERY_FIELDS_CONFIG);
    boolean allowRequiredFieldRelaxation = getBoolean(BigQuerySinkConfig.ALLOW_BIGQUERY_REQUIRED_FIELD_RELAXATION_CONFIG);
    if ((allowNewBigQueryFields || allowRequiredFieldRelaxation) && schemaRetriever == null) {
      throw new ConfigException(
          "Cannot perform schema updates without a schema retriever"
      );
    }

    if (schemaRetriever == null) {
      logger.warn(
          "No schema retriever class provided; auto schema updates are impossible"
      );
    }
  }

  /**
   * Returns the field name to use for timestamp partitioning.
   * @return String that represents the field name.
   */
  public Optional<String> getTimestampPartitionFieldName() {
    return Optional.ofNullable(getString(BIGQUERY_TIMESTAMP_PARTITION_FIELD_NAME_CONFIG));
  }

  /**
   * Returns the field names to use for clustering.
   * @return List of Strings that represent the field names.
   */
  public Optional<List<String>> getClusteringPartitionFieldName() {
    return Optional.ofNullable(getList(BIGQUERY_CLUSTERING_FIELD_NAMES_CONFIG));
  }

  /**
   * Check the validity of table partitioning configs.
   */
  private void checkPartitionConfigs() {
    if (getTimestampPartitionFieldName().isPresent() && getBoolean(BIGQUERY_PARTITION_DECORATOR_CONFIG)) {
      throw new ConfigException(
          "Only one partitioning configuration mode may be specified for the connector. "
              + "Use either bigQueryPartitionDecorator OR timestampPartitionFieldName."
      );
    }
  }

  /**
   * Check the validity of table clustering configs.
   */
  private void checkClusteringConfigs() {
    if (getClusteringPartitionFieldName().isPresent()) {
      if (!getTimestampPartitionFieldName().isPresent() && !getBoolean(BIGQUERY_PARTITION_DECORATOR_CONFIG)) {
        throw new ConfigException(
            "Clustering field name may be specified only on a partitioned table."
        );
      }
      if (getClusteringPartitionFieldName().get().size() > 4) {
        throw new ConfigException(
            "You can only specify up to four clustering field names."
        );
      }
    }
  }

  public static ConfigDef getConfig() {
    return config;
  }

  /**
   * @param properties A Map detailing configuration properties and their respective values.
   */
  public BigQuerySinkTaskConfig(Map<String, String> properties) {
    super(config, properties);
    checkSchemaUpdates();
    checkPartitionConfigs();
    checkClusteringConfigs();
  }
}

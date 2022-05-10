# Flink
FLINK_HOME=/home/ubuntu/flink-build-target
FLINK_SOURCE=/home/ubuntu/Meces/Meces-on-Flink

FLINK_CLASSPATH=${FLINK_HOME}/lib/flink-csv-1.12.0.jar:${FLINK_HOME}/lib/flink-json-1.12.0.jar:${FLINK_HOME}/lib/flink-shaded-zookeeper-3.4.14.jar:${FLINK_HOME}/lib/flink-table_2.11-1.12.0.jar:${FLINK_HOME}/lib/flink-table-blink_2.11-1.12.0.jar:${FLINK_HOME}/lib/log4j-1.2-api-2.12.1.jar:${FLINK_HOME}/lib/log4j-api-2.12.1.jar:${FLINK_HOME}/lib/log4j-core-2.12.1.jar:${FLINK_HOME}/lib/log4j-slf4j-impl-2.12.1.jar:${FLINK_HOME}/lib/flink-dist_2.11-1.12.0.jar

# Kafka
KAFKA_HOST=localhost
KAFKA_PORT=9092
KAFKA_SERVERS=${KAFKA_HOST}:${KAFKA_PORT}
KAFKA_WORDCOUNT_GENERATE_JAR_PATH=/home/ubuntu/exp/kafka-exp/target/exp-1.0-SNAPSHOT.jar
KAFKA_WORDCOUNT_TOPIC=word_topic_par_24
KAFKA_HOME=/home/ubuntu/environment/kafka_2.13-3.0.1

# Redis
REDIS_HOME=/home/ubuntu/environment/redis-3.2.0

# Maven
MAVEN_REPO_PATH=/home/ubuntu/.m2/repository

WORDCOUNT_JAR_HOME=/home/ubuntu/exp/wordcount-exp

# path
DATA_DIR=/home/ubuntu/data
SCRIPT_DIR=/home/ubuntu/scripts
SAVEPOINT_DIR_PATH=/home/ubuntu/tmp/flink_checkpoints/

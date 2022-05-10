#!/bin/bash

source ~/scripts/variables.sh

RATE=$1
WORD_LENGTH=$2

JAVA_BIN=${JAVA_HOME}/bin/java
KAFKA_WORDCOUNT_GENERATE_JAR_PATH=/home/ubuntu/exp/kafka-exp/target/exp-1.0-SNAPSHOT.jar

${JAVA_BIN} -classpath ${FLINK_CLASSPATH}:${KAFKA_WORDCOUNT_GENERATE_JAR_PATH} exp.KafkaProducerWordCount \
-kafkaServers ${KAFKA_SERVERS} \
-KafkaTopic ${KAFKA_WORDCOUNT_TOPIC} \
-rate ${RATE} \
-wordLength ${WORD_LENGTH}


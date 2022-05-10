#!/bin/bash
source ~/scripts/variables.sh

# ZOOKEEPER=localhost:2181
KAFKA_SERVERS=localhost:9092
# KAFKA_SERVERS=slave201:9092,slave202:9092,slave206:9092,slave208:9092,slave209:9092
#ZOOKEEPER=slave201:2181,slave202:2181,slave206:2181,slave208:2181,slave209:2181

# TOPIC_NAME=bid_nexmark_par_25
# TOPIC_NAME=person_nexmark_par_25
# TOPIC_NAME=auction_nexmark_par_25
TOPIC_NAME=word_topic_par_24
# TOPIC_NAME=postFetchRequestTopic
# TOPIC_NAME=postFetchStateTopic
# TOPIC_NAME=postFetchStateInfoTopic

${KAFKA_HOME}/bin/kafka-topics.sh --create --bootstrap-server ${KAFKA_SERVERS} --replication-factor 1 --partitions 1 --topic ${TOPIC_NAME}
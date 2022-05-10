#!/bin/bash
source ~/scripts/variables.sh

# ZOOKEEPER=localhost:2181
KAFKA_SERVERS=localhost:9092
#ZOOKEEPER=slave201:2181,slave202:2181,slave206:2181,slave208:2181,slave209:2181
# ZOOKEEPER=slave207:2181,slave208:2181,slave209:2181
# ZOOKEEPER=slave203:2181

# TOPIC_NAME=person_nexmark_par_25
# TOPIC_NAME=auction_nexmark_par_25
#TOPIC_NAME=tt_par_25
#TOPIC_NAME=fail_over_topic_4
TOPIC_NAME=word_topic_par_24
# TOPIC_NAME=postFetchRequestTopic
# TOPIC_NAME=postFetchStateTopic
# TOPIC_NAME=postFetchStateInfoTopic

${KAFKA_HOME}/bin/kafka-topics.sh --describe --bootstrap-server ${KAFKA_SERVERS} --topic ${TOPIC_NAME}

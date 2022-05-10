
source ~/scripts/variables.sh

MODE=$1
JOB_DURATION=$2

#JAR_PATH=${FLINK_SOURCE}/flink-examples/flink-examples-streaming/target/flink-examples-streaming_2.11-1.12.0-RescaleWordCount.jar
JAR_PATH=${WORDCOUNT_JAR_HOME}/flink-examples-streaming_2.11-1.12.0-RescaleWordCount.jar
# params
DEFAULT_PARA=1
COUNT_PARA=1
COUNTER_LOOPS=0
COUNTER_MAX_PAPA=128
SOURCE_RATE=40000
SINK_INTERVAL=1000
EARLIEST=false
NATIVE_FLINK=false


# deprecated params
INTERVAL=5 # milli sedcons

#KAFKA_CLASSPATH=file://${FLINK_SOURCE}/flink-connectors/flink-connector-kafka/target/flink-connector-kafka_2.11-1.12.0.jar
#BYTE_CLASSPATH=file://${MAVEN_REPO_PATH}/org/apache/kafka/kafka-clients/2.4.1/kafka-clients-2.4.1.jar
KAFKA_CLASSPATH=file://${WORDCOUNT_JAR_HOME}/flink-connector-kafka_2.11-1.12.0.jar
BYTE_CLASSPATH=file://${WORDCOUNT_JAR_HOME}/kafka-clients-2.4.1.jar

# submit job
JOB_PARAMS="
-C ${KAFKA_CLASSPATH} \
-C ${BYTE_CLASSPATH} \
-d \
-p ${DEFAULT_PARA} \
${JAR_PATH} \
-wordFile ${WORDFILE_PATH} \
-interval ${INTERVAL} \
-useDictionary ${USEDICTIONARY} \
-partialPause ${PARTIALPAUSE} \
-counterCostlyOperationLoops ${COUNTER_LOOPS} \
-KafkaHost ${KAFKA_HOST} \
-KafkaPort ${KAFKA_PORT} \
-kafkaServers ${KAFKA_SERVERS} \
-topic ${KAFKA_WORDCOUNT_TOPIC} \
-startFromEarliest ${EARLIEST} \
-counterPar ${COUNT_PARA} \
-counterMaxPara ${COUNTER_MAX_PAPA} \
-sinkInterval ${SINK_INTERVAL} \
-nativeFlink ${NATIVE_FLINK} \
-sourceRate ${SOURCE_RATE} \
-defaultPar ${DEFAULT_PARA}"

${FLINK_HOME}/bin/flink run ${JOB_PARAMS}
# SUBMIT_OUTPUT=`${FLINK_HOME}/bin/flink run ${JOB_PARAMS}`
# JOB_ID=${SUBMIT_OUTPUT##*submitted with JobID }
# echo ${JOB_ID}

echo -e "\033[31m Job running... \033[0m" 
sleep ${JOB_DURATION}

if [ ${MODE} == "test" ]; then
  exit 1
fi

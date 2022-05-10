
source ~/scripts/variables.sh


MODE=$1
JOB_DURATION=$2
AFTER_RESCALE_DURATION=$3

#JAR_PATH=${FLINK_SOURCE}/flink-examples/flink-examples-streaming/target/flink-examples-streaming_2.11-1.12.0-RescaleWordCount.jar
JAR_PATH=${WORDCOUNT_JAR_HOME}/flink-examples-streaming_2.11-1.12.0-RescaleWordCount.jar

# params
DEFAULT_PARA=3
COUNT_PARA=3
COUNT_PARA_AFTER=5
COUNTER_LOOPS=0
COUNTER_MAX_PAPA=128
SOURCE_RATE=40000
SINK_INTERVAL=1000
EARLIEST=false

if [[ ${MODE} == "rescale" ]] || [[ ${MODE} == "test" ]]; then
  NATIVE_FLINK=false
else
  NATIVE_FLINK=true
fi



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


# SUBMIT_OUTPUT=`${FLINK_HOME}/bin/flink run ${JOB_PARAMS}`
# JOB_ID=${SUBMIT_OUTPUT##*submitted with JobID }
# echo ${JOB_ID}


if [ ${MODE} == "test" ]; then
  exit 1
fi


SUBMIT_OUTPUT=`${FLINK_HOME}/bin/flink run ${JOB_PARAMS}`
JOB_ID=${SUBMIT_OUTPUT##*submitted with JobID }
echo ${JOB_ID}
echo -e "\033[31m Job running... \033[0m" 
sleep ${JOB_DURATION}


if [ ${MODE} == "rescale" ]; then
  # on-the-fly rescaling
  OPERATOR_NAME="FlatMap-Counter -> Appender -> Sink: Sink"
  # rescale
  ${FLINK_HOME}/bin/flink rescale -rmd 3 -rpl ["${OPERATOR_NAME}":${COUNT_PARA_AFTER}]  ${JOB_ID}
else
  # restart job
  STOP_OUTPUT=`${FLINK_HOME}/bin/flink stop -p ${SAVEPOINT_DIR_PATH} ${JOB_ID}`
  SAVEPOINT_PATH=${STOP_OUTPUT##*Savepoint completed. Path: }
  echo ${SAVEPOINT_PATH}

  COUNT_PARA=${COUNT_PARA_AFTER}
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
  SUBMIT_OUTPUT=`${FLINK_HOME}/bin/flink run -s ${SAVEPOINT_PATH} ${JOB_PARAMS}`
  JOB_ID=${SUBMIT_OUTPUT##*submitted with JobID }
  echo ${JOB_ID}
fi

echo -e "\033[31m Waiting after rescaling... \033[0m"
sleep ${AFTER_RESCALE_DURATION}
echo `${FLINK_HOME}/bin/flink cancel ${JOB_ID}`

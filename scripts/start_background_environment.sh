source ~/scripts/variables.sh

rm nohup.out
rm -rf ~/tmp/*

echo "---------------------- Clean Kafka    ----------------------"
ps -x | grep kafka | grep -v grep | cut -c 1-6 | xargs kill -9
rm -rf /home/ubuntu/tmp/kafka-logs/*

echo "---------------------- Start Kafka    ----------------------"
nohup ${KAFKA_HOME}/bin/zookeeper-server-start.sh ${KAFKA_HOME}/config/zookeeper.properties &
nohup ${KAFKA_HOME}/bin/kafka-server-start.sh ${KAFKA_HOME}/config/server.properties &
sleep 2
${SCRIPT_DIR}/kafka/create_topic.sh

echo "---------------------- Create Dirs    ----------------------"
mkdir ${DATA_DIR}/restart
mkdir ${DATA_DIR}/meces
mkdir ${DATA_DIR}/order
mkdir ${DATA_DIR}/test



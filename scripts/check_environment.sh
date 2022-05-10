source ~/scripts/variables.sh

TEST_MODE=test

echo -e "\033[34m ----------------------    History data cleaning   ---------------------- \033[0m"
rm -rf ${DATA_DIR}/${TEST_MODE}/*
echo -e "\033[34m ----------------------    History data cleaned    ---------------------- \033[0m"

echo -e "\033[34m ----------------------    Environment starting   ---------------------- \033[0m"
/bin/cp ${SCRIPT_DIR}/flink-confs/${TEST_MODE}/flink-conf.yaml ${FLINK_HOME}/conf/
/bin/cp ${SCRIPT_DIR}/flink-confs/${TEST_MODE}/meces.conf.prop ${FLINK_HOME}/conf/
${SCRIPT_DIR}/start_environment.sh
nohup ${SCRIPT_DIR}/kafka_wordcount_generate.sh 1000 6 &
echo -e "\033[34m ----------------------    Environment started ---------------------- \033[0m"

# TODO：check kafka topic
# TODO：check echo 1 > /proc/sys/vm/overcommit_memory
# TODO：check hdfs,hdfs savepoint dir, data dir

echo -e "\033[34m ----------------------    Submit and run wordcount   ---------------------- \033[0m"
${SCRIPT_DIR}/meces_exp/flink-wordcount.sh ${TEST_MODE} 60
echo -e "\033[34m ----------------------    Wordcount complted  ---------------------- \033[0m"


echo -e "\033[34m ----------------------    Wordcount Data Collecting   ---------------------- \033[0m"
${SCRIPT_DIR}/collect_data/sync_log_file.sh ${TEST_MODE}
bash -i ${SCRIPT_DIR}/collect_data/collect_log.sh ${TEST_MODE} 10 100 10
echo -e "\033[34m ----------------------    Wordcount Data Collected    ---------------------- \033[0m"

echo -e "\033[34m ----------------------    Cleaning    ---------------------- \033[0m" 
${SCRIPT_DIR}/close_environment.sh

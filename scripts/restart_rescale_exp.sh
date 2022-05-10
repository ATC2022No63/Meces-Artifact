source ~/scripts/variables.sh

TEST_MODE=restart

echo -e "\033[34m ----------------------    History data cleaning   ---------------------- \033[0m"" "
rm -rf ${DATA_DIR}/${TEST_MODE}/*
echo -e "\033[34m ----------------------    History data cleaned    ---------------------- \033[0m" 

echo -e "\033[34m ----------------------    Environment starting   ---------------------- \033[0m" 
/bin/cp ${SCRIPT_DIR}/flink-confs/${TEST_MODE}/flink-conf.yaml ${FLINK_HOME}/conf/
/bin/cp ${SCRIPT_DIR}/flink-confs/${TEST_MODE}/meces.conf.prop ${FLINK_HOME}/conf/
${SCRIPT_DIR}/start_environment.sh
nohup ${SCRIPT_DIR}/kafka_wordcount_generate.sh 20000 7 &
echo -e "\033[34m ----------------------    Environment started ---------------------- \033[0m" 

echo -e "\033[34m ----------------------    Submit and run wordcount   ---------------------- \033[0m" 
${SCRIPT_DIR}/meces_exp/flink-wordcount_rescale.sh restart 120 60
echo -e "\033[34m ----------------------    Wordcount complted  ---------------------- \033[0m" 


echo -e "\033[34m ----------------------    Wordcount Data Collecting   ---------------------- \033[0m" 
${SCRIPT_DIR}/collect_data/sync_log_file.sh ${TEST_MODE}
bash -i ${SCRIPT_DIR}/collect_data/collect_log.sh ${TEST_MODE} 10 600 100
echo -e "\033[34m ----------------------    Wordcount Data Collected    ---------------------- \033[0m" 


echo -e "\033[34m ----------------------    Cleaning    ---------------------- \033[0m" 
${SCRIPT_DIR}/close_environment.sh

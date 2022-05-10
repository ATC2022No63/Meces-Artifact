source ~/scripts/variables.sh

SUB_DIR_NAME=$1
cp ${FLINK_HOME}/log/flink--taskexecutor-0-${HOSTNAME}.out ${DATA_DIR}/${SUB_DIR_NAME}/


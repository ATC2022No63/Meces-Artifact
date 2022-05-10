source ~/scripts/variables.sh

echo "---------------------- Start Redis    ----------------------"
${REDIS_HOME}/src/redis-server ${REDIS_HOME}/redis.conf --daemonize yes
${REDIS_HOME}/src/redis-cli flushall


echo "---------------------- Start Flink    ----------------------"
${FLINK_HOME}/bin/stop-cluster.sh
${FLINK_HOME}/bin/start-cluster.sh
sleep 10


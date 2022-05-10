source ~/scripts/variables.sh


echo "---------------------- Stop Redis ----------------------"
${REDIS_HOME}/src/redis-cli flushall
pkill redis
sleep 2

echo "---------------------- Stop Flink ----------------------"
${FLINK_HOME}/bin/stop-cluster.sh


echo "---------------------- Stop Word Generator    ----------------------"
ps -x | grep KafkaProducerWordCount | grep -v grep | cut -c 1-6 | xargs kill -9




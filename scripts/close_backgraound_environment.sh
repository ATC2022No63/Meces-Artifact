source ~/scripts/variables.sh


echo "---------------------- Stop Kafka    ----------------------"
ps -x | grep kafka | grep -v grep | cut -c 1-6 | xargs kill -9



export JAVA_HOME=/home/ubuntu/environment/jdk1.8.0_181
# export JAVA_HOME=/home/ubuntu/environment/jdk-11.0.10
PATH=$JAVA_HOME/bin:$PATH
export MAVEN_HOME=/home/ubuntu/environment/apache-maven-3.6.3
export PATH=$MAVEN_HOME/bin:$PATH
# export ZOOKEEPER_HOME=/home/ubuntu/apache-zookeeper-3.6.0-bin
# export PATH=$ZOOKEEPER_HOME/bin:$PATH
export HADOOP_HOME=/home/ubuntu/environment/hadoop-2.9.2
export PATH=$HADOOP_HOME/bin:$PATH
export HADOOP_CLASSPATH=$($HADOOP_HOME/bin/hadoop classpath)
export CLASSPATH=$CLASSPATH:$($HADOOP_HOME/bin/hadoop classpath)
export PATH=$PATH:/home/ubuntu/.local/bin

mkdir -p data/{test,order,meces,restart}
ln -s flink-1.12.0 flink-build-target
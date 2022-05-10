# Document for the Artifact Evaluation of Meces

This is the repository for the artifact evaluation of Meces. Meces is described in the ATC '22 paper "Meces: Latency-efficient Rescaling via Prioritized State Migration for Stateful Distributed Stream Processing Systems".

Meces proposes an on-the-fly state migration mechanism for stream processing. By choosing the state migration order dynamically and prioritizing the migration for states currently needed, Meces reduces latency caused by queuing during the migration process and realizes a smoother state migration process.

## Experimental Environment

Meces is implemented on Apache Flink, which is compiled using Maven and ran with Java. It also relies on Kafka and Redis to function properly.

To save your trouble of setting up all these components, we offer two ways to access our pre-prepared Linux environment in cloud. You can either access a remote machine via SSH, or use a Docker image to deploy the environment yourself.

### Remote Machine via SSH

We provide a server for the evaluation,  which is a `c6i.8xlarge` instance in the AWS EC2 cloud. You can log in via sshpass:

```
ssh -p {password} ssh ubunutu@host
```

The home directory contains the following files:

```
> tree .
├─ environment              # dependencies of Meces
    ├── hadoop-2.9.2
    ├── kafka_2.13
    ├── redis-3.2.0
    ├── jdk-1.8.0
    ├── jdk-11.0.10
    ├── maven-3.6.3
├─ scripts                  # scripts for performing experiments with Meces
├─ data                     # collected experimental results
├─ exp                      # external jar files used in experiments
├─ src                      # Meces source code
├─ Flink-build-target       # compiled Meces project
├─ tmp                      # temporary files
```

### Deploy with a Docker Image

You can run the experiments in our prepared Docker image by running following commands:

```
# get Docker images
docker pull meetzhongwc/dockerhub:meces
# init container from Docker image
docker run -it meetzhongwc/dockerhub:meces
```

We recommend running experiments with Docker image to avoid situation where multiple evaluators are using the remote machine at the same time.


## Steps for Evaluating Meces

We have automated most of the integration and launching operations. You can refer to the script files in ```/home/ubuntu/scripts```.

### 1. Checking the Environment and Basic Functions

You can check if the environment and the basic functions of Meces are working properly by running:

```
/home/ubuntu/scripts/start_background_environment.sh 
/home/ubuntu/scripts/check_environment.sh
```

The script generally goes through the following steps:

1. Start Meces

   Initialize the external services (Kafka and Redis) that Meces depends on, and start running Meces.

2. Test

   Submit a stream processing job (wordcount) for testing to Meces and initialize the data source needed by the job.

3. Collect data

   After waiting for a period of time (60s), collect latency data during the job process and draw a plot.

If everything goes fine, you can see a green line of words on the terminal: ```"Sucessfully collect wordcount latency data."```. You should also find a ```latency_test.pdf``` in ```/home/ubuntu/data/test```, plotting the latency curve of the wordcount job.

### 2. Evaluating the Rescaling Performance of Meces

You can evaluate the rescaling performance of Meces through a wordcount job. You can also compare it with that of ```Native Flink``` （restart） and ```Order-Unaware``` （block-based）, as reported in our paper.

To do so, run the following:

```
/home/ubuntu/scripts/rescale_exp.sh
```

This script will perform three experiments in series, each experiment will run the wordcount job, and rescale with different mechanisms (`Meces`, `Order-Unaware`, `Native-Flink`).

Each experiment generally includes the following steps:

1. Prepare the environment

   Clear historical data, initialize external services, and start Flink.

2. Run the Job

   Submit a stream processing job (wordcount) and initialize the job data source.

3. Rescale

   After waiting for a period of time (120 seconds), start the rescaling process.

4. Collect data

   Wait for a period of time (60s) after rescaling, collect the latency data during the job process and draw the plot.

If everything goes fine, you can see a green line of words on the terminal: ```"Sucessfully collect wordcount latency data."``` at the end of each stage. You should also find plots of the latency curve of each stage:

```
/home/ubuntu/data/meces/latency_meces.pdf
/home/ubuntu/data/order/latency_order.pdf
/home/ubuntu/data/restart/latency_restart.pdf
```

The plots should show that, compared with ```Native Flink``` （restart） and ```Order-Unaware``` （block-based）Meces can significantly lower the latency peak during rescaling.


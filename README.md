# Meces: Latency-efficient Rescaling via Prioritized State Migration for Stateful Distributed Stream Processing Systems

This is the repository for the artifact evaluation of Meces. Meces is described in the ATC '22 paper "Meces: Latency-efficient Rescaling via Prioritized State Migration for Stateful Distributed Stream Processing Systems".

Meces proposes an on-the-fly state migration mechanism for stateful stream processing. By prioritizing the migration with a fetch-on-demand design, Meces reduces latency caused by queuing and realizes a smoother state migration.

## 1. Experimental Environment

Meces is implemented on Apache Flink, which is compiled using Maven and run with Java. It also relies on Kafka and Redis to function properly.

To save you the trouble of setting up all these components, we provide two ways to get a pre-prepared environment. You can SSH into our pre-prepared machine in the AWS Cloud or deploy the environment yourself using the Docker image we provide.

### 1.1 Remote Machine via SSH

We provide an AWS EC2 server (```c6i.8xlarge``` with 32-core vCPU, 64 GB of RAM, and 30GB of EBS storage) and have all the dependencies well-prepared.

You can contact us to get access to the machine (ip address and password etc.) anytime during the artifact evaluation process. After that, you can log in via ssh:

``` shell
ssh -p {password} ubunutu@host
```

The home directory contains the following files:
```
├─ environment              # dependencies of Meces
    ├── kafka_2.13
    ├── redis-3.2.0
    ├── jdk-1.8.0
    ├── maven-3.6.3
├─ scripts                  # scripts for setting the environment and for running test programs
├─ src                      # Meces source code
├─ Flink-build-target       # compiled Meces project
├─ data                     # collected experimental results
├─ exp                      # external jar files used in experiments
├─ tmp                      # temporary files
```

### 1.2 Deploy the Environment with a Docker Image

You can deploy the environment with our prepared Docker image by running following commands:

``` shell
# get the Docker image
docker pull meetzhongwc/dockerhub:latest
# init container from the Docker image
docker run -it meetzhongwc/dockerhub:latest
#  Now you are inside the container, setup the environment:
source /home/ubuntu/scripts/install.sh
```

For artifact evaluation, we recommend deploying the image on a machine similar to ours (```c6i.8xlarge``` with 32-core vCPU, 64 GB of RAM, and 30GB of EBS storage), or a machine with at least a 16-core CPU and 32 GB of RAM.

## 2. Steps for Evaluating Meces

We have automated most of the integration and launching operations of our artifact. You can refer to the script files in ```/home/ubuntu/scripts```.

### 2.1. Checking the Environment and Basic Functions

In this part, you can check if the environment and the basic functions of Meces are working properly by running:

``` shell
/home/ubuntu/scripts/start_background_environment.sh 
/home/ubuntu/scripts/check_environment.sh
```

The script generally goes through the following steps:


1. Start Meces

   - Initialize the external services (Kafka and Redis) that Meces depends on, and start running Meces on Apache Flink.
   
2. Submit a Testing Job

   - Run a testing ```wordcount``` job to Meces and initialize the data source needed by the job.

3. Collect data

   - After waiting for a period of time (60s), collect latency data during the job process and draw a plot of the latency curve.

If everything goes fine, you should see a green line of words on the terminal: ```"Sucessfully collect wordcount latency data."```. You should also find a ```latency_test.pdf``` in ```/home/ubuntu/data/test```, plotting the latency curve of the wordcount job.

### 2.2. Evaluating the Rescaling Performance of Meces

In this part, you can evaluate the rescaling performance of Meces using the wordcount job. You can compare it with that of ```Native Flink``` (restart) and ```Order-Unaware``` (block-based), as reported in our paper.

To do so, run the following command:

``` shell
/home/ubuntu/scripts/rescale_exp.sh
```

This script goes through three stages in series, namely `Meces`, `Order-Unaware`, `Native-Flink`.

Generally, each stage submits the ```wordcount``` job, runs for a while and then rescales the operator with its corresponding mechanism. After that, the experimental data is collected in the ```~/data``` folder. 

Specifically, each stage includes the following steps:

1. Prepare the environment

   - Clear historical data, initialize external services, and start Flink.

2. Run the Job

   - Submit the ```wordcount``` job and initialize the data source.

3. Rescale

   - After waiting for a while (120 seconds), start the rescaling process.

4. Collect data

   - Wait for a period of time (60s) after rescaling, collect the latency data during the job process and draw the plot of the latency curve.

If everything goes fine, you can see a green line of words on the terminal: ```"Sucessfully collect wordcount latency data."``` at the end of each experiment. You should also find plots of the latency curve of each experiment:

``` shell
/home/ubuntu/data/meces/latency_meces.pdf       # Meces
/home/ubuntu/data/order/latency_order.pdf       # Order-Unaware
/home/ubuntu/data/restart/latency_restart.pdf   # Native Flink
```

The plots should show that, compared with ```Native Flink``` (restart) and ```Order-Unaware``` (block-based), Meces can significantly lower the latency peak during rescaling, as reported in the paper.






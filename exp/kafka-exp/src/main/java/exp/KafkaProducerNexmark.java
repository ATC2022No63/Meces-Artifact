package exp;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class KafkaProducerNexmark {
    static class RecordGenerator {
        private Random rnd = new Random();
//        private CopyOnWriteArrayList<Long> personIds = new CopyOnWriteArrayList<>();

        RecordGenerator() throws IOException {
            getOnePerson();
        }

        String getOnePerson() {
            long id = rnd.nextInt(10);
            String name = "SomeBody";
            long now = System.currentTimeMillis();
//            personIds.add(id);
            return id + ":" + name + ":" + now;
        }

        String getOnePerson(long id) {
            String name = "SomeBody";
            long now = System.currentTimeMillis();
//            personIds.add(id);
            return id + ":" + name + ":" + now;
        }

        String getOneAuction() {
            long id = rnd.nextLong();
            // Probability of 1 in 10000 to generate a valid auction
            long sellerId = rnd.nextInt(10000000);
            long now = System.currentTimeMillis();
            long category = rnd.nextInt(1000000);
            long price = rnd.nextInt(10000);
            return id
                    + ":" + sellerId
                    + ":" + category
                    + ":" + price
                    + ":" + now;
        }

        String getOneBid() {
            long itemId = rnd.nextLong();
            long bidderId = rnd.nextInt(100000);
            int price = rnd.nextInt(10000000);
            long bidTime = System.currentTimeMillis();
            long timestamp = System.currentTimeMillis();
            return itemId + ":" + bidderId + ":" + price + ":" + bidTime + ":" + timestamp;
        }
    }

    public static void main(String[] args) throws Exception {
        final ParameterTool params = ParameterTool.fromArgs(args);
        String kafkaServers = params.get("kafkaServers", "localhost:9092");
        String kafkaAuctionTopic = params.get("AuctionTopic");
        String kafkaPersonTopic = params.get("PersonTopic");
        String kafkaBidTopic = params.get("BidTopic");
        String target = params.get("target");
        int period = Integer.parseInt(params.get("period"));
        int messageCountPerSec = params.getInt("rate", 10000);
        System.out.println(messageCountPerSec);
        System.out.println(kafkaAuctionTopic);
        System.out.println(kafkaPersonTopic);
        System.out.println(kafkaBidTopic);

        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServers);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        RecordGenerator recordGenerator = new RecordGenerator();
        Timer timer = new Timer();
        switch (target) {
            case "person":
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                for (int i = 0; i < 10; i++) {
                                    producer.send(new ProducerRecord<>(kafkaPersonTopic, recordGenerator.getOnePerson(i)));
                                }
                            }
                        },
                        5000, period
                );
                break;
            case "auction":
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                for (int i = 0; i < messageCountPerSec; i++) {
                                    producer.send(new ProducerRecord<>(kafkaAuctionTopic, recordGenerator.getOneAuction()));
                                }
                            }
                        },
                        5000, 1000
                );
                break;
            case "bid":
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                for (int i = 0; i < messageCountPerSec; i++) {
                                    producer.send(new ProducerRecord<>(kafkaBidTopic, recordGenerator.getOneBid()));
                                }
                            }
                        },
                        5000, 1000
                );
                break;
            default:
                System.out.println("Unknow target: " + target);
        }
    }
}

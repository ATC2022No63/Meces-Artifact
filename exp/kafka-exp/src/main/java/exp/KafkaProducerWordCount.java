package exp;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.kafka.clients.producer.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class KafkaProducerWordCount {

    static class RecordGenerator {
        private Random rnd = new Random();
        private String[] dictionary;
        private final int wordLength;

        RecordGenerator(String wordFilePath, int wordLength) throws IOException {
            this.wordLength = wordLength;
            if (wordFilePath != null) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(wordFilePath));
                ArrayList<String> wordList = new ArrayList<>();
                String strLine;
                while (null != (strLine = bufferedReader.readLine())) {
                    if (StringUtils.isBlank(strLine)) {
                        continue;
                    }
                    wordList.add(strLine);
                }
                dictionary = wordList.toArray(new String[0]);
            }
        }

        String getOneStringRecord() {
            return getOneRandomSentence();
//            return getOneSentenceFromDictionary();
        }

        private String getOneSentenceFromDictionary(){
            return dictionary[rnd.nextInt(dictionary.length)];
        }

        private String getOneRandomSentence(){
            return RandomStringUtils.random(wordLength, false, true);
        }
    }

    public static void main(String[] args) throws Exception {
        final ParameterTool params = ParameterTool.fromArgs(args);
//        String kafkaHost = params.get("KafkaHost", "localhost");
//        String kafkaPort = params.get("KafkaPort", "9092");
        String kafkaServers = params.get("kafkaServers", "slave201:9092,slave202:9092");
        String dictionaryFilePath = params.get("DictionaryFilePath", null);
        String kafkaTopic = params.get("KafkaTopic");
        int rate = params.getInt("rate", 10000);
        int wordLength = params.getInt("wordLength", 8);
        System.out.println(
                String.format(
                        "Generating random words of length: %d at a rate of %d/s",
                        wordLength,
                        rate));

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
        RecordGenerator recordGenerator = new RecordGenerator(dictionaryFilePath, wordLength);
        Timer timer = new Timer();
        final int messageCountPerSec = rate / 10;
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        for (int i = 0; i < messageCountPerSec; i++) {
                            producer.send(new ProducerRecord<>(kafkaTopic, System.currentTimeMillis() + ":" + recordGenerator.getOneStringRecord()));
                        }
                    }
                },
                1000, 100
        );
    }
}

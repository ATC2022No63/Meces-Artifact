package exp;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class TestJob {
    private static class RandomSource implements SourceFunction<Tuple2<String, Integer>> {
        private static final long serialVersionUID = 1L;

        private Random rnd = new Random();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        private volatile boolean isRunning = true;

        @Override
        public void run(SourceContext<Tuple2<String, Integer>> ctx) throws Exception {
            while (isRunning) {
                String first = df.format(new Date());
                int second = rnd.nextInt(10000);
                ctx.collect(new Tuple2<>(first, second));
                Thread.sleep(500L);
            }
        }

        @Override
        public void cancel() {
            isRunning = false;
        }
    }

    public static class Map_S_1 extends RichMapFunction<Tuple2<String, Integer>, String> {

        @Override
        public void open(Configuration parameters) {
            System.out.println("Subtask_" + getRuntimeContext().getIndexOfThisSubtask() + " is online.");
        }

        @Override
        public String map (Tuple2<String, Integer> item) {

            return item.f0 + "_map_" + getRuntimeContext().getIndexOfThisSubtask();
        }

        @Override
        public void close() {
            System.out.println("Subtask_" + getRuntimeContext().getIndexOfThisSubtask() + " is offline.");
        }
    }
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.disableOperatorChaining();
        DataStream<Tuple2<String, Integer>> ds = env.addSource(new RandomSource()).rebalance();
        ds.map(new Map_S_1())
//                .setParallelism(2)
                .rebalance()
            .addSink(new FlinkKafkaProducer<String>(
                "slave203:9092",
                "test_out_par_1",
                new SimpleStringSchema()
            ));
//        DataStream<String> text = env.readTextFile("file:///home/yinhan/flink-exp/data/Sonnet-12-words");
//        text.map((MapFunction<String, Integer>) Integer::parseInt)
//                .print();

        env.execute("Flink Streaming test job");
    }
}

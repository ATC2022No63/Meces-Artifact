package exp;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class TestWordCount {
    private static class RandomWordSource implements SourceFunction<String> {
        private static final long serialVersionUID = 1L;

        private Random rnd = new Random();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        private volatile boolean isRunning = true;

        @Override
        public void run(SourceContext<String> ctx) throws Exception {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("/home/yinhan/flink-exp/data/Sonnet-12-words"));
            while (isRunning) {
                String line = bufferedReader.readLine();
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                ctx.collect(line);
                Thread.sleep(50L);
            }
        }

        @Override
        public void cancel() {
            isRunning = false;
        }
    }
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<Tuple2<String, Integer>> dataStream = env
                .addSource(new RandomWordSource()).rebalance()
                .flatMap(new Splitter())
                .keyBy(value -> value.f0)
                .flatMap(new WordCountFlatMap());
//                .sum(1);

        dataStream.print();

        env.execute("Test WordCount");
    }

    public static class Splitter implements FlatMapFunction<String, Tuple2<String, Integer>> {
        @Override
        public void flatMap(String sentence, Collector<Tuple2<String, Integer>> out) throws Exception {
            for (String word: sentence.split(" ")) {
                out.collect(new Tuple2<>(word, 1));
            }
        }
    }

    public static class WordCountFlatMap extends RichFlatMapFunction<Tuple2<String, Integer>, Tuple2<String, Integer>> {

        private ValueState<Tuple2<String, Integer>> valueState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);

            // 配置 StateTTL(TimeToLive)
            StateTtlConfig ttlConfig = StateTtlConfig.newBuilder(Time.minutes(3))   // 存活时间
                    .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)  // 永远不返回过期的用户数据
                    .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)  // 每次写操作创建和更新时,修改上次访问时间戳
//                    .setTimeCharacteristic(StateTtlConfig.TimeCharacteristic.ProcessingTime) // 目前只支持 ProcessingTime
                    .build();

            // 创建 ValueStateDescriptor
            ValueStateDescriptor<Tuple2<String, Integer>> descriptor = new ValueStateDescriptor<>("wordCountStateDesc",
                    TypeInformation.of(new TypeHint<Tuple2<String, Integer>>() {}));

            // 激活 StateTTL
//            descriptor.enableTimeToLive(ttlConfig);

            // 基于 ValueStateDescriptor 创建 ValueState
            valueState = getRuntimeContext().getState(descriptor);

        }

        @Override
        public void flatMap(Tuple2<String, Integer> input, Collector<Tuple2<String, Integer>> collector) throws Exception {
            Tuple2<String, Integer> currentState = valueState.value();

            // 初始化 ValueState 值
            if (null == currentState) {
                currentState = new Tuple2<>(input.f0, 0);
            }

            Tuple2<String, Integer> newState = new Tuple2<>(currentState.f0, currentState.f1 + input.f1);

            // 更新 ValueState 值
            valueState.update(newState);

            collector.collect(newState);
        }
    }
}

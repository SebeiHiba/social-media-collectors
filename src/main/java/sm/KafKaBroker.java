package sm;

import java.util.Arrays;

import java.util.List;
import java.util.Properties;


import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import sm.wrapper.Wrapper;
import twitter4j.JSONException;

public class KafKaBroker {
    public Producer<String, String> producerSubcription() {
        System.out.println("kafka producer configuration");
        // identify topic name

        // create instance for properties to access producer configs
        Properties props = new Properties();
        // Assign localhost id
        props.put("bootstrap.servers", "localhost:9092");

        // Set acknowledgements for producer requests.
        props.put("acks", "all");

        // If the request fails, the producer can automatically retry,
        props.put("retries", 0);

        // Specify buffer size in config
        props.put("batch.size", 16384);

        // Reduce the no of requests less than 0
        props.put("linger.ms", 1);

        // The buffer.memory controls the total amount of memory available to
        // the producer for buffering.
        props.put("buffer.memory", 33554432);

        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");

        props.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        // Properties props= kafkaProducerConfig();
        Producer<String, String> producer = new KafkaProducer<String, String>(
                props);
        return producer;
    }

    public void sendMessage(Producer<String, String> producer,
                            String topicName, String post) {
        ProducerRecord<String, String> message = new ProducerRecord<String, String>(
                topicName, post);
        producer.send(message);
    }


    private static Consumer<Long, String> createConsumer( List<String> topics) {

        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "masternode:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                "KafkaExampleConsumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());

        // Create the consumer using props.
        final Consumer<Long, String> consumer =
                new KafkaConsumer<>(props);

        // Subscribe to the topic.
        // consumer.subscribe(Collections.singletonList(TOPIC));
        // consumer.subscribe(Arrays.asList(topics);
        consumer.subscribe(topics);
        return consumer;
    }
    //https://www.oreilly.com/library/view/kafka-the-definitive/9781491936153/ch04.html
    static void runConsumer(List<String> topics) throws InterruptedException {

        final Consumer<Long, String> consumer = createConsumer(topics);
        Wrapper wrapper =new Wrapper  ();

        final int giveUp = 100;   int noRecordsCount = 0;

        while (true) {
            final ConsumerRecords<Long, String> consumerRecords =
                    consumer.poll(1000);

            if (consumerRecords.count()==0) {
                noRecordsCount++;
                if (noRecordsCount > giveUp) break;
                else continue;
            }

            consumerRecords.forEach(record -> {
                System.out.printf("Consumer Record:(%d, %s, %d, %d)\n",
                        record.key(), record.value(),
                        record.partition(), record.offset());
            });
            consumerRecords.forEach(record -> {
                String topic=record.topic();
                try {
                    wrapper.mapp(record.value(),topic);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            });

            consumer.commitAsync();
        }
        consumer.close();
        System.out.println("DONE");
    }
    public static void main(String[] args){
        List<String> topics = Arrays.asList("twitterPost", "youtubePost");


        try {
            runConsumer(topics);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }



}

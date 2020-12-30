package com.example.demo2.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.util.Properties;


/**
 * @className   Producer
 * @mehtod      produce
 * @description producer properties 설정 후 MSG 전송
 */
public class Producer {
    public static void produce(String brokers, String topicName, String log) throws IOException {
        // kafka producer prop
        Properties properties = new Properties();
        // broker server list
        properties.setProperty("bootstrap.servers", brokers);
        // String -> ByteArray
        properties.setProperty("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty("value.serializer","org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // 전송
        try {
            producer.send(new ProducerRecord<String, String>(topicName, log)).get();
        }
        catch (Exception ex) {
            System.out.print(ex.getMessage());
            throw new IOException(ex.toString());
        }

    }
}
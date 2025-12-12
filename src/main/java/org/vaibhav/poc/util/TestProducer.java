package org.vaibhav.poc.util;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class TestProducer {
    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        String json = """
        {
          "partnerId": 101,
          "eventType": "REVENUE_MISMATCH",
          "description": "Partner ABC reported mismatched revenue for Q3",
          "timestamp": "2025-12-11T17:00:00Z"
        }
        """;

        ProducerRecord<String, String> record =
                new ProducerRecord<>("partner-events", json);

        producer.send(record, (metadata, ex) -> {
            if (ex != null) {
                ex.printStackTrace();
            } else {
                System.out.println("Message sent to " + metadata.topic() + " offset=" + metadata.offset());
            }
        });

        producer.flush();
        producer.close();
    }
}

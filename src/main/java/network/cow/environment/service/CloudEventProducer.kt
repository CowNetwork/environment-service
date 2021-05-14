package network.cow.environment.service

import network.cow.cloudevents.kafka.CloudEventKafkaProducer
import network.cow.cloudevents.kafka.config.EnvironmentProducerConfig

/**
 * @author Benedikt Wüller
 */
object CloudEventProducer : CloudEventKafkaProducer(EnvironmentProducerConfig("ENVIRONMENT_SERVICE"))

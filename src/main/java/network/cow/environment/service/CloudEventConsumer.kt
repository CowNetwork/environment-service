package network.cow.environment.service

import network.cow.cloudevents.kafka.CloudEventKafkaConsumer
import network.cow.cloudevents.kafka.config.EnvironmentConsumerConfig

/**
 * @author Benedikt Wüller
 */
object CloudEventConsumer : CloudEventKafkaConsumer(EnvironmentConsumerConfig("ENVIRONMENT_SERVICE"))

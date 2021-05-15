# environment-service
WebSocket service for the environment audio system.

## Environment variables

| Name | Default | Description |
| ---- | ------- | ----------- |
| `ENVIRONMENT_SERVICE_PORT` | `35721` | The port the service will listen on. |
| `ENVIRONMENT_SERVICE_BASE_URL` | `http://moo.cow.sexy/35721` | The base url used to create the individual consumer urls. |
| `ENVIRONMENT_SERVICE_USERNAME` | `environment` | TODO |
| `ENVIRONMENT_SERVICE_PASSWORD` | `environment` | TODO |
| `ENVIRONMENT_SERVICE_PUBLIC_HOST` | `localhost` | TODO |
| `ENVIRONMENT_SERVICE_PUBLIC_PORT` | `$ENVIRONMENT_SERVICE_PORT` | TODO |
| `ENVIRONMENT_SERVICE_KAFKA_BROKERS` | `127.0.0.1:9092` | TODO |
| `ENVIRONMENT_SERVICE_CLOUD_EVENT_SOURCE` | `cow.undefined.undefined` | TODO |
| `ENVIRONMENT_SERVICE_KAFKA_TOPICS` | ` ` | TODO |
| `ENVIRONMENT_SERVICE_GROUP_ID_PREFIX` | `$ENVIRONMENT_SERVICE_CLOUD_EVENT_SOURCE` | TODO |
| `ENVIRONMENT_SERVICE_KAFKA_PRODUCER_TOPIC` | `cow.global.environment` | TODO |

package network.cow.environment.service

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.*
import network.cow.environment.service.consumer.consumerWebSocketRoute
import network.cow.environment.service.producer.producerWebSocketRoute
import java.time.Duration

/**
 * @author Benedikt Wüller
 */

fun main() {
    val internalPort = System.getenv("ENVIRONMENT_SERVICE_PORT")?.toInt() ?: 35721
    val internalUsername = System.getenv("ENVIRONMENT_SERVICE_USERNAME") ?: "environment"
    val internalPassword = System.getenv("ENVIRONMENT_SERVICE_PASSWORD") ?: "environment"

    embeddedServer(Netty, port = internalPort) {
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(60)
            timeout = Duration.ofSeconds(300)
        }
        install(Authentication) {
            basic("auth-basic-internal") {
                realm = "Access to the '/' path"
                validate { credentials ->
                    if (credentials.name == internalUsername && credentials.password == internalPassword) {
                        UserIdPrincipal(credentials.name)
                    } else {
                        null
                    }
                }
            }
        }
        routing {
            authenticate("auth-basic-internal") {
                producerWebSocketRoute()
            }
            consumerWebSocketRoute()
        }
    }.start(true)
}

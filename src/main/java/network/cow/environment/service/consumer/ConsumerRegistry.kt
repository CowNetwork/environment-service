package network.cow.environment.service.consumer

import com.google.common.collect.HashBiMap
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import network.cow.environment.service.database.ConsumerState
import network.cow.environment.service.database.DatabaseService
import network.cow.environment.service.database.dao.Consumer
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object ConsumerRegistry {

    private val sessions = HashBiMap.create<UUID, WebSocketServerSession>()

    fun getSession(uuid: UUID) = sessions[uuid]

    fun getSessionId(session: WebSocketServerSession) = sessions.inverse()[session]

    fun connect(id: UUID, session: WebSocketServerSession) {
        sessions[id] = session
        transaction(DatabaseService.database) {
            val consumer = Consumer.findById(id) ?: return@transaction
            consumer.state = ConsumerState.CONNECTED
        }
    }

    suspend fun disconnect(session: WebSocketServerSession) {
        session.close()
        val id = this.sessions.inverse().remove(session) ?: return
        transaction(DatabaseService.database) {
            val consumer = Consumer.findById(id) ?: return@transaction
            consumer.state = ConsumerState.REGISTERED
        }
    }

}

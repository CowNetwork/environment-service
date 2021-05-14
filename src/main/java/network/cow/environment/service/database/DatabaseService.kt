package network.cow.environment.service.database

import network.cow.environment.service.database.table.Consumers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * @author Benedikt Wüller
 */
object DatabaseService {

    val database: Database

    init {
        val host = System.getenv("ENVIRONMENT_SERVICE_POSTGRES_HOST") ?: "127.0.0.1:5432"
        val db = System.getenv("ENVIRONMENT_SERVICE_POSTGRES_DB") ?: "postgres"
        val username = System.getenv("ENVIRONMENT_SERVICE_POSTGRES_USERNAME") ?: "postgres"
        val password = System.getenv("ENVIRONMENT_SERVICE_POSTGRES_PASSWORD") ?: "postgres"
        val schema = System.getenv("ENVIRONMENT_SERVICE_POSTGRES_SCHEMA") ?: "public"

        this.database = Database.connect("jdbc:postgresql://$host/$db?currentSchema=$schema", "org.postgresql.Driver", username, password)

        transaction (this.database) {
            // Make sure the tables exist.
            SchemaUtils.createMissingTablesAndColumns(Consumers)
        }
    }

}

package network.cow.environment.service

import com.google.gson.GsonBuilder

/**
 * @author Benedikt Wüller
 */
object JsonService {

    private val gson = GsonBuilder().create()

    fun toJson(any: Any) = this.gson.toJson(any)

    fun <T : Any> fromJson(json: String, type: Class<T>) = this.gson.fromJson(json, type)

}

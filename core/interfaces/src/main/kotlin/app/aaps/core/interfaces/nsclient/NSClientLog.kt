package app.aaps.core.interfaces.nsclient

import kotlinx.serialization.json.JsonElement

class NSClientLog(
    val action: String,
    val logText: String? = null,
    val json: JsonElement? = null
) {

    var date = System.currentTimeMillis()
}

package app.aaps.plugins.sync.nsShared.mvvm

import app.aaps.core.interfaces.utils.DateUtil

class NSClientLog(private val newEntry: Pair<String, String?>) {

    var date = System.currentTimeMillis()
    var formatted: String? = null

    fun toPreparedHtml(dateUtil: DateUtil) =
        formatted ?: StringBuilder().also {
            it.append(dateUtil.timeStringWithSeconds(date))
            it.append(" <b>")
            it.append(newEntry.first)
            it.append("</b> ")
            it.append(newEntry.second)
        }.toString().also {
            formatted = it
        }
}
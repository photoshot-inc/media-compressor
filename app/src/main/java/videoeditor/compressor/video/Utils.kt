package videoeditor.compressor.video

import android.util.StatsLog.logEvent
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


object Utils {
    @JvmStatic
    fun Long.readableSize(): String {
        val si: Boolean = false
        val unit = if (si) 1000 else 1024
        if (this < unit) return "$this B"
        val exp = (Math.log(this.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1].toString() + if (si) "" else "i"
        return String.format(
            Locale.ENGLISH,
            "%.1f %sB",
            this / Math.pow(unit.toDouble(), exp.toDouble()),
            pre
        )
    }

    @JvmStatic
    fun Long.toFormattedDuration(): String {
        val date = Date(this)
        val formatter: DateFormat = SimpleDateFormat("HH:mm:ss.SSS")
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(date)

    }

    @JvmStatic
    fun Long.toBitrateStr(): String {
        return ""
    }
}
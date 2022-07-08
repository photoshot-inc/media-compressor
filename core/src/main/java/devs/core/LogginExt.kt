package devs.core

import com.noveogroup.android.log.LoggerManager

private val logger = LoggerManager.getLogger()
fun String.logAsMessage() {
    logger.i(this)
}

fun String.logAsInfo() {
    logger.i(this)
}

fun Throwable.logError(message: String? = null) {
    logger.e(message, this)
}

fun Any.logObject() {
    logger.d(this.toString())
}

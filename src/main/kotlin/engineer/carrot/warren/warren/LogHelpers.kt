package engineer.carrot.warren.warren

import org.slf4j.LoggerFactory

inline fun <reified T : Any> loggerFor(): org.slf4j.Logger = LoggerFactory.getLogger(T::class.java)
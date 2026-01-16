package com.eclipse.cobblemon.flan.di

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface CobblemonFlanLoggerService {
    fun info(message: String)
    fun debug(message: String)
    fun warn(message: String)
    fun warn(message: String, throwable: Throwable)
    fun error(message: String)
    fun error(message: String, throwable: Throwable)
}

class CobblemonFlanLoggerServiceImpl(modId: String = "CobblemonFlan") : CobblemonFlanLoggerService {
    private val logger: Logger = LoggerFactory.getLogger(modId)

    override fun info(message: String) = logger.info(message)
    override fun debug(message: String) = logger.debug(message)
    override fun warn(message: String) = logger.warn(message)
    override fun warn(message: String, throwable: Throwable) = logger.warn(message, throwable)
    override fun error(message: String) = logger.error(message)
    override fun error(message: String, throwable: Throwable) = logger.error(message, throwable)
}

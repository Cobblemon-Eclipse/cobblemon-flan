package com.eclipse.cobblemon.flan.config

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

object CobblemonFlanConfig {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val configPath: Path = FabricLoader.getInstance().configDir.resolve("cobblemon-flan").resolve("config.json")

    @Volatile
    var config: CobblemonFlanConfigData = CobblemonFlanConfigData()
        private set

    fun load() {
        if (!Files.exists(configPath)) {
            config = CobblemonFlanConfigData()
            save()
            return
        }
        Files.newBufferedReader(configPath).use { reader ->
            val parsed: CobblemonFlanConfigData? = gson.fromJson(reader, CobblemonFlanConfigData::class.java)
            config = parsed ?: CobblemonFlanConfigData()
        }
    }

    fun save() {
        Files.createDirectories(configPath.parent)
        Files.newBufferedWriter(
            configPath,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        ).use { writer ->
            gson.toJson(config, writer)
        }
    }

    fun reload() {
        load()
    }
}

data class CobblemonFlanConfigData(
    val protections: ProtectionSettings = ProtectionSettings(),
    val messages: MessageSettings = MessageSettings()
)

data class ProtectionSettings(
    // Wild Pokemon spawning protection
    val preventWildSpawns: Boolean = true,

    // Pokeball throwing/catching protection
    val preventCatching: Boolean = true,

    // Battle initiation protection
    val preventBattles: Boolean = true,

    // Pokemon send out protection
    val preventSendOut: Boolean = true,

    // Pokemon riding protection
    val preventRiding: Boolean = true,

    // Display case protection (prevent stealing items)
    val preventDisplayCaseInteraction: Boolean = true,

    // Allow claim owners to always bypass
    val ownerBypass: Boolean = true
)

data class MessageSettings(
    val prefix: String = "<red>[Flan] </red>",
    val cannotCatch: String = "<yellow>You cannot catch Pokemon in this claim!</yellow>",
    val cannotBattle: String = "<yellow>You cannot battle Pokemon in this claim!</yellow>",
    val cannotSendOut: String = "<yellow>You cannot send out Pokemon in this claim!</yellow>",
    val cannotRide: String = "<yellow>You cannot ride Pokemon in this claim!</yellow>",
    val cannotUseDisplayCase: String = "<yellow>You cannot interact with display cases in this claim!</yellow>"
)

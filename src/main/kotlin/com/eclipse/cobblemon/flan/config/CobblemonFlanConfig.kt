package com.eclipse.cobblemon.flan.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
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
            val parsed = gson.fromJson(reader, JsonObject::class.java)
            if (parsed == null) {
                config = CobblemonFlanConfigData()
                return
            }
            // Gson builds objects without calling the Kotlin constructor, so any key missing from
            // an older config.json would load as false/null instead of its default. Overlay the
            // old file onto a fresh default so settings added in later versions keep their intended
            // defaults (e.g. new prevent* flags stay ON, new message strings stay non-null).
            val merged = gson.toJsonTree(CobblemonFlanConfigData()).asJsonObject
            deepMerge(merged, parsed)
            config = gson.fromJson(merged, CobblemonFlanConfigData::class.java)
        }
        save() // persist any settings the old file was missing
    }

    // Overlay `overrides` onto `base`: nested objects merge key-by-key, everything else is
    // overwritten. Keys absent from `overrides` keep base's default.
    private fun deepMerge(base: JsonObject, overrides: JsonObject) {
        for ((key, value) in overrides.entrySet()) {
            val existing = base.get(key)
            if (existing != null && existing.isJsonObject && value.isJsonObject) {
                deepMerge(existing.asJsonObject, value.asJsonObject)
            } else {
                base.add(key, value)
            }
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

    // Allow honey-lure (saccharine log slathered) spawns even when preventWildSpawns is true
    val allowHoneyLureSpawns: Boolean = true,

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

    // PC use protection
    val preventPCUse: Boolean = true,

    // Healing Machine use protection
    val preventHealingMachineUse: Boolean = true,

    // Honey lure (honey bottle on saccharine log) placement protection
    val preventHoneyLurePlacement: Boolean = true
)

data class MessageSettings(
    val prefix: String = "<red>[Flan] </red>",
    val cannotCatch: String = "<yellow>You cannot catch Pokemon in this claim!</yellow>",
    val cannotBattle: String = "<yellow>You cannot battle Pokemon in this claim!</yellow>",
    val cannotSendOut: String = "<yellow>You cannot send out Pokemon in this claim!</yellow>",
    val cannotRide: String = "<yellow>You cannot ride Pokemon in this claim!</yellow>",
    val cannotUseDisplayCase: String = "<yellow>You cannot interact with display cases in this claim!</yellow>",
    val cannotUsePC: String = "<yellow>You cannot use the PC in this claim!</yellow>",
    val cannotUseHealingMachine: String = "<yellow>You cannot use the Healing Machine in this claim!</yellow>",
    val cannotPlaceHoneyLure: String = "<yellow>You cannot place honey lures in this claim!</yellow>"
)

package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.api.impl.BukkitComponentManager
import me.arasple.mc.trchat.module.display.ChatSession
import me.arasple.mc.trchat.module.internal.TrChatBukkit
import me.arasple.mc.trchat.module.internal.command.main.CommandMute
import me.arasple.mc.trchat.module.internal.data.PlayerData
import me.arasple.mc.trchat.module.internal.script.Condition
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.util.unsafeLazy
import taboolib.expansion.DataContainer
import taboolib.expansion.playerDataContainer
import taboolib.expansion.playerDatabase
import taboolib.module.chat.ComponentText
import taboolib.module.ui.MenuHolder
import taboolib.module.ui.type.Basic
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.sendLang

val isDragonCoreHooked by unsafeLazy { Bukkit.getPluginManager().isPluginEnabled("DragonCore") }
private val noClickBasic = object : Basic() {
    init {
        rows(6)
        onClick(lock = true)
    }
}

@Suppress("Deprecation")
fun createNoClickInventory(size: Int, title: String) =
    Bukkit.createInventory(MenuHolder(noClickBasic), size, title)

fun String?.toCondition() = if (this == null) Condition.EMPTY else Condition(this)

fun Condition?.pass(commandSender: CommandSender): Boolean {
    return if (commandSender is Player) {
        this?.eval(commandSender) != false
    } else {
        true
    }
}

fun Player.passPermission(permission: String?): Boolean {
    return permission.isNullOrEmpty()
            || permission.equals("null", ignoreCase = true)
            || permission.equals("none", ignoreCase = true)
            || hasPermission(permission)
}

fun String.setPlaceholders(sender: CommandSender): String {
    return try {
        if (sender is Player) {
            replacePlaceholder(sender)
        } else {
            this
        }
    } catch (t: Throwable) {
        t.print("Error occurred when parsing placeholder!This is not a bug of TrChat")
        this
    }
}

inline val Player.session get() = ChatSession.getSession(this)

inline val OfflinePlayer.data get() = PlayerData.getData(this)

fun Player.checkMute(): Boolean {
    if (TrChatBukkit.isGlobalMuting && !hasPermission("trchat.bypass.globalmute")) {
        sendLang("General-Global-Muting")
        return false
    }
    val data = data
    if (data.isMuted) {
        sendLang("General-Muted", CommandMute.muteDateFormat.format(data.muteTime), data.muteReason)
        return false
    }
    return true
}

fun OfflinePlayer.getDataContainer(): DataContainer {
    return if (isOnline) {
        playerDataContainer[uniqueId] ?: error("Database is not initialized")
    } else {
        playerDataContainer.computeIfAbsent(uniqueId) { DataContainer(uniqueId.parseString(), playerDatabase!!) }
    }
}

fun Any.sendComponent(sender: Any?, component: ComponentText) {
    BukkitComponentManager.sendComponent(this, component, sender)
}

fun Player.getCooldownLeft(type: CooldownType) = Cooldowns.getCooldownLeft(uniqueId, type.alias)

fun Player.isInCooldown(type: CooldownType) = Cooldowns.isInCooldown(uniqueId, type.alias)

fun Player.updateCooldown(type: CooldownType, lasts: Long) = Cooldowns.updateCooldown(uniqueId, type.alias, lasts)
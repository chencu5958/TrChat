package me.arasple.mc.trchat.module.internal.data

import me.arasple.mc.trchat.util.getDataContainer
import me.arasple.mc.trchat.util.toUUID
import org.bukkit.OfflinePlayer
import taboolib.common5.cbool
import taboolib.common5.clong
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author ItsFlicker
 * @since 2022/6/25 18:17
 */
class PlayerData(val player: OfflinePlayer) {

    init {
        if (isVanishing) {
            vanishing += player.name!!
        }
    }

    val isSpying get() = player.getDataContainer()["spying"].cbool

    val isFilterEnabled get() = player.getDataContainer()["filter"]?.cbool ?: true

    val muteTime get() = player.getDataContainer()["mute_time"].clong

    val isMuted get() = muteTime > System.currentTimeMillis()

    val muteReason
        get() = player.getDataContainer()["mute_reason"] ?: "null"

    val isVanishing get() = player.getDataContainer()["vanish"].cbool

    val ignored get() = player.getDataContainer()["ignored"]?.split(",")?.map { it.toUUID() } ?: emptyList()

    fun selectColor(color: String) {
        player.getDataContainer()["color"] = color
    }

    fun setFilter(value: Boolean) {
        player.getDataContainer()["filter"] = value
    }

    fun updateMuteTime(time: Long) {
        player.getDataContainer()["mute_time"] = System.currentTimeMillis() + time
    }

    fun setMuteReason(reason: String) {
        player.getDataContainer()["mute_reason"] = reason
    }

    fun switchSpy(): Boolean {
        player.getDataContainer()["spying"] = !isSpying
        return isSpying
    }

    fun switchVanish(): Boolean {
        player.getDataContainer()["vanish"] = !isVanishing
        return isVanishing.also {
            if (it) vanishing += player.name!! else vanishing -= player.name!!
        }
    }

    fun addIgnored(uuid: UUID) {
        val new = (player.getDataContainer()["ignored"]?.split(",") ?: return) + uuid.toString()
        player.getDataContainer()["ignored"] = new
    }

    fun removeIgnored(uuid: UUID) {
        val new = (player.getDataContainer()["ignored"]?.split(",") ?: return) - uuid.toString()
        player.getDataContainer()["ignored"] = new
    }

    fun switchIgnored(uuid: UUID): Boolean {
        return if (ignored.contains(uuid)) {
            removeIgnored(uuid)
            false
        } else {
            addIgnored(uuid)
            true
        }
    }

    companion object {

        @JvmField
        val data = ConcurrentHashMap<UUID, PlayerData>()

        val vanishing = mutableSetOf<String>()

        fun getData(player: OfflinePlayer): PlayerData {
            return data.computeIfAbsent(player.uniqueId) {
                PlayerData(player)
            }
        }

    }
}
package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.module.conf.file.Filters
import me.arasple.mc.trchat.module.conf.file.Settings
import me.arasple.mc.trchat.module.internal.TrChatBukkit
import me.arasple.mc.trchat.util.color.MessageColors
import me.arasple.mc.trchat.util.legacyColorToTag
import me.arasple.mc.trchat.util.miniMessage
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptPlayer
import taboolib.platform.util.isAir
import taboolib.platform.util.modifyMeta

/**
 * @author ItsFlicker
 * @date 2019/8/15 21:18
 */
@PlatformSide([Platform.BUKKIT])
object ListenerAnvilChange {

    @Suppress("Deprecation")
    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onAnvilCraft(e: PrepareAnvilEvent) {
        val p = e.view.player
        val result = e.result

        if (e.inventory.type != InventoryType.ANVIL || result.isAir()) {
            return
        }
        result!!.modifyMeta<ItemMeta> {
            if (!hasDisplayName()) {
                return@modifyMeta
            }
            if (Filters.CONF.getBoolean("Enable.Anvil")) {
                setDisplayName(TrChat.api().getFilterManager().filter(displayName, player = adaptPlayer(p)).filtered)
            }
            if (Settings.CONF.getBoolean("Color.Anvil")) {
                setDisplayName(MessageColors.replaceWithPermission(p, displayName, MessageColors.Type.ANVIL))
            }
            if (TrChatBukkit.isPaperEnv && p.hasPermission("trchat.color.anvil.minimessage")) {
                displayName(miniMessage.deserialize(displayName.legacyColorToTag()))
            }
        }
        e.result = result
    }
}
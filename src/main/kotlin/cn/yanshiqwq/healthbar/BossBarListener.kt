package cn.yanshiqwq.healthbar

import cn.yanshiqwq.healthbar.Main.Companion.INSTANCE
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.bossbar.*
import net.kyori.adventure.bossbar.BossBar.Color
import net.kyori.adventure.bossbar.BossBar.Overlay
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.scheduler.BukkitTask

/**
 * healthbar
 * cn.yanshiqwq.healthbar.BossBarListener
 *
 * @author yanshiqwq
 * @since 2024/6/5 17:12
 */

class BossBarListener : Listener {
    @EventHandler
    fun onPlayerDamageEntity(event: EntityDamageByEntityEvent) {
        val damagerPlayer: Player = when (val damager = event.damager) {
            is Player -> damager
            is Projectile -> if (damager.shooter is Player) damager.shooter as Player else return
            is TNTPrimed -> if (damager.source is Player) damager.source as Player else return
            else -> return
        }
        updateBossBar(damagerPlayer, event)
    }

    companion object {
        private val bars = mutableMapOf<Player, Pair<BossBar, BukkitTask>>()
        fun updateBossBar(player: Player, event: EntityDamageByEntityEvent) {
            val entity = event.entity
            if (entity !is LivingEntity) return

            bars[player]?.second?.cancel()

            val damage = event.finalDamage
            val currentHealth: Double = (entity.health - damage).coerceAtLeast(0.0)
            val maxHealth: Double = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value?.coerceAtLeast(0.0)
                ?: 0.0
            val progress = (currentHealth / maxHealth).coerceAtLeast(0.0)

            val barColor = when (progress) {
                in 0.75..1.0 -> Color.GREEN
                in 0.35..0.75 -> Color.YELLOW
                else -> Color.RED
            }

            val barOverlay = when (maxHealth) {
                in 0.0..20.0 -> Overlay.NOTCHED_6
                in 20.0..40.0 -> Overlay.NOTCHED_10
                in 40.0..Double.POSITIVE_INFINITY -> Overlay.NOTCHED_12
                else -> Overlay.PROGRESS
            }

            val format = "%.3f"
            val nameComponent = entity.customName() ?: Component.translatable(entity.type.translationKey())
            val percentComponent = Component.text("${String.format(format, progress * 100)}%", NamedTextColor.RED)
            val splitter = Component.text(" | ", NamedTextColor.GRAY)
            val healthComponent = Component.text(String.format("$format / $format", currentHealth, maxHealth), NamedTextColor.WHITE)
            val damageComponent = Component.text(String.format("-$format", damage), NamedTextColor.RED)
            val barName = nameComponent
                .append(Component.text(" [", NamedTextColor.GRAY))
                .append(percentComponent)
                .append(Component.text("]", NamedTextColor.GRAY))
                .append(splitter)
                .append(healthComponent)
                .append(Component.text(" (", NamedTextColor.GRAY))
                .append(damageComponent)
                .append(Component.text(")", NamedTextColor.GRAY))

            val bossBar = bars[player]?.first ?: BossBar.bossBar(barName, progress.toFloat(), barColor, barOverlay)
            bossBar.name(barName)
            bossBar.progress(progress.toFloat())
            bossBar.color(barColor)
            bossBar.overlay(barOverlay)
            bossBar.addViewer(player)

            // timer - 4s remove
            val task = Bukkit.getScheduler().runTaskLater(INSTANCE!!, Runnable {
                bossBar.removeViewer(player)
            }, 80)

            bars[player] = Pair(bossBar, task)
        }
    }
}


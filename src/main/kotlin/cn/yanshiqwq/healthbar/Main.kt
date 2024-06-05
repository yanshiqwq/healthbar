package cn.yanshiqwq.healthbar

import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    override fun onEnable() {
        INSTANCE = this
        LOGGER = logger
        server.pluginManager.registerEvents(BossBarListener(), this)
        logger.info("Plugin enabled")
    }

    override fun onDisable() {
        logger.info("Plugin disabled")
        INSTANCE = null
    }

    companion object {
        var INSTANCE: Plugin? = null
        var LOGGER: java.util.logging.Logger? = null
    }
}

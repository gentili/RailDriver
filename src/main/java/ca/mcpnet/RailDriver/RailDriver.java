package ca.mcpnet.RailDriver;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class RailDriver extends JavaPlugin {
	
	Logger log = Logger.getLogger("Minecraft");

	public void onEnable() {
		log.info("RailDriver Plugin Enabled!");
	}
	public void onDisable() {
		log.info("RailDriver Plugin Disabled!");		
	}
}

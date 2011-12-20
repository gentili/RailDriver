package ca.mcpnet.RailDriver;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

public class RailDriverPlayerListener extends PlayerListener {

	private RailDriver plugin;
	public RailDriverPlayerListener(RailDriver instance) {
		plugin = instance;
	}
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		// plugin.log.info(event.getAction().name()+":"+event.getEventName()+":"+event.getClickedBlock().getType().name());
	}	
}

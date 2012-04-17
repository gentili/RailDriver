package ca.mcpnet.RailDriver;

import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class RailDriverWorldListener implements Listener {

	private RailDriver plugin;
	public RailDriverWorldListener(RailDriver instance) {
		plugin = instance;
	}

//	@EventHandler(priority = EventPriority.NORMAL)
//	public void onChunkLoad(ChunkLoadEvent event) {		
//		RailDriver.log(event.getEventName()+":"+event.getChunk());
//	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event) {
		plugin.stopPlayerRailDrivers(event.getPlayer());
	}
}

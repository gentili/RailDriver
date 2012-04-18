package ca.mcpnet.RailDriver;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RailDriverPlayerListener implements Listener {
	
	private RailDriver plugin;
	public RailDriverPlayerListener(RailDriver instance) {
		plugin = instance;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event) {
		plugin.stopPlayerRailDrivers(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.hasBlock()) {
			Block interactedBlock = event.getClickedBlock();
			if(interactedBlock != null && interactedBlock.getType() == Material.LEVER) {
				if(interactedBlock.getBlockPower() > 0) {
					// Block is already powered, we're about to turn it off
					RailDriverTask task = plugin.findRailDriverTask(interactedBlock);
					if(task != null) {
						task.deactivate();
					}
				} else {
					// Block isn't powered, this interaction will turn it on
					if(plugin.isRailDriver(interactedBlock)) {
						RailDriverTask task = plugin.findCreateRailDriverTask(interactedBlock);
						task.activate(event.getPlayer());
					}
				}
			}
		}
	}
}

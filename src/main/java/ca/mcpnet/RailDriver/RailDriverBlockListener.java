package ca.mcpnet.RailDriver;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockRedstoneEvent;

public class RailDriverBlockListener implements Listener {

	private RailDriver plugin;

	public RailDriverBlockListener(RailDriver instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		if (event.getBlock().getType() == Material.LEVER) {
			if (event.getOldCurrent() > event.getNewCurrent()) {
				// If theres a raildriver task associated with this lever then 
				// we're attempting to deactivate an existing raildriver
				RailDriverTask task = plugin.findRailDriverTask(event.getBlock());
				if (task != null) {
					task.deactivate();
				}
			}
			if (plugin.isRailDriver(event.getBlock())) {
				if (event.getOldCurrent() < event.getNewCurrent()) {
					// We're attempting to activate a raildriver
					RailDriverTask task = plugin.findCreateRailDriverTask(event.getBlock());
					task.activate();
				}
			}
		}
	}
}

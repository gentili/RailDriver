package ca.mcpnet.RailDriver;

import org.bukkit.Material;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;

public class RailDriverBlockListener extends BlockListener {

	private RailDriver plugin;

	public RailDriverBlockListener(RailDriver instance) {
		plugin = instance;
	}

	@Override
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		if (event.getBlock().getType() == Material.LEVER) {
			if (event.getOldCurrent() > event.getNewCurrent()) {
				// If theres a raildriver task associated with this lever then 
				// we're attempting to deactivate an existing raildriver
				RailDriverTask task = plugin.findRemoveRailDriverTask(event.getBlock());
				if (task != null) {
					task.deactivate();
				}
			}
			if (plugin.isRailDriver(this, event.getBlock())) {
				if (event.getOldCurrent() < event.getNewCurrent()) {
					// We're attempting to activate a raildriver
					RailDriverTask task = plugin.findCreateRailDriverTask(event.getBlock());
					task.activate();
				}
			}
		}
	}
}

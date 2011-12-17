package ca.mcpnet.RailDriver;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

public class RailDriverBlockListener extends BlockListener {

	private RailDriver plugin;

	public RailDriverBlockListener(RailDriver instance) {
		plugin = instance;
	}
	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		plugin.log.info("A block broke!");
		event.getPlayer().sendMessage("You may not break blocks!");
		event.setCancelled(true);
	}
}

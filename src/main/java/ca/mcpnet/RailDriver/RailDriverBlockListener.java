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
			RailDriver.log.info(event.getOldCurrent()+"->"+event.getNewCurrent());
			if (plugin.isRailDriver(this, event.getBlock())) {
				RailDriver.log.info("It's a raildriver!");
			} else {
				RailDriver.log.info("Not a raildriver!");
			}
		}
	}
}

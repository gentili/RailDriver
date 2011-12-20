package ca.mcpnet.RailDriver;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Lever;

public class RailDriverTask implements Runnable {

	private RailDriver plugin;
	private int x,y,z;
	private BlockFace direction;
	private boolean shutdown;
	private int taskid;
	
	RailDriverTask(RailDriver instance, Block block) {
		plugin = instance;
		x = block.getX();
		y = block.getY();
		z = block.getZ();
		Lever lever = new Lever(block.getType(),block.getData());
		direction = lever.getAttachedFace();
		shutdown = false;
		taskid = -1;
	}
	
	public void run() {
		plugin.log.info("Updating Raildriver "+taskid);
	}
	
	public boolean matchBlock(Block block) {
		if (block.getLocation().getBlockX() == x &&
				block.getLocation().getBlockY() == y &&
				block.getLocation().getBlockZ() == z) {
			return true;
		}
		return false;
	}
	public void activate() {
		if (taskid != -1) {
			plugin.log.warning("Activation requested on already active raildriver "+taskid);
			return;
		}
		taskid = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 20L, 20L);
	}
	
	public void deactivate() {
		if (taskid == -1) {
			plugin.log.warning("Deactivation requestd for already inactive raildriver!");
			return;
		}
		plugin.getServer().getScheduler().cancelTask(taskid);
	}
}

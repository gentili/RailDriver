package ca.mcpnet.RailDriver;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Lever;
import org.bukkit.util.Vector;

import ca.mcpnet.RailDriver.RailDriver.Facing;

public class RailDriverTask implements Runnable {

	private RailDriver plugin;
	private int x,y,z;
	private BlockFace direction;
	private boolean shutdown;
	private World world;
	private int taskid;
	
	int i;
	
	RailDriverTask(RailDriver instance, Block block) {
		plugin = instance;
		x = block.getX();
		y = block.getY();
		z = block.getZ();
		Lever lever = new Lever(block.getType(),block.getData());
		direction = lever.getAttachedFace();
		shutdown = false;
		world = block.getWorld();
		taskid = -1;
		
		i = 0;
	}
	
	Block getRelativeBlock(int i, int j, int k) {
		if (direction == BlockFace.NORTH) {
			return world.getBlockAt(x-i,y-1+k,z+1-j);
		} else if (direction == BlockFace.EAST) {
			return world.getBlockAt(x-1+j,y-1+k,z-i);
		} else if (direction == BlockFace.WEST) {
			return world.getBlockAt(x+1-j,y-1+k,z+i);
		} else if (direction == BlockFace.SOUTH) {
			return world.getBlockAt(x+i,y-1+k,z-1+j);
		} else {
			return null;
		}
	}

	public void run() {
		RailDriver.log.info("Updating Raildriver "+taskid);
		/*
		Block block = this.getRelativeBlock(1, 0, i);
		i++;
		RailDriver.log.info(block.getType().name());
		// world.createExplosion(x, y, z, 0);
		// world.playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
		// Light the fires
		*/
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
			RailDriver.log.warning("Activation requested on already active raildriver "+taskid);
			return;
		}
		taskid = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 10L, 20L);
		RailDriver.log.info("Activated "+direction.name()+ " raildriver "+taskid);
	}
	
	public void deactivate() {
		if (taskid == -1) {
			RailDriver.log.warning("Deactivation requested for already inactive raildriver!");
			return;
		}
		plugin.getServer().getScheduler().cancelTask(taskid);
		RailDriver.log.info("Deactivated raildriver "+taskid);		
	}
}

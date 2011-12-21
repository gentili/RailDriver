package ca.mcpnet.RailDriver;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.material.Lever;
import org.bukkit.material.RedstoneTorch;
import org.bukkit.util.Vector;

import ca.mcpnet.RailDriver.RailDriver.Facing;

public class RailDriverTask implements Runnable {

	private RailDriver plugin;
	private int x,y,z;
	private BlockFace direction;
	private boolean shutdown;
	private World world;
	private int taskid;
	int iteration;
	
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
		iteration = 0;
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

	void setDrillBit(boolean on) {
		Block block = getRelativeBlock(2,1,2);
		Lever leverblock = new Lever(block.getType(),block.getData());
		leverblock.setPowered(on);
		block.setData(leverblock.getData());
	}
	public void run() {
		// RailDriver.log.info("Updating Raildriver "+taskid);
		iteration++;
		// Check that it's still a raildriver
		/*
		Block leverblock = world.getBlockAt(x, y, z);
		if (!plugin.isRailDriver(leverblock)) {
			RailDriver.log.info("Raildriver corrupted!");
			plugin.taskset.remove(taskid);
			deactivate();
			return;
		}
		*/
		if (iteration == 1) {
			setDrillBit(false);
		}
		if (iteration == 6) {
			// Remove materials in front of bit
			setDrillBit(true);
		}
		if (iteration == 8) {
			world.createExplosion(getRelativeBlock(6,1,1).getLocation(), 0);
		}
		if (iteration == 12) {
			setDrillBit(false);
		}
		if (iteration == 18) {
			setDrillBit(true);
		}
		if (iteration == 20) {
			world.createExplosion(getRelativeBlock(6,1,1).getLocation(), 0);
		}
		if (iteration == 24) {
			setDrillBit(false);
		}
		if (iteration == 30) {
			setDrillBit(true);
		}
		if (iteration == 32) {
			world.createExplosion(getRelativeBlock(6,1,1).getLocation(), 0);
		}
		if (iteration == 40) {
			// Do track laying noises
			iteration = 0;
		}
		
		// world.playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
		/*
		Block block = this.getRelativeBlock(1, 0, i);
		i = (i + 1) % 3;
		RailDriver.log.info(block.getType().name());
		*/
		// world.createExplosion(x, y, z, 0);
		// Light the fires
	}
	
	public boolean matchBlock(Block block) {
		if (block.getLocation().getBlockX() == x &&
				block.getLocation().getBlockY() == y &&
				block.getLocation().getBlockZ() == z) {
			return true;
		}
		return false;
	}
	public void setBlockTypeSaveData(Block block, Material type) {
		byte data = block.getData();
		block.setType(type);
		block.setData(data);		
	}
	public void activate() {
		if (taskid != -1) {
			RailDriver.log.warning("Activation requested on already active raildriver "+taskid);
			return;
		}
		taskid = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 10L, 2L);
		RailDriver.log.info("Activated "+direction.name()+ " raildriver "+taskid);
		// Light the fires
		RailDriver.log.info(getRelativeBlock(1,0,0).getType().name());

		setBlockTypeSaveData(getRelativeBlock(1,0,0), Material.BURNING_FURNACE);
		setBlockTypeSaveData(getRelativeBlock(1,2,0), Material.BURNING_FURNACE);
		
		// Furnace furnace = (Furnace) getRelativeBlock(1,0,0).getState();
		// furnace.setType(Material.BURNING_FURNACE);
		// furnace.update();
	}
	
	public void deactivate() {
		if (taskid == -1) {
			RailDriver.log.warning("Deactivation requested for already inactive raildriver!");
			return;
		}
		plugin.getServer().getScheduler().cancelTask(taskid);
		RailDriver.log.info("Deactivated raildriver "+taskid);

		setBlockTypeSaveData(getRelativeBlock(1,0,0), Material.FURNACE);
		setBlockTypeSaveData(getRelativeBlock(1,2,0), Material.FURNACE);
		// Furnace furnace = (Furnace) getRelativeBlock(1,0,0).getState();
		// furnace.setsetType(Material.FURNACE);
		// furnace.update();
	}
}

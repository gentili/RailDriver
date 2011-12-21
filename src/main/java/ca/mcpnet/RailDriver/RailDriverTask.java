package ca.mcpnet.RailDriver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
	int nextiteration;
	ArrayList<ItemStack> collecteditems;
	Iterator<ItemStack> itemitr;
	boolean whichdispenser;
	int smokedir;
	
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
		nextiteration = 1;
		collecteditems = new ArrayList<ItemStack>();
		whichdispenser = false;
		if (direction == BlockFace.EAST) {
			smokedir = 7;
		} else if (direction == BlockFace.WEST) {
			smokedir = 1;
		} else if (direction == BlockFace.SOUTH) {
			smokedir = 3;
		} else if (direction == BlockFace.NORTH) {
			smokedir = 5;
		} else {
			smokedir = 4;
		}

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
	void smokePuff() {
		world.playEffect(getRelativeBlock(1,0,1).getLocation(), Effect.SMOKE, smokedir);
		world.playEffect(getRelativeBlock(1,2,1).getLocation(), Effect.SMOKE, smokedir);
	}
	void setDrillSwitch(boolean on) {
		Block block = getRelativeBlock(2,1,2);
		Lever leverblock = new Lever(block.getType(),block.getData());
		leverblock.setPowered(on);
		block.setData(leverblock.getData());
	}
	void setMainSwitch(boolean on) {
		Block block = getRelativeBlock(0,1,1);
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
			setDrillSwitch(false);
		}
		if (iteration == 6) {
			// Remove materials in front of bit
			if (!excavate()) {
				RailDriver.log.info("Obstruction encountered!");
				deactivate();
				return;
			}
			setDrillSwitch(true);
			smokePuff();
		}
		if (iteration == 8) {
			ejectItems();
		}
		if (iteration == 12) {
			setDrillSwitch(false);
		}
		if (iteration == 18) {
			setDrillSwitch(true);
			smokePuff();
		}
		if (iteration == 20) {
			ejectItems();
		}
		if (iteration == 24) {
			setDrillSwitch(false);
		}
		if (iteration == 30) {
			setDrillSwitch(true);
			smokePuff();
		}
		if (iteration == 32) {
			ejectItems();
		}
		if (iteration == 36) {
			setDrillSwitch(false);
			iteration = 0;
		}
		if (iteration == 38) {
			// Do track laying stuff
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
	
	private void ejectItems() {
		if (!collecteditems.isEmpty()) {
			world.createExplosion(getRelativeBlock(6,1,1).getLocation(), 0);
			for (int i = 0;i < 3 && itemitr.hasNext(); i++) {
				ItemStack curstack = itemitr.next();
				itemitr.remove();
				Location loc;
				if (whichdispenser) {
					loc = getRelativeBlock(0,0,1).getLocation();
					whichdispenser = false;
				} else {
					loc = getRelativeBlock(0,2,1).getLocation();
					whichdispenser = true;
				}
				world.dropItem(loc, curstack);
				world.playEffect(loc, Effect.STEP_SOUND, curstack.getTypeId());
			}
		}
	}

	private boolean excavate() {
		for (int lx = 0; lx < 3; lx++) {
			for (int ly = 0; ly < 3; ly++) {
				Block block = getRelativeBlock((RailDriver.raildriverblocklist[lx][ly]).length - 1, lx, ly);
				if (block.isLiquid()) {					
					return false; 
				}
				if (!block.isEmpty()) {
					collecteditems.add(new ItemStack(block.getType(),1));
					block.setType(Material.AIR);
				}
			}
		}
		itemitr = collecteditems.iterator();
		return true;
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
		setMainSwitch(false);
		world.playEffect(new Location(world,x,y,z), Effect.EXTINGUISH,0);
		plugin.taskset.remove(this);
	}
}

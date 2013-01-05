package ca.mcpnet.RailDriver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.CoalType;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Torch;

import ca.mcpnet.RailDriver.RailDriver.Facing;

public class RailDriverTask implements Runnable {

	private RailDriver plugin;
	private int x,y,z;
	private BlockFace direction;
	private World world;
	private int taskid;
	private Player playerOwner = null;
	private int steps = 0;
	int iteration;
	boolean nexttorch;
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
		world = block.getWorld();
		taskid = -1;
		iteration = 0;
		nexttorch = false;
		collecteditems = new ArrayList<ItemStack>();
		whichdispenser = false;
		if (direction == BlockFace.NORTH) {
			smokedir = 7;
		} else if (direction == BlockFace.SOUTH) {
			smokedir = 1;
		} else if (direction == BlockFace.EAST) {
			smokedir = 3;
		} else if (direction == BlockFace.WEST) {
			smokedir = 5;
		} else {
			smokedir = 4;
		}

	}
	
	Block getRelativeBlock(int i, int j, int k) {
		if (direction == BlockFace.WEST) {
			return world.getBlockAt(x-i,y-1+k,z+1-j);
		} else if (direction == BlockFace.NORTH) {
			return world.getBlockAt(x-1+j,y-1+k,z-i);
		} else if (direction == BlockFace.SOUTH) {
			return world.getBlockAt(x+1-j,y-1+k,z+i);
		} else if (direction == BlockFace.EAST) {
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
		//localbroadcast("itterator:"+iteration);
		if (taskid == -1) {
			setMainSwitch(false);
			setDrillSwitch(false);
			world.playEffect(new Location(world,x,y,z), Effect.EXTINGUISH,0);
			smokePuff();
			plugin.taskset.remove(this);
		}

		iteration++;
		if (iteration == 1) {
			setDrillSwitch(false);
			if (plugin.getConfig().getBoolean("requires_fuel")) 
			{
				if (!burnCoal()) {//steps != 0 && 
					localbroadcast("Raildriver has insufficient fuel!");
					plugin.taskset.remove(taskid);
					deactivate();
				}
				
			}
		}
		if (iteration == 6) {
			Block leverblock = world.getBlockAt(x, y, z);
			if (!plugin.isRailDriver(leverblock)) {
				localbroadcast("Raildriver malfunction during drill phase!");
				plugin.taskset.remove(taskid);
				deactivate();
				return;
			}
			// Remove materials in front of bit
			if (!excavate()) {
				localbroadcast("Raildriver encountered obstruction!");
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
		}
		if (iteration == 40) {
			world.playEffect(getRelativeBlock(0,1,-1).getLocation(), Effect.STEP_SOUND, Material.LAVA.getId());
			// Do track laying stuff
		}
		if (iteration == 42) {
			world.playEffect(getRelativeBlock(0,0,-1).getLocation(), Effect.STEP_SOUND, Material.LAVA.getId());
			world.playEffect(getRelativeBlock(0,2,-1).getLocation(), Effect.STEP_SOUND, Material.LAVA.getId());
			// Do track laying stuff			
		}
		if (iteration == 44) {
			world.playEffect(getRelativeBlock(0,-1,1).getLocation(), Effect.STEP_SOUND, Material.LAVA.getId());
			world.playEffect(getRelativeBlock(0,3,1).getLocation(), Effect.STEP_SOUND, Material.LAVA.getId());
			// Do track laying stuff			
		}
		if (iteration == 46) {
			world.playEffect(getRelativeBlock(1,-1,2).getLocation(), Effect.STEP_SOUND, Material.LAVA.getId());
			world.playEffect(getRelativeBlock(1,3,2).getLocation(), Effect.STEP_SOUND, Material.LAVA.getId());
			// Do track laying stuff			
		}
		if (iteration == 48) {
			// Check that it's still a raildriver
			Block leverblock = world.getBlockAt(x, y, z);
			if (!plugin.isRailDriver(leverblock)) {
				localbroadcast("Raildriver malfunction during advance phase!");
				plugin.taskset.remove(taskid);
				deactivate();
				return;
			}
			if (!advance()) {
				
				deactivate();
				return;
			}
			// RailDriver.log.info("RailDriver "+taskid+" in chunk "+world.getChunkAt(getRelativeBlock(0,1,1)));
			world.createExplosion(getRelativeBlock(2,1,1).getLocation(), 0);
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
	
	private void localbroadcast(String msg) {
		Block block = getRelativeBlock(0,0,0);
		Location location = block.getLocation();
		List<Player> players = block.getWorld().getPlayers(); 
		Iterator<Player> pitr = players.iterator();
		while (pitr.hasNext()) {
			Player player = pitr.next();
			if (location.distanceSquared(player.getLocation()) < 36) {
				player.sendMessage(msg);
			}
		}
	}

	private ItemStack findCoal(Inventory inventory)
	{
		//plugin.logger.info("item:"+inventory);
		ItemStack[] items =  inventory.getContents();
		//plugin.logger.info("item length:"+items.length);
		for (int i = 0; i < items.length; i++) {
			
			ItemStack item = items[i]; 
			//plugin.logger.info("item:"+item);
			if (item == null) continue;
			if (item.getType() == Material.COAL)
			{
				//plugin.logger.info(item.getData().getData() +" == "+ CoalType.CHARCOAL.getData());
				if (item.getData().getData() == CoalType.CHARCOAL.getData())
				{
					return item;
				}
				//plugin.logger.info(item.getData().getData() +" == "+ CoalType.CHARCOAL.getData());
				if (item.getData().getData() == CoalType.COAL.getData()) 
				{
					return item;
				}
				return item;
			}
		}
		//plugin.logger.info("coal not fund ?");
		return null;
	}
	
	private void removeCoal(Inventory inventory)
	{
		ItemStack coal =  findCoal(inventory);
		int amount = coal.getAmount() -1;
		
		if (amount == 0)
			inventory.removeItem(coal);
		else 
			coal.setAmount(amount);
	}
	
	private boolean burnCoal() 
	{
		Block leftblock = getRelativeBlock(1,0,0);
		Furnace leftfurnace = (Furnace) leftblock.getState();
		Inventory leftinventory = leftfurnace.getInventory();
		Block rightblock = getRelativeBlock(1,2,0);
		Furnace rightfurnace = (Furnace) rightblock.getState();
		Inventory rightinventory = rightfurnace.getInventory();
		
		if (leftinventory.contains(Material.COAL) && (rightinventory.contains(Material.COAL)))
		{
			
			
			removeCoal(leftinventory);
			removeCoal(rightinventory);
	
			//ItemStack stack  = leftinventory.getItem(new ItemStack(Material.COAL.getId(),1,(short)0))
			//leftinventory.removeItem(,new ItemStack(Material.COAL,1));
			//rightinventory.removeItem(new ItemStack(Material.COAL.getId(),1,(short)0),new ItemStack(Material.COAL,1));
			leftfurnace.update();
			rightfurnace.update();
			return true;
		}
		
		return false;
	}

	private boolean advance() {
		// Check to make sure ground under is solid
		for (int lx = 0; lx < 3; lx++) {
			for (int lz = 0; lz < RailDriver.raildriverblocklist[lx][0].length; lz++) {
				Block block = getRelativeBlock(lz,lx,-1);
				if (!plugin.getConfig().getBoolean("ignore_broken_ground")) 
				{
					if (block.isEmpty() || block.isLiquid()) {
					
						localbroadcast("Raildriver encountered broken ground!");
						return false;
					}
				}else
				{
					//#debug debug
//@					plugin.logger.info("ignoring broken ground");
				}
				if (!canBreakBlock(block)) {
					localbroadcast("Raildriver encountered unbreakable ground!");
					return false;
				}
			}
		}
		// Check to make sure behind has no liquid
		for (int lx = 0; lx < 3; lx++) {
			Block block = getRelativeBlock(0,lx,0);
			if (block.isLiquid()) {
				localbroadcast("Raildriver encountered unstable environment!");
				return false;
			}			
		}
		// Check to make sure raildriver has enough materials
		int period = 8;
		int distance;
		if (direction == BlockFace.WEST ||
				direction == BlockFace.EAST) {
			distance = x;
		} else {
			distance = z;
		}
		if (plugin.getConfig().getBoolean("requires_fuel")) {
			Chest chest = (Chest) getRelativeBlock(1,1,2).getState();
			Inventory inventory = chest.getInventory();
			boolean torchcolumns = distance % period == 0;
			if (torchcolumns) {
				if (!inventory.contains(Material.POWERED_RAIL,1)) {
					// If we don't have powered rails, we try and convert other materials
					if (!inventory.contains(Material.GOLD_INGOT,4) ||
							!inventory.contains(Material.STICK,1) ||
							!inventory.contains(Material.REDSTONE,1)) {
						localbroadcast("Raildriver has insufficient building materials for power rails!");
						return false;
					}
					inventory.removeItem(
							new ItemStack(Material.GOLD_INGOT,4),
							new ItemStack(Material.STICK,1),
							new ItemStack(Material.REDSTONE,1));
					inventory.addItem(new ItemStack(Material.POWERED_RAIL,14));
				}
				if (!inventory.contains(Material.POWERED_RAIL, 1) ||
						!inventory.contains(Material.COBBLESTONE, 9) ||
						!inventory.contains(Material.STICK, 2) ||
						!inventory.contains(Material.REDSTONE, 1) ||
						findCoal(inventory)== null) {
					localbroadcast("Raildriver has insufficient building materials for power columns!");
					return false;				
				}
				inventory.removeItem(
						new ItemStack(Material.POWERED_RAIL,1),
						new ItemStack(Material.COBBLESTONE,9),
						new ItemStack(Material.STICK,2),
						new ItemStack(Material.REDSTONE,1)
						);
				
				removeCoal(inventory);
						
						//new ItemStack(Material.COAL,1));					
			} else {
				if (!inventory.contains(Material.RAILS, 1)) {
					// If we don't have rails, we try and convert other materials
					if (!inventory.contains(Material.IRON_INGOT,4) ||
							!inventory.contains(Material.STICK,1)) {
						localbroadcast("Raildriver has insufficient building materials for rails!");
						return false;
					}
					inventory.removeItem(
							new ItemStack(Material.IRON_INGOT,4),
							new ItemStack(Material.STICK,1));
					inventory.addItem(new ItemStack(Material.RAILS,14));
				}
				// Now try to lay the track
				if (!inventory.contains(Material.RAILS, 1) ||
						!inventory.contains(Material.COBBLESTONE, 3)) {
					localbroadcast("Raildriver has insufficient building materials for rails!");
					return false;
				}
				inventory.removeItem(
						new ItemStack(Material.RAILS,1),
						new ItemStack(Material.COBBLESTONE,3));					
			}
		}
		for (int lx = 0; lx < 3; lx++) {
			for (int ly = 0; ly < 3; ly++) {
				for (int lz = RailDriver.raildriverblocklist[lx][ly].length; lz > 0; lz--) {
					Block target = getRelativeBlock(lz,lx,ly);
					Block source = getRelativeBlock(lz-1,lx,ly);
					// RailDriver.log.info(source.getType().name());
					if (source.getType() == Material.CHEST) {
						// Get the old chest info
						Chest sourcechest = (Chest) source.getState();
						Inventory sourceinventory = sourcechest.getInventory();
						ItemStack[] sourceitems = sourceinventory.getContents();
						sourceinventory.clear();
						MaterialData sourcedata = sourcechest.getData();
						// Blow the old chest away
						source.setType(Material.AIR);
						
						target.setType(Material.CHEST);
						Chest targetchest = (Chest) target.getState();
						targetchest.setData(sourcedata);
						Inventory targetinventory = targetchest.getInventory();
						targetinventory.setContents(sourceitems);
						targetchest.update();
					} else if (source.getType() == Material.BURNING_FURNACE) {
						Furnace sourcefurnace = (Furnace) source.getState();
						Inventory sourceinventory = sourcefurnace.getInventory();
						ItemStack[] sourceitems = sourceinventory.getContents();
						sourceinventory.clear();
						MaterialData sourcedata = sourcefurnace.getData();
						// Blow the old chest away
						source.setType(Material.AIR);
						
						target.setType(Material.BURNING_FURNACE);
						Furnace targetfurnace = (Furnace) target.getState();
						
						targetfurnace.setData(sourcedata);
						Inventory targetinventory = targetfurnace.getInventory();
						targetinventory.setContents(sourceitems);
						targetfurnace.update();
					} else if (source.getType() == Material.DISPENSER || 
							source.getType() == Material.FURNACE ||
							source.getType() == Material.LEVER) {
						byte sourcedata = source.getData();
						Material sourcematerial = source.getType();
						source.setType(Material.AIR);
						target.setType(sourcematerial);
						target.setData(sourcedata);
					} else if (source.getType() == Material.TORCH ||
							source.getType() == Material.REDSTONE_TORCH_ON ||
							source.getType() == Material.REDSTONE_TORCH_OFF) {
						// Prevent free torch dropping side effect
						target.setType(Material.AIR);						
					} else {
						target.setType(source.getType());
						target.setData(source.getData());
					}
				}
			}
		}
		// Set the floor made of stone
		
		for (int lx = 0; lx < 3; lx++)
		{
			Block floor = getRelativeBlock(1,lx,-1);
			if (!floor.isEmpty()) {
				collectBlock(floor.getType());
				floor.setTypeId(98);
			}
		}
		
		for (int lx = 0; lx < 3; lx++) {
			for (int ly = 0; ly < 3; ly++) {
				if (!(lx == 1 && ly == 1)) {
					getRelativeBlock(1,lx,ly).setType(Material.AIR);
				}
			}
		}
		if (distance % period == 0) {
			for (int ly = 0; ly < 3; ly++) {
				getRelativeBlock(1,-1,ly-1).setTypeId(98);
				getRelativeBlock(1,3,ly-1).setTypeId(98);
			}
			
			Block left;
			if (nexttorch) {
				left = getRelativeBlock(1,0,0);
				left.setType(Material.REDSTONE_TORCH_ON);
			} else {
				left = getRelativeBlock(1,0,1);
				left.setType(Material.TORCH);				
			}
			Torch lefttorch = new Torch(left.getType(),left.getData());
			lefttorch.setFacingDirection(Facing.RIGHT.translate(direction));
			left.setData(lefttorch.getData());

			getRelativeBlock(1,1,0).setType(Material.POWERED_RAIL);

			Block right;
			if (nexttorch) {
				right = getRelativeBlock(1,2,1);
				right.setType(Material.TORCH);				
			} else {
				right = getRelativeBlock(1,2,0);
				right.setType(Material.REDSTONE_TORCH_ON);
			}
			Torch righttorch = new Torch(right.getType(),right.getData());
			righttorch.setFacingDirection(Facing.LEFT.translate(direction));
			right.setData(righttorch.getData());
			
			nexttorch = !nexttorch;
		} else { // Regular rail
			getRelativeBlock(1,1,0).setType(Material.RAILS);
		}
		// plugin.getServer().getScheduler().cancelTask(taskid);
		// plugin.taskset.remove(this);
		Location newloc = getRelativeBlock(1,1,1).getLocation();
		x = newloc.getBlockX();
		y = newloc.getBlockY();
		z = newloc.getBlockZ();
		return true;
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

	// Check if we're allowed to break a particular block
	// This can be extended to include additional cases (exclusion lists etc..)
	// We probably should introduce any other checks as well (including isLiquid) into here
	private boolean canBreakBlock(Block blockToBreak) {
		if (!isWaterCooled() && !plugin.getConfig().getBoolean("break_obsidian"))
		{
			if (blockToBreak.getType() == Material.OBSIDIAN)
				return false;
		}
		else
		{
			//plugin.logger.info("will break lava/obsidian");
		}
		
		if (blockToBreak.getType() == Material.BEDROCK)
			return false;
		// Do Specific WorldGuard test
	
		// Use fancy event catching trick 
		BlockBreakEvent canBreak = new BlockBreakEvent(blockToBreak, playerOwner);
		plugin.getServer().getPluginManager().callEvent(canBreak);
		return !canBreak.isCancelled();
	}
	
	private boolean isWaterCooled()
	{
		if (!plugin.getConfig().getBoolean("water_cooling"))return false;
		Chest chest = (Chest) getRelativeBlock(1,1,2).getState();
		if(!chest.getInventory().contains(Material.WATER_BUCKET)) return false;
		return true;
		
	}
	
	private boolean useGlass()
	{
		if (!plugin.getConfig().getBoolean("use_glass"))return false;
		Chest chest = (Chest) getRelativeBlock(1,1,2).getState();
		if(!chest.getInventory().contains(Material.LAVA_BUCKET)) return false;
		return true;
		
	}
	
	private boolean excavate() {
		try
		{
		// Check all the blocks we're about to excavate
		for (int lx = 0; lx < 3; lx++) {
			for (int ly = 0; ly < 3; ly++) {
				Block block = getRelativeBlock((RailDriver.raildriverblocklist[lx][ly]).length, lx, ly);
				//#debug debug
//@				plugin.logger.info("["+lx+"]" + "["+ly+"]" + block.getType());
				if (isWaterCooled())
				{
					
					Location loc = block.getLocation();
					
					if (block.getType() == Material.STATIONARY_LAVA || block.getType() == Material.LAVA)
					{			
						//#debug debug
//@						plugin.logger.info("converting lava to obsidian");
						block.setType(Material.OBSIDIAN);
					}
					/*
					if (block.getType() == Material.LAVA)
					{
						plugin.logger.info("converting lava to coblestone");
						block.setType(Material.COBBLESTONE);
					}*/
					
					Chest chest = (Chest) getRelativeBlock(1,1,2).getState();
					Inventory inventory = chest.getInventory();
					
					int radius = 1;
					boolean sandUsed = false;
					for (int x = -radius; x <= radius; x++)
					{
						for (int y = -radius; y <= radius + (radius<=1 ? 1 : 0); y++)
						{
							for (int z = -radius; z <= radius; z++)
							{
								Block relativeBlock = world.getBlockAt(loc.getBlockX() + x , loc.getBlockY() + y ,loc.getBlockZ() + z);
								
								//plugin.logger.info("relativeBlock: ["+(loc.getBlockX() + x)+"]" + "["+(loc.getBlockY() + y)+"]" +"["+(loc.getBlockZ() + z)+"]" + block.getType());
								
								if (relativeBlock.getType() == Material.STATIONARY_LAVA || relativeBlock.getType() == Material.LAVA)
								{
									if (useGlass())
									{
										if( inventory.contains(Material.SAND) || inventory.contains(Material.GLASS))
										{
											//#debug debug
//@											plugin.logger.info("converting lava to glass");
											relativeBlock.setType(Material.GLASS);
	
											 if (!sandUsed) 
											 {
												ItemStack[] items =  inventory.getContents();
		
												for (int i = 0; i < items.length; i++)
												{
													
													ItemStack item = items[i]; 
		
													if (item == null) continue;
		
													if (item.getType() == Material.GLASS ||  item.getType() == Material.SAND)
													{
														int amount = item.getAmount() -1;
														
														if (amount == 0)
															inventory.removeItem(item);
														else 
															item.setAmount(amount);
														break;
													}
													// only use 1 sand per move
													sandUsed = true;
												}	
											}
										}
										else
										{
											localbroadcast("Out of glass / sand, add more or remove lava bukket");
											return false;
										}
									}
									else
									{

										
										//#debug debug
//@										plugin.logger.info("converting lava to obsidian");
										relativeBlock.setType(Material.OBSIDIAN);
									}
								}
								
								
							}
						}
					}
					

				}
				
				
				if (block.isLiquid()) 
				{
					
					return false; 
				}
				if(!canBreakBlock(block))
				{
					return false;
				}
				
			}
		}
		
		
		// OK, now excavate them
		for (int lx = 0; lx < 3; lx++) {
			for (int ly = 0; ly < 3; ly++) {
				Block block = getRelativeBlock((RailDriver.raildriverblocklist[lx][ly]).length, lx, ly);
				if (!block.isEmpty()) {
					collectBlock(block);
					//collecteditems.addAll(block.getType());
					block.setType(Material.AIR);
				}
			}
		}
		itemitr = collecteditems.iterator();
		//
		}catch(Exception ex)
		{
			//#debug warn
			plugin.logger.warning("exception:"+ex.getMessage());
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	private void collectBlock(Material mat)
	{
		if (mat == Material.GLASS) return;
		if (mat == Material.RAILS) return;
		if (mat == Material.POWERED_RAIL) return;
		
		collecteditems.add(new ItemStack(mat,1));
		
		if (!plugin.getConfig().getBoolean("break_blocks"))
		{
			collecteditems.add(new ItemStack(mat,1));
		}else
		{
			
		}
	}
	
	private void collectBlock(Block block)
	{
		
		
		if (block.getType() == Material.GLASS) return;
		if (block.getType() == Material.RAILS) return;
		
		
		
		if (!plugin.getConfig().getBoolean("break_blocks"))
		{
			collecteditems.add(new ItemStack(block.getType(),1));
		}else
		{
			
			collecteditems.addAll(block.getDrops());
			block.breakNaturally();
		}
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
	public void setFurnaceBurning(Block block, boolean on) {
		if (block.getType() != Material.BURNING_FURNACE &&
				block.getType()  != Material.FURNACE) {
			return;
		}
		Furnace furnace = (Furnace) block.getState();
		Inventory inventory = furnace.getInventory();
		ItemStack[] contents = inventory.getContents();
		inventory.clear();
		MaterialData data = furnace.getData();

		if (on) {
			block.setType(Material.BURNING_FURNACE);
		} else {
			block.setType(Material.FURNACE);
		}
		furnace = (Furnace) block.getState();
		furnace.setData(data);
		inventory = furnace.getInventory();
		inventory.setContents(contents);
		furnace.update();

	}
	public void activate(Player actor) {
		if (taskid != -1) {
			//#debug info
			RailDriver.log("Activation requested on already active raildriver "+taskid);
			
			return;
		}
		
		// Keep a reference to the actor for some context checks during operation
		playerOwner = actor;
		
		taskid = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 10L, 2L);
		//#debug info
		RailDriver.log("Player " + playerOwner.getName() + " activated "+direction.name()+ "BOUND raildriver " + taskid);
		// Light the fires
		setFurnaceBurning(getRelativeBlock(1,0,0),true);
		setFurnaceBurning(getRelativeBlock(1,2,0),true);
	}
	
	public void deactivate() {
		if (taskid == -1) {
			//#debug info
			RailDriver.log("Deactivation requested for already inactive raildriver!");
			return;
		}
		playerOwner = null;
		plugin.getServer().getScheduler().cancelTask(taskid);
		//#debug info
		RailDriver.log("Deactivated raildriver "+taskid);
		// Shut off furnaces
		setFurnaceBurning(getRelativeBlock(1,0,0),false);
		setFurnaceBurning(getRelativeBlock(1,2,0),false);
		// Shutdown hiss
		world.playEffect(new Location(world,x,y,z), Effect.EXTINGUISH,0);
		taskid = -1;
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, 10L);
	}
	
	public Player getOwner() {
		return playerOwner;
	}
}

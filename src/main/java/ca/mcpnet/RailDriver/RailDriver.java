package ca.mcpnet.RailDriver;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Diode;
import org.bukkit.material.Dispenser;
import org.bukkit.material.Furnace;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class RailDriver extends JavaPlugin {

	static Logger log = Logger.getLogger("Minecraft");
	PluginManager pm;
	private final RailDriverPlayerListener playerListener = new RailDriverPlayerListener(
			this);
	private final RailDriverBlockListener blockListener = new RailDriverBlockListener(
			this);

	private final ItemStack[] devkit = {
			new ItemStack(Material.DIAMOND_PICKAXE, 1),
			new ItemStack(Material.DIAMOND_SWORD, 1),
			new ItemStack(Material.DIAMOND_SPADE, 1),
			new ItemStack(Material.DIAMOND_AXE, 1),
			new ItemStack(Material.DIAMOND_HOE, 1),
			new ItemStack(Material.TORCH,64)};

	private final ItemStack[] devarmor = {
			new ItemStack(Material.DIAMOND_BOOTS, 1),
			new ItemStack(Material.DIAMOND_LEGGINGS, 1),
			new ItemStack(Material.DIAMOND_CHESTPLATE, 1),
			new ItemStack(Material.DIAMOND_HELMET, 1),
	};

	private final Material[] diodes = {
			Material.DIODE,
			Material.DIODE_BLOCK_OFF,
			Material.DIODE_BLOCK_ON
	};
	static protected BlockTemplate[][][] raildriverblocklist;
	
	static public enum Facing {
		FORWARD,
		LEFT,
		BACKWARD,
		RIGHT,
		UP,
		DOWN,
		DONTCARE;
		
		static private final BlockFace horizBlockFaceArray[] = {
			BlockFace.NORTH,
			BlockFace.WEST,
			BlockFace.SOUTH,
			BlockFace.EAST
		};
		
		public boolean checkFace(BlockFace direction, BlockFace blockdirection) {
			if (this == DONTCARE) {
				return true;
			}
			if (this == UP) {
				return blockdirection == BlockFace.UP;
			}
			if (this == DOWN) {
				return blockdirection == BlockFace.DOWN;
			}
			BlockFace expectedDirection;
			if (direction == BlockFace.NORTH) {
				expectedDirection = horizBlockFaceArray[this.ordinal()];
			} else if (direction == BlockFace.WEST) {
				expectedDirection = horizBlockFaceArray[(this.ordinal() + 1) % 4];
			} else if (direction == BlockFace.SOUTH) {
				expectedDirection = horizBlockFaceArray[(this.ordinal() + 2) % 4];
			} else if (direction == BlockFace.EAST) {
				expectedDirection = horizBlockFaceArray[(this.ordinal() + 3) % 4];
			} else {
				// Should never get here, but if we do then this block is definitely not 
				// facing a legal direction
				return false;
			}
			
			return expectedDirection == blockdirection;
		}
	}
	
	static public class BlockTemplate {
		Material[] materials;
		Facing facing;
		boolean optional;
		
		BlockTemplate(Material m, Facing f, boolean o) {
			materials = new Material[1];
			materials[0] = m;
			facing = f;
			optional = o;
		}
		
		BlockTemplate(Material[] ma, Facing f, boolean o) {
			materials = ma;
			facing = f;
			optional = o;
		}
		
		public boolean checkBlock(Block block, BlockFace direction) {
			// Does the block type match what's specified in the template?
			boolean typematch = false;
			for (int i = 0; i < materials.length; i++) {
				log.info("  Match Type "+materials[i].name());
				if (block.getType() == materials[i]) {
					typematch = true;
				}
			}
			if (!typematch) {
				return false;
			}
			// Does the block's orientation match what's in the template?
			BlockFace f;
			if (block.getType() == Material.DIODE || 
					block.getType() == Material.DIODE_BLOCK_OFF ||
					block.getType() == Material.DIODE_BLOCK_ON) {
				Diode diode = new Diode(block.getType(),block.getData());
				f = diode.getFacing();
			} else if (block.getType() == Material.PISTON_STICKY_BASE) {
				PistonBaseMaterial piston = new PistonBaseMaterial(block.getType(),block.getData());
				f = piston.getFacing();
			} else if (block.getType() == Material.FURNACE) {
				Furnace furnace = new Furnace(block.getType(),block.getData());
				f = furnace.getFacing();
			} else if (block.getType() == Material.DISPENSER) {
				Dispenser dispenser = new Dispenser(block.getType(),block.getData());
				f = dispenser.getFacing();
			} else {
				f = direction;
			}
			log.info("  Match Direction "+f.name());
			return facing.checkFace(direction, f);
		}

		public boolean isOptional() {
			return optional;
		}
	}

	public RailDriver() {
		super();
		if (raildriverblocklist == null) {
			raildriverblocklist = new BlockTemplate[3][3][];
		}
		
		raildriverblocklist[0][0] = new BlockTemplate[7];
		raildriverblocklist[0][0][0] = new BlockTemplate(Material.AIR, Facing.DONTCARE, false);
		raildriverblocklist[0][0][1] = new BlockTemplate(Material.FURNACE, Facing.BACKWARD, false);
		raildriverblocklist[0][0][2] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[0][0][3] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[0][0][4] = new BlockTemplate(Material.PISTON_STICKY_BASE, Facing.FORWARD, false);
		raildriverblocklist[0][0][5] = new BlockTemplate(Material.PISTON_EXTENSION, Facing.FORWARD, true);
		raildriverblocklist[0][0][6] = new BlockTemplate(Material.DIAMOND_BLOCK, Facing.DONTCARE, false);

		raildriverblocklist[0][1] = new BlockTemplate[8];
		raildriverblocklist[0][1][0] = new BlockTemplate(Material.AIR, Facing.DONTCARE, false);
		raildriverblocklist[0][1][1] = new BlockTemplate(Material.DISPENSER, Facing.BACKWARD, false);
		raildriverblocklist[0][1][2] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[0][1][3] = new BlockTemplate(diodes, Facing.FORWARD, false);
		raildriverblocklist[0][1][4] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[0][1][5] = new BlockTemplate(Material.PISTON_STICKY_BASE, Facing.FORWARD, false);
		raildriverblocklist[0][1][6] = new BlockTemplate(Material.PISTON_EXTENSION, Facing.FORWARD, true);
		raildriverblocklist[0][1][7] = new BlockTemplate(Material.DIAMOND_BLOCK, Facing.DONTCARE, false);

		raildriverblocklist[0][2] = new BlockTemplate[7];
		raildriverblocklist[0][2][0] = new BlockTemplate(Material.AIR, Facing.DONTCARE, false);
		raildriverblocklist[0][2][1] = new BlockTemplate(Material.AIR, Facing.DONTCARE, false);
		raildriverblocklist[0][2][2] = new BlockTemplate(Material.REDSTONE_WIRE, Facing.FORWARD, false);
		raildriverblocklist[0][2][3] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[0][2][4] = new BlockTemplate(Material.PISTON_STICKY_BASE, Facing.FORWARD, false);
		raildriverblocklist[0][2][5] = new BlockTemplate(Material.PISTON_EXTENSION, Facing.FORWARD, true);
		raildriverblocklist[0][2][6] = new BlockTemplate(Material.DIAMOND_BLOCK, Facing.DONTCARE, false);

		raildriverblocklist[1][0] = new BlockTemplate[8];
		raildriverblocklist[1][0][0] = new BlockTemplate(Material.AIR, Facing.DONTCARE, false);
		raildriverblocklist[1][0][1] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[1][0][2] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[1][0][3] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[1][0][4] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[1][0][5] = new BlockTemplate(Material.PISTON_STICKY_BASE, Facing.FORWARD, false);
		raildriverblocklist[1][0][6] = new BlockTemplate(Material.PISTON_EXTENSION, Facing.FORWARD, true);
		raildriverblocklist[1][0][7] = new BlockTemplate(Material.DIAMOND_BLOCK, Facing.DONTCARE, false);

		raildriverblocklist[1][1] = new BlockTemplate[9];
		raildriverblocklist[1][1][0] = new BlockTemplate(Material.LEVER, Facing.FORWARD, false);
		raildriverblocklist[1][1][1] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[1][1][2] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[1][1][3] = new BlockTemplate(diodes, Facing.FORWARD, false);
		raildriverblocklist[1][1][4] = new BlockTemplate(diodes, Facing.FORWARD, false);
		raildriverblocklist[1][1][5] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[1][1][6] = new BlockTemplate(Material.PISTON_STICKY_BASE, Facing.FORWARD, false);
		raildriverblocklist[1][1][7] = new BlockTemplate(Material.PISTON_EXTENSION, Facing.FORWARD, true);
		raildriverblocklist[1][1][8] = new BlockTemplate(Material.DIAMOND_BLOCK, Facing.DONTCARE, false);

		raildriverblocklist[1][2] = new BlockTemplate[8];
		raildriverblocklist[1][2][0] = new BlockTemplate(Material.AIR, Facing.DONTCARE, false);
		raildriverblocklist[1][2][1] = new BlockTemplate(Material.REDSTONE_WIRE, Facing.FORWARD, false);
		raildriverblocklist[1][2][2] = new BlockTemplate(Material.REDSTONE_WIRE, Facing.FORWARD, false);
		raildriverblocklist[1][2][3] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[1][2][4] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[1][2][5] = new BlockTemplate(Material.PISTON_STICKY_BASE, Facing.FORWARD, false);
		raildriverblocklist[1][2][6] = new BlockTemplate(Material.PISTON_EXTENSION, Facing.FORWARD, true);
		raildriverblocklist[1][2][7] = new BlockTemplate(Material.DIAMOND_BLOCK, Facing.DONTCARE, false);

		raildriverblocklist[2][0] = new BlockTemplate[7];
		raildriverblocklist[2][0][0] = new BlockTemplate(Material.AIR, Facing.DONTCARE, false);
		raildriverblocklist[2][0][1] = new BlockTemplate(Material.FURNACE, Facing.BACKWARD, false);
		raildriverblocklist[2][0][2] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[2][0][3] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[2][0][4] = new BlockTemplate(Material.PISTON_STICKY_BASE, Facing.FORWARD, false);
		raildriverblocklist[2][0][5] = new BlockTemplate(Material.PISTON_EXTENSION, Facing.FORWARD, true);
		raildriverblocklist[2][0][6] = new BlockTemplate(Material.DIAMOND_BLOCK, Facing.DONTCARE, false);

		raildriverblocklist[2][1] = new BlockTemplate[8];
		raildriverblocklist[2][1][0] = new BlockTemplate(Material.AIR, Facing.DONTCARE, false);
		raildriverblocklist[2][1][1] = new BlockTemplate(Material.DISPENSER, Facing.BACKWARD, false);
		raildriverblocklist[2][1][2] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[2][1][3] = new BlockTemplate(diodes, Facing.FORWARD, false);
		raildriverblocklist[2][1][4] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[2][1][5] = new BlockTemplate(Material.PISTON_STICKY_BASE, Facing.FORWARD, false);
		raildriverblocklist[2][1][6] = new BlockTemplate(Material.PISTON_EXTENSION, Facing.DONTCARE, true);
		raildriverblocklist[2][1][7] = new BlockTemplate(Material.DIAMOND_BLOCK, Facing.DONTCARE, false);

		raildriverblocklist[2][2] = new BlockTemplate[7];
		raildriverblocklist[2][2][0] = new BlockTemplate(Material.AIR, Facing.DONTCARE, false);
		raildriverblocklist[2][2][1] = new BlockTemplate(Material.AIR, Facing.DONTCARE, false);
		raildriverblocklist[2][2][2] = new BlockTemplate(Material.REDSTONE_WIRE, Facing.FORWARD, false);
		raildriverblocklist[2][2][3] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE, false);
		raildriverblocklist[2][2][4] = new BlockTemplate(Material.PISTON_STICKY_BASE, Facing.FORWARD, false);
		raildriverblocklist[2][2][5] = new BlockTemplate(Material.PISTON_EXTENSION, Facing.FORWARD, true);
		raildriverblocklist[2][2][6] = new BlockTemplate(Material.DIAMOND_BLOCK, Facing.DONTCARE, false);
	}

	public void onEnable() {
		log.info("RailDriver Plugin Enabled!");
		pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.REDSTONE_CHANGE, blockListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
	}

	public void onDisable() {
		log.info("RailDriver Plugin Disabled!");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		// carve out a cavern from the players location outwards
		if (cmd.getName().equals("rd_cavern")) {
			if (args.length > 2) {
				sender.sendMessage("Too many arguments!");
				return false;
			}
			if (args.length == 2) {
				player = getServer().getPlayer(args[1]);
				if (player == null) {
					sender.sendMessage("Player not found!");
					return false;
				}
			}
			if (player == null) {
				sender.sendMessage("Must specify player when calling from server console!");
				return false;
			}
			int radius = 0;
			try {
				radius = Integer.decode(args[0]);
			} catch (NumberFormatException e) {
				sender.sendMessage("Must specify the size of the cavern");
				return false;
			}
			CavernCarverTask task = new CavernCarverTask(this, sender, player, player.getLocation(), radius);
			task.setTaskid(getServer().getScheduler().scheduleSyncRepeatingTask(this, task, 10L, 2L));
			sender.sendMessage("Scheduled cavern carver job "+task.getTaskid()+", radius "+ radius);
			return true;
		}
		
		// Stock the player with things useful for a developer of this plugin
		if (cmd.getName().equals("rd_devkit")) {
			if (args.length > 1) {
				sender.sendMessage("Too many arguments!");
				return false;
			}
			if (args.length == 1) {
				player = getServer().getPlayer(args[0]);
				if (player == null) {
					sender.sendMessage("Player not found!");
					return false;
				}
			}
			if (player == null) {
				sender.sendMessage("Must specify player when calling from server console!");
				return false;
			}
			sender.sendMessage("DevKit applied!");
			if (sender != player) {
				player.sendMessage("DevKit applied!");
			}
			PlayerInventory inventory = player.getInventory();
			for (int i = 0; i < devkit.length; i++) {
				if (!inventory.contains(devkit[i].getType())) {
					inventory.addItem(devkit[i]);
				}
			}
			inventory.setArmorContents(devarmor);
			return true;
		}
		
		// Stock the player with materials required to build a raildriver
		if (cmd.getName().equalsIgnoreCase("rd_stock")) {
			// FIXME Should be able to stock a specific player
			if (player == null) {
				sender.sendMessage("Not a server console command!");
				return false;
			} else {
				sender.sendMessage("Yeah! Stock that fucker up!");
				// 29 sticky piston
				// 42 iron block
				// 76 redstone torch
				// 331 redstone
				// 69 lever
				// 61 furnace
				// 23 dispencer
				// 57 block of diamond
				// 
			}
			return true;
		}
		return false;
	}
	

}

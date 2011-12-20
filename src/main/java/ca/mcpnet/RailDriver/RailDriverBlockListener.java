package ca.mcpnet.RailDriver;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.material.Diode;
import org.bukkit.material.Lever;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import ca.mcpnet.RailDriver.RailDriver.BlockTemplate;
import ca.mcpnet.RailDriver.RailDriver.Facing;

public class RailDriverBlockListener extends BlockListener {

	private RailDriver plugin;

	public RailDriverBlockListener(RailDriver instance) {
		plugin = instance;
	}

	@Override
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		if (event.getBlock().getType() == Material.LEVER) {
			plugin.log.info(event.getOldCurrent()+"->"+event.getNewCurrent());
			if (isRailDriver(event.getBlock())) {
				plugin.log.info("It's a raildriver!");
			} else {
				plugin.log.info("Not a raildriver!");
			}
		}
	}
	/**
	 * This function will determine if this block is the 
	 * lever block in a properly constructed RailDriver
	 * @return
	 */
	boolean isRailDriver(Block block) {
		// First is this block a lever block
		if (block.getType() != Material.LEVER)
			return false;
		// What block is the lever attached to?
		Lever lever = new Lever(block.getType(),block.getData());
		BlockFace direction = lever.getAttachedFace();
		Vector directionVector;
		RailDriver.log.info(direction.name());
		if (direction == BlockFace.NORTH) {
			directionVector = new Vector(-1,0,0);
		} else if (direction == BlockFace.SOUTH) {
			directionVector = new Vector(1,0,0);
		} else if (direction == BlockFace.EAST) {
			directionVector = new Vector(0,0,-1);
		} else if (direction == BlockFace.WEST) {
			directionVector = new Vector(0,0,1);
		} else {
			return false;
		}

		for (int j = 0; j < 3; j++) {
			for (int k = 0; k < 3; k++) {
				Vector startBlock = null;
				if (direction == BlockFace.NORTH) {
					startBlock = new Vector(block.getX(),block.getY()-1+k,block.getZ()-1+j);
				}
				/*
				 * Some Handy code to generate code describing the machine
				 * 
				int i = 0;
				BlockIterator bitr = new BlockIterator(block.getWorld(),startBlock,directionVector,0,10);
				Block b;
				while (bitr.hasNext()) {
					b = bitr.next();
					RailDriver.log.info("raildriverblocklist["+j+"]["+k+"]["+i+"] = new BlockTemplate(Material."+b.getType().name()+
							", Facing.FIXME, false);");
					i++;
					if (b.getType() == Material.DIAMOND_BLOCK) {
						RailDriver.log.info("// raildriverblocklist["+j+"]["+k+"] = new BlockTemplate["+(i)+"];");
						break;
					}
				}
				*/
				RailDriver.log.info("Checking "+j+","+k);
				BlockIterator bitr = new BlockIterator(block.getWorld(),startBlock,directionVector,0,10);// RailDriver.raildriverblocklist[1][1].length);
				Block b = bitr.next();
				boolean lastmatch = false;
				for (int i = 0; i < RailDriver.raildriverblocklist[j][k].length; i++) {
					if (lastmatch)
						b = bitr.next();
					RailDriver.log.info("Checking Block "+b.getType().name()+ " against template "+i);
					BlockTemplate bt = RailDriver.raildriverblocklist[j][k][i];
					if (!bt.checkBlock(b, direction)) {
						if (!bt.isOptional()) {
							return false;
						}
						lastmatch = false;
					} else {
						lastmatch = true;
					}
				}
			}
		}
		return true;
	}
}

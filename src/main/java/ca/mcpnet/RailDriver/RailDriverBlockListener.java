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
		BlockIterator bitr = new BlockIterator(block.getWorld(),block.getLocation().toVector(),directionVector,0,RailDriver.raildriverblocklist[1][1].length);
		boolean lastmatch = false;
		Block b = bitr.next();
		for (int i = 0; i < RailDriver.raildriverblocklist[1][1].length; i++) {
			if (lastmatch)
				b = bitr.next();
			RailDriver.log.info("Checking Block "+b.getType().name()+ " against template "+i);
			BlockTemplate bt = RailDriver.raildriverblocklist[1][1][i];
			if (!bt.checkBlock(b, direction)) {
				if (!bt.isOptional()) {
					return false;
				}
				lastmatch = false;
			} else {
				lastmatch = true;
			}
		}
		return true;
	}
}

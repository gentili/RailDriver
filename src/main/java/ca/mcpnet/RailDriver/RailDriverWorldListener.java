package ca.mcpnet.RailDriver;

import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldListener;

public class RailDriverWorldListener extends WorldListener {

	private RailDriver plugin;
	public RailDriverWorldListener(RailDriver instance) {
		plugin = instance;
	}
	@Override
	public void onChunkLoad(ChunkLoadEvent event) {
		RailDriver.log(event.getEventName()+":"+event.getChunk());
	}	
}

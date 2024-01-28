package com.snuri.customshophud;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

public class EventHandler {
	private Cs cs;
	
	public EventHandler(Cs cs) {
		this.cs = cs;
	}
	
	@SubscribeEvent
	public void renderGameOverlay(RenderGameOverlayEvent.Post event) {
		if(event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
			cs.onRender(event.getResolution(), event.getPartialTicks());
		}
	}
	
	@SubscribeEvent
	public void clientTick(TickEvent.ClientTickEvent event) {
		if(event.phase == TickEvent.Phase.END) {
			cs.onTick();
		}
	}
	
	@SubscribeEvent
	public void clientDisconnectionFromServer(ClientDisconnectionFromServerEvent event) {
		cs.onDisconnection();
	}
}

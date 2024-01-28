package com.snuri.customshophud;

import com.snuri.customshophud.network.ServerPacketUpdateHUD;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;

public class Cs {
	
	private static Cs instance;
	
	public Minecraft mc = null;
	
	private HUD hud;
	
	public static Cs getInstance() {
		if(instance == null) {
			instance = new Cs();
		}
		
		return instance;
	}
	
	private Cs() {
		mc = Minecraft.getInstance();
	}
	
	public void onTick() {
		if(hud != null) {
			hud.update();
		}
	}
	
	public void onRender(float partialTicks) {
		if(mc.player == null)
			return;
			
		if(hud != null) {
			hud.render(partialTicks);
		}
	}
	
	public void onLoggedOut() {
		hud = null;
	}
	
	/* PacketHandle */
	public void processPacket(ClientWorld clientWorld, ServerPacketUpdateHUD packet) {
		if(hud == null) {
			hud = new HUD(packet.getMoney());
		} else {
			hud.updateMoney(packet.getMoney());
		}
	}
}

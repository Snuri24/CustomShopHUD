package com.snuri.customshophud;

import com.snuri.customshophud.network.ServerPacketUpdateHUD;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

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
		mc = Minecraft.getMinecraft();
	}
	
	public void onTick() {
		if(hud != null) {
			hud.update();
		}
	}
	
	public void onRender(ScaledResolution resolution, float partialTicks) {
		if(mc.player == null)
			return;
			
		if(hud != null) {
			hud.render(resolution, partialTicks);
		}
	}
	
	public void onDisconnection() {
		hud = null;
	}
	
	/* PacketHandle */
	public void handleMessage(ServerPacketUpdateHUD message) {
		if(hud == null) {
			hud = new HUD(message.getMoney());
		} else {
			hud.updateMoney(message.getMoney());
		}
	}
}

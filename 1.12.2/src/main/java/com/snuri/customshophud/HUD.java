package com.snuri.customshophud;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class HUD {
	
	public static double referenceWidth = 1920.0D;
	public static double referenceHeight = 1080.0D;
	
	public static double[] backgroundPos = { 0.0083D, 0.0148D };
	public static double[] backgroundSize = { 0.2229D, 0.1343D };
	public static double[] playerSkinPos = { 0.0198D, 0.0352D };
	public static double[] playerSkinSize = { 0.0521D, 0.0926D };
	public static float[] textPos = { 0.1896F, 0.0324F };
	public static float textFontSize = 0.0481F;
	public static int textAlignment = 2;
	
	private ResourceLocation texture = new ResourceLocation("customshophudmod", "background.png");
	private ResourceLocation skin;
	
	private String text;
	
	private boolean skinReloaded = false;
	private int t = 60;
	
	public HUD(int money) {
		Minecraft mc = Minecraft.getMinecraft();
		
		File modFolder = new File(Minecraft.getMinecraft().mcDataDir, "mods/CustomShopHUD");
		try {
			Image image = ImageIO.read(new File(modFolder, "background.png"));
			backgroundSize[0] = image.getWidth(null) / referenceWidth;
			backgroundSize[1] = image.getHeight(null) / referenceHeight;
		} catch (IOException e) { }
		
		NetworkPlayerInfo playerInfo = mc.getConnection().getPlayerInfo(mc.player.getUniqueID());
		if(playerInfo == null) {
			skin = DefaultPlayerSkin.getDefaultSkin(mc.player.getUniqueID());
		} else {
			skin = playerInfo.getLocationSkin();
		}
		text = String.format("%,d", money);
	}
	
	public void render(ScaledResolution resolution, float partialTicks) {
		int width = resolution.getScaledWidth();
		int height = resolution.getScaledHeight();
		
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		
		Render.bindTexture(texture);
 		Render.setColor(0xFFFFFFFF);
 		Render.drawTexturedRect(width * backgroundPos[0], height * backgroundPos[1], width * backgroundSize[0], height * backgroundSize[1]);
		
 		if(skin != null) {
 			Minecraft.getMinecraft().getTextureManager().bindTexture(skin);
 			Render.setColor(0xFFFFFFFF);
 			Render.drawTexturedRect(width * playerSkinPos[0], height * playerSkinPos[1], width * playerSkinSize[0], height * playerSkinSize[1], 0.125F, 0.125F, 0.25F, 0.25F);
 			Render.drawTexturedRect(width * playerSkinPos[0], height * playerSkinPos[1], width * playerSkinSize[0], height * playerSkinSize[1], 0.625F, 0.125F, 0.75F, 0.25F);
 		}
 		
 		Render.drawString(text, width * textPos[0], height * textPos[1], height * textFontSize, textAlignment);
 		
		GlStateManager.popMatrix();
	}
	
	public void update() {
		if(!skinReloaded) { // 플레이어 스킨 최초 로드 시 소요 시간 때문에 기본 스킨이 우선 적용될 때가 있어 다시 불러온다.
			t --;
			if(t == 0) {
				skinReloaded = true;
				
				Minecraft mc = Minecraft.getMinecraft();
				NetworkPlayerInfo playerInfo = mc.getConnection().getPlayerInfo(mc.player.getUniqueID());
				if(playerInfo != null) {
					skin = playerInfo.getLocationSkin();
				}
			}
		}
	}
	
	public void updateMoney(int money) {
		text = String.format("%,d", money);
	}
}


package com.snuri.customshophud;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class Render {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static double zDepth = 0.0D;
	
	public static void setColor(int color) {
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(((color >> 16) & 0xff) / 255.0f, ((color >> 8) & 0xff) / 255.0f, ((color) & 0xff) / 255.0f, ((color >> 24) & 0xff) / 255.0f);
		GlStateManager.disableBlend();
	}
	
	public static int getTextureWidth() {
		return GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
	}

	public static int getTextureHeight() {
		return GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
	}
	
	public static void drawTexturedRect(double x, double y, double w, double h) {
		drawTexturedRect(x, y, w, h, 0.0D, 0.0D, 1.0D, 1.0D);
	}
	
	public static void drawTexturedRect(double x, double y, double w, double h, double u1, double v1, double u2, double v2) {
		try {
			GlStateManager.enableTexture2D();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(x + w, y, zDepth).tex(u2, v1).endVertex();
			buffer.pos(x, y, zDepth).tex(u1, v1).endVertex();
			buffer.pos(x, y + h, zDepth).tex(u1, v2).endVertex();
			buffer.pos(x + w, y + h, zDepth).tex(u2, v2).endVertex();
			tessellator.draw();
			GlStateManager.disableBlend();
		} catch(NullPointerException e) {
			LOGGER.error("Render.drawTexturedRect : Null Pointer Exception");
		}
	}
	
	public static void drawString(String s, float x, float y) {
		drawString(s, x, y, 15.0F, 0, 0xFFFFFFFF);
	}
	
	public static void drawString(String s, float x, float y, float fontSize) {
		drawString(s, x, y, fontSize, 0, 0xFFFFFFFF);
	}
	
	public static void drawString(String s, float x, float y, float fontSize, int alignment) {
		drawString(s, x, y, fontSize, alignment, 0xFFFFFFFF);
	}
	
	public static void drawString(String s, float x, float y, float fontSize, int alignment, int color) {
		FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		float scale = fontSize / 15.0F;
		
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.scale(scale, scale, 1.0f);
		if(alignment == 0) { // LEFT
			fr.drawString(s, x / scale, y / scale, color, false);
		} else if(alignment == 1) { // CENTER
			fr.drawString(s, (x - (fr.getStringWidth(s) * scale) / 2) / scale, y / scale, color, false);
		} else { // RIGHT
			fr.drawString(s, (x - (fr.getStringWidth(s) * scale)) / scale, y / scale, color, false);
		}
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
	
	public static void drawStringWithShadow(String s, float x, float y, float fontSize, int alignment) {
		FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		float scale = fontSize / 15.0F;
		
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.scale(scale, scale, 1.0f);
		if(alignment == 0) { // LEFT
			fr.drawString(s, x / scale, y / scale, 0xFFFFFFFF, true);
		} else if(alignment == 1) { // CENTER
			fr.drawString(s, (x - (fr.getStringWidth(s) * scale) / 2) / scale, y / scale, 0xFFFFFFFF, true);
		} else { // RIGHT
			fr.drawString(s, (x - (fr.getStringWidth(s) * scale)) / scale, y / scale, 0xFFFFFFFF, true);
		}
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
	
	public static void bindTexture(ResourceLocation resource) {
    	ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(resource);
    	if(textureObj == null) {
    		textureObj = new BlurTexture(resource);
    		Minecraft.getMinecraft().getTextureManager().loadTexture(resource, textureObj);
    	}
    	GlStateManager.bindTexture(textureObj.getGlTextureId());
    }
	
	public static void deleteTexture(ResourceLocation resource) {
		Minecraft.getMinecraft().getTextureManager().deleteTexture(resource);
	}
}

package com.snuri.customshophud;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class Render {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static double zDepth = 0.0D;
	
	@SuppressWarnings("deprecation")
	public static void setColor(int color) {
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		RenderSystem.color4f(((color >> 16) & 0xff) / 255.0f, ((color >> 8) & 0xff) / 255.0f, ((color) & 0xff) / 255.0f, ((color >> 24) & 0xff) / 255.0f);
		RenderSystem.disableBlend();
	}
	
	public static int getTextureWidth() {
		return GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
	}

	public static int getTextureHeight() {
		return GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
	}
	
	public static void drawTexturedRect(double x, double y, double w, double h) {
		drawTexturedRect(x, y, w, h, 0.0F, 0.0F, 1.0F, 1.0F);
	}
	
	public static void drawTexturedRect(double x, double y, double w, double h, float u1, float v1, float u2, float v2) {
		try {
			RenderSystem.enableTexture();
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferBuilder = tessellator.getBuilder();
			bufferBuilder.begin(GL11.GL_QUADS , DefaultVertexFormats.POSITION_TEX);
			bufferBuilder.vertex(x + w, y, zDepth).uv(u2, v1).endVertex();
			bufferBuilder.vertex(x, y, zDepth).uv(u1, v1).endVertex();
			bufferBuilder.vertex(x, y + h, zDepth).uv(u1, v2).endVertex();
			bufferBuilder.vertex(x + w, y + h, zDepth).uv(u2, v2).endVertex();
			tessellator.end();
			RenderSystem.disableBlend();
		} catch(NullPointerException e) {
			LOGGER.error("Render.drawTexturedRect : Null Pointer Exception");
		}
	}
	
	public static void drawString(MatrixStack matrixStack, String s, float x, float y) {
		drawString(matrixStack, s, x, y, 15.0F, 0, 0xFFFFFFFF);
	}
	
	public static void drawString(MatrixStack matrixStack, String s, float x, float y, float fontSize) {
		drawString(matrixStack, s, x, y, fontSize, 0, 0xFFFFFFFF);
	}
	
	public static void drawString(MatrixStack matrixStack, String s, float x, float y, float fontSize, int alignment) {
		drawString(matrixStack, s, x, y, fontSize, alignment, 0xFFFFFFFF);
	}
	
	@SuppressWarnings({ "deprecation", "resource" })
	public static void drawString(MatrixStack matrixStack, String s, float x, float y, float fontSize, int alignment, int color) {
		FontRenderer font = Minecraft.getInstance().font;
		float scale = fontSize / 15.0F;
		
		RenderSystem.pushMatrix();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.scalef(scale, scale, 1.0f);
		if(alignment == 0) { // LEFT
			font.draw(matrixStack, s, x / scale, y / scale, color);
		} else if(alignment == 1) { // CENTER
			font.draw(matrixStack, s, (x - (font.width(s) * scale) / 2) / scale, y / scale, color);
		} else { // RIGHT
			font.draw(matrixStack, s, (x - (font.width(s) * scale)) / scale, y / scale, color);
		}
		RenderSystem.disableBlend();
		RenderSystem.popMatrix();
	}
	
	@SuppressWarnings({ "deprecation", "resource" })
	public static void drawStringWithShadow(MatrixStack matrixStack, String s, float x, float y, float fontSize, int alignment, int color) {
		FontRenderer font = Minecraft.getInstance().font;
		float scale = fontSize / 15.0F;
		
		RenderSystem.pushMatrix();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.scalef(scale, scale, 1.0f);
		if(alignment == 0) { // LEFT
			font.drawShadow(matrixStack, s, x / scale, y / scale, color);
		} else if(alignment == 1) { // CENTER
			font.drawShadow(matrixStack, s, (x - (font.width(s) * scale) / 2) / scale, y / scale, color);
		} else { // RIGHT
			font.drawShadow(matrixStack, s, (x - (font.width(s) * scale)) / scale, y / scale, color);
		}
		RenderSystem.disableBlend();
		RenderSystem.popMatrix();
	}
	
	public static void bindTexture(ResourceLocation resource) {
    	Texture texture = Minecraft.getInstance().getTextureManager().getTexture(resource);
    	if(texture == null) {
    		texture = new BlurTexture(resource);
    		Minecraft.getInstance().getTextureManager().register(resource, texture);
    	}
    	texture.bind();
    }
	
	public static void deleteTexture(ResourceLocation resource) {
		Minecraft.getInstance().getTextureManager().release(resource);
	}
}

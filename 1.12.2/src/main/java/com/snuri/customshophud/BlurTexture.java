package com.snuri.customshophud;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlurTexture extends AbstractTexture {
    protected final ResourceLocation textureLocation;

    public BlurTexture(ResourceLocation textureResourceLocation) {
        textureLocation = textureResourceLocation;
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException {
        this.deleteGlTexture();
        IResource iresource = null;

        try {
            iresource = resourceManager.getResource(textureLocation);
            BufferedImage bufferedimage = TextureUtil.readBufferedImage(iresource.getInputStream());

            TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), bufferedimage, true, true);
        } finally {
            IOUtils.closeQuietly((Closeable)iresource);
        }
    }
}

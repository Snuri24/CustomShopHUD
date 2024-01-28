package com.snuri.customshophud;

import java.io.Closeable;
import java.io.IOException;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlurTexture extends Texture {
    protected final ResourceLocation location;

    public BlurTexture(ResourceLocation textureResourceLocation) {
        location = textureResourceLocation;
    }

    public void load(IResourceManager resourceManager) throws IOException {
    	BlurTexture.TextureData texturedata = this.getTextureImage(resourceManager);
        texturedata.throwIfError();
        boolean blur = true;
        boolean clamp = true;
        
        NativeImage nativeImage = texturedata.getImage();
        if(!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> {
               this.doLoad(nativeImage, blur, clamp);
            });
        } else {
        	this.doLoad(nativeImage, blur, clamp);
        }
    }
    
    private void doLoad(NativeImage nativeImage, boolean blur, boolean clamp) {
        TextureUtil.prepareImage(this.getId(), 0, nativeImage.getWidth(), nativeImage.getHeight());
        nativeImage.upload(0, 0, 0, 0, 0, nativeImage.getWidth(), nativeImage.getHeight(), blur, clamp, false, true);
    }
    
    protected BlurTexture.TextureData getTextureImage(IResourceManager resourceManager) {
    	return BlurTexture.TextureData.load(resourceManager, this.location);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static class TextureData implements Closeable {
       @Nullable
       private final TextureMetadataSection metadata;
       @Nullable
       private final NativeImage image;
       @Nullable
       private final IOException exception;

       public TextureData(IOException exception) {
          this.exception = exception;
          this.metadata = null;
          this.image = null;
       }

       public TextureData(@Nullable TextureMetadataSection textureMetadataSection, NativeImage nativeImage) {
          this.exception = null;
          this.metadata = textureMetadataSection;
          this.image = nativeImage;
       }

       public static BlurTexture.TextureData load(IResourceManager resourceManager, ResourceLocation resourceLocation) {
          try(IResource iresource = resourceManager.getResource(resourceLocation)) {
             NativeImage nativeImage = NativeImage.read(iresource.getInputStream());
             TextureMetadataSection textureMetadataSection = null;

             return new BlurTexture.TextureData(textureMetadataSection, nativeImage);
          } catch(IOException ioexception) {
             return new BlurTexture.TextureData(ioexception);
          }
       }

       @Nullable
       public TextureMetadataSection getTextureMetadata() {
          return this.metadata;
       }

       public NativeImage getImage() throws IOException {
          if(this.exception != null) {
             throw this.exception;
          } else {
             return this.image;
          }
       }

       public void close() {
          if(this.image != null) {
             this.image.close();
          }
       }

       public void throwIfError() throws IOException {
          if(this.exception != null) {
             throw this.exception;
          }
       }
    }
}

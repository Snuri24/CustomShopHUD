package com.snuri.customshophud.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;

public class ModResourcePack implements IResourcePack {
	
	private File modFolder; 
	private static final Set<String> domains = ImmutableSet.of("customshophudmod");
	
	public ModResourcePack(File modFolder) {
		this.modFolder = modFolder;
	}
	
	@Override
	public InputStream getInputStream(ResourceLocation location) throws IOException {
		return new FileInputStream(new File(modFolder, location.getResourcePath()));
	}

	@Override
	public boolean resourceExists(ResourceLocation location) {
		return new File(modFolder, location.getResourcePath()).exists();
	}

	@Override
	public Set<String> getResourceDomains() {
		return domains;
	}

	@Override
	public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
		return null;
	}

	@Override
	public BufferedImage getPackImage() throws IOException {
		return null;
	}

	@Override
	public String getPackName() {
		return "CustomShopHUDResourcePack";
	}
	
}

package com.snuri.customshophud.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class ModResourcePack implements IResourcePack {
	
	private File modFolder;
	private static final Set<String> namespaces = ImmutableSet.of("customshophudmod");
	
	public ModResourcePack(File modFolder) {
		this.modFolder = modFolder;
	}
	
	@Override
	public InputStream getRootResource(String name) throws IOException {
		if (!name.contains("/") && !name.contains("\\")) {
			return getResource(name);
		} else {
			throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
		}
	}

	@Override
	public InputStream getResource(ResourcePackType type, ResourceLocation location) throws IOException {
		return new FileInputStream(new File(modFolder, location.getPath()));
	}

	@Override
	public Collection<ResourceLocation> getResources(ResourcePackType type, String resourceNamespace,
			String pathIn, int maxDepth, Predicate<String> filter) {
		try {
            Path root = new File(modFolder, type.getDirectory()).toPath();
            Path inputPath = root.getFileSystem().getPath(pathIn);

            return Files.walk(root).
                    map(path -> root.relativize(path.toAbsolutePath())).
                    filter(path -> path.getNameCount() <= maxDepth). // Make sure the depth is within bounds
                    filter(path -> !path.toString().endsWith(".mcmeta")). // Ignore .mcmeta files
                    filter(path -> path.startsWith(inputPath)). // Make sure the target path is inside this one
                    filter(path -> filter.test(path.getFileName().toString())). // Test the file name against the predicate
                    // Finally we need to form the RL, so use the first name as the domain, and the rest as the path
                    // It is VERY IMPORTANT that we do not rely on Path.toString as this is inconsistent between operating systems
                    // Join the path names ourselves to force forward slashes
                    map(path -> new ResourceLocation(resourceNamespace, Joiner.on('/').join(path))).
                    collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
	}
	
	@Override
	public boolean hasResource(ResourcePackType type, ResourceLocation location) {
		return new File(modFolder, location.getPath()).exists();
	}

	@Override
	public Set<String> getNamespaces(ResourcePackType type) {
		return namespaces;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getMetadataSection(IMetadataSectionSerializer<T> serializer) throws IOException {
		Object object;
		try(InputStream inputstream = getResource("pack.mcmeta")) {
			object = getMetadataFromStream(serializer, inputstream);
		}
		return (T)object;
	}
	
	@Override
	public String getName() {
		return "CustomShopHUDResourcePack";
	}

	@Override
	public void close() {
		
	}

	protected InputStream getResource(String name) throws IOException {
        return new FileInputStream(new File(modFolder, name));
	}

	protected boolean hasResource(String name) {
		return new File(modFolder, name).exists();
	}
	
	public static <T> T getMetadataFromStream(IMetadataSectionSerializer<T> serializer, InputStream inputstream) {
		JsonObject jsonobject;
		try(BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))) {
			jsonobject = JSONUtils.parse(bufferedreader);
		} catch (JsonParseException | IOException e) {
	         return (T)null;
	    }
		
		if (!jsonobject.has(serializer.getMetadataSectionName())) {
			return (T)null;
		} else {
			try {
				return serializer.fromJson(JSONUtils.getAsJsonObject(jsonobject, serializer.getMetadataSectionName()));
			} catch (JsonParseException jsonparseexception) {
	            return (T)null;
	        }
		}
	}
}

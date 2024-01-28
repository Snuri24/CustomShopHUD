package com.snuri.customshophud.proxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.snuri.customshophud.Cs;
import com.snuri.customshophud.EventHandler;
import com.snuri.customshophud.HUD;
import com.snuri.customshophud.network.PacketType;
import com.snuri.customshophud.network.ServerPacketUpdateHUD;
import com.snuri.customshophud.util.ModResourcePack;
import com.snuri.customshophud.util.Reference;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ClientProxy extends CommonProxy {
	
	private static final Logger LOGGER = LogManager.getLogger();
	public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.CHANNEL);
	
	@Override
	public void preInit(File configFile) {
		File modFolder = new File(Minecraft.getMinecraft().mcDataDir, "mods/CustomShopHUD");
		if(!modFolder.exists()) {
			modFolder.mkdirs();
			
			try {
				String[] fileArr = { "background.png" };
				for(String file : fileArr) {
					Files.copy(getClass().getResourceAsStream("/assets/customshophud/" + file), new File(modFolder, file).toPath());
				}
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
		}
		
		List<IResourcePack> defaultResourcePacks = ObfuscationReflectionHelper.getPrivateValue(FMLClientHandler.class, FMLClientHandler.instance(), "resourcePackList");
		IResourcePack pack = new ModResourcePack(modFolder);
		defaultResourcePacks.add(pack);
		((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).reloadResourcePack(pack);
		
		File json = new File(modFolder, "settings.json");
		if(!json.exists()) {
			try {
				json.createNewFile();
				if(!json.canWrite()) 
					json.setWritable(true);

				JsonWriter jsonWriter = new JsonWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(json), "UTF-8")));
		        jsonWriter.setIndent("    ");
		        jsonWriter.setLenient(false);
		        jsonWriter.beginObject();
				
		        jsonWriter.name("reference_width").value(1920L);
		        jsonWriter.name("reference_height").value(1080L);
		        
		        jsonWriter.name("background_position").beginArray().value(16L).value(16L).endArray();
		        jsonWriter.name("player_skin_position").beginArray().value(38L).value(38L).endArray();
		        jsonWriter.name("player_skin_size").beginArray().value(100L).value(100L).endArray();
		        jsonWriter.name("text_position").beginArray().value(364L).value(35L).endArray();
		        jsonWriter.name("text_fontsize").value(26L);
		        jsonWriter.name("text_alignment").value("RIGHT");
		        
		        jsonWriter.endObject();
		        jsonWriter.close();
			} catch (IOException e) {
				LOGGER.error("Failed to write settings.json");
			}
		} else {
			try {
				JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(new FileInputStream(json), "UTF-8")));
				JsonParser jsonParser = new JsonParser();
				JsonObject jsonObject = jsonParser.parse(jsonReader).getAsJsonObject();

				HUD.referenceWidth = jsonObject.get("reference_width").getAsDouble();
				HUD.referenceHeight = jsonObject.get("reference_height").getAsDouble();
				
				HUD.backgroundPos[0] = jsonObject.getAsJsonArray("background_position").get(0).getAsDouble() / HUD.referenceWidth;
				HUD.backgroundPos[1] = jsonObject.getAsJsonArray("background_position").get(1).getAsDouble() / HUD.referenceHeight;
				HUD.playerSkinPos[0] = jsonObject.getAsJsonArray("player_skin_position").get(0).getAsDouble() / HUD.referenceWidth;
				HUD.playerSkinPos[1] = jsonObject.getAsJsonArray("player_skin_position").get(1).getAsDouble() / HUD.referenceHeight;
				HUD.playerSkinSize[0] = jsonObject.getAsJsonArray("player_skin_size").get(0).getAsDouble() / HUD.referenceWidth;
				HUD.playerSkinSize[1] = jsonObject.getAsJsonArray("player_skin_size").get(1).getAsDouble() / HUD.referenceHeight;
				HUD.textPos[0] = (float) (jsonObject.getAsJsonArray("text_position").get(0).getAsDouble() / HUD.referenceWidth);
				HUD.textPos[1] = (float) (jsonObject.getAsJsonArray("text_position").get(1).getAsDouble() / HUD.referenceHeight);
				HUD.textFontSize = (float) (jsonObject.get("text_fontsize").getAsDouble() / HUD.referenceHeight * 2.0D);
				
				String s = jsonObject.get("text_alignment").getAsString();
				if(s.equals("LEFT")) {
					HUD.textAlignment = 0;
				} else if(s.equals("CENTER")) {
					HUD.textAlignment = 1;
				} else {
					HUD.textAlignment = 2;
				}
				
				jsonReader.close();
			} catch (IOException e) {
				LOGGER.error("Failed to read settings.json");
			}
		}
	}
	
	@Override
	public void init() {
		EventHandler eventHandler = new EventHandler(Cs.getInstance());
		MinecraftForge.EVENT_BUS.register(eventHandler);
		
		NETWORK.registerMessage(ServerPacketUpdateHUD.Handle.class, ServerPacketUpdateHUD.class, PacketType.SERVER_UPDATE_HUD, Side.CLIENT);
	}
	
	@Override
	public void postInit() {

	}
}

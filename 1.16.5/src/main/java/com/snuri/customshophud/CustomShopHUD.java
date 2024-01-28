package com.snuri.customshophud;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.IPackNameDecorator;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackInfo.IFactory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.snuri.customshophud.network.PacketType;
import com.snuri.customshophud.network.ServerPacketUpdateHUD;
import com.snuri.customshophud.util.ModResourcePack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CustomShopHUD.MOD_ID)
public class CustomShopHUD {
	public static final String MOD_ID = "customshophud";
	public static final String NAME = "CustomShopHUD";
	public static final String VERSION = "1.1.0";
	
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static SimpleChannel simpleChannel;
    
    private Cs cs;

    public CustomShopHUD() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        cs = Cs.getInstance();
        MinecraftForge.EVENT_BUS.register(this);
    }

    // some preinit code
    private void setup(final FMLCommonSetupEvent event) {
		simpleChannel = NetworkRegistry.newSimpleChannel(new ResourceLocation("customshop", "channel2"), 
    		   () -> VERSION, (version) -> true, (version) -> true);
		
		simpleChannel.registerMessage(PacketType.SERVER_UPDATE_HUD, ServerPacketUpdateHUD.class, 
				ServerPacketUpdateHUD::encode, ServerPacketUpdateHUD::decode, 
				ServerPacketUpdateHUD::onPacketReceived, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    // do something that can only be done on the client
    private void doClientStuff(final FMLClientSetupEvent event) {
    	Minecraft mc = Minecraft.getInstance();
    	File modFolder = new File(mc.gameDirectory, "mods/CustomShopHUD");
    	if(!modFolder.exists()) {
			modFolder.mkdirs();
			
			try {
				String[] fileArr = { "pack.mcmeta", "background.png" };
				for(String file : fileArr) {
					Files.copy(getClass().getResourceAsStream("/assets/customshophud/" + file), new File(modFolder, file).toPath());
				}
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
		}
		
		/*
		IResourcePack pack = new ModResourcePack(modFolder);
		pack.close();
		((SimpleReloadableResourceManager) mc.getResourceManager()).add(pack);
		*/
		
		IResourcePack pack = new ModResourcePack(modFolder);
		pack.close();
		
		synchronized(mc.getResourcePackRepository()) {
			mc.getResourcePackRepository().addPackFinder(new IPackFinder() {
				@Override
				public void loadPacks(Consumer<ResourcePackInfo> consumer, IFactory factory) {
					ResourcePackInfo packInfo = ResourcePackInfo.create(pack.getName(), true, () -> pack, factory, ResourcePackInfo.Priority.TOP, IPackNameDecorator.DEFAULT);
		            if (packInfo != null) {
		               consumer.accept(packInfo);
		            }
				}
			});
			mc.getResourcePackRepository().reload();
			
			Set<String> selected = Sets.newLinkedHashSet();
			selected.addAll(mc.options.resourcePacks);
			selected.add(pack.getName());
			mc.getResourcePackRepository().setSelected(selected);
		}
		
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

    // some example code to dispatch IMC to another mod
    private void enqueueIMC(final InterModEnqueueEvent event) {
       
    }

    // some example code to receive and process InterModComms from other mods
    private void processIMC(final InterModProcessEvent event) {
       
    }
    
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    
    @SubscribeEvent
    public void renderGameOverlay(RenderGameOverlayEvent.Post event) {
    	if(event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
			cs.onRender(event.getPartialTicks());
		} 
    }
    
    @SubscribeEvent
	public void clientTick(TickEvent.ClientTickEvent event) {
		if(event.phase == TickEvent.Phase.END) {
			cs.onTick();
		}
	}
    
    @SubscribeEvent
	public void clientLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		cs.onLoggedOut();
	}

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        
    }
}

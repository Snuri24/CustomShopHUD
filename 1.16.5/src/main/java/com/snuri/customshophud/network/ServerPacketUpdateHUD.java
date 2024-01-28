package com.snuri.customshophud.network;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Supplier;

import com.snuri.customshophud.Cs;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

public class ServerPacketUpdateHUD {
	
	private boolean valid;
	
	private int money;
	
	public ServerPacketUpdateHUD() {
		valid = false;
	}
	
	public ServerPacketUpdateHUD(int money) {
		valid = true;
		this.money = money;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public static ServerPacketUpdateHUD decode(PacketBuffer buf) {
		int len = buf.readInt();
		String data = buf.toString(buf.readerIndex(), len, StandardCharsets.UTF_8);
		buf.readerIndex(buf.readerIndex() + len);
		
		ServerPacketUpdateHUD packet = new ServerPacketUpdateHUD(Integer.parseInt(data));
		return packet;
	}
	
	public void encode(PacketBuffer buf) {
		if(!valid) return;
		
		String data = new StringBuilder(money).toString();
		
		byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
	}
	
	public int getMoney() {
		return money;
	}
	
	public static void onPacketReceived(final ServerPacketUpdateHUD packet, Supplier<NetworkEvent.Context> ctxSupplier) {
		NetworkEvent.Context ctx = ctxSupplier.get();
		LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
		ctx.setPacketHandled(true);
		
		if(sideReceived != LogicalSide.CLIENT) {
			return;
		}
		if(!packet.isValid()) {
			return;
		}
		Optional<ClientWorld> clientWorld = LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
		if(!clientWorld.isPresent()) {
			return;
		}
		
		ctx.enqueueWork(() -> Cs.getInstance().processPacket(clientWorld.get(), packet));
	}
}

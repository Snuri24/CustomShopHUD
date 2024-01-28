package com.snuri.customshophud.network;

import java.nio.charset.StandardCharsets;

import com.snuri.customshophud.Cs;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ServerPacketUpdateHUD implements IMessage {
	
	private int money;
	
	public ServerPacketUpdateHUD() {
		
	}
	
	public ServerPacketUpdateHUD(int money) {
		this.money = money;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		int len = buf.readInt();
		String data = buf.toString(buf.readerIndex(), len, StandardCharsets.UTF_8);
		buf.readerIndex(buf.readerIndex() + len);
		
		money = Integer.parseInt(data);
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		String data = new StringBuilder(money).toString();
		
		byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
	}
	
	public int getMoney() {
		return money;
	}
	
	public static class Handle implements IMessageHandler<ServerPacketUpdateHUD, IMessage> {
		@Override
		public IMessage onMessage(ServerPacketUpdateHUD message, MessageContext ctx) {
			Cs.getInstance().handleMessage(message);
			return null;
		}
	}
}

package com.luacraft;

import com.naef.jnlua.LuaRuntimeException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerDisconnectionFromClientEvent;

public class LuaPacketManager {

	private LuaCraftState l;

	public LuaPacketManager(LuaCraftState state) {
		l = state;
	}
	
	@SubscribeEvent
	public void onClientConnectToServer(ClientConnectedToServerEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;
			
			EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).player;

			try {
				l.pushHookCall();
				l.pushString("client.connect");
				l.pushUserdataWithMeta(player, "Player");
				l.pushBoolean(event.isLocal());
				l.pushString(event.getConnectionType());
				l.call(3, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaRuntimeError(e);
			} finally {
				l.setTop(0);
			}
		}
	}
	
	@SubscribeEvent
	public void onClientDisconnectFromServer(ClientDisconnectionFromServerEvent event) {

		synchronized (l) {
			if (!l.isOpen())
				return;
			
			EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).player;
			
			try {
				l.pushHookCall();
				l.pushString("client.disconnect");
				l.pushUserdataWithMeta(player, "Player");
				l.call(2, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaRuntimeError(e);
			} finally {
				l.setTop(0);
			}
		}
	}
	
	@SubscribeEvent
	public void onClientConnectToServer(ServerConnectionFromClientEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;
			
			EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).player;
			
			try {
				l.pushHookCall();
				l.pushString("client.connect");
				l.pushUserdataWithMeta(player, "Player");
				l.pushBoolean(event.isLocal());
				l.call(2, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaRuntimeError(e);
			} finally {
				l.setTop(0);
			}
		}
	}
	
	@SubscribeEvent
	public void onClientDisconnectFromServer(ServerDisconnectionFromClientEvent event) {

		synchronized (l) {
			if (!l.isOpen())
				return;
			
			EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).player;
			
			try {
				l.pushHookCall();
				l.pushString("client.disconnect");
				l.pushUserdataWithMeta(player, "Player");
				l.call(2, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaRuntimeError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	@SubscribeEvent
	public void onClientPacket(ClientCustomPacketEvent event) {
		if (event.getPacket().channel().equals(LuaCraft.NET_CHANNEL)) {
			synchronized (l) {
				if (!l.isOpen())
					return;
	
				try {
					PacketBuffer buffer = new PacketBuffer(event.getPacket().payload());
	
					// Peek into the packet
					String func = buffer.readString(32767);
	
					// If it's a LuaFile handle it internally
					if (func.equals("LuaFile")) {
						String file = buffer.readString(32767);
						byte[] data = buffer.readByteArray();
						l.downloadLuaFile(file, data);
						return;
					}
	
					buffer.readerIndex(0);
					l.pushIncomingNet();
					l.pushUserdataWithMeta(buffer, "ByteBuf");
					l.call(1, 0);
				} catch (LuaRuntimeException e) {
					l.handleLuaRuntimeError(e);
				}
			}
		}
	}

	@SubscribeEvent
	public void onServerPacket(ServerCustomPacketEvent event) {
		if (event.getPacket().channel().equals(LuaCraft.NET_CHANNEL)) {
			synchronized (l) {
				if (!l.isOpen())
					return;

				try {
					PacketBuffer buffer = new PacketBuffer(event.getPacket().payload());
					EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).player;
					l.pushIncomingNet();
					l.pushUserdataWithMeta(buffer, "ByteBuf");
					l.pushUserdataWithMeta(player, "Player");
					l.call(2, 0);
				} catch (LuaRuntimeException e) {
					l.handleLuaRuntimeError(e);
				}
			}
		}
	}
}

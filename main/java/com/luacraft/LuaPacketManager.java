package com.luacraft;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.luacraft.classes.LuaCache;
import com.naef.jnlua.LuaRuntimeException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LuaPacketManager {

	private LuaCraftState l;

	public LuaPacketManager(LuaCraftState state) {
		l = state;
	}

	private void handleClientPacket(PacketBuffer buffer) throws LuaRuntimeException {
		// Peek into the packet
		buffer.readerIndex(0);
		String func = buffer.readString(32767);

		// If it's a LuaFile handle it internally
		if (func.equals("CachedLuaFiles")) {
			int numFiles = buffer.readVarInt();
			for (int i = 1; i <= numFiles; i++) {
				String file = buffer.readString(32767);
				String hash = buffer.readString(32767);
				byte[] data = buffer.readByteArray();
				try {
					l.info("Downloaded file: " + file);
					LuaCache.cacheFile(file, hash, data);
				} catch (SQLException e) {
					l.error("Failed to cache file: " + e.getLocalizedMessage());
				}
			}
			((LuaClient) l).autorunScripts();
			return;
		} else if (func.equals("LuaCacheSync")) {
			l.warning("Received LuaCacheSync");
			// Store the servers values so we can compare against our own
			HashMap<String, String> serverCache = new HashMap<String, String>();
			
			int numFiles = buffer.readVarInt();
			for (int i = 1; i <= numFiles; i++) {
				String file = buffer.readString(32767);
				String hash = buffer.readString(32767);
				serverCache.put(file, hash);
				l.info("\t" + file + ":\t" + hash);
			}
			try {
				boolean needFiles = LuaCache.compareAndRequestFiles(serverCache);
				if (!needFiles) {
					((LuaClient) l).autorunScripts();
				}
			} catch (SQLException | NoSuchAlgorithmException | IOException e) {
				l.error("Failed to validate cache: " + e.getLocalizedMessage());
			}
			return;
		}

		// Call net.Incomming library
		buffer.readerIndex(0);
		l.pushIncomingNet();
		l.pushUserdataWithMeta(buffer, "ByteBuf");
		l.call(1, 0);
	}

	private void handleServerPacket(PacketBuffer buffer, EntityPlayer player) throws LuaRuntimeException {
		// Peek into the packet
		buffer.readerIndex(0);
		String func = buffer.readString(32767);

		// If it's a LuaFile handle it internally
		if (func.equals("GetCachedLuaFiles")) {
			ArrayList<String> files = new ArrayList<String>();
			int numFiles = buffer.readVarInt();
			for (int i = 1; i <= numFiles; i++) {
				files.add(buffer.readString(32767));
			}
			try {
				LuaCache.sendFilesToClient(files, player);
			} catch (SQLException e) {
				l.error("Failed to send file to client: " + e.getLocalizedMessage());
			}
			return;
		}

		// Call net.Incomming library
		buffer.readerIndex(0);
		l.pushIncomingNet();
		l.pushUserdataWithMeta(buffer, "ByteBuf");
		l.pushUserdataWithMeta(player, "Player");
		l.call(2, 0);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onClientPacket(ClientCustomPacketEvent event) {
		if (event.getPacket().channel().equals(LuaCraft.NET_CHANNEL)) {
			
			PacketBuffer buffer = new PacketBuffer(event.getPacket().payload());			
			Side target = event.getPacket().getTarget();
			
			synchronized (l) {
				if (!l.isOpen() || target != l.getSide())
					return;
	
				try {
					switch(target) {
					case CLIENT:
						handleClientPacket(buffer);
						break;
					case SERVER:
						handleServerPacket(buffer, LuaCraft.getClient().player); // This only happens on listened servers
						break;
					default:
						break;
					}
				} catch (LuaRuntimeException e) {
					l.handleLuaRuntimeError(e);
				}
			}
		}
	}

	@SubscribeEvent
	public void onServerPacket(ServerCustomPacketEvent event) {
		if (event.getPacket().channel().equals(LuaCraft.NET_CHANNEL)) {

			PacketBuffer buffer = new PacketBuffer(event.getPacket().payload());			
			Side target = event.getPacket().getTarget();
			
			synchronized (l) {
				if (!l.isOpen() || target != l.getSide())
					return;

				try {
					switch(target) {
					case CLIENT:
						handleClientPacket(buffer);
						break;
					case SERVER:
						handleServerPacket(buffer, ((NetHandlerPlayServer) event.getHandler()).player);
						break;
					default:
						break;
					}
				} catch (LuaRuntimeException e) {
					l.handleLuaRuntimeError(e);
				}
			}
		}
	}
}

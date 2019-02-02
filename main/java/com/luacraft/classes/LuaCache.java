package com.luacraft.classes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Hex;

import com.luacraft.LuaCraft;

import io.netty.buffer.Unpooled;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class LuaCache {
	private static Connection connection;
	
	private static HashMap<String, String> cacheMap;
	
	public static void initialize() throws SQLException {
		cacheMap = new HashMap<String, String>();
		connection = DriverManager.getConnection("jdbc:sqlite:luacraft/cache.db");
		Statement stmt = connection.createStatement();
		stmt.execute("CREATE TABLE IF NOT EXISTS cache ( file TEXT NOT NULL PRIMARY KEY, hash TEXT NOT NULL, data TEXT )");
	}
	
	public static void addCSLuaFile(String file) throws IOException, SQLException {
		String path = "lua/" + file;
		
		File luaFile = FileMount.GetFile(path);
		
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		
		byte[] buffer = new byte[4096];
		FileInputStream stream = new FileInputStream(luaFile);
		
		int bytesRead;
        while ((bytesRead = stream.read(buffer)) != -1) {
        	gzip.write(buffer, 0, bytesRead);
    		digest.update(buffer, 0, bytesRead);
        }
        
		stream.close();
		gzip.close();
		
		String hash = Hex.encodeHexString(digest.digest());
		
		cacheMap.put(file, hash);
        
		PreparedStatement stmt = connection.prepareStatement("REPLACE INTO cache(file, hash, data) VALUES(?, ?, ?);");
		stmt.setString(1, file);
		stmt.setString(2, hash);
		stmt.setBytes(3, out.toByteArray());
		stmt.execute();
		stmt.close();
		
		out.close();
	}
	
	public static GZIPInputStream getFileInputStream(String file) throws SQLException, IOException {
		PreparedStatement stmt = connection.prepareStatement("SELECT data FROM cache WHERE file=?;");
		stmt.setString(1, file);
		ResultSet result = stmt.executeQuery();
		while (result.next()) {
			return new GZIPInputStream(result.getBinaryStream("data"));
		}
		return null;
	}
	
	public static HashMap<String, String> getCacheMap() {
		return cacheMap;
	}
	
	@SubscribeEvent
	public void onClientConnectToServer(ServerConnectionFromClientEvent event) {
		HashMap<String, String> cache = LuaCache.getCacheMap();
		
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		buffer.writeString("LuaCacheSync");
		buffer.writeVarInt(cache.size());

		LuaCraft.getLogger().info("LuaCacheSync");
		LuaCraft.getLogger().info("Files: " + cache.size());
		
		for (Entry<String, String> entry : cache.entrySet()) {
		    String file = entry.getKey();
		    String hash = entry.getValue();
		    buffer.writeString(file);
		    buffer.writeString(hash);
		    
		    LuaCraft.getLogger().info("\t" + file + ": " + hash);
		}
		
		LuaCraft.channel.sendTo(new FMLProxyPacket(buffer, LuaCraft.NET_CHANNEL), ((NetHandlerPlayServer) event.getHandler()).player);
		
		LuaCraft.getLogger().info("Sent LuaCacheSync to player..");
	}
	
	// SELECT * FROM cache WHERE file LIKE 'autorun/%.lua'
}

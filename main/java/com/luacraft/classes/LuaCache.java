package com.luacraft.classes;

import java.io.ByteArrayInputStream;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Hex;

import com.luacraft.LuaCraft;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class LuaCache {
	private static Connection connection;

	private static HashMap<String, String> cacheMap;
	private static HashMap<String, String> serverCache;
	
	private static final String HASH_TYPE = "SHA-256";
	private static final int CACHE_VERSION = 1;
	
	public static HashMap<String, String> getCachedFilesForServer() {
		return serverCache;
	}
	
	public static List<String> getAutorunShared() {
		List<String> files = new ArrayList<String>();
		
		for (Entry<String, String> entry : serverCache.entrySet()) {
			String file = entry.getKey();
			if (!file.startsWith("autorun/client/") && (file.startsWith("autorun/") || file.startsWith("autorun/shared/")))
				files.add(file);
		}
		
		return files;
	}
	
	public static List<String> getAutorunClient() {
		List<String> files = new ArrayList<String>();
		
		for (Entry<String, String> entry : serverCache.entrySet()) {
			String file = entry.getKey();
			if (file.startsWith("autorun/client/"))
				files.add(file);
		}
		
		return files;
	}
	
	// Server and client both have a cache.db file to sync
	public static void initialize() throws SQLException {
		cacheMap = new HashMap<String, String>();
		serverCache = new HashMap<String, String>();
		connection = DriverManager.getConnection("jdbc:sqlite:luacraft/cache.db");
		Statement stmt = connection.createStatement();
		stmt.execute("CREATE TABLE IF NOT EXISTS cache ( file TEXT NOT NULL PRIMARY KEY, hash TEXT NOT NULL, data TEXT NOT NULL )");
		stmt.close();
	}
	
	// Only used by the server
	public static void addCSLuaFile(String file) throws IOException, SQLException, NoSuchAlgorithmException {
		File luaFile = FileMount.GetFile("lua/" + file);
		
		MessageDigest digest = MessageDigest.getInstance(HASH_TYPE);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		
		byte[] buffer = new byte[4096];
		FileInputStream stream = new FileInputStream(luaFile);
		
		int bytesRead;
        while ((bytesRead = stream.read(buffer)) != -1) {
        	gzip.write(buffer, 0, bytesRead); // Compress our file in chunks
    		digest.update(buffer, 0, bytesRead); // Hash our file in chunks
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
	
	// Allows for the client to decompress and load the file
	public static GZIPInputStream getFileInputStream(String file) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("SELECT data FROM cache WHERE file=?;");
		stmt.setString(1, file);
		ResultSet result = stmt.executeQuery();
		
		GZIPInputStream stream = null;
		
		if (result.next()) {
			try {
				stream = new GZIPInputStream(new ByteArrayInputStream(result.getBytes("data")));
			} catch (IOException e) {} // Ignore GZIP errors, allowing this to return null and have the client request a download
		}
		
		result.close();
		stmt.close();
		
		return stream;
	}
	
	// Only used by the server
	public static void syncCacheToPlayer(EntityPlayer player) {		
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		buffer.writeString("LuaCacheSync");
		buffer.writeVarInt(cacheMap.size());

		for (Entry<String, String> entry : cacheMap.entrySet()) {
		    buffer.writeString(entry.getKey()); // Filename
		    buffer.writeString(entry.getValue()); // Hash value
		}
		LuaCraft.channel.sendTo(new FMLProxyPacket(buffer, LuaCraft.NET_CHANNEL), (EntityPlayerMP) player);
	}
	
	// Only used by the server
	public static void sendFilesToClient(ArrayList<String> files, EntityPlayer player) throws SQLException {
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		buffer.writeString("CachedLuaFiles");
		buffer.writeVarInt(files.size());

		for (String file : files) {
			PreparedStatement stmt = connection.prepareStatement("SELECT hash, data FROM cache WHERE file=?;");
			stmt.setString(1, file);
			ResultSet result = stmt.executeQuery();
			
			if (result.next()) {
				buffer.writeString(file);
				buffer.writeString(result.getString("hash"));
				buffer.writeByteArray(result.getBytes("data"));
			}
			
			result.close();
			stmt.close();
		}
		
		LuaCraft.channel.sendTo(new FMLProxyPacket(buffer, LuaCraft.NET_CHANNEL), (EntityPlayerMP) player);
	}
	
	// Only used by the client
	public static boolean compareAndRequestFiles(HashMap<String, String> serverHashes) throws SQLException, NoSuchAlgorithmException, IOException {
		ArrayList<String> files = new ArrayList<String>();
		
		for (Entry<String, String> entry : serverHashes.entrySet()) {
			String file = entry.getKey();
			String hash = entry.getValue();
			
			serverCache.put(file, hash);
			
			GZIPInputStream stream = getFileInputStream(file);
			String clHash = null;
			
			if (stream != null) {
				MessageDigest digest = MessageDigest.getInstance(HASH_TYPE);
				byte[] buffer = new byte[4096];
				int bytesRead;
		        while ((bytesRead = stream.read(buffer)) != -1) {
		    		digest.update(buffer, 0, bytesRead); // Hash our file in chunks
		        }
		        clHash = Hex.encodeHexString(digest.digest());
		        stream.close();
			}
			
			// Request the file if the entry doesn't exist in the cache or the hashes mismatch
			if (stream == null || clHash == null || !clHash.equals(hash)) {
				files.add(file);
			}
		}
		
		if (files.isEmpty()) return false; // Nothing needed!
		
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		buffer.writeString("GetCachedLuaFiles");
		buffer.writeVarInt(files.size());
		for (String file : files) {
			buffer.writeString(file);
		}
		LuaCraft.channel.sendToServer(new FMLProxyPacket(buffer, LuaCraft.NET_CHANNEL));
		
		return true;
	}

	// Only used by the client
	public static void cacheFile(String file, String hash, byte[] data) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("REPLACE INTO cache(file, hash, data) VALUES(?, ?, ?);");
		stmt.setString(1, file);
		stmt.setString(2, hash);
		stmt.setBytes(3, data);
		stmt.execute();
		stmt.close();
	}
	
	// SELECT * FROM cache WHERE file LIKE 'autorun/client/%.lua'
}

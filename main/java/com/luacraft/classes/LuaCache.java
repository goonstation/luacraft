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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Hex;

public class LuaCache {
	private static Connection connection;
	
	public static void initialize() throws SQLException {
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
        
		PreparedStatement stmt = connection.prepareStatement("REPLACE INTO cache(file, hash, data) VALUES(?, ?, ?);");
		stmt.setString(1, file);
		stmt.setString(2, Hex.encodeHexString(digest.digest()));
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
	
	// SELECT * FROM cache WHERE file LIKE 'autorun/%.lua'
}

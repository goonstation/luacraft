package com.luacraft;

import org.apache.commons.lang3.SystemUtils;

import com.naef.jnlua.NativeSupport.Loader;

public class LuaLoader implements Loader {
	private static boolean DEVELOPER = true;
	
	private String rootDir = null;

	private String arch = null;
	private String liblua = null;
	private String libjnlua = null;

	private String libraryDir = "libraries/com/luacraft/";

	public LuaLoader(String dir) {
		rootDir = dir;
		arch = SystemUtils.OS_ARCH.contains("64") ? "64" : "32";
		
		if (SystemUtils.IS_OS_WINDOWS) {
			liblua = "lua51.dll";
			libjnlua = "jnlua51.dll";
		} else if (SystemUtils.IS_OS_LINUX) {
			liblua = "libluajit.so";
			libjnlua = "libjnluajit.so";
		} else {
			LuaCraft.getLogger().error(String.format("Your OS (%s) is currently unsupported", System.getProperty("os.name")));
		}
	}

	private void loadLib(String lib) {
		LuaCraft.extractFile("bins/" + arch + "/" + lib, libraryDir + lib);
		System.load(rootDir + libraryDir + lib);
	}

	public void load() {
		loadLib(liblua);
		loadLib(libjnlua);
	}
}
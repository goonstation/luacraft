package com.luacraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.luacraft.classes.FileMount;
import com.luacraft.classes.LuaCache;
import com.luacraft.classes.LuaJavaChannel;
import com.luacraft.classes.LuaJavaRunCommand;
import com.luacraft.console.ConsoleManager;
import com.naef.jnlua.NativeSupport;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = LuaCraft.MODID,
		name = LuaCraft.MODNAME,
		version = LuaCraft.VERSION,
		guiFactory = "com.luacraft.LuaCraftGuiFactory")
public class LuaCraft {
	public static final String MODNAME = "LuaCraft";
	public static final String MODID = "luacraft";
	public static final String VERSION = "1.3";
	public static final String DEFAULT_RESOURCEPACK = "luacraftassets";
	public static final String NET_CHANNEL = "LuaCraft:net";
	
	public static FMLEventChannel channel;
	private static Logger logger;
	public static LuaConfig config;

	public static String rootDir = System.getProperty("user.dir") + File.separator + MODID + File.separator;
	
	public static HashMap<String, LuaJavaChannel> threadChannels = new HashMap<String, LuaJavaChannel>();
	public static HashMap<Side, LuaCraftState> states = new HashMap<Side, LuaCraftState>();
	private static LuaLoader loader = new LuaLoader(rootDir);

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		NativeSupport.getInstance().setLoader(loader);
		
		ModContainer modContainer = FMLCommonHandler.instance().findContainerFor(this);
		
		logger = LogManager.getLogger(modContainer.getName());
		config = new LuaConfig(event.getSuggestedConfigurationFile());

		FileMount.SetRoot(rootDir);
		FileMount.CreateDirectories("addons");
		FileMount.CreateDirectories("jars");
		FileMount.CreateDirectories("lua\\autorun\\client");
		FileMount.CreateDirectories("lua\\autorun\\server");
		FileMount.CreateDirectories("lua\\autorun\\shared");
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(NET_CHANNEL);
		MinecraftForge.EVENT_BUS.register(config);

		try {
			LuaCache.initialize();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		LuaAddonManager.initialize();
		if (event.getSide() == Side.CLIENT)
			LuaResourcePackLoader.initialize();

		if (event.getSide() == Side.CLIENT) {
			LuaClient state = new LuaClient();
			synchronized (state) {
				ConsoleManager.create();
				state.initialize(true);
				//state.autoRunScripts();
			}
			synchronized (states) {
				states.put(Side.CLIENT, state);
			}
		} else {
			LuaServer state = new LuaServer();
			synchronized (state) {
				if (state.getServer().getGuiEnabled())
					ConsoleManager.create();
				state.initialize(true);
				state.autorunScripts();
			}
			synchronized (states) {
				states.put(Side.SERVER, state);
			}
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		logger.info(rootDir);
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {		
		event.registerServerCommand(new LuaJavaRunCommand());

		if (event.getSide().isClient() && states.get(Side.SERVER) == null) {
			LuaServer state = new LuaServer();
			synchronized (state) {
				state.setRunningSide(Side.CLIENT); // Singleplayer fix.. the server is running on the client
				state.initialize(true);
				state.autorunScripts();
			}
			synchronized (states) {
				states.put(Side.SERVER, state);
			}
		}
	}

	@EventHandler
	public void serverStarted(FMLServerStartedEvent event) {
	}

	@EventHandler
	public void serverStopping(FMLServerStoppingEvent event) {
		LuaCraftState state = states.get(Side.SERVER);
		synchronized (state) {
			if (event.getSide().isClient() && state != null) {
				state.close();
				states.remove(Side.SERVER);
			}
		}
	}
	
	public static FMLClientHandler getForgeClient() {
		return FMLClientHandler.instance();
	}
	
	@SideOnly(Side.CLIENT)
	public static net.minecraft.client.Minecraft getClient() {
		return getForgeClient().getClient();
	}
	
	public static boolean isListenedServer() {
		return getClient().world.isRemote;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static LuaCraftState getLuaState(Side side) {
		return states.get(side);
	}

	public static InputStream getPackedFileInputStream(String file) {
		return LuaCraft.class.getResourceAsStream('/' + file);
	}

	public static File extractFile(String strFrom, String strTo) {
		File extractedFile = new File(rootDir, strTo);
		int lastSlash = extractedFile.toString().lastIndexOf(File.separator);
		String filePath = extractedFile.toString().substring(0, lastSlash);

		File fileDirectory = new File(filePath);
		if (!fileDirectory.exists())
			fileDirectory.mkdirs();

		int readBytes;
		byte[] buffer = new byte[1024];

		InputStream fileInStream = getPackedFileInputStream(strFrom);
		OutputStream fileOutStream = null;

		try {
			fileOutStream = new FileOutputStream(extractedFile);

			while ((readBytes = fileInStream.read(buffer)) != -1)
				fileOutStream.write(buffer, 0, readBytes);
		} catch (Exception e) {
			throw new Error(e.getMessage());
		} finally {
			try {
				fileOutStream.close();
				fileInStream.close();
			} catch (Exception e) {
				// throw new Error(e.getMessage());
				e.printStackTrace();
			}

		}
		return extractedFile;
	}

	public static void reloadClientState() {
		LuaClient state = (LuaClient) getLuaState(Side.CLIENT);

		synchronized (state) {
			state.autorunScripts();
		}
	}

	public static void reloadServerState() {
		LuaServer state = (LuaServer) getLuaState(Side.SERVER);
		
		synchronized (state) {
			state.autorunScripts();
		}
	}

	public static void checkVersion() {
		try {
			URL requestURL = new URL("http://luacraft.com/update/" + VERSION);
			HttpURLConnection httpCon = (HttpURLConnection) requestURL.openConnection();

			httpCon.setRequestMethod("GET");
			httpCon.setRequestProperty("User-Agent", "Minecraft, LuaCraft");

			String inputLine;
			StringBuffer resBuffer = new StringBuffer();
			int responseCode = httpCon.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));

			while ((inputLine = in.readLine()) != null)
				resBuffer.append(inputLine);

			in.close();
			String webVersion = resBuffer.toString();
			if (!webVersion.equals(VERSION))
				LuaCraft.getLogger().info("A newer version of LuaCraft (" + webVersion + ") is available!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getLuaDirectory() {
		return rootDir + "lua" + File.separator;
	}
}

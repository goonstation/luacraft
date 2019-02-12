package com.luacraft;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.lang3.SystemUtils;

import com.luacraft.classes.LuaCache;
import com.luacraft.classes.LuaScriptedItem;
import com.luacraft.library.LuaGlobals;
import com.luacraft.library.LuaLibConsole;
import com.luacraft.library.LuaLibFile;
import com.luacraft.library.LuaLibHTTP;
import com.luacraft.library.LuaLibLanguage;
import com.luacraft.library.LuaLibSQL;
import com.luacraft.library.LuaLibThread;
import com.luacraft.library.LuaLibUtil;
import com.luacraft.meta.LuaAngle;
import com.luacraft.meta.LuaBiomeGenBase;
import com.luacraft.meta.LuaBlock;
import com.luacraft.meta.LuaByteBuf;
import com.luacraft.meta.LuaChannel;
import com.luacraft.meta.LuaChunk;
import com.luacraft.meta.LuaColor;
import com.luacraft.meta.LuaContainer;
import com.luacraft.meta.LuaDamageSource;
import com.luacraft.meta.LuaDataWatcher;
import com.luacraft.meta.LuaEntity;
import com.luacraft.meta.LuaEntityDamageSource;
import com.luacraft.meta.LuaEntityItem;
import com.luacraft.meta.LuaExplosion;
import com.luacraft.meta.LuaFile;
import com.luacraft.meta.LuaItemStack;
import com.luacraft.meta.LuaLiving;
import com.luacraft.meta.LuaLivingBase;
import com.luacraft.meta.LuaNBTTag;
import com.luacraft.meta.LuaObject;
import com.luacraft.meta.LuaPlayer;
import com.luacraft.meta.LuaResource;
import com.luacraft.meta.LuaSQLDatabase;
import com.luacraft.meta.LuaSQLQuery;
import com.luacraft.meta.LuaThread;
import com.luacraft.meta.LuaVector;
import com.luacraft.meta.LuaWorld;
import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaSyntaxException;

import net.minecraftforge.common.MinecraftForge;

public class LuaShared extends LuaCraftState {
	private LuaEventManager luaEvent;
	private LuaPacketManager packet;

	public void initializeShared(boolean hooks) {
		if(hooks) setupReloader();

		if (hooks) {
			packet = new LuaPacketManager(this);
			luaEvent = new LuaEventManager(this);

			printSide("Registering packet manager");
			LuaCraft.channel.register(packet);
			printSide("Registering shared event manager");
			MinecraftForge.EVENT_BUS.register(luaEvent);
		}

		loadBaseLibraries();
		loadLibraries();
		loadExtensions();
	}

	public void autorunSharedScripts() {
		info("Loading lua/autorun/*.lua");
		try {
			for (String file : LuaCache.getAutorunShared()) {
				includeFileStream(LuaCache.getFileInputStream(file), "cache:" + file);
			}
			autorun(); // Load all files within autorun
			autorun("shared"); // Failsafe, incase someone thinks they need a shared folder
		} catch (IOException | SQLException | LuaSyntaxException e) {
			error(e.getMessage());
			e.printStackTrace();
		} catch(LuaRuntimeException e) {
			handleLuaRuntimeError(e);
		}
	}

	public void close() {
		if (packet != null) {
			printSide("Unregistering packet manager");
			LuaCraft.channel.unregister(packet);
			packet = null;
		}
		if (luaEvent != null) {
			printSide("Unregistering shared event manager");
			MinecraftForge.EVENT_BUS.unregister(luaEvent);
			luaEvent = null;
		}
		LuaLibThread.interruptActiveThreads();
		super.close();
	}

	private void loadExtensions() {
		printSide("Loading extensions..");

		// Load all packed modules from our Jar
		includePackedFile("lua/modules/hook.lua");
		includePackedFile("lua/modules/net.lua");

		// Load all packed extensions from our Jar
		includePackedFile("lua/extensions/math.lua");
		includePackedFile("lua/extensions/player.lua");
		includePackedFile("lua/extensions/string.lua");
		includePackedFile("lua/extensions/table.lua");

		includeDirectory("extensions"); // Load any extensions a user made
	}
	
	private void loadBaseLibraries() {
		printSide("Loading base libraries..");
		
		openLib(Library.BASE);
		openLib(Library.PACKAGE);
		openLib(Library.TABLE);
		openLib(Library.IO);
		openLib(Library.OS);
		openLib(Library.STRING);
		openLib(Library.MATH);
		openLib(Library.DEBUG);
		openLib(Library.BIT);
		openLib(Library.JIT);
		openLib(Library.FFI);

		// Set the registry to support _R
		pushValue(LuaState.REGISTRYINDEX);
		setGlobal("_R");

		// Disable the output/input buffer
		load("io.stdout:setvbuf('no')", "=LuaShared.LoadLibraries");
		call(0, 0);
		load("io.stderr:setvbuf('no')", "=LuaShared.LoadLibraries");
		call(0, 0);

		String lua = LuaCraft.getLuaDirectory();

		// Set the package path to the correct location
		getGlobal("package");
		pushString(lua + "modules/?.lua;" + lua + "modules/bin/?.lua;" + lua + "modules/?/init.lua");
		setField(-2, "path");
		pop(1);

		// Set the package path to the correct location
		getGlobal("package");
		if (SystemUtils.IS_OS_WINDOWS)
			pushString(lua + "modules/?.dll;" + lua + "modules/bin/?.dll;" + lua + "modules/bin/loadall.dll");
		else if (SystemUtils.IS_OS_LINUX)
			pushString(lua + "modules/?.so;" + lua + "modules/bin/?.so;" + lua + "modules/bin/loadall.so");
		else if (SystemUtils.IS_OS_MAC_OSX)
			pushString(lua + "modules/?.dylib;" + lua + "modules/bin/?.dylib;" + lua + "modules/bin/loadall.dylib");
		setField(-2, "cpath");
		pop(1);
	}

	private void loadLibraries() {
		printSide("Loading shared libraries..");

		// Libs
		LuaGlobals.Init(this);
		LuaLibFile.Init(this);
		LuaLibHTTP.Init(this);
		LuaLibConsole.Init(this);
		LuaLibLanguage.Init(this);
		LuaLibSQL.Init(this);
		LuaLibThread.Init(this);
		LuaLibUtil.Init(this);

		// Meta
		LuaAngle.Init(this);
		LuaBiomeGenBase.Init(this);
		LuaBlock.Init(this);
		LuaByteBuf.Init(this);
		LuaChannel.Init(this);
		LuaColor.Init(this);
		LuaContainer.Init(this);
		LuaChunk.Init(this);
		LuaDamageSource.Init(this);
		LuaDataWatcher.Init(this);
		LuaEntity.Init(this);
		LuaEntityDamageSource.Init(this);
		LuaEntityItem.Init(this);
		LuaExplosion.Init(this);
		LuaFile.Init(this);
		LuaScriptedItem.Init(this);
		LuaItemStack.Init(this);
		LuaLiving.Init(this);
		LuaLivingBase.Init(this);
		LuaObject.Init(this);
		LuaNBTTag.Init(this);
		LuaPlayer.Init(this);
		LuaResource.Init(this);
		LuaThread.Init(this);
		LuaSQLDatabase.Init(this);
		LuaSQLQuery.Init(this);
		LuaVector.Init(this);
		LuaWorld.Init(this);
	}
}

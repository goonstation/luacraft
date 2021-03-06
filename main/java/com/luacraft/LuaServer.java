package com.luacraft;

import com.luacraft.library.server.LuaGlobals;
import com.luacraft.library.server.LuaLibCommand;
import com.luacraft.library.server.LuaLibGame;
import com.luacraft.meta.server.LuaByteBuf;
import com.luacraft.meta.server.LuaCommand;
import com.luacraft.meta.server.LuaPlayer;
import com.luacraft.meta.server.LuaPropertyManager;
import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaSyntaxException;

import net.minecraftforge.fml.relauncher.Side;

public class LuaServer extends LuaShared {
	
	public LuaServer() {
		super();
		setSide(Side.SERVER);
	}

	public void initialize(boolean hooks) {
		pushBoolean(false);
		setGlobal("CLIENT");
		pushBoolean(true);
		setGlobal("SERVER");
		
		initializeShared(hooks);
		loadLibraries();
	}

	public void autorunScripts() {
		autorunSharedScripts();
		info("Loading lua/autorun/server/*.lua");
		try {
			autorun("server"); // Load all files within autorun/server
		} catch(LuaRuntimeException e) {
			handleLuaRuntimeError(e);
		} catch(LuaSyntaxException e) {
			error(e.getMessage());
			e.printStackTrace();
		}
	}

	private void loadLibraries() {
		printSide("Loading server libraries..");

		// Libs
		LuaGlobals.Init(this);
		LuaLibCommand.Init(this);
		LuaLibGame.Init(this);

		// Meta
		LuaCommand.Init(this);
		LuaPlayer.Init(this);
		LuaByteBuf.Init(this);
		LuaPropertyManager.Init(this);
	}
}
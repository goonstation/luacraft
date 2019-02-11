package com.luacraft.meta.server;

import com.luacraft.LuaCraftState;
import com.luacraft.LuaUserdata;
import com.luacraft.classes.LuaJavaCommand;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class LuaCommand {
	
	private static JavaFunction __tostring = new JavaFunction() {
		public int invoke(LuaState l) {
			LuaJavaCommand self = (LuaJavaCommand) l.checkUserdata(1, LuaJavaCommand.class, "Command");
			l.pushString(String.format("Command: 0x%08x", l.toPointer(1)));
			return 1;
		}
	};
	
	private static JavaFunction SetName = new JavaFunction() {
		public int invoke(LuaState l) {
			LuaJavaCommand self = (LuaJavaCommand) l.checkUserdata(1, LuaJavaCommand.class, "Command");
			self.setName(l.checkString(2));
			l.setTop(1); // Return ourself to chain aliases and stuff
			return 1;
		}
	};
	
	private static JavaFunction GetName = new JavaFunction() {
		public int invoke(LuaState l) {
			LuaJavaCommand self = (LuaJavaCommand) l.checkUserdata(1, LuaJavaCommand.class, "Command");
			l.pushString(self.getName());
			return 1;
		}
	};
	
	private static JavaFunction AddAlias = new JavaFunction() {
		public int invoke(LuaState l) {
			LuaJavaCommand self = (LuaJavaCommand) l.checkUserdata(1, LuaJavaCommand.class, "Command");
			self.addAlias(l.checkString(2));
			l.setTop(1); // Return ourself to chain aliases and stuff
			return 1;
		}
	};
	
	private static JavaFunction GetAliases = new JavaFunction() {
		public int invoke(LuaState l) {
			LuaJavaCommand self = (LuaJavaCommand) l.checkUserdata(1, LuaJavaCommand.class, "Command");
			l.newTable();
			for (String name : self.getAliases()) {
				l.pushNumber(l.tableSize(-1) + 1);
				l.pushString(name);
				l.setTable(-3);
			}
			return 1;
		}
	};
	
	private static JavaFunction SetUsage = new JavaFunction() {
		public int invoke(LuaState l) {
			LuaJavaCommand self = (LuaJavaCommand) l.checkUserdata(1, LuaJavaCommand.class, "Command");
			self.setUsage(l.checkString(2));
			l.setTop(1); // Return ourself to chain aliases and stuff
			return 1;
		}
	};
	
	private static JavaFunction GetUsage = new JavaFunction() {
		public int invoke(LuaState l) {
			LuaJavaCommand self = (LuaJavaCommand) l.checkUserdata(1, LuaJavaCommand.class, "Command");
			l.pushString(self.getUsage(null));
			return 1;
		}
	};
	
	private static JavaFunction CanPlayerUseCommand = new JavaFunction() {
		public int invoke(LuaState l) {
			LuaJavaCommand self = (LuaJavaCommand) l.checkUserdata(1, LuaJavaCommand.class, "Command");
			ICommandSender player = (ICommandSender) l.checkUserdata(2, EntityPlayer.class, "Player");
			l.pushBoolean(player.canUseCommand(self.getRequiredPermissionLevel(), self.getName()));
			return 1;
		}
	};

	public static void Init(final LuaCraftState l) {		
		l.newMetatable("Command");
		{
			l.pushJavaFunction(__tostring);
			l.setField(-2, "__tostring");

			LuaUserdata.SetupBasicMeta(l);
			LuaUserdata.SetupMeta(l, false);

			l.newMetatable("Object");
			l.setField(-2, "__basemeta");

			l.pushJavaFunction(AddAlias);
			l.setField(-2, "AddAlias");
			l.pushJavaFunction(SetName);
			l.setField(-2, "SetName");
			l.pushJavaFunction(GetName);
			l.setField(-2, "GetName");
			l.pushJavaFunction(GetAliases);
			l.setField(-2, "GetAliases");
			l.pushJavaFunction(SetUsage);
			l.setField(-2, "SetUsage");
			l.pushJavaFunction(GetUsage);
			l.setField(-2, "GetUsage");
		}
	}
}

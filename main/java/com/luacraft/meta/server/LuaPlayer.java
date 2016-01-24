package com.luacraft.meta.server;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Calendar;
import java.util.Date;

import com.luacraft.LuaCraftState;
import com.luacraft.classes.Vector;
import com.mojang.authlib.GameProfile;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaType;

public class LuaPlayer {
	public static MinecraftServer server = null;

	/**
	 * @author Jake
	 * @function GetPing
	 * @info Return the players ping
	 * @arguments nil
	 * @return [[Number]]:ping
	 */

	public static JavaFunction GetPing = new JavaFunction() {
		public int invoke(LuaState l) {
			EntityPlayerMP self = (EntityPlayerMP) l.checkUserdata(1, EntityPlayerMP.class, "Player");
			l.pushInteger(self.ping);
			return 1;
		}
	};

	/**
	 * @author Jake
	 * @function GetIP
	 * @info Return the players ping
	 * @arguments nil
	 * @return [[Number]]:ping
	 */

	public static JavaFunction GetIP = new JavaFunction() {
		public int invoke(LuaState l) {
			EntityPlayerMP self = (EntityPlayerMP) l.checkUserdata(1, EntityPlayerMP.class, "Player");

			String ip = self.playerNetServerHandler.netManager.getRemoteAddress().toString();
			ip = ip.substring(ip.indexOf("/") + 1);
			ip = ip.substring(0, ip.indexOf(":"));
			l.pushString(ip);
			return 1;
		}
	};

	/**
	 * @author Gregor
	 * @function SetPos
	 * @info Sets the players position
	 * @arguments [[Vector]]:pos
	 * @return nil
	 */

	public static JavaFunction SetPos = new JavaFunction() {
		public int invoke(LuaState l) {
			EntityPlayerMP self = (EntityPlayerMP) l.checkUserdata(1, EntityPlayerMP.class, "Player");
			Vector pos = (Vector) l.checkUserdata(2, Vector.class, "Vector");
			self.playerNetServerHandler.setPlayerLocation(pos.x, pos.z, pos.y, self.rotationYaw, self.rotationPitch);
			return 0;
		}
	};

	/**
	 * @author Gregor
	 * @function Msg
	 * @info Print something to a players chat
	 * @arguments [[String]]:msg / [[Number]]:color See [[color]] for further information
	 * @return nil
	 */

	public static JavaFunction Msg = new JavaFunction() {
		public int invoke(LuaState l) {
			EntityPlayerMP self = (EntityPlayerMP) l.checkUserdata(1, EntityPlayerMP.class, "Player");

			StringBuilder message = new StringBuilder();

			for (int i = 2; i <= l.getTop(); i++) {
				if (l.isNoneOrNil(i))
					continue;

				if (l.type(i) == LuaType.NUMBER) {
					EnumChatFormatting format = EnumChatFormatting.values()[l.toInteger(i)];
					message.append(format);
				} else {
					l.getGlobal("tostring");
					l.pushValue(i);
					l.call(1, 1);
					message.append(l.toString(-1));
					l.pop(1);
				}
			}

			self.addChatMessage(new ChatComponentText(message.toString()));
			return 0;
		}
	};

	/**
	 * @author Jake
	 * @function Remove
	 * @info Remove the entity
	 * @arguments nil
	 * @return nil
	 */

	public static JavaFunction Remove = new JavaFunction() {
		public int invoke(LuaState l) {
			EntityPlayerMP self = (EntityPlayerMP) l.checkUserdata(1, EntityPlayerMP.class, "Player");
			self.playerNetServerHandler.kickPlayerFromServer("You were removed");
			return 0;
		}
	};

	/**
	 * @author Gregor
	 * @function IsOP
	 * @info Check if a player is op
	 * @arguments nil
	 * @return [[Boolean]]:op
	 */

	public static JavaFunction IsOP = new JavaFunction() {
		public int invoke(LuaState l) {
			EntityPlayerMP self = (EntityPlayerMP) l.checkUserdata(1, EntityPlayerMP.class, "Player");
			ServerConfigurationManager config = server.getConfigurationManager();
			l.pushBoolean(config.getOppedPlayers().getEntry(self.getGameProfile()) != null);
			return 1;
		}
	};

	/**
	 * @author Gregor
	 * @function SetOP
	 * @info Give op to a player
	 * @arguments [[Boolean]]:op
	 * @return nil
	 */

	public static JavaFunction SetOP = new JavaFunction() {
		public int invoke(LuaState l) {
			EntityPlayerMP self = (EntityPlayerMP) l.checkUserdata(1, EntityPlayerMP.class, "Player");
			boolean state = l.checkBoolean(2);

			ServerConfigurationManager config = server.getConfigurationManager();
			GameProfile user = self.getGameProfile();

			if (state)
				config.addOp(user);
			else
				config.removeOp(user);

			return 0;
		}
	};

	/**
	 * @author Gregor
	 * @function Kick
	 * @info Kick a player from the server
	 * @arguments [ [[String]]:reason ]
	 * @return nil
	 */

	public static JavaFunction Kick = new JavaFunction() {
		public int invoke(LuaState l) {
			EntityPlayerMP self = (EntityPlayerMP) l.checkUserdata(1, EntityPlayerMP.class, "Player");
			self.playerNetServerHandler.kickPlayerFromServer(l.checkString(2, "You have been kicked."));
			return 0;
		}
	};

	/**
	 * @author Gregor
	 * @function Ban
	 * @info Ban a player from the server
	 * @arguments [ [[String]]:reason, [[Number]]:minutes, [[String]]:banner ]
	 * @return nil
	 */

	public static JavaFunction Ban = new JavaFunction() {
		public int invoke(LuaState l) {
			EntityPlayerMP self = (EntityPlayerMP) l.checkUserdata(1, EntityPlayerMP.class, "Player");
			String reason = l.checkString(2, "You have been banned.");

			Date date = new Date();

			Calendar cal = null;
			
			if (l.isNumber(3)) {
				cal = Calendar.getInstance();
				cal.setTime(new Date());
				cal.add(Calendar.MINUTE, l.checkInteger(3, 0));
			}
			
			String banner = l.checkString(4, "LuaCraft");

			ServerConfigurationManager config = server.getConfigurationManager();

			UserListBansEntry banEntry = new UserListBansEntry(self.getGameProfile(), date, banner, cal != null ? cal.getTime() : null, reason);

			self.playerNetServerHandler.kickPlayerFromServer(reason);
			config.getBannedPlayers().addEntry(banEntry);
			return 0;
		}
	};

	public static void Init(final LuaCraftState l) {
		server = l.getMinecraftServer();

		l.newMetatable("Player");
		{
			l.pushJavaFunction(GetPing);
			l.setField(-2, "GetPing");
			l.pushJavaFunction(GetIP);
			l.setField(-2, "GetIP");
			l.pushJavaFunction(SetPos);
			l.setField(-2, "SetPos");
			l.pushJavaFunction(Msg);
			l.setField(-2, "Msg");
			l.pushJavaFunction(Remove);
			l.setField(-2, "Remove");
			l.pushJavaFunction(IsOP);
			l.setField(-2, "IsOP");
			l.pushJavaFunction(SetOP);
			l.setField(-2, "SetOP");
			l.pushJavaFunction(Kick);
			l.setField(-2, "Kick");
			l.pushJavaFunction(Ban);
			l.setField(-2, "Ban");
		}
		l.pop(1);
	}
}

package com.luacraft.meta.server;

import com.luacraft.LuaCraft;
import com.luacraft.LuaCraftState;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class LuaByteBuf {

	private static MinecraftServer server = null;

	/**
	 * @author Jake
	 * @function Send
	 * @info Sends the buffer to a player or a table of palyers
	 * @arguments [[Player]]:player or [[Table]]:players, ...
	 * @return nil
	 */

	private static JavaFunction Send = new JavaFunction() {
		public int invoke(LuaState l) {
			PacketBuffer self = (PacketBuffer) l.checkUserdata(1, PacketBuffer.class, "ByteBuf");

			for (int i = 2; i <= l.getTop(); i++) {
				if (l.isUserdata(i, EntityPlayerMP.class)) {
					EntityPlayerMP player = (EntityPlayerMP) l.checkUserdata(i, EntityPlayerMP.class, "Player");
					LuaCraft.channel.sendToAll(new FMLProxyPacket(self, LuaCraft.NET_CHANNEL));
				} else if (l.isTable(i)) {
					l.pushNil();
					while (l.next(i)) {
						if (l.isUserdata(-1, EntityPlayerMP.class)) {
							EntityPlayerMP player = (EntityPlayerMP) l.checkUserdata(-1, EntityPlayerMP.class,
									"Player");							
							LuaCraft.channel.sendTo(new FMLProxyPacket(self, LuaCraft.NET_CHANNEL), player);
						}
						l.pop(1);
					}
				}
			}
			return 0;
		}
	};

	/**
	 * @author Jake
	 * @function Broadcast
	 * @info Sends the buffer to all players or all players within a specific dimension
	 * @arguments [ [[Number]]:Dimension ]
	 * @return nil
	 */

	private static JavaFunction Broadcast = new JavaFunction() {
		public int invoke(LuaState l) {
			PacketBuffer self = (PacketBuffer) l.checkUserdata(1, PacketBuffer.class, "ByteBuf");
			SPacketCustomPayload packet = new SPacketCustomPayload(LuaCraft.NET_CHANNEL, self);

			if (l.isNumber(2))
				LuaCraft.channel.sendToDimension(new FMLProxyPacket(self, LuaCraft.NET_CHANNEL), l.toInteger(2));
			else
				LuaCraft.channel.sendToAll(new FMLProxyPacket(self, LuaCraft.NET_CHANNEL));

			return 0;
		}
	};

	public static void Init(final LuaCraftState l) {
		server = l.getServer();

		l.newMetatable("ByteBuf");
		{
			l.pushJavaFunction(Send);
			l.setField(-2, "Send");
			l.pushJavaFunction(Broadcast);
			l.setField(-2, "Broadcast");
		}
		l.pop(1);
	}
}

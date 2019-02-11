package com.luacraft.meta.client;

import com.luacraft.LuaCraft;
import com.luacraft.LuaCraftState;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class LuaByteBuf {

	private static final Minecraft client = LuaCraft.getClient();

	/**
	 * @author Jake
	 * @function SendToServer
	 * @info Sends the buffer to to the server
	 * @arguments nil
	 * @return nil
	 */

	private static JavaFunction SendToServer = new JavaFunction() {
		public int invoke(LuaState l) {
			if (client.player == null)
				return 0;

			PacketBuffer self = (PacketBuffer) l.checkUserdata(1, PacketBuffer.class, "ByteBuf");			
			LuaCraft.channel.sendToServer(new FMLProxyPacket(self, LuaCraft.NET_CHANNEL));
			return 0;
		}
	};

	public static void Init(final LuaCraftState l) {
		l.newMetatable("ByteBuf");
		{
			l.pushJavaFunction(SendToServer);
			l.setField(-2, "SendToServer");
			l.pushJavaFunction(SendToServer);
			l.setField(-2, "Send");
		}
		l.pop(1);
	}
}

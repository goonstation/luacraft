package com.luacraft.meta.client;

import com.luacraft.LuaCraft;
import com.luacraft.LuaCraftState;
import com.luacraft.classes.Vector;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class LuaEntity {
	private static final Minecraft client = LuaCraft.getClient();

	/**
	 * @author Gregor
	 * @function GetPos
	 * @info Returns the entity position
	 * @arguments nil
	 * @return [[Vector]]:pos
	 */

	private static JavaFunction GetPos = new JavaFunction() {
		public int invoke(LuaState l) {
			Entity self = (Entity) l.checkUserdata(1, Entity.class, "Entity");
			double posX = self.prevPosX + (self.posX - self.prevPosX) * client.timer.renderPartialTicks;
			double posY = self.prevPosY + (self.posY - self.prevPosY) * client.timer.renderPartialTicks;
			double posZ = self.prevPosZ + (self.posZ - self.prevPosZ) * client.timer.renderPartialTicks;
			Vector pos = new Vector(posX, posZ, posY);
			pos.push(l);
			return 1;
		}
	};

	/**
	 * @author Jake
	 * @function GetServerPos
	 * @info Returns the entity position on the server
	 * @arguments nil
	 * @return [[Vector]]:pos
	 */

	private static JavaFunction GetServerPos = new JavaFunction() {
		public int invoke(LuaState l) {
			Entity self = (Entity) l.checkUserdata(1, Entity.class, "Entity");
			Vector pos = new Vector(self.serverPosX, self.serverPosZ, self.serverPosY);
			pos.push(l);
			return 1;
		}
	};

	/**
	 * @author Gregor
	 * @function GetEyePos
	 * @info Returns the entity's eye position
	 * @arguments nil
	 * @return [[Vector]]:pos
	 */

	private static JavaFunction GetEyePos = new JavaFunction() {
		public int invoke(LuaState l) {
			Entity self = (Entity) l.checkUserdata(1, Entity.class, "Entity");
			double posX = self.prevPosX + (self.posX - self.prevPosX) * client.timer.renderPartialTicks;
			double posY = self.prevPosY + (self.posY - self.prevPosY) * client.timer.renderPartialTicks;
			double posZ = self.prevPosZ + (self.posZ - self.prevPosZ) * client.timer.renderPartialTicks;
			Vector pos = new Vector(posX, posZ, posY + self.getEyeHeight());
			pos.push(l);
			return 1;
		}
	};

	public static void Init(final LuaCraftState l) {
		l.newMetatable("Entity");
		{
			l.pushJavaFunction(GetPos);
			l.setField(-2, "GetPos");
			l.pushJavaFunction(GetServerPos);
			l.setField(-2, "GetServerPos");
			l.pushJavaFunction(GetEyePos);
			l.setField(-2, "GetEyePos");
		}
		l.pop(1);
	}
}

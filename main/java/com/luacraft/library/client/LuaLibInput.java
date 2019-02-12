package com.luacraft.library.client;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;

public class LuaLibInput {
	/**
	 * @author Jake
	 * @library input
	 * @function IsKeyDown
	 * @info Returns if the specified key is being pressed
	 * @arguments nil
	 * @return [[Boolean]]:isdown
	 */

	private static JavaFunction IsKeyDown = new JavaFunction() {
		public int invoke(LuaState l) {
			l.pushBoolean(Keyboard.isKeyDown(l.checkInteger(1)));
			return 1;
		}
	};

	/**
	 * @author Gregor
	 * @library input
	 * @function IsMouseDown
	 * @info Returns if the specified mouse button is being pressed
	 * @arguments nil
	 * @return [[Boolean]]:isdown
	 */

	private static JavaFunction IsMouseDown = new JavaFunction() {
		public int invoke(LuaState l) {
			l.pushBoolean(Mouse.isButtonDown(l.checkInteger(1)));
			return 1;
		}
	};

	public static void Init(final LuaState l) {
		l.newTable();
		{
			l.pushJavaFunction(IsKeyDown);
			l.setField(-2, "IsKeyDown");
			l.pushJavaFunction(IsMouseDown);
			l.setField(-2, "IsMouseDown");
		}
		l.setGlobal("input");
		
		for (int i = 0; i <= 16; i++) {
			String name = Mouse.getButtonName(i);
			if (name == null) continue;
			l.pushInteger(i);
			l.setGlobal(name);
		}
		
		for (int i = 0; i <= 223; i++) {
			String name = Keyboard.getKeyName(i);
			if (name == null) continue;
			l.pushInteger(i);
			l.setGlobal("KEY_" + name);
		}
	}
}

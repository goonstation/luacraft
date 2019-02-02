package com.luacraft.library;

import java.net.URL;

import com.luacraft.LuaCraft;
import com.luacraft.LuaCraftState;
import com.luacraft.classes.Color;
import com.luacraft.classes.LuaJavaHTTPRequest;
import com.luacraft.console.ConsoleManager;
import com.luacraft.console.ConsoleManager.MessageCallbacks;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaType;

import net.minecraft.util.text.TextFormatting;

public class LuaLibConsole {
	
	private static Color getTypeColor(LuaType type) {
		Color color = null;
		
		switch(type) {
		case BOOLEAN:
			color = new Color(236, 96, 98);
			break;
		case FUNCTION:
			color = new Color(102, 153, 204);
			break;
		case LIGHTUSERDATA:
			break;
		case NIL:
			color = new Color(195, 195, 195);
			break;
		case NUMBER:
			color = new Color(249, 174, 87);
			break;
		case STRING:
			color = new Color(153, 199, 148);
			break;
		case TABLE:
			break;
		case THREAD:
			break;
		case USERDATA:
			break;
		default:
			break;
		}
		
		return color;
	}

	public static String easyMsgC(LuaState l, int stackPos, Color defColor, boolean useTabs) {
		StringBuilder message = new StringBuilder();
		
		Color color = defColor;
		
		MessageCallbacks console = ConsoleManager.get(((LuaCraftState) l).getSide());

		for (int i = stackPos; i <= l.getTop(); i++) {
			if (l.isUserdata(i, Color.class)) {
				color = (Color) l.checkUserdata(i, Color.class, "Color");
			} else {
				Color objColor = getTypeColor(l.type(i));
				
				l.getGlobal("tostring");
				l.pushValue(i);
				l.call(1, 1);
				
				String text = l.checkString(-1);
				
				if (useTabs == true && i > stackPos) {
					text = "\t" + text;
				}
				
				message.append(text);
				
				console.msg((objColor != null ? objColor : color).toJavaColor(), text);
				l.pop(1);
			}
		}
		
		console.msg("\n");
		
		return message.toString();
	}
	
	/**
	 * @author Jake
	 * @function print
	 * @info Prints a message to the console
	 * @arguments [[Object]]:object, ...
	 * @return nil
	 */

	public static JavaFunction print = new JavaFunction() {
		public int invoke(LuaState l) {
			LuaCraft.getLogger().info(easyMsgC(l, 1, new Color(ConsoleManager.PRINT), true));
			return 0;
		}
	};
	
	/**
	 * @author Jake
	 * @library console
	 * @function print
	 * @info Prints a message to the console
	 * @arguments [[Color]]:color, [[String]]:string, ...
	 * @return nil
	 */

	public static JavaFunction consolePrint = new JavaFunction() {
		public int invoke(LuaState l) {
			LuaCraft.getLogger().info(easyMsgC(l, 1, new Color(ConsoleManager.PRINT), false));
			return 0;
		}
	};

	/**
	 * @author Jake
	 * @library console
	 * @function log
	 * @info Prints information to the console
	 * @arguments [[Color]]:color, [[String]]:string, ...
	 * @return nil
	 */

	public static JavaFunction consoleInfo = new JavaFunction() {
		public int invoke(LuaState l) {
			LuaCraft.getLogger().info(easyMsgC(l, 1, new Color(ConsoleManager.INFO), false));
			return 0;
		}
	};

	/**
	 * @author Jake
	 * @library console
	 * @function warn
	 * @info Prints a warning to the console
	 * @arguments [[Color]]:color, [[String]]:string, ...
	 * @return nil
	 */

	public static JavaFunction consoleWarn = new JavaFunction() {
		public int invoke(LuaState l) {
			LuaCraft.getLogger().warn(easyMsgC(l, 1, new Color(ConsoleManager.WARNING), false));
			return 0;
		}
	};

	/**
	 * @author Jake
	 * @library console
	 * @function log
	 * @info Prints an error to the console
	 * @arguments [[Color]]:color, [[String]]:string, ...
	 * @return nil
	 */

	public static JavaFunction consoleError = new JavaFunction() {
		public int invoke(LuaState l) {
			LuaCraft.getLogger().error(easyMsgC(l, 1, new Color(ConsoleManager.ERROR), false));
			return 0;
		}
	};

	public static void Init(final LuaCraftState l) {
		l.pushJavaFunction(print);
		l.setGlobal("print");
		
		l.newTable();
		{
			l.pushJavaFunction(consolePrint);
			l.setField(-2, "print");
			l.pushJavaFunction(consolePrint);
			l.setField(-2, "log");
			l.pushJavaFunction(consoleInfo);
			l.setField(-2, "info");
			l.pushJavaFunction(consoleWarn);
			l.setField(-2, "warn");
			l.pushJavaFunction(consoleError);
			l.setField(-2, "error");
		}
		l.setGlobal("console");
	}

}

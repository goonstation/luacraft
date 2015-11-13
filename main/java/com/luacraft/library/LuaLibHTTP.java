package com.luacraft.library;

import java.net.URL;

import com.luacraft.LuaCraftState;
import com.luacraft.classes.LuaJavaHTTPRequest;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;

public class LuaLibHTTP {

	public static JavaFunction Get = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			try {
				URL url = new URL(l.checkString(1));
				
				LuaJavaHTTPRequest request = new LuaJavaHTTPRequest(l, url, 2);
				request.connection.setRequestMethod("GET");
				request.start();
				
				if (l.isTable(3)) {
					l.pushNil();
					
					while(l.next(-2))
					{
						StringBuilder out = new StringBuilder();
						request.connection.setRequestProperty(l.toString(-2), l.toString(-1));
						l.pop(1);
					}
				}
				
				l.pushBoolean(true);
				return 1;
			} catch (Exception e) {
				l.pushBoolean(false);
				l.pushString(e.getMessage());
				return 2;
			}
		}
	};
	
	// TODO: Add http.Post

	public static void Init(final LuaCraftState l)
	{
		l.newTable();
		{
			l.pushJavaFunction(Get);
			l.setField(-2, "Get");
		}
		l.setGlobal("http");
	}

}

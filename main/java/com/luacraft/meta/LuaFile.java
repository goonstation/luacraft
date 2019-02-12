package com.luacraft.meta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

import com.luacraft.LuaCraftState;
import com.luacraft.LuaUserdata;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;

public class LuaFile {

	private static JavaFunction __tostring = new JavaFunction() {
		public int invoke(LuaState l) {
			File self = (File) l.checkUserdata(1, File.class, "File");
			l.pushString(String.format("File: 0x%08x", l.toPointer(1)));
			return 1;
		}
	};

	private static JavaFunction attributes = new JavaFunction() {
		public int invoke(LuaState l) {
			File self = (File) l.checkUserdata(1, File.class, "File");

			l.newTable();
			{
				try {
					BasicFileAttributes attrib = Files.readAttributes(self.toPath(), BasicFileAttributes.class);
					l.pushNumber(attrib.lastModifiedTime().to(TimeUnit.SECONDS));
					l.setField(-2, "modification");
					
					l.pushNumber(attrib.lastAccessTime().to(TimeUnit.SECONDS));
					l.setField(-2, "access");
					
					l.pushNumber(attrib.creationTime().to(TimeUnit.SECONDS));
					l.setField(-2, "creation");
					
					if (attrib.isSymbolicLink())
						l.pushString("link");
					if (self.isDirectory())
						l.pushString("directory");
					else if (self.isFile())
						l.pushString("file");
					l.setField(-2, "mode");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				try {
					l.pushNumber((Integer) Files.getAttribute(self.toPath(), "unix:dev"));
				} catch (IOException e) {
					l.pushNil();
				}
				l.setField(-2, "dev");
				
				try {
					l.pushNumber((Integer) Files.getAttribute(self.toPath(), "unix:ino"));
				} catch (IOException e) {
					l.pushNil();
				}
				l.setField(-2, "ino");
				
				try {
					l.pushNumber((Integer) Files.getAttribute(self.toPath(), "unix:uid"));
				} catch (IOException e) {
					l.pushNumber(0);
				}
				l.setField(-2, "uid");
				
				try {
					l.pushNumber((Integer) Files.getAttribute(self.toPath(), "unix:gid"));
				} catch (IOException e) {
					l.pushNumber(0);
				}
				l.setField(-2, "gid");
			}
			
			return 1;
		}
	};

	public static void Init(final LuaCraftState l) {
		l.newMetatable("File");
		{
			l.pushJavaFunction(__tostring);
			l.setField(-2, "__tostring");

			LuaUserdata.SetupBasicMeta(l);
			LuaUserdata.SetupMeta(l, false);

			l.newMetatable("Object");
			l.setField(-2, "__basemeta");

			l.pushJavaFunction(attributes);
			l.setField(-2, "attributes");
		}
	}
	
}

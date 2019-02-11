package com.luacraft.library;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import com.luacraft.LuaCraftState;
import com.luacraft.classes.FileMount;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;

public class LuaLibFile {

	/**
	 * @author Jake
	 * @library file
	 * @function Get
	 * @info Gets a file in either a mounted directory or in root directory
	 * @arguments nil
	 * @return [[String]]:file
	 */

	private static JavaFunction Get = new JavaFunction() {
		public int invoke(LuaState l) {
			try {
				l.pushUserdataWithMeta(FileMount.GetFile(l.checkString(1)), "File");
				return 1;
			} catch (FileNotFoundException e) {
				l.pushNil();
				l.pushString(e.getLocalizedMessage());
				return 2;
			}
		}
	};

	/**
	 * @author Jake
	 * @library file
	 * @function Search
	 * @info Gets a list of the files in both the root and then the mounted directories. Can now use wildcards when searching for files.
	 * @arguments nil
	 * @return [[Table]]:files
	 */

	private static JavaFunction Search = new JavaFunction() {
		public int invoke(LuaState l) {
			List<File> files = FileMount.GetFilesIn(l.checkString(1));
			l.newTable();
			for (File file : files) {
				l.pushNumber(l.tableSize(-1) + 1);
				l.pushString(file.toString());
				l.setTable(-3);
			}
			return 1;
		}
	};

	/**
	 * @author Jake
	 * @library file
	 * @function Mount
	 * @info Add a directory to the file mount path
	 * @arguments [[String]]:directory
	 * @return [[Boolean]]:success
	 */

	private static JavaFunction Mount = new JavaFunction() {
		public int invoke(LuaState l) {
			l.pushBoolean(FileMount.MountDirectory(l.checkString(1)));
			return 1;
		}
	};

	public static void Init(final LuaCraftState l) {
		l.newTable();
		{
			l.pushJavaFunction(Get);
			l.setField(-2, "Get");
			l.pushJavaFunction(Search);
			l.setField(-2, "Search");
			l.pushJavaFunction(Mount);
			l.setField(-2, "Mount");
		}
		l.setGlobal("file");
	}
}

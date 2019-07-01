package com.luacraft.meta;

import com.luacraft.LuaUserdata;
import com.luacraft.classes.Angle;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;

public class LuaAngle {

	private static JavaFunction __tostring = new JavaFunction() {
		public int invoke(LuaState l) {
			Angle self = (Angle) l.checkUserdata(1, Angle.class, "Angle");
			l.pushString(self.toString());
			return 1;
		}
	};

	private static JavaFunction __index = new JavaFunction() {
		public int invoke(LuaState l) {
			Angle self = (Angle) l.checkUserdata(1, Angle.class, "Angle");
			String key = l.checkString(2);

			if (key.equals("p"))
				l.pushNumber(self.p);
			else if (key.equals("y"))
				l.pushNumber(self.y);
			else if (key.equals("r"))
				l.pushNumber(self.r);
			else
				LuaUserdata.PushBaseMeta(l);

			return 1;
		}
	};

	private static JavaFunction __newindex = new JavaFunction() {
		public int invoke(LuaState l) {
			Angle self = (Angle) l.checkUserdata(1, Angle.class, "Angle");
			String key = l.checkString(2);
			double val = l.toNumber(3);

			if (key.equals("p"))
				self.p = val;
			else if (key.equals("y"))
				self.y = val;
			else if (key.equals("r"))
				self.r = val;

			return 0;
		}
	};

	private static JavaFunction iterator = new JavaFunction() {
		public int invoke(LuaState l) {
			Angle self = (Angle) l.checkUserdata(1, Angle.class, "Angle");
			
			String key = l.toString(2);
			double value = 0;
			
			if (key == null) {
				l.pushString("p");
				l.pushNumber(self.p);
				return 2;
			}
			
			switch (key) {
			case "p":
				l.pushString("y");
				l.pushNumber(self.y);
				return 2;
			case "y":
				l.pushString("r");
				l.pushNumber(self.r);
				return 2;
			default:
				return 0;
			}
		}
	};

	private static JavaFunction __pairs = new JavaFunction() {
		public int invoke(LuaState l) {
			Angle self = (Angle) l.checkUserdata(1, Angle.class, "Angle");
			l.pushJavaFunction(iterator);
			self.push(l);
			l.pushNil();
			return 3;
		}
	};

	private static JavaFunction __eq = new JavaFunction() {
		public int invoke(LuaState l) {
			Angle self = (Angle) l.checkUserdata(1, Angle.class, "Angle");
			Angle other = (Angle) l.checkUserdata(2, Angle.class, "Angle");
			l.pushBoolean(self.equals(other));
			return 1;
		}
	};

	private static JavaFunction __add = new JavaFunction() {
		public int invoke(LuaState l) {
			Angle self = (Angle) l.checkUserdata(1, Angle.class, "Angle");
			Angle other = (Angle) l.checkUserdata(2, Angle.class, "Angle");
			self.copy().add(other).push(l);
			return 1;
		}
	};

	private static JavaFunction __sub = new JavaFunction() {
		public int invoke(LuaState l) {
			Angle self = (Angle) l.checkUserdata(1, Angle.class, "Angle");
			Angle other = (Angle) l.checkUserdata(2, Angle.class, "Angle");
			self.copy().sub(other).push(l);
			return 1;
		}
	};

	private static JavaFunction __mul = new JavaFunction() {
		public int invoke(LuaState l) {
			Angle self = (Angle) l.checkUserdata(1, Angle.class, "Angle");
			Angle ret = self.copy();
			if (l.isUserdata(2, Angle.class)) {
				Angle other = (Angle) l.checkUserdata(2, Angle.class, "Angle");
				ret.mul(other);
			} else if (l.isNumber(2)) {
				double other = l.toNumber(2);
				ret.mul(other);
			}
			ret.push(l);
			return 1;
		}
	};

	private static JavaFunction __div = new JavaFunction() {
		public int invoke(LuaState l) {
			Angle self = (Angle) l.checkUserdata(1, Angle.class, "Angle");
			Angle ret = self.copy();
			if (l.isUserdata(2, Angle.class)) {
				Angle other = (Angle) l.checkUserdata(2, Angle.class, "Angle");
				ret.div(other).push(l);
			} else if (l.isNumber(2)) {
				double other = l.toNumber(2);
				ret.div(other).push(l);
			}
			ret.push(l);
			return 1;
		}
	};

	/**
	 * @author Jake
	 * @function Forward
	 * @info Get the forward vector of an angle
	 * @arguments nil
	 * @return [[Vector]]:forward
	 */

	private static JavaFunction Forward = new JavaFunction() {
		public int invoke(LuaState l) {
			Angle self = (Angle) l.checkUserdata(1, Angle.class, "Angle");
			self.forward().push(l);
			return 1;
		}
	};

	/**
	 * @author Jake
	 * @function Right
	 * @info Get the right vector of an angle
	 * @arguments nil
	 * @return [[Vector]]:right
	 */

	private static JavaFunction Right = new JavaFunction() {
		public int invoke(LuaState l) {
			Angle self = (Angle) l.checkUserdata(1, Angle.class, "Angle");
			self.right().push(l);
			return 1;
		}
	};

	/**
	 * @author Jake
	 * @function Up
	 * @info Get the upward vector of an angle
	 * @arguments nil
	 * @return [[Vector]]:up
	 */

	private static JavaFunction Up = new JavaFunction() {
		public int invoke(LuaState l) {
			Angle self = (Angle) l.checkUserdata(1, Angle.class, "Angle");
			self.up().push(l);
			return 1;
		}
	};

	public static void Init(final LuaState l) {
		l.newMetatable("Angle");
		{
			l.pushJavaFunction(__tostring);
			l.setField(-2, "__tostring");

			l.pushJavaFunction(__index);
			l.setField(-2, "__index");
			l.pushJavaFunction(__newindex);
			l.setField(-2, "__newindex");

			LuaUserdata.SetupBasicMeta(l);

			l.newMetatable("Object");
			l.setField(-2, "__basemeta");

			l.pushJavaFunction(__pairs);
			l.setField(-2, "__pairs");
			l.pushJavaFunction(__eq);
			l.setField(-2, "__eq");
			l.pushJavaFunction(__add);
			l.setField(-2, "__add");
			l.pushJavaFunction(__sub);
			l.setField(-2, "__sub");
			l.pushJavaFunction(__mul);
			l.setField(-2, "__mul");
			l.pushJavaFunction(__div);
			l.setField(-2, "__div");

			l.pushJavaFunction(Forward);
			l.setField(-2, "Forward");
			l.pushJavaFunction(Right);
			l.setField(-2, "Right");
			l.pushJavaFunction(Up);
			l.setField(-2, "Up");
		}
		l.pop(1);
	}
}
package com.luacraft.meta;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.FoodStats;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import com.luacraft.LuaCraftState;
import com.luacraft.LuaUserdataManager;
import com.luacraft.classes.Vector;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;

public class LuaPlayer
{
	public static JavaFunction __tostring = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			l.pushString(String.format("Player [%d][%s]", self.getEntityId(), self.getGameProfile().getName()));
			return 1;
		}
	};

	/**
	 * @author Gregor
	 * @function GetName
	 * Get a players name
	 * @arguments nil
	 * @return [[String]]:name
	 */

	public static JavaFunction GetName = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			l.pushString(self.getGameProfile().getName());
			return 1;
		}
	};

	/**
	 * @author Gregor
	 * @function GetScore
	 * Return the players score
	 * @arguments nil
	 * @return [[Number]]:score
	 */

	public static JavaFunction GetScore = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			l.pushInteger(self.getScore());
			return 1;
		}
	};

	/**
	 * @author Gregor
	 * @function SetHunger
	 * Set the players hunger (reaches from 0 to 20)
	 * @arguments [[Number]]:hunger
	 * @return nil
	 */

	public static JavaFunction SetHunger = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			ReflectionHelper.setPrivateValue(FoodStats.class, self.getFoodStats(), l.checkInteger(2), "foodLevel");
			return 0;
		}
	};

	/**
	 * @author Gregor
	 * @function GetHunger
	 * Returns the players hunger
	 * @arguments nil
	 * @return [[Number]]:hunger
	 */

	public static JavaFunction GetHunger = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			l.pushInteger(self.getFoodStats().getFoodLevel());
			return 1;
		}
	};

	/**
	 * @author Gregor
	 * @function GetArmor
	 * Returns the players armor value
	 * @arguments nil
	 * @return [[Number]]:armor
	 */

	public static JavaFunction GetArmor = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			l.pushInteger(self.inventory.getTotalArmorValue());
			return 1;
		}
	};

	/**
	 * @author Gregor
	 * @function GetInventory
	 * Returns a table containing [[Item]] / [[ItemStack]] objects
	 * @arguments nil
	 * @return [[Table]]:inv
	 */

	public static JavaFunction GetInventory = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");

			l.newTable();

			InventoryPlayer inventory = self.inventory;

			for (int i=0; i < inventory.getSizeInventory(); i++ )
			{
				ItemStack item = inventory.getStackInSlot(i);
				if (item != null)
				{
					l.pushInteger(i+1);
					l.pushUserdataWithMeta(item, "ItemStack");
					l.setTable(-3);
				}
			}

			return 1;
		}
	};

	/**
	 * @author Gregor
	 * @function GetEquipment
	 * Returns a table containing [[Item]] / [[ItemStack]] objects
	 * @arguments nil
	 * @return [[Table]]:equipment
	 */

	public static JavaFunction GetEquipment = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");

			l.newTable();

			ItemStack[] inventory = self.inventory.armorInventory;

			for (int i=0; i < inventory.length; i++ )
			{
				ItemStack item = inventory[i];
				if (item != null)
				{
					l.pushInteger(i+1);
					l.pushUserdataWithMeta(item, "ItemStack");
					l.setTable(-3);
				}
			}

			return 1;
		}
	};

	/**
	 * @author Gregor
	 * @function GetActiveSlot
	 * Returns an [[Item]] / [[ItemStack]] object
	 * @arguments nil
	 * @return [[ItemStack]]:item, [[Number]]:slot
	 */

	public static JavaFunction GetActiveSlot = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			l.pushUserdataWithMeta(self.getCurrentEquippedItem(), "ItemStack");
			return 1;
		}
	};

	/**
	 * @author Jake
	 * @function SetCreative
	 * Sets if the player is in creative mode or not
	 * @arguments [[Boolean]]:creative
	 * @return nil
	 */

	public static JavaFunction SetCreative = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			self.capabilities.isCreativeMode = l.checkBoolean(2);
			return 0;
		}
	};

	/**
	 * @author Jake
	 * @function IsCreative
	 * Returns whether or not the player is in creative
	 * @arguments nil
	 * @return [[Boolean]]:creative
	 */

	public static JavaFunction IsCreative = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			l.pushBoolean(self.capabilities.isCreativeMode);
			return 1;
		}
	};

	/**
	 * @author Gregor
	 * @function IsSleeping
	 * Check if the player is in a bed
	 * @arguments nil
	 * @return [[Boolean]]:inbed
	 */

	public static JavaFunction IsSleeping = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			l.pushBoolean(self.isPlayerSleeping());
			return 1;
		}
	};

	/**
	 * @author Gregor
	 * @function IsFullyAsleep
	 * Check if the player is fully asleep
	 * @arguments nil
	 * @return [[Boolean]]:asleep
	 */

	public static JavaFunction IsFullyAsleep = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			l.pushBoolean(self.isPlayerFullyAsleep());
			return 1;
		}
	};

	/**
	 * @author Jake
	 * @function GetExperience
	 * Returns the percentage of completion the player has for the current level
	 * @arguments nil
	 * @return [[Number]]:exp
	 */

	public static JavaFunction GetExperience = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			l.pushNumber(self.experience);
			return 1;
		}
	};

	/**
	 * @author Jake
	 * @function GetTotalExperience
	 * Returns a players total experience
	 * @arguments nil
	 * @return [[Number]]:totalexp
	 */

	public static JavaFunction GetTotalExperience = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			l.pushNumber(self.experienceTotal);
			return 1;
		}
	};

	/**
	 * @author Jake
	 * @function GetLevel
	 * Returns the players experience level
	 * @arguments nil
	 * @return [[Number]]:level
	 */

	public static JavaFunction GetLevel = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			l.pushNumber(self.experienceLevel);
			return 1;
		}
	};

	/**
	 * @author Jake
	 * @function SetLevel
	 * Sets the players experience level
	 * @arguments [[Number]]:level
	 * @return nil
	 */

	public static JavaFunction SetLevel = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			self.experienceLevel = l.checkInteger(2);
			return 0;
		}
	};

	/**
	 * @author Jake
	 * @function GetLevelCap
	 * Returns the max amount of experience needed for level up
	 * @arguments nil
	 * @return [[Number]]:lvlcap
	 */

	public static JavaFunction GetLevelCap = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			l.pushInteger(self.xpBarCap());
			return 1;
		}
	};

	/**
	 * @author Jake
	 * @function AddExperience
	 * Adds player experience to their current level
	 * @arguments [[Number]]:exp
	 * @return nil
	 */

	public static JavaFunction AddExperience = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			self.addExperience(l.checkInteger(2));
			return 0;
		}
	};

	/**
	 * @author Jake
	 * @function SubExperience
	 * Subtracts player experience from their current level
	 * @arguments [[Number]]:exp
	 * @return nil
	 */

	public static JavaFunction SubExperience = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			int diff = self.experienceTotal - l.checkInteger(2);
			self.experienceLevel = 0;
			self.experience = 0;
			self.experienceTotal = 0;
			self.addExperience(diff);
			return 0;
		}
	};

	/**
	 * @author Jake
	 * @function SetExperience
	 * Sets a players experience percentage for their current level
	 * @arguments [[Number]]:exp
	 * @return nil
	 */

	public static JavaFunction SetExperience = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			self.experience = l.checkInteger(2);
			return 0;
		}
	};

	/**
	 * @author Jake
	 * @function SetTotalExperience
	 * Sets a players total experience
	 * @arguments [[Number]]:totalexp
	 * @return nil
	 */

	public static JavaFunction SetTotalExperience = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			self.experienceTotal = l.checkInteger(2);
			return 0;
		}
	};

	/**
	 * @author Jake
	 * @function SetGod
	 * Set if the player should take damage or not
	 * @arguments [[Boolean]]:godmode
	 * @return nil
	 */

	public static JavaFunction SetGod = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			self.capabilities.disableDamage = l.checkBoolean(2);
			return 0;
		}
	};

	/**
	 * @author Jake
	 * @function IsGod
	 * Return if the player is in god mode or not
	 * @arguments [[Boolean]]:godmode
	 * @return nil
	 */

	public static JavaFunction IsGod = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			l.pushBoolean(self.capabilities.disableDamage);
			return 1;
		}
	};

	/**
	 * @author Jake
	 * @function GetSkinURL
	 * Return the players url to their minecraft skin
	 * @arguments nil
	 * @return [[String]]url
	 */

	public static JavaFunction GetSkinURL = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			l.pushString("http://s3.amazonaws.com/MinecraftSkins/" + self.getGameProfile().getName() + ".png");
			return 1;
		}
	};

	/**
	 * @author Jake
	 * @function GetContainer
	 * Get's the container object for the player
	 * @arguments nil
	 * @return [[Container]]:inv
	 */

	public static JavaFunction GetContainer = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			l.pushUserdataWithMeta(self.inventory, "Container");
			return 1;
		}
	};

	/**
	 * @author Jake
	 * @function SetBedPos
	 * Set a players bed position
	 * @arguments [[Vector]]:vec, [ [[Boolean]]:override, [[Number]]:dimensionID ]
	 * @return nil
	 */

	public static JavaFunction SetBedPos = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			Vector pos = (Vector) l.checkUserdata(2, Vector.class, "Vector");
			self.setSpawnChunk(new BlockPos((int) pos.x, (int) pos.z, (int) pos.y), l.checkBoolean(3, false), l.checkInteger(4, self.dimension));
			return 0;
		}
	};

	/**
	 * @author Jake
	 * @function GetBedPos
	 * Returns an open area around the players set bed position
	 * @arguments nil
	 * @return [[Vector]]:vec
	 */

	public static JavaFunction GetBedPos = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			BlockPos bed = self.getBedLocation(l.checkInteger(2, self.dimension));
			if (bed != null)
			{
				Vector pos = new Vector(bed.getX(), bed.getZ(), bed.getY());
				pos.push(l);
				return 1;
			}
			return 0;
		}
	};

	/**
	 * @author Jake
	 * @function SetFlying
	 * Set whether or not the player is flying
	 * @arguments [[Boolean]]:flying
	 * @return nil
	 */

	public static JavaFunction SetFlying = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			self.capabilities.isFlying = l.checkBoolean(2);
			return 0;
		}
	};

	/**
	 * @author Jake
	 * @function IsFlying
	 * Returns if the player is flying
	 * @arguments nil
	 * @return [[Boolean]]:flying
	 */

	public static JavaFunction IsFlying = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			l.pushBoolean(self.capabilities.isFlying);
			return 1;
		}
	};

	/**
	 * @author Jake
	 * @function SetFlightAllowed
	 * Set whether or not the player is allowed to fly
	 * @arguments [[Boolean]]:enabled
	 * @return nil
	 */

	public static JavaFunction SetFlightAllowed = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			self.capabilities.allowFlying = l.checkBoolean(2);
			return 0;
		}
	};

	/**
	 * @author Jake
	 * @function IsFlyingAllowed
	 * Returns if the player is allowed to fly
	 * @arguments nil
	 * @return [[Boolean]]:flying
	 */

	public static JavaFunction IsFlightAllowed = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			EntityPlayer self = (EntityPlayer) l.checkUserdata(1, EntityPlayer.class, "Player");
			l.pushBoolean(self.capabilities.allowFlying);
			return 1;
		}
	};

	public static void Init(final LuaCraftState l)
	{
		l.newMetatable("Player");
		{
			l.pushJavaFunction(__tostring);
			l.setField(-2, "__tostring");

			LuaUserdataManager.SetupMetaMethods(l);

			l.newMetatable("LivingBase");
			l.setField(-2, "__basemeta");

			l.pushJavaFunction(GetName);
			l.setField(-2, "GetName");
			l.pushJavaFunction(GetScore);
			l.setField(-2, "GetScore");
			l.pushJavaFunction(SetHunger);
			l.setField(-2, "SetHunger");
			l.pushJavaFunction(GetHunger);
			l.setField(-2, "GetHunger");
			l.pushJavaFunction(GetArmor);
			l.setField(-2, "GetArmor");
			l.pushJavaFunction(GetInventory);
			l.setField(-2, "GetInventory");
			l.pushJavaFunction(GetEquipment);
			l.setField(-2, "GetEquipment");
			l.pushJavaFunction(GetActiveSlot);
			l.setField(-2, "GetActiveSlot");
			l.pushJavaFunction(SetCreative);
			l.setField(-2, "SetCreative");
			l.pushJavaFunction(IsCreative);
			l.setField(-2, "IsCreative");
			l.pushJavaFunction(IsSleeping);
			l.setField(-2, "IsSleeping");
			l.pushJavaFunction(IsFullyAsleep);
			l.setField(-2, "IsFullyAsleep");
			l.pushJavaFunction(GetExperience);
			l.setField(-2, "GetExperience");
			l.pushJavaFunction(GetTotalExperience);
			l.setField(-2, "GetTotalExperience");
			l.pushJavaFunction(GetLevel);
			l.setField(-2, "GetLevel");
			l.pushJavaFunction(SetLevel);
			l.setField(-2, "SetLevel");
			l.pushJavaFunction(GetLevelCap);
			l.setField(-2, "GetLevelCap");
			l.pushJavaFunction(AddExperience);
			l.setField(-2, "AddExperience");
			l.pushJavaFunction(SubExperience);
			l.setField(-2, "SubExperience");
			l.pushJavaFunction(SetExperience);
			l.setField(-2, "SetExperience");
			l.pushJavaFunction(SetTotalExperience);
			l.setField(-2, "SetTotalExperience");
			l.pushJavaFunction(SetGod);
			l.setField(-2, "SetGod");
			l.pushJavaFunction(IsGod);
			l.setField(-2, "IsGod");
			l.pushJavaFunction(GetSkinURL);
			l.setField(-2, "GetSkinURL");
			l.pushJavaFunction(GetContainer);
			l.setField(-2, "GetContainer");
			l.pushJavaFunction(SetBedPos);
			l.setField(-2, "SetBedPos");
			l.pushJavaFunction(GetBedPos);
			l.setField(-2, "GetBedPos");
			l.pushJavaFunction(SetFlying);
			l.setField(-2, "SetFlying");
			l.pushJavaFunction(IsFlying);
			l.setField(-2, "IsFlying");
			l.pushJavaFunction(SetFlightAllowed);
			l.setField(-2, "SetFlightAllowed");
			l.pushJavaFunction(IsFlightAllowed);
			l.setField(-2, "IsFlightAllowed");
		}
		l.pop(1);
	}
}

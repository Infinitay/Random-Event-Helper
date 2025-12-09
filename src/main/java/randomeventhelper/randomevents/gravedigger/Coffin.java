package randomeventhelper.randomevents.gravedigger;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;

@Getter
@AllArgsConstructor
public enum Coffin
{
	CRAFTING(0, ItemID.MACRO_DIGGER_COFFIN_OBJECT_1, ItemID.POT_EMPTY, "Pot", SpriteID.Staticons.CRAFTING, "Craft", new Color(88, 58, 12)),
	MINING(1, ItemID.MACRO_DIGGER_COFFIN_OBJECT_2, ItemID.BRONZE_PICKAXE, "Pickaxe", SpriteID.Staticons.MINING, "Mine", Color.LIGHT_GRAY),
	COOKING(2, ItemID.MACRO_DIGGER_COFFIN_OBJECT_3, ItemID.CHEFS_HAT, "Cook", SpriteID.Staticons.COOKING, "Cook", Color.ORANGE),
	FARMING(3, ItemID.MACRO_DIGGER_COFFIN_OBJECT_4, ItemID.DIBBER, "Seed", SpriteID.Staticons2.FARMING, "Farm", Color.BLUE),
	WOODCUTTING(4, ItemID.MACRO_DIGGER_COFFIN_OBJECT_5, ItemID.BRONZE_AXE, "Axe", SpriteID.Staticons.WOODCUTTING, "Woodcut", Color.GREEN),
	EMPTY(5, -1, -1, "", -1, "", Color.BLACK); // No item ID for empty coffin

	private final int varbitValue; // Value for both MACRO_DIGGER_GRAVE and MACRO_DIGGER_COFFIN
	private final int itemID; // Item ID of the coffin item itself
	private final int associatedItemID; // An item ID associated with the coffin's contents
	private final String associatedItemName; // Name of the associated item
	private final int associatedSkillSpriteID; // Skill sprite ID associated with the coffin's contents
	private final String associatedSkillName; // Name of the associated skill
	private final Color color; // A color associated with the coffin and the skill it represents based on its contents

	private static final Map<Integer, Coffin> VARBIT_COFFIN_MAP;
	private static final Map<Integer, Coffin> ITEMID_COFFIN_MAP;

	static
	{
		VARBIT_COFFIN_MAP = Maps.uniqueIndex(ImmutableSet.copyOf(values()), Coffin::getVarbitValue);

		ITEMID_COFFIN_MAP = Maps.uniqueIndex(ImmutableSet.copyOf(values()), Coffin::getItemID);
	}

	public static Coffin getCoffinFromVarbitValue(int varbitValue)
	{
		return VARBIT_COFFIN_MAP.get(varbitValue);
	}

	public static Coffin getCoffinFromItemID(int itemID)
	{
		return ITEMID_COFFIN_MAP.get(itemID);
	}

	public BufferedImage getItemImage(ItemManager itemManager)
	{
		if (this.associatedItemID == -1)
		{
			return null;
		}
		return itemManager.getImage(this.associatedItemID);
	}

	public BufferedImage getSkillIconImage(SpriteManager spriteManager)
	{
		if (this.associatedSkillSpriteID == -1)
		{
			return null;
		}
		return spriteManager.getSprite(this.associatedSkillSpriteID, 0);
	}
}

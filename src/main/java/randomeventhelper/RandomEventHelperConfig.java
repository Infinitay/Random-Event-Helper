package randomeventhelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import randomeventhelper.randomevents.freakyforester.PheasantMode;

@ConfigGroup("randomeventhelper")
public interface RandomEventHelperConfig extends Config
{
	@ConfigItem(
		keyName = "isBeekeeperEnabled",
		name = "Beekeeper",
		description = "Helps highlight the correct order for the Beekeeper random event.",
		position = 0
	)
	default boolean isBeekeeperEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isCaptArnavChestEnabled",
		name = "Capt' Arnav's Chest",
		description = "Helps with aligning the chest slots to unlock Capt' Arnav's Chest random event.",
		position = 1
	)
	default boolean isCaptArnavChestEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isDrillDemonEnabled",
		name = "Drill Demon",
		description = "Helps highlight the correct exercise mat for the Drill Demon random event.",
		position = 2
	)
	default boolean isDrillDemonEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isFreakyForesterEnabled",
		name = "Freaky Forester",
		description = "Helps highlight the correct pheasant to kill for the Freaky Forester random event.",
		position = 3
	)
	default boolean isFreakyForesterEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isGravediggerEnabled",
		name = "Gravedigger",
		description = "Helps highlight where each coffin belongs to each grave for the Gravedigger random event.",
		position = 4
	)
	default boolean isGravediggerEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isMazeEnabled",
		name = "Maze",
		description = "Automatically sets path of Shortest Path plugin to the Strange Shrine in the Maze random event.",
		position = 5
	)
	default boolean isMazeEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "isMimeEnabled",
		name = "Mime",
		description = "Helps highlight the answers for the Mime random event.",
		position = 6
	)
	default boolean isMimeEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isPinballEnabled",
		name = "Pinball",
		description = "Helps highlight the correct pillars to touch for the Pinball random event.",
		position = 7
	)
	default boolean isPinballEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isSandwichLadyEnabled",
		name = "Sandwich Lady",
		description = "Helps highlight the correct food to take from the Sandwich Lady random event.",
		position = 8
	)
	default boolean isSandwichLadyEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isSurpriseExamEnabled",
		name = "Surprise Exam",
		description = "Helps highlight the answers for the Surprise Exam random event. Supports both matching and next item questions.",
		position = 9
	)
	default boolean isSurpriseExamEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isQuizMasterEnabled",
		name = "Quiz Master",
		description = "Helps highlight the correct odd item for the Quiz Master random event.",
		position = 10
	)
	default boolean isQuizMasterEnabled()
	{
		return true;
	}

	@ConfigSection(
		name = "Options",
		description = "Further configure various options",
		position = 11,
		closedByDefault = true
	)
	String SECTION_OPTIONS = "sectionOptions";

	@ConfigItem(
		keyName = "pheasantHighlightMode",
		name = "Pheasant Highlight Mode",
		description = "Configures how to highlight the pheasant(s) for the Freaky Forester random event.",
		section = SECTION_OPTIONS,
		position = 0
	)
	default PheasantMode pheasantHighlightMode()
	{
		return PheasantMode.SPECIFIC;
	}
}

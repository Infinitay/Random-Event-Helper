package randomeventhelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("randomeventhelper")
public interface RandomEventHelperConfig extends Config
{
	@ConfigItem(
		keyName = "isSurpriseExamEnabled",
		name = "Surprise Exam",
		description = "Helps highlight the answers for the Surprise Exam random event. Supports both matching and next item questions."
	)
	default boolean isSurpriseExamEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isBeekeeperEnabled",
		name = "Beekeeper",
		description = "Helps highlight the correct order for the Beekeeper random event."
	)
	default boolean isBeekeeperEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isFreakyForesterEnabled",
		name = "Freaky Forester",
		description = "Helps highlight the correct pheasant to kill for the Freaky Forester random event."
	)
	default boolean isFreakyForesterEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isPinballEnabled",
		name = "Pinball",
		description = "Helps highlight the correct pillars to touch for the Pinball random event."
	)
	default boolean isPinballEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isDrillDemonEnabled",
		name = "Drill Demon",
		description = "Helps highlight the correct exercise mat for the Drill Demon random event."
	)
	default boolean isDrillDemonEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isGravediggerEnabled",
		name = "Gravedigger",
		description = "Helps highlight where each coffin belongs to each grave for the Gravedigger random event."
	)
	default boolean isGravediggerEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isMimeEnabled",
		name = "Mime",
		description = "Helps highlight the answers for the Mime random event."
	)
	default boolean isMimeEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isMazeEnabled",
		name = "Maze",
		description = "Automatically sets path of Shortest Path plugin to the Strange Shrine in the Maze random event."
	)
	default boolean isMazeEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "isQuizMasterEnabled",
		name = "Quiz Master",
		description = "Helps highlight the correct odd item for the Quiz Master random event."
	)
	default boolean isQuizMasterEnabled()
	{
		return true;
	}
}

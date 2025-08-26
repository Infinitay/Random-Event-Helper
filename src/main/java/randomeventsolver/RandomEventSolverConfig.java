package randomeventsolver;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.eventbus.Subscribe;

@ConfigGroup("randomeventhelper")
public interface RandomEventSolverConfig extends Config
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
		description = "Helps highlight the answers for the Beekeeper random event."
	)
	default boolean isBeekeeperEnabled()
	{
		return true;
	}
}

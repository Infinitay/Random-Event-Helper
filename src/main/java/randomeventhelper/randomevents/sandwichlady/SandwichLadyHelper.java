package randomeventhelper.randomevents.sandwichlady;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.pluginmodulesystem.PluginModule;

@Slf4j
@Singleton
public class SandwichLadyHelper extends PluginModule
{
	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SandwichLadyOverlay sandwichLadyOverlay;

	@Getter
	private Widget trayFoodAnswerWidget;

	private final String SANDWICH_LADY_TRAY_REGEX = "Have a (?<foodName>[\\w\\s]+) for free!";
	private final Pattern SANDWICH_LADY_PATTERN = Pattern.compile(SANDWICH_LADY_TRAY_REGEX, Pattern.CASE_INSENSITIVE);

	@Inject
	public SandwichLadyHelper(OverlayManager overlayManager, RandomEventHelperConfig config, Client client)
	{
		super(overlayManager, config, client);
	}

	@Override
	public void onStartUp()
	{
		this.overlayManager.add(sandwichLadyOverlay);
		this.trayFoodAnswerWidget = null;
		WidgetLoaded temp = new WidgetLoaded();
		temp.setGroupId(InterfaceID.SANDWICH_TRAY);
		onWidgetLoaded(temp);
	}

	@Override
	public void onShutdown()
	{
		this.eventBus.unregister(this);
		this.overlayManager.remove(sandwichLadyOverlay);
		this.trayFoodAnswerWidget = null;
	}

	@Override
	public boolean isEnabled()
	{
		return this.config.isSandwichLadyEnabled();
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (widgetLoaded.getGroupId() == InterfaceID.SANDWICH_TRAY)
		{
			log.debug("Player opened sandwich tray widget");
			this.clientThread.invokeLater(() -> {
				Widget refreshmentWidget = this.client.getWidget(InterfaceID.SandwichTray.SANDWHICH_REFRESHMENT_LAYER);
				if (refreshmentWidget != null && !refreshmentWidget.isHidden())
				{
					// First fetch the food the Sandwich Lady is offering us
					Widget trayLabelWidget = this.client.getWidget(InterfaceID.SandwichTray.SANDWICH_TRAY_LABEL);
					if (trayLabelWidget != null && !trayLabelWidget.isHidden())
					{
						String trayLabelText = Text.sanitizeMultilineText(trayLabelWidget.getText());
						Matcher sandwichTrayMatcher = SANDWICH_LADY_PATTERN.matcher(trayLabelText);
						if (!sandwichTrayMatcher.find())
						{
							log.debug("Couldn't match the given string \"{}\"", trayLabelText);
							return;
						}
						String extractedFood = sandwichTrayMatcher.group("foodName");
						if (extractedFood != null)
						{
							SandwichTrayFood requestedTrayFood = SandwichTrayFood.fromName(extractedFood);
							if (requestedTrayFood != null)
							{
								Widget[] foodOptionWidgets = refreshmentWidget.getStaticChildren();
								if (foodOptionWidgets != null)
								{
									for (Widget foodOptionWidget : foodOptionWidgets)
									{
										if (foodOptionWidget != null && !foodOptionWidget.isHidden() && foodOptionWidget.getModelId() == requestedTrayFood.getModelID())
										{
											log.debug("Found matching tray food option widget for {}", requestedTrayFood);
											this.trayFoodAnswerWidget = foodOptionWidget;
										}
										else
										{
											log.debug("Could not find matching tray food option widget for {}", requestedTrayFood);
										}
									}
								}
								else
								{
									log.debug("Sandwich tray food option widgets was null - Could not fetch refreshment widget children");
								}
							}
							else
							{
								log.debug("Didn't find SandwichTrayFood from extracted food name - {} (Extracted Food Name: {})", trayLabelText, extractedFood);
							}
						}
						else
						{
							log.debug("Could not extract the food name from the sandwich tray label text - '{}'", trayLabelText);
						}
					}
				}
			});
		}

	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed)
	{
		if (widgetClosed.getGroupId() == InterfaceID.SANDWICH_TRAY)
		{
			log.debug("Player closed sandwich tray widget");
			this.trayFoodAnswerWidget = null;
		}
	}
}

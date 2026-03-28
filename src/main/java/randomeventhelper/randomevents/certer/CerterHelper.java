package randomeventhelper.randomevents.certer;

import com.google.common.collect.Maps;
import java.util.Map;
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
public class CerterHelper extends PluginModule
{
	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private CerterOverlay certerOverlay;

	@Getter
	private Widget certerAnswerWidget;

	private final int[] CERTER_OPTION_WIDGETS = {
		InterfaceID.MacroCerter.MACRO_CERTER_A,
		InterfaceID.MacroCerter.MACRO_CERTER_B,
		InterfaceID.MacroCerter.MACRO_CERTER_C,
	};

	private final int[] CERTER_OPTION_TEXT_WIDGETS = {
		InterfaceID.MacroCerter.MACRO_CERTER_TEXTA,
		InterfaceID.MacroCerter.MACRO_CERTER_TEXTB,
		InterfaceID.MacroCerter.MACRO_CERTER_TEXTC,
	};

	@Inject
	public CerterHelper(OverlayManager overlayManager, RandomEventHelperConfig config, Client client)
	{
		super(overlayManager, config, client);
	}

	@Override
	public void onStartUp()
	{
		this.overlayManager.add(certerOverlay);
		this.certerAnswerWidget = null;

		if (this.isLoggedIn())
		{
			this.clientThread.invokeLater(() -> {
				if (this.client.getWidget(InterfaceID.MacroCerter.MACRO_CERTER_ITEM) != null)
				{
					WidgetLoaded certerItemWidgetLoaded = new WidgetLoaded();
					certerItemWidgetLoaded.setGroupId(InterfaceID.MACRO_CERTER);
					this.eventBus.post(certerItemWidgetLoaded);
				}
			});
		}
	}

	@Override
	public void onShutdown()
	{
		this.eventBus.unregister(this);
		this.overlayManager.remove(certerOverlay);
		this.certerAnswerWidget = null;
	}

	@Override
	public boolean isEnabled()
	{
		return this.config.isCerterEnabled();
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (widgetLoaded.getGroupId() == InterfaceID.MACRO_CERTER)
		{
			log.debug("Player has opened the Certer random event widget");
			this.clientThread.invokeLater(() -> {
				CerterEventItem certerEventItem = null;
				Widget certerItemWidget = this.client.getWidget(InterfaceID.MacroCerter.MACRO_CERTER_ITEM);
				if (certerItemWidget != null && !certerItemWidget.isHidden())
				{
					certerEventItem = CerterEventItem.fromModelID(certerItemWidget.getModelId());
					if (certerEventItem != null)
					{
						log.debug("Determined the required item for the Certer: {}", certerEventItem);
						// We'll handle fetching and setting the answer widget later
					}
					else
					{
						log.debug("Couldn't the item model ID {} to any CerterEventItem", certerItemWidget.getModelId());
						return;
					}
				}

				for (int i = 0; i < CERTER_OPTION_TEXT_WIDGETS.length; i++)
				{
					Widget optionWidget = this.client.getWidget(CERTER_OPTION_TEXT_WIDGETS[i]);
					if (optionWidget != null && !optionWidget.isHidden())
					{
						String optionText = Text.sanitizeMultilineText(optionWidget.getText());
						char optionLetter = (char) ('A' + i);
						log.debug("Certer option widget '{}' with text: \"{}\"", optionLetter, optionText);
						if (certerEventItem != null && certerEventItem.getOptionText().equals(optionText))
						{
							this.certerAnswerWidget = this.client.getWidget(this.CERTER_OPTION_WIDGETS[i]);
							log.debug("Found answer {}. \"{}\" given the CerterEventItem: {}", optionLetter, optionText, certerEventItem);
							return;
						}
					}
				}
			});
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed)
	{
		if (widgetClosed.getGroupId() == InterfaceID.MACRO_CERTER)
		{
			log.debug("Player has closed the Certer random event widget, clearing last known answer");
			this.certerAnswerWidget = null;
		}
	}
}

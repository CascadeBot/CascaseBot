/*
 * Copyright (c) 2019 CascadeBot. All rights reserved.
 * Licensed under the MIT license.
 */

package org.cascadebot.cascadebot.events;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Component;
import org.cascadebot.cascadebot.CascadeBot;
import org.cascadebot.cascadebot.metrics.Metrics;
import org.cascadebot.cascadebot.utils.interactions.CascadeActionRow;
import org.cascadebot.cascadebot.utils.interactions.CascadeButton;
import org.cascadebot.cascadebot.utils.interactions.CascadeComponent;
import org.cascadebot.cascadebot.utils.interactions.CascadeSelectBox;
import org.cascadebot.cascadebot.utils.interactions.ComponentContainer;
import org.cascadebot.cascadebot.utils.interactions.InteractionMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ButtonEventListener extends ListenerAdapter {

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        ComponentContainer container = getContainerFromEvent(event);
        if (container == null) {
            return;
        }

        CascadeButton button = container.getComponents().stream()
                .flatMap((row) -> row.getComponents().stream())
                .filter(CascadeButton.class::isInstance)
                .map(CascadeButton.class::cast)
                .filter(buttonToCheck -> buttonToCheck.getId().equals(event.getComponentId()))
                .findFirst()
                .orElse(null);

        if (button == null) {
            // this should not be possible because as long as the container is in the cache, it will be able to find the button as the button is on the message that the container applies to.
            CascadeBot.LOGGER.error("Button was null when it should not be able to be null! Something is broken! Maybe race condition?");
            return;
        }
        
        event.deferEdit().queue(interactionHook -> {
            button.getConsumer().invoke(event.getMember(), null /* TODO: Owner? */ , event.getTextChannel(), new InteractionMessage(event.getMessage(), container));
            Metrics.INS.buttonsPressed.labels(button.getId(), "button").inc();
        });
    }

    @Override
    public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
        ComponentContainer container = getContainerFromEvent(event);
        if (container == null) {
            return;
        }

        CascadeSelectBox select = null;
        for (CascadeActionRow actionRow : container.getComponents()) {
            if (Objects.equals(actionRow.getComponentType(), Component.Type.SELECTION_MENU)) {
                for (CascadeComponent selectToCheck : actionRow.getComponents()) {
                    if (selectToCheck.getId().equals(event.getComponentId())) {
                        select = (CascadeSelectBox) selectToCheck;
                        break;
                    }
                }
                if (select != null) {
                    break;
                }
            }
        }
        if (select == null) {
            // TODO log as this should not be possible
            return;
        }

        CascadeSelectBox finalSelect = select;
        event.deferEdit().queue(interactionHook -> {
            finalSelect.getConsumer().run(event.getMember(), event.getTextChannel(), new InteractionMessage(event.getMessage(), container), event.getValues());
            Metrics.INS.buttonsPressed.labels(finalSelect.getId(), "select").inc();
        });
    }

    private ComponentContainer getContainerFromEvent(GenericComponentInteractionCreateEvent event) {
        if (event.getChannel().getType().equals(ChannelType.TEXT)) {
            TextChannel channel = (TextChannel) event.getChannel();
            Message message = channel.retrieveMessageById(event.getMessageIdLong()).complete();
            return ComponentContainer.Companion.fromDiscordObjects(message.getActionRows());
        }
        return null;
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent e) {
        if (e.getChannel().getType().equals(ChannelType.TEXT)) {
            TextChannel channel = (TextChannel) e.getChannel();
            /*GuildData data = GuildDataManager.getGuildData(channel.getGuild().getIdLong());
            InteractionCache cache = data.getComponentCache();
            if (cache.containsKey(channel.getIdLong())) {
                cache.get(channel.getIdLong()).remove(e.getMessageIdLong());
            }*/
        }
    }

}

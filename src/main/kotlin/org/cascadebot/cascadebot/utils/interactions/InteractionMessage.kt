package org.cascadebot.cascadebot.utils.interactions

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.api.requests.restaction.MessageAction

class InteractionMessage(val message: Message, val container: ComponentContainer) {

    /**
     * Message ID
     */
    val idLong: Long = message.idLong

    fun editMessage(content: String): MessageAction {
        return message.editMessage(content).override(true).setActionRows(container.getComponents().map { it.toDiscordActionRow() })
    }

    fun editMessage(embed: MessageEmbed): MessageAction {
        return message.editMessageEmbeds(embed).override(true).setActionRows(container.getComponents().map { it.toDiscordActionRow() })
    }

    fun editMessage(message: Message): MessageAction {
        return message.editMessage(message).override(true).setActionRows(container.getComponents().map { it.toDiscordActionRow() })
    }

    fun notifyContainerChange(): MessageAction {
        return message.editMessageComponents().setActionRows(container.getComponents().map { it.toDiscordActionRow() })
    }

    fun deleteMessage(): AuditableRestAction<Void> {
        return message.delete()
    }

}
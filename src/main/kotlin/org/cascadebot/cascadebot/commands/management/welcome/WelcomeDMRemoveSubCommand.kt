/*
 * Copyright (c) 2020 CascadeBot. All rights reserved.
 * Licensed under the MIT license.
 */

package org.cascadebot.cascadebot.commands.management.welcome

import net.dv8tion.jda.api.entities.Member
import org.cascadebot.cascadebot.commandmeta.CommandContext
import org.cascadebot.cascadebot.commandmeta.SubCommand
import org.cascadebot.cascadebot.data.entities.GuildGreetingEntity
import org.cascadebot.cascadebot.data.entities.GuildGreetingId
import org.cascadebot.cascadebot.data.objects.GreetingType
import org.cascadebot.cascadebot.messaging.MessageType
import org.cascadebot.cascadebot.messaging.embed
import org.cascadebot.cascadebot.utils.listOf

class WelcomeDMRemoveSubCommand : SubCommand() {

    override fun onCommand(sender: Member, context: CommandContext) {
        if (context.args.size != 1) {
            context.uiMessaging.replyUsage()
            return
        }

        if (!context.isArgInteger(0)) {
            context.typedMessaging.replyDanger(context.i18n("commands.welcomedm.remove.message_index_number"))
            return
        }

        val index = context.getArgAsInteger(0)!! - 1
        val welcomedmMessages = context.transaction {
            return@transaction listOf(
                GuildGreetingEntity::class.java,
                mapOf(Pair("guild_id", context.getGuildId()), Pair("type", GreetingType.WELCOME_DM))
            )
        } ?: throw UnsupportedOperationException("This shouldn't happen")
        if (index < 0 || index >= welcomedmMessages.size) {
            context.typedMessaging.replyDanger(context.i18n("commands.welcomedm.invalid_message_index", welcomedmMessages.size))
            return
        }

        val message = welcomedmMessages.removeAt(index)
        context.deleteDataObject(GuildGreetingEntity::class.java, GuildGreetingId(message.id, message.guildId))

        context.typedMessaging.replySuccess(embed(MessageType.INFO, context.user) {
            title {
                name = context.i18n("commands.welcomedm.remove.remove_success_title")
            }
            description = context.i18n("commands.welcomedm.remove.remove_success_text", message.content)
        })
    }

    override fun command(): String = "remove"

    override fun parent(): String = "welcomedm"

}
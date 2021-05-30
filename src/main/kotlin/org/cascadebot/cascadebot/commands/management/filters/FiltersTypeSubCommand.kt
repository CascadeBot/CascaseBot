/*
 * Copyright (c) 2020 CascadeBot. All rights reserved.
 * Licensed under the MIT license.
 */

package org.cascadebot.cascadebot.commands.management.filters

import net.dv8tion.jda.api.entities.Member
import org.cascadebot.cascadebot.commandmeta.CommandContext
import org.cascadebot.cascadebot.commandmeta.SubCommand
import org.cascadebot.cascadebot.data.objects.CommandFilter
import org.cascadebot.cascadebot.data.objects.ModlogEventData
import org.cascadebot.cascadebot.moderation.ModlogEmbedField
import org.cascadebot.cascadebot.moderation.ModlogEmbedPart
import org.cascadebot.cascadebot.moderation.ModlogEvent
import org.cascadebot.cascadebot.permissions.CascadePermission
import org.cascadebot.cascadebot.utils.FormatUtils
import org.cascadebot.cascadebot.utils.language.LanguageUtils
import org.cascadebot.cascadebot.utils.toCapitalized
import org.cascadebot.cascadebot.utils.toTitleCase

class FiltersTypeSubCommand : SubCommand() {

    override fun onCommand(sender: Member, context: CommandContext) {
        if (context.args.isEmpty()) {
            context.uiMessaging.replyUsage()
            return
        }

        val filterName = context.getArg(0)
        val filter = context.data.management.filters.find { it.name == filterName }

        if (filter == null) {
            context.typedMessaging.replyDanger(context.i18n("commands.filters.doesnt_exist", filterName))
            return
        }

        val type: CommandFilter.FilterType? = LanguageUtils.findEnumByI18n(CommandFilter.FilterType::class.java, context.locale, context.getArg(1), true)

        if (type == null) {
            val validTypes = CommandFilter.FilterType.values().joinToString(", ") { "`${it.name.toTitleCase()}`" }
            context.typedMessaging.replyDanger(context.i18n("commands.filters.type.invalid_type", validTypes))
            return
        }

        if (type == filter.type) {
            context.typedMessaging.replyInfo(context.i18n("commands.filters.type.already_set", FormatUtils.formatEnum(type, context.locale), filterName))
            return
        }

        val oldType = filter.type
        filter.type = type
        context.typedMessaging.replySuccess(context.i18n("commands.filters.type.success", FormatUtils.formatEnum(type, context.locale), filterName))

        val embedFields = mutableListOf<ModlogEmbedPart>()

        embedFields.add(
            ModlogEmbedField(
                false,
                "words.type",
                "modlog.general.small_change",
                FormatUtils.formatEnum(oldType, context.locale).toCapitalized(),
                FormatUtils.formatEnum(type, context.locale).toCapitalized()
            )
        )

        val eventStore = ModlogEventData(ModlogEvent.CASCADE_FILTER_UPDATE, context.user, filter, embedFields)
        context.data.moderation.sendModlogEvent(context.guild.idLong, eventStore)
    }

    override fun command(): String  = "type"

    override fun parent(): String = "filters"

    override fun permission(): CascadePermission = CascadePermission.of("filters.type", true)

}
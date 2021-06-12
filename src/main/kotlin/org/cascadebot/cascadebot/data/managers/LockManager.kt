package org.cascadebot.cascadebot.data.managers


import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.IPermissionHolder
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction
import java.time.OffsetDateTime
import java.util.EnumSet

enum class Status {
    ALLOW,
    DENY,
    NEUTRAL;

    fun apply(action: PermissionOverrideAction, perm: EnumSet<Permission>) {
        when (this) {
            ALLOW -> action.grant(perm)
            DENY -> action.deny(perm)
            NEUTRAL -> action.clear(perm)
        }
    }

}

data class LockPermissionState(val target: Status, val selfMember: Status, val createdAt: OffsetDateTime = OffsetDateTime.now())

object LockManager {

    fun getPerm(channel: TextChannel, target: IPermissionHolder): LockPermissionState {
        var perm = LockPermissionState(Status.NEUTRAL, Status.NEUTRAL)

        val targetOverride = channel.getPermissionOverride(target)
        if (targetOverride != null) {
            if (targetOverride.allowed.contains(Permission.MESSAGE_WRITE)) perm = perm.copy(target = Status.ALLOW)
            if (targetOverride.denied.contains(Permission.MESSAGE_WRITE)) perm = perm.copy(target = Status.DENY)
        }

        val selfMemberOverride = channel.getPermissionOverride(channel.guild.selfMember)
        if (selfMemberOverride != null) {
            if (selfMemberOverride.allowed.contains(Permission.MESSAGE_WRITE)) perm = perm.copy(selfMember = Status.ALLOW)
            if (selfMemberOverride.denied.contains(Permission.MESSAGE_WRITE)) perm = perm.copy(selfMember = Status.DENY)
        }
        return perm
    }

    private fun storePermissions(channel: TextChannel, target: IPermissionHolder) {
        val lockedChannels = GuildDataManager.getGuildData(channel.guild.idLong).lockedChannels
        val mutableMap = lockedChannels[channel.id]
        if (mutableMap == null) {
            lockedChannels[channel.id] = mutableMapOf(Pair(target.id, getPerm(channel, target)))
        } else {
            mutableMap[target.id] = getPerm(channel, target)
        }
    }

    fun lock(channel: TextChannel, target: IPermissionHolder) {
        storePermissions(channel, target)
        channel.upsertPermissionOverride(channel.guild.selfMember).grant(Permission.MESSAGE_WRITE).queue()
        channel.upsertPermissionOverride(target).deny(Permission.MESSAGE_WRITE).queue()
    }

    fun unlock(guild: Guild, channel: TextChannel, target: IPermissionHolder) : Boolean {
        val state = GuildDataManager.getGuildData(channel.guild.idLong).lockedChannels[channel.id]?.get(target.id)
                // If there is no state to restore, we can't do anything!
                ?: return false
        val perm = EnumSet.of(Permission.MESSAGE_WRITE)

        var changed = false;

        val selfPermissionAction = channel.getPermissionOverride(guild.selfMember)?.manager
        if (selfPermissionAction != null) {
            state.selfMember.apply(selfPermissionAction, perm)
            selfPermissionAction.queue { if (it.allowedRaw == 0L && it.deniedRaw == 0L) it.delete().queue() }
            changed = true;
        }

        val targetPermissionAction = channel.getPermissionOverride(target)?.manager
        if (targetPermissionAction != null) {
            state.target.apply(targetPermissionAction, perm)
            targetPermissionAction.queue {
                GuildDataManager.getGuildData(guild.idLong).lockedChannels[channel.idLong.toString()]?.remove(target.idLong.toString())
                if (it.allowedRaw == 0L && it.deniedRaw == 0L) it.delete().queue()
            }
            changed = true
        }
        return changed
    }

}

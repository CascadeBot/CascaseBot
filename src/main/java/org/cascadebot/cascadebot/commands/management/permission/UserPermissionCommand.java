/*
 * Copyright (c) 2019 CascadeBot. All rights reserved.
 * Licensed under the MIT license.
 */

package org.cascadebot.cascadebot.commands.management.permission;

import java.util.Set;
import net.dv8tion.jda.core.entities.Member;
import org.cascadebot.cascadebot.commandmeta.CommandContext;
import org.cascadebot.cascadebot.commandmeta.ICommandExecutable;
import org.cascadebot.cascadebot.commandmeta.ICommandMain;
import org.cascadebot.cascadebot.commandmeta.Module;
import org.cascadebot.cascadebot.permissions.CascadePermission;

public class UserPermissionCommand implements ICommandMain {

    @Override
    public void onCommand(Member sender, CommandContext context) {
        context.getUIMessaging().replyUsage(this);
    }

    @Override
    public String command() {
        return "userperms";
    }

    @Override
    public Set<String> getGlobalAliases() {
        return Set.of("userpermissions", "user_permissions");
    }

    @Override
    public CascadePermission getPermission() {
        return CascadePermission.of("User permissions command", "permissions.user", false, Module.MANAGEMENT);
    }

    @Override
    public Set<ICommandExecutable> getSubCommands() {
        return Set.of(new UserPermissionAddSubCommand(), new UserPermissionRemoveSubCommand(), new UserPermissionGroupSubCommand(),
                new UserPermissionListSubCommand(), new UserPermissionTestSubCommand());
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public Module getModule() {
        return Module.MANAGEMENT;
    }

}

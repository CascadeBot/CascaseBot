/*
 * Copyright (c) 2019 CascadeBot. All rights reserved.
 * Licensed under the MIT license.
 */

package com.cascadebot.cascadebot.permissions;

import com.cascadebot.cascadebot.commandmeta.Module;

import java.util.Arrays;
import java.util.EnumSet;

public class Permission {

    public static final Permission ALL_PERMISSIONS = Permission.of("All permissions", "*");

    private final String label;
    private final String permission;
    private final boolean defaultPerm;
    private final EnumSet<net.dv8tion.jda.core.Permission> discordPerm;
    private final Module module;


    private Permission(String label, String permission, boolean defaultPerm, Module module, net.dv8tion.jda.core.Permission... discordPerm) {
        this.label = label;
        this.permission = "cascade." + permission;
        this.defaultPerm = defaultPerm;
        this.module = module;
        this.discordPerm = EnumSet.noneOf(net.dv8tion.jda.core.Permission.class);
        this.discordPerm.addAll(Arrays.asList(discordPerm));
    }

    public static Permission of(String permission) {
        return new Permission(null, permission, false, null);
    }

    public static Permission of(String label, String permission) {
        return new Permission(label, permission, false, null);
    }

    public static Permission of(String label, String permission, boolean defaultPerm) {
        return new Permission(label, permission, defaultPerm, null);
    }

    public static Permission of(String label, String permission, Module module) {
        return new Permission(label, permission, false, module);
    }

    public static Permission of(String label, String permission, boolean defaultPerm, Module module) {
        return new Permission(label, permission, defaultPerm, module);
    }

    public static Permission of(String label, String permission, boolean defaultPerm, net.dv8tion.jda.core.Permission... discordPerm) {
        return new Permission(label, permission, defaultPerm, null, discordPerm);
    }


    public String getPermissionNode() {
        return permission;
    }

    public boolean isDefaultPerm() {
        return defaultPerm;
    }

    public Module getModule() {
        return module;
    }

    public EnumSet<net.dv8tion.jda.core.Permission> getDiscordPerm() {
        return discordPerm;
    }

    @Override
    public String toString() {
        return getPermissionNode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Permission)) return false;
        Permission otherPerm = (Permission) obj;
        return this.label.equals(otherPerm.label) &&
                this.defaultPerm == otherPerm.defaultPerm &&
                this.module == otherPerm.module &&
                this.permission.equals(otherPerm.permission) &&
                this.discordPerm.equals(otherPerm.discordPerm);
    }

}

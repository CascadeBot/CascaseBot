package com.cascadebot.cascadebot;

import java.util.regex.Pattern;

public class Constants {

    public static final Pattern INTEGER_REGEX = Pattern.compile("-?[0-9]+");
    public static final Pattern DECIMAL_REGEX = Pattern.compile("-?[0-9]*([.,])[0-9]+");

}

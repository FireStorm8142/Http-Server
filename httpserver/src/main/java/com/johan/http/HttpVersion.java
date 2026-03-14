package com.johan.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum HttpVersion {
    HTTP_1_1("HTTP/1.1", 1, 1);

    public final String Literal;
    public final int Major;
    public final int Minor;
    HttpVersion(String Literal, int Major, int Minor){
        this.Literal=Literal;
        this.Major=Major;
        this.Minor=Minor;
    }

    private static final Pattern httpVersionRegexPattern = Pattern.compile("^HTTP/(?<Major>\\d+).(?<Minor>\\d+)");

    public static HttpVersion getBestCompatibleVersion(String literalVersion) throws BadHttpVersionException {
        Matcher matcher = httpVersionRegexPattern.matcher(literalVersion);
        if(!matcher.find() || matcher.groupCount() != 2){
            throw new BadHttpVersionException();
        }
        int major = Integer.parseInt(matcher.group("Major"));
        int minor = Integer.parseInt(matcher.group("Minor"));
        HttpVersion tempBestCompatible = null;
        for (HttpVersion version : HttpVersion.values()) {
            if (version.Literal.equals(literalVersion)) {
                return version;
            } else {
                if (version.Major == major) {
                    if (version.Minor < minor) {
                        tempBestCompatible = version;
                    }
                }
            }
        }
        return tempBestCompatible;
    }
}

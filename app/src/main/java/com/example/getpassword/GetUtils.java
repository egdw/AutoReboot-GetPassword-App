package com.example.getpassword;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取闪讯密码和闪讯过期时间工具类
 *
 * @author egdw
 */
public class GetUtils {
    public static String getPassword(String text) {
        Pattern pattern = Pattern.compile("\\d{6}");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            return matcher.group();
        }
        return null;
    }


    public static String getTime(String text) {
        Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}\\s{1}\\d{2}:\\d{2}:\\d{2}");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}

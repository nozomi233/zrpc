package com.zhulang;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author Nozomi
 * @Date 2024/4/20 11:01
 */

public class DateUtils {
    public static Date get(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return  sdf.parse(pattern);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}

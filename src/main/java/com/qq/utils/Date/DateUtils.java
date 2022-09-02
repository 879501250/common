package com.qq.utils.Date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    //日期转字符串
    public static String date2String(Date date,String patt){
        SimpleDateFormat sdf = new SimpleDateFormat(patt);
        String format = sdf.format(date);
        return format;
    }

    //字符串转日期
    public static Date string2Date(String str,String patt) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(patt);
        Date date = sdf.parse(str);
        return date;
    }

    private static final String dateFormat = "yyyy-MM-dd";

    /**
     * 格式化日期
     *
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(date);
    }

    /**
     * 在日期date上增加amount天 。
     *
     * @param date   处理的日期，非null
     * @param amount 要加的天数，可能为负数
     */
    public static Date addDays(Date date, int amount) {
        Calendar now =Calendar.getInstance();
        now.setTime(date);
        now.set(Calendar.DATE,now.get(Calendar.DATE)+amount);
        return now.getTime();
    }
}

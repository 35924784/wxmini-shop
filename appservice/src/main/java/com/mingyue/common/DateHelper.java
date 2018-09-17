package com.mingyue.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper{

    // format 如 yyyyMMddhhmmssSSS
    public static String getDateString(String format) {
        
        Date date=new Date();
        SimpleDateFormat df=new SimpleDateFormat(format);
        return df.format(date);
    }

}
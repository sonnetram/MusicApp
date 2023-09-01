package com.example.music1.util;

public class TimeUtil {
    public static String millToTimeFormat(int timeMills){
        int second = timeMills / 1000;
        int minute = second / 60;
        int lastSecond = second % 60;
        String strSecond = "";
        if(lastSecond < 10 ){
            strSecond = "0" +lastSecond;
        }else {
            strSecond = "" + lastSecond;
        }
        String strMinute = "";
        if(minute < 10 ){
            strMinute = "0" +minute;
        }else {
            strMinute = "" + minute;
        }
        return strMinute + ":" + strSecond;

    }
}

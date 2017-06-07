package com.chat.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * ��Ҫ����ҳ��ʱ���ת��
 * ���ڹ�����
 * @yingmu
 *
 */
public class DateUtil {
    /**
     * �°�ʱ��չʾ ����ҳ��
     * @param mTimeStamp
     * @return
     * ����ע��ע��ʱ�䵥λ�Ǻ���
     */
    public static String getSessionTime(int mTimeStamp) {
        if (mTimeStamp <= 0) {
            return null;
        }
        String[] weekDays = {
                "������", "����һ", "���ڶ�", "������", "������", "������", "������"
        };
        String strDesc = null;
        SimpleDateFormat formatYear = new SimpleDateFormat("yy/MM/dd");
        SimpleDateFormat formatToday = new SimpleDateFormat("HH:mm");
        /**��Ϣʱ���*/
        long changeTime = (long) mTimeStamp;
        long messageTimeStamp = changeTime * 1000;
        /**��ǰ��ʱ���*/
        long currentTimeStamp =System.currentTimeMillis();
        /**��ȡ����� 0 ��ʱ���*/
        long todayTimeStamp = getTimesmorning();
        /**��ȡ ��һ�� 0��ʱ���*/
        long rangeWeekStamp = todayTimeStamp - 86400000*6;

        /**�������ʾ hh:mm   (����������)
         * ����
         * ����һ
         * ������ �� �������� �����塢������
         * yy-hh-mm
         * */
        do{
            long diff = currentTimeStamp -  messageTimeStamp;
            long diffToday = currentTimeStamp - todayTimeStamp;
            /**����֮�ڵ�*/
            if(diff < diffToday){
                strDesc = formatToday.format(messageTimeStamp);
                break;
            }

            long diffWeek = currentTimeStamp - rangeWeekStamp;
            /**���һ�ܵ��ж�*/
            if(diff < diffWeek){
                /**��������ʱ��*/
                long yesterday = todayTimeStamp - 86400000;
                long diffYesterday = currentTimeStamp - yesterday;
                if(diff < diffYesterday){
                    strDesc = "����";
                }else{
                    Calendar weekCal = Calendar.getInstance();
                    weekCal.setTimeInMillis(messageTimeStamp);
                    int w =  weekCal.get(Calendar.DAY_OF_WEEK) -1;
                    w = w<0?0:w;
                    strDesc = weekDays[w];
                }
                break;
            }
            /**��������ʾ*/
            strDesc = formatYear.format(messageTimeStamp);
        }while(false);
        return strDesc;
    }

    /**
     * ��ȡ���� ����ʱ�����linux��
     * @return
     */
    public  static long getTimesmorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    
    public static boolean needDisplayTime(int predateTime, int curdateTime) {
        long timediff = (curdateTime - predateTime);
        return (timediff >= 5 * 60 );
    }

    public static String getTimeDiffDesc(Date date) {

        if (date == null) {
            return null;
        }

        String strDesc = null;
        Calendar curCalendar = Calendar.getInstance();
        Date curDate = new Date();
        curCalendar.setTime(curDate);
        Calendar thenCalendar = Calendar.getInstance();
        thenCalendar.setTime(date);

        String[] weekDays = {
                "������", "����һ", "���ڶ�", "������", "������", "������", "������"
        };
        int w = thenCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        // SimpleDateFormat format = new
        // SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar current = Calendar.getInstance();
        Calendar today = Calendar.getInstance(); // ����
        today.set(Calendar.YEAR, current.get(Calendar.YEAR));
        today.set(Calendar.MONTH, current.get(Calendar.MONTH));
        today.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        // Date datetoday = today.getTime();
        // System.out.println(format.format(datetoday));

        Calendar yesterday = Calendar.getInstance(); // ����
        yesterday.setTime(curDate);
        yesterday.add(Calendar.DATE, -1);
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);
        // Date dateyestoday = yesterday.getTime();
        // System.out.println(format.format(dateyestoday));

        Calendar sevendaysago = Calendar.getInstance(); // 7��
        sevendaysago.setTime(curDate);
        sevendaysago.add(Calendar.DATE, -7);
        sevendaysago.set(Calendar.HOUR_OF_DAY, 0);
        sevendaysago.set(Calendar.MINUTE, 0);
        sevendaysago.set(Calendar.SECOND, 0);
        // Date datesevenago = sevendaysago.getTime();
        // System.out.println(format.format(datesevenago));
        /*
         * Date tasktime = yesterday.getTime(); SimpleDateFormat df=new
         * SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         * System.out.println(df.format(tasktime));
         */

        int thenMonth = thenCalendar.get(Calendar.MONTH);
        int thenDay = thenCalendar.get(Calendar.DAY_OF_MONTH);
        int h = thenCalendar.get(Calendar.HOUR_OF_DAY);
        int m = thenCalendar.get(Calendar.MINUTE);
        String sh = "", sm = "";
        if (h < 10)
            sh = "0";

        if (m < 10)
            sm = "0";
        if (thenCalendar.after(today))// today
        {
            if (h < 6) {
                strDesc = "�賿 " + sh + h + ":" + sm + m;
            } else if (h < 12) {
                strDesc = "���� " + sh + h + ":" + sm + m;
            } else if (h < 13) {
                strDesc = "���� " + h + ":" + sm + m;
            } else if (h < 19) {
                strDesc = "���� " + (h - 12) + ":" + sm + m;
            } else {
                strDesc = "���� " + (h - 12) + ":" + sm + m;
            }
        } else if (thenCalendar.before(today) && thenCalendar.after(yesterday)) {// yestoday
            // System.out.println("yestoday");
            if (h < 6) {
                strDesc = "�����賿 " + sh + h + ":" + sm + m;
            } else if (h < 12) {
                strDesc = "�������� " + sh + h + ":" + sm + m;
            } else if (h < 13) {
                strDesc = "�������� " + h + ":" + sm + m;
            } else if (h < 19) {
                strDesc = "�������� " + (h - 12) + ":" + sm + m;
            } else {
                strDesc = "�������� " + (h - 12) + ":" + sm + m;
            }
        } else if (thenCalendar.before(yesterday)
                && thenCalendar.after(sevendaysago)) {// 2 ~ 7days ago
            // System.out.println("2~7");
            if (h < 6) {
                strDesc = weekDays[w] + "�賿 " + sh + h + ":" + sm + m;
            } else if (h < 12) {
                strDesc = weekDays[w] + "���� " + sh + h + ":" + sm + m;
            } else if (h < 13) {
                strDesc = weekDays[w] + "���� " + h + ":" + sm + m;
            } else if (h < 19) {
                strDesc = weekDays[w] + "���� " + (h - 12) + ":" + sm + m;
            } else {
                strDesc = weekDays[w] + "���� " + (h - 12) + ":" + sm + m;
            }
        } else {
            // System.out.println("7~");
            if (h < 6) {
                strDesc = (thenMonth + 1) + "��" + thenDay + "��" + "�賿 " + sh
                        + h + ":" + sm + m;
            } else if (h < 12) {
                strDesc = (thenMonth + 1) + "��" + thenDay + "��" + "���� " + sh
                        + h + ":" + sm + m;
            } else if (h < 13) {
                strDesc = (thenMonth + 1) + "��" + thenDay + "��" + "���� " + h
                        + ":" + sm + m;
            } else if (h < 19) {
                strDesc = (thenMonth + 1) + "��" + thenDay + "��" + "���� "
                        + (h - 12) + ":" + sm + m;
            } else {
                strDesc = (thenMonth + 1) + "��" + thenDay + "��" + "���� "
                        + (h - 12) + ":" + sm + m;
            }
        }
        // System.out.println(strDesc);
        return strDesc;
    }


    public static String getCurrentNowTime(){
    	SimpleDateFormat mFormat = new java.text.SimpleDateFormat("yyyy:MM:dd HH:mm:ss:SSS");
		return mFormat.format(System.currentTimeMillis());
    }

}

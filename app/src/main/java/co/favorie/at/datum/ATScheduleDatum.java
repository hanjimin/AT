package co.favorie.at.datum;

import com.orm.SugarRecord;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by bmac on 2015-07-27.
 */
public class ATScheduleDatum extends SugarRecord<ATScheduleDatum> {
    public String title, start, end, lable, unit;
    public int percentage, type, additionalDatum = 0, selectedCharacter = 0, listIndex = 0;
    public int tabIndex = 0;
    public int datePreafter = 0;
    public int toggleIndex = 0;
    public Date startDate, endDate;
    public boolean widgetStatus = true;
    public boolean notiStatus = true;
    public static final int TIME_TO_TIME = 0;
    public static final int DATE_TO_DATE = 1;
    public static final int CUSTOM = 2;
    public static final int DAILY = 100;
    public static final int WEEKLY = 101;
    public static final int MONTHLY = 102;
    public static final int YEARLY = 103;
    public static final int LIFETIME = 104;
    public static final int WEEKLY_MON = 105;
    /////////////////////////////////////////////////////////////////////////////////////////
    /*   title                                           (label)              percentage   */
    /*                  ========progressbar(percentage)======---------                     */
    /*   start                                                                   end       */
    /////////////////////////////////////////////////////////////////////////////////////////
    /*  startDate and endDate save their date or time */
    /* when add or update AT => set() */
    /* when refresh AT list => refresh() ----- means recalculate with current date(time) */

    /* calculateDateToDate() and calculateTimeToTime() set lable in their each procedure */

    private Calendar calendarStart = Calendar.getInstance(), calendarEnd = Calendar.getInstance();

    public ATScheduleDatum() {
    }

    public ATScheduleDatum(ATScheduleDatum t) {
        title = t.title;
        start = t.start;
        end = t.end;
        unit = t.unit;
        lable = t.lable;
        percentage = t.percentage;
        type = t.type;
        additionalDatum = t.additionalDatum;
        selectedCharacter = t.selectedCharacter;
        listIndex = t.listIndex;
        startDate = t.startDate;
        endDate = t.endDate;
        widgetStatus = t.widgetStatus;
        notiStatus = t.notiStatus;
        tabIndex = t.tabIndex;
        datePreafter = t.datePreafter;
        toggleIndex = t.toggleIndex;
    }

    public void set(int toggleIndex) {
        this.toggleIndex = toggleIndex;
    }
    public void set(int toggleIndex, int mLabel) {
        this.toggleIndex = toggleIndex;
        if (mLabel > 0) {
            this.lable = "+" + mLabel;
        } else {
            this.lable = "" + mLabel;
        }
    }

    public void set(int whichCharacter, boolean widgetStatus, boolean notiStatus, int additionalDatum) {
        this.additionalDatum = additionalDatum;
        set(whichCharacter, widgetStatus, notiStatus);
    }

    public void set(int whichCharacter, boolean widgetStatus, boolean notiStatus) {
        this.selectedCharacter = whichCharacter;
        this.widgetStatus = widgetStatus;
        this.notiStatus = notiStatus;
    }

    public void set(String title, Date startDate, Date endDate, int type, int whichCharacter, boolean widgetStatus, boolean notiStatus) {
        set(title, startDate, endDate, type, whichCharacter);
        this.widgetStatus = widgetStatus;
        this.notiStatus = notiStatus;
    }

    public void set(String title, Date startDate, Date endDate, int type, int whichCharacter, boolean widgetStatus, boolean notiStatus, int tabIndex, int datePreafter) {
        set(title, startDate, endDate, type, whichCharacter);
        this.widgetStatus = widgetStatus;
        this.notiStatus = notiStatus;
        this.tabIndex = tabIndex;
        this.datePreafter = datePreafter;
    }

    public void set(String title, String start, String lable, String end, String unit, int type, int whichCharacter, boolean widgetStatus, boolean notiStatus) {
        this.widgetStatus = widgetStatus;
        this.notiStatus = notiStatus;
        this.type = type;
        this.title = title;
        this.selectedCharacter = whichCharacter;
        this.start = start;
        this.lable = lable;
        this.end = end;
        this.unit = unit;

        this.startDate = new Date();
        this.endDate = new Date();
    }

    public void set(String title, Date startDate, Date endDate, int type, int whichCharacter) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.selectedCharacter = whichCharacter;
        calendarStart.setTime(startDate);
        calendarEnd.setTime(endDate);
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        Locale eng = new Locale("eng");
        Locale kor = new Locale("ko");
        Locale chin = new Locale("zh");
        Locale jap = new Locale("ja");
        String lang = Locale.getDefault().getLanguage();

        switch (type) {
            case TIME_TO_TIME:
                int h1 = calendarStart.get(Calendar.HOUR_OF_DAY), m1 = calendarStart.get(Calendar.MINUTE), h2 = calendarEnd.get(Calendar.HOUR_OF_DAY), m2 = calendarEnd.get(Calendar.MINUTE);
                start = hhmmFormatter(h1, m1);
                end = hhmmFormatter(h2, m2);
                break;
            case DATE_TO_DATE:
                if (lang.equals("ko")) {
                    start = calendarStart.get(Calendar.DAY_OF_MONTH) + "\n" + new DateFormatSymbols(kor).getShortMonths()[calendarStart.get(Calendar.MONTH)] + "";
                    end = calendarEnd.get(Calendar.DAY_OF_MONTH) + "\n" + new DateFormatSymbols(kor).getShortMonths()[calendarEnd.get(Calendar.MONTH)] + "";
                } else if(lang.equals("zh")){
                    start = calendarStart.get(Calendar.DAY_OF_MONTH) + "\n" + new DateFormatSymbols(chin).getShortMonths()[calendarStart.get(Calendar.MONTH)] + "";
                    end = calendarEnd.get(Calendar.DAY_OF_MONTH) + "\n" + new DateFormatSymbols(chin).getShortMonths()[calendarEnd.get(Calendar.MONTH)] + "";
                } else if(lang.equals("ja")){
                    start = calendarStart.get(Calendar.DAY_OF_MONTH) + "\n" + new DateFormatSymbols(jap).getShortMonths()[calendarStart.get(Calendar.MONTH)] + "";
                    end = calendarEnd.get(Calendar.DAY_OF_MONTH) + "\n" + new DateFormatSymbols(jap).getShortMonths()[calendarEnd.get(Calendar.MONTH)] + "";
                } else {
                    start = calendarStart.get(Calendar.DAY_OF_MONTH) + "\n" + new DateFormatSymbols(eng).getShortMonths()[calendarStart.get(Calendar.MONTH)] + "";
                    end = calendarEnd.get(Calendar.DAY_OF_MONTH) + "\n" + new DateFormatSymbols(eng).getShortMonths()[calendarEnd.get(Calendar.MONTH)] + "";
                }
                break;
            case DAILY:
                start = "0";
                end = "24";
                break;
            case WEEKLY:
                if (lang.equals("ko")) {
                    start = "일";
                    end = "토";
                } else if(lang.equals("zh")){
                    start = "周日";
                    end = "周六";
                } else if(lang.equals("ja")){
                    start = "日";
                    end = "土";
                } else {
                    start = "Sun";
                    end = "Sat";
                }
                break;
            case WEEKLY_MON:
                if (lang.equals("ko")) {
                    start = "월";
                    end = "일";
                } else if(lang.equals("zh")){
                    start = "周一";
                    end = "周日";
                } else if(lang.equals("ja")){
                    start = "月";
                    end = "日";
                } else {
                    start = "Mon";
                    end = "Sun";
                }
                this.toggleIndex = 1;
                break;
            case MONTHLY:
                c.add(Calendar.MONTH, 1);
                c.set(Calendar.DAY_OF_MONTH, 1);
                c.add(Calendar.DATE, -1);
                start = "01";
                end = c.get(Calendar.DAY_OF_MONTH) + "";
                break;
            case YEARLY:
                if (lang.equals("ko")) {
                    start = "1월";
                    end = "12월";
                } else if(lang.equals("zh")){
                    start = "1月";
                    end = "12月";
                } else if(lang.equals("ja")){
                    start = "1月";
                    end = "12月";
                } else {
                    start = "Jan";
                    end = "Dec";
                }
                break;
            case LIFETIME:
                start = "0";
                end = "100";
                break;
        }
    }

    public void refresh() {
        double result = 0;
        Calendar c = Calendar.getInstance();
        Locale eng = new Locale("eng");
        Locale kor = new Locale("ko");
        Locale chin = new Locale("zh");
        Locale jap = new Locale("ja");
        String lang = Locale.getDefault().getLanguage();
        c.setTime(new Date());
        switch (type) {
            case TIME_TO_TIME:
                result = calculateTimeToTime();
                break;
            case DATE_TO_DATE:
                result = calculateDateToDate(startDate, endDate);
                break;
            case CUSTOM:
                result = calculateCustom(start, end);
                break;
            case DAILY:
                start = "0";
                end = "24";
                lable = hhmmFormatter(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                Calendar tommorow = Calendar.getInstance();
                tommorow = (Calendar) c.clone();
                tommorow.add(Calendar.DATE, 1);
                result = calculateDateTimeToDateTime(c.getTime(), tommorow.getTime());
                break;
            case WEEKLY:
                if (lang.equals("ko")) {
                    lable = new DateFormatSymbols(kor).getShortWeekdays()[c.get(Calendar.DAY_OF_WEEK)];
                } else if(lang.equals("zh")){
                    lable = new DateFormatSymbols(chin).getShortWeekdays()[c.get(Calendar.DAY_OF_WEEK)];
                } else if(lang.equals("ja")){
                    lable = new DateFormatSymbols(jap).getShortWeekdays()[c.get(Calendar.DAY_OF_WEEK)];
                } else {
                    lable = new DateFormatSymbols(eng).getShortWeekdays()[c.get(Calendar.DAY_OF_WEEK)];
                }
                c.setTime(new Date());
                Calendar t1 = Calendar.getInstance(), t2 = Calendar.getInstance();
                setCalendarHHMMSSToZero(t1);
                setCalendarHHMMSSToZero(t2);
                t1.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                t2.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                t2.add(Calendar.DATE, 1);
                result = calculateDateTimeToDateTime(t1.getTime(), t2.getTime());
                break;
            case WEEKLY_MON:
                if (lang.equals("ko")) {
                    lable = new DateFormatSymbols(kor).getShortWeekdays()[c.get(Calendar.DAY_OF_WEEK)];
                } else if(lang.equals("zh")){
                    lable = new DateFormatSymbols(chin).getShortWeekdays()[c.get(Calendar.DAY_OF_WEEK)];
                } else if(lang.equals("ja")){
                    lable = new DateFormatSymbols(jap).getShortWeekdays()[c.get(Calendar.DAY_OF_WEEK)];
                } else {
                    lable = new DateFormatSymbols(eng).getShortWeekdays()[c.get(Calendar.DAY_OF_WEEK)];
                }
                c.setTime(new Date());
                Calendar tt1 = Calendar.getInstance(), tt2 = Calendar.getInstance(), tt3 = Calendar.getInstance();
                Date d = new Date();
                setCalendarHHMMSSToZero(tt1);
                setCalendarHHMMSSToZero(tt2);
                tt1.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                tt2.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                tt2.add(Calendar.DATE, 8);
                tt3.setTime(d);
                if (tt1.compareTo(tt3) == 1) {
                    tt1.add(Calendar.DATE, -7);
                    tt2.add(Calendar.DATE, -7);
                }
                result = calculateDateTimeToDateTime(tt1.getTime(), tt2.getTime());
                break;
            case MONTHLY:
                int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
                c.add(Calendar.MONTH, 1);
                c.set(Calendar.DAY_OF_MONTH, 1);
                c.add(Calendar.DATE, -1);
                int lastDayOfMonth = c.get(Calendar.DAY_OF_MONTH);
                start = "01";
                end = lastDayOfMonth + "";
                if (dayOfMonth == lastDayOfMonth) {
                    lable = "D-day";
                } else {
                    lable = "" + (dayOfMonth - lastDayOfMonth);
                }
                result = (double) (dayOfMonth - 1) / (lastDayOfMonth - 1);
                break;
            case YEARLY:
                GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
                int leap = (gcal.isLeapYear(c.get(Calendar.YEAR))) ? 1 : 0;
                int restDay = 365 + leap - c.get(Calendar.DAY_OF_YEAR);
                if (restDay != 0) {
                    lable = "" + (-(restDay));
                } else {
                    lable = "D-day";
                }


                result = (double) (c.get(Calendar.DAY_OF_YEAR)) / (365 + leap);
                break;
            case LIFETIME:
                lable = "" + additionalDatum;
                result = (double) additionalDatum / 100;
                break;
        }
        percentage = (int) (result * 100);
        if (percentage < 0)
            percentage = 0;
        else if (percentage > 100 && type != LIFETIME)
            percentage = 100;
        else if (percentage > 100 && type == LIFETIME)
            percentage = additionalDatum;
    }

    // 'Date' Customizing part
    // return : percentage
    private double calculateDateToDate(Date startDate, Date endDate) {

        if (tabIndex == 0) {
            tabIndex = 1;
        }
        Date sd = setDateToCalendar(startDate);
        Date ed = setDateToCalendar(endDate);
        Date cd = setDateToCalendar(new Date());
        double diffDay = (cd.getTime() - ed.getTime()) / (1000 * 3600 * 24); // 오늘 - 마지막날
        double diffStartEndDay = (sd.getTime() - ed.getTime()) / (1000 * 3600 * 24); // 시작날 - 마지막날
        int diffStartEndDayForAdd = (int) ((endDate.getTime() - startDate.getTime()) / (1000 * 3600 * 24)); //예) 100, 200, 365

        // 간격이 오차가 생기는 경우에 대한 처리 예) 99.5 -> 100
        if (diffStartEndDayForAdd % 100 != 0 && diffStartEndDayForAdd % 365 != 0) {
            diffStartEndDayForAdd = (int) Math.ceil(diffStartEndDayForAdd + 0.1);
        }

        if (diffDay == 0) {
            lable = "D-Day";
        } else if (diffDay >= diffStartEndDay) {
            if (diffDay > 0) {
                lable = "+" + (int) diffDay;
                if (tabIndex == 1 && toggleIndex == 1) {
                    lable = "+" + ((int) diffDay + diffStartEndDayForAdd);
                }

            } else {

                // 1. D-DAY 기능 중 '일'(tabIndex:2) 2.오늘 이전의 날을 선택했으면서(datePreafter:0) 3. +값이 label에 들어가야하는 경우
                if (tabIndex == 2 && datePreafter == 0 && toggleIndex == 1) {
                    lable = "+" + ((int) diffDay + diffStartEndDayForAdd + 1);

                    // 1. D-DAY 기능 중 '기간'(tabIndex:1) 2. +값이 label에 들어가야하는 경우
                } else if (tabIndex == 1 && toggleIndex == 1) {
                    lable = "+" + ((int) diffDay + diffStartEndDayForAdd);
                } else {
                    lable = "" + (int) (diffDay);
                }
            }
        } else {
            lable = "Zzz";
        }
        return (double) (cd.getTime() - sd.getTime()) / (ed.getTime() - sd.getTime());
    }


    private double calculateDateTimeToDateTime(Date sd, Date ed) {
        Date cd = new Date();
        return (double) (cd.getTime() - sd.getTime()) / (ed.getTime() - sd.getTime());
    }

    private double calculateCustom(String start, String end) {
        this.start = start;
        this.end = end;
        return ((double) (Integer.parseInt(start) - Integer.parseInt(lable)) / (double) (Integer.parseInt(start) - Integer.parseInt(end)));
    }

    private void setCalendarHHMMSSToZero(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private Date setCalendarSSToZero(Calendar calendar) {
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }


    private Date setDateToCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setCalendarHHMMSSToZero(calendar);
        Date mDate = new Date(calendar.getTimeInMillis());
        return mDate;
    }

    private double calculateTimeToTime() {
        double result = 0;
        Date curDate = new Date();
        setCalendarsToCurrentYMD();
        Date calStartDate = setCalendarSSToZero(calendarStart), calEndDate = setCalendarSSToZero(calendarEnd);


        // startDate < endDate
        if (calStartDate.compareTo(calEndDate) < 0) {
            result = calculateDateTimeToDateTime(calStartDate, calEndDate);

            // startDate > endDate
        } else {
            calendarEnd.add(Calendar.DATE, 1);
            calEndDate = setCalendarSSToZero(calendarEnd);
            result = calculateDateTimeToDateTime(calStartDate, calEndDate);
        }

        int diffMin = (int) (curDate.getTime() / 60000 - calendarEnd.getTime().getTime() / 60000);
        int diffStartEnd = (int) (calendarStart.getTime().getTime() / 60000 - calendarEnd.getTime().getTime() / 60000);
        int absDiffMin = Math.abs(diffMin);
        int h = absDiffMin / 60, m = absDiffMin % 60;
        lable = "-" + hhmmFormatter(h, m);
        if (diffMin > 0 || diffMin < diffStartEnd)
            lable = "Zzz";
        return result;
    }


    private void setCalendarsToCurrentYMD() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        calendarStart.set(Calendar.YEAR, c.get(Calendar.YEAR));
        calendarStart.set(Calendar.MONTH, c.get(Calendar.MONTH));
        calendarStart.set(Calendar.DATE, c.get(Calendar.DATE));
        calendarEnd.set(Calendar.YEAR, c.get(Calendar.YEAR));
        calendarEnd.set(Calendar.MONTH, c.get(Calendar.MONTH));
        calendarEnd.set(Calendar.DATE, c.get(Calendar.DATE));
    }

    public static String hhmmFormatter(int hour, int minute) {
        return (hour < 10 ? "0" + hour : hour + "") + ":" + (minute < 10 ? "0" + minute : minute + "");
    }
}

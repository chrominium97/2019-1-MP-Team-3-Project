package edu.skku.map.class42.team3;

import java.util.Calendar;

class SearchOptions {

    enum SearchMode {
        BY_DEPARTURE, BY_ARRIVAL
    }

    SearchOptions(Calendar dateTime, SearchMode mode) {
        this.dateTime = dateTime;
        this.mode = mode;
    }

    private Calendar dateTime;
    private SearchMode mode;

    SearchOptions setMode(SearchMode mode) {
        this.mode = mode;
        return this;
    }

    SearchOptions setDateTimeNow() {
        dateTime = Calendar.getInstance();
        return this;
    }

    SearchOptions setDate(int year, int month, int dayOfMonth) {
        dateTime.set(year, month, dayOfMonth);
        return this;
    }

    SearchOptions setTime(int hour, int minute) {
        dateTime.set(Calendar.HOUR_OF_DAY, hour);
        dateTime.set(Calendar.MINUTE, minute);
        return this;
    }

    Calendar getDateTime() {
        return dateTime;
    }
}
package com.ringme.cms.enums;

public enum RecurrenceType {
    NONE,
    WEEKLY,
    FORTNIGHTLY,
    THREE_WEEKLY,
    FOUR_WEEKLY,
    SIX_WEEKLY,
    EIGHT_WEEKLY,
    MONTHLY,
    DAILY;

    public static RecurrenceType stringToRecurrentType(String input){
        for(RecurrenceType rt : RecurrenceType.values()){
            if(input.equalsIgnoreCase(rt.toString())){
                return rt;
            }
        }
        return NONE;
    }
}

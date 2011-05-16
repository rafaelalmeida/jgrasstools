package org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils;

public enum CalibrationTimeParameterCodes {
    DURATION(0, "DURATION", "Simulation duration [min]", "0"), //
    HYDSTEP(1, "HYDRAULIC TIMESTEP", "Hydraulic time step [min]", "60"), //
    QUALSTEP(2, "QUALITY TIMESTEP", "Water quality time step [min]", "6"), //
    PATTERNSTEP(3, "PATTERN TIMESTEP", "Time pattern time step [min]", "60"), //
    PATTERNSTART(4, "PATTERN START", "Time pattern start time [min]", "0"), //
    REPORTSTEP(5, "REPORT TIMESTEP", "Reporting time step [min]", "60"), //
    REPORTSTART(6, "REPORT START", "Report starting time [min]", "0"), //
    RULESTEP(7, "RULE TIMESTEP", "Time step for evaluating rule-based controls [min]", "6"), //
    STATISTIC(8, "STATISTIC", "Type of time series post-processing to use (can be AVERAGED, MINIMUM, MAXIMUM, NONE)", "NONE"), //
    STARTCLOCKTIME(-1, "START CLOCKTIME", "The time of the day at which the simulation begins (format HH:MM AM/PM)", "12:00 AM");
    // PERIODS(9, "", "Number of reporting periods saved to binary output file");

    private int code;
    private String key;
    private String description;
    private final String defaultValue;
    CalibrationTimeParameterCodes( int code, String key, String description, String defaultValue ) {
        this.code = code;
        this.key = key;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    public int getCode() {
        return code;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public static CalibrationTimeParameterCodes forCode( int i ) {
       CalibrationTimeParameterCodes[] values = values();
        for( CalibrationTimeParameterCodes type : values ) {
            if (type.code == i) {
                return type;
            }
        }
        throw new IllegalArgumentException("No type for the given code: " + i);
    }

    public static CalibrationTimeParameterCodes forKey( String key ) {
        CalibrationTimeParameterCodes   [] values = values();
        for( CalibrationTimeParameterCodes type : values ) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No type for the given key: " + key);
    }
}

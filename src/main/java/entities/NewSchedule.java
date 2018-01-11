package entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NewSchedule {
	
    private Employee employee;
	private List<WeekSchedule> weeks;
    private Set<PlannedFeature> plannedFeatures;
    
    // The number of hours left this employee has for the whole release
    private double totalHoursLeft;
    private final int nbWeeks;
    private final double hoursPerWeek;
    private final double globalHoursPerWeek;
    private double currentHour;

    public NewSchedule(Employee employee, int nbWeeks, double globalHoursPerWeek) {
        this.employee = employee;
        this.nbWeeks = nbWeeks;
        this.hoursPerWeek = employee.getWeekAvailability();
        this.globalHoursPerWeek = globalHoursPerWeek;
        this.currentHour = 0.0;

        totalHoursLeft = nbWeeks * employee.getWeekAvailability();

        weeks = new ArrayList<>();
        plannedFeatures = new HashSet<>();
    }

}

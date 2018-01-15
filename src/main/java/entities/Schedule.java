package entities;

import java.util.*;

/**
 * Represents the schedule of an employee during a certain number of weeks
 * and takes care of assigning features to said employee.
 */
@Deprecated
public class Schedule implements Iterable<WeekSchedule> {
    private List<WeekSchedule> weeks;
    private Employee employee;
    private Set<PlannedFeature> plannedFeatures;

    // The number of hours left this employee has for the whole release
    private double totalHoursLeft;
    private final int nbWeeks;
    private final double hoursPerWeek;
    private final double globalHoursPerWeek;
    
    private double currentHour;

	public Schedule(Employee employee, int nbWeeks, double globalHoursPerWeek) {
        this.employee = employee;
        this.nbWeeks = nbWeeks;
        this.hoursPerWeek = employee.getWeekAvailability();
        this.globalHoursPerWeek = globalHoursPerWeek;
        this.currentHour = 0.0;

        totalHoursLeft = nbWeeks * employee.getWeekAvailability();

        weeks = new ArrayList<>();
        plannedFeatures = new HashSet<>();
    }

    // Copy constructor
    public Schedule(Schedule origin) {
        this(origin.employee, origin.nbWeeks, origin.globalHoursPerWeek);

        totalHoursLeft = origin.totalHoursLeft;

        weeks = new ArrayList<>();
        for (WeekSchedule week : weeks)
            weeks.add(new WeekSchedule(week));

        plannedFeatures = new HashSet<>();
        for (PlannedFeature pf : origin.plannedFeatures)
            plannedFeatures.add(new PlannedFeature(pf));
    }
    
    public double getCurrentHour() {
		return currentHour;
	}

	public void setCurrentHour(double currentHour) {
		this.currentHour = currentHour;
	}

    /* --- PUBLIC --- */

    public boolean scheduleFeature(PlannedFeature pf) {
        return scheduleFeature(pf, true);
    }

    /**
     * Tries to schedule a PlannedFeature in the first available week
     * @param pf the PlannedFeature to be scheduled
     * @return a boolean indicating whether the PlannedFeature could be scheduled
     */
    public boolean scheduleFeature(PlannedFeature pf, boolean adjustHours) {
    	
    	double featureHoursLeft = pf.getFeature().getDuration();
        // Not enough hours left for this feature in the iteration
        if (totalHoursLeft < featureHoursLeft)
            return false;
    	
    	WeekSchedule weekSchedule = getValidWeek(pf);
    	
    	if (weekSchedule == null) return false;
    	
    	double remainingWeekHours = weekSchedule.getRemainingHours();
        /*WeekSchedule previousWeek = getPreviousWeek(weekSchedule);
        PlannedFeature lastPlanned = getLastPlannedFeature(weekSchedule, previousWeek);*/
    	
    	if (featureHoursLeft <= remainingWeekHours) {
    		
         	pf.setBeginHour(currentHour);
         	
         	weekSchedule.addPlannedFeature(pf);
            weekSchedule.setRemainingHours(remainingWeekHours - featureHoursLeft);
            weekSchedule.setEndHour(pf.getEndHour());
            
            plannedFeatures.add(pf);
            currentHour += featureHoursLeft;
            
            totalHoursLeft -= featureHoursLeft;
            pf.setEndHour(currentHour);
            
        } else {
        	
        	pf.setBeginHour(currentHour);
        	
        	while (featureHoursLeft > 0.0) {
        		
        		weekSchedule.addPlannedFeature(pf);
                plannedFeatures.add(pf);

                double doneHours = Math.min(featureHoursLeft, remainingWeekHours);
                featureHoursLeft -= doneHours;
                totalHoursLeft -= doneHours;
                
                weekSchedule.setRemainingHours(remainingWeekHours - doneHours);
                currentHour += doneHours;
                weekSchedule.setEndHour(currentHour);
                
                if (featureHoursLeft > 0.0) weekSchedule = getNextWeek(weekSchedule);
                remainingWeekHours = weekSchedule.getRemainingHours();
        		
        	}
        	pf.setEndHour(currentHour);
        	         	
        }
    	
    	return true;
    }
    
    private WeekSchedule getNextWeek(WeekSchedule weekSchedule) {
    	double beginHour = Math.ceil(currentHour / globalHoursPerWeek) * globalHoursPerWeek;
    	WeekSchedule nextWeek = new WeekSchedule(beginHour, hoursPerWeek);
    	weeks.add(nextWeek);
    	currentHour = beginHour;
    	return nextWeek;
    }

    private WeekSchedule getValidWeek(PlannedFeature pf) {
    	//System.out.println("This feature should be done at " + pf.getBeginHour() + " but employee can start at " + currentHour);
    	
    	double beginHour = Math.max(pf.getBeginHour(), currentHour);
    	currentHour = beginHour;
    	
    	int weekInt = (int) Math.floor(beginHour / globalHoursPerWeek);
    	
    	while (weeks.size() - 1 < weekInt) {
        	double weekBeginHour = weeks.size() > 0 ? weeks.size() * globalHoursPerWeek : 0.0;
    		WeekSchedule weekSchedule = new WeekSchedule(weekBeginHour, hoursPerWeek);
    		//currentHour = Math.max(currentHour, weekBeginHour);
    		weeks.add(weekSchedule);
    		if (weekInt == weeks.size() - 1) {
    			weekSchedule.setBeginHour(currentHour);
    			weekSchedule.setRemainingHours(Math.min(hoursPerWeek, globalHoursPerWeek - currentHour % globalHoursPerWeek));
    		}
    	}
    	
    	return weeks.get(weekInt);
    	
    }
    
	public WeekSchedule getWeek(int i) { return weeks.get(i); }

    public int size() { return weeks.size(); }

    public boolean isEmpty() { return plannedFeatures.isEmpty(); }

    public List<WeekSchedule> getAllWeeks() {
        weeks.removeIf(week -> week.getPlannedFeatures().isEmpty());
        return weeks;
    }

    public boolean contains(PlannedFeature pf) { return plannedFeatures.contains(pf); }

    public Employee getEmployee() { return employee; }

    public List<PlannedFeature> getPlannedFeatures() {
        return new ArrayList<>(plannedFeatures);
    }

    public double getTotalHoursLeft() { return totalHoursLeft; }

    public void clear() {
        weeks.clear();
        plannedFeatures.clear();
        totalHoursLeft = nbWeeks * employee.getWeekAvailability();
    }

    @Override
    public Iterator<WeekSchedule> iterator() { return weeks.iterator(); }



    /* --- PRIVATE --- */
    private double normalizeHours(double doneHours) {
        return doneHours * (hoursPerWeek/employee.getWeekAvailability());
    }

    // Returns the first non-full week of the employee, or a new one if there isn't any.
    private WeekSchedule getCurrentWeek() {
       return null;
    }

    // Returns the previous week to the given one, null if the given one is the first
    private WeekSchedule getPreviousWeek(WeekSchedule week) {
        int index = weeks.indexOf(week) - 1;

        return index < 0 ? null : weeks.get(index);
    }

    public PlannedFeature getLastPlannedFeature(WeekSchedule week, WeekSchedule previousWeek) {
        List<PlannedFeature> jobs = week.getPlannedFeatures();
        if (!jobs.isEmpty())
            return jobs.get(jobs.size() - 1);

        if (previousWeek == null) return null;

        jobs = previousWeek.getPlannedFeatures();
        if (!jobs.isEmpty())
            return jobs.get(jobs.size() - 1);

        return null;
    }




    @Override
    public String toString() {
        double availability = employee.getWeekAvailability();
        double hours = availability * nbWeeks - totalHoursLeft;

        return String.format(
                "%f hours planned over %d weeks with an availability of %f hours",
                hours, getAllWeeks().size(), availability);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Schedule schedule = (Schedule) o;

        if (Double.compare(schedule.totalHoursLeft, totalHoursLeft) != 0) return false;
        if (nbWeeks != schedule.nbWeeks) return false;
        if (weeks != null ? !weeks.equals(schedule.weeks) : schedule.weeks != null) return false;
        if (!employee.equals(schedule.employee)) return false;
        return plannedFeatures != null ? plannedFeatures.equals(schedule.plannedFeatures) : schedule.plannedFeatures == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = weeks != null ? weeks.hashCode() : 0;
        result = 31 * result + employee.hashCode();
        result = 31 * result + (plannedFeatures != null ? plannedFeatures.hashCode() : 0);
        temp = Double.doubleToLongBits(totalHoursLeft);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + nbWeeks;
        return result;
    }
}


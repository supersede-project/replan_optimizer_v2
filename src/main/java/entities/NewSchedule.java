package entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NewSchedule implements Iterable<WeekSchedule> {
	
    private Employee employee;
	private List<WeekSchedule> weeks;
    private Set<PlannedFeature> plannedFeatures;
    
    // The number of hours left this employee has for the whole release
    private double totalHoursLeft;
    private final int nbWeeks;
    private final double hoursPerWeek;
    private final double globalHoursPerWeek;
    
    private List<Slot> slots; 

    public NewSchedule(Employee employee, int nbWeeks, double globalHoursPerWeek) {
        this.employee = employee;
        this.nbWeeks = nbWeeks;
        this.hoursPerWeek = employee.getWeekAvailability();
        this.globalHoursPerWeek = globalHoursPerWeek;
        
        List<WeekSlot> weekSlots = new ArrayList<>();
        this.weeks = new ArrayList<>();
        this.slots = new ArrayList<>();
        
        for (int i = 0; i < nbWeeks; ++i) {
        	weekSlots.add(new WeekSlot(globalHoursPerWeek*i, globalHoursPerWeek*i + hoursPerWeek));
        	double weekBeginHour = globalHoursPerWeek*i;
    		WeekSchedule weekSchedule = new WeekSchedule(weekBeginHour, hoursPerWeek);
    		weeks.add(weekSchedule);
        }
        
        slots.add(new Slot(weekSlots, hoursPerWeek*nbWeeks));
        
        totalHoursLeft = nbWeeks * employee.getWeekAvailability();

        plannedFeatures = new HashSet<>();
    }
    
    public NewSchedule(NewSchedule origin) {
        this(origin.employee, origin.nbWeeks, origin.globalHoursPerWeek);

        totalHoursLeft = origin.totalHoursLeft;

        weeks = new ArrayList<>();
        for (WeekSchedule week : weeks)
            weeks.add(new WeekSchedule(week));

        plannedFeatures = new HashSet<>();
        for (PlannedFeature pf : origin.plannedFeatures)
            plannedFeatures.add(new PlannedFeature(pf));
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
    	Slot slot = getFirstAvailableSlot(pf);
    	if (slot == null) return false;

    	//System.out.println("Available slot between " + slot.beginHour + " and " + slot.endHour);
    	
    	pf.setBeginHour(Math.max(pf.getBeginHour(), slot.beginHour));
    	//System.out.println("Feature will start at " + pf.getBeginHour());
    	WeekSchedule weekSchedule = getValidWeek(pf.getBeginHour());    
    	double remainingWeekHours = weekSchedule.getRemainingHours();
    	
    	double currentHour = pf.getBeginHour();
    	
    	//TODO REVIEW
    	if (featureHoursLeft <= remainingWeekHours) {
    		         	
         	weekSchedule.addPlannedFeature(pf);
            weekSchedule.setRemainingHours(remainingWeekHours - featureHoursLeft);
            weekSchedule.setEndHour(pf.getEndHour());
            
            plannedFeatures.add(pf);
            currentHour += featureHoursLeft;
            
            totalHoursLeft -= featureHoursLeft;
            pf.setEndHour(currentHour);
                        
        } else {
        	        	
        	while (featureHoursLeft > 0.0) {
        		
        		weekSchedule.addPlannedFeature(pf);
                plannedFeatures.add(pf);

                double doneHours = Math.min(featureHoursLeft, remainingWeekHours);
                featureHoursLeft -= doneHours;
                totalHoursLeft -= doneHours;
                
                weekSchedule.setRemainingHours(remainingWeekHours - doneHours);
                currentHour += doneHours;
                weekSchedule.setEndHour(currentHour);
                
                if (featureHoursLeft > 0.0)  {
                	weekSchedule = getNextWeek(weekSchedule);
                	currentHour = weekSchedule.getBeginHour();
                }
                remainingWeekHours = weekSchedule.getRemainingHours();
        		
        	}
        	pf.setEndHour(currentHour);
        	         	
        }
    	 
        updateSlots(pf, slot);

    	return true;
    	
    }

	private void updateSlots(PlannedFeature pf, Slot slot) {
		int i = slots.indexOf(slot);
		List<WeekSlot> beforeWeekSlots = new ArrayList<>();
		double beforeDuration = 0.0;
		List<WeekSlot> afterWeekSlots = new ArrayList<>();
		double afterDuration = 0.0;
		
		for (WeekSlot weekSlot : slot.weekSlots) {
			//If the weekSlot is previous to the feature
			if (weekSlot.endHour < pf.getBeginHour()) {
				beforeWeekSlots.add(weekSlot);
				beforeDuration += weekSlot.endHour - weekSlot.beginHour;
			}
			//If the weekSlot is after the feature
			else if (weekSlot.beginHour > pf.getEndHour()) {
				afterWeekSlots.add(weekSlot);
				afterDuration += weekSlot.endHour - weekSlot.beginHour;
			}
			else {
				if (weekSlot.endHour > pf.getBeginHour() && weekSlot.beginHour < pf.getBeginHour()) {
					WeekSlot before = new WeekSlot(weekSlot.beginHour, pf.getBeginHour());
					beforeWeekSlots.add(before);
					beforeDuration += before.endHour - before.beginHour;
				}
				if (weekSlot.beginHour < pf.getEndHour() && weekSlot.endHour > pf.getEndHour()) {
					WeekSlot after = new WeekSlot(pf.getEndHour(), weekSlot.endHour);
					afterWeekSlots.add(after);
					afterDuration += after.endHour - after.beginHour;
				}
			}
		}
		
		slots.remove(i);
		if (!afterWeekSlots.isEmpty()) {
			slots.add(i, new Slot(afterWeekSlots, afterDuration));
		}
		if (!beforeWeekSlots.isEmpty()) {
			slots.add(i, new Slot(beforeWeekSlots, beforeDuration));
		}
		
		
		/*for (Slot s : slots) {
			System.out.println("Slot from " + s.beginHour + " to " + s.endHour + " with duration of " + s.duration);
			for (WeekSlot ws : s.weekSlots) {
				System.out.println("\t" + ws.beginHour + " to " + ws.endHour);
			}
		}*/
		
	}

	private WeekSchedule getValidWeek(double beginHour) {
		return weeks.get((int) Math.floor(beginHour / globalHoursPerWeek));
	}
	
	private WeekSchedule getNextWeek(WeekSchedule weekSchedule) {
    	int i = weeks.indexOf(weekSchedule);
    	return weeks.get(i+1);
    }

	private Slot getFirstAvailableSlot(PlannedFeature pf) {
		
		int i = 0;
		while (i < slots.size()) {
			if (slots.get(i).duration >= pf.getFeature().getDuration()) {
				if (pf.getBeginHour() < slots.get(i).beginHour ) {
					pf.setBeginHour(slots.get(i).beginHour);
					return slots.get(i);
				} else if (pf.getBeginHour() >= slots.get(i).beginHour && pf.getEndHour() <= slots.get(i).endHour) {
					return slots.get(i);
				}
				else ++i;
			}
			else ++i;
		}
		return null;
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

        NewSchedule schedule = (NewSchedule) o;

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
	
	/**
	 * Please extract this classes
	 * @author jmotger
	 *
	 */
	private class Slot {
		public List<WeekSlot> weekSlots;
		public double duration;
		public double beginHour;
		public double endHour;
		public Slot(List<WeekSlot> weekSlots, double duration) {
			this.weekSlots = weekSlots;
			this.beginHour = this.weekSlots.get(0).beginHour;
			this.endHour = weekSlots.get(weekSlots.size()-1).endHour;
			this.duration = duration;
		}
    }
	private class WeekSlot {
		public double beginHour;
		public double endHour;
		public WeekSlot(double beginHour, double endHour) {
			this.beginHour = beginHour;
			this.endHour = endHour;
		}
	}
	
}

package entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NewSchedule {
	
    private Employee employee;
	//private List<WeekSchedule> weeks;
    private Set<PlannedFeature> plannedFeatures;
    
    // The number of hours left this employee has for the whole release
    private double totalHoursLeft;
    private final int nbWeeks;
    private final double hoursPerWeek;
    private final double globalHoursPerWeek;
    
    private double beginHour;
    private double endHour;
    
    private List<Slot> slots; 

    public NewSchedule(Employee employee, int nbWeeks, double globalHoursPerWeek) {
        this.employee = employee;
        this.nbWeeks = nbWeeks;
        this.hoursPerWeek = employee.getWeekAvailability();
        this.globalHoursPerWeek = globalHoursPerWeek;
        
        this.beginHour = 0.0;
        this.endHour = 0.0;
        
        HashMap<Integer, WeekSlot> weekSlots = new HashMap<>();
        this.slots = new ArrayList<>();
        
        for (int i = 0; i < nbWeeks; ++i) 
        	weekSlots.put(i, new WeekSlot(globalHoursPerWeek*i, globalHoursPerWeek*(i+1), hoursPerWeek));
        
        slots.add(new Slot(weekSlots));
        
        //printSlots();
        
        totalHoursLeft = nbWeeks * employee.getWeekAvailability();

        plannedFeatures = new HashSet<>();
    }
    
    public NewSchedule(NewSchedule old) {
		this(old.getEmployee(), old.nbWeeks, old.globalHoursPerWeek);
		this.beginHour = old.getBeginHour();
		this.endHour = old.getEndHour();
		this.slots = old.slots;
		totalHoursLeft = old.getTotalHoursLeft();
		plannedFeatures = old.plannedFeatures;
	}

	public double getBeginHour() {
    	return this.beginHour;
    }
    
    public double getEndHour() {
    	return this.endHour;
    }
    
    private void printSlots() {
    	System.out.println("SLOTS");
    	for (Slot s : slots){
    		System.out.println("Slot from " + s.getBeginHour() + " to " + s.getEndHour() + " (" + s.getTotalDuration() +")");
    		for (Integer week : s.getWeekSlots().keySet()) {
    	        System.out.println("\tWeek " + week + " from " + s.getWeekSlots().get(week).getBeginHour()+ " to " + 
    	        		s.getWeekSlots().get(week).getEndHour() + " with " + s.getWeekSlots().get(week).getDuration());
    	    }
    	}
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
    	if (slot == null) 
    		return false;
    	
    	/*
    	pf.setBeginHour(Math.max(pf.getBeginHour(), week.getBeginHour()));
    	double currentHour = pf.getBeginHour();
    	
    	while (currentHour < week.getBeginHour() && currentHour < week.getEndHour() && iterator.hasNext()){
    		week = (WeekSlot) iterator.next();
    	}*/
    	pf.setBeginHour(Math.max(pf.getBeginHour(), slot.getBeginHour()));
    	double currentHour = pf.getBeginHour();
    	
    	Collection<WeekSlot> weeks = slot.getWeekSlots().values();
    	Iterator iterator = weeks.iterator();
    	WeekSlot week = (WeekSlot) iterator.next();
    	while (iterator.hasNext() && week.getBeginHour() < pf.getBeginHour() && week.getEndHour() <= pf.getBeginHour()) {
    		week = (WeekSlot) iterator.next();
    	}
    	
    	while (featureHoursLeft > 0.0) {
    		double doneHours = Math.min(featureHoursLeft, week.getDuration());
    		doneHours = Math.min(doneHours, week.getEndHour() - currentHour);
    		featureHoursLeft -= doneHours;
    		totalHoursLeft -= doneHours;
    		currentHour += doneHours;
    		if (featureHoursLeft > 0.0 && currentHour % globalHoursPerWeek != 0) {
    			currentHour += (globalHoursPerWeek - currentHour % globalHoursPerWeek);
    		}
    		if (featureHoursLeft > 0.0 && iterator.hasNext()) week = (WeekSlot) iterator.next();
    	}
    	
    	pf.setEndHour(currentHour);
    	
    	plannedFeatures.add(pf);
    	    	    	 
        try {
			updateSlots(pf, slot);
		} catch (Exception e) {
			e.printStackTrace();
		}

    	return true;
    	
    }

	private void updateSlots(PlannedFeature pf, Slot slot) throws Exception {
		
		int i = slots.indexOf(slot);
		
		HashMap<Integer, WeekSlot> beforeWeekSlots = new HashMap<>();
		HashMap<Integer, WeekSlot> afterWeekSlots = new HashMap<>();
		
		List<Integer> removedWeeks = new ArrayList<>();
								
		for (Integer week : slot.getWeekSlots().keySet()) {
			WeekSlot weekSlot = slot.getWeekSlots().get(week);
			
			if (weekSlot.getEndHour() <= pf.getBeginHour()) {
				beforeWeekSlots.put(week, new WeekSlot(weekSlot.getBeginHour(), weekSlot.getEndHour(), weekSlot.getDuration()));
			} 
			
			else if (weekSlot.getBeginHour() >= pf.getEndHour()) {
				afterWeekSlots.put(week, new WeekSlot(weekSlot.getBeginHour(), weekSlot.getEndHour(), weekSlot.getDuration()));
			} 
			
			else if (weekSlot.getBeginHour() < pf.getBeginHour()
					&& weekSlot.getEndHour() <= pf.getEndHour()) {
				double duration = weekSlot.getDuration() - (weekSlot.getEndHour() - pf.getBeginHour());
				if (duration > 0.0)
					beforeWeekSlots.put(week, new WeekSlot(weekSlot.getBeginHour(), pf.getBeginHour(), duration));
			} 
			
			else if (weekSlot.getBeginHour() >= pf.getBeginHour()
					&& weekSlot.getEndHour() > pf.getEndHour()) {
				double duration = weekSlot.getDuration() - (pf.getEndHour() - weekSlot.getBeginHour());
				if (duration > 0.0)
					afterWeekSlots.put(week, new WeekSlot(pf.getEndHour(), weekSlot.getEndHour(), duration));
			} 
			
			else if (weekSlot.getBeginHour() < pf.getBeginHour()
					&& weekSlot.getEndHour() > pf.getEndHour()) {
				double duration = Math.max(0, weekSlot.getDuration() - pf.getFeature().getDuration());
				if (duration > 0.0) {
					beforeWeekSlots.put(week, new WeekSlot(weekSlot.getBeginHour(), pf.getBeginHour(), Math.min(duration, pf.getBeginHour() - weekSlot.getBeginHour())));
					afterWeekSlots.put(week, new WeekSlot(pf.getEndHour(), weekSlot.getEndHour(), Math.min(duration, weekSlot.getEndHour() - pf.getEndHour())));
				}
			} 
			else	removedWeeks.add(week);
			//else throw new Exception("Error in Scheduling algorithm - review slot redistribution");
			
		}
		
		for (Integer w : removedWeeks) removeWeek(w);
		
		slots.remove(i);
		if (!afterWeekSlots.isEmpty()) {
			slots.add(i, new Slot(afterWeekSlots));
		}
		if (!beforeWeekSlots.isEmpty()) {
			slots.add(i, new Slot(beforeWeekSlots));
		}
		
		this.beginHour = Math.min(beginHour, pf.getBeginHour());
		this.endHour = Math.max(endHour, pf.getEndHour());
		//System.out.println("After scheduling:");
		//printSlots();
		
	}

	private void removeWeek(Integer week) {
		for (Slot s : slots) {
			s.getWeekSlots().remove(week);
		}
	}

	private Slot getFirstAvailableSlot(PlannedFeature pf) {
		int i = 0;
		while (i < slots.size()) {
			if (slots.get(i).isFeatureFit(pf)) {
				return slots.get(i);
			}
			else ++i;
		}
		return null;
	}

    public boolean isEmpty() { return plannedFeatures.isEmpty(); }

    public boolean contains(PlannedFeature pf) { return plannedFeatures.contains(pf); }

    public Employee getEmployee() { return employee; }

    public List<PlannedFeature> getPlannedFeatures() {
        return new ArrayList<>(plannedFeatures);
    }

    public double getTotalHoursLeft() { return totalHoursLeft; }

    public void clear() {
        //weeks.clear();
        plannedFeatures.clear();
        totalHoursLeft = nbWeeks * employee.getWeekAvailability();
    }
    
    @Override
    public String toString() {
        double availability = employee.getWeekAvailability();
        double hours = availability * nbWeeks - totalHoursLeft;

        return String.format(
                "%f hours planned over %d weeks with an availability of %f hours",
                hours, nbWeeks, availability);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NewSchedule schedule = (NewSchedule) o;

        if (Double.compare(schedule.totalHoursLeft, totalHoursLeft) != 0) return false;
        if (nbWeeks != schedule.nbWeeks) return false;
        if (!employee.equals(schedule.employee)) return false;
        return plannedFeatures != null ? plannedFeatures.equals(schedule.plannedFeatures) : schedule.plannedFeatures == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = 31 + employee.hashCode();
        result = 31 * result + (plannedFeatures != null ? plannedFeatures.hashCode() : 0);
        temp = Double.doubleToLongBits(totalHoursLeft);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + nbWeeks;
        return result;
    }
	
}

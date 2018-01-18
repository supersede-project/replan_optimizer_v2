package entities;

import java.util.ArrayList;
import java.util.HashMap;
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
        
        HashMap<Integer, WeekSlot> weekSlots = new HashMap<>();
        this.weeks = new ArrayList<>();
        this.slots = new ArrayList<>();
        
        for (int i = 0; i < nbWeeks; ++i) {
        	weekSlots.put(i, new WeekSlot(globalHoursPerWeek*i, globalHoursPerWeek*(i+1), hoursPerWeek));
    		WeekSchedule weekSchedule = new WeekSchedule(hoursPerWeek);
    		weeks.add(weekSchedule);
        }
        
        slots.add(new Slot(weekSlots, 0, globalHoursPerWeek*nbWeeks));
        
        //printSlots();
        
        totalHoursLeft = nbWeeks * employee.getWeekAvailability();

        plannedFeatures = new HashSet<>();
    }
    
    private void printSlots() {
    	for (Slot s : slots){
    		System.out.println("Slot from " + s.getBeginHour() + " to " + s.getEndHour() + " (" + s.getTotalDuration() +")");
    		for (Integer week : s.getWeekSlots().keySet()) {
    	        System.out.println("\tWeek " + week + " from " + s.getWeekSlots().get(week).getBeginHour()+ " to " + 
    	        		s.getWeekSlots().get(week).getEndHour() + " with " + s.getWeekSlots().get(week).getDuration());
    	    }
    	}
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
    	
    	pf.setBeginHour(Math.max(pf.getBeginHour(), slot.getBeginHour()));
    	//System.out.println("Feature will start at " + pf.getBeginHour());
    	WeekSchedule weekSchedule = getValidWeek(pf.getBeginHour());   
    	double remainingWeekHours = weekSchedule.getRemainingHours();
    	
    	double currentHour = pf.getBeginHour();
    	
    	//TODO REVIEW
    	if (featureHoursLeft <= remainingWeekHours) {
            
            plannedFeatures.add(pf);
            weekSchedule.addPlannedFeature(pf, currentHour, currentHour + featureHoursLeft);
            
            currentHour += featureHoursLeft;
            
            totalHoursLeft -= featureHoursLeft;
            pf.setEndHour(currentHour);
                        
        } else {

        	while (featureHoursLeft > 0.0) {
        		
                plannedFeatures.add(pf);

                double doneHours = Math.min(featureHoursLeft, remainingWeekHours);

                featureHoursLeft -= doneHours;
                totalHoursLeft -= doneHours;
                
                //System.out.println("Week scheduled between " + currentHour + " and " + doneHours);
        		weekSchedule.addPlannedFeature(pf, currentHour, currentHour + doneHours);
                currentHour += doneHours;
                
                if (featureHoursLeft > 0.0)  {
                	weekSchedule = getNextWeek(weekSchedule);
                	if (currentHour % globalHoursPerWeek != 0)
                		currentHour = currentHour + (globalHoursPerWeek - currentHour % globalHoursPerWeek);
                }
                remainingWeekHours = weekSchedule.getRemainingHours();
        		
        	}
        	pf.setEndHour(currentHour);
        	         	
        }
    	
    	//System.out.println("Week " + weekSchedule.getBeginHour() + " to " + weekSchedule.getEndHour());
    	    	 
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
				
		for (Integer week : slot.getWeekSlots().keySet()) {
			WeekSlot weekSlot = slot.getWeekSlots().get(week);
			if (weekSlot.getEndHour() <= pf.getBeginHour()) {
				beforeWeekSlots.put(week, new WeekSlot(weekSlot.getBeginHour(), weekSlot.getEndHour(), weekSlot.getDuration()));
			} else if (weekSlot.getBeginHour() >= pf.getEndHour()) {
				afterWeekSlots.put(week, new WeekSlot(weekSlot.getBeginHour(), weekSlot.getEndHour(), weekSlot.getDuration()));
			} else if (weekSlot.getBeginHour() < pf.getBeginHour()
					&& weekSlot.getEndHour() <= pf.getEndHour()) {
				double duration = weekSlot.getDuration() - (weekSlot.getEndHour() - pf.getBeginHour());
				beforeWeekSlots.put(week, new WeekSlot(pf.getBeginHour(), weekSlot.getBeginHour(), duration));
			} else if (weekSlot.getBeginHour() >= pf.getBeginHour()
					&& weekSlot.getEndHour() > pf.getEndHour()) {
				double duration = weekSlot.getDuration() - (pf.getEndHour() - weekSlot.getBeginHour());
				afterWeekSlots.put(week, new WeekSlot(pf.getEndHour(), weekSlot.getEndHour(), duration));
			} else if (weekSlot.getBeginHour() < pf.getBeginHour()
					&& weekSlot.getEndHour() > pf.getEndHour()) {
				double duration = Math.max(0, weekSlot.getDuration() - pf.getFeature().getDuration());
				beforeWeekSlots.put(week, new WeekSlot(weekSlot.getBeginHour(), pf.getBeginHour(), duration));
				afterWeekSlots.put(week, new WeekSlot(pf.getEndHour(), weekSlot.getEndHour(), duration));
			} 
			//else throw new Exception("Error in Scheduling algorithm - review slot redistribution");
			
		}
		slots.remove(i);
		if (!afterWeekSlots.isEmpty()) {
			slots.add(i, new Slot(afterWeekSlots, pf.getEndHour(), slot.getEndHour()));
		}
		if (!beforeWeekSlots.isEmpty()) {
			slots.add(i, new Slot(beforeWeekSlots, slot.getBeginHour(), pf.getBeginHour()));
		}
		//System.out.println("After scheduling:");
		//printSlots();
		
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
			if (slots.get(i).isFeatureFit(pf)) {
				return slots.get(i);
			}
			/*if (slots.get(i).getTotalDuration() >= pf.getFeature().getDuration()) {
				/*if (pf.getBeginHour() < slots.get(i).getBeginHour() ) {
					pf.setBeginHour(slots.get(i).getBeginHour());
					return slots.get(i);
				} else if (pf.getBeginHour() >= slots.get(i).getBeginHour() && pf.getEndHour() <= slots.get(i).getEndHour()) {
					return slots.get(i);
				}
				else ++i;
				
			}*/
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
		private HashMap<Integer, WeekSlot> weekSlots;
		private double beginHour;
		private double endHour;
		public Slot(HashMap<Integer, WeekSlot> weekSlots, double beginHour, double endHour) {
			this.weekSlots = weekSlots;
			this.beginHour = beginHour;
			this.endHour = endHour;
		}
		public boolean isFeatureFit(PlannedFeature pf) {
			if (pf.getBeginHour() >= endHour) return false;
			double leftTime = 0.0;
			for (WeekSlot ws : weekSlots.values()) {
				if (ws.getBeginHour() <= pf.getBeginHour() && ws.getEndHour() >= pf.getBeginHour()) {
					leftTime += Math.min(ws.getDuration(), ws.getEndHour() - pf.getBeginHour());
				}
				else if (pf.getBeginHour() < ws.getBeginHour()) {
					leftTime += ws.getDuration();
				}
			}
			//System.out.println(leftTime+ " leftDuration " + beginHour + " to " + endHour + " starting at " + pf.getBeginHour());
			return leftTime >= pf.getFeature().getDuration();
		}
		public double getTotalDuration() {
			double sum = 0.0;
			for (WeekSlot ws : weekSlots.values()) sum += ws.duration;
			return sum;
		}
		public HashMap<Integer, WeekSlot> getWeekSlots() {
			return weekSlots;
		}
		public double getBeginHour() {
			return beginHour;
		}
		public double getEndHour() {
			return endHour;
		}
    }
	private class WeekSlot {
		private double beginHour;
		private double endHour;
		private double duration;
		public WeekSlot(double beginHour, double endHour, double duration) {
			this.beginHour = beginHour;
			this.endHour = endHour;
			this.duration = duration;
		}
		public double getBeginHour() {
			return beginHour;
		}
		public double getEndHour() {
			return endHour;
		}
		public double getDuration() {
			return duration;
		}
	}
	
}

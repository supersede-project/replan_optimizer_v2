package entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Slot {
	
	private HashMap<Integer, WeekSlot> weekSlots;
	private double beginHour;
	private double endHour;
	
	public Slot(HashMap<Integer, WeekSlot> weekSlots) {
		this.weekSlots = weekSlots;
		
		List<Double> beginHours = new ArrayList<>();
		List<Double> endHours = new ArrayList<>();
		for (WeekSlot ws : weekSlots.values()) {
			beginHours.add(ws.getBeginHour());
			endHours.add(ws.getEndHour());
		}
		this.beginHour = Collections.min(beginHours);
		this.endHour = Collections.max(endHours);
	}
	public boolean isFeatureFit(PlannedFeature pf) {
		if (pf.getBeginHour() >= getEndHour()) return false;
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
		for (WeekSlot ws : weekSlots.values()) sum += ws.getDuration();
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

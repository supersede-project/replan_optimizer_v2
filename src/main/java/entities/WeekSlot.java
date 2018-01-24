package entities;

public class WeekSlot {
	
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

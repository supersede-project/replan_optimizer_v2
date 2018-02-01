package entities.parameters;

public class Objective {
	
	private int priority;
	private double value;
	
	public Objective(int priority, double value) {
		this.priority = priority;
		this.value = value;
	}
	
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}

}

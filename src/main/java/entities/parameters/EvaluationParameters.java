package entities.parameters;

public class EvaluationParameters {
	
	Objective end_date_quality;
	Objective distribution_quality;
	Objective completion_quality;
	Objective similarity_quality;
	Objective priority_quality;

	public EvaluationParameters() {
		configureDefaultHighPriorityObjectives();
		configureDefaultLowPriorityObjectives();
	}
	
	public EvaluationParameters(Objective endDateQuality, Objective completionQuality, Objective distributionQuality,
			Objective priorityQuality, Objective similarityQuality) {
		this.end_date_quality = endDateQuality;
		this.completion_quality = completionQuality;
		this.distribution_quality = distributionQuality;
		this.priority_quality = priorityQuality;
		this.similarity_quality = similarityQuality;
	}
	
	public Objective getEndDateQuality() {
		return end_date_quality;
	}

	public void setEndDateQuality(Objective endDateQuality) {
		this.end_date_quality = endDateQuality;
	}

	public Objective getDistributionQuality() {
		return distribution_quality;
	}

	public void setDistributionQuality(Objective distributionQuality) {
		this.distribution_quality = distributionQuality;
	}

	public Objective getCompletionQuality() {
		return completion_quality;
	}

	public void setCompletionQuality(Objective completionQuality) {
		this.completion_quality = completionQuality;
	}

	public Objective getSimilarityQuality() {
		return similarity_quality;
	}

	public void setSimilarityQuality(Objective similarityQuality) {
		this.similarity_quality = similarityQuality;
	}

	public Objective getPriorityQuality() {
		return priority_quality;
	}

	public void setPriorityQuality(Objective priorityQuality) {
		this.priority_quality = priorityQuality;
	}

	private void configureDefaultLowPriorityObjectives() {
		Objective endDateQuality = new Objective(1, 0.25);
		this.end_date_quality = endDateQuality;
		Objective distributionQuality = new Objective(1, 0.25);
		this.distribution_quality = distributionQuality;
		Objective completionQuality = new Objective(1, 0.25);
		this.completion_quality = completionQuality;
		Objective similarityQuality = new Objective(1, 0.25);
		this.similarity_quality = similarityQuality;
	}

	private void configureDefaultHighPriorityObjectives() {
		Objective priorityQuality = new Objective(0, 1.0);
		this.priority_quality = priorityQuality;
	}

	public double evaluate(double endDateQuality, double completionQuality, double distributionQuality,
			double priorityQuality, double similarityQuality) {
		double endDateObj = getValue(endDateQuality, end_date_quality);
		double completionObj = getValue(completionQuality, completion_quality);
		double distributionObj = getValue(distributionQuality, distribution_quality);
		double priorityObj = getValue(priorityQuality, priority_quality);
		double similarityObj = getValue(similarityQuality, similarity_quality);
		return endDateObj + completionObj + distributionObj + priorityObj + similarityObj;
	}
	
	private double getValue(double val, Objective obj) {
		return val * Math.min(obj.getValue() / Math.pow(10, obj.getPriority()*3), 1.0 - 1.0/ Math.pow(10, (obj.getPriority()+1)*3));
	}

}

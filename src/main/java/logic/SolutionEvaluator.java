package logic;

import entities.*;
import entities.parameters.EvaluationParameters;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kredes on 22/06/2017.
 */
public class SolutionEvaluator {

    private static SolutionEvaluator instance = new SolutionEvaluator();

    public static SolutionEvaluator getInstance() {
        return instance;
    }

    private SolutionEvaluator() {}


    /* --- OBJECTIVES --- */
    public double completionObjective(PlanningSolution solution) {
    	if (solution.getProblem().getFeatures().size() == 0) return 0.0;
        return 1.0 - (double) solution.getUndoneFeatures().size() / (double) (solution.getProblem().getFeatures().size());
    }

    public double endDateObjective(PlanningSolution solution) {
    	
    	if (solution.getPlannedFeatures().size() == 0) return 0.0;
    	
        NextReleaseProblem problem = solution.getProblem();
        double worstEndDate = problem.getNbWeeks() * problem.getNbHoursByWeek();

        //return solution.getPlannedFeatures().isEmpty() ? worstEndDate : solution.getEndDate();
        return 1.0 - Math.max(0.0,(solution.getEndDate() - solution.computeCriticalPath()) / (worstEndDate - solution.computeCriticalPath()));
    }
    
    public double distributionObjective(PlanningSolution solution) {
    	if (solution.getPlannedFeatures().size() == 0) return 0.0;
    	Map<Employee, Double> hoursPerEmployee = new HashMap<>();
        double totalHours = 0.0;
        //FIXME iterate over PF and update HoursPerEmployee
        for (Map.Entry<Employee, NewSchedule> entry : solution.getEmployeesPlanning().entrySet()) {
            Employee employee = entry.getKey();
            double aux = 0.0;
            for (PlannedFeature pf : entry.getValue().getPlannedFeatures()) {
            	aux += pf.getFeature().getDuration();
            }
            hoursPerEmployee.put(employee, aux);
        }
        
        double totalEmployees = solution.getProblem().getEmployees().size();
        //Calculates the standard deviation of the hours
        double expectedAvg = totalHours/totalEmployees;
        double sum = 0.0;
        for (Double nbHours : hoursPerEmployee.values()) {
            sum += Math.pow(Math.abs(nbHours - expectedAvg), 2);
        }
        double standardDeviation = Math.sqrt(sum/totalEmployees);
        
        //Normalizes the standard deviation
        double max = ((totalEmployees - 1.0) * Math.pow(expectedAvg, 2) + Math.pow(totalHours - expectedAvg, 2))/totalEmployees;
        double normalizedSd = max > 0 ? standardDeviation / max : 0;
        		
        return 1.0 - normalizedSd;
    }
    
    public double priorityObjective(PlanningSolution solution) {
        double score = worstScore(solution.getProblem());
    	return (score - solution.getPriorityScore()) / worstScore(solution.getProblem());
    }
    
    private double objectivePriorityRange = 0.999;
    
    /* --- NEW QUALITY --- */
    public double newQuality(PlanningSolution solution) {
        
        double endDateQuality = endDateObjective(solution);
                
        double completionQuality = completionObjective(solution);
        		
        double distributionQuality = distributionObjective(solution);
        
        double priorityQuality = priorityObjective(solution);
                        
/*        double qualityScore = (
        		endDateQuality*0.5 + 
        		completionQuality*0.0 + 
        		distributionQuality*0.5) / 
        		(double) Math.pow(10, COMPLETION_SCALE);
        
        double quality = priorityScore + qualityScore;*/
        
        EvaluationParameters evaluationParameters = solution.getProblem().getEvaluationParameters();
        int priorityLevels = evaluationParameters.getPriorityLevels();
        
        double quality = 0.0;
        
        for (int i = 0; i < priorityLevels; ++i) {
        	HashMap<Integer, Double> objectives = evaluationParameters.getObjectivesOfPriority(i);
        	double score = 0.0;
        	for (Integer objectiveIndex : objectives.keySet()) {
        		switch(objectiveIndex) {
        			case EvaluationParameters.completionQuality:
        				score += completionQuality * objectives.get(objectiveIndex);
        				break;
        			case EvaluationParameters.distributionQuality:
        				score += distributionQuality * objectives.get(objectiveIndex);
        				break;
        			case EvaluationParameters.endDateQuality:
        				score += endDateQuality * objectives.get(objectiveIndex);
        				break;
        			case EvaluationParameters.priorityQuality:
        				score += priorityQuality * objectives.get(objectiveIndex);
        				break;
    				default:
    					break;
        		}
        	}
        	double max = objectivePriorityRange / Math.pow(10, i*3);
        	quality += score * max;
        	//quality += score / Math.pow(10, i*3);
        }
        
        /*System.out.println("For " + solution.getPlannedFeatures().size() + " planned features:");
        System.out.println("End date " + endDateQuality);
        System.out.println("Completion " + completionQuality);
        System.out.println("Distirbution " + distributionQuality);
        System.out.println("Priority " + priorityScore);*/

        return quality;
    	
    }

	/* --- PRIVATE AUX --- */
    private double worstScore(NextReleaseProblem problem) {
        return problem.getFeatures().stream()
                .map(Feature::getPriority)
                .reduce(0.0, (sum, next) -> sum += next.getScore(), Double::sum);
    }
}
package logic;

import entities.*;

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
        for (Map.Entry<Employee, Schedule> entry : solution.getEmployeesPlanning().entrySet()) {
            Employee employee = entry.getKey();
            hoursPerEmployee.put(employee, 0.0);
            for (WeekSchedule week : entry.getValue()) {
                double aux = hoursPerEmployee.get(employee);
                for (PlannedFeature pf : week.getPlannedFeatures()) {
                    totalHours += pf.getFeature().getDuration();
                    aux += pf.getFeature().getDuration();
                }
                hoursPerEmployee.put(employee, aux);
            }
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


    /* --- OLD QUALITY --- */
    /*public double quality(PlanningSolution solution) {
        NextReleaseProblem problem = solution.getProblem();
        double worstEndDate = problem.getNbWeeks() * problem.getNbHoursByWeek();

        double unplannedFeatures = solution.getUndoneFeatures().size();
        double totalFeatures = problem.getFeatures().size();
        double penalty = worstEndDate/totalFeatures;
        //FIXME updated to avoid NaN solution answers
        double endDateQuality = totalFeatures > 0 ?
        		Math.max(0.0, 1.0 - (penalty * unplannedFeatures) / worstEndDate) : 0;
        //FIXME updated to avoid NaN solution answers
        double priorityQuality = worstScore(problem) > 0 ? 
        		1.0 - priorityObjective(solution) / worstScore(problem) : 0;
        double distributionQuality = 1.0 - distributionObjective(solution);
        //System.out.println(endDateQuality + " " + priorityQuality + " " + distributionQuality);
        return (endDateQuality*0.3 + priorityQuality*0.4 + distributionQuality*0.3);
    }*/
    
    private int COMPLETION_SCALE;
    
    /* --- NEW QUALITY --- */
    public double newQuality(PlanningSolution solution) {
        
        double endDateQuality = endDateObjective(solution);
                
        double completionQuality = completionObjective(solution);
        		
        double distributionQuality = distributionObjective(solution);
        
        double priorityScore = priorityScore(solution);
                        
        double qualityScore = (
        		endDateQuality*0.5 + 
//        		completionQuality*0.0 + 
        		distributionQuality*0.5) / 
        		(double) Math.pow(10, COMPLETION_SCALE);
        
        double quality = priorityScore + qualityScore;
        
        /*System.out.println("For " + solution.getPlannedFeatures().size() + " planned features:");
        System.out.println("End date " + endDateQuality);
        System.out.println("Completion " + completionQuality);
        System.out.println("Distirbution " + distributionQuality);
        System.out.println("Priority " + priorityScore);*/

        return quality;
    	
    }


    private double priorityScore(PlanningSolution solution) {
    	if (solution.getPlannedFeatures().size() == 0) return 0.0;
    	COMPLETION_SCALE = 0;
    	double aux = 0.9;
        double max = 0;
        int worstScore = (int) worstScore(solution.getProblem());
        while (worstScore > 0) {
        	max += aux;
        	aux /= 10;
        	worstScore /= 10;
        	++COMPLETION_SCALE;
        }
        double score = worstScore(solution.getProblem());
        return max * (score - solution.getPriorityScore()) / worstScore(solution.getProblem());
	}

	/* --- PRIVATE AUX --- */
    private double worstScore(NextReleaseProblem problem) {
        return problem.getFeatures().stream()
                .map(Feature::getPriority)
                .reduce(0.0, (sum, next) -> sum += next.getScore(), Double::sum);
    }
}
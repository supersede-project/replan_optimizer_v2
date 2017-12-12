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
    public double priorityObjective(PlanningSolution solution) {
        return solution.getPriorityScore();
    }

    public double endDateObjective(PlanningSolution solution) {
        NextReleaseProblem problem = solution.getProblem();
        double worstEndDate = problem.getNbWeeks() * problem.getNbHoursByWeek();

        return solution.getPlannedFeatures().isEmpty() ? worstEndDate : solution.getEndDate();
    }

    public double distributionObjective(PlanningSolution solution) {
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

        int nbEmployees = hoursPerEmployee.size();
        double expectedAvg = totalHours/nbEmployees;
        double result = 0.0;
        for (Double nbHours : hoursPerEmployee.values()) {
            double rating;
            if (nbHours <= expectedAvg)
                //FIXME updated to avoid NaN solution answers
                rating = expectedAvg > 0 ? nbHours * (1/expectedAvg) : 0;
            else
                rating = Math.max(0.0, 1.0 - (nbHours - expectedAvg) * (1/expectedAvg));

            result += rating/nbEmployees;
        }

        return 1.0 - result;    // Because the algorithm minimizes objectives
    }


    /* --- OLD QUALITY --- */
    public double quality(PlanningSolution solution) {
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
    }
    
    private final static int COMPLETION_SCALE = 2;
    
    /* --- NEW QUALITY --- */
    public double newQuality(PlanningSolution solution) {
    	NextReleaseProblem problem = solution.getProblem();
    	
    	double worstEndDate = problem.getNbWeeks() * problem.getNbHoursByWeek();
    	double unplannedFeatures = solution.getUndoneFeatures().size();
        double totalFeatures = problem.getFeatures().size();
        double penalty = worstEndDate/totalFeatures;
                
        double endDateQuality = totalFeatures > 0 ?
        		Math.max(0.0, 1.0 - (penalty * unplannedFeatures) / worstEndDate) : 0;
        double priorityQuality = worstScore(problem) > 0 ? 
        		1.0 - priorityObjective(solution) / worstScore(problem) : 0;
        double distributionQuality = 1.0 - distributionObjective(solution);
        
        double completionScore = computeCompletionScore(totalFeatures, unplannedFeatures);
        
        double qualityScore = (endDateQuality*0.3 + priorityQuality*0.4 + distributionQuality*0.3) / 
        		(double) Math.pow(10, COMPLETION_SCALE);
        
        double quality = completionScore + qualityScore;

        return quality;
    	
    }


    private double computeCompletionScore(double totalFeatures, double unplannedFeatures) {
    	double aux = 0.9;
        double max = 0;
        int nbFeatures = (int) totalFeatures;
        while (nbFeatures > 0) {
        	max += aux;
        	aux /= 10;
        	nbFeatures /= 10;
        }
        return max * (totalFeatures - unplannedFeatures) / totalFeatures;
	}

	/* --- PRIVATE AUX --- */
    private double worstScore(NextReleaseProblem problem) {
        return problem.getFeatures().stream()
                .map(Feature::getPriority)
                .reduce(0.0, (sum, next) -> sum += next.getScore(), Double::sum);
    }
}

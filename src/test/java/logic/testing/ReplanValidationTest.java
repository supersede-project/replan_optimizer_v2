package logic.testing;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import entities.Employee;
import entities.Feature;
import entities.PlannedFeature;
import entities.Skill;
import entities.WeekSchedule;
import logic.NextReleaseProblem;
import logic.PlanningSolution;
import logic.SolverNRP;
import logic.test.RandomThings;
import logic.test.Validator;

public class ReplanValidationTest {
	
	private static final Logger logger = LoggerFactory.getLogger(ReplanValidationTest.class);
	
	private static SolverNRP solver;
	private static RandomThings random;
    private static Validator validator;
    
    @BeforeClass
    public static void setUpBeforeClass() {
		logger.info("Set up...");
        solver = new SolverNRP(SolverNRP.AlgorithmType.NSGAII);
        random = new RandomThings();
        validator = new Validator();
        logger.info("NRP solver initialized with " + solver.getAlgorithmType() + " algorithm type");
    }
	
	@Test
	@Ignore
	public void averageReplanning() {
		List<Skill> skills = random.skillList(1);
		List<Employee> employees = random.employeeList(2);
        List<Feature> features = random.featureList(10);

        employees.get(0).getSkills().add(skills.get(0));
        employees.get(1).getSkills().add(skills.get(0));
        
        features.get(0).getRequiredSkills().add(skills.get(0));
        features.get(1).getRequiredSkills().add(skills.get(0));
        features.get(2).getRequiredSkills().add(skills.get(0));
        features.get(3).getRequiredSkills().add(skills.get(0));
        features.get(4).getRequiredSkills().add(skills.get(0));
        features.get(5).getRequiredSkills().add(skills.get(0));
        features.get(6).getRequiredSkills().add(skills.get(0));
        features.get(7).getRequiredSkills().add(skills.get(0));
        features.get(8).getRequiredSkills().add(skills.get(0));
        features.get(9).getRequiredSkills().add(skills.get(0));


        NextReleaseProblem problem = new NextReleaseProblem(features, employees, 5, 40.0);
        PlanningSolution solution = solver.executeNRP(problem);
        System.out.println(solution.toString());
        
        Employee newE = random.employee();
        newE.getSkills().add(skills.get(0));
        employees.add(newE);
        
        double replanHour = 30.0;
        
        System.out.println("Adding a new employee and replanning at " + replanHour);
        
        NextReleaseProblem replanProblem = new NextReleaseProblem(solution, features, employees, 5, 40.0, replanHour);
        PlanningSolution replanSolution = solver.executeNRP(replanProblem);
        System.out.println(replanSolution.toString());
        /*for (Employee e : replanSolution.getEmployeesPlanning().keySet()) {
        	System.out.println("Employee " + e.getName());
        	for (WeekSchedule w : replanSolution.getEmployeesPlanning().get(e).getAllWeeks()) {
        		System.out.println("From " + w.getBeginHour() + " to " + w.getEndHour() + " we do " + w.getPlannedFeatures().size() + " features");
        	}
        }*/
	}

	@Test
	public void replanWithFrozenFeature() {
		List<Skill> skills = random.skillList(1);
		List<Employee> employees = random.employeeList(2);
        List<Feature> features = random.featureList(10);
        
        employees.get(0).getSkills().add(skills.get(0));
        employees.get(1).getSkills().add(skills.get(0));
        
        features.get(0).getRequiredSkills().add(skills.get(0));
        features.get(1).getRequiredSkills().add(skills.get(0));
        features.get(2).getRequiredSkills().add(skills.get(0));
        features.get(3).getRequiredSkills().add(skills.get(0));
        features.get(4).getRequiredSkills().add(skills.get(0));
        features.get(5).getRequiredSkills().add(skills.get(0));
        features.get(6).getRequiredSkills().add(skills.get(0));
        features.get(7).getRequiredSkills().add(skills.get(0));
        features.get(8).getRequiredSkills().add(skills.get(0));
        features.get(9).getRequiredSkills().add(skills.get(0));
        
        NextReleaseProblem problem = new NextReleaseProblem(features, employees, 5, 40.0);
        PlanningSolution solution = solver.executeNRP(problem);
        System.out.println(solution.toString());
        
        Employee newE = random.employee();
        newE.getSkills().add(skills.get(0));
        employees.add(newE);
        newE.setWeekAvailability(20.0);
        
        List<Feature> smallFeatures = random.featureList(10);
        smallFeatures.get(0).getRequiredSkills().add(skills.get(0));
        smallFeatures.get(1).getRequiredSkills().add(skills.get(0));
        smallFeatures.get(2).getRequiredSkills().add(skills.get(0));
        smallFeatures.get(3).getRequiredSkills().add(skills.get(0));
        smallFeatures.get(4).getRequiredSkills().add(skills.get(0));
        smallFeatures.get(5).getRequiredSkills().add(skills.get(0));
        smallFeatures.get(6).getRequiredSkills().add(skills.get(0));
        smallFeatures.get(7).getRequiredSkills().add(skills.get(0));
        smallFeatures.get(8).getRequiredSkills().add(skills.get(0));
        smallFeatures.get(9).getRequiredSkills().add(skills.get(0));
        
        smallFeatures.get(0).setDuration(1.0);
        smallFeatures.get(1).setDuration(1.0);
        smallFeatures.get(2).setDuration(1.0);
        smallFeatures.get(3).setDuration(1.0);
        smallFeatures.get(4).setDuration(1.0);
        smallFeatures.get(5).setDuration(1.0);
        smallFeatures.get(6).setDuration(1.0);
        smallFeatures.get(7).setDuration(1.0);
        smallFeatures.get(8).setDuration(1.0);
        smallFeatures.get(9).setDuration(1.0);
        
        double replanHour = 20.0;
        PlannedFeature pf = solution.getPlannedFeature(solution.getPlannedFeatures().size()-1);
        PlannedFeature pf2 = solution.getPlannedFeature(solution.getPlannedFeatures().size()-3);
        pf.setFrozen(true);
        pf2.setFrozen(true);
        System.out.println("Feature " + pf.getFeature().getName() + " and " + pf2.getFeature().getName() + " are now frozen");
        System.out.println("Performing replanning at " + replanHour + "\n");
        features.addAll(smallFeatures);
        
        NextReleaseProblem replanProblem = new NextReleaseProblem(solution, features, employees, 5, 40.0, replanHour);
        PlanningSolution replanSolution = solver.executeNRP(replanProblem);
        System.out.println(replanSolution.toString());
        
	}
	
}

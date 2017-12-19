package logic.testing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Assert;
import org.junit.BeforeClass;
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
import logic.SolutionQuality;
import logic.SolverNRP;
import logic.analytics.Analytics;
import logic.test.RandomThings;
import logic.test.Validator;

public class ValidatePlanningSolutionTest {
	
	private static final Logger logger = LoggerFactory.getLogger(ValidatePlanningSolutionTest.class);
	
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
	public void testWrapper() {
		logger.info("1. Planning solution should be empty");
		emptyPlanningSolutions();
		logger.info("2. EndTime should be the criticalPath");
		maxPathPlanningSolutions();
		logger.info("3. Constraints and rules");
		constraintsPlanningSolutions();
	}

	/**
	 * Wraps all test UCs in which the PlanningSolution should include the critical path
	 */
	private void maxPathPlanningSolutions() {
		maxPathManyFeaturesToOneEmployee();
		maxPathOneFeatureToEachEmployee();
		maxPathWithoutEnoughTime();
	}

	/**
	 * Wraps all test UCs in which the Planning Solution should always be empty
	 */
	private void emptyPlanningSolutions() {
		emptyPlanIfNoSkilledResource();
		emptyPlanIfNoEmployees();
		emptyPlanIfNoFeatures();
		//emptyPlanIfFeatureIsSelfDependent();
		//emptyPlanIfFeatureDependenciesCauseDeadlock();
		emptyPlanIfNoTime();
	}
	
	/**
	 * Wraps all test UCs to check if constraints and PS rules are respected
	 */
	private void constraintsPlanningSolutions() {
		featurePrecedencesAreRespected();
		featureWithNoRequiredSkillsCanBeDoneByAnSkilledEmployee();
		featureWithRequiredSkillsCanBeDoneOnlyByTheSkilledEmployee();
		noOverlappedJobs();
		endHourMinusBeginHourEqualsDuration();
		differentSkillDependentFeatures();
	}

	private void differentSkillDependentFeatures() {
		List<Skill> skills = random.skillList(2);
		List<Employee> employees = random.employeeList(2);
        List<Feature> features = random.featureList(2);

        employees.get(0).getSkills().add(skills.get(0));
        employees.get(1).getSkills().add(skills.get(1));
        
        features.get(0).getRequiredSkills().add(skills.get(0));
        features.get(1).getRequiredSkills().add(skills.get(1));

        features.get(1).getPreviousFeatures().add(features.get(0));

        NextReleaseProblem problem = new NextReleaseProblem(features, employees, 5, 40.0);
        PlanningSolution solution = solver.executeNRP(problem);
        // System.out.println(problem.toString());
        // System.out.println(solution.toString());
        /*for (Employee e : solution.getEmployeesPlanning().keySet()) {
        	System.out.println(e.getName() + " ");
        	for (WeekSchedule w : solution.getEmployeesPlanning().get(e).getAllWeeks()) {
        		System.out.println(w.getBeginHour() + " to " + w.getEndHour() + " with " + w.getRemainingHours());
        	}
        }*/

	}

	/**
	 * Executes a NRP with no features and
	 * checks if the PlanningSolution is empty as expected
	 */
	private void emptyPlanIfNoFeatures() {
		logger.info("\tNRP with no features");
		Skill s1 = random.skill();
		Employee e1 = random.employee();
        List<Feature> features = new ArrayList<>();

        e1.getSkills().add(s1);

        NextReleaseProblem problem = new NextReleaseProblem(features, asList(e1), 3, 40.0);
        PlanningSolution solution = solver.executeNRP(problem);

        Assert.assertTrue(solution.getPlannedFeatures().isEmpty());
	}

	/**
	 * Executes a NRP with no employees and
	 * checks if the PlanningSolution is empty as expected
	 */
	private void emptyPlanIfNoEmployees() {
		logger.info("\tNRP with no employees");
		Skill s1 = random.skill();
		List<Employee> employees = new ArrayList<>();
		Feature f1 = random.feature();

      	f1.getRequiredSkills().add(s1);

        NextReleaseProblem problem = new NextReleaseProblem(asList(f1), employees, 3, 40.0);
        PlanningSolution solution = solver.executeNRP(problem);

        Assert.assertTrue(solution.getPlannedFeatures().isEmpty());
	}

	/**
	 * Executes a NRP with no skilled resources (i.e. with
	 * no employees with the required skills) and checks if the 
	 * PlanningSolution is empty as expected
	 */
	private void emptyPlanIfNoSkilledResource() {
		logger.info("\tNRP with no skilled resources");
		 List<Skill> skills = random.skillList(2);
        Feature f = random.feature();
        Employee e = random.employee();

        f.getRequiredSkills().addAll(skills);
        e.getSkills().add(skills.get(0));

        NextReleaseProblem problem = new NextReleaseProblem(asList(f), asList(e), 3, 40.0);
        PlanningSolution solution = solver.executeNRP(problem);

        Assert.assertTrue(solution.getPlannedFeatures().isEmpty());
        validator.validateSkills(solution);
	}

	/**
	 * Executes a NRP with a dependency deadlock (i.e. a
	 * cyclic feature dependency chain) and checks if the PlanningSolution
	 * is empty as expected
	 */
	private void emptyPlanIfFeatureDependenciesCauseDeadlock() {
		logger.info("\tNRP with a feature dependency deadlock");
		Skill s1 = random.skill();
        List<Feature> features = random.featureList(2);
        List<Employee> employees = random.employeeList(2);

        Feature f0 = features.get(0);
        Feature f1 = features.get(1);

        f0.getRequiredSkills().add(s1);
        f1.getRequiredSkills().add(s1);

        employees.get(0).getSkills().add(s1);
        employees.get(1).getSkills().add(s1);

        f0.getPreviousFeatures().add(f1);
        f1.getPreviousFeatures().add(f0);

        NextReleaseProblem problem = new NextReleaseProblem(features, employees, 3, 40.0);
        PlanningSolution solution = solver.executeNRP(problem);

        Assert.assertTrue(solution.getPlannedFeatures().isEmpty());
	}

	/**
	 * Executes a NRP with a self-dependent feature and
	 * checks if the PlanningSolution is empty as expected
	 */
	private void emptyPlanIfFeatureIsSelfDependent() {
		logger.info("\tNRP with a self-dependent feature");
		Skill s1 = random.skill();
        Feature f1 = random.feature();
        Employee e1 = random.employee();

        e1.getSkills().add(s1);

        f1.getRequiredSkills().add(s1);

        f1.getPreviousFeatures().add(f1);

        NextReleaseProblem problem = new NextReleaseProblem(asList(f1), asList(e1), 3, 40.0);
        PlanningSolution solution = solver.executeNRP(problem);

        Assert.assertTrue(solution.getPlannedFeatures().isEmpty());
	}
	
	/**
	 * Executes a NRP with not enough time to plan any
	 * feature and checks if the PlanningSolution is empty as expected
	 */
	private void emptyPlanIfNoTime() {
		logger.info("\tNRP with not enough time");
		Skill s1 = random.skill();
        Feature f1 = random.feature();
        Employee e1 = random.employee();

        e1.getSkills().add(s1);
        e1.setWeekAvailability(24.9);

        f1.getRequiredSkills().add(s1);
        f1.setDuration(50.0);

        NextReleaseProblem problem = new NextReleaseProblem(asList(f1), asList(e1), 2, 24.9);
        PlanningSolution solution = solver.executeNRP(problem);

        Assert.assertTrue(solution.getPlannedFeatures().isEmpty());
	}
	
	/**
	 *  Executes a NRP with N features and 1 employee
	 *  and checks that all features are done in the correct amount of time
	 */
	private void maxPathManyFeaturesToOneEmployee() {
		logger.info("\tNRP with many features and 1 employee");
		Skill s1 = random.skill();
        List<Feature> features = random.featureList(5);
        Employee e1 = random.employee();

        e1.getSkills().add(s1);
        double duration = 0.0;
        for (Feature f : features) {
        	f.getRequiredSkills().add(s1);
        	duration += f.getDuration();
        }
        
        NextReleaseProblem problem = new NextReleaseProblem(features, asList(e1), 5, 40);
        PlanningSolution solution = solver.executeNRP(problem);

        Assert.assertTrue(solution.getPlannedFeatures().size() == features.size());
        Assert.assertEquals(solution.getEndDate(), duration, 0.0);
	}
	
	/**
	 *  Executes a NRP with equal number of features and 
	 *  skilled employees and checks that endTime is the longest feature
	 */
	private void maxPathOneFeatureToEachEmployee() {
		logger.info("\tNRP with N features and N skilled employees");
		Skill s1 = random.skill();
        List<Feature> features = random.featureList(5);
        List<Employee> employees = random.employeeList(features.size());

        double max = 0.0;
        
        for (int i = 0; i < features.size(); ++i) {
        	max = Math.max(max, features.get(i).getDuration());
        	features.get(i).getRequiredSkills().add(s1);
        	employees.get(i).getSkills().add(s1);
        }
        
        NextReleaseProblem problem = new NextReleaseProblem(features, employees, 1, 40);
        PlanningSolution solution = solver.executeNRP(problem);

        Assert.assertTrue(solution.getPlannedFeatures().size() == features.size());
        Assert.assertEquals(solution.getEndDate(), max, 0.0);
	}
	
	/**
	 * 	Executes a NRP with not enough time to plan all features
	 * 	and checks no other feature could be planned
	 */
	private void maxPathWithoutEnoughTime() {
		logger.info("\tNRP with N features, 1 employee but not enough time");
		Skill s1 = random.skill();
        List<Feature> features = random.featureList(5);
        Employee e1 = random.employee();
        
        double duration = 0.0;
        
        for (Feature f : features) {
        	f.getRequiredSkills().add(s1);
        	duration += f.getDuration();
        }
        
        e1.getSkills().add(s1);
        
        double maxEndDate = duration/2;
        int nbWeeks = Math.max((int)(maxEndDate/e1.getWeekAvailability()), 1);
                
        NextReleaseProblem problem = new NextReleaseProblem(features, asList(e1), nbWeeks,e1.getWeekAvailability());
        PlanningSolution solution = solver.executeNRP(problem);

        Assert.assertTrue(solution.getPlannedFeatures().size() < features.size());
        
        List<Feature> plannedFeatures = new ArrayList<>();
        for (PlannedFeature pf : solution.getPlannedFeatures()) plannedFeatures.add(pf.getFeature());
        features.removeAll(plannedFeatures);
        
        //Check what would happen if planned
        for (Feature f : features) {        	
        	Assert.assertTrue(f.getDuration() > (nbWeeks*e1.getWeekAvailability() - solution.getEndDate()));
        }
        
	}
	
	/**
	 * Executes a NRP with feature dependencies and checks if 
	 * precedences are respected
	 */
	private void featurePrecedencesAreRespected() {
		double nbHoursPerWeek = 40.0;
        Skill s1 = random.skill();
        List<Feature> features = random.featureList(20);
        List<Employee> employees = random.employeeList(1);

        employees.get(0).getSkills().add(s1);

        for (int i = 0; i < features.size(); ++i) features.get(i).getRequiredSkills().add(s1);
        for (int i = 1; i < features.size(); ++i) features.get(i).getPreviousFeatures().add(features.get(i-1));

        NextReleaseProblem problem = new NextReleaseProblem(features, employees, getTotalRequiredWeeks(features, nbHoursPerWeek), nbHoursPerWeek);
        PlanningSolution solution = solver.executeNRP(problem);

        SolutionQuality s = new SolutionQuality();
        Assert.assertEquals((double) s.getAttribute(solution), 1.0, 0);
        validator.validateDependencies(solution);
	}
	
	/**
	 * Executes a NRP with a feature without required skill and check
	 * that it's done by a skilled employee
	 */
    public void featureWithNoRequiredSkillsCanBeDoneByAnSkilledEmployee() {
        Feature f = random.feature();
        Employee e = random.employee();
        Skill s = random.skill();

        e.getSkills().add(s);

        NextReleaseProblem problem = new NextReleaseProblem(asList(f), asList(e), 4, 40.0);
        PlanningSolution solution = solver.executeNRP(problem);

        List<PlannedFeature> jobs = solution.getPlannedFeatures();
        PlannedFeature pf = jobs.get(0);

        Assert.assertTrue(jobs.size() == 1 && pf.getFeature().equals(f) && pf.getEmployee().equals(e));
    }

    /**
     * Executes a NRP with a feature without required skill and check
	 * that it's done by a non-skilled employee
     */
    public void featureWithNoRequiredSkillsCanBeDoneByANonSkilledEmployee() {
        Feature f = random.feature();
        Employee e = random.employee();
        NextReleaseProblem problem = new NextReleaseProblem(asList(f), asList(e), 4, 40.0);
        PlanningSolution solution = solver.executeNRP(problem);

        List<PlannedFeature> jobs = solution.getPlannedFeatures();
        PlannedFeature pf = jobs.get(0);

        Assert.assertTrue(jobs.size() == 1 && pf.getFeature().equals(f) && pf.getEmployee().equals(e));
    }

    /**
     * Executes a NRP with a feature with required skills and check that
     * it is always done by the skilled employee
     */
    public void featureWithRequiredSkillsCanBeDoneOnlyByTheSkilledEmployee() {
        List<Skill> skills = random.skillList(2);
        List<Feature> features = random.featureList(1);
        List<Employee> employees = random.employeeList(2);

        // 1 employee with 1 skill
        employees.get(0).getSkills().add(skills.get(0));

        // 1 employee with 2 skills
        employees.get(1).getSkills().add(skills.get(0));
        employees.get(1).getSkills().add(skills.get(1));

        // 1 feature requires 2 skills
        features.get(0).getRequiredSkills().add(skills.get(0));
        features.get(0).getRequiredSkills().add(skills.get(1));

        NextReleaseProblem problem = new NextReleaseProblem(features, employees, 4, 40.0);
        PlanningSolution solution = solver.executeNRP(problem);

        Assert.assertTrue(solution.getPlannedFeatures().size() == 1 && // is planned
                solution.getPlannedFeatures().get(0).getEmployee().equals(employees.get(1))); // and done by the skilled employee
    }

    public void noOverlappedJobs() {
        List<Skill> skills = random.skillList(3);
        List<Feature> features = random.featureList(5);
        List<Employee> employees = random.employeeList(3);

        random.mix(features, skills, employees);

        NextReleaseProblem problem = new NextReleaseProblem(features, employees, 5, 40.0);
        PlanningSolution solution = solver.executeNRP(problem);

        validator.validateNoOverlappedJobs(solution);
    }

    public void endHourMinusBeginHourEqualsDuration() {
        List<Skill> skills = random.skillList(6);
        List<Feature> features = random.featureList(14);
        List<Employee> employees = random.employeeList(4);

        random.mix(features, skills, employees);

        NextReleaseProblem problem = new NextReleaseProblem(features, employees, 3, 40.0);
        PlanningSolution solution = solver.executeNRP(problem);

        validator.validateAll(solution);

        for (PlannedFeature pf : solution.getPlannedFeatures())
            Assert.assertTrue(
                    "endHour - beginHour != feature.duration for plannedFeature " + pf.toString(),
                    pf.getEndHour() - pf.getBeginHour() == pf.getFeature().getDuration()
            );
    }
	
	private <T> List<T> asList(T... elements) {
        return Arrays.asList(elements);
    }
	
	private int getTotalRequiredWeeks(List<Feature> features, double nbHoursPerWeek) {
	     return (int) Math.ceil(getTotalHours(features)/nbHoursPerWeek);
	}
	
	private double getTotalHours(List<Feature> features) {
		 double nbHours = 0;
	     for (int i = 0; i < features.size(); ++i) nbHours += features.get(i).getDuration();
	     return nbHours;
	}

}

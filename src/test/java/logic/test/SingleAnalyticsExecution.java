package logic.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import entities.Employee;
import entities.Feature;
import entities.Skill;
import logic.NextReleaseProblem;
import logic.PlanningSolution;
import logic.SolutionQuality;
import logic.SolverNRP;
import logic.analytics.Analytics;

public class SingleAnalyticsExecution {
	
	private static SolverNRP solver;
	private static RandomThings random;
    private static Validator validator;
	
	@BeforeClass
    public static void setUpBeforeClass() {
        solver = new SolverNRP(SolverNRP.AlgorithmType.NSGAII);
        random = new RandomThings();
        validator = new Validator();
    }
	
	@Test
	public void test() {
		realistic();
	}

	private void realistic() {
		List<Skill> skills = random.skillList(5);
        List<Feature> features = random.featureList(20);
        List<Employee> employees = random.employeeList(4);

        // resource skills
        employees.get(0).getSkills().add(skills.get(0));
        employees.get(0).getSkills().add(skills.get(3));

        employees.get(1).getSkills().add(skills.get(0));
        employees.get(1).getSkills().add(skills.get(1));
        employees.get(1).getSkills().add(skills.get(3));

        employees.get(2).getSkills().add(skills.get(0));
        employees.get(2).getSkills().add(skills.get(1));
        employees.get(2).getSkills().add(skills.get(2));

        employees.get(3).getSkills().add(skills.get(2));
        employees.get(3).getSkills().add(skills.get(4));
        employees.get(3).getSkills().add(skills.get(3));

        employees.get(3).setWeekAvailability(1.0);

        // dependencies
        //features.get(3).getPreviousFeatures().add(features.get(0));
        //features.get(3).getPreviousFeatures().add(features.get(1));

        features.get(7).getPreviousFeatures().add(features.get(9));

        features.get(10).getPreviousFeatures().add(features.get(2));

        features.get(11).getPreviousFeatures().add(features.get(7));

        features.get(16).getPreviousFeatures().add(features.get(10));

        features.get(19).getPreviousFeatures().add(features.get(16));
        
        features.get(19).getPreviousFeatures().add(features.get(11));

        features.get(3).setDuration(4.0);


        // required skills by features
        features.get(0).getRequiredSkills().add(skills.get(0));
        features.get(0).getRequiredSkills().add(skills.get(1));

        features.get(1).getRequiredSkills().add(skills.get(2));

        features.get(2).getRequiredSkills().add(skills.get(3));

        features.get(3).getRequiredSkills().add(skills.get(3));
        features.get(3).getRequiredSkills().add(skills.get(4));

        features.get(4).getRequiredSkills().add(skills.get(0));

        features.get(5).getRequiredSkills().add(skills.get(0));
        features.get(5).getRequiredSkills().add(skills.get(1));

        features.get(6).getRequiredSkills().add(skills.get(0));
        features.get(6).getRequiredSkills().add(skills.get(3));

        features.get(7).getRequiredSkills().add(skills.get(0));

        features.get(8).getRequiredSkills().add(skills.get(1));

        features.get(9).getRequiredSkills().add(skills.get(0));

        features.get(10).getRequiredSkills().add(skills.get(3));

        features.get(11).getRequiredSkills().add(skills.get(1));
        features.get(11).getRequiredSkills().add(skills.get(3));

        features.get(12).getRequiredSkills().add(skills.get(2));
        features.get(12).getRequiredSkills().add(skills.get(4));

        features.get(13).getRequiredSkills().add(skills.get(0));

        features.get(14).getRequiredSkills().add(skills.get(1));

        features.get(15).getRequiredSkills().add(skills.get(0));

        features.get(16).getRequiredSkills().add(skills.get(3));

        features.get(17).getRequiredSkills().add(skills.get(0));

        features.get(18).getRequiredSkills().add(skills.get(3));

        features.get(19).getRequiredSkills().add(skills.get(0));
        features.get(19).getRequiredSkills().add(skills.get(3));

        features.get(19).getRequiredSkills().add(new Skill("No one has this skill"));


        for (int i = 0; i < 10; ++i) {
            NextReleaseProblem problem = new NextReleaseProblem(features, employees, 4, 40.0);
            PlanningSolution solution = solver.executeNRP(problem);

            validator.validateAll(solution);

            Analytics analytics = new Analytics(solution);
            problem.evaluate(solution);
            SolutionQuality s = new SolutionQuality();
            double d = (double) s.getAttribute(solution);
            System.out.println(String.format("After posprocessing: %1$,.6f", d));
            //for (String s : analytics.getInfo()) System.out.println(s);
            solutionToDataFile(solution);
        }
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
	
	private void solutionToDataFile(PlanningSolution solution) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");

        String base = "src/test/data";
        String filename = String.format("%s_%s", solver.getAlgorithmType().getName(),
                dateFormat.format(Calendar.getInstance().getTime()));
        String fullPath = String.format("%s/%s.txt", base, filename);

        File f = new File(fullPath);
        f.getParentFile().mkdirs();

        try {
            Files.write(f.toPath(), solution.toR().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
}

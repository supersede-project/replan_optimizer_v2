package logic.testing;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import entities.Employee;
import entities.Feature;
import entities.Skill;
import io.swagger.model.ApiPlanningSolution;
import logic.NextReleaseProblem;
import logic.PlanningSolution;
import logic.SolverNRP;
import logic.test.RandomThings;

public class ParameterizedNRPTest {
	
	private final static Logger logger = LoggerFactory.getLogger(ParameterizedNRPTest.class);
		
	private int nbFeatures;					//number of features
	private int nbSkills;					//number of skills
	private int nbEmployees;				//number of employees
	
	private int nbWeeks;					//number of weeks
	private double nbHoursPerWeek;			//number of hours per week
	
	private double dependencyRate;			//probability of a feature to be dependent of another one
	private double[] skillEmployeeRates; 	//proportion of employees with the assigned skills
	private double[] skillFeatureRates;		//proportion of features with the required skills
	private double[] availabilityRates; 	//proportion of availability w.r.t. number of hours per week for employees
	
	@Before
	public void before() {
		nbFeatures = 30;
		nbSkills = 3;
		nbEmployees = 4;
		
		nbWeeks = 5;
		nbHoursPerWeek = 40.0;
		
		dependencyRate = 0.30;
		availabilityRates = new double[]{0.5,0.8,1.0,1.0};
		skillEmployeeRates = new double[]{0.2,0.5,0.8};
		skillFeatureRates = new double[]{0.2,0.5,0.8};
		
	}

	@Test
	public void parameterizedNRPExample() throws Exception {
		
		ParameterizedNRP nrpParam = new ParameterizedNRP(nbFeatures, nbSkills, nbEmployees, nbWeeks, nbHoursPerWeek, dependencyRate,
        		availabilityRates, skillEmployeeRates, skillFeatureRates);

		NextReleaseProblem nrp = nrpParam.createNRP();
		
		System.out.println("***NEXT RELEASE PROBLEM***");
		System.out.println(nrp.toString());
		
		SolverNRP solver = new SolverNRP(SolverNRP.AlgorithmType.NSGAII);
		for (int i = 0; i < 1; ++i) {
			PlanningSolution solution = solver.executeNRP(nrp);
			System.out.println("***BEST SOLUTION FOUND***");
			System.out.println(solution.toString());
			
			ApiPlanningSolution replan = new ApiPlanningSolution(solution);
			/*NextReleaseProblem replanProblem = new NextReleaseProblem(nrp.getFeatures(), nrp.getEmployees(), 
					nbWeeks, nbHoursPerWeek, replan);
			PlanningSolution replanSolution = solver.executeNRP(replanProblem);
			System.out.println("***REPLANNING 1***");
			System.out.println(replanSolution.toString());
			
			ApiPlanningSolution replan2 = new ApiPlanningSolution(replanSolution);
			NextReleaseProblem replanProblem2 = new NextReleaseProblem(nrp.getFeatures(), nrp.getEmployees(), 
					nbWeeks, nbHoursPerWeek, replan2);
			PlanningSolution replanSolution2 = solver.executeNRP(replanProblem2);
			System.out.println("***REPLANNING 2***");
			System.out.println(replanSolution2.toString());*/
			
		}
		
	}
	
}

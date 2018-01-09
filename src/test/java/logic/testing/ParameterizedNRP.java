package logic.testing;

import java.util.List;

import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import entities.Employee;
import entities.Feature;
import entities.Skill;
import logic.NextReleaseProblem;
import logic.test.RandomThings;

public class ParameterizedNRP {
	
    private static JMetalRandom jRandom;
	private static RandomThings random;
	
	private int nbFeatures;					//number of features
	private int nbSkills;					//number of skills
	private int nbEmployees;				//number of employees
	
	private int nbWeeks;					//number of weeks
	private double nbHoursPerWeek;			//number of hours per week
	
	private double dependencyRate;			//probability of a feature to be dependent of another one
	private double[] skillEmployeeRates; 	//proportion of employees with the assigned skills
	private double[] skillFeatureRates;		//proportion of features with the required skills
	private double[] availabilityRates; 	//proportion of availability w.r.t. number of hours per week for employees
	
	public ParameterizedNRP(int nbFeatures, int nbSkills, int nbEmployees, int nbWeeks,
			double nbHoursPerWeek, double dependencyRate, double[] availabilityRates, double[] skillEmployeeRates,
			double[] skillFeatureRates) throws Exception {
		
		jRandom = JMetalRandom.getInstance();
		random = new RandomThings();
		
		this.nbFeatures = nbFeatures;
		this.nbSkills = nbSkills;
		this.nbEmployees = nbEmployees;
		
		this.nbWeeks = nbWeeks;
		this.nbHoursPerWeek = nbHoursPerWeek;
		
		this.dependencyRate = dependencyRate;
		this.skillEmployeeRates = skillEmployeeRates;
		this.skillFeatureRates = skillFeatureRates;
		this.availabilityRates = availabilityRates;
		
		checkErrors();

	}
	
	public NextReleaseProblem createNRP() {
		List<Feature> features = random.featureList(nbFeatures);
		List<Skill> skills = random.skillList(nbSkills);
		List<Employee> employees = random.employeeList(nbEmployees);
		
		for (int i = 1; i < nbFeatures; ++i) {
			if (jRandom.nextDouble(0, 1.0) <= dependencyRate) 
				features.get(i).getPreviousFeatures().add(features.get(i-1));
		}
		
		for (int i = 0; i < nbEmployees; ++i) {
			Employee e = employees.get(i);
			e.setWeekAvailability(availabilityRates[i] * nbHoursPerWeek);
			
			for (int j = 0; j < nbSkills; ++j) {
				if (jRandom.nextDouble(0,1.0) <= skillEmployeeRates[j]) 
					e.getSkills().add(skills.get(j));
			}
			if (e.getSkills().size() == 0){
				int index = findMaxProbable(skillEmployeeRates);
				e.getSkills().add(skills.get(index));
			}
		}
		
		for (int i = 0; i < nbFeatures; ++i) {
			Feature f = features.get(i);
			for (int j = 0; j < nbSkills; ++j) {
				if (jRandom.nextDouble(0,1.0) <= skillFeatureRates[j]) 
					f.getRequiredSkills().add(skills.get(j));
			}
			if (f.getRequiredSkills().size() == 0){
				int index = findMaxProbable(skillFeatureRates);
				f.getRequiredSkills().add(skills.get(index));
			}
		}

		return new NextReleaseProblem(features, employees, nbWeeks, nbHoursPerWeek);
	}
	
	private int findMaxProbable(double[] rates) {
		double max = rates[0];
		int item = 0;
		for (int j = 1; j < rates.length; ++j) {
			if (max < rates[j]) {
				max = rates[j];
				item = j;
			}
		}
		return item;
	}
	
	private void checkErrors() throws Exception {
		if (nbFeatures <= 0) 
			throw new Exception("Number of features must be positive");
		if (nbSkills <= 0) 
			throw new Exception("Number of skills must be positive");
		if (nbEmployees <= 0) 
			throw new Exception("Number of employees must be positive");
		if (nbWeeks <= 0) 
			throw new Exception("Number of weeks must be positive");
		if (nbHoursPerWeek <= 0) 
			throw new Exception("Number of hours per week must be positive");
		if (dependencyRate < 0 || dependencyRate > 1) 
			throw new Exception("Dependency rate must be normalized between 0 and 1");
		if (availabilityRates.length != nbEmployees) 
			throw new Exception ("Availability rate list length must be equal to nb of employees");
		for (double d : availabilityRates) if (d < 0 || d > 1) 
			throw new Exception("Availability rates must be normalized between 0 and 1");
		if (skillEmployeeRates.length != nbSkills) 
			throw new Exception("Skill-employee rate list lenght must be equal to nb of skills");
		for (double d : skillEmployeeRates) if (d < 0 || d > 1) 
			throw new Exception("Skill-employee rates must be normalized between 0 and 1");
		if (skillFeatureRates.length != nbSkills)
			throw new Exception("Skill-feature rate list length must be equal to nb of skills");
		for (double d : skillFeatureRates) if (d < 0 || d > 1)
			throw new Exception("Skill-feature rates must be normalized between 0 and 1");
	}

}

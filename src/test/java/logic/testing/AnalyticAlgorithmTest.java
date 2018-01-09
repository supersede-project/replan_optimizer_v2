package logic.testing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.qualityindicator.impl.Epsilon;
import org.uma.jmetal.qualityindicator.impl.GenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistancePlus;
import org.uma.jmetal.qualityindicator.impl.Spread;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentBuilder;
import org.uma.jmetal.util.experiment.component.ComputeQualityIndicators;
import org.uma.jmetal.util.experiment.component.ExecuteAlgorithms;
import org.uma.jmetal.util.experiment.component.GenerateBoxplotsWithR;
import org.uma.jmetal.util.experiment.component.GenerateLatexTablesWithStatistics;
import org.uma.jmetal.util.experiment.component.GenerateReferenceParetoFront;
import org.uma.jmetal.util.experiment.component.GenerateReferenceParetoSetAndFrontFromDoubleSolutions;
import org.uma.jmetal.util.experiment.component.GenerateWilcoxonTestTablesWithR;
import org.uma.jmetal.util.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.util.experiment.util.ExperimentProblem;

import entities.parameters.AlgorithmParameters;
import logic.NextReleaseProblem;
import logic.PlanningSolution;
import logic.comparators.PlanningSolutionDominanceComparator;
import logic.operators.PlanningCrossoverOperator;
import logic.operators.PlanningMutationOperator;

public class AnalyticAlgorithmTest {

	private static final Logger logger = LoggerFactory.getLogger(ValidatePlanningSolutionTest.class);
		
	private static final String experimentBaseDirectory = ".";
	
    private static final int INDEPENDENT_RUNS = 25 ;
    
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
    public void setUpBeforeClass() {
		nbFeatures = 30;
		nbSkills = 3;
		nbEmployees = 5;
		
		nbWeeks = 4;
		nbHoursPerWeek = 40.0;
		
		dependencyRate = 0.25;
		availabilityRates = new double[]{0.5,0.5,0.8,1.0,1.0};
		skillEmployeeRates = new double[]{0.2,0.4,0.8};
		skillFeatureRates = new double[]{0.1,0.4,0.6};
    }
	
	@Test
	@Ignore
    public void analyticTest() throws Exception {
		
		// Create an instance for parameterized NRP generation
		ParameterizedNRP nrpParam = new ParameterizedNRP(nbFeatures, nbSkills, nbEmployees, nbWeeks, nbHoursPerWeek, dependencyRate,
        		availabilityRates, skillEmployeeRates, skillFeatureRates);
		
		// Create a list of 5 experiments with 5 different NRP with same parameters
		List<ExperimentProblem<PlanningSolution>> problemList = new ArrayList<>();
		for (int i = 0; i < 5; ++i) {
			NextReleaseProblem nrp = nrpParam.createNRP();
			nrp.setTag("NRP-" + (i+1));
			problemList.add(new ExperimentProblem<>(nrp));
		}
	    
	    // Create list of algorithm experiments
	    List<ExperimentAlgorithm<PlanningSolution, List<PlanningSolution>>> algorithmList =
	    		configureAlgorithmList(problemList);
	    
	    // Create experiment
	    Experiment<PlanningSolution, List<PlanningSolution>> experiment =
	    		new ExperimentBuilder<PlanningSolution, List<PlanningSolution>>("NSGAIIStudy")
	    			.setAlgorithmList(algorithmList)
	    			.setProblemList(problemList)
	    			.setExperimentBaseDirectory(experimentBaseDirectory)
	    			.setOutputParetoFrontFileName("FUN")
	    			.setOutputParetoSetFileName("VAR")
	    			.setReferenceFrontDirectory(experimentBaseDirectory + "/referenceFronts")
	    			.setIndicatorList(Arrays.asList(
	    					new Epsilon<PlanningSolution>(), 
	    					new Spread<PlanningSolution>(),
	    					new GenerationalDistance<PlanningSolution>(),
	    					new PISAHypervolume<PlanningSolution>(),
	    					new InvertedGenerationalDistance<PlanningSolution>(),
	    					new InvertedGenerationalDistancePlus<PlanningSolution>()))
	    			.setIndependentRuns(INDEPENDENT_RUNS)
	    			.setNumberOfCores(8)
	    			.build();
	    
	    new ExecuteAlgorithms<>(experiment).run();
	    new GenerateReferenceParetoFront(experiment).run();
	    new ComputeQualityIndicators<>(experiment).run();
	    new GenerateLatexTablesWithStatistics(experiment).run();
	    new GenerateWilcoxonTestTablesWithR<>(experiment).run();
	    new GenerateBoxplotsWithR<>(experiment).setRows(3).setColumns(3).run();
	    
    }

	private List<ExperimentAlgorithm<PlanningSolution, List<PlanningSolution>>> configureAlgorithmList(
			List<ExperimentProblem<PlanningSolution>> problemList) {
		
		List<ExperimentAlgorithm<PlanningSolution, List<PlanningSolution>>> algorithms = new ArrayList<>();
		
		CrossoverOperator<PlanningSolution> crossover;
        MutationOperator<PlanningSolution> mutation;
        SelectionOperator<List<PlanningSolution>, PlanningSolution> selection;
		
        selection = new BinaryTournamentSelection<>(new PlanningSolutionDominanceComparator());
		
        // Iteration with default algorithm parameters
		for (int i = 0; i < problemList.size(); ++i) {
			NextReleaseProblem problem = (NextReleaseProblem) problemList.get(i).getProblem();
			crossover = new PlanningCrossoverOperator(problem);
	        mutation = new PlanningMutationOperator(problem);
			
	        AlgorithmParameters parameters = problem.getAlgorithmParameters();
	        int nbIterations = parameters.getNumberOfIterations();
	        int populationSize = parameters.getPopulationSize();
			
			Algorithm<List<PlanningSolution>> algorithm = new
					NSGAIIBuilder<>(problem, crossover, mutation)
                    .setSelectionOperator(selection)
                    .setMaxEvaluations(nbIterations)
                    .setPopulationSize(populationSize)
                    .build();
			
			algorithms.add(new ExperimentAlgorithm<>(algorithm, "NSGAIIa", problemList.get(i).getTag()));
		}
		
		return algorithms;
		
	}
	
}

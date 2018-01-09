package logic;

import java.util.List;

import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.measure.Measurable;
import org.uma.jmetal.measure.MeasureManager;
import org.uma.jmetal.measure.PullMeasure;
import org.uma.jmetal.measure.impl.MeasureFactory;
import org.uma.jmetal.measure.impl.SimpleMeasureManager;
import org.uma.jmetal.measure.impl.SimplePullMeasure;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

public class CustomAlgorithm extends NSGAII<PlanningSolution> implements Measurable {
	
	private static final long serialVersionUID = 1L;
	
	private static final int POPULATION_MEASURE = 0;
	private static final int ITERATION_MEASURE = 1;
	
	private SimpleMeasureManager measureManager;
	
	public CustomAlgorithm(Problem<PlanningSolution> problem, int maxEvaluations, int populationSize,
			CrossoverOperator<PlanningSolution> crossoverOperator, MutationOperator<PlanningSolution> mutationOperator,
			SelectionOperator<List<PlanningSolution>, PlanningSolution> selectionOperator,
			SolutionListEvaluator<PlanningSolution> evaluator) {
		super(problem, maxEvaluations, populationSize, crossoverOperator, mutationOperator, selectionOperator, evaluator);

		PullMeasure<List<PlanningSolution>> populationMeasure = new SimplePullMeasure<List<PlanningSolution>>("population",
				"The set of planning solutions used so far.") {
			@Override
			public List<PlanningSolution> get() {
				return getPopulation();
			}
		};
		
		PullMeasure<Integer> iterationMeasure = new SimplePullMeasure<Integer>("iteration", "The current iteration.") {
			@Override
			public Integer get() {
				return evaluations;
			}
		};
		
		measureManager = new SimpleMeasureManager();
		measureManager.setPullMeasure(POPULATION_MEASURE, populationMeasure);
		measureManager.setPullMeasure(ITERATION_MEASURE, iterationMeasure);

	}

	@Override
	public MeasureManager getMeasureManager() {
		return measureManager;
	}

}

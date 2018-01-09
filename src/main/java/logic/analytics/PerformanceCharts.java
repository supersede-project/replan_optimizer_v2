package logic.analytics;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import logic.NextReleaseProblem;
import logic.SolverNRP;

public class PerformanceCharts {
	
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
	
	public static void generateAvgPlannedFeaturesChart(NextReleaseProblem problem, SolverNRP solver, 
			List<Integer> iterations, List<Integer> nbPlannedFeatures, int totalPlannedFeatures) {
		
		String title = String.format("Algorithm: %s. Test set: %s", solver.getAlgorithmType().getName(), "Random");
        XYChart chart = new XYChartBuilder().width(1024).height(512).title(title)
                .xAxisTitle("Iteration").yAxisTitle("Number of features planned").build();
        chart.addSeries("Number of planned features", iterations, nbPlannedFeatures);

        List<Double> average = new ArrayList<>();
        for (int i = 0; i < iterations.size(); ++i)
            average.add((double) totalPlannedFeatures/iterations.size());

        chart.addSeries("Average planned features", iterations, average);
        chart.getStyler().setXAxisMin(0.0).setYAxisMin(0.0)
                .setXAxisMax((double) iterations.size()).setYAxisMax((double) problem.getFeatures().size());

        saveChart(chart, String.format("AveragePlannedFeatures_%s_%s", algorithmName(solver), timestamp()));
	}
	
	public static void generatePopulationChart(SolverNRP solver, List<Integer> populationSize, 
			List<Double> executionTime, List<Integer> plannedFeatures) {
		XYChart chart = new XYChartBuilder().width(1024).height(512).title("Population/Performance chart")
                .xAxisTitle("Population size").build();
        chart.addSeries("Execution time (seconds)", populationSize, executionTime);
        chart.addSeries("Number of planned features", populationSize, plannedFeatures);

        saveChart(chart, String.format("PopulationSizeTest_%s_%s", algorithmName(solver), timestamp()));
	}
	
	private static String timestamp() {
        return dateFormat.format(Calendar.getInstance().getTime());
    }
	
	private static String algorithmName(SolverNRP solver) {
        return solver.getAlgorithmType().getName();
    }
	
	private static void saveChart(XYChart chart, String filename) {
        try {
            String base = "src/test/charts";
            String fullPath = String.format("%s/%s", base, filename);
            File f = new File(fullPath);
            f.getParentFile().mkdirs();

            BitmapEncoder.saveBitmapWithDPI(chart, fullPath, BitmapEncoder.BitmapFormat.PNG, 300);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}

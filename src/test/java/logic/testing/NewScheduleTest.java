package logic.testing;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import entities.Employee;
import entities.Feature;
import entities.NewSchedule;
import entities.PlannedFeature;
import entities.PriorityLevel;
import entities.Skill;

public class NewScheduleTest {
	
	@Test
	public void test() {
		Skill s = new Skill("S001");
		Employee e = new Employee("E001", 40.0, Arrays.asList(s));
		Feature f2 = new Feature("F002", PriorityLevel.VERY_HIGH, 17.0, Arrays.asList(), Arrays.asList(s));
		Feature f = new Feature("F001", PriorityLevel.VERY_HIGH, 33.0, Arrays.asList(), Arrays.asList(s));
		Feature f3 = new Feature("F003", PriorityLevel.VERY_HIGH, 30.0, Arrays.asList(), Arrays.asList(s));
		Feature f4 = new Feature("F004", PriorityLevel.VERY_HIGH, 30.0, Arrays.asList(), Arrays.asList(s));
		Feature f5 = new Feature("F005", PriorityLevel.VERY_HIGH, 30.0, Arrays.asList(), Arrays.asList(s));
		NewSchedule schedule = new NewSchedule(e, 4, 40.0);
		
		PlannedFeature pf = new PlannedFeature(f,e);
		PlannedFeature pf2 = new PlannedFeature(f2,e);
		PlannedFeature pf3 = new PlannedFeature(f3,e);
		PlannedFeature pf4 = new PlannedFeature(f4,e);
		PlannedFeature pf5 = new PlannedFeature(f5,e);
		
		pf.setBeginHour(0.0);
		pf2.setBeginHour(35.0);
		pf3.setBeginHour(0.0);
		
		System.out.println("\nScheduling feature " + pf.getFeature() + " at " + pf.getBeginHour() + "\n");
		schedule.scheduleFeature(pf);
		System.out.println("\nScheduling feature " + pf2.getFeature() + " at " + pf2.getBeginHour() + "\n");
		schedule.scheduleFeature(pf2);
		System.out.println("\nScheduling feature " + pf3.getFeature() + " at " + pf3.getBeginHour() + "\n");
		schedule.scheduleFeature(pf3);
		
		/*Assert.assertEquals(schedule.getCurrentHour(), 85.0, 0.0);
		Assert.assertEquals(schedule.getAllWeeks().size(), 3);
		
		Assert.assertEquals(schedule.getPlannedFeatures().get(0).getBeginHour(), 0.0, 0.0);
		Assert.assertEquals(schedule.getPlannedFeatures().get(0).getEndHour(), 55.0, 0.0);
		
		Assert.assertEquals(schedule.getPlannedFeatures().get(1).getBeginHour(), 55.0, 0.0);
		Assert.assertEquals(schedule.getPlannedFeatures().get(1).getEndHour(), 60.0, 0.0);
		
		Assert.assertEquals(schedule.getPlannedFeatures().get(2).getBeginHour(), 60.0, 0.0);
		Assert.assertEquals(schedule.getPlannedFeatures().get(2).getEndHour(), 85.0, 0.0);*/

		for (int i = 0; i < schedule.getPlannedFeatures().size(); ++i) {
			System.out.println(schedule.getPlannedFeatures().get(i).getFeature().getName() 
					+ " from " + schedule.getPlannedFeatures().get(i).getBeginHour() + " to "
					+ schedule.getPlannedFeatures().get(i).getEndHour());
		}
	}

}

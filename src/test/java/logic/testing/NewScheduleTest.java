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
		Employee e = new Employee("E001", 20.0, Arrays.asList(s));
		Feature f = new Feature("F001", PriorityLevel.VERY_HIGH, 15.0, Arrays.asList(), Arrays.asList(s));
		Feature f2 = new Feature("F002", PriorityLevel.VERY_HIGH, 15.0, Arrays.asList(), Arrays.asList(s));
		Feature f3 = new Feature("F003", PriorityLevel.VERY_HIGH, 15.0, Arrays.asList(), Arrays.asList(s));
		Feature f4 = new Feature("F004", PriorityLevel.VERY_HIGH, 15.0, Arrays.asList(), Arrays.asList(s));
		NewSchedule schedule = new NewSchedule(e, 4, 40.0);
		
		PlannedFeature pf = new PlannedFeature(f,e);
		PlannedFeature pf2 = new PlannedFeature(f2,e);
		PlannedFeature pf3 = new PlannedFeature(f3,e);
		PlannedFeature pf4 = new PlannedFeature(f4,e);
		
		pf2.setBeginHour(85.0);
		pf.setBeginHour(85.0);

		schedule.scheduleFeature(pf2);
		schedule.scheduleFeature(pf);
		schedule.scheduleFeature(pf3);
		schedule.scheduleFeature(pf4);
		
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
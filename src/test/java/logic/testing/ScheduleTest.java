package logic.testing;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import entities.Employee;
import entities.Feature;
import entities.Schedule;
import entities.PlannedFeature;
import entities.PriorityLevel;
import entities.Skill;

public class ScheduleTest {
	
	@Test
	public void test() {
		Skill s = new Skill("S001");
		Employee e = new Employee("E001", 20.0, Arrays.asList(s));
		Feature f = new Feature("F001", PriorityLevel.URGENT, 35.0, Arrays.asList(), Arrays.asList(s));
		Feature f2 = new Feature("F002", PriorityLevel.URGENT, 5.0, Arrays.asList(), Arrays.asList(s));
		Feature f3 = new Feature("F003", PriorityLevel.URGENT, 5.0, Arrays.asList(), Arrays.asList(s));
		Schedule schedule = new Schedule(e, 4, 40.0);
		
		PlannedFeature pf = new PlannedFeature(f,e);
		PlannedFeature pf2 = new PlannedFeature(f2,e);
		PlannedFeature pf3 = new PlannedFeature(f3,e);

		schedule.scheduleFeature(pf);
		schedule.scheduleFeature(pf2);
		schedule.scheduleFeature(pf3);
		
		Assert.assertEquals(schedule.getCurrentHour(), 85.0, 0.0);
		Assert.assertEquals(schedule.getAllWeeks().size(), 3);
		
		Assert.assertEquals(schedule.getPlannedFeatures().get(0).getBeginHour(), 0.0, 0.0);
		Assert.assertEquals(schedule.getPlannedFeatures().get(0).getEndHour(), 55.0, 0.0);
		
		Assert.assertEquals(schedule.getPlannedFeatures().get(1).getBeginHour(), 55.0, 0.0);
		Assert.assertEquals(schedule.getPlannedFeatures().get(1).getEndHour(), 60.0, 0.0);
		
		Assert.assertEquals(schedule.getPlannedFeatures().get(2).getBeginHour(), 60.0, 0.0);
		Assert.assertEquals(schedule.getPlannedFeatures().get(2).getEndHour(), 85.0, 0.0);

		
		/*for (int i = 0; i < schedule.getAllWeeks().size(); ++i) {
			System.out.println(schedule.getWeek(i).getBeginHour() + " to " + schedule.getWeek(i).getEndHour() + " with " + 
		schedule.getWeek(i).getRemainingHours() + " and " + schedule.getCurrentHour() + " or " + schedule.getTotalHoursLeft());
		}
		for (int i = 0; i < schedule.getPlannedFeatures().size(); ++i) {
			System.out.println(schedule.getPlannedFeatures().get(i).getFeature().getName() 
					+ " from " + schedule.getPlannedFeatures().get(i).getBeginHour() + " to "
					+ schedule.getPlannedFeatures().get(i).getEndHour());
		}*/
	}

}

package pl.rychu.embian.refresh;

import pl.rychu.embian.emby.EmbyClient;
import pl.rychu.embian.emby.ScheduledTask;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("squid:S106")
public class JobMon {

	private static final String JOB_STATE_IDLE = "Idle";

	private final Supplier<EmbyClient> embyClientSupplier;

	public JobMon(Supplier<EmbyClient> embyClientSupplier) {
		this.embyClientSupplier = embyClientSupplier;
	}

	// --------

	public void exec(String[] args) {
		EmbyClient embyClient = embyClientSupplier.get();

		List<ScheduledTask> scheduledTasks = embyClient.getScheduledTasks();
		System.out.println("got " + scheduledTasks.size() + " scheduled tasks");
		Map<String, List<ScheduledTask>> tasksByState =
			 scheduledTasks.stream().collect(Collectors.groupingBy(ScheduledTask::getState));
		for (Map.Entry<String, List<ScheduledTask>> e : tasksByState.entrySet()) {
			String state = e.getKey();
			List<ScheduledTask> st = e.getValue();
			System.out.println("---------");
			System.out.println("" + st.size() + " tasks with state " + state);
			for (ScheduledTask task : st) {
				String ts = task.getName() + (task.getState().equals(JOB_STATE_IDLE) ? "" :
					 task.getCurrentProgressPercentage());
				System.out.println(ts);
			}
		}
	}

}

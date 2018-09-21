package pl.rychu.embian.refresh;

import pl.rychu.embian.emby.EmbyClient;

import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("squid:S106")
public class JobMon {

	// --------

	private final Supplier<EmbyClient> embyClientSupplier;

	public JobMon(Supplier<EmbyClient> embyClientSupplier) {
		this.embyClientSupplier = embyClientSupplier;
	}

	// --------

	public void exec(String[] args) {
		EmbyClient embyClient = embyClientSupplier.get();

		Map<String, Object> scheduledTasks = embyClient.getScheduledTasks();
		System.out.println("got "+scheduledTasks.size()+" scheduled tasks");
	}

}

package pl.rychu.embian.refresh;

import pl.rychu.embian.emby.EmbyClient;

import java.util.function.Supplier;

@SuppressWarnings("squid:S106")
public class Refresh {

	// --------

	private final Supplier<EmbyClient> embyClientSupplier;

	public Refresh(Supplier<EmbyClient> embyClientSupplier) {
		this.embyClientSupplier = embyClientSupplier;
	}

	// --------

	public void exec(String[] args) {
		EmbyClient embyClient = embyClientSupplier.get();

		System.out.println("refreshing...");
		embyClient.refresh();
	}

}

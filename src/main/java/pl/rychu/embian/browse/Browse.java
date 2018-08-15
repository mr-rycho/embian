package pl.rychu.embian.browse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.rychu.embian.emby.EmbyClient;
import pl.rychu.embian.emby.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@SuppressWarnings("squid:S106")
public class Browse {

	private static final Logger log = LoggerFactory.getLogger(Browse.class);

	// --------

	private final Supplier<EmbyClient> embyClientSupplier;

	public Browse(Supplier<EmbyClient> embyClientSupplier) {
		this.embyClientSupplier = embyClientSupplier;
	}

	// --------

	public void exec(String[] args) {
		String parentId = args.length >= 1 ? args[0] : null;

		EmbyClient embyClient = embyClientSupplier.get();

		List<Item> itemsLite = embyClient.getItems(parentId, "");

		log.debug("got {} items", itemsLite.size());

		if (itemsLite.isEmpty()) {
			return;
		}

		List<Item> items = new ArrayList<>(itemsLite.size());
		List<Item> itemsSafe = Collections.synchronizedList(items);

		ExecutorService executorService = Executors.newFixedThreadPool(10);

		for (Item itemLite : itemsLite) {
			executorService.execute(() -> {
				Item item = embyClient.getItem(itemLite.getId());
				itemsSafe.add(item);
			});
		}

		shutdownAndWait(executorService);

		items.sort(Comparator.comparing(item -> (String) item.get("SortName")));

		for (Item item : items) {
			System.out.println(item.getName() + " (" + item.getType() + ") / " + item.getId());
			System.out.println("  " + item.getPath());
			System.out.println("  " + item.get("SortName") + " / " + item.get("ForcedSortName") + " / " + item.getTags());
		}
	}

	// --------

	private void shutdownAndWait(ExecutorService executorService) {
		executorService.shutdown();
		try {
			executorService.awaitTermination(1L, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

}

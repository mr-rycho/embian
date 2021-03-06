package pl.rychu.embian.crawl;

import pl.rychu.embian.emby.EmbyClient;
import pl.rychu.embian.emby.Item;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Created on 2018-07-15 by rychu.
 */
public class EmbyCrawler {

	private final EmbyClient embyClient;
	private final List<ItemVisitor> itemVisitors;
	private final String fieldQuery;

	public EmbyCrawler(EmbyClient embyClient, ItemVisitor... itemVisitors) {
		this(embyClient, Arrays.asList(itemVisitors));
	}

	public EmbyCrawler(EmbyClient embyClient, Collection<ItemVisitor> itemVisitors) {
		this.embyClient = embyClient;
		List<ItemVisitor> str = new ArrayList<>(itemVisitors);
		this.itemVisitors = str;
		Set<String> allFields = str.stream().flatMap(s -> s.getFields().stream()).collect(toSet());
		this.fieldQuery = String.join(",", allFields);
	}

	public List<ItemOperations> crawl() {
		List<ItemOperations> commands = new ArrayList<>();
		List<ItemOperations> commandsThreadSafe = Collections.synchronizedList(commands);
		new ForkJoinPool(10).invoke(new Task(null, commandsThreadSafe));
		return commands;
	}

	//===============

	private class Task extends RecursiveAction {

		private final String itemId;
		private final List<ItemOperations> commands;

		public Task(String itemId, List<ItemOperations> targetCommands) {
			this.itemId = itemId;
			this.commands = targetCommands;
		}

		@Override
		protected void compute() {
			List<Item> items = embyClient.getItems(itemId, fieldQuery).stream().filter(Item::isFolder).collect(toList());
			List<Task> recursiveTasks = new ArrayList<>();
			for (Item item : items) {
				boolean shouldRecurse = false;
				List<ItemOperation> allOperations = new ArrayList<>();
				for (ItemVisitor itemVisitor : itemVisitors) {
					ItemScanResult itemScanResult = itemVisitor.process(item);
					allOperations.addAll(itemScanResult.getOperations());
					shouldRecurse |= itemScanResult.isShouldRecurse();
				}
				if (!allOperations.isEmpty()) {
					ItemOperations itemOperations = new ItemOperations(item.getId(), item.getPath(), allOperations);
					commands.add(itemOperations);
				}
				if (shouldRecurse) {
					recursiveTasks.add(new Task(item.getId(), commands));
				}
			}
			if (!recursiveTasks.isEmpty()) {
				invokeAll(recursiveTasks);
			}
		}
	}

}

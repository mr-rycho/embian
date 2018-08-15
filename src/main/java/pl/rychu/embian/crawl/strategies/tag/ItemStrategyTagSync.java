package pl.rychu.embian.crawl.strategies.tag;

import pl.rychu.embian.crawl.ItemOperation;
import pl.rychu.embian.crawl.ItemScanResult;
import pl.rychu.embian.crawl.ItemStrategy;
import pl.rychu.embian.emby.Item;
import pl.rychu.embian.fs.Filesystem;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static pl.rychu.embian.crawl.ItemCommandType.ASSIGN_TAG;
import static pl.rychu.embian.crawl.ItemCommandType.REMOVE_TAG;

/**
 * Created on 2018-07-10 by rychu.
 */
public class ItemStrategyTagSync implements ItemStrategy {

	private final Filesystem filesystem;

	public ItemStrategyTagSync(Filesystem filesystem) {
		this.filesystem = filesystem;
	}

	// ----------

	@Override
	public Set<String> getFields() {
		return new HashSet<>(Arrays.asList("Tags", "Path"));

	}

	@Override
	public ItemScanResult process(Item item) {
		Set<String> itemTags = new HashSet<>(item.getTags());
		Set<String> fsTags = filesystem.listTags(item.getPath());
		boolean itemPriv = itemTags.contains("priv");
		boolean fsPriv = fsTags.contains("priv");

		if (itemPriv && fsPriv) {
			return new ItemScanResult(emptyList(), false);
		}
		if (!itemPriv && !fsPriv) {
			return new ItemScanResult(emptyList(), true);
		}
		if (itemPriv) { //  && !fsPriv
			Map<String, Object> params = new HashMap<>();
			params.put("tags", singletonList("priv"));
			ItemOperation op = new ItemOperation(REMOVE_TAG, params);
			return new ItemScanResult(singletonList(op), true);
		}
		// !itemPriv && fsPriv
		Map<String, Object> params = new HashMap<>();
		params.put("tags", singletonList("priv"));
		ItemOperation op = new ItemOperation(ASSIGN_TAG, params);
		return new ItemScanResult(singletonList(op), false);
	}

}

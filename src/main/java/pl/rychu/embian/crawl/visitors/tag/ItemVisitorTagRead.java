package pl.rychu.embian.crawl.visitors.tag;

import pl.rychu.embian.crawl.ItemOperation;
import pl.rychu.embian.crawl.ItemScanResult;
import pl.rychu.embian.crawl.ItemVisitor;
import pl.rychu.embian.emby.Item;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static pl.rychu.embian.crawl.ItemCommandType.TAG_INFO;

/**
 * Created on 2018-08-03 by rychu.
 */
public class ItemVisitorTagRead implements ItemVisitor {

	@Override
	public Set<String> getFields() {
		return new HashSet<>(Arrays.asList("Tags", "Path"));
	}

	@Override
	public ItemScanResult process(Item item) {
		Set<String> itemTags = new HashSet<>(item.getTags());
		boolean itemPriv = itemTags.contains("priv");

		if (itemPriv) {
			Map<String, Object> params = new HashMap<>();
			params.put("tags", itemTags);
			ItemOperation op = new ItemOperation(TAG_INFO, params);
			return new ItemScanResult(singletonList(op), false);
		}
		return new ItemScanResult(emptyList(), true);
	}

}

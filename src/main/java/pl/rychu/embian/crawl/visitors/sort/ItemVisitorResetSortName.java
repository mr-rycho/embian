package pl.rychu.embian.crawl.visitors.sort;

import pl.rychu.embian.crawl.ItemOperation;
import pl.rychu.embian.crawl.ItemScanResult;
import pl.rychu.embian.crawl.ItemVisitor;
import pl.rychu.embian.emby.Item;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static pl.rychu.embian.crawl.ItemCommandType.ASSIGN_SORTNAME;

public class ItemVisitorResetSortName implements ItemVisitor {

	@Override
	public Set<String> getFields() {
		return new HashSet<>(Arrays.asList("SortName", "Path"));
	}

	@Override
	public ItemScanResult process(Item item) {
		if (item.getPath() != null) {
			Map<String, Object> params = new HashMap<>();
			params.put("sortname", null);
			ItemOperation op = new ItemOperation(ASSIGN_SORTNAME, params);
			return new ItemScanResult(singletonList(op), true);
		}

		return new ItemScanResult(emptyList(), true);
	}

}

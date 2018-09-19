package pl.rychu.embian.crawl.strategies.sort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.rychu.embian.crawl.ItemOperation;
import pl.rychu.embian.crawl.ItemScanResult;
import pl.rychu.embian.crawl.ItemVisitor;
import pl.rychu.embian.emby.Item;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static pl.rychu.embian.crawl.ItemCommandType.ASSIGN_SORTNAME;

/**
 * Created on 2018-08-09 by rychu.
 */
public class ItemVisitorSortByDate implements ItemVisitor {

	private static final Logger log = LoggerFactory.getLogger(ItemVisitorSortByDate.class);

	private final SortNameCalculator sortNameCalculator = new SortNameCalculator();

	// ----------

	@Override
	public Set<String> getFields() {
		return new HashSet<>(Arrays.asList("SortName", "Path"));
	}

	@Override
	public ItemScanResult process(Item item) {
		if (item.getPath() != null) {
			String name = item.getName();
			Optional<String> expSortNameOpt = sortNameCalculator.calcSortName(name);
			if (expSortNameOpt.isPresent()) {
				String expSortName = expSortNameOpt.get();
				Object actSortName = item.get("SortName");
				boolean eq = actSortName.equals(expSortName);
				log.debug("{} / {}", name, item.getPath());
				log.debug("  {} {} {}", expSortName, eq ? "==" : "!=", actSortName);
				if (!eq) {
					Map<String, Object> params = new HashMap<>();
					params.put("sortname", expSortName);
					ItemOperation op = new ItemOperation(ASSIGN_SORTNAME, params);
					return new ItemScanResult(singletonList(op), true);
				}
			}
		}

		return new ItemScanResult(emptyList(), true);
	}

}

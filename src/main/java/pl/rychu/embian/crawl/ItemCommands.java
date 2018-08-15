package pl.rychu.embian.crawl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created on 2018-07-08 by rychu.
 */
public class ItemCommands {
	private final String itemId;
	private final String itemPath;
	private final List<ItemOperation> operations;

	public ItemCommands(String itemId, String itemPath, Collection<ItemOperation> ops) {
		this.itemId = itemId;
		this.itemPath = itemPath;
		this.operations = Collections.unmodifiableList(new ArrayList<>(ops));
	}

	public String getItemId() {
		return itemId;
	}

	public String getItemPath() {
		return itemPath;
	}

	public List<ItemOperation> getOperations() {
		return operations;
	}

	@Override
	public String toString() {
		return "ItemCommands{" + "itemId='" + itemId + '\'' + ", itemPath='" + itemPath + '\'' + ", operations=" + operations + '}';
	}

}

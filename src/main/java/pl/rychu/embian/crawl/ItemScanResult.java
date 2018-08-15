package pl.rychu.embian.crawl;

import java.util.List;

/**
 * Created on 2018-07-08 by rychu.
 */
public class ItemScanResult {
	private final List<ItemOperation> operations;
	private final boolean shouldRecurse;

	public ItemScanResult(List<ItemOperation> operations, boolean shouldRecurse) {
		this.operations = operations;
		this.shouldRecurse = shouldRecurse;
	}

	public List<ItemOperation> getOperations() {
		return operations;
	}

	public boolean isShouldRecurse() {
		return shouldRecurse;
	}

}

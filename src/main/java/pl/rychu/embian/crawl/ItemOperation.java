package pl.rychu.embian.crawl;

import java.util.Map;

/**
 * Created on 2018-08-04 by rychu.
 */
public class ItemOperation {

	private final ItemCommandType itemCommandType;
	private final Map<String, Object> params;

	public ItemOperation(ItemCommandType itemCommandType, Map<String, Object> params) {
		this.itemCommandType = itemCommandType;
		this.params = params;
	}

	public ItemCommandType getItemCommandType() {
		return itemCommandType;
	}

	public Object getParam(String key) {
		return params.get(key);
	}

	@Override
	public String toString() {
		return "ItemOperation{" + "type=" + itemCommandType + ", params=" + params + '}';
	}

}

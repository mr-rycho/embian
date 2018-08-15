package pl.rychu.embian.emby;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * Created on 2018-07-08 by rychu.
 */
public class Item {

	private final Map<String, Object> map;

	public Item(Map<String, Object> map) {
		this.map = new HashMap<>(map);
	}

	public List<String> getKeys() {
		List<String> keys = new ArrayList<>(map.keySet());
		Collections.sort(keys);
		return keys;
	}

	public Object get(String key) {
		return map.get(key);
	}

	public String getName() {
		return (String) get("Name");
	}

	public String getId() {
		return (String) get("Id");
	}

	public String getPath() {
		return (String) get("Path");
	}

	public String getType() {
		return (String) get("Type");
	}

	public boolean isFolder() {
		return EmbyConst.EMBY_ITEM_TYPE_FOLDER.equals(getType()) || EmbyConst.EMBY_ITEM_TYPE_PHOTO_ALBUM
		 .equals(getType()) || EmbyConst.EMBY_ITEM_TYPE_COLL_FOLDER.equals(getType());
	}

	public List<String> getTags() {
		//noinspection unchecked
		List<String> tags = (List<String>) get("Tags");
		return tags == null || tags.isEmpty() ? emptyList() : unmodifiableList(tags);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Item item = (Item) o;
		return Objects.equals(map, item.map);
	}

	@Override
	public int hashCode() {
		return Objects.hash(map);
	}

	public Map<String, Object> cloneMap() {
		return new HashMap<>(map);
	}

	@Override
	public String toString() {
		return "Item:" + getName() + " / " + getId() + " / " + getTags();
	}
}

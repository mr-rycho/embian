package pl.rychu.embian.crawl.visitors.tag;

import pl.rychu.embian.crawl.ItemOperation;
import pl.rychu.embian.crawl.ItemScanResult;
import pl.rychu.embian.crawl.ItemVisitor;
import pl.rychu.embian.emby.Item;
import pl.rychu.embian.fs.Filesystem;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;
import static pl.rychu.embian.crawl.ItemCommandType.ASSIGN_TAG;
import static pl.rychu.embian.crawl.ItemCommandType.REMOVE_TAG;

/**
 * Created on 2018-07-10 by rychu.
 */
public class ItemVisitorTagSync implements ItemVisitor {

	private final Filesystem filesystem;

	public ItemVisitorTagSync(Filesystem filesystem) {
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
		Set<String> itemPrivTags = itemTags.stream().filter(t -> t.startsWith("priv")).collect(toSet());
		Set<String> fsPrivTags = fsTags.stream().filter(t -> t.startsWith("priv")).collect(toSet());

		Set<String> tagsOnlyInItem = diff(itemPrivTags, fsPrivTags);
		Set<String> tagsOnlyInFs = diff(fsPrivTags, itemPrivTags);

		if (tagsOnlyInFs.isEmpty() && tagsOnlyInItem.isEmpty()) {
			return new ItemScanResult(emptyList(), true);
		}
		List<ItemOperation> ops = new ArrayList<>();
		if (!tagsOnlyInFs.isEmpty()) {
			Map<String, Object> params = new HashMap<>();
			params.put("tags", new ArrayList<>(tagsOnlyInFs));
			ItemOperation op = new ItemOperation(ASSIGN_TAG, params);
			ops.add(op);
		}
		if (!tagsOnlyInItem.isEmpty()) {
			Map<String, Object> params = new HashMap<>();
			params.put("tags", new ArrayList<>(tagsOnlyInItem));
			ItemOperation op = new ItemOperation(REMOVE_TAG, params);
			ops.add(op);
		}
		return new ItemScanResult(ops, true);
	}

	private static <T> Set<T> diff(Set<T> setA, Set<T> setB) {
		return setA.stream().filter(t -> !setB.contains(t)).collect(toSet());
	}

}

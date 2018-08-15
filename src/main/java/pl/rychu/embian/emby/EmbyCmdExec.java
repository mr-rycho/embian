package pl.rychu.embian.emby;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.rychu.embian.crawl.ItemCommandType;
import pl.rychu.embian.crawl.ItemCommands;
import pl.rychu.embian.crawl.ItemOperation;

import java.util.*;

import static java.util.Collections.emptyList;

/**
 * Created on 2018-07-16 by rychu.
 */
public class EmbyCmdExec {

	private static final Logger log = LoggerFactory.getLogger(EmbyCmdExec.class);

	private final EmbyClient embyClient;

	public EmbyCmdExec(EmbyClient embyClient) {
		this.embyClient = embyClient;
	}

	public void execute(ItemCommands itemCommands) {
		log.debug("executing {}", itemCommands);

		List<ItemOperation> ops = itemCommands.getOperations();
		if (ops.isEmpty()) {
			return;
		}

		boolean isUpdating = ops.stream().anyMatch(op -> op.getItemCommandType().isUpdatingType());
		if (!isUpdating) {
			return;
		}

		String itemId = itemCommands.getItemId();
		Item item = embyClient.getItem(itemId);
		Map<String, Object> newItem = item.cloneMap();
		for (ItemOperation op : ops) {
			executeOp(newItem, op);
		}
		embyClient.updateItem(itemId, new Gson().toJson(newItem));
	}

	private void executeOp(Map<String, Object> newItemMap, ItemOperation op) {
		ItemCommandType type = op.getItemCommandType();
		switch (type) {
			case ASSIGN_TAG: {
				List<String> tagsRaw = (List<String>) newItemMap.get("Tags");
				List<String> tags = tagsRaw == null || tagsRaw.isEmpty() ? emptyList() : tagsRaw;
				Set<String> newTags = new HashSet<>(tags);
				newTags.addAll((List<String>) op.getParam("tags"));
				newItemMap.put("tags", new ArrayList<>(newTags));
			}
			break;
			case REMOVE_TAG: {
				List<String> tagsRaw = (List<String>) newItemMap.get("Tags");
				List<String> tags = tagsRaw == null || tagsRaw.isEmpty() ? emptyList() : tagsRaw;
				Set<String> newTags = new HashSet<>(tags);
				newTags.removeAll((List<String>) op.getParam("tags"));
				newItemMap.put("tags", new ArrayList<>(newTags));
			}
			break;
			case TAG_INFO:
				break;
			case ASSIGN_SORTNAME: {
				newItemMap.put("ForcedSortName", op.getParam("sortname"));
			}
			break;
		}
	}

}

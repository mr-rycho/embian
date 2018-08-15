package pl.rychu.embian.crawl;

import pl.rychu.embian.emby.Item;

import java.util.Set;

/**
 * Created on 2018-07-10 by rychu.
 */
public interface ItemStrategy {

	Set<String> getFields();

	ItemScanResult process(Item item);

}

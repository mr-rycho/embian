package pl.rychu.embian.crawl;

/**
 * Created on 2018-07-08 by rychu.
 */
public enum ItemCommandType {


	ASSIGN_TAG(true), REMOVE_TAG(true), TAG_INFO(false), ASSIGN_SORTNAME(true);

	private final boolean isUpdatingType;

	ItemCommandType(boolean isUpdatingType) {
		this.isUpdatingType = isUpdatingType;
	}

	public boolean isUpdatingType() {
		return isUpdatingType;
	}

}

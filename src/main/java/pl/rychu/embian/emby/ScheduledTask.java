package pl.rychu.embian.emby;

import java.util.HashMap;
import java.util.Map;

public class ScheduledTask {

	private final Map<String, Object> map;

	public ScheduledTask(Map<String, Object> map) {
		this.map = new HashMap<>(map);
	}

	public String getName() {
		return (String) map.get("Name");
	}

	public String getState() {
		return (String) map.get("State");
	}

	public int getCurrentProgressPercentage() {
		Object obj = map.get("CurrentProgressPercentage");
		if (obj == null) {
			return -1;
		}
		if (obj instanceof Integer) {
			return (Integer) obj;
		}
		if (obj instanceof String) {
			return Integer.parseInt((String) obj, 10);
		}
		throw new IllegalStateException("cannot determine value for 'CurrentProgressPercentage': " + obj);
	}

}

package pl.rychu.embian.emby;

import com.google.gson.Gson;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created on 2018-07-16 by rychu.
 */
public class EmbyClient {

	private final Client client;
	private final String emby;
	private final String accessToken;
	private final String userId;

	public EmbyClient(Client client, String emby, String accessToken, String userId) {
		this.client = client;
		this.emby = emby;
		this.accessToken = accessToken;
		this.userId = userId;
	}

	public Item getItem(String id) {
		String path = emby + "/Users/" + userId + "/Items/" + id;
		WebTarget webTarget = client.target(path);
		Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
		builder.header("X-MediaBrowser-Token", accessToken);
		String auth = "MediaBrowser UserId=\"" + userId + "\", Client=\"Dashboard\", Device=\".NET Webclient\", " +
			 "DeviceId=\"" + UUID.randomUUID() + "\", Version=\"1.0.0.0\"";
		builder.header("Authorization", auth);
		Response resp = builder.get();
		String s = resp.readEntity(String.class);
		//noinspection unchecked
		Map<String, Object> map = (Map<String, Object>) new Gson().fromJson(s, Map.class);
		return new Item(map);
	}

	public List<Item> getItems(String parentId, String fields) {
		String path = emby + "/Users/" + userId + "/Items";
		if (parentId != null) {
			path += "?parentId=" + parentId;
			path += "&Fields=" + fields;
		}
		WebTarget webTarget = client.target(path);
		Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
		builder.header("X-MediaBrowser-Token", accessToken);
		String auth = "MediaBrowser UserId=\"" + userId + "\", Client=\"Dashboard\", Device=\".NET Webclient\", " +
			 "DeviceId=\"" + UUID.randomUUID() + "\", Version=\"1.0.0.0\"";
		builder.header("Authorization", auth);
		Response resp = builder.get();
		String s = resp.readEntity(String.class);
		//noinspection unchecked
		Map<String, Object> map = (Map<String, Object>) new Gson().fromJson(s, Map.class);
		List<Item> items = new ArrayList<>();
		if (map.containsKey("Items") && map.get("Items") instanceof List) {
			//noinspection unchecked
			List<Object> jsonItems = (List<Object>) map.get("Items");
			for (Object jsonItem : jsonItems) {
				if (jsonItem instanceof Map) {
					//noinspection unchecked
					Map<String, Object> m = (Map<String, Object>) jsonItem;
					items.add(new Item(m));
				}
			}
		}
		return items;
	}

	public int updateItem(String itemId, String json) {
		String path = emby + "/Items/" + itemId;
		WebTarget webTarget = client.target(path);
		Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
		builder.header("X-MediaBrowser-Token", accessToken);
		String auth = "MediaBrowser UserId=\"" + userId + "\", Client=\"Dashboard\", Device=\".NET Webclient\", " +
			 "DeviceId=\"" + UUID.randomUUID() + "\", Version=\"1.0.0.0\"";
		builder.header("Authorization", auth);
		Entity<String> stringEntity = Entity.entity(json, MediaType.APPLICATION_JSON_TYPE);
		Response resp = builder.post(stringEntity);
		return resp.getStatus();
	}

	public int refresh() {
		String path = emby + "/library/refresh";
		WebTarget webTarget = client.target(path);
		Invocation.Builder builder = webTarget.request();
		builder.header("X-MediaBrowser-Token", accessToken);
		String auth = "MediaBrowser UserId=\"" + userId + "\", Client=\"Dashboard\", Device=\".NET Webclient\", " +
			 "DeviceId=\"" + UUID.randomUUID() + "\", Version=\"1.0.0.0\"";
		builder.header("Authorization", auth);
		Response resp = builder.post(null);
		return resp.getStatus();
	}

	public List<ScheduledTask> getScheduledTasks() {
		String path = emby + "/ScheduledTasks";
		WebTarget webTarget = client.target(path);
		Invocation.Builder builder = webTarget.request();
		builder.header("X-MediaBrowser-Token", accessToken);
		String auth = "MediaBrowser UserId=\"" + userId + "\", Client=\"Dashboard\", Device=\".NET Webclient\", " +
			 "DeviceId=\"" + UUID.randomUUID() + "\", Version=\"1.0.0.0\"";
		builder.header("Authorization", auth);
		Response resp = builder.get();
		String s = resp.readEntity(String.class);
		//noinspection unchecked
		List<Map<String, Object>> jobMaps = (List<Map<String, Object>>) new Gson().fromJson(s, List.class);
		List<ScheduledTask> tasks = new ArrayList<>();
		for (Map<String, Object> jobMap : jobMaps) {
			tasks.add(new ScheduledTask(jobMap));
		}
		return tasks;
	}

}

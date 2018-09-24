package pl.rychu.embian.emby;

import com.google.gson.Gson;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import java.security.MessageDigest;
import java.util.*;

import static java.util.Collections.singletonList;

/**
 * Created on 2018-07-16 by rychu.
 */
public class EmbyClientFactory {

	public EmbyClient authenticate(Client client, String address, String user, String pass) {
		WebTarget target = client.target(address + "Users/AuthenticateByName");
		MultivaluedMap<String, String> form = new MultivaluedHashMap<>();
		// https://emby.media/community/index.php?/topic/33979-question-regarding-authentication/
		form.put("username", singletonList(user));
		form.put("pw", singletonList(pass));
		form.put("password", singletonList(getSha1(pass)));
		form.put("passwordMd5", singletonList(getMd5(pass)));
		Entity<Form> formEntity = Entity.form(form);
		Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);
		builder.header("Content-Type", "application/x-www-form-urlencoded");
		String auth = "MediaBrowser UserId=\"" + user + "\", Client=\"Dashboard\", Device=\".NET Webclient\", DeviceId=\"" + UUID
		 .randomUUID() + "\", Version=\"1.0.0.0\"";
		builder.header("Authorization", auth);

		Response resp = builder.post(formEntity);
		String s = resp.readEntity(String.class);
		if (!s.startsWith("{")) {
			throw new IllegalStateException("login error: "+s);
		}
		Gson gson = new Gson();
		//noinspection unchecked
		Map<String, Object> map = (Map<String, Object>) gson.fromJson(s, Map.class);
		String userId = null;
		for (String s1 : map.keySet()) {
			Object v = map.get(s1);
			if (v instanceof Map) {
				//noinspection unchecked
				Map<String, Object> m = (Map<String, Object>) v;
				List<String> mk = new ArrayList<>(m.keySet());
				Collections.sort(mk);
				for (String k2 : mk) {
					Object v2 = m.get(k2);
					if (s1.equals("User") && k2.equals("Id") && v2 instanceof String) {
						userId = (String) v2;
					}
				}
			}
		}
		String accessToken = null;
		if (map.containsKey("AccessToken")) {
			Object v = map.get("AccessToken");
			if (v instanceof String) {
				accessToken = (String) v;
			}
		}

		if (accessToken != null && userId != null) {
			return new EmbyClient(client, address, accessToken, userId);
		} else {
			throw new IllegalStateException("cannot authenticate, missing return information");
		}
	}

	// ----------

	private static String getSha1(String str) {
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			byte[] digest = sha1.digest(str.getBytes("utf8"));
			return byteToHex(digest);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String getMd5(String str) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] digest = md5.digest(str.getBytes("utf8"));
			return byteToHex(digest);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String byteToHex(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}

}

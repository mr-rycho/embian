package pl.rychu.embian.fs;

import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

/**
 * Created on 2018-07-07 by rychu.
 */
public class FilesystemMock implements Filesystem {

	@Override
	public Set<String> listTags(String path) {
		if (path == null) {
			return emptySet();
		} else if (path.endsWith("\\_")) {
			return singleton("priv");
		} else if (path.endsWith("\\2013")) {
			return emptySet();
		} else {
			int i = path.lastIndexOf('\\');
			if (i >= 0) {
				String lp = path.substring(i + 1);
				if (lp.startsWith("2013-10-12")) {
					return emptySet();
				}
			}
			return singleton("priv");
		}
	}

}

package pl.rychu.embian.fs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

/**
 * Created on 2018-08-03 by rychu.
 */
public class FilesystemDrive implements Filesystem {

	private static final String EMBY_TAG_FILE_PREFIX = ".emby.tag.";

	@Override
	public Set<String> listTags(String path) {
		if (path == null || path.isEmpty()) {
			return emptySet();
		}
		File file = new File(path);
		if (!file.exists()) {
			return emptySet();
		}
		if (file.isDirectory()) {
			Set<String> tags = new HashSet<>();
			String pathname = path + "/.emby.tags";
			File tagsFile = new File(pathname);

			if (tagsFile.exists() && !tagsFile.isDirectory()) {
				tags.addAll(readLines(pathname));
			}

			String[] tagFiles = file.list((dir, name) -> name.startsWith(EMBY_TAG_FILE_PREFIX));
			if (tagFiles != null) {
				for (String tagFile : tagFiles) {
					tags.add(tagFile.substring(EMBY_TAG_FILE_PREFIX.length()));
				}
			}

			return tags;
		} else {
			throw new IllegalStateException("only directories are supported but got " + path);
		}
	}

	private static List<String> readLines(String file) {
		try {
			return Files.lines(new File(file).toPath()).map(String::trim).filter(line -> !line.isEmpty())
			 .filter(line -> !line.startsWith("#")).collect(toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}

package pl.rychu.embian.crawl.visitors.tag;

import org.junit.Before;
import org.junit.Test;
import pl.rychu.embian.crawl.ItemOperation;
import pl.rychu.embian.crawl.ItemScanResult;
import pl.rychu.embian.emby.Item;
import pl.rychu.embian.fs.Filesystem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pl.rychu.embian.crawl.ItemCommandType.ASSIGN_TAG;
import static pl.rychu.embian.crawl.ItemCommandType.REMOVE_TAG;

public class ItemVisitorTagSyncTest {

	private ItemVisitorTagSync visitorPriv;
	private ItemVisitorTagSync visitorNoPriv;

	@Before
	public void setupVisitor() {
		Filesystem fs = mock(Filesystem.class);
		when(fs.listTags("/dirA")).thenReturn(new HashSet<>(asList("priv", "privB", "kot")));
		visitorPriv = new ItemVisitorTagSync(fs);

		Filesystem fs2 = mock(Filesystem.class);
		when(fs2.listTags("/dirA")).thenReturn(new HashSet<>(asList("privB", "kot")));
		visitorNoPriv = new ItemVisitorTagSync(fs2);
	}

	// --------

	@Test
	public void shouldDoNothingAndNotRecurse() {
		// given
		Item item = itemWithTags("priv", "privB", "kot");

		// when
		ItemScanResult result = visitorPriv.process(item);

		// then
		assertThat(result.isShouldRecurse()).isFalse();
		assertThat(result.getOperations()).isEmpty();
	}

	@Test
	public void shouldAddTagAndNotRecurse() {
		// given
		Item item = itemWithTags("privB", "kot");

		// when
		ItemScanResult result = visitorPriv.process(item);

		// then
		assertThat(result.isShouldRecurse()).isFalse();
		assertThat(result.getOperations()).hasSize(1);
		ItemOperation op = result.getOperations().get(0);
		assertThat(op.getItemCommandType()).isEqualTo(ASSIGN_TAG);
		List<String> opTags = (List<String>) op.getParam("tags");
		assertThat(opTags).hasSize(1);
		assertThat(opTags.get(0)).isEqualTo("priv");
	}

	@Test
	public void shouldRemoveTagAndRecurse() {
		// given
		Item item = itemWithTags("priv", "privB", "kot");

		// when
		ItemScanResult result = visitorNoPriv.process(item);

		// then
		assertThat(result.isShouldRecurse()).isTrue();
		assertThat(result.getOperations()).hasSize(1);
		ItemOperation op = result.getOperations().get(0);
		assertThat(op.getItemCommandType()).isEqualTo(REMOVE_TAG);
		List<String> opTags = (List<String>) op.getParam("tags");
		assertThat(opTags).hasSize(1);
		assertThat(opTags.get(0)).isEqualTo("priv");
	}

	@Test
	public void shouldDoNothingAndRecurse() {
		// given
		Item item = itemWithTags("privB", "kot");

		// when
		ItemScanResult result = visitorNoPriv.process(item);

		// then
		assertThat(result.isShouldRecurse()).isTrue();
		assertThat(result.getOperations()).isEmpty();
	}

	// --------

	private static Item itemWithTags(String... tags) {
		Map<String, Object> im = new HashMap<>();
		im.put("Tags", asList(tags));
		im.put("Path", "/dirA");
		return new Item(im);
	}

}
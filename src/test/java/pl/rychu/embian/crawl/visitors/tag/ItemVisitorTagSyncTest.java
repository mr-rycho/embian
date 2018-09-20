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

	private ItemVisitorTagSync visitor;

	@Before
	public void setupVisitor() {
		Filesystem fs = mock(Filesystem.class);
		when(fs.listTags("/dir12")).thenReturn(new HashSet<>(asList("priv", "privB")));
		when(fs.listTags("/dir2")).thenReturn(new HashSet<>(asList("privB")));
		when(fs.listTags("/dir1")).thenReturn(new HashSet<>(asList("priv")));
		visitor = new ItemVisitorTagSync(fs);
	}

	// --------

	@Test
	public void shouldDoNothingAndRecurse() {
		// given
		Item item = itemWithTags("dir12", "priv", "privB", "kot");

		// when
		ItemScanResult result = visitor.process(item);

		// then
		assertThat(result.isShouldRecurse()).isTrue();
		assertThat(result.getOperations()).isEmpty();
	}

	@Test
	public void shouldAddTagAndRecurse() {
		// given
		Item item = itemWithTags("dir12", "privB", "kot");

		// when
		ItemScanResult result = visitor.process(item);

		// then
		assertThat(result.isShouldRecurse()).isTrue();
		assertAssignTagOp(result, "priv");
	}

	@Test
	public void shouldAddTagAndRecurse2() {
		// given
		Item item = itemWithTags("dir12", "priv", "kot");

		// when
		ItemScanResult result = visitor.process(item);

		// then
		assertThat(result.isShouldRecurse()).isTrue();
		assertAssignTagOp(result, "privB");
	}

	@Test
	public void shouldAddTagAndRecurse3() {
		// given
		Item item = itemWithTags("dir12", "kot");

		// when
		ItemScanResult result = visitor.process(item);

		// then
		assertThat(result.isShouldRecurse()).isTrue();
		assertAssignTagOp(result, "privB", "priv");
	}

	@Test
	public void shouldRemoveTagAndRecurse() {
		// given
		Item item = itemWithTags("dir1", "priv", "privB", "kot");

		// when
		ItemScanResult result = visitor.process(item);

		// then
		assertThat(result.isShouldRecurse()).isTrue();
		assertRemoveTagOp(result, "privB");
	}

	@Test
	public void shouldRemoveTagAndRecurse2() {
		// given
		Item item = itemWithTags("dir2", "priv", "privB", "kot");

		// when
		ItemScanResult result = visitor.process(item);

		// then
		assertThat(result.isShouldRecurse()).isTrue();
		assertRemoveTagOp(result, "priv");
	}

	@Test
	public void shouldAddAndRemoveTagAndRecurse() {
		// given
		Item item = itemWithTags("dir2", "priv", "kot");

		// when
		ItemScanResult result = visitor.process(item);

		// then
		assertThat(result.isShouldRecurse()).isTrue();
		List<ItemOperation> ops = result.getOperations();
		assertThat(ops).hasSize(2);
		ItemOperation opAss =
			 ops.stream().filter(op -> op.getItemCommandType() == ASSIGN_TAG).findAny().orElseThrow(() -> new IllegalStateException("expecting to have assign op"));
		ItemOperation opRem =
			 ops.stream().filter(op -> op.getItemCommandType() == REMOVE_TAG).findAny().orElseThrow(() -> new IllegalStateException("expecting to have remove op"));
		assertRemoveTagOp(opRem, "priv");
		assertAssignTagOp(opAss, "privB");
	}

	// --------

	private static Item itemWithTags(String path, String... tags) {
		Map<String, Object> im = new HashMap<>();
		im.put("Path", "/" + path);
		im.put("Tags", asList(tags));
		return new Item(im);
	}

	private static void assertAssignTagOp(ItemScanResult r, String... tags) {
		assertThat(r.getOperations()).hasSize(1);
		ItemOperation op = r.getOperations().get(0);
		assertAssignTagOp(op, tags);
	}

	private static void assertAssignTagOp(ItemOperation op, String... tags) {
		assertThat(op.getItemCommandType()).isEqualTo(ASSIGN_TAG);
		@SuppressWarnings("unchecked") List<String> opTags = (List<String>) op.getParam("tags");
		assertThat(opTags).hasSize(tags.length);
		for (String tag : tags) {
			assertThat(opTags.contains(tag));
		}
	}

	private static void assertRemoveTagOp(ItemScanResult r, String... tags) {
		assertThat(r.getOperations()).hasSize(1);
		ItemOperation op = r.getOperations().get(0);
		assertRemoveTagOp(op, tags);
	}

	private static void assertRemoveTagOp(ItemOperation op, String... tags) {
		assertThat(op.getItemCommandType()).isEqualTo(REMOVE_TAG);
		@SuppressWarnings("unchecked") List<String> opTags = (List<String>) op.getParam("tags");
		assertThat(opTags).hasSize(tags.length);
		for (String tag : tags) {
			assertThat(opTags.contains(tag));
		}
	}

}
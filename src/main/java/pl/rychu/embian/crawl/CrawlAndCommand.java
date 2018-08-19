package pl.rychu.embian.crawl;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.rychu.embian.crawl.strategies.sort.ItemStrategyResetSortName;
import pl.rychu.embian.crawl.strategies.sort.ItemStrategySortByDate;
import pl.rychu.embian.crawl.strategies.tag.ItemStrategyTagSync;
import pl.rychu.embian.emby.EmbyClient;
import pl.rychu.embian.emby.EmbyCmdExec;
import pl.rychu.embian.fs.Filesystem;
import pl.rychu.embian.fs.FilesystemDrive;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@SuppressWarnings("squid:S106")
public class CrawlAndCommand {

	private static final Logger log = LoggerFactory.getLogger(CrawlAndCommand.class);

	private static final String OPT_RESET_SORT_NAME = "reset-sort-name";
	private static final String OPT_SET_SORT_NAME = "set-sort-name";
	private static final String OPT_SYNC_TAGS = "sync-tags";

	// --------

	private final Supplier<EmbyClient> embyClientSupplier;

	public CrawlAndCommand(Supplier<EmbyClient> embyClientSupplier) {
		this.embyClientSupplier = embyClientSupplier;
	}

	// --------

	public void exec(String[] args) {
		Options opts = new Options();
		opts.addOption("", OPT_RESET_SORT_NAME, false, "reset sort names in all directories i.e. make emby recompute it");
		opts.addOption("", OPT_SET_SORT_NAME, false, "set sort names in date directories e.g. \"2018-08 ...\"");
		opts.addOption("", OPT_SYNC_TAGS, false, "synchronizes tags with filesystem");

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(opts, args);
		} catch (ParseException e) {
			System.err.println("ERROR: error parsing command line");
			e.printStackTrace(new PrintWriter(System.err));
			System.exit(1);
			return;
		}

		List<String> req1 = Arrays.asList(OPT_RESET_SORT_NAME, OPT_SET_SORT_NAME, OPT_SYNC_TAGS);
		boolean anyCmd = req1.stream().anyMatch(cmd::hasOption);
		if (!anyCmd) {
			System.err.println("must pass any of :" + req1);
			System.exit(1);
			return;
		}

		EmbyClient embyClient = embyClientSupplier.get();

		List<ItemStrategy> itemStrategies = new ArrayList<>();
		if (cmd.hasOption(OPT_RESET_SORT_NAME)) {
			itemStrategies.add(new ItemStrategyResetSortName());
		}
		if (cmd.hasOption(OPT_SET_SORT_NAME)) {
			itemStrategies.add(new ItemStrategySortByDate());
		}
		if (cmd.hasOption(OPT_SYNC_TAGS)) {
			Filesystem filesystem = new FilesystemDrive();
//		Filesystem filesystem = new FilesystemMock();
			itemStrategies.add(new ItemStrategyTagSync(filesystem));
		}

		EmbyCrawler embyCrawler = new EmbyCrawler(embyClient, itemStrategies);

		long t0 = System.currentTimeMillis();
		List<ItemOperations> commands = embyCrawler.crawl();
		long t1 = System.currentTimeMillis();
		log.info("crawled in {} ms", t1 - t0);
		Comparator<ItemOperations> cmp = Comparator.comparing(ItemOperations::getItemPath);
		commands.sort(cmp);
		log.info("got {} commands", commands.size());
		for (ItemCommandType type : ItemCommandType.values()) {
			long count =
				 commands.stream().flatMap(c -> c.getOperations().stream()).filter(op -> op.getItemCommandType() == type).count();
			if (count != 0L) {
				log.info("  {}: {}", type, count);
			}
		}

		if (!commands.isEmpty()) {
			t0 = System.currentTimeMillis();
			EmbyCmdExec embyCmdExec = new EmbyCmdExec(embyClient);

			ExecutorService executorService = Executors.newFixedThreadPool(10);

			commands.forEach(c -> executorService.execute(() -> embyCmdExec.execute(c)));

			executorService.shutdown();

			try {
				while (true) {
					boolean isDone = executorService.awaitTermination(10L, TimeUnit.SECONDS);
					if (isDone) {
						break;
					}
					if (executorService instanceof ThreadPoolExecutor) {
						ThreadPoolExecutor tpe = (ThreadPoolExecutor) executorService;
						int x = tpe.getActiveCount() + tpe.getQueue().size();
						double percent = 100.0 * x / commands.size();
						System.out.println(String.format("%5.1f", percent));
					}
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			t1 = System.currentTimeMillis();
			log.info("executed {} commands in {} ms", commands.size(), t1 - t0);
		}
	}

}

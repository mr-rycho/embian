package pl.rychu.embian.crawl;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.rychu.embian.crawl.visitors.sort.ItemVisitorResetSortName;
import pl.rychu.embian.crawl.visitors.sort.ItemVisitorSortByDate;
import pl.rychu.embian.crawl.visitors.tag.ItemVisitorTagSync;
import pl.rychu.embian.emby.EmbyClient;
import pl.rychu.embian.emby.EmbyCmdExec;
import pl.rychu.embian.fs.Filesystem;
import pl.rychu.embian.fs.FilesystemDrive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("squid:S106")
public class CrawlAndCommand {

	private static final Logger log = LoggerFactory.getLogger(CrawlAndCommand.class);

	private static final String OPT_HELP = "help";
	private static final String OPT_RESET_SORT_NAME = "reset-sort-name";
	private static final String OPT_SET_SORT_NAME = "set-sort-name";
	private static final String OPT_SYNC_TAGS = "sync-tags";

	private static final List<String> ALL_VIS = Arrays.asList(OPT_RESET_SORT_NAME, OPT_SET_SORT_NAME, OPT_SYNC_TAGS);
	// --------

	private final Supplier<EmbyClient> embyClientSupplier;

	public CrawlAndCommand(Supplier<EmbyClient> embyClientSupplier) {
		this.embyClientSupplier = embyClientSupplier;
	}

	// --------

	public void exec(String[] args) {
		Options opts = new Options();
		opts.addOption(null, OPT_RESET_SORT_NAME, false, "reset sort names in all directories i.e. make emby recompute " +
			 "it");
		opts.addOption(null, OPT_SET_SORT_NAME, false, "set sort names in date directories e.g. \"2018-08 ...\"");
		opts.addOption(null, OPT_SYNC_TAGS, false, "synchronizes priv* tags with filesystem");
		opts.addOption(null, OPT_HELP, false, "prints this help");

		if (args.length == 0) {
			printHelpAndExitWithStatus(opts, 0);
			return;
		}

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(opts, args);
		} catch (ParseException e) {
			System.out.println("ERROR: error parsing 'crawl' command line");
			System.out.println("message: " + e.getMessage());
			System.exit(1);
			return;
		}

		if (cmd.hasOption(OPT_HELP)) {
			printHelpAndExitWithStatus(opts, 0);
			return;
		}

		boolean anyCmd = ALL_VIS.stream().anyMatch(cmd::hasOption);
		if (!anyCmd) {
			List<String> avo = ALL_VIS.stream().map(s -> "--" + s).collect(toList());
			System.err.println("must pass any of :" + avo);
			System.exit(1);
			return;
		}

		EmbyClient embyClient = embyClientSupplier.get();

		List<ItemVisitor> itemVisitors = new ArrayList<>();
		if (cmd.hasOption(OPT_RESET_SORT_NAME)) {
			itemVisitors.add(new ItemVisitorResetSortName());
		}
		if (cmd.hasOption(OPT_SET_SORT_NAME)) {
			itemVisitors.add(new ItemVisitorSortByDate());
		}
		if (cmd.hasOption(OPT_SYNC_TAGS)) {
			Filesystem filesystem = new FilesystemDrive();
//		Filesystem filesystem = new FilesystemMock();
			itemVisitors.add(new ItemVisitorTagSync(filesystem));
		}

		EmbyCrawler embyCrawler = new EmbyCrawler(embyClient, itemVisitors);

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

			waitWithProgressBar(executorService, commands.size());

			t1 = System.currentTimeMillis();
			log.info("executed {} commands in {} ms", commands.size(), t1 - t0);
		}
	}

	// --------

	private void waitWithProgressBar(ExecutorService executorService, int fullSize) {
		try {
			while (true) {
				boolean isDone = executorService.awaitTermination(10L, TimeUnit.SECONDS);
				if (isDone) {
					break;
				}
				if (executorService instanceof ThreadPoolExecutor) {
					ThreadPoolExecutor tpe = (ThreadPoolExecutor) executorService;
					int x = tpe.getActiveCount() + tpe.getQueue().size();
					double percent = 100.0 * x / fullSize;
					System.out.println(String.format("%5.1f", percent));
				}
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void printHelpAndExitWithStatus(Options options, int exitStatus) {
		HelpFormatter hf = new HelpFormatter();
		hf.setSyntaxPrefix("");
		hf.printHelp("crawl options:", options);
		System.exit(exitStatus);
	}


}

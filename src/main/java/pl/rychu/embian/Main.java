package pl.rychu.embian;

import org.apache.commons.cli.*;
import org.glassfish.jersey.client.ClientConfig;
import pl.rychu.embian.browse.Browse;
import pl.rychu.embian.crawl.CrawlAndCommand;
import pl.rychu.embian.emby.EmbyClient;
import pl.rychu.embian.emby.EmbyClientFactory;
import pl.rychu.embian.refresh.JobMon;
import pl.rychu.embian.refresh.Refresh;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * Created on 2018-06-10 by rychu.
 */
@SuppressWarnings("squid:S106")
public class Main {

	private static final String EMBIAN_VERSION = "embian.02";

	private static final String OPT_URL = "url";
	private static final String OPT_USER = "user";
	private static final String OPT_PASS = "pass";
	private static final String OPT_HELP = "help";
	private static final String OPT_VER = "version";

	private static final String CMD_CRAWL = "crawl";
	private static final String CMD_BROWSE = "browse";
	private static final String CMD_REFRESH = "refresh";
	private static final String CMD_JOB_MON = "job-mon";
	private static final String[] CMDS = new String[]{CMD_CRAWL, CMD_BROWSE, CMD_REFRESH, CMD_JOB_MON};

	// ----------

	public static void main(String[] args) {
		Options opts = new Options();
		opts.addOption(null, OPT_URL, true, "emby url");
		opts.addOption(null, OPT_USER, true, "user");
		opts.addOption(null, OPT_PASS, true, "pass");
		opts.addOption(null, OPT_HELP, false, "prints this help");
		opts.addOption(null, OPT_VER, false, "prints version");

		if (args.length == 0) {
			printHelp(opts);
			System.exit(1);
			return;
		}

		Properties props = loadDefaultConfig();

		try {
			CommandLine cmd = new DefaultParser().parse(opts, args, props, true);

			if (cmd.hasOption(OPT_HELP)) {
				printHelp(opts);
				System.exit(0);
				return;
			}

			if (cmd.hasOption(OPT_VER)) {
				System.out.println(EMBIAN_VERSION);
				System.exit(0);
				return;
			}

			if (!cmd.hasOption(OPT_URL) || !cmd.hasOption(OPT_USER) || !cmd.hasOption(OPT_PASS)) {
				System.out.println("ERROR: incomplete login params (url/user/pass)");
				printHelp(opts);
				System.exit(1);
				return;
			}

			Supplier<EmbyClient> embyClientSupplier = () -> {
				Client client = ClientBuilder.newClient(new ClientConfig());
				return new EmbyClientFactory().authenticate(client, cmd.getOptionValue(OPT_URL), cmd.getOptionValue(OPT_USER),
					 cmd.getOptionValue(OPT_PASS));
			};

			String[] argsCmd = cmd.getArgs();
			if (argsCmd == null || argsCmd.length == 0) {
				System.out.println("ERROR: no command");
				printHelp(opts);
				System.exit(1);
				return;
			}
			String command = argsCmd[0];
			String[] cmdArgs = Arrays.copyOfRange(argsCmd, 1, argsCmd.length);
			if (command.equals(CMD_CRAWL)) {
				new CrawlAndCommand(embyClientSupplier).exec(cmdArgs);
			} else if (command.equals(CMD_BROWSE)) {
				new Browse(embyClientSupplier).exec(cmdArgs);
			} else if (command.equals(CMD_REFRESH)) {
				new Refresh(embyClientSupplier).exec(cmdArgs);
			} else if (command.equals(CMD_JOB_MON)) {
				new JobMon(embyClientSupplier).exec(cmdArgs);
			} else {
				System.out.println("Unrecognized command: " + command);
				System.exit(1);
				return;
			}
		} catch (ParseException e) {
			System.out.println("ERROR: " + e.getMessage());
			System.exit(1);
		}
	}

	// --------

	private static Properties loadDefaultConfig() {
		String[] envHomes = new String[]{"HOME", "USERPROFILE"};
		String homeDir = getFirstEnv(envHomes);
		String fname = (homeDir != null ? homeDir : ".") + "/.embian";
		if (new File(fname).exists()) {
			Properties props = new Properties();
			try (InputStream is = new FileInputStream(fname)) {
				props.load(is);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			return props;
		} else {
			return new Properties();
		}
	}

	private static String getFirstEnv(String... envKeys) {
		return Arrays.stream(envKeys).map(System::getenv).filter(Objects::nonNull).findFirst().orElse(null);
	}

	private static void printHelp(Options options) {
		HelpFormatter hf = new HelpFormatter();
		hf.setSyntaxPrefix("");
		System.out.println("java -jar ... [login options] [command] [command's parameters]");
		hf.printHelp("options", options);
		System.out.println("login options may alternatively be read from ~/.embian file");
		System.out.println("Commands:");
		for (String cmd : CMDS) {
			System.out.println("  " + cmd);
		}
	}

}

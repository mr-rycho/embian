package pl.rychu.embian;

import org.apache.commons.cli.*;
import org.glassfish.jersey.client.ClientConfig;
import pl.rychu.embian.browse.Browse;
import pl.rychu.embian.crawl.CrawlAndCommand;
import pl.rychu.embian.emby.EmbyClient;
import pl.rychu.embian.emby.EmbyClientFactory;

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

	private static final String OPT_HOST = "host";
	private static final String OPT_USER = "user";
	private static final String OPT_PASS = "pass";

	private static final String CMD_CRAWL = "crawl";
	private static final String CMD_BROWSE = "browse";
	private static final String[] CMDS = new String[]{CMD_CRAWL, CMD_BROWSE};

	// ----------

	public static void main(String[] args) {
		Options opts = new Options();
		opts.addOption(null, OPT_HOST, true, "emby url");
		opts.addOption(null, OPT_USER, true, "user");
		opts.addOption(null, OPT_PASS, true, "pass");

		if (args.length == 0) {
			printHelp(opts);
			System.exit(1);
			return;
		}

		Properties props = loadDefaultConfig();

		try {
			CommandLine cmd = new DefaultParser().parse(opts, args, props, true);

			if (!cmd.hasOption(OPT_HOST) || !cmd.hasOption(OPT_USER) || !cmd.hasOption(OPT_PASS)) {
				System.out.println("ERROR: incomplete login params (host/user/pass)");
				printHelp(opts);
				System.exit(1);
				return;
			}

			Supplier<EmbyClient> embyClientSupplier = () -> {
				Client client = ClientBuilder.newClient(new ClientConfig());
				return new EmbyClientFactory().authenticate(client, cmd.getOptionValue(OPT_HOST), cmd.getOptionValue(OPT_USER)
					 , cmd.getOptionValue(OPT_PASS));
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
			}
		} catch (ParseException e) {
			throw new RuntimeException(e);
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
		hf.printHelp("login options: (alternatively may be read from ~/.embian file)", options);
		System.out.println("Commands:");
		for (String cmd : CMDS) {
			System.out.println("  " + cmd);
		}
	}

}

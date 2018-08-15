package pl.rychu.embian;

import org.apache.commons.cli.*;
import org.junit.Test;

import java.util.Properties;

import static org.fest.assertions.Assertions.assertThat;

public class MainTest {

	@Test
	public void checkCmdline() throws ParseException {
		Options opts = new Options();
		opts.addOption("", "hostname", true, "hostname");

		CommandLineParser parser = new DefaultParser();
		String[] args = new String[]{"--hostname", "tosia", "crawl", "--depth", "5"};
		CommandLine cmd = parser.parse(opts, args, true);
		assertThat(cmd.getArgList()).hasSize(3);
	}

	@Test
	public void checkCmdlineWithDefaults() throws ParseException {
		Options opts = new Options();
		opts.addOption("", "hostname", true, "hostname");
		opts.addOption("", "user", true, "user");
		Properties props = new Properties();
		props.setProperty("user", "rybka");

		DefaultParser parser = new DefaultParser();
		String[] args = new String[]{"--hostname", "tosia", "crawl", "--depth", "5"};
		CommandLine cmd = parser.parse(opts, args, props, true);
		assertThat(cmd.getArgList()).hasSize(3);
		assertThat(cmd.getOptionValue("user")).isEqualTo("rybka");
	}

}

package pl.rychu.embian.crawl.visitors.sort;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created on 2018-08-10 by rychu.
 */
@RunWith(JUnitParamsRunner.class)
public class SortNameCalculatorTest {

	private SortNameCalculator sortNameCalculator;

	@Before
	public void setupSortNameCalculator() {
		sortNameCalculator = new SortNameCalculator();
	}

	// ----------

	@Test
	@Parameters
	public void testSortName(String name, String expSortName) {
		// given - params

		// when
		Optional<String> actSortNameOpt = sortNameCalculator.calcSortName(name);

		// then
		assertThat(actSortNameOpt.isPresent()).isTrue();
		assertThat(actSortNameOpt.get()).isEqualTo(expSortName);
	}

	protected List<Object[]> parametersForTestSortName() {
		List<Object[]> params = new ArrayList<>();

		params.add(new Object[]{"2013-07 Praca", "0020130700 praca"});
		params.add(new Object[]{"2013-07", "0020130700"});
		params.add(new Object[]{"2013 Praca", "0020130000 praca"});
		params.add(new Object[]{"2013", "0020130000"});

		return params;
	}

}
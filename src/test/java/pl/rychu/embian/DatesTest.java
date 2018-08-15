package pl.rychu.embian;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created on 2018-08-01 by rychu.
 */
public class DatesTest {

	@Test
	public void test() {
		String s1 = "0000002013-0000000011-0000000007 psi";
		String s2 = "0000002013-0000000010-0000000005 psi";
		String s3 = "0000002013-0000000011 psi";

		List<String> list = new ArrayList<>(Arrays.asList(s1, s2, s3));
		Collections.sort(list);

		list.forEach(System.out::println);
	}

}

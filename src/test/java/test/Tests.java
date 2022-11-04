package test;

import among.construct.Constructors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

public class Tests{
	@Test public void simpleTest() throws IOException{
		Object[] constructed = TestUtil.construct(
				TestUtil.expectSourceFrom("test", "simple1"),
				Constructors.INT,
				Object[]::new);
		System.out.println(Arrays.toString(constructed));
		Assertions.assertArrayEquals(constructed, new Object[]{0, 1, 2, 3, 4, 5});
	}
}

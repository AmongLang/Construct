package test;

import among.construct.Constructor;
import among.construct.Constructors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import test.data.Matrix33;
import test.data.Person;
import test.data.Pos2;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class Tests{
	@Test public void ints() throws IOException{
		Object[] constructed = TestUtil.construct(
				TestUtil.expectSourceFrom("test", "int"),
				Constructors.INT,
				Object[]::new);
		System.out.println(Arrays.toString(constructed));
		Assertions.assertArrayEquals(constructed, new Object[]{0, 1, 2, 3, 4, 5});
	}
	@Test public void listOrElementOfInt() throws IOException{
		Object[] constructed = TestUtil.construct(
				TestUtil.expectSourceFrom("test", "listOrElementOfInt"),
				Constructor.listOrElementOf(Constructors.INT),
				Object[]::new);
		System.out.println(Arrays.toString(constructed));
		Assertions.assertArrayEquals(constructed, new Object[]{
				Collections.singletonList(0),
				Collections.singletonList(1),
				Arrays.asList(0, 1, 2, 3)
		});
	}
	@Test public void person() throws IOException{
		Object[] constructed = TestUtil.construct(
				TestUtil.expectSourceFrom("test", "person"),
				Person.CONSTRUCTOR,
				Object[]::new);
		System.out.println(Arrays.toString(constructed));
		Assertions.assertArrayEquals(constructed, new Object[]{
				new Person("John Doe", "My name is John.", 1.5, 0.002,
						Collections.singletonList("Object class: keter")),
				new Person("Jane Doe", "My name is Jane.", 12345824376987857298435.0, 0.00002,
						Collections.emptyList())
		});
	}
	@Test public void pos2() throws IOException{
		Object[] constructed = TestUtil.construct(
				TestUtil.expectSourceFrom("test", "pos2"),
				Pos2.CONSTRUCTOR,
				Object[]::new);
		System.out.println(Arrays.toString(constructed));
		Assertions.assertArrayEquals(constructed, new Object[]{
				new Pos2(0, 1),
				new Pos2(2, 3),
				new Pos2(4, 5)
		});
	}
	@Test public void matrix() throws IOException{
		Object[] constructed = TestUtil.construct(
				TestUtil.expectSourceFrom("test", "matrix"),
				Matrix33.CONSTRUCTOR,
				Object[]::new);
		System.out.println(Arrays.toString(constructed));
		Assertions.assertArrayEquals(constructed, new Object[]{
				new Matrix33(),
				new Matrix33(
						.1, .2, .3,
						.4, .5, .6,
						.7, .8, .9
				)
		});
	}

	@Test public void intError() throws IOException{
		TestUtil.expectError(
				TestUtil.expectSourceFrom("error", "int"),
				Constructors.INT);
	}
	@Test public void personError() throws IOException{
		TestUtil.expectError(
				TestUtil.expectSourceFrom("error", "person"),
				Person.CONSTRUCTOR);
	}
	@Test public void pos2Error() throws IOException{
		TestUtil.expectError(
				TestUtil.expectSourceFrom("error", "pos2"),
				Pos2.CONSTRUCTOR);
	}
	@Test public void matrixError() throws IOException{
		TestUtil.expectError(
				TestUtil.expectSourceFrom("error", "matrix"),
				Matrix33.CONSTRUCTOR);
	}
}

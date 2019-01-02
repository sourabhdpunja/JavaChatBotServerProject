package edu.northeastern.ccs.im.guidance;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Test for inappropriate filter
 * @author shweta
 *
 */
public class TestInappropriateFilter {
	/**
	 * testing inappropriate filter
	 * @throws IOException
	 */
	@Test
	public void test() throws IOException {

		InappropriateFilter filter = new InappropriateFilter();

		filter.buildTree("src/main/resources/vulgar.txt");

		String badWord = "asshole off ";
		String badWord1 = "mathematics";

		String result = filter.filterBadWords(badWord);
		assertEquals(result,"******* off ");
		assertEquals(filter.inappropriate(badWord), true);
		assertEquals(filter.inappropriate(badWord1), false);
		String result1 = filter.filterBadWords(badWord1);
		assertEquals(result1,badWord1);
	}
}

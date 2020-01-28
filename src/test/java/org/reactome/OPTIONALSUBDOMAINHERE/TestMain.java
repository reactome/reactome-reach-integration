package org.reactome.OPTIONALSUBDOMAINHERE;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

/**
 * Template class for unit tests
 *
 * NOTE: To be automatically included in tests run by the Maven Surefire Plugin (executed by the command "mvn test"),
 * Java files created for unit tests must have a name that:
 * - Starts with "Test"
 * - Ends with "Test"
 * - Ends with "Tests"
 * - Ends with "TestCase"
 *
 * For more detail and unit test inclusion/exclusion customization see:
 * http://maven.apache.org/components/surefire/maven-surefire-plugin/examples/inclusion-exclusion.html
 *
 * NOTE: JUnit 5 and Hamcrest are recommended and a sample test is included below.
 *
 * CAVEAT:
 * If using PowerMock, JUnit4 will be required as PowerMock is not yet compatible with JUnit5 (as of January 28, 2020)
 */
public class TestMain {

	@Test
	public void testTemplate() {
		assertThat("TEST".toLowerCase(), is(equalTo("test")));
	}
}

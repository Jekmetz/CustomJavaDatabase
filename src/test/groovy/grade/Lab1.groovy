package grade

import static org.junit.Assert.*
import org.junit.Test
import org.junit.BeforeClass
import org.junit.AfterClass

class Lab1 {
	private static int passed_tests = 0, total_tests = 0
	
	@BeforeClass
	static void explain() {
		System.out.println('Confirming changed student details in the Server class...')
	}
	
	@Test
	void testChangedName() {
		total_tests++;
		
		assertNotEquals(
			'You must set your full name in the Server class',
			'Your Name',
			core.Server.STUDENT_NAME
		)
		
		System.out.println("[Name]  ${core.Server.STUDENT_NAME}")
		passed_tests++;
	}
	
	@Test
	void testChangedIDNum() {
		total_tests++;

		assertNotEquals(
			'You must set your student ID number in the Server class',
			'000000000',
			core.Server.STUDENT_IDNUM
		)
		
		System.out.println("[IDNum]    ${core.Server.STUDENT_IDNUM}")
		passed_tests++;
	}
	
	@Test
	void testChangedEmail() {
		total_tests++;

		assertNotEquals(
			'You must set your email address in the Server class',
			'email@mix.wvu.edu',
			core.Server.STUDENT_EMAIL
		)
		
		System.out.println("[Email] ${core.Server.STUDENT_EMAIL}")
		passed_tests++;
	}
	
	@AfterClass
	static void report() {
		if (passed_tests == total_tests)
			System.out.println('\nStudent detail changes confirmed in the Server class.\nThis does not confirm the accuracy of those details.\nCheck the details for accuracy before submitting.\n')
		else
			System.err.println('\nYou must accurately set all the student details in the Server class.\n')
		
		final double rate = passed_tests / (double) total_tests
		System.out.println(
			'[L1 PASSED ' + Math.round(rate * 100) + '% OF UNIT TESTS]',
		)
	}
}
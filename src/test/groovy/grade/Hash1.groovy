package grade

import static org.junit.jupiter.api.Assertions.*
import static org.junit.jupiter.api.DynamicTest.*
import static org.junit.jupiter.api.DynamicContainer.*

import org.junit.jupiter.api.*
import java.util.stream.*

public class Hash1 {
	static final MAP_OPERATIONS	= 500,
				 RNG_SEED		= 2019_01,
				 KEY_ALPHABET	= 'abcde1234_'
	
	static successes = 0
	
	@DisplayName('Canonical Battery')
	@TestFactory
	def battery() {
		final subject = new adt.HashMap<String, Integer>()
		if (subject instanceof java.util.AbstractMap)
			return [
				dynamicTest('Hash Map Compliance', {
					fail('Expected an original hash map, but project uses built-in hash map.')
				})
			]
		
		final exemplar = new java.util.HashMap<String, Integer>()
		
		final rng = new Random(RNG_SEED),
			key = {rng.ints((long) (Math.abs(rng.nextGaussian())*1.5+1)).mapToObj({i -> KEY_ALPHABET[i % KEY_ALPHABET.size()]}).collect(Collectors.joining())},
			val = {rng.nextInt(1000)},
			test = {method, ...args ->
				final call = "$method(${args ? args.inspect()[1..-2] : ''})"
				dynamicTest(call, {
					try {
						assertEquals(
							exemplar."$method"(*args),
							subject."$method"(*args),
							"$call must return correct results"
						)
					}
					catch (Exception e) {
						fail(
							"$call must not throw exception"
						)
					}
					successes++
				})
			}
			
		rng.doubles(MAP_OPERATIONS).mapToObj({ p -> 
			if      (p < 0.01) test('isEmpty')
			else if (p < 0.05) test('size')
			else if (p < 0.45) test('put', key(), val())
			else if (p < 0.70) test('get', key())
			else if (p < 0.95) test('remove', key())
			else               test('containsKey', key())
		})
	}
	
	@AfterAll
	static void report() {
		final rate = Math.round(successes / MAP_OPERATIONS * 100)
		System.out.println("[H1 PASSED $rate% OF BATTERY TESTS]")
	}
}
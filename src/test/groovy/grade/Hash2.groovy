package grade

import static org.junit.jupiter.api.Assertions.*
import static org.junit.jupiter.api.DynamicTest.*
import static org.junit.jupiter.api.DynamicContainer.*

import org.junit.jupiter.api.*
import java.util.stream.*
import org.apache.commons.math3.stat.regression.SimpleRegression

public class Hash2 {
	static final MAP_OPERATIONS	= 250_000,
				 SKIP_BEFORE	= 5_000,
				 PLOT_INTERVAL	= 500,
				 SLOPE_BOUND	= 0.2,
				 RNG_SEED		= 2019_01,
				 HIT_RATE		= 0.8,
				 HIT_CACHE		= 50

	static final CALL_LOGGER = null
	// DO NOT LOG MAP CALLS:   null
	// LOG CALLS TO FILE:	   new PrintStream("h2.txt")
	
	static milestones = 0
	
	@DisplayName('Canonical Analysis')
	@TestFactory
	def analysis() {
		final subject = new adt.HashMap<String, Integer>()
		if (subject instanceof java.util.AbstractMap)
			return [
				dynamicTest('Hash Map Compliance', {
					fail('Expected an original hash map, but project uses built-in hash map.')
				})
			]
		
		final exemplar = new java.util.HashMap<String, Integer>()
		
		final rng = new Random(RNG_SEED),
			plot = new SimpleRegression(),
			cache = [RNG_SEED.toString()] as ArrayList,
			key = {
				if (rng.nextDouble() < HIT_RATE) {
					cache[rng.nextInt(cache.size())]
				}
				else {
					if (cache.size() >= HIT_CACHE)
						cache.remove(rng.nextInt(cache.size()))
					cache.add(new BigInteger(128, rng).toString(Character.MAX_RADIX))
					cache[-1]
				}
			},
			val = {rng.nextInt()},
			test = {method, ...args ->
				final call = "$method(${args ? args.inspect()[1..-2] : ''})"
				CALL_LOGGER?.println("map.$call;".replace("'", '"'))
				assertEquals(
					exemplar."$method"(*args),
					subject."$method"(*args),
					"$call must return correct results"
				)
			}
		
		CALL_LOGGER?.println("Map map = new adt.HashMap();")
		IntStream.rangeClosed(1, MAP_OPERATIONS).filter({n -> n % PLOT_INTERVAL == 0}).mapToObj({ upto -> 
			dynamicTest(String.format("%,d ops [%s]", upto, upto >= SKIP_BEFORE ? 'correctness, analysis' : 'correctness only'), {
				final started = System.nanoTime()
				
				rng.doubles(PLOT_INTERVAL).forEach({ p ->
					if      (p < 0.01) test('isEmpty')
					else if (p < 0.05) test('size')
					else if (p < 0.45) test('put', key(), val())
					else if (p < 0.70) test('get', key())
					else if (p < 0.95) test('remove', key())
					else               test('containsKey', key())
				})
				
				final elapsed = System.nanoTime() - started
				final average = elapsed / PLOT_INTERVAL
				
				plot.addData(upto, average)
				
				final slope = plot.getSlope()
				final alpha = plot.getSignificance()
				
				System.out.printf(String.format(
					"%,7d ops: %,.3f ms/op, slope %.3f, alpha %.3f\n",
					upto,
					average / 1_000,
					slope,
					alpha
				))
				
				if (upto >= SKIP_BEFORE)
					assertTrue(
						slope <= SLOPE_BOUND,
						"slope $slope of average runtime plot must not exceed bound $SLOPE_BOUND"
					)

				milestones++
			})
		})
	}
	
	@AfterAll
	static void report() {
		final rate = Math.round(milestones / (MAP_OPERATIONS / PLOT_INTERVAL) * 100)
		System.out.println("[H2 PASSED $rate% OF ANALYSIS TESTS]")
	}
}

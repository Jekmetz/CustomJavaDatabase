package grade

import static org.junit.jupiter.api.Assertions.*
import static org.junit.jupiter.api.DynamicTest.*
import static org.junit.jupiter.api.DynamicContainer.*

import org.junit.jupiter.api.*
import java.util.stream.*
import org.apache.commons.math3.stat.regression.SimpleRegression

public class Hash3 {
	static final BATCH_COUNT	= 500,
			     BATCH_SIZE 	= 100,
				 RNG_SEED		= 2019_01,
				 KV_ALPHABET	= '0123456789abcdef'

	static final CALL_LOGGER = null
	// DO NOT LOG MAP CALLS:   null
	// LOG CALLS TO FILE:	   new PrintStream("h3.txt")
	
	static milestones = 0
	
	@DisplayName('Non-Canonical Battery')
	@TestFactory
	def battery() {
		final subject = new adt.HashMap<String, String>()
		if (subject instanceof java.util.AbstractMap)
			return [
				dynamicTest('Hash Map Compliance', {
					fail('Expected an original hash map, but project uses built-in hash map.')
				})
			]
		
		final exemplar = new java.util.HashMap<String, String>()
		
		final rng = new Random(RNG_SEED),
			data = {rng.ints((long) (Math.abs(rng.nextGaussian())*1.5+1)).mapToObj({i -> KV_ALPHABET[i % KV_ALPHABET.size()]}).collect(Collectors.joining())}
		
		CALL_LOGGER?.println("Map map = new adt.HashMap();")
		CALL_LOGGER?.println("Map batch = new java.util.HashMap();")
		IntStream.rangeClosed(1, BATCH_COUNT).mapToObj({ puts -> 
			dynamicTest(String.format("%,d batches [%,d puts]", puts, puts * BATCH_SIZE), {
				CALL_LOGGER?.println("batch.clear();")
				final batch = [:] as HashMap
				while (batch.size() < BATCH_SIZE) {
					final k = data(),
						  v = data()
					CALL_LOGGER?.println("batch.put(${k.inspect()}, ${v.inspect()});".replace("'", '"'))
					batch[k] = v
				}
				
				CALL_LOGGER?.println("map.putAll(batch);")
				exemplar.putAll(batch)
				subject.putAll(batch)
				
				final v = data()
				assertEquals(
					exemplar.containsValue(v),
					subject.containsValue(v),
					"containsValue(${v.inspect()}) must return correct results"
				)
				
				assertEquals(
					exemplar.size(),
					subject.size(),
					'size() must return correct results'
				)
				
				assertEquals(
					exemplar.entrySet(),
					subject.entrySet(),
					'entrySet() must return correct results'
				)
				
				assertEquals(
					exemplar.keySet(),
					subject.keySet(),
					'keySet() must return correct results'
				)
				
				assertEquals(
					exemplar.values().toSorted(),
					subject.values().toSorted(),
					'values() must return correct results (disregarding order)'
				)
				
				assertTrue(
					exemplar.equals(subject),
					'other_map.equals(your_map) must return correct results'
				)
				
				assertTrue(
					subject.equals(exemplar),
					'your_map.equals(other_map) must return correct results'
				)
				
				milestones++
			})
		})
	}
	
	@AfterAll
	static void report() {
		final rate = Math.round(milestones / BATCH_COUNT * 100)
		System.out.println("[H3 PASSED $rate% OF BATTERY TESTS]")
	}
}

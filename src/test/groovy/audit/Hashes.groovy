package audit


import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.*
import java.lang.reflect.*

public class Hashes {
	static subject
	
	@BeforeAll
	static void initialize() {
		subject = new adt.HashMap<String, Integer>()
	}
	
	@Test
	void testMapIsOriginal() {
		if (subject instanceof java.util.AbstractMap)
			fail('Map is not original hash map, inherits from built-in hash map.')
	}
	
	@Test
	void testNoForbiddenClassFields() {
		final allowed = [] as Set,
			  forbidden = [] as Set;
		
		final internal = ['adt', 'core', 'driver'],
			  exempt = ['java.lang']
			  
		def clazz = adt.HashMap.class;
		while (clazz != null) {
			for (Field f: clazz.getFields() + clazz.getDeclaredFields()) {
				f.setAccessible(true)
				if (f.get(subject) != null) {
					final gc = f.get(subject).getClass()
					final used = gc.isArray() ? gc.getComponentType() : gc;
					if (!used.isPrimitive() && !used.isInterface()) {
						if (!(used.getPackage()?.getName() in exempt + internal))
							forbidden.add(used)
						else if (!used.getPackage()?.getName() in internal)
							allowed.add(used)
					}
				}
				f.setAccessible(false)
			}
			clazz = clazz.getSuperclass();
		}
		
		if (allowed.size() + forbidden.size())
			System.out.println('Map fields use external classes:')
		allowed.each({System.out.println(it)})
		forbidden.each({System.err.println("$it (forbidden)")})
		
		if (forbidden)
			fail("Map fields use forbidden ${forbidden.join(', ')}.")
	}
}
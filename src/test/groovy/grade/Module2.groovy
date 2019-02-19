package grade

import static org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*

import adt.Response
import adt.Table
import core.Server

class Module2 {
	static query_data = [
		// PREREQUISITE
		[ true,  'CREATE TABLE table_1 (PRIMARY STRING ps, INTEGER i, BOOLEAN b)', 'insertion depends on table creation' ],
		
		// ORDERING
		[ true,  'INSERT INTO table_1 (ps, i, b) VALUES ("a1", 2, true)', null ],
		[ true,  'INSERT INTO table_1 (ps, b, i) VALUES ("a3", false, 4)', 'reordering defined columns is allowed' ],
		[ true,  'INSERT INTO table_1 (i, ps, b) VALUES (5, "a6", true)', 'reordering defined columns is allowed' ],
		[ true,  'INSERT INTO table_1 (b, ps, i) VALUES (false, "a7", 8)', 'reordering defined columns is allowed' ],
		[ true,  'INSERT INTO table_1 (i, b, ps) VALUES (9, true, "a10")', 'reordering defined columns is allowed' ],
		[ true,  'INSERT INTO table_1 (b, i, ps) VALUES (false, 11, "a12")', 'reordering defined columns is allowed' ],
		
		// OMISSION
		[ true,  'INSERT INTO table_1 (ps, i) VALUES ("b1", 2)', 'omitting columns is allowed' ],
		[ true,  'INSERT INTO table_1 (ps, b) VALUES ("b3", true)', 'omitting columns is allowed' ],
		[ true,  'INSERT INTO table_1 (i, ps) VALUES (4, "b5")', 'omitting columns is allowed' ],
		[ true,  'INSERT INTO table_1 (b, ps) VALUES (false, "b6")', 'omitting columns is allowed' ],
		[ true,  'INSERT INTO table_1 (ps) VALUES ("b7")', 'omitting columns is allowed' ],
		
		// SEQUENCES
		[ false, 'INSERT INTO table_1 (ps, i, b, i) VALUES ("c1", 2, true, 3)', 'column names cannot be repeated' ],
		[ false, 'INSERT INTO table_1 (ps, ps) VALUES ("c4", "c5")', 'column names cannot be repeated' ],
		[ false, 'INSERT INTO table_1 () VALUES ()', 'at least 1 column must be defined' ],
		[ false, 'INSERT INTO table_1 (ps, i, b) VALUES ("c6", 7, false, 8)', 'the number of values must match the number of defined columns' ],
		[ false, 'INSERT INTO table_1 (ps, i, b) VALUES ("c9", 10)', 'the number of values must match the number of columns' ],
		
		// STRINGS
		[ true,  'INSERT INTO table_1 (ps) VALUES ("0 d1+d2 d3! ")', 'strings support arbitrary whitespace and non-alphanumeric symbols' ],
		[ true,  'INSERT INTO table_1 (ps) VALUES ("456")', 'strings can contain integer-like text' ],
		[ true,  'INSERT INTO table_1 (ps) VALUES ("null")', 'strings can contain keyword-like text ' ],
		[ true,  'INSERT INTO table_1 (ps) VALUES ("")', 'strings can be empty' ],
		[ false, 'INSERT INTO table_1 (ps) VALUES (7)', 'string columns cannot accept integer literals' ],
		[ false, 'INSERT INTO table_1 (ps) VALUES (true)', 'string columns cannot accept boolean literals' ],
		
		// BOOLEANS
		[ true,  'INSERT INTO table_1 (ps, b) VALUES ("e1", true)', 'lowercase boolean literals are allowed' ],
		[ true,  'INSERT INTO table_1 (ps, b) VALUES ("e2", false)', 'lowercase boolean literals are allowed' ],
		[ true,  'INSERT INTO table_1 (ps, b) VALUES ("e3", TRUE)', 'uppercase boolean literals are allowed' ],
		[ true,  'INSERT INTO table_1 (ps, b) VALUES ("e4", FALSE)', 'uppercase boolean literals are allowed' ],
		[ true,  'INSERT INTO table_1 (ps, b) VALUES ("e5", null)', 'booleans can be null' ],
		[ true,  'INSERT INTO table_1 (ps, b) VALUES ("e6", NULL)', 'booleans can be null' ],
		[ false, 'INSERT INTO table_1 (ps, b) VALUES ("e7", "true")', 'boolean columns cannot accept string literals' ],
		[ false, 'INSERT INTO table_1 (ps, b) VALUES ("e8", 9)', 'boolean columns cannot accept integer literals' ],
		
		// INTEGERS
		[ true,  'INSERT INTO table_1 (ps, i) VALUES ("f1", -200)', 'negative integer literals are supported' ],
		[ true,  'INSERT INTO table_1 (ps, i) VALUES ("f3", 0)', 'zero is a valid integer literal' ],
		[ true,  'INSERT INTO table_1 (ps, i) VALUES ("f4", 500)', 'positive integer literals are supported' ],
		[ true,  'INSERT INTO table_1 (ps, i) VALUES ("f6", +700)', 'optional positive signs are supported' ],
		[ true,  'INSERT INTO table_1 (ps, i) VALUES ("f8", null)', 'integers can be null' ],
		[ false, 'INSERT INTO table_1 (ps, i) VALUES ("f9", 010)', 'leading zeroes are not supported' ],
		[ false, 'INSERT INTO table_1 (ps, i) VALUES ("f11", 12.0)', 'integer literals cannot contain decimal points' ],
		[ false, 'INSERT INTO table_1 (ps, i) VALUES ("f13", "14")', 'integer columns cannot accept string literals' ],
		[ false, 'INSERT INTO table_1 (ps, i) VALUES ("f15", true)', 'integer columns cannot accept boolean literals' ],
		
		// PRIMARY COLUMN STRING (INDEX ZERO)
		[ false, 'INSERT INTO table_1 (i, b) VALUES (1, true)', 'every row requires a primary column value' ],
		[ false, 'INSERT INTO table_1 (ps) VALUES (null)', 'primary column values cannot be null' ],
		[ false, 'INSERT INTO table_1 (ps, i, b) VALUES (null, 2, false)', 'primary column values cannot be null' ],
		[ false, 'INSERT INTO table_1 (ps, i, b) VALUES ("g3", 4, true); INSERT INTO table_1 (ps, i, b) VALUES ("g3", 5, false)', 'different rows may not share the same primary column value' ],
		
		// PRIMARY COLUMN INTEGER (INDEX NON-ZERO)
		[ true,  'CREATE TABLE table_2 (STRING s, PRIMARY INTEGER pi, BOOLEAN b)', 'insertion depends on table creation' ],
		[ true,  'INSERT INTO table_2 (s, pi, b) VALUES ("h1", 2, true)', 'primary column need not be leftmost' ],
		[ true,  'INSERT INTO table_2 (s, b, pi) VALUES ("h3", false, 4)', 'primary column need not be leftmost' ],
		[ true,  'INSERT INTO table_2 (pi, s, b) VALUES (5, "h6", true)', 'primary column need not be leftmost' ],
		[ true,  'INSERT INTO table_2 (b, s, pi) VALUES (false, "h7", 8)', 'primary column need not be leftmost' ],
		[ true,  'INSERT INTO table_2 (pi, b, s) VALUES (9, true, "h10")', 'primary column need not be leftmost' ],
		[ true,  'INSERT INTO table_2 (b, pi, s) VALUES (false, 11, "h12")', 'primary column need not be leftmost' ],
		[ false, 'INSERT INTO table_2 (s, b) VALUES ("h13", true)', 'every row requires a primary column value' ],
		[ false, 'INSERT INTO table_2 (pi) VALUES (null)', 'primary column values cannot be null' ],
		[ false, 'INSERT INTO table_2 (s, pi, b) VALUES ("h14", null, false)', 'primary column values cannot be null' ],
		[ false, 'INSERT INTO table_2 (s, pi, b) VALUES ("h15", 16, true); INSERT INTO table_2 (s, pi, b) VALUES ("h17", 16, false)', 'different rows may not share the same primary column value' ],
		
		// PRIMARY COLUMN BOOLEAN (INDEX NON-ZERO)
		[ true,  'CREATE TABLE table_3 (STRING s, INTEGER i, PRIMARY BOOLEAN pb)', 'insertion depends on table creation' ],
		[ true,  'INSERT INTO table_3 (s, i, pb) VALUES ("i1", 2, true)', 'primary column need not be leftmost' ],
		[ true,  'INSERT INTO table_3 (s, pb, i) VALUES ("i3", false, 4)', 'primary column need not be leftmost' ],
		[ false, 'INSERT INTO table_3 (s, i) VALUES ("i5", 6)', 'every row requires a primary column value' ],
		[ false, 'INSERT INTO table_3 (s, i, pb) VALUES ("i7", 8, null)', 'primary column values cannot be null' ],
		[ false, 'INSERT INTO table_3 (s, i, pb) VALUES ("i9", 10, true)', 'different rows may not share the same primary column value' ],
		[ false, 'INSERT INTO table_3 (s, i, pb) VALUES ("i11", 12, false)', 'different rows may not share the same primary column value' ],
		
		// SHORT FORM
		[ true,  'INSERT INTO table_1 VALUES ("j1", 2, true)', 'column names can be omitted if all values are ordered' ],
		[ false, 'INSERT INTO table_1 VALUES ("j3", 4, false, null)', 'the number of values must match the number of columns in the schema' ],
		[ false, 'INSERT INTO table_1 VALUES ("j5", 6)', 'the number of values must match the number of columns in the schema' ],
		[ false, 'INSERT INTO table_1 VALUES (null, 7, true)', 'primary column values cannot be null' ],
		[ false, 'INSERT INTO table_1 VALUES ("j8", 9, false); INSERT INTO table_1 VALUES ("j8", 10, true)', 'different rows may not share the same primary column value' ],
		[ false, 'INSERT INTO table_1 VALUES (true, 11, "j12")', 'the ordered types of values must match the ordered types of columns in the schema' ],
	
		// INSERT EDGE CASES		
		[ false, 'INSERT INTO table_1 ps, i, b VALUES ("k1", 2, true)', 'the parentheses are required for the column names when given' ],
		[ false, 'INSERT INTO table_1 (ps, i, b) VALUES "k3", 4, false', 'the parentheses are required for the values' ],
		[ false, 'INSERT INTO table_1 VALUES "k5", 6, false', 'the parentheses are required for the values' ],
		[ false, 'INSERT INTO table_4 (ps, i, b) VALUES ("k7", 8, false)', 'the table name must exist' ],

		// DUMP TABLE
		[ true,  'DUMP TABLE table_1', 'dump must succeed for valid table' ],
		[ true,  'DUMP TABLE table_2', 'dump must succeed for valid table' ],
		[ true,  'DUMP TABLE table_3', 'dump must succeed for valid table' ],
		[ false, 'DUMP TABLE table_4', 'dump must fail for invalid table' ],
		
		// ROBUSTNESS
		[ true,  'SHOW TABLES', 'row counts for show tables must be correct' ],
		[ true,  'DROP TABLE table_1', 'drop table must succeed for table with rows' ],
		[ false, 'DUMP TABLE table_1', 'dump must fail for dropped table' ],
		[ true,  'SHOW TABLES', 'row counts for show tables must be correct' ],
	]
	
	static data() {[
		query_data,
		serialized_computed_schema,
		serialized_computed_rows,
		serialized_stored_schema,
		serialized_stored_rows
	].transpose()*.flatten()*.toArray()}

	static Server SERVER = new Server()
	
	static int passed_queries = 0
	static int total_queries = 0
	
	@DisplayName('Queries')
	@ParameterizedTest(name = '[{index}] {1}')
	@MethodSource('data')
	void testQuery(
		boolean success_flag, String script_text, String test_reason,
		Map computed_schema, Map computed_rows,
		Map stored_schema, Map stored_rows
	) {
		total_queries++;
		
		System.out.println(script_text)
		
		def queries = script_text.split(';')
		def query_count = queries.size()
		def stored_table_name = stored_schema?.get('table_name')
		
		def responses = SERVER.interpret(script_text)
		def last = responses[-1]
		
		assertEquals(
			query_count,
			responses?.count({it != null}),
			String.format(
				'%s returned wrong number of non-null responses,',
				query_count == 1 ? 'Query' : 'Script'
			)
		)
		
		assertEquals(
			query_count == 1 ? success_flag : [true]*(query_count-1) + [success_flag],
			query_count == 1 ? last.get('success') : responses.collect{it.get('success')},
			String.format(
				'%s %s was expected to %s%s%s.',
				success_flag ? 'Valid' : 'Invalid',
				query_count == 1 ? 'query' : 'script',
				success_flag ? 'succeed' : 'fail',
				query_count == 1 || success_flag ? '' : ' only on last query',
				test_reason ? (' because ' + test_reason) : ''
			)
		)
		
		if (computed_schema) assertEquals(
			computed_schema,
			((Table) last?.get('table'))?.getSchema()?.subMap(computed_schema?.keySet()),
			String.format(
				'%s returned <computed> table with incorrect standard schema properties,',
				query_count == 1 ? 'Query' : 'Last query of script'
			)
		)
		
		if (computed_rows) assertEquals(
			computed_rows,
			last?.get('table'),
			String.format(
				'%s returned <computed> table with incorrect rows,',
				query_count == 1 ? 'Query' : 'Last query of script'
			)
		)
		
		if (stored_schema) assertEquals(
			stored_schema,
			SERVER.database()?.get(stored_table_name)?.getSchema()?.subMap(stored_schema?.keySet()),
			String.format(
				'%s caused <stored> table to have incorrect standard schema properties,',
				query_count == 1 ? 'Query' : 'Last query of script'
				
			)
		)
		
		if (stored_rows) assertEquals(
			stored_rows,
			SERVER.database().get(stored_table_name),
			String.format(
				'%s caused <stored> table to have incorrect rows,',
				query_count == 1 ? 'Query' : 'Last query of script'
			)
		)
		
		passed_queries++;
	}
	
	@AfterAll
	static void report() {
		final double rate = passed_queries / (double) total_queries
		System.out.println(
			'[M2 PASSED ' + Math.round(rate * 100) + '% OF UNIT TESTS]',
		)
	}
	
	static serialized_computed_schema = [
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		null,
		null,
		null,
		null,
		null,
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		null,
		null,
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		null,
		null,
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':'table_2'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':null],
		null,
		null,
		null,
		null,
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'i', 'pb'], 'primary_index':2, 'table_name':'table_3'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'i', 'pb'], 'primary_index':2, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'i', 'pb'], 'primary_index':2, 'table_name':null],
		null,
		null,
		null,
		null,
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':'table_2'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'i', 'pb'], 'primary_index':2, 'table_name':'table_3'],
		null,
		['column_types':['string', 'integer'], 'column_names':['table_name', 'row_count'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		null,
		['column_types':['string', 'integer'], 'column_names':['table_name', 'row_count'], 'primary_index':0, 'table_name':null],
	]

	static serialized_computed_rows = [
		[:],
		['a1':['a1', 2, true]],
		['a3':['a3', 4, false]],
		['a6':['a6', 5, true]],
		['a7':['a7', 8, false]],
		['a10':['a10', 9, true]],
		['a12':['a12', 11, false]],
		['b1':['b1', 2, null]],
		['b3':['b3', null, true]],
		['b5':['b5', 4, null]],
		['b6':['b6', null, false]],
		['b7':['b7', null, null]],
		null,
		null,
		null,
		null,
		null,
		['0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null]],
		['456':['456', null, null]],
		['null':['null', null, null]],
		['':['', null, null]],
		null,
		null,
		['e1':['e1', null, true]],
		['e2':['e2', null, false]],
		['e3':['e3', null, true]],
		['e4':['e4', null, false]],
		['e5':['e5', null, null]],
		['e6':['e6', null, null]],
		null,
		null,
		['f1':['f1', -200, null]],
		['f3':['f3', 0, null]],
		['f4':['f4', 500, null]],
		['f6':['f6', 700, null]],
		['f8':['f8', null, null]],
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		[:],
		[2:['h1', 2, true]],
		[4:['h3', 4, false]],
		[5:['h6', 5, true]],
		[8:['h7', 8, false]],
		[9:['h10', 9, true]],
		[11:['h12', 11, false]],
		null,
		null,
		null,
		null,
		[:],
		[(true):['i1', 2, true]],
		[(false):['i3', 4, false]],
		null,
		null,
		null,
		null,
		['j1':['j1', 2, true]],
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'j1':['j1', 2, true], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'j8':['j8', 9, false], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'g3':['g3', 4, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		[16:['h15', 16, true], 2:['h1', 2, true], 4:['h3', 4, false], 5:['h6', 5, true], 8:['h7', 8, false], 9:['h10', 9, true], 11:['h12', 11, false]],
		[(false):['i3', 4, false], (true):['i1', 2, true]],
		null,
		['table_1':['table_1', 29], 'table_2':['table_2', 7], 'table_3':['table_3', 2]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'j1':['j1', 2, true], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'j8':['j8', 9, false], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'g3':['g3', 4, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		null,
		['table_2':['table_2', 7], 'table_3':['table_3', 2]],
	]

	static serialized_stored_schema = [
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':'table_2'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':'table_2'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':'table_2'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':'table_2'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':'table_2'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':'table_2'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':'table_2'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':'table_2'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':'table_2'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':'table_2'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'i', 'pb'], 'primary_index':2, 'table_name':'table_3'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'i', 'pb'], 'primary_index':2, 'table_name':'table_3'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'i', 'pb'], 'primary_index':2, 'table_name':'table_3'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'i', 'pb'], 'primary_index':2, 'table_name':'table_3'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'i', 'pb'], 'primary_index':2, 'table_name':'table_3'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'i', 'pb'], 'primary_index':2, 'table_name':'table_3'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'i', 'pb'], 'primary_index':2, 'table_name':'table_3'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		null,
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'table_1'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'pi', 'b'], 'primary_index':1, 'table_name':'table_2'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['s', 'i', 'pb'], 'primary_index':2, 'table_name':'table_3'],
		null,
		null,
		null,
		null,
		null,
	]

	static serialized_stored_rows = [
		[:],
		['a1':['a1', 2, true]],
		['a1':['a1', 2, true], 'a3':['a3', 4, false]],
		['a1':['a1', 2, true], 'a6':['a6', 5, true], 'a3':['a3', 4, false]],
		['a1':['a1', 2, true], 'a3':['a3', 4, false], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['a1':['a1', 2, true], 'a10':['a10', 9, true], 'a3':['a3', 4, false], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['a1':['a1', 2, true], 'a10':['a10', 9, true], 'a3':['a3', 4, false], 'a12':['a12', 11, false], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['a1':['a1', 2, true], 'a10':['a10', 9, true], 'a3':['a3', 4, false], 'a12':['a12', 11, false], 'a6':['a6', 5, true], 'a7':['a7', 8, false], 'b1':['b1', 2, null]],
		['a1':['a1', 2, true], 'a10':['a10', 9, true], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'a12':['a12', 11, false], 'a6':['a6', 5, true], 'a7':['a7', 8, false], 'b1':['b1', 2, null]],
		['a1':['a1', 2, true], 'a10':['a10', 9, true], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'a12':['a12', 11, false], 'b5':['b5', 4, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false], 'b1':['b1', 2, null]],
		['a1':['a1', 2, true], 'a10':['a10', 9, true], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'a12':['a12', 11, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'a6':['a6', 5, true], 'a7':['a7', 8, false], 'b1':['b1', 2, null]],
		['a1':['a1', 2, true], 'a10':['a10', 9, true], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'a12':['a12', 11, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'a6':['a6', 5, true], 'b7':['b7', null, null], 'a7':['a7', 8, false], 'b1':['b1', 2, null]],
		['a1':['a1', 2, true], 'a10':['a10', 9, true], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'a12':['a12', 11, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'a6':['a6', 5, true], 'b7':['b7', null, null], 'a7':['a7', 8, false], 'b1':['b1', 2, null]],
		['a1':['a1', 2, true], 'a10':['a10', 9, true], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'a12':['a12', 11, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'a6':['a6', 5, true], 'b7':['b7', null, null], 'a7':['a7', 8, false], 'b1':['b1', 2, null]],
		['a1':['a1', 2, true], 'a10':['a10', 9, true], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'a12':['a12', 11, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'a6':['a6', 5, true], 'b7':['b7', null, null], 'a7':['a7', 8, false], 'b1':['b1', 2, null]],
		['a1':['a1', 2, true], 'a10':['a10', 9, true], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'a12':['a12', 11, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'a6':['a6', 5, true], 'b7':['b7', null, null], 'a7':['a7', 8, false], 'b1':['b1', 2, null]],
		['a1':['a1', 2, true], 'a10':['a10', 9, true], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'a12':['a12', 11, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'a6':['a6', 5, true], 'b7':['b7', null, null], 'a7':['a7', 8, false], 'b1':['b1', 2, null]],
		['a1':['a1', 2, true], 'a10':['a10', 9, true], 'b3':['b3', null, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a3':['a3', 4, false], 'a12':['a12', 11, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'a6':['a6', 5, true], 'b7':['b7', null, null], 'a7':['a7', 8, false], 'b1':['b1', 2, null]],
		['a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'b1':['b1', 2, null], 'a1':['a1', 2, true], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'a6':['a6', 5, true], 'b7':['b7', null, null], '456':['456', null, null], 'a7':['a7', 8, false]],
		['a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'b1':['b1', 2, null], 'a1':['a1', 2, true], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'b7':['b7', null, null], '456':['456', null, null], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'b1':['b1', 2, null], 'a1':['a1', 2, true], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'b7':['b7', null, null], '456':['456', null, null], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'b1':['b1', 2, null], 'a1':['a1', 2, true], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'b7':['b7', null, null], '456':['456', null, null], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'b1':['b1', 2, null], 'a1':['a1', 2, true], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'b7':['b7', null, null], '456':['456', null, null], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'e1':['e1', null, true], 'b1':['b1', 2, null], 'a1':['a1', 2, true], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'b7':['b7', null, null], '456':['456', null, null], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'e1':['e1', null, true], 'e2':['e2', null, false], 'b1':['b1', 2, null], 'a1':['a1', 2, true], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'b7':['b7', null, null], '456':['456', null, null], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'e1':['e1', null, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'b1':['b1', 2, null], 'a1':['a1', 2, true], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'b7':['b7', null, null], '456':['456', null, null], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'e1':['e1', null, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'b1':['b1', 2, null], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'b7':['b7', null, null], '456':['456', null, null], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'e1':['e1', null, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'b1':['b1', 2, null], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'b3':['b3', null, true], 'a3':['a3', 4, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'b7':['b7', null, null], '456':['456', null, null], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'e1':['e1', null, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'b1':['b1', 2, null], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'b3':['b3', null, true], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'b7':['b7', null, null], '456':['456', null, null], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'e1':['e1', null, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'b1':['b1', 2, null], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'b3':['b3', null, true], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'b7':['b7', null, null], '456':['456', null, null], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'e1':['e1', null, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'b1':['b1', 2, null], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'b3':['b3', null, true], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'b7':['b7', null, null], '456':['456', null, null], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'f1':['f1', -200, null], 'e1':['e1', null, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'b1':['b1', 2, null], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'b3':['b3', null, true], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'b7':['b7', null, null], '456':['456', null, null], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'f1':['f1', -200, null], 'e1':['e1', null, true], 'e2':['e2', null, false], 'f3':['f3', 0, null], 'e3':['e3', null, true], 'b1':['b1', 2, null], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'b3':['b3', null, true], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'b7':['b7', null, null], '456':['456', null, null], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'f1':['f1', -200, null], 'e1':['e1', null, true], 'e2':['e2', null, false], 'f3':['f3', 0, null], 'e3':['e3', null, true], 'f4':['f4', 500, null], 'b1':['b1', 2, null], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'b3':['b3', null, true], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'b7':['b7', null, null], '456':['456', null, null], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'g3':['g3', 4, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		[:],
		[2:['h1', 2, true]],
		[4:['h3', 4, false], 2:['h1', 2, true]],
		[4:['h3', 4, false], 5:['h6', 5, true], 2:['h1', 2, true]],
		[8:['h7', 8, false], 2:['h1', 2, true], 4:['h3', 4, false], 5:['h6', 5, true]],
		[8:['h7', 8, false], 9:['h10', 9, true], 2:['h1', 2, true], 4:['h3', 4, false], 5:['h6', 5, true]],
		[8:['h7', 8, false], 9:['h10', 9, true], 2:['h1', 2, true], 11:['h12', 11, false], 4:['h3', 4, false], 5:['h6', 5, true]],
		[8:['h7', 8, false], 9:['h10', 9, true], 2:['h1', 2, true], 11:['h12', 11, false], 4:['h3', 4, false], 5:['h6', 5, true]],
		[8:['h7', 8, false], 9:['h10', 9, true], 2:['h1', 2, true], 11:['h12', 11, false], 4:['h3', 4, false], 5:['h6', 5, true]],
		[8:['h7', 8, false], 9:['h10', 9, true], 2:['h1', 2, true], 11:['h12', 11, false], 4:['h3', 4, false], 5:['h6', 5, true]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'g3':['g3', 4, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		[:],
		[(true):['i1', 2, true]],
		[(false):['i3', 4, false], (true):['i1', 2, true]],
		[(false):['i3', 4, false], (true):['i1', 2, true]],
		[(false):['i3', 4, false], (true):['i1', 2, true]],
		[(false):['i3', 4, false], (true):['i1', 2, true]],
		[(false):['i3', 4, false], (true):['i1', 2, true]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'j1':['j1', 2, true], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'g3':['g3', 4, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'j1':['j1', 2, true], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'g3':['g3', 4, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'j1':['j1', 2, true], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'g3':['g3', 4, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'j1':['j1', 2, true], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'g3':['g3', 4, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'j1':['j1', 2, true], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'j8':['j8', 9, false], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'g3':['g3', 4, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'j1':['j1', 2, true], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'j8':['j8', 9, false], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'g3':['g3', 4, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'j1':['j1', 2, true], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'j8':['j8', 9, false], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'g3':['g3', 4, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'j1':['j1', 2, true], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'j8':['j8', 9, false], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'g3':['g3', 4, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'j1':['j1', 2, true], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'j8':['j8', 9, false], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'g3':['g3', 4, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		null,
		['':['', null, null], 'a10':['a10', 9, true], '0 d1+d2 d3! ':['0 d1+d2 d3! ', null, null], 'a12':['a12', 11, false], 'j1':['j1', 2, true], 'f1':['f1', -200, null], 'f3':['f3', 0, null], 'f4':['f4', 500, null], 'j8':['j8', 9, false], 'b1':['b1', 2, null], 'f6':['f6', 700, null], 'b3':['b3', null, true], 'f8':['f8', null, null], 'b5':['b5', 4, null], 'b6':['b6', null, false], 'b7':['b7', null, null], '456':['456', null, null], 'e1':['e1', null, true], 'g3':['g3', 4, true], 'e2':['e2', null, false], 'e3':['e3', null, true], 'e4':['e4', null, false], 'a1':['a1', 2, true], 'e5':['e5', null, null], 'e6':['e6', null, null], 'a3':['a3', 4, false], 'null':['null', null, null], 'a6':['a6', 5, true], 'a7':['a7', 8, false]],
		[16:['h15', 16, true], 2:['h1', 2, true], 4:['h3', 4, false], 5:['h6', 5, true], 8:['h7', 8, false], 9:['h10', 9, true], 11:['h12', 11, false]],
		[(false):['i3', 4, false], (true):['i1', 2, true]],
		null,
		null,
		null,
		null,
		null,
	]
}
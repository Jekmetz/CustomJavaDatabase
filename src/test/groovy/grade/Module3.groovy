package grade

import static org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*

import adt.Response
import adt.Table
import core.Server

class Module3 {
	static query_data = [
		// PREREQUISITE
		[ true,  'CREATE TABLE test_table (PRIMARY STRING ps, INTEGER i, BOOLEAN b); INSERT INTO test_table VALUES ("x",  null, true); INSERT INTO test_table VALUES ("xy", 5, false); INSERT INTO test_table VALUES ("xx", 4, true); INSERT INTO test_table VALUES ("y",  1, null); INSERT INTO test_table VALUES ("yy", 2, true); INSERT INTO test_table VALUES ("yx", 3, false); INSERT INTO test_table VALUES ("z",  null, null); DUMP TABLE test_table', 'selection depends on table creation and insertion' ],
		
		// STAR FORM
		[ true,  'SELECT * FROM test_table', 'star form is allowed' ],
		[ true,  'select * from test_table', 'lowercase keywords are allowed' ],
		[ false, 'SELECT FROM test_table', 'star or column definition is missing' ],
		[ false, 'SELECT *, ps, i, b FROM test_table', 'star is not supported in a list of column names' ],
		
		// GENERAL FORM
		[ true,  'SELECT ps FROM test_table', 'a single column is allowed' ],
		[ true,  'SELECT ps, i, b FROM test_table', 'multiple columns are allowed' ],
		[ true,  'select ps, i, b from test_table', 'lowercase keywords are allowed' ],
		[ false, 'SELECT PS FROM TEST_TABLE', 'column and table names are case sensitive' ],
		[ false, 'SELECT ps test_table', 'the FROM keyword is required' ],
		[ false, 'ps FROM test_table', 'the SELECT keyword is required' ],
		[ false, 'SELECT ps, i, b', 'the FROM keyword and a table name are required' ],
		[ false, 'SELECT ps i b FROM test_table', 'column definitions must be separated by commas' ],
	
		// ALIASING
		[ true,  'SELECT ps AS primary FROM test_table', 'aliasing is supported' ],
		[ true,  'SELECT ps AS primary, i, b FROM test_table', 'partial aliasing is supported' ],
		[ true,  'SELECT ps AS primary, i AS number, b AS flag FROM test_table', 'aliasing is supported' ],
		[ true,  'select ps as primary, i as number, b as flag from test_table', 'lowercase aliasing keywords are allowed' ],
		[ true,  'SELECT ps, i AS number, b AS flag FROM test_table', 'partial aliasing is supported' ],

		// REORDERING
		[ true,  'SELECT ps, b, i FROM test_table', 'reordering defined columns is allowed' ],
		[ true,  'SELECT i, b, ps FROM test_table', 'reordering defined columns is allowed' ],
		[ true,  'SELECT i AS number, b AS flag, ps AS primary FROM test_table', 'reordering defined columns with aliasing is allowed' ],
		[ true,  'SELECT i AS number, b AS flag, ps FROM test_table', 'reordering defined columns with partial aliasing is allowed' ],
		
		// REPETITION
		[ true,  'SELECT ps, i AS number_1, i AS number_2 FROM test_table', 'selecting repeated columns with unambiguous aliasing is allowed' ],
		[ true,  'SELECT ps, i, i AS i_copy FROM test_table', 'selecting repeated columns with unambiguous aliasing is allowed' ],
		[ true,  'SELECT ps, i AS i_copy, i FROM test_table', 'selecting repeated columns with unambiguous aliasing is allowed' ],
		[ true,  'SELECT b, b AS b_copy, ps FROM test_table', 'selecting repeated columns with unambiguous aliasing is allowed' ],
		[ true,  'SELECT b AS b_copy, b, ps FROM test_table', 'selecting repeated columns with unambiguous aliasing is allowed' ],
		[ false, 'SELECT ps, i, i FROM test_table', 'selecting repeated columns without unambiguous aliasing is not allowed' ],
		[ false, 'SELECT ps, b, i, b FROM test_table', 'selecting repeated columns without unambiguous aliasing is not allowed' ],
		[ false, 'SELECT ps, ps FROM test_table', 'selecting repeated columns without unambiguous aliasing is not allowed' ],
		[ false, 'SELECT ps AS primary, ps AS primary FROM test_table', 'selecting repeated columns without unambiguous aliasing is not allowed' ],
		[ false, 'SELECT i, b, ps AS primary, ps, ps FROM test_table', 'selecting repeated columns without unambiguous aliasing is not allowed' ],
		
		// CROSS-ALIASING
		[ true,  'SELECT ps, i AS b, b AS i FROM test_table', 'cross-aliasing columns with unambiguous results is allowed' ],
		[ true,  'SELECT ps, i AS b, i AS i_copy, b AS i, b AS b_copy, ps AS ps_copy FROM test_table', 'cross-aliasing columns with unambiguous results is allowed' ],
		[ true,  'SELECT i AS b, b AS ps, ps AS i FROM test_table', 'cross-aliasing columns with unambiguous results is allowed' ],
		
		// PRIMARY COLUMN
		[ true,  'SELECT ps AS primary_1, ps AS primary_2 FROM test_table', 'selecting repeated primary column with unambiguous aliasing is allowed' ],
		[ true,  'SELECT ps AS primary_1, ps FROM test_table', 'selecting repeated primary column with unambiguous partial aliasing is allowed' ],
		[ true,  'SELECT ps, ps AS primary_2 FROM test_table', 'selecting repeated primary column with unambiguous partial aliasing is allowed' ],
		[ false, 'SELECT i, b FROM test_table', 'the primary column must be selected' ],
		
		// STRING CONDITIONS
		[ true,  'SELECT * FROM test_table WHERE ps = "y"', null ],
		[ true,  'SELECT * FROM test_table WHERE ps <> "y"', null ],
		[ true,  'SELECT * FROM test_table WHERE ps < "y"', null ],
		[ true,  'SELECT * FROM test_table WHERE ps > "y"', null ],
		[ true,  'SELECT * FROM test_table WHERE ps <= "y"', null ],
		[ true,  'SELECT * FROM test_table WHERE ps >= "y"', null ],
		[ true,  'SELECT * FROM test_table WHERE ps = ""', null ],
		[ true,  'SELECT * FROM test_table WHERE ps <> ""', null ],
		[ true,  'SELECT * FROM test_table WHERE ps < ""', null ],
		[ true,  'SELECT * FROM test_table WHERE ps > ""', null ],
		[ true,  'SELECT * FROM test_table WHERE ps <= ""', null ],
		[ true,  'SELECT * FROM test_table WHERE ps >= ""', null ],
		
		// INTEGER CONDITIONS
		[ true,  'SELECT * FROM test_table WHERE i = 3', null ],
		[ true,  'SELECT * FROM test_table WHERE i <> 3', null ],
		[ true,  'SELECT * FROM test_table WHERE i < 3', null ],
		[ true,  'SELECT * FROM test_table WHERE i > 3', null ],
		[ true,  'SELECT * FROM test_table WHERE i <= 3', null ],
		[ true,  'SELECT * FROM test_table WHERE i >= 3', null ],
		
		// BOOLEAN CONDITIONS
		[ true,  'SELECT * FROM test_table WHERE b = true', null ],
		[ true,  'SELECT * FROM test_table WHERE b <> true', null ],
		[ true,  'SELECT * FROM test_table WHERE b = false', null ],
		[ true,  'SELECT * FROM test_table WHERE b <> false', null ],
		
		// BOOLEAN EDGE CASES
		[ false, 'SELECT * FROM test_table WHERE b < true', 'operator < is invalid for booleans' ],
		[ false, 'SELECT * FROM test_table WHERE b > false', 'operator > is invalid for booleans' ],
		[ false, 'SELECT * FROM test_table WHERE b <= true', 'operator <= is invalid for booleans' ],
		[ false, 'SELECT * FROM test_table WHERE b >= false', 'operator >= is invalid for booleans' ],
		
		// NULL CONDITIONS
		[ true,  'SELECT * FROM test_table WHERE ps = null', null ],
		[ true,  'SELECT * FROM test_table WHERE ps <> null', null ],
		[ true,  'SELECT * FROM test_table WHERE i = null', null ],
		[ true,  'SELECT * FROM test_table WHERE i <> null', null ],
		[ true,  'SELECT * FROM test_table WHERE b = null', null ],
		[ true,  'SELECT * FROM test_table WHERE b <> null', null ],
		
		// NULL INEQUALITIES
		[ true,  'SELECT * FROM test_table WHERE ps < null', 'operator < evaluates as false when operand is null' ],
		[ true,  'SELECT * FROM test_table WHERE ps > null', 'operator > evaluates as false when operand is null' ],
		[ true,  'SELECT * FROM test_table WHERE ps <= null', 'operator <= evaluates as false when operand is null' ],
		[ true,  'SELECT * FROM test_table WHERE ps >= null', 'operator >= evaluates as false when operand is null' ],
		[ true,  'SELECT * FROM test_table WHERE i < null', 'operator < evaluates as false when operand is null' ],
		[ true,  'SELECT * FROM test_table WHERE i > null', 'operator > evaluates as false when operand is null' ],
		[ true,  'SELECT * FROM test_table WHERE i <= null', 'operator <= evaluates as false when operand is null' ],
		[ true,  'SELECT * FROM test_table WHERE i >= null', 'operator >= evaluates as false when operand is null' ],
		[ false, 'SELECT * FROM test_table WHERE b < null', 'operator < is invalid for booleans even when operand is null' ],
		[ false, 'SELECT * FROM test_table WHERE b > null', 'operator > is invalid for booleans even when operand is null' ],
		[ false, 'SELECT * FROM test_table WHERE b <= null', 'operator <= is invalid for booleans even when operand is null' ],
		[ false, 'SELECT * FROM test_table WHERE b >= null', 'operator >= is invalid for booleans even when operand is null' ],

		// TYPE EDGE CASES
		[ false, 'SELECT * FROM test_table WHERE ps = 3', 'column and value types must match' ],
		[ false, 'SELECT * FROM test_table WHERE ps = true', 'column and value types must match' ],
		[ false, 'SELECT * FROM test_table WHERE i = "y"', 'column and value types must match' ],
		[ false, 'SELECT * FROM test_table WHERE i = true', 'column and value types must match' ],
		[ false, 'SELECT * FROM test_table WHERE b = "y"', 'column and value types must match' ],
		[ false, 'SELECT * FROM test_table WHERE b = 3', 'column and value types must match' ],
		
		// SYNTAX EDGE CASES
		[ false, 'SELECT * FROM test_table  i = 3', 'the WHERE keyword is required' ],
		[ false, 'SELECT * FROM test_table WHERE  = 3', 'the column is required' ],
		[ false, 'SELECT * FROM test_table WHERE i = ', 'the value is required' ],
		[ false, 'SELECT * FROM test_table WHERE x = 3', 'the column name must be valid' ],
		[ false, 'SELECT * FROM test_table WHERE ps is "y"', 'the operator must be valid' ],
		[ false, 'SELECT * FROM test_table WHERE ps = asdf', 'the value data type must be valid' ],
		[ false, 'SELECT * FROM test_tableWHEREps = "y"', 'whitespace between keywords and names is required' ],
		[ true,  'SELECT * FROM test_table  WHERE  ps = "y"', 'excess internal whitespace is allowed' ],
		[ true,  'SELECT * FROM test_table WHERE ps="y"', 'whitespace is not required around operators' ],
		
		// CONDITIONS WITH ALIASING
		[ true,  'SELECT b, i, ps FROM test_table WHERE ps <> "z"', 'conditions do not depend on column ordering' ],
		[ true,  'SELECT ps AS s FROM test_table WHERE ps <> "z"', 'conditions use unaliased column names' ],
	]
	
	static data() {[
		query_data,
		serialized_computed_schema,
		serialized_computed_rows
	].transpose()*.flatten()*.toArray()}

	static Server SERVER = new Server()
	
	static int passed_queries = 0
	static int total_queries = 0
	
	@DisplayName('Queries')
	@ParameterizedTest(name = '[{index}] {1}')
	@MethodSource('data')
	void testQuery(
		boolean success_flag, String script_text, String test_reason,
		Map computed_schema,
		Map computed_rows
	) {
		total_queries++;
		
		System.out.println(script_text)
		
		def queries = script_text.split(';')
		def query_count = queries.size()
		
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
		
		assertTrue(
			last?.get('success') == success_flag &&
			responses.take(-1).count({!it.get('success')}) == 0,
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
		
		passed_queries++;
	}
	
	@AfterAll
	static void report() {
		final double rate = passed_queries / (double) total_queries
		System.out.println(
			'[M3 PASSED ' + Math.round(rate * 100) + '% OF UNIT TESTS]',
		)
	}
	
	static serialized_computed_schema = [
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':'test_table'],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		null,
		null,
		['column_types':['string'], 'column_names':['ps'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		null,
		null,
		null,
		null,
		null,
		['column_types':['string'], 'column_names':['primary'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['primary', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['primary', 'number', 'flag'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['primary', 'number', 'flag'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'number', 'flag'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'boolean', 'integer'], 'column_names':['ps', 'b', 'i'], 'primary_index':0, 'table_name':null],
		['column_types':['integer', 'boolean', 'string'], 'column_names':['i', 'b', 'ps'], 'primary_index':2, 'table_name':null],
		['column_types':['integer', 'boolean', 'string'], 'column_names':['number', 'flag', 'primary'], 'primary_index':2, 'table_name':null],
		['column_types':['integer', 'boolean', 'string'], 'column_names':['number', 'flag', 'ps'], 'primary_index':2, 'table_name':null],
		['column_types':['string', 'integer', 'integer'], 'column_names':['ps', 'number_1', 'number_2'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'integer'], 'column_names':['ps', 'i', 'i_copy'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'integer'], 'column_names':['ps', 'i_copy', 'i'], 'primary_index':0, 'table_name':null],
		['column_types':['boolean', 'boolean', 'string'], 'column_names':['b', 'b_copy', 'ps'], 'primary_index':2, 'table_name':null],
		['column_types':['boolean', 'boolean', 'string'], 'column_names':['b_copy', 'b', 'ps'], 'primary_index':2, 'table_name':null],
		null,
		null,
		null,
		null,
		null,
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'b', 'i'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'integer', 'boolean', 'boolean', 'string'], 'column_names':['ps', 'b', 'i_copy', 'i', 'b_copy', 'ps_copy'], 'primary_index':0, 'table_name':null],
		['column_types':['integer', 'boolean', 'string'], 'column_names':['b', 'ps', 'i'], 'primary_index':2, 'table_name':null],
		['column_types':['string', 'string'], 'column_names':['primary_1', 'primary_2'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'string'], 'column_names':['primary_1', 'ps'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'string'], 'column_names':['ps', 'primary_2'], 'primary_index':0, 'table_name':null],
		null,
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
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer', 'boolean'], 'column_names':['ps', 'i', 'b'], 'primary_index':0, 'table_name':null],
		['column_types':['boolean', 'integer', 'string'], 'column_names':['b', 'i', 'ps'], 'primary_index':2, 'table_name':null],
		['column_types':['string'], 'column_names':['s'], 'primary_index':0, 'table_name':null],
	]

	static serialized_computed_rows = [
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'xy':['xy', 5, false], 'x':['x', null, true], 'y':['y', 1, null], 'z':['z', null, null], 'yx':['yx', 3, false]],
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'xy':['xy', 5, false], 'x':['x', null, true], 'y':['y', 1, null], 'z':['z', null, null], 'yx':['yx', 3, false]],
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'xy':['xy', 5, false], 'x':['x', null, true], 'y':['y', 1, null], 'z':['z', null, null], 'yx':['yx', 3, false]],
		null,
		null,
		['xx':['xx'], 'yy':['yy'], 'xy':['xy'], 'x':['x'], 'y':['y'], 'z':['z'], 'yx':['yx']],
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'xy':['xy', 5, false], 'x':['x', null, true], 'y':['y', 1, null], 'z':['z', null, null], 'yx':['yx', 3, false]],
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'xy':['xy', 5, false], 'x':['x', null, true], 'y':['y', 1, null], 'z':['z', null, null], 'yx':['yx', 3, false]],
		null,
		null,
		null,
		null,
		null,
		['xx':['xx'], 'yy':['yy'], 'xy':['xy'], 'x':['x'], 'y':['y'], 'z':['z'], 'yx':['yx']],
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'xy':['xy', 5, false], 'x':['x', null, true], 'y':['y', 1, null], 'z':['z', null, null], 'yx':['yx', 3, false]],
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'xy':['xy', 5, false], 'x':['x', null, true], 'y':['y', 1, null], 'z':['z', null, null], 'yx':['yx', 3, false]],
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'xy':['xy', 5, false], 'x':['x', null, true], 'y':['y', 1, null], 'z':['z', null, null], 'yx':['yx', 3, false]],
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'xy':['xy', 5, false], 'x':['x', null, true], 'y':['y', 1, null], 'z':['z', null, null], 'yx':['yx', 3, false]],
		['xx':['xx', true, 4], 'yy':['yy', true, 2], 'xy':['xy', false, 5], 'x':['x', true, null], 'y':['y', null, 1], 'z':['z', null, null], 'yx':['yx', false, 3]],
		['xx':[4, true, 'xx'], 'yy':[2, true, 'yy'], 'xy':[5, false, 'xy'], 'x':[null, true, 'x'], 'y':[1, null, 'y'], 'z':[null, null, 'z'], 'yx':[3, false, 'yx']],
		['xx':[4, true, 'xx'], 'yy':[2, true, 'yy'], 'xy':[5, false, 'xy'], 'x':[null, true, 'x'], 'y':[1, null, 'y'], 'z':[null, null, 'z'], 'yx':[3, false, 'yx']],
		['xx':[4, true, 'xx'], 'yy':[2, true, 'yy'], 'xy':[5, false, 'xy'], 'x':[null, true, 'x'], 'y':[1, null, 'y'], 'z':[null, null, 'z'], 'yx':[3, false, 'yx']],
		['xx':['xx', 4, 4], 'yy':['yy', 2, 2], 'xy':['xy', 5, 5], 'x':['x', null, null], 'y':['y', 1, 1], 'z':['z', null, null], 'yx':['yx', 3, 3]],
		['xx':['xx', 4, 4], 'yy':['yy', 2, 2], 'xy':['xy', 5, 5], 'x':['x', null, null], 'y':['y', 1, 1], 'z':['z', null, null], 'yx':['yx', 3, 3]],
		['xx':['xx', 4, 4], 'yy':['yy', 2, 2], 'xy':['xy', 5, 5], 'x':['x', null, null], 'y':['y', 1, 1], 'z':['z', null, null], 'yx':['yx', 3, 3]],
		['xx':[true, true, 'xx'], 'yy':[true, true, 'yy'], 'xy':[false, false, 'xy'], 'x':[true, true, 'x'], 'y':[null, null, 'y'], 'z':[null, null, 'z'], 'yx':[false, false, 'yx']],
		['xx':[true, true, 'xx'], 'yy':[true, true, 'yy'], 'xy':[false, false, 'xy'], 'x':[true, true, 'x'], 'y':[null, null, 'y'], 'z':[null, null, 'z'], 'yx':[false, false, 'yx']],
		null,
		null,
		null,
		null,
		null,
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'xy':['xy', 5, false], 'x':['x', null, true], 'y':['y', 1, null], 'z':['z', null, null], 'yx':['yx', 3, false]],
		['xx':['xx', 4, 4, true, true, 'xx'], 'yy':['yy', 2, 2, true, true, 'yy'], 'xy':['xy', 5, 5, false, false, 'xy'], 'x':['x', null, null, true, true, 'x'], 'y':['y', 1, 1, null, null, 'y'], 'z':['z', null, null, null, null, 'z'], 'yx':['yx', 3, 3, false, false, 'yx']],
		['xx':[4, true, 'xx'], 'yy':[2, true, 'yy'], 'xy':[5, false, 'xy'], 'x':[null, true, 'x'], 'y':[1, null, 'y'], 'z':[null, null, 'z'], 'yx':[3, false, 'yx']],
		['xx':['xx', 'xx'], 'yy':['yy', 'yy'], 'xy':['xy', 'xy'], 'x':['x', 'x'], 'y':['y', 'y'], 'z':['z', 'z'], 'yx':['yx', 'yx']],
		['xx':['xx', 'xx'], 'yy':['yy', 'yy'], 'xy':['xy', 'xy'], 'x':['x', 'x'], 'y':['y', 'y'], 'z':['z', 'z'], 'yx':['yx', 'yx']],
		['xx':['xx', 'xx'], 'yy':['yy', 'yy'], 'xy':['xy', 'xy'], 'x':['x', 'x'], 'y':['y', 'y'], 'z':['z', 'z'], 'yx':['yx', 'yx']],
		null,
		['y':['y', 1, null]],
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'xy':['xy', 5, false], 'x':['x', null, true], 'z':['z', null, null], 'yx':['yx', 3, false]],
		['xx':['xx', 4, true], 'xy':['xy', 5, false], 'x':['x', null, true]],
		['yy':['yy', 2, true], 'z':['z', null, null], 'yx':['yx', 3, false]],
		['xx':['xx', 4, true], 'xy':['xy', 5, false], 'x':['x', null, true], 'y':['y', 1, null]],
		['yy':['yy', 2, true], 'y':['y', 1, null], 'z':['z', null, null], 'yx':['yx', 3, false]],
		[:],
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'xy':['xy', 5, false], 'x':['x', null, true], 'y':['y', 1, null], 'z':['z', null, null], 'yx':['yx', 3, false]],
		[:],
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'xy':['xy', 5, false], 'x':['x', null, true], 'y':['y', 1, null], 'z':['z', null, null], 'yx':['yx', 3, false]],
		[:],
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'xy':['xy', 5, false], 'x':['x', null, true], 'y':['y', 1, null], 'z':['z', null, null], 'yx':['yx', 3, false]],
		['yx':['yx', 3, false]],
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'xy':['xy', 5, false], 'x':['x', null, true], 'y':['y', 1, null], 'z':['z', null, null]],
		['yy':['yy', 2, true], 'y':['y', 1, null]],
		['xx':['xx', 4, true], 'xy':['xy', 5, false]],
		['yy':['yy', 2, true], 'y':['y', 1, null], 'yx':['yx', 3, false]],
		['xx':['xx', 4, true], 'xy':['xy', 5, false], 'yx':['yx', 3, false]],
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'x':['x', null, true]],
		['xy':['xy', 5, false], 'y':['y', 1, null], 'z':['z', null, null], 'yx':['yx', 3, false]],
		['xy':['xy', 5, false], 'yx':['yx', 3, false]],
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'x':['x', null, true], 'y':['y', 1, null], 'z':['z', null, null]],
		null,
		null,
		null,
		null,
		[:],
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'xy':['xy', 5, false], 'x':['x', null, true], 'y':['y', 1, null], 'z':['z', null, null], 'yx':['yx', 3, false]],
		['x':['x', null, true], 'z':['z', null, null]],
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'xy':['xy', 5, false], 'y':['y', 1, null], 'yx':['yx', 3, false]],
		['y':['y', 1, null], 'z':['z', null, null]],
		['xx':['xx', 4, true], 'yy':['yy', 2, true], 'xy':['xy', 5, false], 'x':['x', null, true], 'yx':['yx', 3, false]],
		[:],
		[:],
		[:],
		[:],
		[:],
		[:],
		[:],
		[:],
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		['y':['y', 1, null]],
		['y':['y', 1, null]],
		['xx':[true, 4, 'xx'], 'yy':[true, 2, 'yy'], 'xy':[false, 5, 'xy'], 'x':[true, null, 'x'], 'y':[null, 1, 'y'], 'yx':[false, 3, 'yx']],
		['xx':['xx'], 'yy':['yy'], 'xy':['xy'], 'x':['x'], 'y':['y'], 'yx':['yx']],
	]
}
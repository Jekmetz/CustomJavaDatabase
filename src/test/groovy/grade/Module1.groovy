package grade

import static org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*

import adt.Response
import adt.Table
import core.Server

class Module1 {
	static data() {
		return [
			// CREATE
			[ true,  'CREATE TABLE table_name01 (PRIMARY INTEGER id, STRING name, BOOLEAN flag)', null ],
			[ true,  'create table TABLE_NAME02 (primary integer ID, string NAME, BOOLEAN flag)', 'lowercase keywords and uppercase table names are allowed' ],
			[ true,  ' CREATE TABLE table_name03 (PRIMARY INTEGER id, STRING name, BOOLEAN flag) ', 'untrimmed whitespace is allowed' ],
			[ true,  'CREATE  TABLE  table_name04  (PRIMARY INTEGER id, STRING name, BOOLEAN flag)', 'excess internal whitespace is allowed' ],
			[ true,  'CREATE TABLE table_name05 ( PRIMARY INTEGER id , STRING name , BOOLEAN flag )', 'excess internal whitespace is allowed' ],
			[ true,  'CREATE TABLE table_name06 (PRIMARY INTEGER id,STRING name,BOOLEAN flag)', 'whitespace around punctuation is not required' ],
			[ false, 'CREATETABLE table_name07 (PRIMARY INTEGER id, STRING name, BOOLEAN flag)', 'whitespace between keywords is required ' ],
			[ false, 'CREATE TABLEtable_name08 (PRIMARY INTEGER id, STRING name, BOOLEAN flag)', 'whitespace between keywords and names is required' ],
			[ true,  'CREATE TABLE t (PRIMARY INTEGER i, STRING n, BOOLEAN f)', 'names can be a single letter' ],
			[ false, 'CREATE TABLE 1table_name10 (PRIMARY INTEGER 2id, STRING 3name, BOOLEAN 4flag)', 'a name cannot start with a number' ],
			[ false, 'CREATE TABLE _table_name11 (PRIMARY INTEGER _id, STRING _name, BOOLEAN _flag)', 'a name cannot start with an underscore' ],
			[ false, 'CREATE TABLE (PRIMARY INTEGER id, STRING name, BOOLEAN flag)', 'the table name cannot be omitted' ],
			[ false, 'CREATE table_name13 (PRIMARY INTEGER id, STRING name, BOOLEAN flag)', 'the TABLE keyword is required' ],
			[ false, 'CREATE TABLE table_name14 (INTEGER PRIMARY id, STRING name, BOOLEAN flag)', 'the PRIMARY and type keywords cannot be inverted' ],
			[ false, 'CREATE TABLE table_name15 PRIMARY INTEGER id, STRING name, BOOLEAN flag', 'the parentheses are required' ],
			
			// CREATE EDGE CASES
			[ false, 'CREATE TABLE table_name16 ()', 'there must be at least one column' ],
			[ true,  'CREATE TABLE table_name17 (PRIMARY INTEGER id)', 'a single column is allowed' ],
			[ false, 'CREATE TABLE table_name18 (INTEGER id, STRING name, BOOLEAN flag)', 'there must be a primary column' ],
			[ false, 'CREATE TABLE table_name19 (PRIMARY INTEGER id, PRIMARY STRING name, PRIMARY BOOLEAN flag)', 'there can be only one primary column' ],
			[ true,  'CREATE TABLE table_name20 (INTEGER id, PRIMARY STRING name, BOOLEAN flag)', 'the primary column need not be the first' ],
			[ false, 'CREATE TABLE table_name01 (PRIMARY STRING ps)', 'the table name must not already be in use' ],
			
			// DROP
			[ true,  'DROP TABLE table_name01', null ],
			[ true,  'drop table TABLE_NAME02', 'lowercase keywords and uppercase table names are allowed' ],
			[ true,  ' DROP TABLE table_name03 ', 'untrimmed whitespace is allowed' ],
			[ true,  'DROP  TABLE  table_name04', 'excess internal whitespace is allowed' ],
			[ false, 'DROPTABLE table_name05', 'whitespace between keywords is required ' ],
			[ false, 'DROP TABLEtable_name06', 'whitespace between keywords and names is required' ],
			[ true,  'DROP TABLE t', 'names can be a single letter' ],
			[ false, 'DROP TABLE', 'the table name cannot be omitted' ],
			[ false, 'DROP table_name17', 'the TABLE keyword is required' ],
			
			// DROP EDGE CASES
			[ false, 'DROP TABLE table_name01', 'the table must already exist' ],
			[ true,  'CREATE TABLE table_name01 (PRIMARY STRING ps)', 'previously dropped table name can be reused' ],
			
			// SHOW TABLES
			[ true,  'SHOW TABLES', null ],
			[ true,  'show tables', 'lowercase keywords are allowed' ],
			[ true,  ' SHOW TABLES ', 'untrimmed whitespace is allowed' ],
			[ true,  'SHOW  TABLES', 'excess internal whitespace is allowed' ],
			[ false, 'SHOWTABLES', 'whitespace between keywords is required ' ],
			[ false, 'TABLE', 'the SHOW keyword is required' ],
			
			// ROBUSTNESS
			[ false, 'A MALFORMED QUERY', 'an unrecognized query should be rejected' ],
			[ true,  'ECHO "1"; ECHO "2"; ECHO "3"', 'multiple semicolon-delimited queries are allowed' ],
		]*.toArray()
	}

	static Server SERVER
	
	static List	CANON_COMPUTED_SCHEMA,
				CANON_COMPUTED_TABLE
	
	@BeforeAll
	static void initialize() {
		SERVER = new Server()
		
		CANON_COMPUTED_SCHEMA = dumpComputedSchema()
		CANON_COMPUTED_TABLE = dumpComputedTable()
	}
	
	private static int passed_queries = 0
	private static int total_queries = 0
	
	@DisplayName('Queries')
	@ParameterizedTest(name = '[{index}] {1}')
	@MethodSource('data')
	void testQuery(boolean success, String query, String explanation) {
		total_queries++
		
		System.out.println(query)
		
		def subqueries = query.split(';').size()
		def responses = SERVER.interpret(query)
		
		assertEquals(
			subqueries,
			responses?.count({it != null}),
			String.format(
				'%s <%s> returned wrong number of non-null responses,',
				subqueries == 1 ? 'Query' : 'Script',
				query
			)
		)
		
		def last = responses[-1]
		
		Map computed_table = CANON_COMPUTED_TABLE.remove(0),
			computed_schema = CANON_COMPUTED_SCHEMA.remove(0)
			
		assertTrue(
			last?.get('success') == success &&
			responses.take(-1).count({!it.get('success')}) == 0,
			String.format(
				'%s %s <%s> was expected to %s%s%s.',
				success ? 'Valid' : 'Invalid',
				subqueries == 1 ? 'query' : 'script',
				query,
				success ? 'succeed' : 'fail',
				subqueries == 1 || success ? '' : ' only on last query',
				explanation ? (' because ' + explanation) : ''
			)
		)
		
		def last_schema = ((Table) last?.get('table'))?.getSchema()
		for (String k: computed_schema?.keySet()) {
			if (!last_schema?.keySet()?.contains(k))
				last_schema?.put(k, null)
		}
			
		if (computed_schema != null) assertEquals(
			computed_schema,
			last_schema?.subMap(computed_schema?.keySet()),
			String.format(
				'%s <%s> returned <computed> table with incorrect standard schema properties,',
				subqueries == 1 ? 'Query' : 'Last query of script',
				query
			)
		)
		
		assertEquals(
			computed_table,
			last?.get('table'),
			String.format(
				'%s <%s> returned <computed> table with incorrect rows,',
				subqueries == 1 ? 'Query' : 'Last query of script',
				query
			)
		)
		
		passed_queries++
	}
	
	@AfterAll
	static void report() {
		final double rate = passed_queries / (double) total_queries
		System.out.println(
			'[M1 PASSED ' + Math.round(rate * 100) + '% OF UNIT TESTS]',
		)
	}
	
	static dumpComputedSchema() {[
		['column_types':['integer', 'string', 'boolean'], 'column_names':['id', 'name', 'flag'], 'primary_index':0, 'table_name':'table_name01'],
		['column_types':['integer', 'string', 'boolean'], 'column_names':['ID', 'NAME', 'flag'], 'primary_index':0, 'table_name':'TABLE_NAME02'],
		['column_types':['integer', 'string', 'boolean'], 'column_names':['id', 'name', 'flag'], 'primary_index':0, 'table_name':'table_name03'],
		['column_types':['integer', 'string', 'boolean'], 'column_names':['id', 'name', 'flag'], 'primary_index':0, 'table_name':'table_name04'],
		['column_types':['integer', 'string', 'boolean'], 'column_names':['id', 'name', 'flag'], 'primary_index':0, 'table_name':'table_name05'],
		['column_types':['integer', 'string', 'boolean'], 'column_names':['id', 'name', 'flag'], 'primary_index':0, 'table_name':'table_name06'],
		null,
		null,
		['column_types':['integer', 'string', 'boolean'], 'column_names':['i', 'n', 'f'], 'primary_index':0, 'table_name':'t'],
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		['column_types':['integer'], 'column_names':['id'], 'primary_index':0, 'table_name':'table_name17'],
		null,
		null,
		['column_types':['integer', 'string', 'boolean'], 'column_names':['id', 'name', 'flag'], 'primary_index':1, 'table_name':'table_name20'],
		null,
		['column_types':['integer', 'string', 'boolean'], 'column_names':['id', 'name', 'flag'], 'primary_index':0, 'table_name':'table_name01'],
		['column_types':['integer', 'string', 'boolean'], 'column_names':['ID', 'NAME', 'flag'], 'primary_index':0, 'table_name':'TABLE_NAME02'],
		['column_types':['integer', 'string', 'boolean'], 'column_names':['id', 'name', 'flag'], 'primary_index':0, 'table_name':'table_name03'],
		['column_types':['integer', 'string', 'boolean'], 'column_names':['id', 'name', 'flag'], 'primary_index':0, 'table_name':'table_name04'],
		null,
		null,
		['column_types':['integer', 'string', 'boolean'], 'column_names':['i', 'n', 'f'], 'primary_index':0, 'table_name':'t'],
		null,
		null,
		null,
		['column_types':['string'], 'column_names':['ps'], 'primary_index':0, 'table_name':'table_name01'],
		['column_types':['string', 'integer'], 'column_names':['table_name', 'row_count'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer'], 'column_names':['table_name', 'row_count'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer'], 'column_names':['table_name', 'row_count'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer'], 'column_names':['table_name', 'row_count'], 'primary_index':0, 'table_name':null],
		null,
		null,
		null,
		null,
	]}

	static dumpComputedTable() {[
		[:],
		[:],
		[:],
		[:],
		[:],
		[:],
		null,
		null,
		[:],
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		[:],
		null,
		null,
		[:],
		null,
		[:],
		[:],
		[:],
		[:],
		null,
		null,
		[:],
		null,
		null,
		null,
		[:],
		['table_name20':['table_name20', 0], 'table_name01':['table_name01', 0], 'table_name05':['table_name05', 0], 'table_name06':['table_name06', 0], 'table_name17':['table_name17', 0]],
		['table_name20':['table_name20', 0], 'table_name01':['table_name01', 0], 'table_name05':['table_name05', 0], 'table_name06':['table_name06', 0], 'table_name17':['table_name17', 0]],
		['table_name20':['table_name20', 0], 'table_name01':['table_name01', 0], 'table_name05':['table_name05', 0], 'table_name06':['table_name06', 0], 'table_name17':['table_name17', 0]],
		['table_name20':['table_name20', 0], 'table_name01':['table_name01', 0], 'table_name05':['table_name05', 0], 'table_name06':['table_name06', 0], 'table_name17':['table_name17', 0]],
		null,
		null,
		null,
		null,
	]}
}
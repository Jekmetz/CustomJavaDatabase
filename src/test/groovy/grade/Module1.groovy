package grade

import static org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*

import adt.Response
import adt.Table
import core.Server

class Module1 {
	static query_data = [
		// CREATE
		[ true,  'CREATE TABLE table_01 (PRIMARY INTEGER id, STRING name, BOOLEAN flag)', null ],
		[ true,  'create table table_02 (primary integer ID, string NAME, BOOLEAN flag)', 'lowercase keywords and uppercase table names are allowed' ],
		[ true,  ' CREATE TABLE table_03 (PRIMARY INTEGER id, STRING name, BOOLEAN flag) ', 'untrimmed whitespace is allowed' ],
		[ true,  'CREATE  TABLE  table_04  (PRIMARY INTEGER id, STRING name, BOOLEAN flag)', 'excess internal whitespace is allowed' ],
		[ true,  'CREATE TABLE table_05 ( PRIMARY INTEGER id , STRING name , BOOLEAN flag )', 'excess internal whitespace is allowed' ],
		[ true,  'CREATE TABLE table_06 (PRIMARY INTEGER id,STRING name,BOOLEAN flag)', 'whitespace around punctuation is not required' ],
		[ false, 'CREATETABLE table_07 (PRIMARY INTEGER id, STRING name, BOOLEAN flag)', 'whitespace between keywords is required ' ],
		[ false, 'CREATE TABLEtable_08 (PRIMARY INTEGER id, STRING name, BOOLEAN flag)', 'whitespace between keywords and names is required' ],
		[ true,  'CREATE TABLE t (PRIMARY INTEGER i, STRING n, BOOLEAN f)', 'names can be a single letter' ],
		[ false, 'CREATE TABLE 1table_10 (PRIMARY INTEGER 2id, STRING 3name, BOOLEAN 4flag)', 'a name cannot start with a number' ],
		[ false, 'CREATE TABLE _table_11 (PRIMARY INTEGER _id, STRING _name, BOOLEAN _flag)', 'a name cannot start with an underscore' ],
		[ false, 'CREATE TABLE (PRIMARY INTEGER id, STRING name, BOOLEAN flag)', 'the table name cannot be omitted' ],
		[ false, 'CREATE table_13 (PRIMARY INTEGER id, STRING name, BOOLEAN flag)', 'the TABLE keyword is required' ],
		[ false, 'CREATE TABLE table_14 (INTEGER PRIMARY id, STRING name, BOOLEAN flag)', 'the PRIMARY and type keywords cannot be inverted' ],
		[ false, 'CREATE TABLE table_15 PRIMARY INTEGER id, STRING name, BOOLEAN flag', 'the parentheses are required' ],
		
		// CREATE EDGE CASES
		[ false, 'CREATE TABLE table_16 ()', 'there must be at least one column' ],
		[ true,  'CREATE TABLE table_17 (PRIMARY INTEGER id)', 'a single column is allowed' ],
		[ false, 'CREATE TABLE table_18 (INTEGER id, STRING name, BOOLEAN flag)', 'there must be a primary column' ],
		[ false, 'CREATE TABLE table_19 (PRIMARY INTEGER id, PRIMARY STRING name, PRIMARY BOOLEAN flag)', 'there can be only one primary column' ],
		[ true,  'CREATE TABLE table_20 (INTEGER id, PRIMARY STRING name, BOOLEAN flag)', 'the primary column need not be the first' ],
		[ false, 'CREATE TABLE table_01 (PRIMARY STRING ps)', 'the table name must not already be in use' ],
		
		// DROP
		[ true,  'DROP TABLE table_01', null ],
		[ true,  'drop table table_02', 'lowercase keywords and uppercase table names are allowed' ],
		[ true,  ' DROP TABLE table_03 ', 'untrimmed whitespace is allowed' ],
		[ true,  'DROP  TABLE  table_04', 'excess internal whitespace is allowed' ],
		[ false, 'DROPTABLE table_05', 'whitespace between keywords is required ' ],
		[ false, 'DROP TABLEtable_06', 'whitespace between keywords and names is required' ],
		[ true,  'DROP TABLE t', 'names can be a single letter' ],
		[ false, 'DROP TABLE', 'the table name cannot be omitted' ],
		[ false, 'DROP table_17', 'the TABLE keyword is required' ],
		
		// DROP EDGE CASES
		[ false, 'DROP TABLE table_01', 'the table must already exist' ],
		[ true,  'CREATE TABLE table_01 (PRIMARY STRING ps)', 'previously dropped table name can be reused' ],
		
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
		total_queries++
		
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
		
		passed_queries++
	}
	
	@AfterAll
	static void report() {
		final double rate = passed_queries / (double) total_queries
		System.out.println(
			'[M1 PASSED ' + Math.round(rate * 100) + '% OF UNIT TESTS]',
		)
	}
	
	static serialized_computed_schema = [
		['column_types':['integer', 'string', 'boolean'], 'column_names':['id', 'name', 'flag'], 'primary_index':0, 'table_name':'table_01'],
		['column_types':['integer', 'string', 'boolean'], 'column_names':['ID', 'NAME', 'flag'], 'primary_index':0, 'table_name':'table_02'],
		['column_types':['integer', 'string', 'boolean'], 'column_names':['id', 'name', 'flag'], 'primary_index':0, 'table_name':'table_03'],
		['column_types':['integer', 'string', 'boolean'], 'column_names':['id', 'name', 'flag'], 'primary_index':0, 'table_name':'table_04'],
		['column_types':['integer', 'string', 'boolean'], 'column_names':['id', 'name', 'flag'], 'primary_index':0, 'table_name':'table_05'],
		['column_types':['integer', 'string', 'boolean'], 'column_names':['id', 'name', 'flag'], 'primary_index':0, 'table_name':'table_06'],
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
		['column_types':['integer'], 'column_names':['id'], 'primary_index':0, 'table_name':'table_17'],
		null,
		null,
		['column_types':['integer', 'string', 'boolean'], 'column_names':['id', 'name', 'flag'], 'primary_index':1, 'table_name':'table_20'],
		null,
		['column_types':['integer', 'string', 'boolean'], 'column_names':['id', 'name', 'flag'], 'primary_index':0, 'table_name':'table_01'],
		['column_types':['integer', 'string', 'boolean'], 'column_names':['ID', 'NAME', 'flag'], 'primary_index':0, 'table_name':'table_02'],
		['column_types':['integer', 'string', 'boolean'], 'column_names':['id', 'name', 'flag'], 'primary_index':0, 'table_name':'table_03'],
		['column_types':['integer', 'string', 'boolean'], 'column_names':['id', 'name', 'flag'], 'primary_index':0, 'table_name':'table_04'],
		null,
		null,
		['column_types':['integer', 'string', 'boolean'], 'column_names':['i', 'n', 'f'], 'primary_index':0, 'table_name':'t'],
		null,
		null,
		null,
		['column_types':['string'], 'column_names':['ps'], 'primary_index':0, 'table_name':'table_01'],
		['column_types':['string', 'integer'], 'column_names':['table_name', 'row_count'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer'], 'column_names':['table_name', 'row_count'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer'], 'column_names':['table_name', 'row_count'], 'primary_index':0, 'table_name':null],
		['column_types':['string', 'integer'], 'column_names':['table_name', 'row_count'], 'primary_index':0, 'table_name':null],
		null,
		null,
		null,
		null,
	]

	static serialized_computed_rows = [
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
		['table_01':['table_01', 0], 'table_05':['table_05', 0], 'table_06':['table_06', 0], 'table_17':['table_17', 0], 'table_20':['table_20', 0]],
		['table_01':['table_01', 0], 'table_05':['table_05', 0], 'table_06':['table_06', 0], 'table_17':['table_17', 0], 'table_20':['table_20', 0]],
		['table_01':['table_01', 0], 'table_05':['table_05', 0], 'table_06':['table_06', 0], 'table_17':['table_17', 0], 'table_20':['table_20', 0]],
		['table_01':['table_01', 0], 'table_05':['table_05', 0], 'table_06':['table_06', 0], 'table_17':['table_17', 0], 'table_20':['table_20', 0]],
		null,
		null,
		null,
		null,
	]
}
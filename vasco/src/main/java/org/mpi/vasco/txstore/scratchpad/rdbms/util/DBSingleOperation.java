package org.mpi.vasco.txstore.scratchpad.rdbms.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.update.Update;
import org.mpi.vasco.txstore.util.Operation;

public class DBSingleOperation
	extends DBOperation
{
	transient public String sql;
	transient Statement stat;
	transient String[][] table;
	
	public DBSingleOperation( String sql) {
		super(sql.getBytes());
		this.sql = sql;
	}
	
	public DBSingleOperation( Operation op) {
		super( op.getOperation());
		this.sql = null; 
	}

	public static DBSingleOperation createOperation(	String sql){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		try{
		dos.writeByte(OP_SINGLEOP);
		dos.writeUTF(sql);
		}catch(IOException e){
			System.err.println("create single op exception");
			System.exit(-1);
		}
		return new DBSingleOperation( baos.toByteArray(), sql);
		
	}


	protected DBSingleOperation(byte[] arr, String sql) {
		super(arr);
		this.sql = sql;
	}
	
	public String getStatement() {
		if( sql == null)
			sql = new String( super.getOperation());
		return sql;
	}
	
	public Statement getStatementObj() {
		if( stat == null)
			throw new RuntimeException( "Not parsed yet - unexpected situation");
		return stat;
	}
	
	public void parseSQL( CCJSqlParserManager parser) throws JSQLParserException {
		if( stat == null)
			stat = parser.parse( new StringReader( getStatement()));
	}
	
	/*
	 * table[][0] - nome da tabela
	 * table[][1] - nome do alias
	 * table[][2] - nome da tabela uppercase
	 */
	public String[][] targetTable() {
		if( table != null)
			return table;
		if( stat == null)
			throw new RuntimeException( "Not parsed yet - unexpected situation");
		if( stat instanceof Insert) {
			table = new String[][] { {((Insert)stat).getTable().getName(), ((Insert)stat).getTable().getName(), ((Insert)stat).getTable().getName().toUpperCase()}};
		} else if( stat instanceof Update) {
			table = new String[][] { {((Update)stat).getTable().getName(), ((Update)stat).getTable().getName(), ((Update)stat).getTable().getName().toUpperCase()}};
		} else if( stat instanceof Delete) {
			table = new String[][] { {((Delete)stat).getTable().getName(), ((Delete)stat).getTable().getName(), ((Delete)stat).getTable().getName().toUpperCase()}};
		} else if( stat instanceof Select) {
			SelectBody sb = ((Select)stat).getSelectBody();
			if( ! (sb instanceof PlainSelect))
				throw new RuntimeException( "Cannot process select : " + stat);
			PlainSelect psb = (PlainSelect)sb;
			FromItem fi = psb.getFromItem();
			if( ! (fi instanceof Table))
				throw new RuntimeException( "Cannot process select : " + stat);
			List joins = psb.getJoins();
			int nJoins = joins == null ? 0 : joins.size();
			table = new String[nJoins+1][3];
			table[0][0] = ((Table)fi).getName();
			table[0][1] = (fi.getAlias() == null || fi.getAlias().length() == 0) ? ((Table)fi).getName() : fi.getAlias();
			table[0][2] = ((Table)fi).getName().toUpperCase();
			if( joins != null) {
				Iterator it = joins.iterator();
				int i = 1;
				while( it.hasNext()) {
					Join jT = (Join)it.next();
					table[i][0] = ((Table)jT.getRightItem()).getName();
					table[i][1] = (((Table)jT.getRightItem()).getAlias() == null || ((Table)jT.getRightItem()).getAlias().length() == 0) ? 
							((Table)jT.getRightItem()).getName() : ((Table)jT.getRightItem()).getAlias();
					table[i][2] = ((Table)jT.getRightItem()).getName().toUpperCase();
					i++;
				}
			}
		} else
			throw new RuntimeException( "Cannot process operation : " + stat);
		return table;
	}
	
	public boolean isQuery() {
		if( stat == null)
			throw new RuntimeException( "Not parsed yet - unexpected situation");
		return stat instanceof Select;
	}
	public String toString() {
		return getStatement();
	}

}

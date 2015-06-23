package org.mpi.vasco.txstore.scratchpad.rdbms.resolution;
import org.mpi.vasco.util.debug.Debug;

import java.util.Iterator;
import java.util.List;

import org.mpi.vasco.txstore.scratchpad.rdbms.DBScratchpad;

import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;

public class TableDefinition
{

	String name;
	String nameAlias;
	int tableId;
	boolean[] colsStr;
	boolean[] colsOriginal;
	String[] colsPlain;
	String[] pkPlain;
	String[] colsAlias;
	String[] pkAlias;
	//newly added
	String[] uqIndicesPlain;
	String[] colsTempAlias;
	String[] pkTempAlias;
	int[] pkPosition;
	int[] uqiPosition;
	String pkListPlain;
	String colListPlain;
	String fullColListPlain;
	String pkListAlias;
	String colListAlias;
	String fullColListAlias;
	String pkListTempAlias;
	String colListTempAlias;
	String fullColListTempAlias;

	public TableDefinition(String name, String nameAlias, int tableId, boolean[] colsStr, String[] cols, 
							String[] colsAlias, String[] colsTempAlias, 
							String[] pk, String[] pkAlias, String[] pkTempAlias, String[] uqIndicesPlain) {
		this.name = name;
		this.nameAlias = nameAlias;
		this.tableId = tableId;
		this.colsStr = colsStr;
		this.colsPlain = cols;
		this.colsAlias = colsAlias;
		this.colsTempAlias = colsTempAlias;
		this.pkPlain = pk;
		this.pkAlias = pkAlias;
		this.pkTempAlias = pkTempAlias;
		this.uqIndicesPlain = uqIndicesPlain;
		init();
	}
	
	private void init() {
		colListPlain = "";
		fullColListPlain = "";
		colListAlias = "";
		fullColListAlias = "";
		colListTempAlias = "";
		fullColListTempAlias = "";
		pkPosition = new int[pkPlain.length];
		uqiPosition = new int[uqIndicesPlain.length];
		colsOriginal = new boolean[colsPlain.length];
		boolean hasAny = false;
		for( int i = 0; i < colsPlain.length; i++) {
			colsOriginal[i] = ! colsPlain[i].startsWith(DBScratchpad.SCRATCHPAD_COL_PREFIX);
			if( i != 0) {
				fullColListPlain = fullColListPlain + ",";
				fullColListAlias = fullColListAlias + ",";
				fullColListTempAlias = fullColListTempAlias + ",";
			}
			fullColListPlain = fullColListPlain + colsPlain[i];
			fullColListAlias = fullColListAlias + colsAlias[i];
			fullColListTempAlias = fullColListTempAlias + colsTempAlias[i];
			if( colsOriginal[i]) {
				if( hasAny) {
					colListPlain = colListPlain + ",";
					colListAlias = colListAlias + ",";
					colListTempAlias = colListTempAlias + ",";
				}
				colListPlain = colListPlain + colsPlain[i];
				colListAlias = colListAlias + colsAlias[i];
				colListTempAlias = colListTempAlias + colsTempAlias[i];
				hasAny = true;
			}
			for( int j = 0; j < pkPlain.length; j++)
				if( colsPlain[i].equalsIgnoreCase( pkPlain[j]))
					pkPosition[j] = i;
			
			//update unique index positions
			for( int j = 0; j < uqIndicesPlain.length; j++)
				if( colsPlain[i].equalsIgnoreCase( uqIndicesPlain[j]))
					this.uqiPosition[j] = i;
		}
		pkListPlain = "";
		pkListAlias = "";
		pkListTempAlias = "";
		for( int i = 0; i < pkPlain.length; i++) {
			if( i != 0) {
				pkListPlain = pkListPlain + ",";
				pkListAlias = pkListAlias + ",";
				pkListTempAlias = pkListTempAlias + ",";
			}
			pkListPlain = pkListPlain + pkPlain[i];
			pkListAlias = pkListAlias + pkAlias[i];
			pkListTempAlias = pkListTempAlias + pkTempAlias[i];
		}
	}
	
	public String getName() {
		return name;
	}

	public String getNameAlias() {
		return nameAlias;
	}


	/**
	 * Returns the list of columns without scratchpad metadata
	 * @return
	 */
	public String getPlainColumnList() {
		return colListPlain;
	}
	/**
	 * Returns the full list of columns including scratchpad metadata
	 * @return
	 */
	public String getPlainFullColumnList() {
		return fullColListPlain;
	}
	
	/**
	 * Returns the list of columns with aliases without scratchpad metadata
	 * @return
	 */
	public String getAliasColumnList() {
		return colListAlias;
	}
	public void addAliasColumnList( StringBuffer buffer, String tableAlias) {
		for( int i = 0; i < colsPlain.length; i++) {
			if( ! colsOriginal[i])
				continue;
			buffer.append(tableAlias);
			buffer.append(".");
			buffer.append(colsPlain[i]);
			buffer.append(",");
		}
 	}
	
	/**
	 * Returns the full list of columns with aliases including scratchpad metadata
	 * @return
	 */
	public String getAliasFullColumnList() {
		return fullColListAlias;
	}
	/**
	 * Returns the list of columns with temporary aliases without scratchpad metadata
	 * @return
	 */
	public String getTempAliasColumnList() {
		return colListTempAlias;
	}
	/**
	 * Returns the full list of columns with temporary aliases including scratchpad metadata
	 * @return
	 */
	public String getTempAliasFullColumnList() {
		return fullColListTempAlias;
	}
	
	/**
	 * 
	 */
	public String[] getPlainPKValue( List columns, ItemsList list) {
		String[] res = new String[pkPlain.length];
		if( columns == null) {
			for( int i = 0; i < pkPosition.length; i++)
				res[i] = ((ExpressionList)list).getExpressions().get(pkPosition[i]).toString();
			return res;
		} else {
			int count = 0;
			Iterator it = columns.iterator();
			while( it.hasNext()) {
				Column c = (Column)it.next();
				String cName = c.getColumnName();
				for( int i = 0; i < pkPlain.length; i++) {
					if( pkPlain[i].equalsIgnoreCase( cName))
						res[i] = ((ExpressionList)list).getExpressions().get(count).toString();
				}
				count++;
			}
			return res;
		}


	}
	
	public String[] getPlainUniqueIndexValue( List columns, ItemsList list) {
		String[] res = new String[uqIndicesPlain.length];
		if( columns == null) {
			for( int i = 0; i < uqiPosition.length; i++)
				res[i] = ((ExpressionList)list).getExpressions().get(uqiPosition[i]).toString();
			return res;
		} else {
			int count = 0;
			Iterator it = columns.iterator();
			while( it.hasNext()) {
				Column c = (Column)it.next();
				String cName = c.getColumnName();
				for( int i = 0; i < uqiPosition.length; i++) {
					if( uqIndicesPlain[i].equalsIgnoreCase( cName))
						res[i] = ((ExpressionList)list).getExpressions().get(count).toString();
				}
				count++;
			}
			return res;
		}
	}

	public String[] getPksPlain() {
		return pkPlain;
	}
	public String[] getPksAlias() {
		return pkAlias;
	}
	public String[] getPksTempAlias() {
		return pkTempAlias;
	}
	public String getPkListPlain() {
		return pkListPlain;
	}

	public String getPkListAlias() {
		return pkListAlias;
	}

	public String getPkListTempAlias() {
		return pkListTempAlias;
	}
	
	public String[] getUqIndicesPlain(){
		return uqIndicesPlain;
	}


}
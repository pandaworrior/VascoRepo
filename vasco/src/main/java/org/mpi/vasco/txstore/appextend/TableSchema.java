package org.mpi.vasco.txstore.appextend;

import java.util.ArrayList;

public class TableSchema {
	
	
	public String table_name;
	public int db_id;
	public ArrayList<column_pair> column_list;
	
	public TableSchema(String s1, int i1){
		table_name = s1;
		db_id = i1;
		column_list = new ArrayList<column_pair>();
	}
	
	public void addToColumnList(String f, String t){
		column_pair cp = new column_pair(f,t);
		column_list.add(cp);
	}
	
	public void printOut(){
		System.out.printf("---print table %s -----\n", table_name);
		for(int i = 0; i < column_list.size(); i++){
			column_pair cp = column_list.get(i);
			System.out.printf("column_name %s, column_type %s \n", cp.column_name, cp.column_type);
		}
		
	}

}

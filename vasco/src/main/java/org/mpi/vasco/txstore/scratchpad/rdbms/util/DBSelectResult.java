package org.mpi.vasco.txstore.scratchpad.rdbms.util;

import org.mpi.vasco.util.debug.Debug;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.io.*;

import org.mpi.vasco.txstore.scratchpad.ScratchpadException;
import org.mpi.vasco.txstore.scratchpad.rdbms.DBScratchpad;
import org.mpi.vasco.txstore.util.Result;

public class DBSelectResult
	extends Result
{
	transient boolean hasDecoded;
	transient List<String[]> result;
	transient int nextLine;
	
	public static DBSelectResult createResult( List<String[]> result) throws ScratchpadException {
		try {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		int nLines = result.size();
		dos.writeShort( nLines);
		if( nLines > 0) {
			dos.writeShort( result.get(0).length);
			for( int i = 0; i < nLines; i++) {
				String[] row = result.get(i);
				for( int j = 0; j < row.length; j++) {
					if( row[j] == null)
						dos.writeUTF( DBScratchpad.SCRATCHPAD_NULL);
					else
						dos.writeUTF( row[j]);
				}
			}
		}
		return new DBSelectResult( result, baos.toByteArray());
		} catch (IOException e) {
			throw new ScratchpadException( "Cannot encode result", e);
		}
	}
	public static DBSelectResult createResult( List<String[]> result, HashMap<String,Integer> columnNamesToNumbersMap) throws ScratchpadException {
//		try {
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		DataOutputStream dos = new DataOutputStream( baos);
//		int nLines = result.size();
//		dos.writeShort( nLines);
//		if( nLines > 0) {
//			dos.writeShort( result.get(0).length);
//			for( int i = 0; i < nLines; i++) {
//				String[] row = result.get(i);
//				for( int j = 0; j < row.length; j++) {
//					if( row[j] == null)
//						dos.writeUTF( DBScratchpad.SCRATCHPAD_NULL);
//					else
//						dos.writeUTF( row[j]);
//				}
//			}
//		}
//		return new DBSelectResult( result, baos.toByteArray(),columnNamesToNumbersMap);
//		} catch (IOException e) {
//			throw new ScratchpadException( "Cannot encode result", e);
//		}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream( baos);
			int nLines = result.size();
			dos.writeShort( nLines);
			if( nLines > 0) {
				dos.writeShort( result.get(0).length);
				for( int i = 0; i < nLines; i++) {
					String[] row = result.get(i);
					for( int j = 0; j < row.length; j++) {
						if( row[j] == null)
							dos.writeUTF( DBScratchpad.SCRATCHPAD_NULL);
						else
							dos.writeUTF( row[j]);
					}
				}
			}
			//now encode the column map
			ByteArrayOutputStream mbaos = new ByteArrayOutputStream();
			DataOutputStream mdos = new DataOutputStream( mbaos);
			nLines = columnNamesToNumbersMap.size();
			mdos.writeShort( nLines);
			Iterator<String> itr = columnNamesToNumbersMap.keySet().iterator();
			while(itr.hasNext()){
				String tmp=itr.next();
				mdos.writeUTF(tmp);
				mdos.writeUTF(columnNamesToNumbersMap.get(tmp).toString());
			}
			return new DBSelectResult( result, baos.toByteArray(),mbaos.toByteArray());
			} catch (IOException e) {
				throw new ScratchpadException( "Cannot encode result", e);
			}
	}
	public static DBSelectResult createResult( byte[] arr) throws ScratchpadException {
		return new DBSelectResult( arr);
	}
	public static DBSelectResult createResult( byte[] arr, HashMap<String,Integer> columnNamesToNumbersMap) throws ScratchpadException {
		return new DBSelectResult( arr,columnNamesToNumbersMap);
	}
	public static DBSelectResult createResult(Result r) throws ScratchpadException {
		return new DBSelectResult(r);
	}

	protected DBSelectResult( List<String[]> result, byte[] resultBA) {
		super( resultBA);
		this.result = result;
		this.hasDecoded = true;
		nextLine = 0;
	}
	
//	protected DBSelectResult( List<String[]> result, byte[] resultBA, HashMap<String,Integer> columnNamesToNumbersMap) {
//		super( resultBA);
//		this.result = result;
//		this.hasDecoded = true;
//		nextLine = 0;
//		this.setAliasToColumnNumbersMap(columnNamesToNumbersMap);
//	}
	protected DBSelectResult( List<String[]> result, byte[] resultBA, byte[] resultMapColumnToIndex) {
		super(resultBA,resultMapColumnToIndex);
		this.result = result;
		this.hasDecoded = true;
		nextLine = 0;
	}

	protected DBSelectResult( byte[] resultBA) {
		super( resultBA);
		this.result = null;
		this.hasDecoded = false;
		nextLine = 0;
	}
	
	protected DBSelectResult( byte[] resultBA, HashMap<String,Integer> columnNamesToNumbersMap) {
		super(resultBA);
		this.result = null;
		this.hasDecoded = false;
		nextLine = 0;
		this.setAliasToColumnNumbersMap(columnNamesToNumbersMap);
	}
	protected DBSelectResult(Result r) {
		super(r.getResult(),r.getColumnIndexesResult());
		this.result = null;
		this.hasDecoded = false;
		nextLine = 0;
	}
	
	public void reset() {
		nextLine = 0;
	}
	
	private void decode() throws ScratchpadException {
		try {
			hasDecoded = true;
			ByteArrayInputStream bais = new ByteArrayInputStream( super.getResult());
			DataInputStream dis = new DataInputStream( bais);
			result = new ArrayList<String[]>();
			int nLines = dis.readShort();
			if( nLines == 0)
				return;
			int nCols = dis.readShort();
			for( int i = 0; i < nLines; i++) {
				String[] row = new String[nCols];
				for( int j = 0; j < nCols; j++) {
					row[j] = dis.readUTF();
				}
				result.add( row);
			}
			
			
			//now decode the column map
			sqlAttributeIndex = new HashMap <String,Integer>();
			if( super.getColumnIndexesResult() != null)
	    	try{
				ByteArrayInputStream mbais = new ByteArrayInputStream(super.getColumnIndexesResult());
				DataInputStream mdis = new DataInputStream(mbais);
				int mapnelements = mdis.readShort();		
				for( int i = 0; i < mapnelements; i++) {
					String columnName = mdis.readUTF();
					String columnPosition = mdis.readUTF();
					sqlAttributeIndex.put(columnName, Integer.parseInt(columnPosition));
				}
			}catch( IOException e) {
				System.err.println("Cannot decode column map");
				e.printStackTrace();
			}
			
		} catch( IOException e) {
			throw new ScratchpadException( "Cannot decode result", e);
		}
		
		//Debug.println(toString());
	}
	

	public List<String[]> getSelectResult() throws ScratchpadException {
		if( ! hasDecoded)
			decode();
		return result;
	}


	/**
	 * Emulate next method in ResultSet
	 * @return
	 * @throws ScratchpadException
	 */
	public boolean next() throws ScratchpadException {
		if( ! hasDecoded)
			decode();
		boolean r = nextLine < result.size();
		if( r)
			nextLine++;
		return r;
	}
	
	
	/**
	 * Emulate first method in ResultSet
	 * @return
	 * @throws ScratchpadException
	 */
	public boolean first() throws ScratchpadException {
		if( ! hasDecoded)
			decode();
		boolean r = (result.size() >= 1);
		if(r)
			nextLine = 1;
		return r;
	}
	
	/**
	 * Emulate getString method in ResultSet. NOTE: col from 1 to number of columns
	 * @param col
	 * @return
	 * @throws ScratchpadException
	 */
	public String getString( int col)  throws ScratchpadException {
		if( ! hasDecoded)
			decode();
		return result.get(nextLine - 1)[col-1];
	}
	/**
	 * Emulate getInt method in ResultSet. NOTE: col from 1 to number of columns
	 * @param col
	 * @return
	 * @throws ScratchpadException
	 */
	public int getInt( int col)  throws ScratchpadException {
		if( ! hasDecoded)
			decode();
		if(result.get(nextLine-1).length < col){
			System.out.println("size of result is " + result.get(nextLine-1).length);
		}
		return Integer.parseInt(result.get(nextLine - 1)[col-1]);
	}
	/**
	 * Emulate getDouble method in ResultSet. NOTE: col from 1 to number of columns
	 * @param col
	 * @return
	 * @throws ScratchpadException
	 */
	public double getDouble( int col)  throws ScratchpadException {
		if( ! hasDecoded)
			decode();
		return Double.parseDouble(result.get(nextLine - 1)[col-1]);
	}

	/**
	 * Emulate getFloat method in ResultSet. NOTE: col from 1 to number of columns
	 * @param col
	 * @return
	 * @throws ScratchpadException
	 */
	public Float getFloat( int col)  throws ScratchpadException {
		if( ! hasDecoded)
			decode();
		String value = result.get(nextLine - 1)[col-1];
		if(value.indexOf("NULL") != -1){
			return 0.0f;
		}
		return Float.parseFloat(value);
	}
	
	public String toString() {
		if( ! hasDecoded)
			try {
				decode();
			} catch (ScratchpadException e) {
				// do nothing
				e.printStackTrace();
			}
		StringBuffer buffer = new StringBuffer();
		Iterator<Map.Entry<String, Integer>> it = sqlAttributeIndex.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Integer> e = it.next();
			buffer.append(e.getKey() +"(pos:"+e.getValue()+")" + "\t");
		}
		buffer.append("\n");

		for( int i = 0; i < result.size(); i++) {
			String[] row = result.get(i);
			for( int j = 0; j < row.length; j++) {
				buffer.append( row[j]);
				buffer.append( "\t");
			}
			buffer.append( "\n");
		}
		return buffer.toString();
	}
	public Date getDate(int col) throws ScratchpadException {
		//the following two lines of code is for UTC date format
		//SimpleDateFormat df= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		//df.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		SimpleDateFormat df= new SimpleDateFormat("yyyy-MM-dd"); 
		Date d=null;
		if( ! hasDecoded)
			decode();
		try {
			
			d = df.parse(result.get(nextLine - 1)[col-1]);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
		
		
	}
}

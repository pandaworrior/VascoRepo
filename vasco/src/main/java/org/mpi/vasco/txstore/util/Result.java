package org.mpi.vasco.txstore.util;
import java.util.HashMap;

import org.mpi.vasco.util.UnsignedTypes;
import org.mpi.vasco.util.debug.Debug;

public class Result implements java.io.Serializable {

    byte[] op;
    byte[] columnindex;
    protected HashMap <String,Integer> sqlAttributeIndex;

    public Result(byte[] b){
	op = b;
    }
    public Result(byte[] b,byte[] queryColumnToIndexMap){
    	op = b;
    	columnindex=queryColumnToIndexMap;
    }

//    public Result(byte b[], int offset){//decode
//
//	long length = UnsignedTypes.bytesToLong(b, offset);
//	offset += UnsignedTypes.uint32Size;
//	op = new byte[(int) length];
//	for(int i = 0;i < op.length; i++)
//	    op[i] = b[i+offset];
//    }
    public Result(byte b[], int offset){//decode
    	long length = UnsignedTypes.bytesToLong(b, offset);
    	offset += UnsignedTypes.uint32Size;

    	int maplength = UnsignedTypes.bytesToInt(b, offset);
    	offset += UnsignedTypes.uint16Size;
    	
    	columnindex = new byte[maplength];
    	
    	for(int i = 0;i < maplength; i++)
    		columnindex[i] = b[i+offset];
    	offset+=maplength;

    	//decode map here - only once!
//    	sqlAttributeIndex = new HashMap <String,Integer>();
//    	try{
//			ByteArrayInputStream mbais = new ByteArrayInputStream(columnindex);
//			DataInputStream mdis = new DataInputStream(mbais);
//			int mapnelements = mdis.readShort();
//	    	Debug.println("elements in the map "+mapnelements);			
//			for( int i = 0; i < mapnelements; i++) {
//				sqlAttributeIndex.put(mdis.readUTF(), Integer.parseInt(mdis.readUTF()));
//			}
//		}catch( IOException e) {
//			System.err.println("Cannot decode column map");
//			e.printStackTrace();
//		}
    	
    	
    	
    	//decoding the result
    	int result_size=(int)length-maplength;
    	op = new byte[result_size];
    	for(int i = 0;i < result_size; i++)
    		op[i] = b[i+offset];
    	offset+=result_size;

        }
    

//    public void getBytes(byte[] b, int offset){//encode
//	UnsignedTypes.longToBytes(op.length, b, offset);
//	offset += UnsignedTypes.uint32Size;
//	for (int i = 0; i < op.length; i++)
//	    b[i+offset] = op[i];
//    }
    public void getBytes(byte[] b, int offset){//encode new header!
    	int initialoffset=offset;
    	Debug.println("header offset "+offset);
    	int total=op.length+columnindex.length;
    	UnsignedTypes.longToBytes(total, b, offset);
    	Debug.println("totalsize "+total);
    	offset += UnsignedTypes.uint32Size;
    	UnsignedTypes.intToBytes(columnindex.length, b, offset);
    	Debug.println("map size "+columnindex.length);
    	offset += UnsignedTypes.uint16Size;
   	
    	for (int i = 0; i < columnindex.length; i++){
    	    b[i+offset] = columnindex[i];
    	}
    	offset +=  columnindex.length;
    	for (int i = 0; i < op.length; i++)
    	    b[i+offset] = op[i];
    }

//    public final int getByteSize(){
//	return op.length + UnsignedTypes.uint32Size;
//    }
    
    public final int getByteSize(){
    	//	   totalsize				#mapoffset                #mapsize            #rowsize
    	return UnsignedTypes.uint32Size+UnsignedTypes.uint16Size+ columnindex.length +op.length;
    }
    public byte[] getResult(){
	return op;
    }
    public byte[] getColumnIndexesResult(){
    	return columnindex;
    }
    /**
     * TODO make it part of the constructor
     */
    public void setAliasToColumnNumbersMap(HashMap<String,Integer> attributeMap){
    	this.sqlAttributeIndex = attributeMap;
    }
    public HashMap<String,Integer> getColumnAliasToNumbersMap(){
    	return sqlAttributeIndex;
    }
}


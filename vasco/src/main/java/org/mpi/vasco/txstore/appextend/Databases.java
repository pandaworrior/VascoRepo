package org.mpi.vasco.txstore.appextend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

/*parse xml file*/

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.mpi.vasco.util.debug.Debug;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class Databases {
	/*define a list of databases*/
	public static int databaseNum;
	public ArrayList<Database> dbList;
	
	public Databases(){
		databaseNum = 0;
		dbList = new ArrayList<Database>();
	}
	
	public void parseXMLfile(String xmlFile){
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			Document dom = db.parse(xmlFile);
			
			parseDocument(dom);

		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}	
	}
	
	public void parseDocument(Document dom){
		//get the root element
		Element docEle = dom.getDocumentElement();
		//get databaseNum
		databaseNum = Integer.parseInt(docEle.getAttribute("dbNum"));
		//get a nodelist of elements
		NodeList nl = docEle.getElementsByTagName("database");
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {

				//get the database element
				Element el = (Element)nl.item(i);

				//get the database object
				Database e = getDatabase(el);
				dbList.add(e);
			}
		}


	}

	public Database getDatabase(Element dbEl){
		int dc_id = 0;
		int db_id = 0;
		Vector<String> connInfo = new Vector<String>();
		Vector<String> tableList = new Vector<String>();
		Vector<String> tableLWWList = new Vector<String>();
		Vector<String> redTableList = new Vector<String>();
		Vector<String> blueTableList = new Vector<String>();
		
		dc_id = Integer.parseInt(dbEl.getAttribute("dcId"));
		db_id = Integer.parseInt(dbEl.getAttribute("dbId"));
		connInfo.add(dbEl.getAttribute("dbHost"));
		connInfo.add(dbEl.getAttribute("dbPort"));
		connInfo.add(dbEl.getAttribute("dbUser"));
		connInfo.add(dbEl.getAttribute("dbPwd"));
		connInfo.add(dbEl.getAttribute("dbName"));
		String urlPrefix = dbEl.getAttribute("url_prefix");
		
		String[] tmp;
		tmp = dbEl.getAttribute("tableList").split(",");
		for(int i = 0; i < tmp.length; i++){
			tableList.add(tmp[i]);
		}
		
		tmp = dbEl.getAttribute("tableLWW").split(",");
		for(int i = 0; i < tmp.length; i++){
			tableLWWList.add(tmp[i]);
		}
	
		tmp = dbEl.getAttribute("redTable").split(",");
		for(int i = 0; i < tmp.length; i++){
			redTableList.add(tmp[i]);
		}
		
		tmp = dbEl.getAttribute("blueTable").split(",");
		for(int i = 0; i < tmp.length; i++){
			if(tmp[i].equals("") == false)
				blueTableList.add(tmp[i]);
		}
		
		Database dbInfo = new Database(dc_id, db_id, connInfo, tableList, tableLWWList, redTableList, blueTableList, urlPrefix);
		getTables(dbInfo, dbEl);
		return dbInfo;
	}
	
	public void getTables(Database dbInfo, Element dbEl){
		
		NodeList nl = dbEl.getElementsByTagName("table");
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {
				System.out.printf("table %d", i);
				//get the table node
				Element el = (Element)nl.item(i);
				// get table
				String primaryKey = el.getAttribute("primaryKey");
				dbInfo.setPrimaryKey(primaryKey);
				dbInfo.setTableSchema(getTable(el, dbInfo.db_id));
			}
		}
		
	}
	
	public TableSchema getTable(Element tableEl, int db_id){
		
		String tableName = tableEl.getAttribute("tableName");
		TableSchema tch = new TableSchema(tableName, db_id);
		String[] tmp;
		tmp = tableEl.getAttribute("columnName").split(",");
		for(int i = 0; i < tmp.length; i++){
			String columnType = tableEl.getAttribute(tmp[i]);
			tch.addToColumnList(tmp[i], columnType);
		}
		return tch;	
	}
	
	public Database returnDB(int dcId, int dbId){
		for(int i = 0; i< dbList.size(); i++){
			Database e = dbList.get(i);
			if(e.dc_id == dcId && e.db_id == dbId){
				return e;
			}
		}
		return null;
	}
	
	public void printOut(){
		Debug.printf("database Num: %d \n", databaseNum);
		for(int i = 0; i < dbList.size(); i++){
			Debug.print("------------------\n");
			Database e = dbList.get(i);
			e.printOut();
			
		}
	}
}

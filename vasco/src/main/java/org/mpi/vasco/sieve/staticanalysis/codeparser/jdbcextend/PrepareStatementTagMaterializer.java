/********************************************************************
Copyright (c) 2013 chengli.
All rights reserved. This program and the accompanying materials
are made available under the terms of the GNU Public License v2.0
which accompanies this distribution, and is available at
http://www.gnu.org/licenses/old-licenses/gpl-2.0.html

Contributors:
    chengli - initial API and implementation

Contact:
    To distribute or use this code requires prior specific permission.
    In this case, please contact chengli@mpi-sws.org.
********************************************************************/
/**
 * This class is used to replace @xxx@ in the PrepareStatement
 * creation with the concrete sql statement.
 */
package org.mpi.vasco.sieve.staticanalysis.codeparser.jdbcextend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mpi.vasco.util.commonfunc.StringOperations;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class PrepareStatementTagMaterializer.
 *
 * @author chengli
 */
public class PrepareStatementTagMaterializer {
	
	/** The dir path. */
	String dirPath;
	
	/** The java file name. */
	String javaFileName;
	
	/** The sql properties file name. */
	String sqlPropertiesFileName;
	
	/** The mappings from tag to sql. */
	HashMap<String, String> mappingsFromTagToSql;
	
	/** The comment opener in sql properties. */
	static String commentOpenerInSqlProperties = "#";
	
	/** The sql properties split symbol. */
	static String sqlPropertiesSplitSymbol = "=";
	
	/** The sql tag reg str. */
	static String sqlTagRegStr = "@.*@";
	
	/** The sql tag start or end. */
	static String sqlTagStartOrEnd = "@";
	
	/** The sql wrap line symbol. */
	static String sqlWrapLineSymbol = "+\\";
	
	/** The double quote str. */
	static String doubleQuoteStr = "\"";
	
	/** The replace command list. */
	List<String> replaceCommandList;
	
	/** The shell script name. */
	static String shellScriptName = "replaceSqlTagScript.sh";
	
	/**
	 * Instantiates a new prepare statement tag materializer.
	 *
	 * @param dirPath the dir path
	 * @param javaFileName the java file name
	 * @param sqlPropertiesFileName the sql properties file name
	 */
	public PrepareStatementTagMaterializer(String dirPath, String javaFileName, String sqlPropertiesFileName){
		this.setDirPath(dirPath);
		this.setJavaFileName(javaFileName);
		this.setSqlPropertiesFileName(sqlPropertiesFileName);
		mappingsFromTagToSql = new HashMap<String, String>();
		this.loadSqlFromPropertiesFile();
		replaceCommandList = new ArrayList<String>();
	}
	
	/**
	 * Load sql from properties file.
	 */
	public void loadSqlFromPropertiesFile(){
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream(this.getSqlPropertiesFileFullPath())));
			String line;
			String readyStr = "";
			while ((line = br.readLine()) != null) {
				if(!line.startsWith(commentOpenerInSqlProperties) && !line.equals("")){
					
					readyStr += line;
					//remove all space
					String newStr = line.replaceAll("\\s+", "");
					if(newStr.endsWith(sqlWrapLineSymbol)){
						continue;
					}
					int splitIndex = readyStr.indexOf(sqlPropertiesSplitSymbol);
					if(splitIndex != -1){
						String tagStr = readyStr.substring(0, splitIndex);
						String sqlStr = readyStr.substring(splitIndex + 1);
						if(mappingsFromTagToSql.containsKey(tagStr)){
							Debug.println("contained " + tagStr);
							System.exit(-1);
						}else{
							mappingsFromTagToSql.put(tagStr, sqlStr);
						}
					}else{
						Debug.println("contain something else");
						System.exit(-1);
					}
					readyStr = "";
				}
			}
			br.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Replace tag str in java file with sql.
	 */
	public void replaceTagStrInJavaFileWithSql(){
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream(this.getJavaFileFullPath())));
			String line;
			while ((line = br.readLine()) != null) {
				int beginIndex = line.indexOf(sqlTagStartOrEnd);
				if(beginIndex != -1){
					int endIndex = line.indexOf(sqlTagStartOrEnd, beginIndex + 1);
					if(endIndex != -1){
						String tagStr = line.substring(beginIndex + 1, endIndex);
						String sqlStr = this.getMappingsFromTagToSql().get(tagStr);
						if(sqlStr == null){
							Debug.println("Not found, please check " + tagStr);
						}else{
							sqlStr = StringOperations.trimDoubleQuotesHeadTail(sqlStr);
							String command = "sed -i 's/@"+tagStr+"@/"+doubleQuoteStr+sqlStr+doubleQuoteStr+"/g'" +" " + this.getJavaFileFullPath();
							Debug.println("sed command " + command);
							this.replaceCommandList.add(command);
						}
					}
				}
			}
			br.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//create a script 
		this.createShellScript();
		//execute all commands
		this.executeShellCommands();
	}
	
	/**
	 * Creates the shell script.
	 */
	public void createShellScript(){
		String path = this.getDirPath()+"/"+shellScriptName;
		File f = new File(path);
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			FileWriter fw = new FileWriter(path);
			fw.write("#!/bin/bash\n");
			for(String command : this.replaceCommandList){
				fw.write(command+"\n");
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Make script executable.
	 */
	public void makeScriptExecutable(){
		try {
			Process proc = Runtime.getRuntime().exec("chmod +x "+ this.getDirPath()+"/"+shellScriptName);
	        InputStream stderr = proc.getErrorStream();
	        InputStreamReader isr = new InputStreamReader(stderr);
	        BufferedReader br = new BufferedReader(isr);
	        String line = null;
	        System.out.println("<ERROR>");
	        while ( (line = br.readLine()) != null)
	        	System.out.println(line);
	            System.out.println("</ERROR>");
				try {
					if(proc.waitFor() == 0){
						Debug.println("command succeeds");
					}else{
						Debug.println("command failed");
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	/**
	 * Execute shell commands.
	 */
	public void executeShellCommands(){
		this.makeScriptExecutable();
		try {
			Process proc = Runtime.getRuntime().exec("sh "+ this.getDirPath()+"/"+shellScriptName);
	        InputStream stderr = proc.getErrorStream();
	        InputStreamReader isr = new InputStreamReader(stderr);
	        BufferedReader br = new BufferedReader(isr);
	        String line = null;
	        System.out.println("<ERROR>");
	        while ( (line = br.readLine()) != null)
	        	System.out.println(line);
	            System.out.println("</ERROR>");
				try {
					if(proc.waitFor() == 0){
						Debug.println("command succeeds");
					}else{
						Debug.println("command failed");
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
	/**
	 * Gets the dir path.
	 *
	 * @return the dirPath
	 */
	public String getDirPath() {
		return dirPath;
	}
	
	/**
	 * Sets the dir path.
	 *
	 * @param dirPath the dirPath to set
	 */
	public void setDirPath(String dirPath) {
		this.dirPath = dirPath;
	}
	
	/**
	 * Gets the java file name.
	 *
	 * @return the javaFileName
	 */
	public String getJavaFileName() {
		return javaFileName;
	}
	
	/**
	 * Sets the java file name.
	 *
	 * @param javaFileName the javaFileName to set
	 */
	public void setJavaFileName(String javaFileName) {
		this.javaFileName = javaFileName;
	}
	
	/**
	 * Gets the sql properties file name.
	 *
	 * @return the sqlPropertiesFileName
	 */
	public String getSqlPropertiesFileName() {
		return sqlPropertiesFileName;
	}
	
	/**
	 * Sets the sql properties file name.
	 *
	 * @param sqlPropertiesFileName the sqlPropertiesFileName to set
	 */
	public void setSqlPropertiesFileName(String sqlPropertiesFileName) {
		this.sqlPropertiesFileName = sqlPropertiesFileName;
	}
	
	/**
	 * Gets the sql properties file full path.
	 *
	 * @return the sql properties file full path
	 */
	public String getSqlPropertiesFileFullPath(){
		return this.dirPath + "/" + this.sqlPropertiesFileName;
	}
	
	/**
	 * Gets the java file full path.
	 *
	 * @return the java file full path
	 */
	public String getJavaFileFullPath(){
		return this.dirPath + "/" + this.javaFileName;
	}

	/**
	 * Gets the mappings from tag to sql.
	 *
	 * @return the mappingsFromTagToSql
	 */
	public HashMap<String, String> getMappingsFromTagToSql() {
		return mappingsFromTagToSql;
	}

	/**
	 * Sets the mappings from tag to sql.
	 *
	 * @param mappingsFromTagToSql the mappingsFromTagToSql to set
	 */
	public void setMappingsFromTagToSql(HashMap<String, String> mappingsFromTagToSql) {
		this.mappingsFromTagToSql = mappingsFromTagToSql;
	}

}

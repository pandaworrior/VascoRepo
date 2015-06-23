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
 * 
 */
package org.mpi.vasco.util.commonfunc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class FileOperations.
 */
public final class FileOperations {

	/**
	 * Removes the last slash.
	 *
	 * @param fileName the file name
	 * @return the string
	 */
	public static String removeLastSlash(String fileName) {
		if (fileName.lastIndexOf('/') == fileName.length() - 1) {
			return fileName.substring(0, fileName.length() - 1);
		} else {
			return fileName;
		}
	}

	/**
	 * Gets the all files under dir.
	 *
	 * @param dirName the dir name
	 * @return the all files under dir
	 */
	public static List<String> getAllFilesUnderDir(String dirName) {
		dirName = removeLastSlash(dirName);
		ArrayList<String> fileList = new ArrayList<String>();
		File dir = new File(dirName);
		if (!dir.isDirectory()) {
			throw new IllegalStateException("Not a dir!");
		}
		for (File file : dir.listFiles()) {
			String fileName = file.getName();
			Debug.println("dir: " + dirName + " file: " + fileName);
			fileList.add(dirName + "/" + fileName);
		}
		return fileList;
	}
	
	public static String getToLevelDir(String fullPath) {
		int index = fullPath.lastIndexOf('/');
		if(index == -1)
			return fullPath;
		else
			return fullPath.substring(0, index);
	}

	/**
	 * Checks if is java source file.
	 *
	 * @param f the f
	 * @return true, if is java source file
	 */
	public static boolean isJavaSourceFile(String f) {
		String ext = FilenameUtils.getExtension(f);
		if (ext.endsWith("java")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if is directory.
	 *
	 * @param fT the f t
	 * @return true, if is directory
	 */
	public static boolean isDirectory(FileTypes fT) {
		if (fT == FileTypes.DIRECTORY) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Gets the file type.
	 *
	 * @param fileName the file name
	 * @return the file type
	 */
	public static FileTypes getFileType(String fileName) {
		File file = new File(fileName);
		if (file.isDirectory()) {
			return FileTypes.DIRECTORY;
		} else {
			if (isJavaSourceFile(fileName)) {
				return FileTypes.JAVA_FILE;
			} else {
				return FileTypes.NON_JAVA_FILE;
			}
		}
	}

	/**
	 * Checks if is dir empty.
	 *
	 * @param fileName the file name
	 * @return true, if is dir empty
	 */
	public static boolean isDirEmpty(String fileName) {
		File dir = new File(fileName);
		if (dir.list().length == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if is existed.
	 *
	 * @param fileName the file name
	 * @return true, if is existed
	 */
	public static boolean isExisted(String fileName) {
		File file = new File(fileName);
		return file.exists();
	}
	
	public static void createFileByGivenFilePath(String fullPath) {
		File f = new File(fullPath);
		if(isExisted(fullPath)) {
			if(!f.delete()) {
				System.out.println(" cannot delete this file " + fullPath);
				System.exit(-1);
			}
		}
		try {
			f.createNewFile();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeToFile(String fullPath, List<String> strs) {
		try {
			FileWriter fw = new FileWriter(fullPath);
			for(String _str : strs) {
				fw.write(_str);
			}
			fw.close();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String createShellScript(String dirPath, String fileName, List<String> commands){
		String path = dirPath+"/"+fileName +".sh";
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
			for(String command : commands) {
				fw.write(command+"\n");
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path;
	}
	
	public static void makeScriptExecutable(String shellScriptPath){
		try {
			Process proc = Runtime.getRuntime().exec("chmod +x "+ shellScriptPath);
	        InputStream stderr = proc.getErrorStream();
	        InputStreamReader isr = new InputStreamReader(stderr);
	        BufferedReader br = new BufferedReader(isr);
	        String line = null;
	        while ( (line = br.readLine()) != null)
	        	Debug.println(line);
				try {
					if(proc.waitFor() == 0){
						Debug.println("command succeeds");
					}else{
						System.out.println("command failed");
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
	public static void executeShellCommands(String shellFilePath){
		Debug.println("execute shell command " + shellFilePath);
		String dirPath = getToLevelDir(shellFilePath);
		Debug.println("get top level dir " + dirPath);
		makeScriptExecutable(shellFilePath);
		
		List<String> commands = new ArrayList<String>();
		commands.add("sh");
		commands.add(shellFilePath);
		ProcessBuilder pb = new ProcessBuilder(commands);
        pb.directory(new File(dirPath));
        pb.redirectErrorStream(true);
        Process process;
		try {
			process = pb.start();
			 //Read output
	        StringBuilder out = new StringBuilder();
	        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String line = null, previous = null;
	        while ((line = br.readLine()) != null)
	            if (!line.equals(previous)) {
	                previous = line;
	                out.append(line).append('\n');
	                Debug.println(line);
	            }

	        //Check result
	        if (process.waitFor() == 0)
	            Debug.println("Success!");
	        else {
	        	System.out.println("Failed!");
	        	System.err.println(out.toString());
	        }
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		/*try {
			Process proc = Runtime.getRuntime().exec("cd " +dirPath +" && sh "+shellFilePath);
	        InputStream stdin = proc.getInputStream();
	        InputStreamReader isr = new InputStreamReader(stdin);
	        BufferedReader br = new BufferedReader(isr);
	        String line = null;
	        System.out.println("<OUTPUT>");
	        while ( (line = br.readLine()) != null)
	        	System.out.println(line);
	            System.out.println("</OUTPUT>");
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
		} */
		
	}
	
	public static String readFirstLineFromFile(String fileFullPath) {
		String _str = "";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(fileFullPath));
	        _str  = br.readLine();
	        br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return _str;
	}

}

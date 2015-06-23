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
package org.mpi.vasco.sieve.staticanalysis.codeparser;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.type.Type;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.mpi.vasco.sieve.staticanalysis.codeparser.javaparserextend.VariableType;

import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGGraph;
import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGNode;
import org.mpi.vasco.util.commonfunc.StringOperations;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class JavaFileParser.
 * 
 * @author chengli This class is used to parse each individual java file and
 *         return a list of transactions and their control flow graph.
 */
public class JavaFileParser {

	/** The file name. */
	private String fileName;

	/** The package name. */
	private PackageDeclaration packageDecl;

	/** The import list. */
	private List<ImportDeclaration> importList;

	/** The compilation unit. */
	private CompilationUnit compilationUnit;

	/**
	 * The class.method to control flow graph mappings. The first
	 * ClassOrInterfaceDeclaration is the class where methods belong to
	 */
	private HashMap<ClassOrInterfaceDeclaration, HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>> classMethodCFGMappings;
	
	/** The class to field definitions mapping. */
	private HashMap<ClassOrInterfaceDeclaration, HashMap<VariableType, String>> classFieldMappings;

	/**
	 * Instantiates a new java file parser.
	 * 
	 * @param f
	 *            the filename
	 */
	public JavaFileParser(String f) {
		this.setFileName(f);
		this.setCompilationUnit();
	}

	/**
	 * Sets the compilation unit.
	 */
	public void setCompilationUnit() {
		FileInputStream in = null;
		try {
			in = new FileInputStream(this.fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			this.compilationUnit = JavaParser.parse(in);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.setpackageDecl(this.compilationUnit.getPackage());
		this.setImportList(this.compilationUnit.getImports());
	}

	/**
	 * Gets the file name.
	 * 
	 * @return the file name
	 */
	public String getFileName() {
		return this.fileName;
	}

	/**
	 * Sets the file name.
	 * 
	 * @param fileName
	 *            the new file name
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Gets the package name.
	 * 
	 * @return the package name
	 */
	public PackageDeclaration getpackageDecl() {
		return this.packageDecl;
	}

	/**
	 * Gets the package name plain text.
	 * 
	 * @return the package name plain text
	 */
	public String getPackageNamePlainText() {
		if(this.getpackageDecl() == null || this.getpackageDecl().getName() == null) {
			return StringOperations.splitStringBySlashReturnLastOne(this.getFileName());
		}else {
			return this.getpackageDecl().getName().getName();
		}
	}

	/**
	 * Sets the package name.
	 * 
	 * @param packageDecl
	 *            the new package name
	 */
	public void setpackageDecl(PackageDeclaration packageDecl) {
		this.packageDecl = packageDecl;
	}

	/**
	 * Gets the import list.
	 *
	 * @return the importList
	 */
	public List<ImportDeclaration> getImportList() {
		return importList;
	}

	/**
	 * Sets the import list.
	 *
	 * @param importList the importList to set
	 */
	public void setImportList(List<ImportDeclaration> importList) {
		this.importList = importList;
	}

	/**
	 * Gets the class method cfg mappings.
	 *
	 * @return the classMethodCFGMappings
	 */
	public HashMap<ClassOrInterfaceDeclaration, HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>> getClassMethodCFGMappings() {
		return classMethodCFGMappings;
	}

	/**
	 * Sets the class method cfg mappings.
	 *
	 * @param classMethodCFGMappings the classMethodCFGMappings to set
	 */
	public void setClassMethodCFGMappings(
			HashMap<ClassOrInterfaceDeclaration, HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>> classMethodCFGMappings) {
		this.classMethodCFGMappings = classMethodCFGMappings;
	}
	
	/**
	 * Parse the whole java file.
	 * It will get all control flow graph.
	 * It will get all field declarations for a class.
	 */
	public void parseJavaFile() {
		this.classMethodCFGMappings = new HashMap<ClassOrInterfaceDeclaration, HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>>();
		this.classFieldMappings = new HashMap<ClassOrInterfaceDeclaration, HashMap<VariableType, String>>();
		List<TypeDeclaration> types = this.compilationUnit.getTypes();
		for (TypeDeclaration type : types) {
			if (type instanceof ClassOrInterfaceDeclaration) {
				ClassOrInterfaceDeclaration classDecl = (ClassOrInterfaceDeclaration) type;
				Debug.println("Class name: " + classDecl.getName());
				List<BodyDeclaration> members = classDecl.getMembers();
				HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>> methodCFGMappings = new HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>();
				HashMap<VariableType, String> fieldMap = new HashMap<VariableType, String>();
				for (BodyDeclaration member : members) {
					if (member instanceof MethodDeclaration) {
						MethodIdentifier mId = new MethodIdentifier(
								this.getPackageNamePlainText(), type.getName(),
								((MethodDeclaration) member).getName());
						// this.methods.put(mId, (MethodDeclaration) member);
						CFGGraph<CodeNodeIdentifier, Expression> cfg = this
								.obtainControlFlowGraphForMethod(mId,
										(MethodDeclaration) member);
						if(cfg == null) {
							Debug.println("This function doesn't have a cfg " + mId.toString());
						}else {
							methodCFGMappings.put((MethodDeclaration) member, cfg);
						}
					}else{
						if(member instanceof FieldDeclaration){
							Debug.println("I have a field definitions");
							Debug.println("entire fieldDeclaration " + member.toString());
							Type defType = ((FieldDeclaration) member).getType();
							Debug.println("start line: " + defType.getBeginLine());
							Debug.println("type " + defType.toString());
							List<VariableDeclarator> fieldList = ((FieldDeclaration) member).getVariables();
							for(VariableDeclarator var : fieldList){
								Debug.println(var.toString());
								fieldMap.put(new VariableType(defType), var.toString());
								Debug.println("fieldMap size: " + fieldMap.size());
							}
						}
					}
				}
				this.classMethodCFGMappings.put(classDecl, methodCFGMappings);
				this.classFieldMappings.put(classDecl, fieldMap);
			}
		}
	}
	
	/**
	 * Gets the method control flow graph mapping.
	 *
	 * @return the method control flow graph mapping
	 */
	public HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>> getMethodControlFlowGraphMapping(){
		HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>> methodCfgMappings = new HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>();
		Iterator<Entry<ClassOrInterfaceDeclaration, HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>>> it = this.classMethodCFGMappings.entrySet().iterator();
		while(it.hasNext()){
			methodCfgMappings.putAll(it.next().getValue());
		}
		return methodCfgMappings;
	}


	/**
	 * Obtain control flow graph for method.
	 *
	 * @param methodId the method identifier
	 * @param methodDecl the method declaration
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> obtainControlFlowGraphForMethod(
			MethodIdentifier methodId, MethodDeclaration methodDecl) {
		CFGGraph<CodeNodeIdentifier, Expression> cfg = new MethodVisitor()
				.visit(methodDecl, methodId);
		return cfg;
	}
	
	/**
	 * Gets the all class or interface definitions.
	 *
	 * @return the all class or interface definitions
	 */
	public Set<ClassOrInterfaceDeclaration> getAllClassOrInterfaceDefinitions(){
		Set<ClassOrInterfaceDeclaration> classOrIntDeclList = this.classMethodCFGMappings.keySet();
		return classOrIntDeclList;
	}
	
	/**
	 * Gets the all variable declarations by class or interface name.
	 *
	 * @param classOrInterfaceName the class or interface name
	 * @return the all variable declarations by class or interface name
	 */
	public HashMap<VariableType, String> getAllVariableDeclarationsByClassOrInterfaceName(String classOrInterfaceName){
		Iterator<Entry<ClassOrInterfaceDeclaration, HashMap<VariableType, String>>> it = this.classFieldMappings.entrySet().iterator();
		while(it.hasNext()){
			Entry<ClassOrInterfaceDeclaration, HashMap<VariableType, String>> tuple = it.next();
			if(tuple.getKey().getName().equals(classOrInterfaceName)){
				return tuple.getValue();
			}
		}
		return null;
	}
	
	/**
	 * Gets the all method de clarations by class or interface name.
	 *
	 * @param classOrInterfaceName the class or interface name
	 * @return the all method de clarations by class or interface name
	 */
	public Set<MethodDeclaration> getAllMethodDeClarationsByClassOrInterfaceName(String classOrInterfaceName){
		Set<MethodDeclaration> methodDeclSet = null;
		Iterator<Entry<ClassOrInterfaceDeclaration, HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>>> it = this.classMethodCFGMappings.entrySet().iterator();
		while(it.hasNext()){
			Entry<ClassOrInterfaceDeclaration, HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>> tuple = it.next();
			if(tuple.getKey().getName().equals(classOrInterfaceName)){
				methodDeclSet = tuple.getValue().keySet();
				break;
			}
		}
		return methodDeclSet;
	}
	
	/**
	 * Gets the all variable declarations by method declaration.
	 *
	 * @param classOrInterfaceName the class or interface name
	 * @param methodDecl the method decl
	 * @return the all variable declarations by method declaration
	 */
	public HashMap<VariableType, String> getAllVariableDeclarationsByMethodDeclaration(String classOrInterfaceName, MethodDeclaration methodDecl){
		//get the method cfg
		CFGGraph<CodeNodeIdentifier, Expression> cfg = this.getControlFlowGraph(classOrInterfaceName, methodDecl);
		return this.getAllLocalVariableDeclarations(cfg);
	}
	
	/**
	 * Gets the all variable declaration exprs from a control flow graph.
	 *
	 * @param cfg the cfg
	 * @return the all variable declaration exprs
	 */
	public HashMap<VariableType, String> getAllLocalVariableDeclarations(CFGGraph<CodeNodeIdentifier, Expression> cfg){
		HashMap<VariableType, String> defTypeMaps = new HashMap<VariableType, String>();
		//iterate the entire control flow graph and get the variable decalaration expression
		List<CFGNode<CodeNodeIdentifier, Expression>> cfgNodeList = cfg.getNodeListViaBFS();
		for(CFGNode<CodeNodeIdentifier, Expression> cfgNode : cfgNodeList){
			Expression expr = cfgNode.getNodeData();
			if(expr instanceof VariableDeclarationExpr){
				VariableType defType = new VariableType(((VariableDeclarationExpr) expr).getType());
				List<VariableDeclarator> varList = ((VariableDeclarationExpr) expr).getVars();
				for(VariableDeclarator var : varList){
					Debug.println(var.toString());
					defTypeMaps.put(defType, var.toString());
				}
			}
		}
		return defTypeMaps;
	}

	
	/**
	 * Search for method declaration by details.
	 *
	 * @param classOrInterfaceName the class or interface name
	 * @param methodName the method name
	 * @return the list
	 */
	public List<MethodDeclaration> searchForMethodDeclarationByDetails(String classOrInterfaceName, String methodName){
		Debug.println("search for method declaration by classname " + classOrInterfaceName + " methodname " + methodName);
		List<MethodDeclaration> methodDeclList = null;
		Iterator<Entry<ClassOrInterfaceDeclaration, HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>>> it = this.classMethodCFGMappings.entrySet().iterator();
		while(it.hasNext()){
			Entry<ClassOrInterfaceDeclaration, HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>> tuple = it.next();
			if(tuple.getKey().getName().equals(classOrInterfaceName)){
				Debug.println("find one matched class");
				methodDeclList = new ArrayList<MethodDeclaration>();
				Set<MethodDeclaration> methodDeclSet = tuple.getValue().keySet();
				Iterator<MethodDeclaration> methodDeclIt = methodDeclSet.iterator();
				while(methodDeclIt.hasNext()){
					MethodDeclaration methodDecl = methodDeclIt.next();
					if(methodDecl.getName().equals(methodName)){
						methodDeclList.add(methodDecl);
					}
				}
			}
		}
		if(methodDeclList == null){
			Debug.println("The method declaration list is null");
		}else{
			if(methodDeclList.isEmpty()){
				Debug.println("Warning there is no such method exit");
			}else{
				if(methodDeclList.size() == 1){
					Debug.println("Perfect that you get one method declaration");
				}else{
					Debug.println("you get more than one method declaration " + methodDeclList.size());
				}
			}
		}
		return methodDeclList;
	}
	
	/**
	 * Search for method declaration by details.
	 *
	 * @param classOrInterfaceName the class or interface name
	 * @param methodName the method name
	 * @param numOfArgs the num of args
	 * @return the list
	 */
	public List<MethodDeclaration> searchForMethodDeclarationByDetails(String classOrInterfaceName, String methodName, int numOfArgs){
		Debug.println("search for method declaration by classname " + classOrInterfaceName + 
				" methodname " + methodName + " numOfArgs " + numOfArgs);
		List<MethodDeclaration> methodDeclList = null;
		Iterator<Entry<ClassOrInterfaceDeclaration, HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>>> it = this.classMethodCFGMappings.entrySet().iterator();
		while(it.hasNext()){
			Entry<ClassOrInterfaceDeclaration, HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>> tuple = it.next();
			if(tuple.getKey().getName().equals(classOrInterfaceName)){
				Debug.println("find one matched class");
				methodDeclList = new ArrayList<MethodDeclaration>();
				Set<MethodDeclaration> methodDeclSet = tuple.getValue().keySet();
				Iterator<MethodDeclaration> methodDeclIt = methodDeclSet.iterator();
				while(methodDeclIt.hasNext()){
					MethodDeclaration methodDecl = methodDeclIt.next();
					if(methodDecl.getName().equals(methodName)){
						if((methodDecl.getParameters() == null && numOfArgs == 0)
								|| (methodDecl.getParameters() != null && 
									methodDecl.getParameters().size() == numOfArgs)){
							methodDeclList.add(methodDecl);	
						}
					}
				}
			}
		}
		if(methodDeclList == null){
			Debug.println("The method declaration list is null");
		}else{
			if(methodDeclList.isEmpty()){
				Debug.println("Warning there is no such method exit");
			}else{
				if(methodDeclList.size() == 1){
					Debug.println("Perfect that you get one method declaration");
				}else{
					Debug.println("you get more than one method declaration " + methodDeclList.size());
				}
			}
		}
		return methodDeclList;
	}
	
	/**
	 * Gets the control flow graph for a method.
	 *
	 * @param classOrInterfaceName the class or interface name
	 * @param methodDecl the method decl
	 * @return the control flow graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> getControlFlowGraph(String classOrInterfaceName, MethodDeclaration methodDecl){
		Iterator<Entry<ClassOrInterfaceDeclaration, HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>>> it = this.classMethodCFGMappings.entrySet().iterator();
		while(it.hasNext()){
			Entry<ClassOrInterfaceDeclaration, HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>> tuple = it.next();
			if(tuple.getKey().getName().equals(classOrInterfaceName)){
				return tuple.getValue().get(methodDecl);
			}
		}
		return null;
	}
	
	/**
	 * Checks if is class or interface declaration contained.
	 *
	 * @param classOrInterfaceName the class or interface name
	 * @return true, if is class or interface declaration contained
	 */
	public boolean isClassOrInterfaceDeclarationContained(String classOrInterfaceName){
		//get class or interface declaration
		Set<ClassOrInterfaceDeclaration> classOrInterfaceDeclSet = this.getAllClassOrInterfaceDefinitions();
		Iterator<ClassOrInterfaceDeclaration> it = classOrInterfaceDeclSet.iterator();
		while(it.hasNext()){
			ClassOrInterfaceDeclaration classDecl = it.next();
			if(classDecl.getName().equals(classOrInterfaceName)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the import package names.
	 *
	 * @return the import package names
	 */
	public List<String> getImportPackageNames(){
		List<String> importPackageNames = new ArrayList<String>();
		if(this.importList != null){
			for(ImportDeclaration importPackage : this.importList){
				importPackageNames.add(importPackage.getName().toString());
				Debug.println("import package : " +  importPackage.getName().toString());
			}
		}
		return importPackageNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	/**
	 * @see java.lang.Object#toString()
	 * @return
	 */
	public String toString() {
		String str = "";
		str += "comments: " + this.compilationUnit.getComments();
		str += "start line at " + this.compilationUnit.getBeginLine();
		str += "end line at " + this.compilationUnit.getEndLine();
		str += "import " + this.compilationUnit.getImports();
		str += "package " + this.compilationUnit.getPackage();
		return str;
	}

	/**
	 * Prints the out.
	 */
	public void printOut() {
		String str = this.toString();
		System.out.println(str);
	}
	
	/**
	 * Prints the out control flow graphs.
	 */
	public void printOutControlFlowGraphs(){
		Iterator<Entry<ClassOrInterfaceDeclaration, HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>>> it = this.classMethodCFGMappings.entrySet().iterator();
		while(it.hasNext()){
			Entry<ClassOrInterfaceDeclaration, HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>> classTuple = it.next();
			//Debug.println("------>print out control flow graph "+classTuple.getKey().toString()+" <------");
			
			Iterator<Entry<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>> methodIt = classTuple.getValue().entrySet().iterator();
			while(methodIt.hasNext()){
				Entry<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>> methodCfgTuple = methodIt.next();
				System.out.println("print out cfg for method " + methodCfgTuple.getKey().getName() + "-" + 
						methodCfgTuple.getKey().getBeginLine());
				methodCfgTuple.getValue().printOut();
			}
			
		}
	}
}

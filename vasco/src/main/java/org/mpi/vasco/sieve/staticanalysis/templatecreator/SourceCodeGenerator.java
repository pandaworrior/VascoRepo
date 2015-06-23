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
package org.mpi.vasco.sieve.staticanalysis.templatecreator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

import org.mpi.vasco.util.annotationparser.SchemaParser;
import org.mpi.vasco.util.commonfunc.FileOperations;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.READONLY_Table;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DataField;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class SourceCodeGenerator.
 */
public abstract class SourceCodeGenerator {
		
	/** The code path. */
	private String codePath = "";
		
	/** The Constant codeName. */
	public final static String codeName = "transformedcode";
	
	/** The project name. */
	private String projectName;
	
	/** The class name. */
	private String className;
	
	/** The ast parser. */
	private ASTParser astParser;
	
	/** The cp unit. */
	private CompilationUnit cpUnit;
	
	/** The ast node. */
	private AST astNode;
	
	/** The document. */
	private Document document;
	
	/** The file. */
	private File file;
	
	private List<String> fileContents;
		
	/**
	 * Instantiates a new source code generator.
	 *
	 * @param projectN the project n
	 * @param classN the class n
	 */
	public SourceCodeGenerator(String projectN, String classN){
		this.setBasicInfo(projectN);
		this.className = classN;
		try {
			this.createFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.setASTParser();
	}
	
	/**
	 * Sets the basic info.
	 *
	 * @param projectN the new basic info
	 */
	public void setBasicInfo(String projectN){
		setProjectName(projectN);
		setCodePath();
	}

	/**
	 * Gets the project name.
	 *
	 * @return the project name
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * Sets the project name.
	 *
	 * @param projectN the new project name
	 */
	public void setProjectName(String projectN) {
		projectName = projectN;
	}
		
	/**
	 * Gets the code path.
	 *
	 * @return the code path
	 */
	public String getCodePath(){
		return codePath;
	}
	
	/**
	 * Sets the code path.
	 */
	public void setCodePath(){
		//get current path and generate a new one
		codePath = System.getProperty("user.dir");
		Debug.println("out fir " + this.getProjectName());
		codePath += "/" + codeName + this.getProjectName();
		createCodeDir();
	}
	
	/**
	 * Gets the class name.
	 *
	 * @return the class name
	 */
	public String getClassName(){
		return this.className;
	}
	
	/**
	 * Creates the file.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void createFile() throws IOException{
		this.file = new File(this.getCodePath() + "/" + className +".java");
		if(this.file.exists()){
			Debug.println("file already existed");
			this.file.delete();
			this.file = new File(this.getCodePath() + "/" + className +".java"); 
		}else{
			Debug.println("file doesn't exist");
		}
		boolean result = this.file.createNewFile();
		if(result){
			Debug.println("file already created");
		}
		this.document = new Document("");
	}
	
	/**
	 * Sets the ast parser.
	 */
	private void setASTParser(){
		this.astParser = ASTParser.newParser(AST.JLS3);
		this.astParser.setSource(this.document.get().toCharArray());
		this.cpUnit = (CompilationUnit) this.astParser.createAST(null);
		this.cpUnit.recordModifications();
		this.astNode = this.cpUnit.getAST();
	}
	
	/**
	 * Gets the aST node.
	 *
	 * @return the aST node
	 */
	public AST getASTNode(){
		return this.astNode;
	}
	
	/**
	 * Gets the compilation unit.
	 *
	 * @return the compilation unit
	 */
	public CompilationUnit getCompilationUnit(){
		return this.cpUnit;
	}
	
	/**
	 * Write to file.
	 */
	public void writeToFile(){
		TextEdit edits = this.cpUnit.rewrite(this.document, null);
		try {
			UndoEdit undo = edits.apply(this.document);
		} catch (MalformedTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedWriter javaFile = null;
		try {
			javaFile = new BufferedWriter(new FileWriter(this.file));
			javaFile.write(this.document.get());
			javaFile.flush();
			javaFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.shiftBodyOfMethodToNewLine();
	}
	
	public void reloadFileToFileContents() {
		BufferedReader reader = null;
		if(this.fileContents == null) {
			this.fileContents = new ArrayList<String>();
		}else {
			if(this.fileContents.size() > 0) {
				this.fileContents.clear();
			}
		}
		try {
			reader = new BufferedReader(new FileReader(this.getFullFilePath()));
			String tmp;
	        while ((tmp = reader.readLine()) != null)
	        	this.fileContents.add(tmp);
	        reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeToFileFromFileContents() {
		BufferedWriter javaFile = null;
		try {
			javaFile = new BufferedWriter(new FileWriter(this.file));
			for(String line : this.getFileContents()) {
				javaFile.write(line + "\n");
			}
			javaFile.flush();
			javaFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the class.
	 *
	 * @return the type declaration
	 */
	public TypeDeclaration createClass(){
		TypeDeclaration type = this.astNode.newTypeDeclaration();
		type.setInterface(false);
		type.modifiers().add(this.astNode.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		type.setName(this.astNode.newSimpleName(this.className));
		return type;
	}
	
	/**
	 * Adds the type declaration.
	 *
	 * @param type the type
	 */
	public void addTypeDeclaration(TypeDeclaration type){
		this.cpUnit.types().add(type);
	}
	
	/**
	 * Creates the code dir.
	 */
	public void createCodeDir(){
		File codeDir = new File(this.getCodePath());
		if(!codeDir.exists()){
			boolean result = codeDir.mkdir();
			if(result){
				Debug.println("code path created");
			}else{
				throw new RuntimeException("code path not created");
			}
		}
	}
	
	/**
	 * Creates the variable declaration statement.
	 *
	 * @param typeName the type name
	 * @param varName the var name
	 * @param isArray the is array
	 * @param initializer the initializer
	 * @return the variable declaration statement
	 */
	public VariableDeclarationStatement createVariableDeclarationStatement(PrimitiveType.Code typeName, String varName, boolean isArray, Expression initializer){
		VariableDeclarationFragment varF = this.astNode.newVariableDeclarationFragment();
		varF.setName(this.astNode.newSimpleName(varName));
		VariableDeclarationStatement varDeclStmt = this.astNode.newVariableDeclarationStatement(varF);
		if(isArray){
			varDeclStmt.setType(this.astNode.newArrayType(this.astNode.newPrimitiveType(typeName)));
		}else{
			varDeclStmt.setType(this.astNode.newPrimitiveType(typeName));
		}
		if(initializer != null){
			varF.setInitializer(initializer);
		}
		return varDeclStmt;
	}
	
	/**
	 * Creates the variable declaration statement.
	 *
	 * @param typeName the type name
	 * @param varName the var name
	 * @param isArray the is array
	 * @param initializer the initializer
	 * @return the variable declaration statement
	 */
	public VariableDeclarationStatement createVariableDeclarationStatement(String typeName, String varName, boolean isArray, Expression initializer){
		VariableDeclarationFragment varF = this.astNode.newVariableDeclarationFragment();
		varF.setName(this.astNode.newSimpleName(varName));
		VariableDeclarationStatement varDeclStmt = this.astNode.newVariableDeclarationStatement(varF);
		if(isArray){
			varDeclStmt.setType(this.astNode.newArrayType(this.astNode.newSimpleType(this.astNode.newSimpleName(typeName))));
		}else{
			varDeclStmt.setType(this.astNode.newSimpleType(this.astNode.newSimpleName(typeName)));
		}
		if(initializer != null){
			varF.setInitializer(initializer);
		}
		return varDeclStmt;
	}
	
	
	/**
	 * Creates the field declaration.
	 *
	 * @param typeName the type name
	 * @param varName the var name
	 * @param modifier the modifier
	 * @param isArray the is array
	 * @return the field declaration
	 */
	public FieldDeclaration createFieldDeclaration(PrimitiveType.Code typeName, String varName, ModifierKeyword modifier, boolean isArray){
		VariableDeclarationFragment varF = this.astNode.newVariableDeclarationFragment();
		varF.setName(this.astNode.newSimpleName(varName));
		FieldDeclaration fieldDeclaration = this.astNode.newFieldDeclaration(varF);
		fieldDeclaration.modifiers().add(this.astNode.newModifier(modifier));		
		if(isArray){
			fieldDeclaration.setType(this.astNode.newArrayType(this.astNode.newPrimitiveType(typeName)));
		}else{
			fieldDeclaration.setType(this.astNode.newPrimitiveType(typeName));
		}
		return fieldDeclaration;
	}
	
	/**
	 * Creates the field declaration.
	 *
	 * @param typeName the type name
	 * @param varName the var name
	 * @param modifier the modifier
	 * @param isArray the is array
	 * @return the field declaration
	 */
	public FieldDeclaration createFieldDeclaration(String typeName, String varName, ModifierKeyword modifier, boolean isArray){
		VariableDeclarationFragment varF = this.astNode.newVariableDeclarationFragment();
		varF.setName(this.astNode.newSimpleName(varName));
		FieldDeclaration fieldDeclaration = this.astNode.newFieldDeclaration(varF);
		fieldDeclaration.modifiers().add(this.astNode.newModifier(modifier));		
		if(isArray){
			fieldDeclaration.setType(this.astNode.newArrayType(this.astNode.newSimpleType(this.astNode.newSimpleName(typeName))));
		}else{
			fieldDeclaration.setType(this.astNode.newSimpleType(this.astNode.newSimpleName(typeName)));
		}
		return fieldDeclaration;
	}
	
	/**
	 * Creates the variable declaration.
	 *
	 * @param typeName the type name
	 * @param varName the var name
	 * @param isArray the is array
	 * @return the single variable declaration
	 */
	public SingleVariableDeclaration createVariableDeclaration(PrimitiveType.Code typeName, String varName, boolean isArray){
		SingleVariableDeclaration variableDeclaration = this.astNode.newSingleVariableDeclaration();
		if(isArray){
			variableDeclaration.setType(this.astNode.newArrayType(this.astNode.newPrimitiveType((typeName))));
		}else{
			variableDeclaration.setType(this.astNode.newPrimitiveType((typeName)));
		}
		variableDeclaration.setName(this.astNode.newSimpleName(varName));
		return variableDeclaration;
	}
	
	/**
	 * Creates the variable declaration.
	 *
	 * @param typeName the type name
	 * @param varName the var name
	 * @param isArray the is array
	 * @return the single variable declaration
	 */
	public SingleVariableDeclaration createVariableDeclaration(String typeName, String varName, boolean isArray){
		SingleVariableDeclaration variableDeclaration = this.astNode.newSingleVariableDeclaration();
		if(isArray){
			variableDeclaration.setType(this.astNode.newArrayType(this.astNode.newSimpleType(this.astNode.newSimpleName(typeName))));
		}else{
			variableDeclaration.setType(this.astNode.newSimpleType(this.astNode.newSimpleName(typeName)));
		}
		variableDeclaration.setName(this.astNode.newSimpleName(varName));
		return variableDeclaration;
	}
	
	/**
	 * Creates the variable declaration.
	 *
	 * @param typeName the type name
	 * @param varName the var name
	 * @param modifier the modifier
	 * @param isArray the is array
	 * @return the single variable declaration
	 */
	public SingleVariableDeclaration createVariableDeclaration(String typeName, String varName, ModifierKeyword modifier, boolean isArray){
		SingleVariableDeclaration variableDeclaration = this.astNode.newSingleVariableDeclaration();
		variableDeclaration.modifiers().add(this.astNode.newModifier(modifier));
		if(isArray){
			variableDeclaration.setType(this.astNode.newArrayType(this.astNode.newSimpleType(this.astNode.newSimpleName(typeName))));
		}else{
			variableDeclaration.setType(this.astNode.newSimpleType(this.astNode.newSimpleName(typeName)));
		}
		variableDeclaration.setName(this.astNode.newSimpleName(varName));
		return variableDeclaration;
	}
	
	/**
	 * Creates the method declaration.
	 *
	 * @param returnType the return type
	 * @param methodName the method name
	 * @param modifier the modifier
	 * @return the method declaration
	 */
	public MethodDeclaration createMethodDeclaration(PrimitiveType.Code returnType, String methodName, ModifierKeyword modifier){
		MethodDeclaration methodDeclaration = this.astNode.newMethodDeclaration();
		methodDeclaration.setConstructor(false);
		methodDeclaration.modifiers().add(this.astNode.newModifier(modifier));
		methodDeclaration.setName(this.astNode.newSimpleName(methodName));
		methodDeclaration.setReturnType2(this.astNode.newPrimitiveType(returnType));
		return methodDeclaration;
	}
	
	/**
	 * Creates the method declaration.
	 *
	 * @param returnType the return type
	 * @param methodName the method name
	 * @param modifier the modifier
	 * @return the method declaration
	 */
	public MethodDeclaration createMethodDeclaration(String returnType, String methodName, ModifierKeyword modifier){
		MethodDeclaration methodDeclaration = this.astNode.newMethodDeclaration();
		methodDeclaration.setConstructor(false);
		methodDeclaration.modifiers().add(this.astNode.newModifier(modifier));
		methodDeclaration.setName(this.astNode.newSimpleName(methodName));
		methodDeclaration.setReturnType2(this.astNode.newSimpleType(this.astNode.newSimpleName(returnType)));
		return methodDeclaration;
	}
	
	/**
	 * Creates the method declaration.
	 *
	 * @param returnType the return type
	 * @param methodName the method name
	 * @param modifiers the modifiers
	 * @return the method declaration
	 */
	public MethodDeclaration createMethodDeclaration(PrimitiveType.Code returnType, String methodName, List<ModifierKeyword> modifiers){
		MethodDeclaration methodDeclaration = this.astNode.newMethodDeclaration();
		methodDeclaration.setConstructor(false);
		for(ModifierKeyword modifier : modifiers){
			methodDeclaration.modifiers().add(this.astNode.newModifier(modifier));
		}
		methodDeclaration.setName(this.astNode.newSimpleName(methodName));
		methodDeclaration.setReturnType2(this.astNode.newPrimitiveType(returnType));
		return methodDeclaration;
	}
	
	/**
	 * Creates the method declaration.
	 *
	 * @param returnType the return type
	 * @param methodName the method name
	 * @param modifiers the modifiers
	 * @return the method declaration
	 */
	public MethodDeclaration createMethodDeclaration(String returnType, String methodName, List<ModifierKeyword> modifiers){
		MethodDeclaration methodDeclaration = this.astNode.newMethodDeclaration();
		methodDeclaration.setConstructor(false);
		for(ModifierKeyword modifier : modifiers){
			methodDeclaration.modifiers().add(this.astNode.newModifier(modifier));
		}
		methodDeclaration.setName(this.astNode.newSimpleName(methodName));
		methodDeclaration.setReturnType2(this.astNode.newSimpleType(this.astNode.newSimpleName(returnType)));
		return methodDeclaration;
	}
	
	/**
	 * Creates the constructor declaration.
	 *
	 * @return the method declaration
	 */
	public abstract MethodDeclaration createConstructorDeclaration();
	
	/**
	 * Creates the fields.
	 *
	 * @return the list
	 */
	public abstract List<FieldDeclaration> createFields();
	
	/**
	 * Creates the functions.
	 *
	 * @return the list
	 */
	public abstract List<MethodDeclaration> createFunctions();
	
	
	/**
	 * Generate code.
	 */
	public abstract void generateCode();
	
	public String getFullFilePath() {
		return this.getCodePath() + "/" + className +".java";
	}
	
	public void shiftBodyOfMethodToNewLine() {
		List<String> commands =new ArrayList<String>();
		commands.add("sed -i \'s/)\\s{/)\\n\\t{/g\'" +" " + this.getFullFilePath());
		commands.add("sed -i 's/while/while\\n\\t\\t/g'" + " " + this.getFullFilePath());
		String shellScript = FileOperations.createShellScript(this.getCodePath(), "shiftBody", commands);
		FileOperations.executeShellCommands(shellScript);
	}

	/**
	 * @return the fileContents
	 */
	public List<String> getFileContents() {
		return fileContents;
	}

	/**
	 * @param fileContents the fileContents to set
	 */
	public void setFileContents(List<String> fileContents) {
		this.fileContents = fileContents;
	}
	
	public int findMethodOffset(String methodName) {
		for(int i = 0; i < this.fileContents.size(); i++) {
			String line = this.fileContents.get(i);
			if(line.contains("public") && line.contains(methodName) &&
					(!line.contains("class") &&
							(!line.contains("encap specvar"))))
				return i;
		}
		throw new RuntimeException("How can be possible that you cannot find your method!" + methodName);
	}
	
	public int findMethodBodyOffset(String methodName) {
		int methodOffset = this.findMethodOffset(methodName);
		return this.findMethodBodyOffset(methodOffset);
	}
	
	private int findMethodBodyOffset(int offset) {
		for(int i = offset + 1; i < this.fileContents.size() ; i++) {
			String line = this.fileContents.get(i);
			if(line.contains("{")) {
				return i;
			}
		}
		throw new RuntimeException("How can be possible that you cannot find your method body!" + offset);
	}
	
	private int findLoopOffset(int offset) {
		for(int i = offset + 1; i < this.fileContents.size() ; i++) {
			String line = this.fileContents.get(i);
			if(line.contains("while")) {
				return i;
			}
		}
		throw new RuntimeException("How can be possible that you cannot find your while loop!" + offset);
	}
	
	public int findLoopBodyOffset(String methodName) {
		int index = findMethodBodyOffset(methodName);
		return findLoopOffset(index);
	}
	
	/**
	 * Merge multiple specifications.
	 *
	 * @param strList the str list
	 * @return the string
	 */
	public String mergeMultipleSpecifications(List<String> strList) {
		String _str = "\t/*:";
		for(String str : strList) {
			_str += str +"\n\t";
		}
		_str += "*/";
		return _str;
	}
	
	public String addJahobSpecsPrefixAndSubfix(String _str) {
		return "\t/*: " + _str +  "*/";
	}
	
	public void appendStringListToOffset(int offset, List<String> strList) {
		for(String _str : strList) {
			this.getFileContents().add(offset, _str);
			offset = offset+1;
		}
	}
	
	public void appendStringListToOffset(int offset, String _str) {
		this.getFileContents().add(offset, _str);
	}
	
	public abstract String getJahobCommand();
	
	public abstract void generateAndExecuteJahobCommand();
}

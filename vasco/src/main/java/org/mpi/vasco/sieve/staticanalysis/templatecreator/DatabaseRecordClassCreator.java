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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;

import org.mpi.vasco.util.commonfunc.FileOperations;
import org.mpi.vasco.util.commonfunc.StringOperations;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.CrdtFactory;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DataField;

// TODO: Auto-generated Javadoc
/**
 * The Class DatabaseRecordClassCreator.
 */
public class DatabaseRecordClassCreator extends SourceCodeGenerator{
	
	/** The field list. */
	List<DataField> fieldList;
	
	/** The Constant RECORDSTRING. */
	public final static String RECORDSTRING = "Record";
	
	/** The field specs. */
	List<String> fieldSpecs;
	
	/** The method specs. 
	 * mapping from method name to frame conditions
	 * */
	HashMap<String, List<String>> methodSpecs;
	
	/**
	 * Instantiates a new database record class creator.
	 *
	 * @param projectName the project name
	 * @param tableN the table name
	 * @param fL the field list
	 */
	public DatabaseRecordClassCreator(String projectName, String tableN, List<DataField> fL){
		super(projectName, tableN+RECORDSTRING);
		this.fieldList = fL;
		this.fieldSpecs = new ArrayList<String>();
		this.methodSpecs = new HashMap<String, List<String>>();
	}
	
	/**
	 * Creates the data field.
	 *
	 * @param df the df
	 * @return the field declaration
	 */
	private FieldDeclaration createDataField(DataField df){
		String crdtImplTypeString = CrdtFactory.getProperCrdtObject(df.get_Crdt_Data_Type(), df.get_Data_Type());
		FieldDeclaration varDecl = super.createFieldDeclaration(crdtImplTypeString, 
				df.get_Data_Field_Name_Escape_Jahob(), ModifierKeyword.PRIVATE_KEYWORD, false);
		//add the field specs
		this.fieldSpecs.add(JahobSpecsUtil.generateFieldSpecification(this.getClassName(), df.get_Data_Field_Name()));
		return varDecl;
	}
	
	/**
	 * Creates the update field function, the field is not lww.
	 *
	 * @param df the df
	 * @return the method declaration
	 */
	private MethodDeclaration createUpdateFieldFunction(DataField df){
		if(CrdtFactory.isNormalDataType(df.get_Crdt_Data_Type())){
			return null;
		}
		String requireSpecs = JahobSpecsUtil.requirePrefix;
		String modifySpecs = JahobSpecsUtil.modifyPrefix;
		String ensureSpecs = JahobSpecsUtil.ensurePrefix;
		String methodName = "update"+df.get_Data_Field_Name();
		MethodDeclaration methodDeclaration = super.createMethodDeclaration(PrimitiveType.VOID,
				methodName, ModifierKeyword.PUBLIC_KEYWORD);
		//set parameter
		String crdtFieldType = CrdtFactory.getProperCrdtObject(df.get_Crdt_Data_Type(), df.get_Data_Type());
		String argName = "arg0";
		SingleVariableDeclaration varDecl = super.createVariableDeclaration(crdtFieldType, argName, false);
		methodDeclaration.parameters().add(varDecl);
		
		org.eclipse.jdt.core.dom.Block block = super.getASTNode().newBlock();
		MethodInvocation methodInvocation = super.getASTNode().newMethodInvocation();
		
		//get this expression
		FieldAccess fieldA = super.getASTNode().newFieldAccess();
		fieldA.setExpression(super.getASTNode().newThisExpression());
		fieldA.setName(super.getASTNode().newSimpleName(df.get_Data_Field_Name_Escape_Jahob()));
		
		methodInvocation.setExpression(fieldA);
		methodInvocation.setName(super.getASTNode().newSimpleName("update")); 
		
		MethodInvocation innerMethodInvocation = super.getASTNode().newMethodInvocation();
		innerMethodInvocation.setExpression(super.getASTNode().newSimpleName(argName));
		innerMethodInvocation.setName(super.getASTNode().newSimpleName("getDelta"));
		
		methodInvocation.arguments().add(innerMethodInvocation);
		ExpressionStatement expressionStatement = super.getASTNode().newExpressionStatement(methodInvocation);
		block.statements().add(expressionStatement);
		
		List<String> specs = new ArrayList<String>();
		requireSpecs += JahobSpecsUtil.getRequireNotNullClause(argName);
		requireSpecs += " & " + JahobSpecsUtil.getRequireNotNullClause(
				JahobSpecsUtil.getJahobRecordField(this.getClassName(), df.get_Data_Field_Name()))+"\"";
		specs.add(requireSpecs);
		modifySpecs += JahobSpecsUtil.getModifyField(df.get_Crdt_Data_Type());
		specs.add(modifySpecs);
		ensureSpecs += JahobSpecsUtil.getEnsureClauseForNumberDelta(this.getClassName(), df, argName) +"\"";
		specs.add(ensureSpecs);
		this.methodSpecs.put(methodName, specs);
		methodDeclaration.setBody(block);
		return methodDeclaration;
	}
	
	/**
	 * Creates the update field function, the field is lww.
	 *
	 * @param df the df
	 * @param lwwLogicTsDf the lww logic ts df
	 * @return the method declaration
	 */
	private MethodDeclaration createUpdateFieldFunction(DataField df, DataField lwwLogicTsDf){
		String methodName = "update"+df.get_Data_Field_Name();
		MethodDeclaration methodDeclaration = super.createMethodDeclaration(PrimitiveType.VOID,
				methodName, ModifierKeyword.PUBLIC_KEYWORD);
		//set parameter
		String fieldArgName = "arg0";
		String lwwtsArgName = "arg1";
		String crdtFieldType = CrdtFactory.getProperCrdtObject(df.get_Crdt_Data_Type(), df.get_Data_Type());
		if(!CrdtFactory.isLwwDeletedFlag(df.get_Crdt_Data_Type())) {
			SingleVariableDeclaration varDecl = super.createVariableDeclaration(crdtFieldType, fieldArgName, false);
			methodDeclaration.parameters().add(varDecl);
		}
		//if lww then put lww timestamp
		String lwwLogicalTsCrdtFieldType = CrdtFactory.getProperCrdtObject(lwwLogicTsDf.get_Crdt_Data_Type(), lwwLogicTsDf.get_Data_Type());
		SingleVariableDeclaration lwwLogicalTsVarDecl = super.createVariableDeclaration(lwwLogicalTsCrdtFieldType, 
				lwwtsArgName, false);
		methodDeclaration.parameters().add(lwwLogicalTsVarDecl);

		org.eclipse.jdt.core.dom.Block block = super.getASTNode().newBlock();
		MethodInvocation methodInvocation = super.getASTNode().newMethodInvocation();
		
		//get this expression
		FieldAccess fieldA = super.getASTNode().newFieldAccess();
		fieldA.setExpression(super.getASTNode().newThisExpression());
		fieldA.setName(super.getASTNode().newSimpleName(df.get_Data_Field_Name_Escape_Jahob()));
		
		methodInvocation.setExpression(fieldA);
		methodInvocation.setName(super.getASTNode().newSimpleName("update")); 
		
		if(CrdtFactory.isLwwDeletedFlag(df.get_Crdt_Data_Type())) {
			NumberLiteral numLit = super.getASTNode().newNumberLiteral("1");
			methodInvocation.arguments().add(numLit);
		}else {
			MethodInvocation innerMethodInvocation = super.getASTNode().newMethodInvocation();
			innerMethodInvocation.setExpression(super.getASTNode().newSimpleName(fieldArgName));
			innerMethodInvocation.setName(super.getASTNode().newSimpleName("getValue"));
			methodInvocation.arguments().add(innerMethodInvocation);
		}
		methodInvocation.arguments().add(super.getASTNode().newSimpleName(lwwtsArgName));
		ExpressionStatement expressionStatement = super.getASTNode().newExpressionStatement(methodInvocation);
		block.statements().add(expressionStatement);
		methodDeclaration.setBody(block);
		
		String requireSpecs = JahobSpecsUtil.requirePrefix;
		String modifySpecs = JahobSpecsUtil.modifyPrefix;
		String ensureSpecs = JahobSpecsUtil.ensurePrefix;
		List<String> specs = new ArrayList<String>();
		if(CrdtFactory.isLwwDeletedFlag(df.get_Crdt_Data_Type())) {
			requireSpecs +=  JahobSpecsUtil.getRequireNotNullClause(
					JahobSpecsUtil.getJahobRecordField(this.getClassName(), df.get_Data_Field_Name()));
			requireSpecs += "&" + JahobSpecsUtil.getRequireNotNullClause(
					JahobSpecsUtil.getJahobRecordFieldLts(this.getClassName(), df));
			requireSpecs += " & " + JahobSpecsUtil.getRequireNotNullClause(lwwtsArgName)+"\"";
			
			ensureSpecs += JahobSpecsUtil.getEnsureClauseForLwwDeletedFlag(this.getClassName(), 
					df, lwwLogicTsDf, lwwtsArgName, "1") +"\"";
		}else {
			requireSpecs += JahobSpecsUtil.getRequireNotNullClause(fieldArgName);
			requireSpecs += " & " + JahobSpecsUtil.getRequireNotNullClause(
					JahobSpecsUtil.getJahobRecordField(this.getClassName(), df.get_Data_Field_Name()));
			requireSpecs += "& " +JahobSpecsUtil.getRequireNotNullClause(
					JahobSpecsUtil.getJahobRecordFieldLts(this.getClassName(), df));
			requireSpecs += " & " + JahobSpecsUtil.getRequireNotNullClause(lwwtsArgName)+"\"";
			
			ensureSpecs += JahobSpecsUtil.getEnsureClauseForLww(this.getClassName(), 
					df, fieldArgName, lwwLogicTsDf, lwwtsArgName) +"\"";
		}
		
		modifySpecs += JahobSpecsUtil.getModifyField(df.get_Crdt_Data_Type());
		specs.add(requireSpecs);
		specs.add(modifySpecs);
		specs.add(ensureSpecs);
		this.methodSpecs.put(methodName, specs);
		return methodDeclaration;
	}
	
	private MethodDeclaration createUndeleteFieldFunction(DataField df, DataField lwwLogicTsDf){
		String methodName = "undelete"+df.get_Data_Field_Name();
		MethodDeclaration methodDeclaration = super.createMethodDeclaration(PrimitiveType.VOID,
				methodName, ModifierKeyword.PUBLIC_KEYWORD);
		//set parameter
		String lwwtsArgName = "arg0";
		//if lww then put lww timestamp
		String lwwLogicalTsCrdtFieldType = CrdtFactory.getProperCrdtObject(lwwLogicTsDf.get_Crdt_Data_Type(), lwwLogicTsDf.get_Data_Type());
		SingleVariableDeclaration lwwLogicalTsVarDecl = super.createVariableDeclaration(lwwLogicalTsCrdtFieldType, 
				lwwtsArgName, false);
		methodDeclaration.parameters().add(lwwLogicalTsVarDecl);

		org.eclipse.jdt.core.dom.Block block = super.getASTNode().newBlock();
		MethodInvocation methodInvocation = super.getASTNode().newMethodInvocation();
		
		//get this expression
		FieldAccess fieldA = super.getASTNode().newFieldAccess();
		fieldA.setExpression(super.getASTNode().newThisExpression());
		fieldA.setName(super.getASTNode().newSimpleName(df.get_Data_Field_Name_Escape_Jahob()));
		
		methodInvocation.setExpression(fieldA);
		methodInvocation.setName(super.getASTNode().newSimpleName("update")); 
		
	
		NumberLiteral numLit = super.getASTNode().newNumberLiteral("0");
		methodInvocation.arguments().add(numLit);
		methodInvocation.arguments().add(super.getASTNode().newSimpleName(lwwtsArgName));
		ExpressionStatement expressionStatement = super.getASTNode().newExpressionStatement(methodInvocation);
		block.statements().add(expressionStatement);
		methodDeclaration.setBody(block);
		
		String requireSpecs = JahobSpecsUtil.requirePrefix;
		String modifySpecs = JahobSpecsUtil.modifyPrefix;
		String ensureSpecs = JahobSpecsUtil.ensurePrefix;
		List<String> specs = new ArrayList<String>();
		requireSpecs +=  JahobSpecsUtil.getRequireNotNullClause(
					JahobSpecsUtil.getJahobRecordField(this.getClassName(), df.get_Data_Field_Name()));
		requireSpecs += "&" + JahobSpecsUtil.getRequireNotNullClause(
					JahobSpecsUtil.getJahobRecordFieldLts(this.getClassName(), df));
		requireSpecs += " & " + JahobSpecsUtil.getRequireNotNullClause(lwwtsArgName)+"\"";
			
		ensureSpecs += JahobSpecsUtil.getEnsureClauseForLwwDeletedFlag(this.getClassName(), 
					df, lwwLogicTsDf, lwwtsArgName, "0") +"\"";
		
		modifySpecs += JahobSpecsUtil.getModifyField(df.get_Crdt_Data_Type());
		specs.add(requireSpecs);
		specs.add(modifySpecs);
		specs.add(ensureSpecs);
		this.methodSpecs.put(methodName, specs);
		return methodDeclaration;
	}
	
	/**
	 * Creates the update lww logical timestamp function.
	 *
	 * @param df the df
	 * @return the method declaration
	 */
	public MethodDeclaration createUpdateLwwLogicalTimestampFunction(DataField df){
		String methodName = "update"+df.get_Data_Field_Name();
		MethodDeclaration methodDeclaration = super.createMethodDeclaration(PrimitiveType.VOID,
				methodName, ModifierKeyword.PUBLIC_KEYWORD);
		String argName = "arg0";
		//set parameter
		String crdtFieldType = CrdtFactory.getProperCrdtObject(df.get_Crdt_Data_Type(), df.get_Data_Type());
		SingleVariableDeclaration varDecl = super.createVariableDeclaration(crdtFieldType, argName, false);
		methodDeclaration.parameters().add(varDecl);
		org.eclipse.jdt.core.dom.Block block = super.getASTNode().newBlock();
		MethodInvocation methodInvocation = super.getASTNode().newMethodInvocation();
		
		//get this expression
		FieldAccess fieldA = super.getASTNode().newFieldAccess();
		fieldA.setExpression(super.getASTNode().newThisExpression());
		fieldA.setName(super.getASTNode().newSimpleName(df.get_Data_Field_Name()));
		
		methodInvocation.setExpression(fieldA);
		methodInvocation.setName(super.getASTNode().newSimpleName("updateComp")); 
		
		//get value of the lww logicaltimestamp
		MethodInvocation innerMethodInvocation = super.getASTNode().newMethodInvocation();
		innerMethodInvocation.setExpression(super.getASTNode().newSimpleName(argName));
		innerMethodInvocation.setName(super.getASTNode().newSimpleName("getValue")); 
		
		methodInvocation.arguments().add(innerMethodInvocation);
		ExpressionStatement expressionStatement = super.getASTNode().newExpressionStatement(methodInvocation);
		block.statements().add(expressionStatement);
		methodDeclaration.setBody(block);
		
		String requireSpecs = JahobSpecsUtil.requirePrefix;
		String modifySpecs = JahobSpecsUtil.modifyPrefix;
		String ensureSpecs = JahobSpecsUtil.ensurePrefix;
		List<String> specs = new ArrayList<String>();
		requireSpecs += JahobSpecsUtil.getRequireNotNullClause(argName) ;
		requireSpecs += " & " + JahobSpecsUtil.getRequireNotNullClause(
				JahobSpecsUtil.getJahobRecordField(this.getClassName(), df.get_Data_Field_Name()))+"\"";
		specs.add(requireSpecs);
		modifySpecs += JahobSpecsUtil.getModifyField(df.get_Crdt_Data_Type());
		specs.add(modifySpecs);
		ensureSpecs += JahobSpecsUtil.getEnsureClauseForLwwLogicalTimestamp(this.getClassName(), df, argName) +"\"";
		specs.add(ensureSpecs);
		this.methodSpecs.put(methodName, specs);
		return methodDeclaration;
	}
	
	/**
	 * Creates the update fields function.
	 *
	 * @return the list
	 */
	public List<MethodDeclaration> createUpdateFieldsFunction(){
		List<MethodDeclaration> methodDecls = new ArrayList<MethodDeclaration>();
		DataField lwwLogicalTimestamp = null;
		for(DataField df : this.fieldList){
			if(CrdtFactory.isLwwLogicalTimestamp(df.get_Crdt_Data_Type())){
				lwwLogicalTimestamp = df;
				break;
			}
		}
		
		for(DataField df : this.fieldList){
			MethodDeclaration methodDecl = null;
			if(CrdtFactory.isNormalDataType(df.get_Crdt_Data_Type())){
				continue;
			}else{
				if(CrdtFactory.isLwwLogicalTimestamp(df.get_Crdt_Data_Type())){
					methodDecl = this.createUpdateLwwLogicalTimestampFunction(df);//done
				}else{
					if(CrdtFactory.isLwwType(df.get_Crdt_Data_Type())){
						methodDecl = this.createUpdateFieldFunction(df, lwwLogicalTimestamp);
						if(CrdtFactory.isLwwDeletedFlag(df.get_Crdt_Data_Type())) {
							methodDecls.add(this.createUndeleteFieldFunction(df, lwwLogicalTimestamp));
						}
					}else{
						methodDecl = this.createUpdateFieldFunction(df);
					}
				}
				methodDecls.add(methodDecl);
			}
		}
		return methodDecls;
	}
	
	/**
	 * Creates the get field function.
	 *
	 * @param df the df
	 * @return the method declaration
	 */
	private MethodDeclaration createGetFieldFunction(DataField df){
		String methodName = "get"+df.get_Data_Field_Name();
		String returnType = CrdtFactory.getProperCrdtObject(df.get_Crdt_Data_Type(), df.get_Data_Type());
		MethodDeclaration methodDeclaration = super.createMethodDeclaration(returnType,
				methodName, ModifierKeyword.PUBLIC_KEYWORD);
		String methodSpecs = JahobSpecsUtil.ensurePrefix;
		methodSpecs += " result = " + this.getClassName() + "_" + df.get_Data_Field_Name() +"\"";
		// add the method body here
		org.eclipse.jdt.core.dom.Block block = super.getASTNode().newBlock();
		ReturnStatement returnStmt = super.getASTNode().newReturnStatement();
		FieldAccess fieldA = super.getASTNode().newFieldAccess();
		fieldA.setExpression(super.getASTNode().newThisExpression());
		fieldA.setName(super.getASTNode().newSimpleName(df.get_Data_Field_Name_Escape_Jahob()));
		returnStmt.setExpression(fieldA);
		block.statements().add(returnStmt);
		methodDeclaration.setBody(block);
		List<String> specs = new ArrayList<String>();
		specs.add(methodSpecs);
		this.methodSpecs.put(methodName, specs);
		return methodDeclaration;
	}
	
	/**
	 * Creates the get field functions.
	 *
	 * @return the list
	 */
	public List<MethodDeclaration> createGetFieldFunctions(){
		List<MethodDeclaration> methodDecls = new ArrayList<MethodDeclaration>();
		for(DataField df : this.fieldList){
			methodDecls.add(this.createGetFieldFunction(df));
		}
		return methodDecls;
	}

	/* (non-Javadoc)
	 * @see staticanalysis.templatecreator.DatabaseClassCreator#createConstructorDeclaration()
	 */
	/**
	 * Creates the constructor declaration.
	 *
	 * @return the method declaration
	 * @see staticanalysis.templatecreator.SourceCodeGenerator#createConstructorDeclaration()
	 */
	@Override
	public MethodDeclaration createConstructorDeclaration() {
		MethodDeclaration methodDeclaration = super.createMethodDeclaration(PrimitiveType.VOID,this.getClassName(), ModifierKeyword.PUBLIC_KEYWORD);
		methodDeclaration.setConstructor(true);
		List<String> specs = new ArrayList<String>();
		String modifySpecs = JahobSpecsUtil.modifyPrefix;
		String ensureSpecs = JahobSpecsUtil.ensurePrefix;
		// set parameters
		for(int i = 0; i < this.fieldList.size(); i++){
			DataField df = this.fieldList.get(i);
			String argName = "arg" + i;
			//create a variable and set this as a parameter
			String crdtImplTypeString = CrdtFactory.getProperCrdtObject(df.get_Crdt_Data_Type(), df.get_Data_Type());
			SingleVariableDeclaration varDecl = super.createVariableDeclaration(crdtImplTypeString, argName, false);
			methodDeclaration.parameters().add(varDecl);
			modifySpecs += JahobSpecsUtil.getModifyField(this.getClassName(), df.get_Data_Field_Name()) +",";
			ensureSpecs += JahobSpecsUtil.getEnsureFieldEqual(this.getClassName(), df.get_Data_Field_Name(), argName) +" & ";
		}
		modifySpecs = StringOperations.removeLastComma(modifySpecs);
		ensureSpecs = StringOperations.replaceLastMathAndWithDoubleQuotes(ensureSpecs);
		
		// here please add all parameters assignment
		org.eclipse.jdt.core.dom.Block block = super.getASTNode().newBlock();
		for(int i = 0; i < this.fieldList.size(); i++){
			DataField df = this.fieldList.get(i);
			String argName = "arg" + i;
			// add assignment for the parameter
			Assignment assignExpr = super.getASTNode().newAssignment();
			assignExpr.setOperator(Assignment.Operator.ASSIGN);
			FieldAccess leftOpr = super.getASTNode().newFieldAccess();
			leftOpr.setExpression(super.getASTNode().newThisExpression());
			leftOpr.setName(super.getASTNode().newSimpleName(df.get_Data_Field_Name()));
			assignExpr.setLeftHandSide(leftOpr);
			assignExpr.setRightHandSide(super.getASTNode().newSimpleName(argName));

			Statement assignStmt = super.getASTNode().newExpressionStatement(assignExpr);
			block.statements().add(assignStmt);
		}
		methodDeclaration.setBody(block);
		specs.add(modifySpecs);
		specs.add(ensureSpecs);
		this.methodSpecs.put(this.getClassName(), specs);
		return methodDeclaration;
	}

	/* (non-Javadoc)
	 * @see staticanalysis.templatecreator.DatabaseClassCreator#createFields()
	 */
	/**
	 * Creates the fields.
	 *
	 * @return the list
	 * @see staticanalysis.templatecreator.SourceCodeGenerator#createFields()
	 */
	@Override
	public List<FieldDeclaration> createFields() {
		List<FieldDeclaration> varDecls = new ArrayList<FieldDeclaration>();
		for(DataField df : this.fieldList){
			varDecls.add(this.createDataField(df));
		}
		return varDecls;
	}

	/* (non-Javadoc)
	 * @see staticanalysis.templatecreator.DatabaseClassCreator#createFunctions()
	 */
	/**
	 * Creates the functions.
	 *
	 * @return the list
	 * @see staticanalysis.templatecreator.SourceCodeGenerator#createFunctions()
	 */
	@Override
	public List<MethodDeclaration> createFunctions() {
		List<MethodDeclaration> methodDecls = new ArrayList<MethodDeclaration>();
		methodDecls.addAll(this.createGetFieldFunctions());
		methodDecls.addAll(this.createUpdateFieldsFunction());
		return methodDecls;
	}

	/* (non-Javadoc)
	 * @see staticanalysis.templatecreator.DatabaseClassCreator#generateCode()
	 */
	/**
	 * Generate code.
	 *
	 * @see staticanalysis.templatecreator.SourceCodeGenerator#generateCode()
	 */
	@Override
	public void generateCode() {
		// TODO Auto-generated method stub
		TypeDeclaration type = this.createClass();
		List<FieldDeclaration> varDecls = this.createFields();
		for(FieldDeclaration varDecl : varDecls){
			type.bodyDeclarations().add(varDecl);
		}
		//TODO: add code here
		MethodDeclaration methodDecl = this.createConstructorDeclaration();
		type.bodyDeclarations().add(methodDecl);
		
		List<MethodDeclaration> methodDecls = this.createFunctions();
		for(MethodDeclaration methodMember : methodDecls){
			type.bodyDeclarations().add(methodMember);
		}
		super.addTypeDeclaration(type);
		super.writeToFile();
		super.reloadFileToFileContents();
		this.appendAllSpecs();
		super.writeToFileFromFileContents();
		
		this.generateAndExecuteJahobCommand();
	}
	
	/**
	 * Gets the record sufix string.
	 *
	 * @return the record sufix string
	 */
	public static String getRecordSufixString(){
		return RECORDSTRING;
	}
	
	//for Jahob specifications

	public void appendAllSpecs() {
		//insert your field specs
		int index = super.findMethodOffset(this.getClassName());
		String fieldSpecs = super.mergeMultipleSpecifications(this.fieldSpecs);
		super.appendStringListToOffset(index, fieldSpecs);
		
		//insert all your method specs
		Iterator<Entry<String, List<String>>> it = this.methodSpecs.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, List<String>> entry = it.next();
			String methodName = entry.getKey();
			int insertIndex = super.findMethodBodyOffset(methodName);
			String methodSpecs = super.mergeMultipleSpecifications(entry.getValue());
			super.appendStringListToOffset(insertIndex, methodSpecs);
		}
	}

	/* (non-Javadoc)
	 * @see staticanalysis.templatecreator.SourceCodeGenerator#getJahobCommand()
	 */
	@Override
	public String getJahobCommand() {
		return JahobSpecsUtil.getVerificationExecCommand(this.getClassName(),this.getFullFilePath(), fieldList);
	}

	/* (non-Javadoc)
	 * @see staticanalysis.templatecreator.SourceCodeGenerator#generateJahobCommand()
	 */
	@Override
	public void generateAndExecuteJahobCommand() {
		List<String> commands = new ArrayList<String>();
		commands.add(this.getJahobCommand());
		String shellScript = FileOperations.createShellScript(this.getCodePath(), "run"+this.getClassName(), commands);
		//FileOperations.executeShellCommands(shellScript);
	}
}

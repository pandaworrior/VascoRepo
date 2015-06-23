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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import org.mpi.vasco.util.debug.Debug;

import org.mpi.vasco.util.annotationparser.SchemaParser;
import org.mpi.vasco.util.commonfunc.FileOperations;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.AosetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.ArsetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.AusetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.CrdtFactory;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.LWW_DELETEDFLAG;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.LWW_LOGICALTIMESTAMP;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.UosetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.CrdtDataFieldType;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DataField;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;

// TODO: Auto-generated Javadoc
/**
 * The Class DatabaseTableClassCreator.
 */
public class DatabaseTableClassCreator extends SourceCodeGenerator{
	
	/** The table instance. */
	private DatabaseTable tableInstance;
	
	/** The Constant TABLESTRING. */
	private final static String TABLESTRING = "Table";
	
	/** The Constant ARRAY_FIELD_NAME. */
	private final static String ARRAY_FIELD_NAME = "table";
	
	/** The field specs. */
	List<String> fieldSpecs;
	
	/** The method specs. 
	 * mapping from method name to frame conditions
	 * */
	HashMap<String, List<String>> methodSpecs;
	
	/** The constructor body specs. */
	String constructorBodySpecs ="";
	
	HashMap<String, String> loopInvartiantSpecs;

	/**
	 * Instantiates a new database table class creator.
	 *
	 * @param projectName the project name
	 * @param tableN the table n
	 */
	public DatabaseTableClassCreator(String projectName, String tableN){
		super(projectName, tableN+TABLESTRING);
		this.fieldSpecs = new ArrayList<String>();
		this.methodSpecs = new HashMap<String, List<String>>();
		this.loopInvartiantSpecs = new HashMap<String, String>();
	}
	

	/**
	 * Instantiates a new database table class creator.
	 *
	 * @param projectName the project name
	 * @param tableN the table n
	 * @param tableIns the table ins
	 */
	public DatabaseTableClassCreator(String projectName, String tableN, DatabaseTable tableIns){
		super(projectName, tableN+TABLESTRING);
		this.setTableInstance(tableIns);
		this.fieldSpecs = new ArrayList<String>();
		this.methodSpecs = new HashMap<String, List<String>>();
		this.loopInvartiantSpecs = new HashMap<String, String>();
	}
	
	/**
	 * Sets the table instance.
	 *
	 * @param dt the new table instance
	 */
	public void setTableInstance(DatabaseTable dt){
		this.tableInstance = dt;
	}
	
	/**
	 * Creates the size field.
	 *
	 * @return the field declaration
	 */
	private FieldDeclaration createSizeField(){
		FieldDeclaration varDecl = super.createFieldDeclaration(PrimitiveType.INT, 
				"size", ModifierKeyword.PRIVATE_KEYWORD, false);
		return varDecl;
	}
	
	/**
	 * Creates the record list field.
	 *
	 * @return the field declaration
	 */
	private FieldDeclaration createRecordListField(){
		String recordName = tableInstance.get_Table_Name() + DatabaseRecordClassCreator.RECORDSTRING;
		FieldDeclaration varDecl = super.createFieldDeclaration(recordName, 
				ARRAY_FIELD_NAME, ModifierKeyword.PRIVATE_KEYWORD, true);
		return varDecl;
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
	public List<FieldDeclaration> createFields(){
		List<FieldDeclaration> fieldDecls = new ArrayList<FieldDeclaration>();
		fieldDecls.add(this.createSizeField());
		fieldDecls.add(this.createRecordListField());
		return fieldDecls;
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
		// TODO Auto-generated method stub
		MethodDeclaration methodDeclaration = super.getASTNode().newMethodDeclaration();
		methodDeclaration.setConstructor(true);
		methodDeclaration.modifiers().add(super.getASTNode().newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		methodDeclaration.setName(super.getASTNode().newSimpleName(super.getClassName()));
		methodDeclaration.setReturnType2(super.getASTNode().newPrimitiveType(PrimitiveType.VOID));
		
		// here please add all parameters
		org.eclipse.jdt.core.dom.Block block = super.getASTNode().newBlock();
		methodDeclaration.setBody(block);
		String requireSpecs = JahobSpecsUtil.requirePrefix;
		String modifySpecs = JahobSpecsUtil.modifyPrefix;
		String ensureSpecs = JahobSpecsUtil.ensurePrefix;
		List<String> specs = new ArrayList<String>();
		requireSpecs +=  JahobSpecsUtil.getTableNotInit(this.getClassName()) + "\"";
		modifySpecs += "\"" + JahobSpecsUtil.getTableInit(this.getClassName()) + "\"";
		ensureSpecs +=  JahobSpecsUtil.getTableInit(this.getClassName());
		ensureSpecs += " & " + JahobSpecsUtil.getTableContentsEmpty(this.getClassName());
		ensureSpecs += " & " + JahobSpecsUtil.getTableSizeZero(this.getClassName()) + "\"";
		specs.add(requireSpecs);
		specs.add(modifySpecs);
		specs.add(ensureSpecs);
		this.methodSpecs.put(this.getClassName(), specs);
		constructorBodySpecs = JahobSpecsUtil.getTableInitFacilities(this.getClassName());
		return methodDeclaration;
	}
	
	/**
	 * Creates the get size function.
	 *
	 * @return the method declaration
	 */
	public MethodDeclaration createGetSizeFunction(){
		String methodName = "getSize";
		MethodDeclaration methodDeclaration = super.createMethodDeclaration(PrimitiveType.INT,
				methodName, ModifierKeyword.PUBLIC_KEYWORD);
		org.eclipse.jdt.core.dom.Block block = super.getASTNode().newBlock();
		ReturnStatement returnStmt = super.getASTNode().newReturnStatement();
		FieldAccess fieldA = super.getASTNode().newFieldAccess();
		fieldA.setExpression(super.getASTNode().newThisExpression());
		fieldA.setName(super.getASTNode().newSimpleName("size"));
		returnStmt.setExpression(fieldA);
		block.statements().add(returnStmt);
		methodDeclaration.setBody(block);
		
		String requireSpecs = JahobSpecsUtil.requirePrefix;
		String ensureSpecs = JahobSpecsUtil.ensurePrefix;
		List<String> specs = new ArrayList<String>();
		requireSpecs +=  JahobSpecsUtil.getTableNotInit(this.getClassName()) + "\"";
		ensureSpecs +=  JahobSpecsUtil.getSizeEqualResult(this.getClassName()) + "\"";
		specs.add(requireSpecs);
		specs.add(ensureSpecs);
		this.methodSpecs.put(methodName, specs);
		return methodDeclaration;
	}
	
	/**
	 * Creates the is contained function.
	 *
	 * @return the method declaration
	 */
	public MethodDeclaration createIsContainedFunction(){
		String methodName = "isContained";
		MethodDeclaration methodDeclaration = super.createMethodDeclaration(PrimitiveType.BOOLEAN,
				methodName, ModifierKeyword.PUBLIC_KEYWORD);
		String recordName = tableInstance.get_Table_Name() + DatabaseRecordClassCreator.RECORDSTRING;
		SingleVariableDeclaration varDecl = super.createVariableDeclaration(recordName, recordName.toLowerCase(), false);
		methodDeclaration.parameters().add(varDecl);
		org.eclipse.jdt.core.dom.Block block = super.getASTNode().newBlock();
		//add the index variable
		String indexVar = "i";
		VariableDeclarationStatement varDeclStmt = super.createVariableDeclarationStatement(PrimitiveType.INT, indexVar, false, null);
		block.statements().add(varDeclStmt);
		
		//put a for loop here
		ForStatement forStmt = super.getASTNode().newForStatement();
		//set initializer
		Assignment assignExpr = super.getASTNode().newAssignment();
		assignExpr.setOperator(Assignment.Operator.ASSIGN);
		assignExpr.setLeftHandSide(super.getASTNode().newSimpleName(indexVar));
		NumberLiteral initializedValue = super.getASTNode().newNumberLiteral("0");
		assignExpr.setRightHandSide(initializedValue);
		forStmt.initializers().add(assignExpr);
		
		//set condition expression
		
		InfixExpression infixExpr = super.getASTNode().newInfixExpression();
		infixExpr.setOperator(InfixExpression.Operator.LESS);
		infixExpr.setLeftOperand(super.getASTNode().newSimpleName(indexVar));
		
		FieldAccess fieldA = super.getASTNode().newFieldAccess();
		fieldA.setExpression(super.getASTNode().newThisExpression());
		fieldA.setName(super.getASTNode().newSimpleName("size"));
		infixExpr.setRightOperand(fieldA);
		
		forStmt.setExpression(infixExpr);
		
		//set update
		PostfixExpression updateExpr = super.getASTNode().newPostfixExpression();
		updateExpr.setOperator(PostfixExpression.Operator.INCREMENT);
		updateExpr.setOperand(super.getASTNode().newSimpleName(indexVar));
		
		forStmt.updaters().add(updateExpr);
		
		//set for loop body
		org.eclipse.jdt.core.dom.Block forLoopBlock = super.getASTNode().newBlock();
		
		//add a if statement
		
		IfStatement ifStmt = super.getASTNode().newIfStatement();
		
		//set if condition
		InfixExpression ifCondExp = super.getASTNode().newInfixExpression();
		//left expr is in the form: this.table[i]
		ArrayAccess arrayA = super.getASTNode().newArrayAccess();
		FieldAccess tableFieldA = super.getASTNode().newFieldAccess();
		tableFieldA.setExpression(super.getASTNode().newThisExpression());
		tableFieldA.setName(super.getASTNode().newSimpleName("table"));
		arrayA.setArray(tableFieldA);
		arrayA.setIndex(super.getASTNode().newSimpleName(indexVar));
		ifCondExp.setLeftOperand(arrayA);
		//equal operator
		ifCondExp.setOperator(InfixExpression.Operator.EQUALS);
		//right expr
		ifCondExp.setRightOperand(super.getASTNode().newSimpleName(recordName.toLowerCase()));
		
		ifStmt.setExpression(ifCondExp);
		//add block for ifStmt
		org.eclipse.jdt.core.dom.Block ifBlock = super.getASTNode().newBlock();
		
		ReturnStatement trueReturnStmt = super.getASTNode().newReturnStatement();
		BooleanLiteral trueValue = super.getASTNode().newBooleanLiteral(true);
		trueReturnStmt.setExpression(trueValue);
		
		ifBlock.statements().add(trueReturnStmt);
		ifStmt.setThenStatement(ifBlock);
		forLoopBlock.statements().add(ifStmt);
		forStmt.setBody(forLoopBlock);
		
		block.statements().add(forStmt);
		
		//return false
		ReturnStatement falseReturnStmt = super.getASTNode().newReturnStatement();
		BooleanLiteral falseValue = super.getASTNode().newBooleanLiteral(false);
		falseReturnStmt.setExpression(falseValue);
		block.statements().add(falseReturnStmt);
		
		methodDeclaration.setBody(block);
		return methodDeclaration;
	}
	
	/**
	 * Creates the unique insert function.
	 *
	 * @return the method declaration
	 */
	public MethodDeclaration createUniqueInsertFunction(){
		String methodName = "uniqueInsert";
		MethodDeclaration methodDeclaration = super.createMethodDeclaration(PrimitiveType.VOID,
				methodName, ModifierKeyword.PUBLIC_KEYWORD);
		String recordTypeName = tableInstance.get_Table_Name() + DatabaseRecordClassCreator.RECORDSTRING;
		String insertRecordArg = "arg0";
		SingleVariableDeclaration varDecl = super.createVariableDeclaration(recordTypeName, insertRecordArg, false);
		methodDeclaration.parameters().add(varDecl);
		
		org.eclipse.jdt.core.dom.Block block = super.getASTNode().newBlock();
		//add block here
		//new array Type
		ArrayCreation arrayC = super.getASTNode().newArrayCreation();
		arrayC.setType(super.getASTNode().newArrayType(
				super.getASTNode().newSimpleType(
						super.getASTNode().newSimpleName(recordTypeName))));
		InfixExpression infixExpr1 = super.getASTNode().newInfixExpression();
		infixExpr1.setOperator(InfixExpression.Operator.PLUS);
		FieldAccess sizeFieldA1 = super.getASTNode().newFieldAccess();
		sizeFieldA1.setExpression(super.getASTNode().newThisExpression());
		sizeFieldA1.setName(super.getASTNode().newSimpleName("size"));
		infixExpr1.setLeftOperand(sizeFieldA1);
		NumberLiteral incrementValue1 = super.getASTNode().newNumberLiteral("1");
		infixExpr1.setRightOperand(incrementValue1);
		arrayC.dimensions().add(infixExpr1);
		
		//new a table
		VariableDeclarationStatement varDeclStmt = super.createVariableDeclarationStatement(recordTypeName, "newTable", true, arrayC);
		block.statements().add(varDeclStmt);
		
		//for loop to copy data from table to new table
		
		//add the index variable
		String indexVar = "i";
		NumberLiteral indexLiter = super.getASTNode().newNumberLiteral("0");
		VariableDeclarationStatement intVarDeclStmt = super.createVariableDeclarationStatement(PrimitiveType.INT, 
				indexVar, false, indexLiter);
		block.statements().add(intVarDeclStmt);
		
		//put a for while here
		WhileStatement whileStmt = super.getASTNode().newWhileStatement();
				
		//set condition expression
		InfixExpression infixExpr = super.getASTNode().newInfixExpression();
		infixExpr.setOperator(InfixExpression.Operator.LESS);
		infixExpr.setLeftOperand(super.getASTNode().newSimpleName(indexVar));
		FieldAccess fieldA = super.getASTNode().newFieldAccess();
		fieldA.setExpression(super.getASTNode().newThisExpression());
		fieldA.setName(super.getASTNode().newSimpleName("size"));
		infixExpr.setRightOperand(fieldA);
		whileStmt.setExpression(infixExpr);
				
		//set for loop body
		org.eclipse.jdt.core.dom.Block whileLoopBlock = super.getASTNode().newBlock();
		
		//assignment 
		Assignment tableRecordAssignExpr = super.getASTNode().newAssignment();
		tableRecordAssignExpr.setOperator(Assignment.Operator.ASSIGN);
		
		//access to the new tale
		ArrayAccess newTableArrayA = super.getASTNode().newArrayAccess();
		newTableArrayA.setArray(super.getASTNode().newSimpleName("newTable"));
		newTableArrayA.setIndex(super.getASTNode().newSimpleName(indexVar));
		tableRecordAssignExpr.setLeftHandSide(newTableArrayA);
		
		//access to table and get table[i]
		ArrayAccess oldTableArrayA = super.getASTNode().newArrayAccess();
		FieldAccess tableFieldA = super.getASTNode().newFieldAccess();
		tableFieldA.setExpression(super.getASTNode().newThisExpression());
		tableFieldA.setName(super.getASTNode().newSimpleName("table"));
		oldTableArrayA.setArray(tableFieldA);
		oldTableArrayA.setIndex(super.getASTNode().newSimpleName(indexVar));
		tableRecordAssignExpr.setRightHandSide(oldTableArrayA);

		Statement tableCopyAssignStmt = super.getASTNode().newExpressionStatement(tableRecordAssignExpr);
		
		whileLoopBlock.statements().add(tableCopyAssignStmt);
		
		//set update
		Assignment updateExpr = super.getASTNode().newAssignment();
		updateExpr.setLeftHandSide(super.getASTNode().newSimpleName(indexVar));
		updateExpr.setOperator(Assignment.Operator.ASSIGN);
		InfixExpression indexIncrementExpr = super.getASTNode().newInfixExpression();
		indexIncrementExpr.setLeftOperand(super.getASTNode().newSimpleName(indexVar));
		indexIncrementExpr.setOperator(InfixExpression.Operator.PLUS);
		indexIncrementExpr.setRightOperand(super.getASTNode().newNumberLiteral("1"));
		updateExpr.setRightHandSide(indexIncrementExpr);
		
		ExpressionStatement indexUpdateStmt = super.getASTNode().newExpressionStatement(updateExpr);
		whileLoopBlock.statements().add(indexUpdateStmt);
		
		whileStmt.setBody(whileLoopBlock);
		block.statements().add(whileStmt);
		
		//newTable[size] = newRecord
		
		Assignment newRecordAddAssignExpr = super.getASTNode().newAssignment();
		newRecordAddAssignExpr.setOperator(Assignment.Operator.ASSIGN);
		
		//access to the new tale
		ArrayAccess newRecordAddArrayA = super.getASTNode().newArrayAccess();
		newRecordAddArrayA.setArray(super.getASTNode().newSimpleName("newTable"));
		FieldAccess newRecordAddFieldA = super.getASTNode().newFieldAccess();
		newRecordAddFieldA.setExpression(super.getASTNode().newThisExpression());
		newRecordAddFieldA.setName(super.getASTNode().newSimpleName("size"));
		newRecordAddArrayA.setIndex(newRecordAddFieldA);
		newRecordAddAssignExpr.setLeftHandSide(newRecordAddArrayA);
		
		//equal to the newRecord
		newRecordAddAssignExpr.setRightHandSide(super.getASTNode().newSimpleName(insertRecordArg));

		Statement newRecordAddAssignStmt = super.getASTNode().newExpressionStatement(newRecordAddAssignExpr);
		block.statements().add(newRecordAddAssignStmt);
		
		//assign table by newTable
		Assignment tableAssignExpr = super.getASTNode().newAssignment();
		tableAssignExpr.setOperator(Assignment.Operator.ASSIGN);
		
		//get old table
		FieldAccess tableFieldA1 = super.getASTNode().newFieldAccess();
		tableFieldA1.setExpression(super.getASTNode().newThisExpression());
		tableFieldA1.setName(super.getASTNode().newSimpleName("table"));
		tableAssignExpr.setLeftHandSide(tableFieldA1);
		
		tableAssignExpr.setRightHandSide(super.getASTNode().newSimpleName("newTable"));
		Statement tableAssignStmt = super.getASTNode().newExpressionStatement(tableAssignExpr);
		block.statements().add(tableAssignStmt);
		
		//increase size by one
		Assignment sizeAssignExpr = super.getASTNode().newAssignment();
		sizeAssignExpr.setOperator(Assignment.Operator.ASSIGN);
		FieldAccess sizeFieldA = super.getASTNode().newFieldAccess();
		sizeFieldA.setExpression(super.getASTNode().newThisExpression());
		sizeFieldA.setName(super.getASTNode().newSimpleName("size"));
		sizeAssignExpr.setLeftHandSide(sizeFieldA);
		InfixExpression sizeInfixExpr = super.getASTNode().newInfixExpression();
		sizeInfixExpr.setOperator(InfixExpression.Operator.PLUS);
		FieldAccess sizeFieldA2 = super.getASTNode().newFieldAccess();
		sizeFieldA2.setExpression(super.getASTNode().newThisExpression());
		sizeFieldA2.setName(super.getASTNode().newSimpleName("size"));
		sizeInfixExpr.setLeftOperand(sizeFieldA2);
		NumberLiteral incrementValue = super.getASTNode().newNumberLiteral("1");
		sizeInfixExpr.setRightOperand(incrementValue);
		sizeAssignExpr.setRightHandSide(sizeInfixExpr);
		
		ExpressionStatement exprStmt = super.getASTNode().newExpressionStatement(sizeAssignExpr);
		
		block.statements().add(exprStmt);
		methodDeclaration.setBody(block);
		
		//add specs
		List<String> specs = new ArrayList<String>();
		String requireSpecs = JahobSpecsUtil.requirePrefix + 
			JahobSpecsUtil.getUniqueInsertRequireSpecs(this.getClassName(), insertRecordArg) + "\"";
		String modifySpecs = JahobSpecsUtil.modifyPrefix + 
				JahobSpecsUtil.getModifyContentsAndSize(this.getClassName());
		String ensureSpecs = JahobSpecsUtil.ensurePrefix + 
			JahobSpecsUtil.getEnsureSizeIncreaseByOne(this.getClassName()) +
			" & " + JahobSpecsUtil.getEnsureContentEnlargedByOne(this.getClassName(), insertRecordArg) + "\"";
		specs.add(requireSpecs);
		specs.add(modifySpecs);
		specs.add(ensureSpecs);
		this.methodSpecs.put(methodName, specs);
		
		//loop invariants
		String loopInvSpecs = " inv \" " +JahobSpecsUtil.getUniqueInsertLoopInvStr(
				this.getClassName(), indexVar, "newTable") + "\"";
		this.loopInvartiantSpecs.put(methodName, loopInvSpecs);
		return methodDeclaration;
	}
	
	/**
	 * Creates the insert function.
	 *
	 * @return the method declaration
	 */
	public MethodDeclaration createInsertFunction(){
		String methodName = "insert";
		MethodDeclaration methodDeclaration = super.createMethodDeclaration(PrimitiveType.VOID,
				methodName, ModifierKeyword.PUBLIC_KEYWORD);
		String recordTypeName = tableInstance.get_Table_Name() + DatabaseRecordClassCreator.RECORDSTRING;
		String argName = "arg0";
		String recordName = "temp";
		SingleVariableDeclaration varDecl = super.createVariableDeclaration(recordTypeName, argName, false);
		methodDeclaration.parameters().add(varDecl);
		
		List<DataField> updateDataFieldList = this.tableInstance.getModifiableDataFieldList();
		List<DataField> primaryKeyList = this.tableInstance.getPrimaryKeyDataFieldList();
		
		org.eclipse.jdt.core.dom.Block block = super.getASTNode().newBlock();
		
		//try to getRecord from the table
		//define a variable declaration
		//MethodInvocation
		MethodInvocation methodInvocation = super.getASTNode().newMethodInvocation();
		methodInvocation.setExpression(super.getASTNode().newThisExpression());
		methodInvocation.setName(super.getASTNode().newSimpleName("getRecord"));
		int index = 0;
		for(DataField df : primaryKeyList){
			MethodInvocation innerMInvocation = super.getASTNode().newMethodInvocation();
			innerMInvocation.setExpression(super.getASTNode().newSimpleName(argName));
			innerMInvocation.setName(super.getASTNode().newSimpleName("get"+df.get_Data_Field_Name()));
			methodInvocation.arguments().add(innerMInvocation);
			index = index + 1;
		}
		VariableDeclarationStatement varDeclStmt = super.createVariableDeclarationStatement(recordTypeName, 
				recordName, false, methodInvocation);
		block.statements().add(varDeclStmt);
		//check if it is contained in the table
		IfStatement ifStmt = super.getASTNode().newIfStatement();
		//set if condition
		InfixExpression infixExpr = super.getASTNode().newInfixExpression();
		infixExpr.setOperator(InfixExpression.Operator.NOT_EQUALS);
		infixExpr.setLeftOperand(super.getASTNode().newSimpleName(recordName));
		
		NullLiteral nullLiteral = super.getASTNode().newNullLiteral();
		infixExpr.setRightOperand(nullLiteral);
		ifStmt.setExpression(infixExpr);
		//set if block
		org.eclipse.jdt.core.dom.Block ifBlock = super.getASTNode().newBlock();
		//get lww logical timestamp
		MethodInvocation fieldAccessMInvoc = super.getASTNode().newMethodInvocation();
		fieldAccessMInvoc.setExpression(super.getASTNode().newSimpleName(argName));
		fieldAccessMInvoc.setName(super.getASTNode().newSimpleName("get"+tableInstance.getLwwTs().get_Data_Field_Name()));
		VariableDeclarationStatement fieldAccessVarDeclStmt = super.createVariableDeclarationStatement(
				CrdtFactory.getLwwLogicalTimestampCrdtTypeString(), "lwwLts", false, fieldAccessMInvoc);
		ifBlock.statements().add(fieldAccessVarDeclStmt);
		//call record
		for(int i = 0; i < updateDataFieldList.size(); i++){
			DataField df = updateDataFieldList.get(i);
			if(!CrdtFactory.isLwwLogicalTimestamp(df.get_Crdt_Data_Type())
					&& !df.is_Primary_Key()){
				
				if(CrdtFactory.isLwwDeletedFlag(df.get_Crdt_Data_Type())) {
					//undelete this field
					MethodInvocation updateLwwMInvoc = super.getASTNode().newMethodInvocation();
					updateLwwMInvoc.setExpression(super.getASTNode().newSimpleName(recordName));
					updateLwwMInvoc.setName(super.getASTNode().newSimpleName("undelete"+df.get_Data_Field_Name()));
					updateLwwMInvoc.arguments().add(super.getASTNode().newSimpleName("lwwLts"));
					ExpressionStatement updateLwwMInvocExprStmt = super.getASTNode().newExpressionStatement(updateLwwMInvoc);
					ifBlock.statements().add(updateLwwMInvocExprStmt);
				}else {
					String crdtType = CrdtFactory.getProperCrdtObject(df.get_Crdt_Data_Type(), df.get_Data_Type());
					fieldAccessMInvoc = super.getASTNode().newMethodInvocation();
					fieldAccessMInvoc.setExpression(super.getASTNode().newSimpleName(argName));
					fieldAccessMInvoc.setName(super.getASTNode().newSimpleName("get"+df.get_Data_Field_Name()));
					fieldAccessVarDeclStmt = super.createVariableDeclarationStatement(crdtType, df.get_Data_Field_Name(), false, fieldAccessMInvoc);
					ifBlock.statements().add(fieldAccessVarDeclStmt);
					//call update for the field
					if(CrdtFactory.isLwwType(df.get_Crdt_Data_Type())){
							//add lwwlogicaltimestamp as parameter
							MethodInvocation updateLwwMInvoc = super.getASTNode().newMethodInvocation();
							updateLwwMInvoc.setExpression(super.getASTNode().newSimpleName(recordName));
							updateLwwMInvoc.setName(super.getASTNode().newSimpleName("update"+df.get_Data_Field_Name()));
							updateLwwMInvoc.arguments().add(super.getASTNode().newSimpleName(df.get_Data_Field_Name()));
							updateLwwMInvoc.arguments().add(super.getASTNode().newSimpleName("lwwLts"));
							ExpressionStatement updateLwwMInvocExprStmt = super.getASTNode().newExpressionStatement(updateLwwMInvoc);
							ifBlock.statements().add(updateLwwMInvocExprStmt);
					}else{
						MethodInvocation updateMInvoc = super.getASTNode().newMethodInvocation();
						updateMInvoc.setExpression(super.getASTNode().newSimpleName(recordName));
						updateMInvoc.setName(super.getASTNode().newSimpleName("update"+df.get_Data_Field_Name()));
						updateMInvoc.arguments().add(super.getASTNode().newSimpleName(df.get_Data_Field_Name()));
						ExpressionStatement updateMInvocExprStmt = super.getASTNode().newExpressionStatement(updateMInvoc);
						ifBlock.statements().add(updateMInvocExprStmt);
					}
				}
			}
		}
		
		//update lww logical timestamp
		MethodInvocation updateLwwLogicalTimestampMInvoc = super.getASTNode().newMethodInvocation();
		updateLwwLogicalTimestampMInvoc.setExpression(super.getASTNode().newSimpleName(recordName));
		updateLwwLogicalTimestampMInvoc.setName(super.getASTNode().newSimpleName("update"+LWW_LOGICALTIMESTAMP.logical_Timestamp_Name));
		updateLwwLogicalTimestampMInvoc.arguments().add(super.getASTNode().newSimpleName("lwwLts"));
		ExpressionStatement updateLwwLogicalTimestampExprStmt = super.getASTNode().newExpressionStatement(updateLwwLogicalTimestampMInvoc);
		ifBlock.statements().add(updateLwwLogicalTimestampExprStmt);
		// if not then please call insert
		ifStmt.setThenStatement(ifBlock);
		//set then block
		org.eclipse.jdt.core.dom.Block thenBlock = super.getASTNode().newBlock();

		//call uniqueInsert		
		MethodInvocation uInsertMInvocation = super.getASTNode().newMethodInvocation();
		uInsertMInvocation.setExpression(super.getASTNode().newThisExpression());
		uInsertMInvocation.setName(super.getASTNode().newSimpleName("uniqueInsert"));
		uInsertMInvocation.arguments().add(super.getASTNode().newSimpleName(argName));
		
		ExpressionStatement exprStmt = super.getASTNode().newExpressionStatement(uInsertMInvocation);
		thenBlock.statements().add(exprStmt);
		
		ifStmt.setElseStatement(thenBlock);
		block.statements().add(ifStmt);
		methodDeclaration.setBody(block);
		
		//add specs
		List<String> specs = new ArrayList<String>();
		String requireSpecs = JahobSpecsUtil.requirePrefix + 
			JahobSpecsUtil.getInsertRequireSpecs(this.getClassName(), argName) + "\"";
		String modifySpecs = JahobSpecsUtil.modifyPrefix + 
				JahobSpecsUtil.getInsertModifySpecs(this.getClassName(), tableInstance.getDataFieldList());
		String ensureSpecs = JahobSpecsUtil.ensurePrefix + 
			JahobSpecsUtil.getInsertEnsure(this.getClassName(), recordTypeName,
					"v", argName, tableInstance.getDataFieldList()) + "\"";
		specs.add(requireSpecs);
		specs.add(modifySpecs);
		specs.add(ensureSpecs);
		this.methodSpecs.put(methodName, specs);
		
		return methodDeclaration;
	}
	
	/**
	 * Creates the get index function.
	 *
	 * @return the method declaration
	 */
	public MethodDeclaration createGetIndexFunction(){
		String methodName = "getIndex";
		MethodDeclaration methodDeclaration = super.createMethodDeclaration(PrimitiveType.INT,
				methodName, ModifierKeyword.PUBLIC_KEYWORD);
		String recordName = tableInstance.get_Table_Name() + DatabaseRecordClassCreator.RECORDSTRING;
		SingleVariableDeclaration varDecl = super.createVariableDeclaration(recordName, recordName.toLowerCase(), false);
		methodDeclaration.parameters().add(varDecl);
		org.eclipse.jdt.core.dom.Block block = super.getASTNode().newBlock();
		//add the index variable
		String indexVar = "i";
		VariableDeclarationStatement varDeclStmt = super.createVariableDeclarationStatement(PrimitiveType.INT, indexVar, false, null);
		block.statements().add(varDeclStmt);
		
		//put a for loop here
		ForStatement forStmt = super.getASTNode().newForStatement();
		//set initializer
		Assignment assignExpr = super.getASTNode().newAssignment();
		assignExpr.setOperator(Assignment.Operator.ASSIGN);
		assignExpr.setLeftHandSide(super.getASTNode().newSimpleName(indexVar));
		NumberLiteral initializedValue = super.getASTNode().newNumberLiteral("0");
		assignExpr.setRightHandSide(initializedValue);
		forStmt.initializers().add(assignExpr);
		
		//set condition expression
		
		InfixExpression infixExpr = super.getASTNode().newInfixExpression();
		infixExpr.setOperator(InfixExpression.Operator.LESS);
		infixExpr.setLeftOperand(super.getASTNode().newSimpleName(indexVar));
		
		FieldAccess fieldA = super.getASTNode().newFieldAccess();
		fieldA.setExpression(super.getASTNode().newThisExpression());
		fieldA.setName(super.getASTNode().newSimpleName("size"));
		infixExpr.setRightOperand(fieldA);
		
		forStmt.setExpression(infixExpr);
		
		//set update
		PostfixExpression updateExpr = super.getASTNode().newPostfixExpression();
		updateExpr.setOperator(PostfixExpression.Operator.INCREMENT);
		updateExpr.setOperand(super.getASTNode().newSimpleName(indexVar));
		
		forStmt.updaters().add(updateExpr);
		
		//set for loop body
		org.eclipse.jdt.core.dom.Block forLoopBlock = super.getASTNode().newBlock();
		
		//add a if statement
		
		IfStatement ifStmt = super.getASTNode().newIfStatement();
		
		//set if condition
		InfixExpression ifCondExp = super.getASTNode().newInfixExpression();
		//left expr is in the form: this.table[i]
		ArrayAccess arrayA = super.getASTNode().newArrayAccess();
		FieldAccess tableFieldA = super.getASTNode().newFieldAccess();
		tableFieldA.setExpression(super.getASTNode().newThisExpression());
		tableFieldA.setName(super.getASTNode().newSimpleName("table"));
		arrayA.setArray(tableFieldA);
		arrayA.setIndex(super.getASTNode().newSimpleName(indexVar));
		ifCondExp.setLeftOperand(arrayA);
		//equal operator
		ifCondExp.setOperator(InfixExpression.Operator.EQUALS);
		//right expr
		ifCondExp.setRightOperand(super.getASTNode().newSimpleName(recordName.toLowerCase()));
		
		ifStmt.setExpression(ifCondExp);
		//add block for ifStmt
		org.eclipse.jdt.core.dom.Block ifBlock = super.getASTNode().newBlock();
		
		ReturnStatement indexReturnStmt = super.getASTNode().newReturnStatement();
		indexReturnStmt.setExpression(super.getASTNode().newSimpleName(indexVar));
		
		ifBlock.statements().add(indexReturnStmt);
		ifStmt.setThenStatement(ifBlock);
		forLoopBlock.statements().add(ifStmt);
		forStmt.setBody(forLoopBlock);
		
		block.statements().add(forStmt);
		
		//return false
		ReturnStatement noIndexReturnStmt = super.getASTNode().newReturnStatement();
		NumberLiteral noIndexValue = super.getASTNode().newNumberLiteral("-1");
		noIndexReturnStmt.setExpression(noIndexValue);
		block.statements().add(noIndexReturnStmt);
		
		methodDeclaration.setBody(block);
		return methodDeclaration;
	}
	
	/**
	 * Combine infix exprs.
	 *
	 * @param exprList the expr list
	 * @return the infix expression
	 */
	private InfixExpression combineInfixExprs(List<InfixExpression> exprList){
		Expression leftOperand = exprList.get(0);
		Expression rightOperand = null;
		InfixExpression finalExpr = null;
		for(int i = 1; i < exprList.size(); i++){
			rightOperand = exprList.get(i);
			finalExpr = super.getASTNode().newInfixExpression();
			finalExpr.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
			finalExpr.setLeftOperand(leftOperand);
			finalExpr.setRightOperand(rightOperand);
			leftOperand = finalExpr;
		}
		return finalExpr;
	}
	
	/**
	 * Creates the get record function.
	 *
	 * @return the method declaration
	 */
	public MethodDeclaration createGetRecordFunction(){
		String methodName = "getRecord";
		String recordTypeName = tableInstance.get_Table_Name() + DatabaseRecordClassCreator.RECORDSTRING;
		MethodDeclaration methodDeclaration = super.createMethodDeclaration(recordTypeName,
				methodName, ModifierKeyword.PUBLIC_KEYWORD);
		
		String requireSpecs = JahobSpecsUtil.requirePrefix;
		String ensureSpecs = JahobSpecsUtil.ensurePrefix;
		List<String> specs = new ArrayList<String>();
		requireSpecs +=  JahobSpecsUtil.getTableInit(this.getClassName());
		String ensureExist = "(" +  JahobSpecsUtil.getExistencePrefix(this.getClassName());
		String ensureNoExist = "";
		//get all primary key as parameters
		List<DataField> primaryKeyList = this.tableInstance.getPrimaryKeyDataFieldList();
		int index = 0;
		for(DataField df : primaryKeyList){
			String crdtObjectType = CrdtFactory.getProperCrdtObject(df.get_Crdt_Data_Type(), df.get_Data_Type());
			String primaryParaName = "pk" + index;
			SingleVariableDeclaration varDecl = super.createVariableDeclaration(crdtObjectType, primaryParaName, false);
			methodDeclaration.parameters().add(varDecl);
			index = index + 1;
			requireSpecs += " & " + JahobSpecsUtil.getRequireNotNullClause(primaryParaName);
			ensureExist += " & " + JahobSpecsUtil.getTableRecordEnsureFieldEqual(recordTypeName, df.get_Data_Field_Name(), 
					primaryParaName);
		}
		requireSpecs += "\"";
		ensureNoExist = "(~" + ensureExist +")";
		ensureExist += " --> " + JahobSpecsUtil.getGetRecordResultEqual(true) + ")";
		ensureNoExist += "-->" + JahobSpecsUtil.getGetRecordResultEqual(false) + ")";
		
		ensureSpecs += ensureExist + " \\<or> \n\t" + ensureNoExist + "\"";
		
		org.eclipse.jdt.core.dom.Block block = super.getASTNode().newBlock();
		//add block here
		//for loop
		//add the index variable
		String indexVar = "i";
		NumberLiteral indexInitializedValue = super.getASTNode().newNumberLiteral("0");
		VariableDeclarationStatement varDeclStmt = super.createVariableDeclarationStatement(PrimitiveType.INT, indexVar, false, indexInitializedValue);
		block.statements().add(varDeclStmt);
				
		//put a while loop here
		WhileStatement whileStmt = super.getASTNode().newWhileStatement();
				
		//set condition expression
		InfixExpression infixExpr = super.getASTNode().newInfixExpression();
		infixExpr.setOperator(InfixExpression.Operator.LESS);
		infixExpr.setLeftOperand(super.getASTNode().newSimpleName(indexVar));
				
		FieldAccess fieldA = super.getASTNode().newFieldAccess();
		fieldA.setExpression(super.getASTNode().newThisExpression());
		fieldA.setName(super.getASTNode().newSimpleName("size"));
		infixExpr.setRightOperand(fieldA);
				
		whileStmt.setExpression(infixExpr);
				
		//set for loop body
		org.eclipse.jdt.core.dom.Block whileLoopBlock = super.getASTNode().newBlock();
				
		//add a if statement
				
		IfStatement ifStmt = super.getASTNode().newIfStatement();
		
		//get all infix sub expression
		List<InfixExpression> infixExprList = new ArrayList<InfixExpression>();
		for(int i = 0; i < primaryKeyList.size() ; i++){
			DataField df = primaryKeyList.get(i);
			//set if condition
			//MethodInvocation
			//MethodInvocation outerMethodInvocation = super.getASTNode().newMethodInvocation();
			MethodInvocation methodInvocation = super.getASTNode().newMethodInvocation();
		
			//left expr is in the form: this.table[i]
			ArrayAccess arrayA = super.getASTNode().newArrayAccess();
			FieldAccess tableFieldA = super.getASTNode().newFieldAccess();
			tableFieldA.setExpression(super.getASTNode().newThisExpression());
			tableFieldA.setName(super.getASTNode().newSimpleName("table"));
			arrayA.setArray(tableFieldA);
			arrayA.setIndex(super.getASTNode().newSimpleName(indexVar));
			
			methodInvocation.setExpression(arrayA);
			methodInvocation.setName(super.getASTNode().newSimpleName("get"+df.get_Data_Field_Name()));
			
			InfixExpression ifConditionExpr = super.getASTNode().newInfixExpression();
			ifConditionExpr.setOperator(InfixExpression.Operator.EQUALS);
			ifConditionExpr.setLeftOperand(methodInvocation);
			ifConditionExpr.setRightOperand(super.getASTNode().newSimpleName("pk"+i));
			
			//outerMethodInvocation.setExpression(methodInvocation);
			//outerMethodInvocation.setName(super.getASTNode().newSimpleName("equalTo"));
			//outerMethodInvocation.arguments().add(super.getASTNode().newSimpleName("pk"+i));
			
			infixExprList.add(ifConditionExpr);
		}
		
		if(infixExprList.size() == 1){
			ifStmt.setExpression(infixExprList.get(0));
		}else{
			InfixExpression expr = this.combineInfixExprs(infixExprList);
			ifStmt.setExpression(expr);
		}
				
		
		//add block for ifStmt
		org.eclipse.jdt.core.dom.Block ifBlock = super.getASTNode().newBlock();
				
		//return the current record
		ArrayAccess returnArrayA = super.getASTNode().newArrayAccess();
		FieldAccess returnTableFieldA = super.getASTNode().newFieldAccess();
		returnTableFieldA.setExpression(super.getASTNode().newThisExpression());
		returnTableFieldA.setName(super.getASTNode().newSimpleName("table"));
		returnArrayA.setArray(returnTableFieldA);
		returnArrayA.setIndex(super.getASTNode().newSimpleName(indexVar));
		ReturnStatement indexReturnStmt = super.getASTNode().newReturnStatement();
		indexReturnStmt.setExpression(returnArrayA);
				
		ifBlock.statements().add(indexReturnStmt);
		ifStmt.setThenStatement(ifBlock);
		whileLoopBlock.statements().add(ifStmt);
		
		//set update
		Assignment updateExpr = super.getASTNode().newAssignment();
		updateExpr.setLeftHandSide(super.getASTNode().newSimpleName(indexVar));
		updateExpr.setOperator(Assignment.Operator.ASSIGN);
		InfixExpression indexIncrementExpr = super.getASTNode().newInfixExpression();
		indexIncrementExpr.setLeftOperand(super.getASTNode().newSimpleName(indexVar));
		indexIncrementExpr.setOperator(InfixExpression.Operator.PLUS);
		indexIncrementExpr.setRightOperand(super.getASTNode().newNumberLiteral("1"));
		updateExpr.setRightHandSide(indexIncrementExpr);
		
		ExpressionStatement indexUpdateStmt = super.getASTNode().newExpressionStatement(updateExpr);
		whileLoopBlock.statements().add(indexUpdateStmt);
		whileStmt.setBody(whileLoopBlock);
				
		block.statements().add(whileStmt);
		methodDeclaration.setBody(block);
		
		//return null if not found
		ReturnStatement noFoundReturnStmt = super.getASTNode().newReturnStatement();
		NullLiteral noFoundValue = super.getASTNode().newNullLiteral();
		noFoundReturnStmt.setExpression(noFoundValue);
		block.statements().add(noFoundReturnStmt);
		
		specs.add(requireSpecs);
		specs.add(ensureSpecs);
		this.methodSpecs.put(methodName, specs);
		
		//loop invariants
		String loopInvSpecs = " inv \" " +JahobSpecsUtil.getGetRecordLoopInvariant(indexVar, recordTypeName, primaryKeyList) + "\"";
		this.loopInvartiantSpecs.put(methodName, loopInvSpecs);
		return methodDeclaration;
	}
	
	/**
	 * Creates the delete function.
	 *
	 * @return the method declaration
	 */
	public MethodDeclaration createDeleteFunction(){
		String methodName = "delete";
		MethodDeclaration methodDeclaration = super.createMethodDeclaration(PrimitiveType.VOID,
				methodName, ModifierKeyword.PUBLIC_KEYWORD);
		String recordName = tableInstance.get_Table_Name() + DatabaseRecordClassCreator.RECORDSTRING;
		
		String requireSpecs = JahobSpecsUtil.requirePrefix;
		String modifySpecs = JahobSpecsUtil.modifyPrefix;
		String ensureSpecs = JahobSpecsUtil.ensurePrefix;
		List<String> specs = new ArrayList<String>();
		requireSpecs +=  JahobSpecsUtil.getTableInit(this.getClassName()) ;
		
		//get all primary key as parameters
		List<DataField> primaryKeyList = this.tableInstance.getPrimaryKeyDataFieldList();
		int index = 0;
		for(DataField df : primaryKeyList){
			String crdtObjectType = CrdtFactory.getProperCrdtObject(df.get_Crdt_Data_Type(), df.get_Data_Type());
			String primaryParaName = "pk" + index;
			SingleVariableDeclaration varDecl = super.createVariableDeclaration(crdtObjectType, primaryParaName, false);
			methodDeclaration.parameters().add(varDecl);
			index = index + 1;
			requireSpecs += " & " + JahobSpecsUtil.getRequireNotNullClause(primaryParaName);
		}
		
		//add lwwlogicaltimestamp
		String lwwLtsType = CrdtFactory.getLwwLogicalTimestampCrdtTypeString();
		String lwwLtsName = "lwwLTS";
		SingleVariableDeclaration varDecl = super.createVariableDeclaration(lwwLtsType, lwwLtsName, false);
		methodDeclaration.parameters().add(varDecl);
		requireSpecs += " & " + JahobSpecsUtil.getRequireNotNullClause(lwwLtsName);
		//existence
		requireSpecs += " & " + JahobSpecsUtil.getExistenceStrByPrimaryKeyList(this.getClassName(),
				recordName, primaryKeyList);
		requireSpecs += "\"";
		org.eclipse.jdt.core.dom.Block block = super.getASTNode().newBlock();
		//add block here
		//get record from call the getRecord function
		//define a variable declaration
		//MethodInvocation
		MethodInvocation methodInvocation = super.getASTNode().newMethodInvocation();
		methodInvocation.setExpression(super.getASTNode().newThisExpression());
		methodInvocation.setName(super.getASTNode().newSimpleName("getRecord"));
		index = 0;
		for(DataField df : primaryKeyList){
			methodInvocation.arguments().add(super.getASTNode().newSimpleName("pk"+index));
			index = index + 1;
		}
		VariableDeclarationStatement varDeclStmt = super.createVariableDeclarationStatement(recordName, recordName.toLowerCase(), false, methodInvocation);
		block.statements().add(varDeclStmt);
		//call update lwwDeletedFlag
		
		MethodInvocation updateLwwDeletedFlagMInvoc = super.getASTNode().newMethodInvocation();
		updateLwwDeletedFlagMInvoc.setExpression(super.getASTNode().newSimpleName(recordName.toLowerCase()));
		updateLwwDeletedFlagMInvoc.setName(super.getASTNode().newSimpleName("update"+LWW_DELETEDFLAG.deleted_Flag_Name));
		updateLwwDeletedFlagMInvoc.arguments().add(super.getASTNode().newSimpleName("lwwLTS"));
		ExpressionStatement updateLwwDeletedFlagExprStmt = super.getASTNode().newExpressionStatement(updateLwwDeletedFlagMInvoc);
		block.statements().add(updateLwwDeletedFlagExprStmt);
		
		methodDeclaration.setBody(block);
		
		modifySpecs += JahobSpecsUtil.getModifyField(CrdtDataFieldType.LWWBOOLEAN);
		ensureSpecs += JahobSpecsUtil.getExistencePrefix(this.getClassName()) + " & (";
		//two conditions
		ensureSpecs += JahobSpecsUtil.getEnsureClauseForLwwDeleteFlagForTable("v.."+recordName,
				LWW_DELETEDFLAG.deleted_Flag_Name, CrdtDataFieldType.LWWBOOLEAN, "lwwLTS");
		ensureSpecs += ")\"";
		specs.add(requireSpecs);
		specs.add(modifySpecs);
		specs.add(ensureSpecs);
		this.methodSpecs.put(methodName, specs);
		return methodDeclaration;
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
		// TODO Auto-generated method stub
		List<MethodDeclaration> methodDecls = new ArrayList<MethodDeclaration>();
		if(tableInstance instanceof UosetTable || 
				tableInstance instanceof AusetTable){
			methodDecls.add(this.createGetRecordFunction());
		}
		if(tableInstance instanceof ArsetTable ||
				tableInstance instanceof AosetTable ||
				tableInstance instanceof AusetTable){
			//methodDecls.add(this.createGetSizeFunction());
			methodDecls.add(this.createUniqueInsertFunction());
		}
		if(tableInstance instanceof ArsetTable){
			Debug.println("not aoset table");
			//methodDecls.add(this.createIsContainedFunction());
			methodDecls.add(this.createInsertFunction());
			//methodDecls.add(this.createGetIndexFunction());
			methodDecls.add(this.createGetRecordFunction());
			methodDecls.add(this.createDeleteFunction());
		}
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
		this.fieldSpecs.add(JahobSpecsUtil.getTableInitSpecs(this.getClassName()));
		this.fieldSpecs.add(JahobSpecsUtil.getTableSizeSpecs(this.getClassName()));
		this.fieldSpecs.add(JahobSpecsUtil.getTableSizeVardefs(this.getClassName()));
		this.fieldSpecs.add(JahobSpecsUtil.getTableContentsSpecs(this.getClassName()));
		this.fieldSpecs.add(JahobSpecsUtil.getTableContentsVardefs(this.getClassName()));
		
		MethodDeclaration methodDecl = this.createConstructorDeclaration();
		type.bodyDeclarations().add(methodDecl);
		
		List<MethodDeclaration> methodDecls = this.createFunctions();
		for(MethodDeclaration methodMember : methodDecls){
			type.bodyDeclarations().add(methodMember);
		}
		super.addTypeDeclaration(type);
		super.writeToFile();
		//append specs
		super.reloadFileToFileContents();
		this.appendAllSpecs();
		super.writeToFileFromFileContents();
		
		this.generateAndExecuteJahobCommand();
	}
	
	/**
	 * Gets the table sufix string.
	 *
	 * @return the table sufix string
	 */
	public static String getTableSufixString(){
		return TABLESTRING;
	}

	/**
	 * Append all specs.
	 */
	public void appendAllSpecs() {
		//insert your field specs
		int index = super.findMethodOffset(this.getClassName());
		String fieldSpecs = super.mergeMultipleSpecifications(this.fieldSpecs);
		super.appendStringListToOffset(index, fieldSpecs);
		
		//insert constructor body specs
		index = super.findMethodBodyOffset(this.getClassName()) + 1;
		super.appendStringListToOffset(index, constructorBodySpecs);
		
		//insert all your method specs
		Iterator<Entry<String, List<String>>> it = this.methodSpecs.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, List<String>> entry = it.next();
			String methodName = entry.getKey();
			int insertIndex = super.findMethodBodyOffset(methodName);
			String methodSpecs = super.mergeMultipleSpecifications(entry.getValue());
			super.appendStringListToOffset(insertIndex, methodSpecs);
		}
		
		//insert loop invariants
		Iterator<Entry<String, String>> loopInvIt = this.loopInvartiantSpecs.entrySet().iterator();
		while(loopInvIt.hasNext()) {
			Entry<String, String> loopInvEntry = loopInvIt.next();
			String methodName = loopInvEntry.getKey();
			String loopInvSpecs = super.addJahobSpecsPrefixAndSubfix(loopInvEntry.getValue());
			int insertIndex = super.findLoopBodyOffset(methodName) + 1;
			super.appendStringListToOffset(insertIndex, loopInvSpecs);
		}
	}
	

	/* (non-Javadoc)
	 * @see staticanalysis.templatecreator.SourceCodeGenerator#getJahobCommand()
	 */
	@Override
	public String getJahobCommand() {
		String recordName = tableInstance.get_Table_Name() + DatabaseRecordClassCreator.RECORDSTRING;
		return JahobSpecsUtil.getTableVerificationExecCommand(this.getClassName(),this.getFullFilePath(), 
				this.getCodePath() + "/" + recordName + ".java", tableInstance.getDataFieldList());
	}


	/* (non-Javadoc)
	 * @see staticanalysis.templatecreator.SourceCodeGenerator#generateAndExecuteJahobCommand()
	 */
	@Override
	public void generateAndExecuteJahobCommand() {
		List<String> commands = new ArrayList<String>();
		commands.add(this.getJahobCommand());
		String shellScript = FileOperations.createShellScript(this.getCodePath(), "run"+this.getClassName(), commands);
		//FileOperations.executeShellCommands(shellScript);
	}
	
}

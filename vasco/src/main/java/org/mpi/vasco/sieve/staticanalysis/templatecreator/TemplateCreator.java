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

import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;

import java.io.StringReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;

import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

import org.mpi.vasco.sieve.runtimelogic.staticinformation.StaticFPtoWPsStore;
import org.mpi.vasco.sieve.staticanalysis.codeparser.CodeNodeIdentifier;
import org.mpi.vasco.sieve.staticanalysis.codeparser.ExpressionParser;
import org.mpi.vasco.sieve.staticanalysis.templatecreator.template.DeleteOperation;
import org.mpi.vasco.sieve.staticanalysis.templatecreator.template.InsertOperation;
import org.mpi.vasco.sieve.staticanalysis.templatecreator.template.Operation;
import org.mpi.vasco.sieve.staticanalysis.templatecreator.template.ShadowOperationTemplate;
import org.mpi.vasco.sieve.staticanalysis.templatecreator.template.UniqueInsertOperation;
import org.mpi.vasco.sieve.staticanalysis.templatecreator.template.UpdateOperation;
import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGGraph;
import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGNode;

import org.mpi.vasco.util.annotationparser.InvariantParser;
import org.mpi.vasco.util.annotationparser.SchemaParser;
import org.mpi.vasco.util.commonfunc.FileOperations;
import org.mpi.vasco.util.commonfunc.StringOperations;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.AosetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.ArsetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.AusetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.CrdtFactory;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.UosetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DataField;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.RuntimeExceptionType;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class TemplateCreator.
 */
public class TemplateCreator extends SourceCodeGenerator{
	
	/** The schema parser. */
	private SchemaParser schemaParser;
	
	/** The inv parser. */
	private InvariantParser invParser;
	
	/** The c jsql parser. */
	private CCJSqlParserManager cJsqlParser;
	
	/** The cfg list. */
	private List<CFGGraph<CodeNodeIdentifier, Expression>> cfgList;
	
	/** The Constant CLASS_NAME_STR. */
	public final static String CLASS_NAME_STR = "shadowOptemplate";
	
	/** The Constant LWWLTS_VALUE. */
	private final static String LWWLTS_VALUE = "lwwLts";
	
	/** The method specs. 
	 * mapping from method name to frame conditions
	 * */
	HashMap<String, List<String>> methodSpecs;
	
	List<ShadowOperationTemplate> tList;
	
	/** The table arg count. */
	private int tableArgCount = 0;
	
	/** The hashmap mapping from the method name to the pair (numOfPath, numOfTemplates) */
	private HashMap<String, PathTemplateNumPair> statisMap;

	/**
	 * Instantiates a new template creator.
	 *
	 * @param sp the sp
	 * @param invP the inv p
	 * @param projectName the project name
	 * @param cfgL the cfg l
	 */
	public TemplateCreator(SchemaParser sp, InvariantParser invP, 
			String projectName, List<CFGGraph<CodeNodeIdentifier, Expression>> cfgL){
		super(projectName, CLASS_NAME_STR);
		this.setSchemaParser(sp);
		this.setInvParser(invP);
		this.setCfgList(cfgL);
		this.cJsqlParser = new CCJSqlParserManager();
		this.methodSpecs = new HashMap<String, List<String>>();
		this.statisMap = new HashMap<String, PathTemplateNumPair>();
	}
	
	/**
	 * Gets the cfg list.
	 *
	 * @return the cfg list
	 */
	public List<CFGGraph<CodeNodeIdentifier, Expression>> getCfgList() {
		return cfgList;
	}
	
	/**
	 * Sets the cfg list.
	 *
	 * @param cfgList the cfg list
	 */
	public void setCfgList(List<CFGGraph<CodeNodeIdentifier, Expression>> cfgList) {
		this.cfgList = cfgList;
	}
	
	//functions	
	
	/**
	 * Checks if is execute update method call expression.
	 *
	 * @param exp the exp
	 * @return true, if is execute update method call expression
	 */
	private boolean isExecuteUpdateMethodCallExpression(Expression exp){
		if(ExpressionParser.isMethodCallExpression(exp)){
			MethodCallExpr methodCallExpr = (MethodCallExpr) exp;
			if(methodCallExpr.getName().equals("executeUpdate")){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	private String assembleStringForBinaryExpr(List<CFGNode<CodeNodeIdentifier, Expression>> precedingNodeList,
			BinaryExpr binExpr) {
		List<Expression> operandList = ExpressionParser.getOperands((BinaryExpr) binExpr);
		String varValueString = "";
		for(Expression operand: operandList){
			Debug.println("we get an operand: " + operand.toString());
			if(ExpressionParser.isLiteralExpression(operand)){
				varValueString += StringOperations.trimDoubleQuotesHeadTail(operand.toString());
			}else{
				if(ExpressionParser.isNameExpr(operand)) {
					varValueString += this.assembleStringForNameExpr(precedingNodeList, (NameExpr) operand);
				}else {
					varValueString +="?";
				}
			}
		}
		return StringOperations.trimDoubleQuotesHeadTail(varValueString);
	}
	
	//find one update/insert/delete statement
	
	/**
	 * Assemble string for name expr.
	 *
	 * @param precedingNodeList the preceding node list
	 * @param nameExpr the name expr
	 * @return the string
	 */
	//TODO: currently we only support = or +=
	private String assembleStringForNameExpr(List<CFGNode<CodeNodeIdentifier, Expression>> precedingNodeList, NameExpr nameExpr){
		Debug.println("Hello, you have a variable: " + nameExpr.getName());
		int index = precedingNodeList.size() - 1;
		while(index >= 0){
			Expression expr = precedingNodeList.get(index).getNodeData();
			Debug.println("currently we work with expr: " + expr.toString());
			if(ExpressionParser.isAssignmentExpr(expr)){
				//if the leftside is nameExpr, then check the right side is the string literal or some literal , return
				AssignExpr assignExpr = (AssignExpr) expr;
				assert(assignExpr.getOperator() == AssignExpr.Operator.assign);
				Expression target = assignExpr.getTarget();
				Expression value = assignExpr.getValue();
				Debug.println("target: " + target.toString());
				Debug.println("value: " + value.toString());
				// if the right side is not a string literal, then it must be a plus operator
				if(target.toString().equals(nameExpr.getName())){
					Debug.println("the assignment target is the one we want: " + target.toString());
					if(ExpressionParser.isLiteralExpression(value)){
						return StringOperations.trimDoubleQuotesHeadTail(value.toString());
					}else{
						// it must be a binaryExpr
						if(ExpressionParser.isBinaryExpr(value)) {
							return this.assembleStringForBinaryExpr(precedingNodeList.subList(0, index), (BinaryExpr) value);
						}else {
							return "?";
						}
					}
				}
			}else if(ExpressionParser.isVariableDeclarationExpr(expr)){
				//if it has initializer, then check whether it is string or not
				VariableDeclarationExpr varDeclExpr = (VariableDeclarationExpr) expr;
				//get the name of the variable
				Expression targetVarExpr = ExpressionParser.getMatchedVarDeclaration(varDeclExpr, nameExpr.getName());
				// if it doesn't have initializer return ""
				if(targetVarExpr != null){
					//if the targetVarExpr is literal then return;
					if(ExpressionParser.isLiteralExpression(targetVarExpr)){
						return StringOperations.trimDoubleQuotesHeadTail(targetVarExpr.toString());
					}else{
						// it must be a binaryExpr
						if(ExpressionParser.isBinaryExpr(targetVarExpr)) {
							return this.assembleStringForBinaryExpr(precedingNodeList.subList(0, index), (BinaryExpr) targetVarExpr);
						}else {
							return "?";
						}
					}
				}
			}
			index--;
		}
		return "";
	}
	
	/**
	 * Find sql updating statement.
	 *
	 * @param precedingNodeList the preceding node list
	 * @param cfgNode the cfg node
	 * @return the string
	 */
	private String findSqlUpdatingStatement(List<CFGNode<CodeNodeIdentifier, Expression>> precedingNodeList, CFGNode<CodeNodeIdentifier, Expression> cfgNode){
		//get the argument from the argument of the executeUpdate function
		MethodCallExpr methodCallExpr = (MethodCallExpr) cfgNode.getNodeData();
		List<Expression> args = methodCallExpr.getArgs();
		/*
		 * Fork into two branches: 
		 * one for connection.execute()
		 * one for preparestatement.executeUpdate()
		 */
		if(args != null) {
			assert(args.size() == 1);
			Expression argExpr = args.get(0);
			//if this argument is a string, then please return this string
			if(ExpressionParser.isStringLiteralExpression(argExpr)){
				return argExpr.toString();
			}else if(ExpressionParser.isNameExpr(argExpr)){
				//if the argument is a variable, please find it along the path back the start of the function
				  //* find the assignment expression
				return this.assembleStringForNameExpr(precedingNodeList, (NameExpr) argExpr);
			}else{
				System.err.println("This method has not been implemented!");
				return null;
			}
		}else {
			Expression scope = methodCallExpr.getScope();
			NameExpr namExpr = this.getNameExpr(scope);
			//find create preparestatement for this name expression
			return this.getUpdateSqlStatementFromPrepareStatement(precedingNodeList, namExpr);
		}
	}
	
	/**
	 * Gets the update sql statement from prepare statement.
	 *
	 * @param precedingNodeList the preceding node list
	 * @param preStat the pre stat
	 * @return the update sql statement from prepare statement
	 */
	private String getUpdateSqlStatementFromPrepareStatement(List<CFGNode<CodeNodeIdentifier, Expression>> precedingNodeList,
			NameExpr preStat) {
		int index = precedingNodeList.size() - 1;
		while(index >=0 ) {
			Expression expr = precedingNodeList.get(index).getNodeData();
			if(ExpressionParser.isAssignmentExpr(expr)){
				AssignExpr asExp = (AssignExpr) expr;
				Expression target = asExp.getTarget();
				if(target instanceof NameExpr) {
					if(((NameExpr)target).getName().equals(preStat.getName())) {
						//you find this
						Expression value = asExp.getValue();
						MethodCallExpr preDef = (MethodCallExpr) value;
						return this.getSqlUpdateStringFromMethodCallExpr(precedingNodeList.subList(0, index), preDef);
					}
				}
			}else if(ExpressionParser.isVariableDeclarationExpr(expr)) {
				VariableDeclarationExpr vdExpr = (VariableDeclarationExpr) expr;
				List<VariableDeclarator> vars = vdExpr.getVars();
				if(vars != null) {
					if(vars.size() == 1) {
						VariableDeclarator varDec = vars.get(0);
						VariableDeclaratorId varId = varDec.getId();
						if(varId.toString().equals(preStat.getName())) {
							//you find it
							Expression varInit = varDec.getInit();
							if(varInit instanceof MethodCallExpr) {
								MethodCallExpr methodCExpr = (MethodCallExpr) varInit;
								return this.getSqlUpdateStringFromMethodCallExpr(precedingNodeList.subList(0, index), 
										methodCExpr);
							}
						}
					}
				}
			}
			index--;
		}
		throw new RuntimeException("You cannot find this sql update statement");
	}
	
	/**
	 * Gets the sql update string from method call expr.
	 *
	 * @param precedingNodeList the preceding node list
	 * @param methodCExpr the method c expr
	 * @return the sql update string from method call expr
	 */
	private String getSqlUpdateStringFromMethodCallExpr(List<CFGNode<CodeNodeIdentifier, Expression>> precedingNodeList,
			MethodCallExpr methodCExpr) {
		List<Expression> args = methodCExpr.getArgs();
		assert(args != null);
		assert(args.size() == 1);
		Expression arg = args.get(0);
		if(arg instanceof NameExpr) {
			return this.assembleStringForNameExpr(precedingNodeList, (NameExpr) arg);
		}else {
			if(arg instanceof BinaryExpr) {
				return this.assembleStringForBinaryExpr(precedingNodeList, (BinaryExpr)arg);
			}else {
				return arg.toString();
			}
		}
	}
	
	/**
	 * Gets the name expr.
	 *
	 * @param scope the scope
	 * @return the name expr
	 */
	private NameExpr getNameExpr(Expression scope) {
		if(scope instanceof NameExpr) {
			return (NameExpr)scope;
		}else {
			throw new RuntimeException("this expression not implemented yet " + scope.toString());
		}
	}
	
	//find all updating statements from a control flow graph
	/**
	 * Find all sql updating statement.
	 *
	 * @param cfg the cfg
	 * @return the list
	 */
	public List<String> findAllSqlUpdatingStatement(CFGGraph<CodeNodeIdentifier, Expression> cfg){
		List<String> updatingQueryList = new ArrayList<String>();
		//identify a function call executeUpdate
		List<CFGNode<CodeNodeIdentifier, Expression>> nodeList = cfg.getNodeListViaBFS();
		List<CFGNode<CodeNodeIdentifier, Expression>> precedingNodeList = new ArrayList<CFGNode<CodeNodeIdentifier, Expression>>();
		for(CFGNode<CodeNodeIdentifier, Expression> cfgNode : nodeList){
			precedingNodeList.add(cfgNode);
			Expression expr = cfgNode.getNodeData();
			if(this.isExecuteUpdateMethodCallExpression(expr)){
				Debug.println("Expr: " + expr.toString());
				String e = this.findSqlUpdatingStatement(precedingNodeList, cfgNode);
				if(e != null){
					Debug.println("I found a string: " + e);
				}
				updatingQueryList.add(e);
			}
		}
		return updatingQueryList;
	}
	
	/**
	 * Creates the crdt unique insert operation.
	 *
	 * @param dT the d t
	 * @return the operation
	 */
	private Operation createCrdtUniqueInsertOperation(DatabaseTable dT){
		UniqueInsertOperation uniqueInsertOp = new UniqueInsertOperation(dT.get_Table_Name(), dT);
		return uniqueInsertOp;
	}
	
	/**
	 * Creates the insert operation.
	 *
	 * @param dT the d t
	 * @return the operation
	 */
	private Operation createInsertOperation(DatabaseTable dT){
		InsertOperation insertOp = new InsertOperation(dT.get_Table_Name(), dT);
		return insertOp;
	}
	
	/**
	 * Creates the update operation.
	 *
	 * @param dT the d t
	 * @param updateStatement the update statement
	 * @return the operation
	 */
	private Operation createUpdateOperation(DatabaseTable dT, Update updateStatement){
		List<Column> colList = updateStatement.getColumns();
		Iterator<Column> colIt = colList.iterator();
		List<DataField> dfList = new ArrayList<DataField>();
		while(colIt.hasNext()){
			Column cL = colIt.next();
			DataField dF = dT.get_Data_Field(cL.getColumnName());
			assert(dF.isNotNormalDataType());
			dfList.add(dF);
		}
		UpdateOperation updateOp = new UpdateOperation(dT.get_Table_Name(), dT, dfList);
		return updateOp;
	}
	
	/**
	 * Creates the delete operation.
	 *
	 * @param dT the d t
	 * @return the operation
	 */
	private Operation createDeleteOperation(DatabaseTable dT){
		DeleteOperation deleteOp = new DeleteOperation(dT.get_Table_Name(), dT);
		return deleteOp;
	}
	
	//create crdt operations for a statement
	/**
	 * Creates the crdt op for statement.
	 *
	 * @param updatingQuery the updating query
	 * @return the operation
	 */
	public Operation createCrdtOpForStatement(String updatingQuery){
		updatingQuery = StringOperations.removeLastSemiColumn(updatingQuery);
		net.sf.jsqlparser.statement.Statement sqlStmt = null;
		try {
			sqlStmt = this.cJsqlParser
					.parse(new StringReader(updatingQuery));
		} catch (JSQLParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (sqlStmt instanceof Insert) {
			Insert insertStatement = (Insert) sqlStmt;
			String tableName = insertStatement.getTable().getName();
			DatabaseTable dTb = this.schemaParser.getTableByName(tableName);

			if (dTb == null) {
				try {
					throw new RuntimeException(
							"This table doesn't appear in the annotation list"
									+ tableName);
				} catch (RuntimeException e) {
					e.printStackTrace();
					System.exit(RuntimeExceptionType.UNKNOWTABLENAME);
				}
			}

			if (dTb instanceof AosetTable || 
					dTb instanceof AusetTable)
				return this.createCrdtUniqueInsertOperation(dTb);
			else if (dTb instanceof ArsetTable)
				return this.createInsertOperation(dTb);
			else {
				try {
					throw new RuntimeException(
							"The type of CRDT table "
									+ dTb.get_CRDT_Table_Type()
									+ "is not supported by our framework or cannot be modified!");
				} catch (RuntimeException e) {
					e.printStackTrace();
					System.exit(RuntimeExceptionType.NOTDEFINEDCRDTTABLE);
				}
			}
		} else if (sqlStmt instanceof Update) {
			Update updateStatement = (Update) sqlStmt;
			String tableName = updateStatement.getTable().getName();
			DatabaseTable dTb = this.schemaParser.getTableByName(tableName);

			if (dTb == null) {
				try {
					throw new RuntimeException(
							"This table doesn't appear in the annotation list"
									+ tableName);
				} catch (RuntimeException e) {
					e.printStackTrace();
					System.exit(RuntimeExceptionType.UNKNOWTABLENAME);
				}
			}

			if (dTb instanceof ArsetTable || 
					dTb instanceof AusetTable ||
					dTb instanceof UosetTable)
				return this.createUpdateOperation(dTb, updateStatement);
			else{
				try {
					throw new RuntimeException(
							"The type of CRDT table "
									+ dTb.get_CRDT_Table_Type()
									+ "is not supported by our framework or cannot be modified!");
				} catch (RuntimeException e) {
					e.printStackTrace();
					System.exit(RuntimeExceptionType.NOTDEFINEDCRDTTABLE);
				}
			}

		} else if (sqlStmt instanceof Delete) {
			Delete deleteStatement = (Delete) sqlStmt;
			String tableName = deleteStatement.getTable().getName();
			DatabaseTable dTb = this.schemaParser.getTableByName(tableName);

			if (dTb == null) {
				try {
					throw new RuntimeException(
							"This table doesn't appear in the annotation list"
									+ tableName);
				} catch (RuntimeException e) {
					e.printStackTrace();
					System.exit(RuntimeExceptionType.UNKNOWTABLENAME);
				}
			}

			if (dTb instanceof ArsetTable)
				return this.createDeleteOperation(dTb);
			else {
				try {
					throw new RuntimeException(
							"The type of CRDT table "
									+ dTb.get_CRDT_Table_Type()
									+ "is not supported by our framework or cannot be modified!");
				} catch (RuntimeException e) {
					e.printStackTrace();
					System.exit(RuntimeExceptionType.NOTDEFINEDCRDTTABLE);
				}
			}
		} else {
			try {
				throw new RuntimeException("Could not identify the sql type "
						+ updatingQuery);
			} catch (RuntimeException e) {
				e.printStackTrace();
				System.exit(RuntimeExceptionType.UNKNOWSQLQUERY);
			}
		}
		return null;
	}
	
	//create crdt operations for all statements for a control flow graph
	/**
	 * Creates the shadow op template.
	 *
	 * @param cfg the cfg
	 * @return the shadow operation template
	 */
	public ShadowOperationTemplate createShadowOpTemplate(CFGGraph<CodeNodeIdentifier, Expression> cfg){
		List<String> allUpdatingQueries = this.findAllSqlUpdatingStatement(cfg);
		ShadowOperationTemplate shdOpTemplate = null;
		if(allUpdatingQueries.size() > 0) {
			shdOpTemplate = new ShadowOperationTemplate();
			for(int i = 0; i < allUpdatingQueries.size(); i++){
				String updatingQuery = allUpdatingQueries.get(i);
				//remove all double quotes
				updatingQuery = StringOperations.removeAllDoubleQuotesAndPlus(updatingQuery);
				updatingQuery = this.replaceContentAfterEqualSignFromUpdateQuery(updatingQuery);
				updatingQuery = this.replaceContentAfterValuesSignFromInsertQuery(updatingQuery);
				//System.out.println("analyze " + updatingQuery);
				Operation op = this.createCrdtOpForStatement(updatingQuery);
				shdOpTemplate.addOperation(op);
			}
		}else {
			Debug.println("No update sql statement are found");
		}
		return shdOpTemplate;
	}
	
	/**
	 * Replace content after equal sign from update query.
	 *
	 * @param updatingQuery the updating query
	 * @return the string
	 */
	public String replaceContentAfterEqualSignFromUpdateQuery(String updatingQuery) {
		if(updatingQuery.startsWith("UPDATE") ||
				updatingQuery.startsWith("update")) {
			String updatingQueryCopy = updatingQuery.toUpperCase();
			String _strAfterSetBeforeWhere = updatingQuery.substring(updatingQueryCopy.indexOf("SET") + 3,
					updatingQueryCopy.indexOf("WHERE"));
			String[] subStrs = _strAfterSetBeforeWhere.split(",");
			String equalStr = "";
			for(int i = 0; i < subStrs.length ; i++) {
				String _str = subStrs[i];
				if(_str.contains("=")) {
					_str = _str.replaceAll("\\=.*", "\\=\\?");
					if(i == 0) {
						equalStr += _str;
					}else {
						equalStr +="," + _str;
					}
				}
			}
			updatingQuery = updatingQuery.substring(0, updatingQueryCopy.indexOf("SET") + 3) +" " +
				equalStr + updatingQuery.substring(updatingQueryCopy.indexOf("WHERE"));
			return updatingQuery;
		}else {
			return updatingQuery;
		}
	}
	
	public String replaceContentAfterValuesSignFromInsertQuery(String updatingQuery) {
		if(updatingQuery.startsWith("INSERT") ||
				updatingQuery.startsWith("insert")) {
			String valueStr = updatingQuery.substring(updatingQuery.indexOf("VALUES (") + 8, updatingQuery.lastIndexOf(')'));
			int numOfFields = valueStr.split(",").length;
			assert(numOfFields > 0);
			updatingQuery = updatingQuery.substring(0, updatingQuery.indexOf("VALUES (") + 8);
			for(int i = 0; i < numOfFields; i++) {
				updatingQuery += "?,";
			}
			updatingQuery = updatingQuery.substring(0, updatingQuery.length() - 1) + ")";
		}
		return updatingQuery;
	}
	
	public void addStatisInfo(String methodName, int numOfTemplate) {
		if(!this.statisMap.containsKey(methodName)) {
			PathTemplateNumPair pTp = new PathTemplateNumPair(0,0);
			this.statisMap.put(methodName, pTp);
		}
		PathTemplateNumPair p = this.statisMap.get(methodName);
		p.addPathNum(1);
		p.addTemplateNum(numOfTemplate);
	}
	
	/**
	 * Creates the all shadow op templates.
	 *
	 * @return the list
	 */
	public List<ShadowOperationTemplate> createAllShadowOpTemplates(){
		Debug.println("----------------------------->create templates from reduced cfgs");
		List<ShadowOperationTemplate> templateList = new ArrayList<ShadowOperationTemplate>();
		for(CFGGraph<CodeNodeIdentifier, Expression> cfgGraph : this.getCfgList()){
			Debug.println("----------> analyze one cfg");
			ShadowOperationTemplate shdOpTemplate = this.createShadowOpTemplate(cfgGraph);
			if(shdOpTemplate != null) {
				templateList.add(shdOpTemplate);
				this.addStatisInfo(cfgGraph.getCfgIdentifierPlainText(), 1);
			}else {
				Debug.println("template is empty");
				this.addStatisInfo(cfgGraph.getCfgIdentifierPlainText(), 0);
			}
			Debug.println("<---------- finish analyzing one cfg");
		}
		return templateList;
	}
	
	/**
	 * Creates the all table variables.
	 *
	 * @param tableNameList the table name list
	 * @return the list
	 */
	private List<SingleVariableDeclaration> createAllTableVariables(HashSet<String> tableNameList){
		List<SingleVariableDeclaration> varList = new ArrayList<SingleVariableDeclaration>();
		for(String tableName : tableNameList){
			String tableClassName = tableName + DatabaseTableClassCreator.getTableSufixString();
			SingleVariableDeclaration varDecl = super.createVariableDeclaration(tableClassName,
					tableClassName.toLowerCase(), false);
			varList.add(varDecl);
		}
		return varList;
	}
	
	/**
	 * Creates the statements for unique insert operation.
	 *
	 * @param op the op
	 * @return the linked hash map
	 */
	private LinkedHashMap<Statement, List<SingleVariableDeclaration>> createStatementsForUniqueInsertOperation(UniqueInsertOperation op){
		LinkedHashMap<Statement, List<SingleVariableDeclaration>> returnMap = new LinkedHashMap<Statement, List<SingleVariableDeclaration>>();
		List<SingleVariableDeclaration> varList = new ArrayList<SingleVariableDeclaration>();
		String tableInstanceName = (op.getTableName() + DatabaseTableClassCreator.getTableSufixString()).toLowerCase();
		String recordType = op.getTableName() + DatabaseRecordClassCreator.getRecordSufixString();
		String recordName = op.getUniqueParameterName();
		SingleVariableDeclaration recordVar = super.createVariableDeclaration(recordType, recordName, false);
		varList.add(recordVar);
		
		MethodInvocation methodInvocation = super.getASTNode().newMethodInvocation();
		methodInvocation.setExpression(super.getASTNode().newSimpleName(tableInstanceName));
		methodInvocation.setName(super.getASTNode().newSimpleName("uniqueInsert"));
		methodInvocation.arguments().add(super.getASTNode().newSimpleName(recordName));
		
		ExpressionStatement expSt = super.getASTNode().newExpressionStatement(methodInvocation);
		returnMap.put(expSt, varList);
		return returnMap;
	}
	
	/**
	 * Creates the statements for insert operation.
	 *
	 * @param op the op
	 * @return the linked hash map
	 */
	private LinkedHashMap<Statement, List<SingleVariableDeclaration>> createStatementsForInsertOperation(InsertOperation op){
		LinkedHashMap<Statement, List<SingleVariableDeclaration>> returnMap = new LinkedHashMap<Statement, List<SingleVariableDeclaration>>();
		List<SingleVariableDeclaration> varList = new ArrayList<SingleVariableDeclaration>();
		String tableInstanceName = (op.getTableName() + DatabaseTableClassCreator.getTableSufixString()).toLowerCase();
		String recordType = op.getTableName() + DatabaseRecordClassCreator.getRecordSufixString();
		String recordName = op.getUniqueParameterName();
		SingleVariableDeclaration recordVar = super.createVariableDeclaration(recordType, recordName, false);
		varList.add(recordVar);
		
		MethodInvocation methodInvocation = super.getASTNode().newMethodInvocation();
		methodInvocation.setExpression(super.getASTNode().newSimpleName(tableInstanceName));
		methodInvocation.setName(super.getASTNode().newSimpleName("insert"));
		methodInvocation.arguments().add(super.getASTNode().newSimpleName(recordName));
		
		ExpressionStatement expSt = super.getASTNode().newExpressionStatement(methodInvocation);
		returnMap.put(expSt, varList);
		return returnMap;
	}
	
	/**
	 * Creates the statements for update operation.
	 *
	 * @param op the op
	 * @return the linked hash map
	 */
	private LinkedHashMap<Statement, List<SingleVariableDeclaration>> createStatementsForUpdateOperation(UpdateOperation op){
		LinkedHashMap<Statement, List<SingleVariableDeclaration>> returnMap = new LinkedHashMap<Statement, List<SingleVariableDeclaration>>();
		List<SingleVariableDeclaration> varList = new ArrayList<SingleVariableDeclaration>();
		String tableInstanceName = (op.getTableName() + DatabaseTableClassCreator.getTableSufixString()).toLowerCase();
		String recordType = op.getTableName() + DatabaseRecordClassCreator.getRecordSufixString();
		String recordName = op.getUniqueParameterName();
		
		//first get this record
		MethodInvocation methodInvocation = super.getASTNode().newMethodInvocation();
		methodInvocation.setExpression(super.getASTNode().newSimpleName(tableInstanceName));
		methodInvocation.setName(super.getASTNode().newSimpleName("getRecord"));
		//get primary key from table
		List<DataField> pkList = this.schemaParser.getTableByName(op.getTableName()).getPrimaryKeyDataFieldList();
		for(DataField pk : pkList){
			String paraName = op.getUniqueParameterName();
			methodInvocation.arguments().add(super.getASTNode().newSimpleName(paraName));
			SingleVariableDeclaration var = super.createVariableDeclaration(CrdtFactory.getProperCrdtObject(pk.get_Crdt_Data_Type(), pk.get_Data_Type()), 
					paraName, false);
			varList.add(var);
		}
		VariableDeclarationStatement varDeclStmt = super.createVariableDeclarationStatement(recordType, recordName, false, methodInvocation);
		returnMap.put(varDeclStmt, varList);
		
		List<DataField> modifiedDfList = op.getModifiedDataFields();
		for(DataField df : modifiedDfList){
			List<SingleVariableDeclaration> newVarList = new ArrayList<SingleVariableDeclaration>();
			String paraName = op.getUniqueParameterName();
			MethodInvocation recordMInvoc = super.getASTNode().newMethodInvocation();
			recordMInvoc.setExpression(super.getASTNode().newSimpleName(recordName));
			recordMInvoc.setName(super.getASTNode().newSimpleName("update"+df.get_Data_Field_Name()));
			if(CrdtFactory.isLwwType(df.get_Crdt_Data_Type())){
				recordMInvoc.arguments().add(super.getASTNode().newSimpleName(paraName));
				recordMInvoc.arguments().add(super.getASTNode().newSimpleName(LWWLTS_VALUE));
			}else{
				recordMInvoc.arguments().add(super.getASTNode().newSimpleName(paraName));
			}
			SingleVariableDeclaration var = super.createVariableDeclaration(CrdtFactory.getProperCrdtObject(df.get_Crdt_Data_Type(), df.get_Data_Type()), 
					paraName, false);
			newVarList.add(var);
			
			ExpressionStatement recordExpSt = super.getASTNode().newExpressionStatement(recordMInvoc);
			returnMap.put(recordExpSt, newVarList);
		}
		//update deleteflag
		if(this.schemaParser.getTableByName(op.getTableName()) instanceof ArsetTable) {
			List<SingleVariableDeclaration> flagVarList = new ArrayList<SingleVariableDeclaration>();
			MethodInvocation recordFlagMInvoc = super.getASTNode().newMethodInvocation();
			recordFlagMInvoc.setExpression(super.getASTNode().newSimpleName(recordName));
			recordFlagMInvoc.setName(super.getASTNode().newSimpleName("undelete"+this.schemaParser.getTableByName(op.getTableName()).getDeletedFlag().get_Data_Field_Name()));
			recordFlagMInvoc.arguments().add(super.getASTNode().newSimpleName(LWWLTS_VALUE));
			ExpressionStatement flagExpSt = super.getASTNode().newExpressionStatement(recordFlagMInvoc);
			returnMap.put(flagExpSt, flagVarList);
		}
		
		//update lww
		List<SingleVariableDeclaration> flagVarList = new ArrayList<SingleVariableDeclaration>();
		MethodInvocation recordFlagMInvoc = super.getASTNode().newMethodInvocation();
		recordFlagMInvoc.setExpression(super.getASTNode().newSimpleName(recordName));
		recordFlagMInvoc.setName(super.getASTNode().newSimpleName("update"+this.schemaParser.getTableByName(op.getTableName()).getLwwTs().get_Data_Field_Name()));
		recordFlagMInvoc.arguments().add(super.getASTNode().newSimpleName(LWWLTS_VALUE));
		ExpressionStatement flagExpSt = super.getASTNode().newExpressionStatement(recordFlagMInvoc);
		returnMap.put(flagExpSt, flagVarList);
		return returnMap;
	}
	
	/**
	 * Creates the statements for delete operation.
	 *
	 * @param op the op
	 * @return the linked hash map
	 */
	private LinkedHashMap<Statement, List<SingleVariableDeclaration>> createStatementsForDeleteOperation(DeleteOperation op){
		LinkedHashMap<Statement, List<SingleVariableDeclaration>> returnMap = new LinkedHashMap<Statement, List<SingleVariableDeclaration>>();
		List<SingleVariableDeclaration> varList = new ArrayList<SingleVariableDeclaration>();
		String tableInstanceName = (op.getTableName() + DatabaseTableClassCreator.getTableSufixString()).toLowerCase();
		
		MethodInvocation methodInvocation = super.getASTNode().newMethodInvocation();
		methodInvocation.setExpression(super.getASTNode().newSimpleName(tableInstanceName));
		methodInvocation.setName(super.getASTNode().newSimpleName("delete"));
		//get primary key from table
		List<DataField> pkList = this.schemaParser.getTableByName(op.getTableName()).getPrimaryKeyDataFieldList();
		for(DataField pk : pkList){
			String paraName = op.getUniqueParameterName();
			methodInvocation.arguments().add(super.getASTNode().newSimpleName(paraName));
			SingleVariableDeclaration var = super.createVariableDeclaration(CrdtFactory.getProperCrdtObject(pk.get_Crdt_Data_Type(), pk.get_Data_Type()), 
					paraName, false);
			varList.add(var);
		}
		methodInvocation.arguments().add(super.getASTNode().newSimpleName(LWWLTS_VALUE));
		ExpressionStatement expSt = super.getASTNode().newExpressionStatement(methodInvocation);
		returnMap.put(expSt, varList);
		return returnMap;
	}
	
	/**
	 * Generate code for shadow op template.
	 *
	 * @param shdOpTemplate the shd op template
	 * @return the method declaration
	 */
	private MethodDeclaration generateCodeForShadowOpTemplate(ShadowOperationTemplate shdOpTemplate){
		String templateName = shdOpTemplate.getUniqueTemplateId();
		MethodDeclaration methodDeclaration = super.createMethodDeclaration(PrimitiveType.VOID,
				templateName, ModifierKeyword.PUBLIC_KEYWORD);
		String requireSpecs = JahobSpecsUtil.requirePrefix;
		String modifySpecs = JahobSpecsUtil.modifyPrefix;
		String ensureSpecs = JahobSpecsUtil.ensurePrefix;
		org.eclipse.jdt.core.dom.Block block = super.getASTNode().newBlock();
		List<Operation> opList = shdOpTemplate.getCrdtOpList();
		HashSet<String> tableNameList = new HashSet<String>();
		List<Statement> statementList = new ArrayList<Statement>();
		List<SingleVariableDeclaration> varList = new ArrayList<SingleVariableDeclaration>();
		List<String> insertTables = new ArrayList<String>();
		List<String> deleteTables = new ArrayList<String>();
		List<DataField> updateFields = new ArrayList<DataField>();
		for(int i = 0 ; i < opList.size(); i ++){
			Operation op = opList.get(i);
			tableNameList.add(op.getTableName());
			LinkedHashMap<Statement, List<SingleVariableDeclaration>> returnValue = null;
			if(op instanceof UniqueInsertOperation){
				returnValue = this.createStatementsForUniqueInsertOperation((UniqueInsertOperation) op);
				insertTables.add(op.getTableName());
			}else if(op instanceof InsertOperation){
				returnValue = this.createStatementsForInsertOperation((InsertOperation) op);
				insertTables.add(op.getTableName());
				updateFields.addAll(op.getTableInstance().getModifiableDataFieldList());
			}else if(op instanceof UpdateOperation){
				returnValue = this.createStatementsForUpdateOperation((UpdateOperation) op);
				updateFields.addAll(((UpdateOperation) op).getModifiedDataFields());
			}else if(op instanceof DeleteOperation){
				returnValue = this.createStatementsForDeleteOperation((DeleteOperation) op);
				deleteTables.add(op.getTableName());
			}else{
				throw new RuntimeException("can not be here");
			}
			
			Iterator<Entry<Statement, List<SingleVariableDeclaration>>> it = returnValue.entrySet().iterator();
			while(it.hasNext()){
				Entry<Statement, List<SingleVariableDeclaration>> itEntry = it.next();
				statementList.add(itEntry.getKey());
				varList.addAll(itEntry.getValue());
			}
		}
		//get table vars
		List<String> missingTables = JahobSpecsUtil.findMissingTableForInvariants(tableNameList, 
				this.getInvParser().getAllTables());
		tableNameList.addAll(missingTables);
		List<SingleVariableDeclaration> tableVars = this.createAllTableVariables(tableNameList);
		//requireSpecs += JahobSpecsUtil.getAllTableInitAndNotNull(tableVars);
		String invSpecs =  JahobSpecsUtil.getInvSpecs(this.getInvParser().getInvariants(), tableVars);
		tableVars.addAll(varList);
		//requireSpecs += " & " + JahobSpecsUtil.getAllArgumentsNotNull(varList);
		for(SingleVariableDeclaration var : tableVars){
			methodDeclaration.parameters().add(var);
		}
		//add the logical timestamp var
		SingleVariableDeclaration lwwLts = super.createVariableDeclaration(CrdtFactory.getLwwLogicalTimestampCrdtTypeString(), LWWLTS_VALUE, false);
		methodDeclaration.parameters().add(lwwLts);
		block.statements().addAll(statementList);
		methodDeclaration.setBody(block);
		
		List<String> specs = new ArrayList<String>();
		//requireSpecs += " & " + JahobSpecsUtil.getRequireNotNullClause(LWWLTS_VALUE);
		//requireSpecs += " & " +invSpecs + "\"";
		requireSpecs += invSpecs + "\"";
		modifySpecs += JahobSpecsUtil.getModifyClauseForTemplate(insertTables, deleteTables, updateFields);
		specs.add(requireSpecs);
		specs.add(modifySpecs);
		ensureSpecs +=  invSpecs + "\"";
		specs.add(ensureSpecs);
		this.methodSpecs.put(templateName, specs);
		return methodDeclaration;
	}
	
	/* (non-Javadoc)
	 * @see staticanalysis.templatecreator.SourceCodeGenerator#generateCode()
	 */
	/**
	 * Generate code.
	 *
	 * @see staticanalysis.templatecreator.SourceCodeGenerator#generateCode()
	 */
	@Override
	public void generateCode() {
		//first generate a class type
		TypeDeclaration type = this.createClass();

		List<ShadowOperationTemplate> templateList = this.createAllShadowOpTemplates();
		this.tList = templateList;
		for(int i = 0; i < templateList.size(); i++){
			ShadowOperationTemplate shdOpTemplate = templateList.get(i);
			Debug.println("create template now");
			MethodDeclaration methodDecl = this.generateCodeForShadowOpTemplate(shdOpTemplate);
			type.bodyDeclarations().add(methodDecl);
		}
		super.addTypeDeclaration(type);
		super.writeToFile();
		
		//append specs
		super.reloadFileToFileContents();
		this.appendAllSpecs();
		super.writeToFileFromFileContents();
	}
	
	private String getWPFileName() {
		return this.getProjectName() + "_sieve_input.txt";
	}
	
	private String getWPFileFullPath() {
		String fileName = this.getWPFileName();
		return this.getCodePath() + "/" + fileName;
	}
	
	private void generateSieveInput() {
		String filePath = this.getWPFileFullPath();
		List<String> outputStrs = new ArrayList<String>();
		for(int i = 0; i < this.tList.size(); i++) {
			ShadowOperationTemplate shdOpTemplate = this.tList.get(i);
			shdOpTemplate.setSignature();
			//generate the signature
			String fingerPrint = shdOpTemplate.getFingerPrint();
			//read the file output by Jahob
			String wpFileName = this.getCodePath() + "/" +"sifter_shadowOptemplate_template" + shdOpTemplate.getShdOpTemplateId() + ".vc";
			//generate a key pair (signature, formula)
			String wpStr = FileOperations.readFirstLineFromFile(wpFileName);
			outputStrs.add(fingerPrint + "\n" +StaticFPtoWPsStore.openerOfWeakestPrecondition + wpStr + "\n");
		}
		FileOperations.createFileByGivenFilePath(filePath);
		FileOperations.writeToFile(filePath, outputStrs);
	}
	
	public void printOutStatis() {
		Iterator it = this.statisMap.entrySet().iterator();
		int numOfTotalPath = 0;
		int numOfTotalTemplate = 0;
		while(it.hasNext()) {
			Map.Entry<String, PathTemplateNumPair> en = (Entry<String, PathTemplateNumPair>) it.next();
			String outputStr = en.getKey() + " & " + en.getValue().getNumOfPath() + " & " + en.getValue().getNumOfTemplate();
			numOfTotalPath += en.getValue().getNumOfPath();
			numOfTotalTemplate += en.getValue().getNumOfTemplate();
			System.out.println(outputStr);
		}
		System.out.println("Generating " + numOfTotalPath + " paths " + numOfTotalTemplate + " templates");
	}

	/* (non-Javadoc)
	 * @see staticanalysis.templatecreator.SourceCodeGenerator#createConstructorDeclaration()
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
		return null;
	}

	/* (non-Javadoc)
	 * @see staticanalysis.templatecreator.SourceCodeGenerator#createFields()
	 */
	/**
	 * Creates the fields.
	 *
	 * @return the list
	 * @see staticanalysis.templatecreator.SourceCodeGenerator#createFields()
	 */
	@Override
	public List<FieldDeclaration> createFields() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see staticanalysis.templatecreator.SourceCodeGenerator#createFunctions()
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
		return null;
	}

	/**
	 * Gets the schema parser.
	 *
	 * @return the schema parser
	 */
	public SchemaParser getSchemaParser() {
		return schemaParser;
	}

	/**
	 * Sets the schema parser.
	 *
	 * @param schemaParser the new schema parser
	 */
	public void setSchemaParser(SchemaParser schemaParser) {
		this.schemaParser = schemaParser;
	}
	
	/**
	 * Append all specs.
	 */
	public void appendAllSpecs() {
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
	 * @see staticanalysis.templatecreator.SourceCodeGenerator#generateAndExecuteJahobCommand()
	 */
	@Override
	public void generateAndExecuteJahobCommand() {
		List<String> commands = new ArrayList<String>();
		commands.add(this.getJahobCommand());
		String shellScript = FileOperations.createShellScript(this.getCodePath(), "run"+this.getClassName(), commands);
		FileOperations.executeShellCommands(shellScript);
		this.generateSieveInput();
	}

	/* (non-Javadoc)
	 * @see staticanalysis.templatecreator.SourceCodeGenerator#getJahobCommand()
	 */
	@Override
	public String getJahobCommand() {
		return JahobSpecsUtil.getTableVerificationExecCommandForTemplate(this.getFullFilePath(), 
				this.getCodePath(), this.schemaParser);
	}

	/**
	 * Gets the inv parser.
	 *
	 * @return the invParser
	 */
	public InvariantParser getInvParser() {
		return invParser;
	}

	/**
	 * Sets the inv parser.
	 *
	 * @param invParser the invParser to set
	 */
	public void setInvParser(InvariantParser invParser) {
		this.invParser = invParser;
	}
	
	/** This class is used to store the path and template numbers for a method*/
	public class PathTemplateNumPair{
		int numOfPath;
		int numOfTemplate;
		
		PathTemplateNumPair( int nOfPath, int nOfTemplate){
			this.numOfPath = nOfPath;
			this.numOfTemplate = nOfTemplate;
		}
		
		public void addPathNum(int n) {
			this.numOfPath = this.numOfPath + n;
		}
		
		public void addTemplateNum(int t) {
			this.numOfTemplate = this.numOfTemplate + t;
		}
		
		public int getNumOfPath() {
			return this.numOfPath;
		}
		
		public int getNumOfTemplate() {
			return this.numOfTemplate;
		}
	}
	
}

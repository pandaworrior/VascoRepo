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

import java.util.ArrayList;
import java.util.List;

import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.ConditionalExpr;
import japa.parser.ast.expr.EnclosedExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.visitor.GenericVisitorAdapter;
import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGGraph;
import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGNode;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class ExpressionVisitor.
 *
 * @author chengli
 */
public class ExpressionVisitor
		extends
		GenericVisitorAdapter<CFGGraph<CodeNodeIdentifier, Expression>, MethodIdentifier> {

	/**
	 * Create a sub control flow graph for an assignment expression. If the
	 * parsing the value of the assignment returns a null, then create a control
	 * flow graph with a node, which represents the assignment itself.
	 * Otherwise, create a control flow graph, and put it follow the one
	 * returned.
	 *
	 * @param assignExp the assign exp
	 * @param methodId the m id
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(AssignExpr assignExp,
			MethodIdentifier methodId) {
		Debug.println("target: " + assignExp.getTarget().toString());
		Debug.println("value: " + assignExp.getValue().toString());
		Debug.println("operator: " + assignExp.getOperator().toString());
		CFGGraph<CodeNodeIdentifier, Expression> cfg = new CFGGraph<CodeNodeIdentifier, Expression>();
		CodeNodeIdentifier cnId = new CodeNodeIdentifier(
				methodId.getPackageName(), methodId.getClassName(),
				methodId.getMethodName(), assignExp.getBeginLine(),
				assignExp.getBeginColumn(), assignExp.getEndLine(),
				assignExp.getEndColumn());
		CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
				cnId, assignExp);
		cfg.addEntryNode(cfgNode);
		cfg.addExitNode(cfgNode);
		CFGGraph<CodeNodeIdentifier, Expression> subCfg = ExpressionParser
				.dispatchToExpressionVisitor(assignExp.getValue(), methodId);
		if (subCfg == null) {
			return cfg;
		} else {
			subCfg.mergeWithOtherControlFlowGraph(cfg);
			return subCfg;
		}
	}
	
	/**
	 * Create a sub control flow graph for a conditional expression 
	 * (((d1 > 9) ? ('A' + d1 - 10) : '0' + d1)). 
	 *
	 * @param methodCallExp the conditional expression
	 * @param methodId the m id
	 * @return the cFG graph
	 */
	
	public CFGGraph<CodeNodeIdentifier, Expression> visit(ConditionalExpr conExp,
			MethodIdentifier methodId) {
		Expression conditionExpr = conExp.getCondition();
		Expression thenExpr = conExp.getThenExpr();
		Expression elseExpr = conExp.getElseExpr();
		CFGGraph<CodeNodeIdentifier, Expression> cfg = ExpressionParser.dispatchToExpressionVisitor(
				conditionExpr, methodId);
		CFGGraph<CodeNodeIdentifier, Expression> cfg1 = ExpressionParser.dispatchToExpressionVisitor(
				thenExpr, methodId);
		CFGGraph<CodeNodeIdentifier, Expression> cfg2 = ExpressionParser.dispatchToExpressionVisitor(
				elseExpr, methodId);
		List<CFGGraph<CodeNodeIdentifier, Expression>> subCfgList = new ArrayList<CFGGraph<CodeNodeIdentifier, Expression>>();
		subCfgList.add(cfg1);
		subCfgList.add(cfg2);
		cfg.mergeWithOtherControlFlowGraphs(subCfgList);
		return cfg;
	}

	/**
	 * Create a sub control flow graph for a method call expression. If there is
	 * not parameters for this method call, then create a control flow graph
	 * with a node that is the call expression. Otherwise,
	 *
	 * @param methodCallExp the method call exp
	 * @param methodId the m id
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(
			MethodCallExpr methodCallExp, MethodIdentifier methodId) {
		Debug.println("name: " + methodCallExp.getName());
		if(methodCallExp.getScope() != null){
			Debug.println("scope: " + methodCallExp.getScope().toString());
		}
		List<Type> typeArgs = methodCallExp.getTypeArgs();
		if (typeArgs != null) {
			for (Type typeArg : typeArgs) {
				Debug.println("typeargs: " + typeArg.toString());
			}
		}

		CFGGraph<CodeNodeIdentifier, Expression> cfg = new CFGGraph<CodeNodeIdentifier, Expression>();
		CodeNodeIdentifier cnId = new CodeNodeIdentifier(
				methodId.getPackageName(), methodId.getClassName(),
				methodId.getMethodName(), methodCallExp.getBeginLine(),
				methodCallExp.getBeginColumn(), methodCallExp.getEndLine(),
				methodCallExp.getEndColumn());
		CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
				cnId, methodCallExp);
		cfg.addEntryNode(cfgNode);
		cfg.addExitNode(cfgNode);

		List<CFGGraph<CodeNodeIdentifier, Expression>> subCfgList = new ArrayList<CFGGraph<CodeNodeIdentifier, Expression>>();
		List<Expression> args = methodCallExp.getArgs();
		if (args != null) {
			for (Expression e : args) {
				Debug.println("args: " + e.toString());
				CFGGraph<CodeNodeIdentifier, Expression> subCfg = ExpressionParser
						.dispatchToExpressionVisitor(e, methodId);
				if (subCfg != null) {
					subCfgList.add(subCfg);
				}
			}
		}
		if (subCfgList.isEmpty()) {
			return cfg;
		} else {
			cfg.mergeControlFlowGraphsCasacading(subCfgList);
		}
		return cfg;
	}

	/**
	 * Visit NameExpr node.
	 *
	 * @param n the NameExpr
	 * @param methodId the method identifier
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(NameExpr n,
			MethodIdentifier methodId) {
		Debug.println("name: " + n.getName());
		return null;
	}

	/**
	 * Create a sub control flow graph for an object creation expr. 
	 * 
	 * @param objCExp the obj creation exp
	 * @param methodId the method identifier
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(
			ObjectCreationExpr objCExp, MethodIdentifier methodId) {
		// testObjectCreationExpr((ObjectCreationExpr)exp);
		CFGGraph<CodeNodeIdentifier, Expression> cfg = null;
		cfg = new CFGGraph<CodeNodeIdentifier, Expression>();
		CodeNodeIdentifier cnId = new CodeNodeIdentifier(
				methodId.getPackageName(), methodId.getClassName(),
				methodId.getMethodName(), objCExp.getBeginLine(),
				objCExp.getBeginColumn(), objCExp.getEndLine(),
				objCExp.getEndColumn());
		CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
				cnId, objCExp);
		cfg.addEntryNode(cfgNode);
		cfg.addExitNode(cfgNode);
		
		List<CFGGraph<CodeNodeIdentifier, Expression>> subCfgList = new ArrayList<CFGGraph<CodeNodeIdentifier, Expression>>();
		List<Expression> expList = ((ObjectCreationExpr) objCExp).getArgs();
		if (expList != null) {
			for (Expression e : expList) {
				CFGGraph<CodeNodeIdentifier, Expression> subCfg = ExpressionParser
						.dispatchToExpressionVisitor(e, methodId);
				if (subCfg != null) {
					subCfgList.add(subCfg);
				}
			}
		}
		
		//first check all arguments and merge with the main control flow graph
		if (!subCfgList.isEmpty()) {
			cfg.mergeControlFlowGraphsCasacading(subCfgList);
		}
		
		//check body declarations
		List<BodyDeclaration> bodyDecls = objCExp.getAnonymousClassBody();
		if(bodyDecls != null){
			for(BodyDeclaration bodyDecl : bodyDecls){
				if (bodyDecl instanceof MethodDeclaration) {
					CFGGraph<CodeNodeIdentifier, Expression> bodySubCfg = new MethodVisitor()
					.visit((MethodDeclaration)bodyDecl, methodId);
					//merge with the current one
					cfg.mergeWithControlFlowPrecedingSpecificNode(cfgNode, bodySubCfg);
				}
			}
		}
		return cfg;
	}

	/**
	 * Create a sub control flow graph for a variable declaration.
	 *
	 * @param varDeclaExp the var declaration exp
	 * @param methodId the method identifier
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(
			VariableDeclarationExpr varDeclaExp, MethodIdentifier methodId) {
		Debug.println("handle the variableDeclarationExpr");
		Type declarType = varDeclaExp.getType();
		if(declarType instanceof PrimitiveType){
			Debug.println("Primitive type");
		}else{
			if(declarType instanceof ClassOrInterfaceType){
				Debug.println("ClassOrInterfaceType");
			}else{
				if(declarType instanceof ReferenceType){
					Debug.println("ReferenceType");
				}
			}
		}
		Debug.println("Type: " + declarType.toString());
		List<CFGGraph<CodeNodeIdentifier, Expression>> subCfgList = new ArrayList<CFGGraph<CodeNodeIdentifier, Expression>>();
		List<VariableDeclarator> vars = varDeclaExp.getVars();
		if (vars != null) {
			for (VariableDeclarator var : vars) {
				Expression objectExp = var.getInit();
				if (objectExp != null) {
					CFGGraph<CodeNodeIdentifier, Expression> subCfg = ExpressionParser
							.dispatchToExpressionVisitor(objectExp, methodId);
					if (subCfg != null) {
						subCfgList.add(subCfg);
					}
					Debug.println("expr: "
							+ var.getInit().toString());
				}
			}
		}
		CFGGraph<CodeNodeIdentifier, Expression> cfg = new CFGGraph<CodeNodeIdentifier, Expression>();
		CodeNodeIdentifier cnId = new CodeNodeIdentifier(
				methodId.getPackageName(), methodId.getClassName(),
				methodId.getMethodName(), varDeclaExp.getBeginLine(),
				varDeclaExp.getBeginColumn(), varDeclaExp.getEndLine(),
				varDeclaExp.getEndColumn());
		CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
				cnId, varDeclaExp);
		cfg.addEntryNode(cfgNode);
		cfg.addExitNode(cfgNode);
		if (!subCfgList.isEmpty()) {
			cfg.mergeControlFlowGraphsCasacading(subCfgList);
		}
		return cfg;
	}
	
	
	/**
	 * Visit an Unary Expression statement.
	 *
	 * @param unaryExp the unary exp
	 * @param methodId the method id
	 * @return the cFG graph
	 * @see japa.parser.ast.visitor.GenericVisitorAdapter#visit(japa.parser.ast.expr.UnaryExpr, java.lang.Object)
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(UnaryExpr unaryExp,
			MethodIdentifier methodId) {
		CFGGraph<CodeNodeIdentifier, Expression> subCfg = null;
		Expression exp = unaryExp.getExpr();
		if(exp != null){
			subCfg = ExpressionParser
					.dispatchToExpressionVisitor(exp, methodId);
		}
		CFGGraph<CodeNodeIdentifier, Expression> cfg = new CFGGraph<CodeNodeIdentifier, Expression>();
		CodeNodeIdentifier cnId = new CodeNodeIdentifier(
				methodId.getPackageName(), methodId.getClassName(),
				methodId.getMethodName(), unaryExp.getBeginLine(),
				unaryExp.getBeginColumn(), unaryExp.getEndLine(),
				unaryExp.getEndColumn());
		CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
				cnId, unaryExp);
		cfg.addEntryNode(cfgNode);
		cfg.addExitNode(cfgNode);
		if(subCfg != null){
			subCfg.mergeWithOtherControlFlowGraph(cfg);
			return subCfg;
		}
		return cfg;
	}
	
	/**
	 * Visit a binary expression statement.
	 *
	 * @param binaryExpr the binary expr
	 * @param methodId the method id
	 * @return the cFG graph
	 * @see japa.parser.ast.visitor.GenericVisitorAdapter#visit(japa.parser.ast.expr.BinaryExpr, java.lang.Object)
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(BinaryExpr binaryExpr,
			MethodIdentifier methodId) {
		Debug.println("left " + binaryExpr.getLeft().toString());
		Debug.println("Operator " + binaryExpr.getOperator().toString());
		Debug.println("right " + binaryExpr.getRight().toString());
		
		Expression leftExpr = binaryExpr.getLeft();
		Expression rightExpr = binaryExpr.getRight();
		CFGGraph<CodeNodeIdentifier, Expression> leftCfg = null;
		CFGGraph<CodeNodeIdentifier, Expression> rightCfg = null;
		List<CFGGraph<CodeNodeIdentifier, Expression>> subCfgList = new ArrayList<CFGGraph<CodeNodeIdentifier, Expression>>();
		if(leftExpr != null){
			leftCfg = ExpressionParser
					.dispatchToExpressionVisitor(leftExpr, methodId);
			if(leftCfg != null){
				subCfgList.add(leftCfg);
			}
		}
		if(rightExpr != null){
			rightCfg = ExpressionParser
					.dispatchToExpressionVisitor(rightExpr, methodId);
			if(rightCfg != null){
				subCfgList.add(rightCfg);
			}
		}
		
		CFGGraph<CodeNodeIdentifier, Expression> cfg = new CFGGraph<CodeNodeIdentifier, Expression>();
		CodeNodeIdentifier cnId = new CodeNodeIdentifier(
				methodId.getPackageName(), methodId.getClassName(),
				methodId.getMethodName(), binaryExpr.getBeginLine(),
				binaryExpr.getBeginColumn(), binaryExpr.getEndLine(),
				binaryExpr.getEndColumn());
		CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
				cnId, binaryExpr);
		cfg.addEntryNode(cfgNode);
		cfg.addExitNode(cfgNode);
		
		if (!subCfgList.isEmpty()) {
			cfg.mergeControlFlowGraphsCasacading(subCfgList);
		}
		
		return cfg;
	}
	
	/**
	 * Obtain control flow graph for a cast statement.
	 *
	 * @param castExpr cast expression
	 * @param methodId method identifier
	 * @return cfg control flow graph
	 * @see japa.parser.ast.visitor.GenericVisitorAdapter#visit(japa.parser.ast.expr.CastExpr, java.lang.Object)
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(CastExpr castExpr,
			MethodIdentifier methodId) {
		CFGGraph<CodeNodeIdentifier, Expression> cfg = new CFGGraph<CodeNodeIdentifier, Expression>();
		CodeNodeIdentifier cnId = new CodeNodeIdentifier(
				methodId.getPackageName(), methodId.getClassName(),
				methodId.getMethodName(), castExpr.getBeginLine(),
				castExpr.getBeginColumn(), castExpr.getEndLine(),
				castExpr.getEndColumn());
		CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
				cnId, castExpr);
		cfg.addEntryNode(cfgNode);
		cfg.addExitNode(cfgNode);
		
		Expression exp = castExpr.getExpr();
		CFGGraph<CodeNodeIdentifier, Expression> subCfg = ExpressionParser.dispatchToExpressionVisitor(exp, methodId);
		if(subCfg != null){
			subCfg.mergeWithOtherControlFlowGraph(cfg);
			return subCfg;
		}
		return cfg;
	}
	
	/**
	 * Obtain control flow graph for enclosed expression
	 * @see japa.parser.ast.visitor.GenericVisitorAdapter#visit(japa.parser.ast.expr.EnclosedExpr, java.lang.Object)
	 * @param enclosedExpr Enclosed Expression
	 * @param methodId method identifier
	 * @return cfg control flow graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(EnclosedExpr enclosedExpr,
			MethodIdentifier methodId) {
		Expression innerExpr = enclosedExpr.getInner();
		CFGGraph<CodeNodeIdentifier, Expression> cfg = ExpressionParser.dispatchToExpressionVisitor(innerExpr, methodId);
		return cfg;
	}

}

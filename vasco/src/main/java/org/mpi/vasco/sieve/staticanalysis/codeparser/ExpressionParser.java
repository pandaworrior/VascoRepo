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

import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGGraph;
import org.mpi.vasco.util.debug.Debug;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.ArrayInitializerExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.CharLiteralExpr;
import japa.parser.ast.expr.ClassExpr;
import japa.parser.ast.expr.ConditionalExpr;
import japa.parser.ast.expr.DoubleLiteralExpr;
import japa.parser.ast.expr.EnclosedExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.InstanceOfExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.IntegerLiteralMinValueExpr;
import japa.parser.ast.expr.LiteralExpr;
import japa.parser.ast.expr.LongLiteralExpr;
import japa.parser.ast.expr.LongLiteralMinValueExpr;
import japa.parser.ast.expr.MarkerAnnotationExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.NullLiteralExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.QualifiedNameExpr;
import japa.parser.ast.expr.SingleMemberAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.expr.SuperExpr;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.Type;

// TODO: Auto-generated Javadoc
/**
 * The Class ExpressionParser. It defines methods to recognize the type of
 * expression, and dispatch them to the corresponding functions to analyze them.
 * 
 * @author chengli
 */
public class ExpressionParser {

	/**
	 * Dispatch an expression to a proper visitor for parsing.
	 * 
	 * @param exp
	 *            the expression
	 * @param methodId
	 *            the m id
	 * @return the cFG graph
	 */
	public static CFGGraph<CodeNodeIdentifier, Expression> dispatchToExpressionVisitor(
			Expression exp, MethodIdentifier methodId) {

		Debug.println("------------> Exp");
		Debug.println(exp.toString());

		if (exp instanceof AnnotationExpr) {
			System.err.println("AnnotationExpr");
			System.err.println("Warning: not implemented yet!!!!!!!!!!!!");
			return null;
		} else if (exp instanceof ArrayAccessExpr) {
			Debug.println("ArrayAccessExpr");
			return null;
		} else if (exp instanceof ArrayCreationExpr) {
			Debug.println("ArrayCreationExpr");
			return null;
		} else if (exp instanceof ArrayInitializerExpr) {
			Debug.println("ArrayInitializerExpr");
			return null;
		} else if (exp instanceof AssignExpr) {
			Debug.println("AssignExpr");
			return new ExpressionVisitor().visit((AssignExpr) exp, methodId);
		} else if (exp instanceof BinaryExpr) {
			Debug.println("BinaryExpr");
			return new ExpressionVisitor().visit((BinaryExpr) exp, methodId);
		} else if (exp instanceof BooleanLiteralExpr) {
			Debug.println("BooleanLiteralExpr");
			return null;
		} else if (exp instanceof CastExpr) {
			Debug.println("CastExpr");
			return new ExpressionVisitor().visit((CastExpr) exp, methodId);
		} else if (exp instanceof CharLiteralExpr) {
			Debug.println("CharLiteralExpr");
			return null;
		} else if (exp instanceof ClassExpr) {
			System.err.println("ClassExpr");
			System.err.println("Warning: not implemented yet!!!!!!!!!!!!");
			return null;
		} else if (exp instanceof ConditionalExpr) {
			Debug.println("ConditionalExpr");
			return new ExpressionVisitor().visit((ConditionalExpr) exp, methodId);
		} else if (exp instanceof DoubleLiteralExpr) {
			Debug.println("DoubleLiteralExpr");
			return null;
		} else if (exp instanceof EnclosedExpr) {
			Debug.println("EnclosedExpr");
			return new ExpressionVisitor().visit((EnclosedExpr) exp, methodId);
		} else if (exp instanceof FieldAccessExpr) {
			Debug.println("FieldAccessExpr");
			return null;
		} else if (exp instanceof InstanceOfExpr) {
			System.err.println("InstanceOfExpr");
			System.err.println("Warning: not implemented yet!!!!!!!!!!!!");
			return null;
		} else if (exp instanceof IntegerLiteralExpr) {
			Debug.println("IntegerLiteralExpr");
			return null;
		} else if (exp instanceof IntegerLiteralMinValueExpr) {
			System.err.println("IntegerLiteralMinValueExpr");
			System.err.println("Warning: not implemented yet!!!!!!!!!!!!");
			return null;
		} else if (exp instanceof LongLiteralExpr) {
			Debug.println("LongLiteralExpr");
			return null;
		} else if (exp instanceof LongLiteralMinValueExpr) {
			System.err.println("LongLiteralMinValueExpr");
			System.err.println("Warning: not implemented yet!!!!!!!!!!!!");
			return null;
		} else if (exp instanceof MarkerAnnotationExpr) {
			System.err.println("MarkerAnnotationExpr");
			System.err.println("Warning: not implemented yet!!!!!!!!!!!!");
			return null;
		} else if (exp instanceof MethodCallExpr) {
			Debug.println("MethodCallExpr");
			return new ExpressionVisitor()
					.visit((MethodCallExpr) exp, methodId);
		} else if (exp instanceof NameExpr) {
			Debug.println("NameExpr");
			return new ExpressionVisitor().visit((NameExpr) exp, methodId);
		} else if (exp instanceof NormalAnnotationExpr) {
			System.err.println("NormalAnnotationExpr");
			System.err.println("Warning: not implemented yet!!!!!!!!!!!!");
			return null;
		} else if (exp instanceof NullLiteralExpr) {
			Debug.println("NullLiteralExpr");
			return null;
		} else if (exp instanceof ObjectCreationExpr) {
			Debug.println("ObjectCreationExpr");
			return new ExpressionVisitor().visit((ObjectCreationExpr) exp,
					methodId);
		} else if (exp instanceof QualifiedNameExpr) {
			System.err.println("QualifiedNameExpr");
			System.err.println("Warning: not implemented yet!!!!!!!!!!!!");
			return null;
		} else if (exp instanceof SingleMemberAnnotationExpr) {
			System.err.println("SingleMemberAnnotationExpr");
			System.err.println("Warning: not implemented yet!!!!!!!!!!!!");
			return null;
		} else if (exp instanceof StringLiteralExpr) {
			Debug.println("StringLiteralExpr");
			return null;
		} else if (exp instanceof SuperExpr) {
			System.err.println("SuperExpr");
			System.err.println("Warning: not implemented yet!!!!!!!!!!!!");
			return null;
		} else if (exp instanceof ThisExpr) {
			Debug.println("ThisExpr");
			return null;
		} else if (exp instanceof UnaryExpr) {
			Debug.println("UnaryExpr");
			return new ExpressionVisitor().visit((UnaryExpr) exp, methodId);
		} else if (exp instanceof VariableDeclarationExpr) {
			Debug.println("VariableDeclarationExpr");
			Debug.println("have to check this case int a = b.integrate()");
			return new ExpressionVisitor().visit((VariableDeclarationExpr) exp,
					methodId);
		} else {
			System.err.println("Some other expressions");
			return null;
		}
	}

	/**
	 * Checks if is exp simple expression.
	 * 
	 * @param exp
	 *            the exp
	 * @return true, if is exp simple expression
	 */
	public static boolean isExpSimpleExpression(Expression exp) {
		if (exp instanceof LiteralExpr) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Checks if is method call expression.
	 *
	 * @param exp the exp
	 * @return true, if is m method call expression
	 */
	public static boolean isMethodCallExpression(Expression exp){
		if(exp instanceof MethodCallExpr){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if is string literal expression.
	 *
	 * @param exp the exp
	 * @return true, if is string literal expression
	 */
	public static boolean isStringLiteralExpression(Expression exp){
		if(exp instanceof StringLiteralExpr){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if is literal expression.
	 *
	 * @param exp the exp
	 * @return true, if is literal expression
	 */
	public static boolean isLiteralExpression(Expression exp){
		if(exp instanceof LiteralExpr){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if is name expr.
	 *
	 * @param exp the exp
	 * @return true, if is name expr
	 */
	public static boolean isNameExpr(Expression exp){
		if(exp instanceof NameExpr){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if is assignment expr.
	 *
	 * @param exp the exp
	 * @return true, if is assignment expr
	 */
	public static boolean isAssignmentExpr(Expression exp){
		if(exp instanceof AssignExpr){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if is variable declaration expr.
	 *
	 * @param exp the exp
	 * @return true, if is variable declaration expr
	 */
	public static boolean isVariableDeclarationExpr(Expression exp){
		if(exp instanceof VariableDeclarationExpr){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if is binary expr.
	 *
	 * @param exp the exp
	 * @return true, if is binary expr
	 */
	public static boolean isBinaryExpr(Expression exp){
		if(exp instanceof BinaryExpr){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Gets the operands.
	 * Break a nested binary expr into a set of operands
	 * 
	 * @param exp the exp
	 * @return the operands
	 */
	public static List<Expression> getOperands(BinaryExpr exp){
		List<Expression> operandList = new ArrayList<Expression>();
		Expression leftSide = exp.getLeft();
		Expression rightSide = exp.getRight();
		if(leftSide instanceof BinaryExpr){
			operandList.addAll(getOperands((BinaryExpr) leftSide));
		}else{
			operandList.add(leftSide);
		}
		
		if(rightSide instanceof BinaryExpr){
			operandList.addAll(getOperands((BinaryExpr) rightSide));
		}else{
			operandList.add(rightSide);
		}
		return operandList;
	}

	/**
	 * Gets the matched var declaration.
	 *
	 * @param exp the exp
	 * @param varName the var name
	 * @return the matched var declaration
	 */
	public static Expression getMatchedVarDeclaration(VariableDeclarationExpr exp, String varName){
		List<VariableDeclarator> vars = exp.getVars();
		for(VariableDeclarator var : vars){
			Debug.println("var declaration: " + var.toString());
			if(var.getId().getName().equals(varName)){
				Debug.println("we found a var decalaration: " + varName);
				Expression varInit = var.getInit();
				if(varInit != null){
					Debug.println("var initialization: " + varInit.toString());
				}
				return varInit;
			}
		}
		return null;
	}
	/**
	 * Test object creation expr.
	 *
	 * @param exp the exp
	 */
	public static void testObjectCreationExpr(ObjectCreationExpr exp) {
		Expression scope = exp.getScope();
		if (scope != null) {
			ExpressionParser.dispatchToExpressionVisitor(scope, null);
		}
		ClassOrInterfaceType ciType = exp.getType();
		Debug.println(ciType.toString());

		List<Type> types = exp.getTypeArgs();
		if (types != null) {
			for (Type t : types) {
				Debug.println("type " + t.toString());
			}
		}

		List<Expression> expList = exp.getArgs();
		if (expList != null) {
			for (Expression e : expList) {
				ExpressionParser.dispatchToExpressionVisitor(e, null);
			}
		}
	}

}

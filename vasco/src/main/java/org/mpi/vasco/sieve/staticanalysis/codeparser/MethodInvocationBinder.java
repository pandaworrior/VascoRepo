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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.mpi.vasco.sieve.staticanalysis.codeparser.javaparserextend.VariableType;

import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGGraph;
import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGNode;
import org.mpi.vasco.util.debug.Debug;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.type.PrimitiveType;

// TODO: Auto-generated Javadoc
/**
 * The Class MethodInvocationBinder. This class is used to bind all method
 * invocations across multiple java files.
 * 
 * @author chengli
 */
public class MethodInvocationBinder {

	/**
	 * Gets the all method call nodes.
	 * 
	 * @param cfg
	 *            the control flow graph
	 * @return the all method call nodes
	 */
	public static List<CFGNode<CodeNodeIdentifier, Expression>> getAllMethodCallNodes(
			CFGGraph<CodeNodeIdentifier, Expression> cfg) {
		List<CFGNode<CodeNodeIdentifier, Expression>> nodeList = new ArrayList<CFGNode<CodeNodeIdentifier, Expression>>();
		List<CFGNode<CodeNodeIdentifier, Expression>> cfgNodeList = cfg
				.getNodeListViaBFS();
		for (CFGNode<CodeNodeIdentifier, Expression> cfgNode : cfgNodeList) {
			Expression expr = cfgNode.getNodeData();
			if (expr instanceof MethodCallExpr) {
				nodeList.add(cfgNode);
			}
		}
		return nodeList;
	}

	/**
	 * Find local def type. Search if the name of scope matches the string in
	 * var def list and the type is defined before scope
	 * 
	 * @param varDefList
	 *            the var def list
	 * @param scope
	 *            the scope
	 * @return the type
	 */
	public static VariableType findLocalDefType(HashMap<VariableType, String> varDefList,
			Expression scope) {
		if (scope != null) {
			Iterator<Entry<VariableType, String>> it = varDefList.entrySet().iterator();
			while (it.hasNext()) {
				Entry<VariableType, String> tuple = it.next();
				VariableType defType = tuple.getKey();
				String varName = tuple.getValue();
				if (scope.toString().equals(varName)) {
					if (scope.getBeginLine() > defType.getBeginLine()) {
						return defType;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Find class field def type. Search if the name of the scope matches
	 * any field defined by the class.
	 * 
	 * @param varDefList
	 *            the var def list
	 * @param scope
	 *            the scope
	 * @return the type
	 */
	public static VariableType findClassFieldDefType(HashMap<VariableType, String> varDefList,
			Expression scope) {
		if (scope != null && varDefList != null) {
			Iterator<Entry<VariableType, String>> it = varDefList.entrySet().iterator();
			while (it.hasNext()) {
				Entry<VariableType, String> tuple = it.next();
				VariableType defType = tuple.getKey();
				String varName = tuple.getValue();
				if (scope.toString().equals(varName)) {
					return defType;
				}
			}
		}
		return null;
	}


	/**
	 * Find method declaration.
	 *
	 * @param methodCallExpr the method call expr
	 * @param methodDecl the method decl
	 * @param classOrInterfaceName the class or interface name
	 * @param jfParser the jf parser
	 * @param pParser the parser
	 * @return the method declaration
	 */
	public static MethodDeclaration findMethodDeclaration(
			MethodCallExpr methodCallExpr, MethodDeclaration methodDecl, 
			String classOrInterfaceName,
			JavaFileParser jfParser, ProjectParser pParser) {

		Expression scope = methodCallExpr.getScope();
		if(scope == null || scope.toString().equals("this") 
				|| scope.toString().equals(classOrInterfaceName)){
			/* If there are more than two functions with the same name, then you have to figure out by use the number of arguments to
			 * figure out which function you want to use.
			 */
			int numOfArgs = 0;
			if(methodCallExpr.getArgs() != null){
				numOfArgs = methodCallExpr.getArgs().size();
			}
			//analyze the local class
			List<MethodDeclaration> methodDeclList = jfParser.searchForMethodDeclarationByDetails(classOrInterfaceName, 
					methodCallExpr.getName(), numOfArgs);
			if(methodDeclList == null ||
					methodDeclList.size() == 0) {
				Debug.println("Unforturnately, you cannot find the function declaration for " + methodCallExpr.toString());
				return null;
			}else {
				return methodDeclList.get(0);
			}
		}else{
			List<MethodDeclaration> methodDeclList = null;
			//get method local methodDecl
			HashMap<VariableType, String> varDefMap = jfParser.getAllVariableDeclarationsByMethodDeclaration(classOrInterfaceName, methodDecl);
			VariableType defType = findLocalDefType(varDefMap, scope);
			if(defType == null){
				varDefMap = jfParser.getAllVariableDeclarationsByClassOrInterfaceName(classOrInterfaceName);
				defType = findClassFieldDefType(varDefMap, scope);
			}else {
				//if this defType is primitive type
				if(defType.getType() instanceof PrimitiveType){
					return null;
				}
				//first search local package
				String packageName = jfParser.getPackageNamePlainText();
				methodDeclList = pParser.searchForMethodDeclarationByDetails(packageName, defType.toString(), methodCallExpr.getName());
				if(methodDeclList == null){
					Debug.println("the method declaration list is null inside the same package");
				}else{
					if(methodDeclList.isEmpty()){
						Debug.println("method declaration list is empty inside the same package");
					}else{
						Debug.println("the number of matched is " + methodDeclList.size());
						return methodDeclList.get(0);
					}
				}
				
				//then search the import list
				List<String> importPackageNames = jfParser.getImportPackageNames();
				if(!importPackageNames.isEmpty()){
					methodDeclList = pParser.searchForMethodDeclarationInImportList(importPackageNames, defType.toString(), methodCallExpr.getName());
					if(methodDeclList == null){
						Debug.println("the method declaration list is null across packages");
					}else{
						if(methodDeclList.isEmpty()){
							Debug.println("method declaration list is empty across packages");
						}else{
							Debug.println("the number of matched is " + methodDeclList.size());
							return methodDeclList.get(0);
						}
					}
				}
			}
		}
		return null;
	}
	
	//search inside the same package
}

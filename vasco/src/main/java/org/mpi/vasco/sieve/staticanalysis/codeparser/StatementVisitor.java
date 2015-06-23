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
import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGNode;
import org.mpi.vasco.util.debug.Debug;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.BreakStmt;
import japa.parser.ast.stmt.CatchClause;
import japa.parser.ast.stmt.ContinueStmt;
import japa.parser.ast.stmt.DoStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.SwitchEntryStmt;
import japa.parser.ast.stmt.SwitchStmt;
import japa.parser.ast.stmt.SynchronizedStmt;
import japa.parser.ast.stmt.ThrowStmt;
import japa.parser.ast.stmt.TryStmt;
import japa.parser.ast.stmt.WhileStmt;
import japa.parser.ast.expr.BreakExpr;
import japa.parser.ast.expr.ContinueExpr;
import japa.parser.ast.expr.ReturnExpr;
import japa.parser.ast.visitor.GenericVisitorAdapter;

// TODO: Auto-generated Javadoc
/**
 * The Class StatementVisitor.
 * 
 * @author chengli
 */
public class StatementVisitor
		extends
		GenericVisitorAdapter<CFGGraph<CodeNodeIdentifier, Expression>, MethodIdentifier> {

	/**
	 * Obtain control flow graph for if statement Recursively analyze both if
	 * and else part Generate a control flow graph with a node that is the
	 * condition expression. Link this graph to two sub graphs from if and else.
	 * If lese is missing, then create an empty node.
	 * 
	 * @param ifStmt
	 *            the if statement
	 * @param methodId
	 *            the method identifier
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(IfStmt ifStmt,
			MethodIdentifier methodId) {
		boolean isElseEmpty = false;
		Expression conditionExpr = ifStmt.getCondition();
		// TODO: need to recursively analyze the conditionExpr
		CFGGraph<CodeNodeIdentifier, Expression> cfg = new CFGGraph<CodeNodeIdentifier, Expression>();
		CodeNodeIdentifier cnId = new CodeNodeIdentifier(
				methodId.getPackageName(), methodId.getClassName(),
				methodId.getMethodName(), ifStmt.getBeginLine(),
				ifStmt.getBeginColumn(), ifStmt.getEndLine(),
				ifStmt.getEndColumn());
		CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
				cnId, conditionExpr);
		cfg.addEntryNode(cfgNode);
		cfg.addExitNode(cfgNode);

		List<CFGGraph<CodeNodeIdentifier, Expression>> subCfgList = new ArrayList<CFGGraph<CodeNodeIdentifier, Expression>>();
		Statement st = ifStmt.getThenStmt();
		if (st != null) {
			Debug.println("then statement: " + st.toString());
			CFGGraph<CodeNodeIdentifier, Expression> subCfg = StatementParser
					.dispatchToStatementVisitor(st, methodId);
			if (subCfg != null) {
				subCfgList.add(subCfg);
			}
		}
		st = ifStmt.getElseStmt();
		if (st != null) {
			Debug.println("else statement: " + st.toString());
			CFGGraph<CodeNodeIdentifier, Expression> subCfg = StatementParser
					.dispatchToStatementVisitor(st, methodId);
			if (subCfg != null) {
				subCfgList.add(subCfg);
			}
		} else {
			//not necessary to create an empty node for this front
			/*CFGGraph<CodeNodeIdentifier, Expression> subCfg = new CFGGraph<CodeNodeIdentifier, Expression>();
			CodeNodeIdentifier elseCnId = new CodeNodeIdentifier(
					methodId.getPackageName(), methodId.getClassName(),
					methodId.getMethodName(), ifStmt.getBeginLine(),
					ifStmt.getBeginColumn(), ifStmt.getEndLine(),
					ifStmt.getEndColumn());
			CFGNode<CodeNodeIdentifier, Expression> elseCfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
					elseCnId, null);
			subCfg.addEntryNode(elseCfgNode);
			subCfg.addExitNode(elseCfgNode);
			subCfgList.add(subCfg);*/
			isElseEmpty = true;
		}
		if (!subCfgList.isEmpty()) {
			cfg.mergeWithOtherControlFlowGraphs(subCfgList);
		}
		
		if(isElseEmpty){
			//make the compare condition to exit node as well
			cfg.addExitNode(cfgNode);
		}
		return cfg;
	}
	
	/**
	 * Obtain control flow graph for switch entry statement. Analyze
	 * each statement.
	 * 
	 * @param swEntryStmt
	 *            the switch entry statement
	 * @param methodId
	 *            the method identifier
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(SwitchEntryStmt swEntryStmt,
			MethodIdentifier methodId) {
		CFGGraph<CodeNodeIdentifier, Expression> cfg = null;
		List<Statement> statList = swEntryStmt.getStmts();
		if(statList != null) {
			for(int i = 0; i < statList.size(); i++) {
				Statement st = statList.get(i);
				CFGGraph<CodeNodeIdentifier, Expression> subCfg = StatementParser.dispatchToStatementVisitor(
						st, methodId);
				if(subCfg != null && cfg == null) {
					cfg = subCfg;
				}else {
					if(subCfg != null) {
						cfg.mergeWithOtherControlFlowGraph(subCfg);
					}
				}
			}
		}
		return cfg;
	}
	
	/**
	 * Obtain control flow graph for switch statement. Analyze
	 * each entry statement.
	 * 
	 * @param swStmt
	 *            the switch statement
	 * @param methodId
	 *            the method identifier
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(SwitchStmt swStmt,
			MethodIdentifier methodId) {
		CFGGraph<CodeNodeIdentifier, Expression> cfg = new CFGGraph<CodeNodeIdentifier, Expression>();
		Expression selectorExpr = swStmt.getSelector();
		CodeNodeIdentifier cnId = new CodeNodeIdentifier(
				methodId.getPackageName(), methodId.getClassName(),
				methodId.getMethodName(), swStmt.getBeginLine(),
				swStmt.getBeginColumn(), swStmt.getEndLine(),
				swStmt.getEndColumn());
		CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
				cnId, selectorExpr);// Perhaps we have to recursively analyze the selectorExpr
		cfg.addEntryNode(cfgNode);
		cfg.addExitNode(cfgNode);
		
		List<SwitchEntryStmt> entryStmtList = swStmt.getEntries();
		List<CFGGraph<CodeNodeIdentifier, Expression>> subCfgList = new ArrayList<CFGGraph<CodeNodeIdentifier, Expression>>();
		for(SwitchEntryStmt en : entryStmtList) {
			CFGGraph<CodeNodeIdentifier, Expression> subCfg = new StatementVisitor().visit(en , methodId);
			if(subCfg != null) {
				subCfgList.add(subCfg);
			}
		}
		if(subCfgList.size() > 0) {
			cfg.mergeWithOtherControlFlowGraphs(subCfgList);
		}
		return cfg;
	}

	/**
	 * Obtain control flow graph for block statement Recursively analyze
	 * all statements. Generate a control flow graph for each unit, and
	 * merge them together.
	 *
	 * @param n the n
	 * @param methodId the method identifier
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(BlockStmt n,
			MethodIdentifier methodId) {
		List<Statement> stList = n.getStmts();
		CFGGraph<CodeNodeIdentifier, Expression> cfg = null;
		if(stList != null){
			for (int i = 0; i < stList.size(); i++) {
				Statement st = stList.get(i);
				Debug.println();
				Debug.println(" statement " + st.toString());
				CFGGraph<CodeNodeIdentifier, Expression> cfgForSt = StatementParser
						.dispatchToStatementVisitor(st, methodId);
				if (cfgForSt != null && cfg == null) {
					cfg = cfgForSt;
				} else {
					if(cfgForSt != null) {
						cfg.mergeWithOtherControlFlowGraph(cfgForSt);
					}
				}
			}
		}
		return cfg;
	}
	
	/**
	 * Obtain control flow graph for Synchronized statement Recursively analyze
	 * all statements. Generate a control flow graph for each unit, and
	 * merge them together. 
	 *
	 * @param synStmt the SynchronizedStmt
	 * @param methodId the method identifier
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(SynchronizedStmt synStmt,
			MethodIdentifier methodId) {
		return StatementParser.dispatchToStatementVisitor(synStmt.getBlock(), methodId);
	}

	/**
	 * Obtain control flow graph for for statement Generate a control flow graph
	 * with a node that is the condition expression. Generate a control flow
	 * graph for the for body. Add the leaf node back to for loop condition
	 * expression.
	 * 
	 * @param forStmt
	 *            the for statement
	 * @param methodId
	 *            the method identifier
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(ForStmt forStmt,
			MethodIdentifier methodId) {
		Expression compExp = forStmt.getCompare();
		// TODO: need to recursively analyze the compExpr
		Debug.println("Compare expression: " + compExp.toString());
		CFGGraph<CodeNodeIdentifier, Expression> cfg = new CFGGraph<CodeNodeIdentifier, Expression>();
		CodeNodeIdentifier cnId = new CodeNodeIdentifier(
				methodId.getPackageName(), methodId.getClassName(),
				methodId.getMethodName(), compExp.getBeginLine(),
				compExp.getBeginColumn(), compExp.getEndLine(),
				compExp.getEndColumn());
		CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
				cnId, compExp);
		cfg.addEntryNode(cfgNode);
		cfg.addExitNode(cfgNode);
		Statement st = forStmt.getBody();
		Debug.println("body statement: " + st.toString());
		CFGGraph<CodeNodeIdentifier, Expression> subCfg = StatementParser
				.dispatchToStatementVisitor(st, methodId);

		if(subCfg != null){
			// get all break and continue expression nodes
			List<CFGNode<CodeNodeIdentifier, Expression>> breakNodeList = new ArrayList<CFGNode<CodeNodeIdentifier, Expression>>();
			List<CFGNode<CodeNodeIdentifier, Expression>> continueNodeList = new ArrayList<CFGNode<CodeNodeIdentifier, Expression>>();
			for (CFGNode<CodeNodeIdentifier, Expression> subCfgNode : subCfg
					.getNodeList()) {
				if (subCfgNode.getNodeData() instanceof BreakExpr) {
					breakNodeList.add(subCfgNode);
				}else if(subCfgNode.getNodeData() instanceof ContinueExpr){
					continueNodeList.add(subCfgNode);
				}
			}
	
			List<CFGNode<CodeNodeIdentifier, Expression>> exitNodeList = subCfg
					.getExitNodeListCopy();
	
			cfg.mergeWithOtherControlFlowGraph(subCfg);
	
			// if the exit node from the subCfg is not break, then the exit node
			// will be removed.
			for (CFGNode<CodeNodeIdentifier, Expression> exNode : exitNodeList) {
				// if this exit is not break, then it will be deleted
				// since it will be directed to the compare expression
				if (!(exNode.getNodeData() instanceof BreakExpr)) {
					cfg.addEdge(exNode, cfgNode);
					cfg.removeExitNode(exNode);
				}
			}
	
			// and remove all their fan out edges and set them as exit node
			for (CFGNode<CodeNodeIdentifier, Expression> breakNode : breakNodeList) {
				cfg.removeEdgesStartingFromOneNode(breakNode);
				cfg.addExitNode(breakNode);
			}
			cfg.addExitNode(cfgNode);// set this node to be exit for the next coming statement
			
			// direct continue statement back to the compare expression node
			for(CFGNode<CodeNodeIdentifier, Expression> continueNode : continueNodeList){
				cfg.addEdge(continueNode, cfgNode);
			}
		}
		return cfg;
	}
	
	/**
	 * Obtain control flow graph for do statement Generate a control flow graph
	 * with a node that is the condition expression. Generate a control flow
	 * graph for the for body. Add the leaf node back to for loop condition
	 * expression. The entry node is the first statement of the loop body
	 * 
	 * @param doStmt
	 *            the do statement
	 * @param methodId
	 *            the method identifier
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(DoStmt doStmt,
			MethodIdentifier methodId) {
		Statement st = doStmt.getBody();
		Debug.println("body statement: " + st.toString());
		CFGGraph<CodeNodeIdentifier, Expression> subCfg = StatementParser
				.dispatchToStatementVisitor(st, methodId);

		if(subCfg != null){
			// get all break and continue expression nodes
			List<CFGNode<CodeNodeIdentifier, Expression>> breakNodeList = new ArrayList<CFGNode<CodeNodeIdentifier, Expression>>();
			List<CFGNode<CodeNodeIdentifier, Expression>> continueNodeList = new ArrayList<CFGNode<CodeNodeIdentifier, Expression>>();
			for (CFGNode<CodeNodeIdentifier, Expression> subCfgNode : subCfg
					.getNodeList()) {
				if (subCfgNode.getNodeData() instanceof BreakExpr) {
					breakNodeList.add(subCfgNode);
				}else if(subCfgNode.getNodeData() instanceof ContinueExpr){
					continueNodeList.add(subCfgNode);
				}
			}
			
			Expression compExp = doStmt.getCondition();
			Debug.println("Compare expression: " + compExp.toString());
			CFGGraph<CodeNodeIdentifier, Expression> cfg = new CFGGraph<CodeNodeIdentifier, Expression>();
			CodeNodeIdentifier cnId = new CodeNodeIdentifier(
					methodId.getPackageName(), methodId.getClassName(),
					methodId.getMethodName(), compExp.getBeginLine(),
					compExp.getBeginColumn(), compExp.getEndLine(),
					compExp.getEndColumn());
			CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
					cnId, compExp);
			cfg.addEntryNode(cfgNode);
			cfg.addExitNode(cfgNode);
			
			
	
			List<CFGNode<CodeNodeIdentifier, Expression>> exitNodeList = subCfg
					.getExitNodeListCopy();
	
			// if the exit node from the subCfg is not break, then the exit node
			// will be removed.
			for (CFGNode<CodeNodeIdentifier, Expression> exNode : exitNodeList) {
				// if this exit is not break, then it will be deleted
				// since it will be directed to the compare expression
				if (!(exNode.getNodeData() instanceof BreakExpr)) {
					subCfg.addEdge(exNode, cfgNode);
					subCfg.removeExitNode(exNode);
				}
			}
			
			// direct continue statement back to the compare expression node
			for(CFGNode<CodeNodeIdentifier, Expression> continueNode : continueNodeList){
				subCfg.addEdge(continueNode, cfgNode);
			}
	
			subCfg.addExitNode(cfgNode);// set this node to be exit for the next coming statement
			
			//add the compression statement node back to the beginning of the loop body
			List<CFGNode<CodeNodeIdentifier, Expression>> entryListOfSubCfg = subCfg.getEntryNodeList();
			for(CFGNode<CodeNodeIdentifier, Expression> entryNodeOfSubCfg : entryListOfSubCfg) {
				subCfg.addEdge(cfgNode, entryNodeOfSubCfg);
			}
		}
		return subCfg;
	}
	
	

	/**
	 * Obtain a sub control flow graph for a return statement. If the expression
	 * after Return keyword is simple one, then create a cfg node for the entire
	 * statement. Otherwise, first parse the expression, and then create a cfg
	 * node for the return, and put the expression's control flow graph
	 * preceding the return node.
	 *
	 * @param rStmt the return statement
	 * @param methodId the method id
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(ReturnStmt rStmt,
			MethodIdentifier methodId) {
		Expression returnExp = rStmt.getExpr();
		CFGGraph<CodeNodeIdentifier, Expression> cfg = new CFGGraph<CodeNodeIdentifier, Expression>();
		CodeNodeIdentifier cnId = new CodeNodeIdentifier(
				methodId.getPackageName(), methodId.getClassName(),
				methodId.getMethodName(), rStmt.getBeginLine(),
				rStmt.getBeginColumn(), rStmt.getEndLine(),
				rStmt.getEndColumn());
		CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
				cnId, new ReturnExpr(rStmt.getBeginLine(), rStmt.getBeginColumn(),
						rStmt.getEndLine(), rStmt.getEndColumn(), returnExp));
		cfg.addEntryNode(cfgNode);
		cfg.addReturnNode(cfgNode);
		cfgNode.setReturnNode(true);
		if (returnExp != null) {
			if (!ExpressionParser.isExpSimpleExpression(returnExp)) {
				CFGGraph<CodeNodeIdentifier, Expression> subCfg = ExpressionParser
						.dispatchToExpressionVisitor(returnExp, methodId);
				if (subCfg != null) {
					subCfg.mergeWithOtherControlFlowGraph(cfg);
					return subCfg;
				}
			}
		}
		return cfg;
	}

	/**
	 * Obtain control flow graph for expression statement. Recursively analyze all
	 * components, and merge all control flow graphs together.
	 *
	 * @param exprStmt the expr stmt
	 * @param methodId the method identifier
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(ExpressionStmt exprStmt,
			MethodIdentifier methodId) {
		Expression exp = exprStmt.getExpression();
		CFGGraph<CodeNodeIdentifier, Expression> cfg = ExpressionParser.dispatchToExpressionVisitor(exp, methodId);
		if(cfg == null){
			//create a node there
			cfg = new CFGGraph<CodeNodeIdentifier, Expression>();
			CodeNodeIdentifier cnId = new CodeNodeIdentifier(
					methodId.getPackageName(), methodId.getClassName(),
					methodId.getMethodName(), exprStmt.getBeginLine(),
					exprStmt.getBeginColumn(), exprStmt.getEndLine(),
					exprStmt.getEndColumn());
			CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
					cnId, exp);
			cfg.addEntryNode(cfgNode);
			cfg.addReturnNode(cfgNode);
		}
		return cfg;
	}

	/**
	 * Obtain control flow graph for break statement. 
	 *
	 * @param bkStmt the bk stmt
	 * @param methodId the method identifier
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(BreakStmt bkStmt,
			MethodIdentifier methodId) {
		CFGGraph<CodeNodeIdentifier, Expression> cfg = new CFGGraph<CodeNodeIdentifier, Expression>();
		CodeNodeIdentifier cnId = new CodeNodeIdentifier(
				methodId.getPackageName(), methodId.getClassName(),
				methodId.getMethodName(), bkStmt.getBeginLine(),
				bkStmt.getBeginColumn(), bkStmt.getEndLine(),
				bkStmt.getEndColumn());
		CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
				cnId, new BreakExpr(bkStmt.getBeginLine(),
						bkStmt.getBeginColumn(), bkStmt.getEndLine(),
						bkStmt.getEndColumn()));
		cfg.addEntryNode(cfgNode);
		cfg.addExitNode(cfgNode);
		return cfg;
	}
	
	/**
	 * Obtain control flow graph for continue statement.
	 *
	 * @param continueStmt the continue stmt
	 * @param methodId the method id
	 * @return the cFG graph
	 * @see japa.parser.ast.visitor.GenericVisitorAdapter#visit(japa.parser.ast.stmt.ContinueStmt, java.lang.Object)
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(ContinueStmt continueStmt,
			MethodIdentifier methodId) {
		CFGGraph<CodeNodeIdentifier, Expression> cfg = new CFGGraph<CodeNodeIdentifier, Expression>();
		CodeNodeIdentifier cnId = new CodeNodeIdentifier(
				methodId.getPackageName(), methodId.getClassName(),
				methodId.getMethodName(), continueStmt.getBeginLine(),
				continueStmt.getBeginColumn(), continueStmt.getEndLine(),
				continueStmt.getEndColumn());
		CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
				cnId, new ContinueExpr(continueStmt.getBeginLine(),
						continueStmt.getBeginColumn(), continueStmt.getEndLine(),
						continueStmt.getEndColumn()));
		cfg.addEntryNode(cfgNode);
		cfg.addExitNode(cfgNode);
		return cfg;
	}
	
	/**
	 * Obtain control flow graph for try statement.
	 *
	 * @param tryStmt the try stmt
	 * @param methodId the method id
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(TryStmt tryStmt,
			MethodIdentifier methodId) {
		BlockStmt tryBlStmt = tryStmt.getTryBlock();
		CFGGraph<CodeNodeIdentifier, Expression> cfg = StatementParser.dispatchToStatementVisitor(tryBlStmt, methodId);
		//get the catch part
		List<CatchClause> catchClauses = tryStmt.getCatchs();
		CFGGraph<CodeNodeIdentifier, Expression> catchCfg = null;
		if(catchClauses != null && catchClauses.size() > 0){
			for(int i = 0; i < catchClauses.size(); i++){
				CatchClause calC = catchClauses.get(i);
				BlockStmt catchBlockStmt = calC.getCatchBlock();
				CFGGraph<CodeNodeIdentifier, Expression> subCatchCfg = StatementParser.dispatchToStatementVisitor(catchBlockStmt, methodId);
				if(i == 0){
					catchCfg = subCatchCfg;
				}else{
					catchCfg.mergeWithOtherControlFlowGraph(subCatchCfg);
				}
			}
		}
		if(catchCfg != null){
			cfg.mergeWithControlFlowInParallel(catchCfg);
		}
		return cfg;
	}
	
	/**
	 * Obtain control flow graph for while statement.
	 *
	 * @param whileStmt the while stmt
	 * @param methodId the method id
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(WhileStmt whileStmt,
			MethodIdentifier methodId) {
		Expression condExp = whileStmt.getCondition();
		Debug.println("Condition expression: " + condExp.toString());
		CFGGraph<CodeNodeIdentifier, Expression> cfg = new CFGGraph<CodeNodeIdentifier, Expression>();
		CodeNodeIdentifier cnId = new CodeNodeIdentifier(
				methodId.getPackageName(), methodId.getClassName(),
				methodId.getMethodName(), condExp.getBeginLine(),
				condExp.getBeginColumn(), condExp.getEndLine(),
				condExp.getEndColumn());
		CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
				cnId, condExp);
		cfg.addEntryNode(cfgNode);
		cfg.addExitNode(cfgNode);
		Statement st = whileStmt.getBody();
		Debug.println("body statement: " + st.toString());
		CFGGraph<CodeNodeIdentifier, Expression> subCfg = StatementParser
				.dispatchToStatementVisitor(st, methodId);

		if(subCfg != null) {
			// get all break and continue expression nodes
			List<CFGNode<CodeNodeIdentifier, Expression>> breakNodeList = new ArrayList<CFGNode<CodeNodeIdentifier, Expression>>();
			List<CFGNode<CodeNodeIdentifier, Expression>> continueNodeList = new ArrayList<CFGNode<CodeNodeIdentifier, Expression>>();
			for (CFGNode<CodeNodeIdentifier, Expression> subCfgNode : subCfg
					.getNodeList()) {
				if (subCfgNode.getNodeData() instanceof BreakExpr) {
					breakNodeList.add(subCfgNode);
				}else if(subCfgNode.getNodeData() instanceof ContinueExpr){
					continueNodeList.add(subCfgNode);
				}
			}
	
			List<CFGNode<CodeNodeIdentifier, Expression>> exitNodeList = subCfg
					.getExitNodeListCopy();
	
			cfg.mergeWithOtherControlFlowGraph(subCfg);
	
			// if the exit node from the subCfg is not break, then the exit node
			// will be removed.
			for (CFGNode<CodeNodeIdentifier, Expression> exNode : exitNodeList) {
				// if this exit is not break, then it will be deleted
				// since it will be directed to the compare expression
				if (!(exNode.getNodeData() instanceof BreakExpr)) {
					cfg.addEdge(exNode, cfgNode);
					cfg.removeExitNode(exNode);
				}
			}
	
			// and remove all their fan out edges and set them as exit node
			for (CFGNode<CodeNodeIdentifier, Expression> breakNode : breakNodeList) {
				cfg.removeEdgesStartingFromOneNode(breakNode);
				cfg.addExitNode(breakNode);
			}
			cfg.addExitNode(cfgNode);// set this node to be exit for the next coming statement
			
			// direct continue statement back to the compare expression node
			// remove all successors from continue node
			for(CFGNode<CodeNodeIdentifier, Expression> continueNode : continueNodeList){
				cfg.removeAllSuccessorsForSpecificNode(continueNode);
				cfg.addEdge(continueNode, cfgNode);
			}
		}else {
			//if the body is empty then add an edge from the condition node back to itself
			cfg.addEdge(cfgNode, cfgNode);
		}
		return cfg;
	}
	
	/**
	 * Obtain control flow graph for throw statement, treat it as
	 * a return statement.
	 *
	 * @param thStmt the throw stmt
	 * @param methodId the method id
	 * @return the cFG graph
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(ThrowStmt thStmt,
			MethodIdentifier methodId) {
		Expression exceptionExp = thStmt.getExpr();
		CFGGraph<CodeNodeIdentifier, Expression> cfg = new CFGGraph<CodeNodeIdentifier, Expression>();
		CodeNodeIdentifier cnId = new CodeNodeIdentifier(
				methodId.getPackageName(), methodId.getClassName(),
				methodId.getMethodName(), thStmt.getBeginLine(),
				thStmt.getBeginColumn(), thStmt.getEndLine(),
				thStmt.getEndColumn());
		CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
				cnId, new ReturnExpr(thStmt.getBeginLine(), thStmt.getBeginColumn(),
						thStmt.getEndLine(), thStmt.getEndColumn(), exceptionExp));
		cfg.addEntryNode(cfgNode);
		cfg.addReturnNode(cfgNode);
		cfgNode.setReturnNode(true);
		return cfg;
	}

}

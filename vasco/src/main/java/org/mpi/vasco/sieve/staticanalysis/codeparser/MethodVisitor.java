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

import java.util.List;

import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGGraph;
import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGNode;
import org.mpi.vasco.util.debug.Debug;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.visitor.GenericVisitorAdapter;

// TODO: Auto-generated Javadoc
/**
 * The Class MethodVisitor.
 * 
 * @author chengli
 */
public class MethodVisitor
		extends
		GenericVisitorAdapter<CFGGraph<CodeNodeIdentifier, Expression>, MethodIdentifier> {


	/**
	 * Obtain the control flow graph for a method.
	 *
	 * @param n the n
	 * @param methodId the method id
	 * @return Control flow graph
	 * @see japa.parser.ast.visitor.GenericVisitorAdapter#visit(japa.parser.ast.body.MethodDeclaration, java.lang.Object)
	 */
	public CFGGraph<CodeNodeIdentifier, Expression> visit(MethodDeclaration n,
			MethodIdentifier methodId) {
		// TODO: here you can access method body
		Debug.println("Method name: " + n.getName());
		BlockStmt bodyStatement = n.getBody();
		CFGGraph<CodeNodeIdentifier, Expression> cfg = null;
		if(bodyStatement == null){
			Debug.println("This method perhaps is an abstract function");
			return cfg;
		}
		List<Statement> statList = bodyStatement.getStmts();
		if(statList != null){
			Debug.println("This method has " + statList.size() + " statements");
			for (int i = 0; i < statList.size(); i++) {
				Statement st = statList.get(i);
				Debug.println();
				Debug.println("Visist " + i + "th statement");
				Debug.println(st.toString());
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
	
			// if this control flow graph still have some exit nodes, then create an
			// empty return node, direct all
			// these exit nodes to this return node.
			if (!cfg.isExitNodeListEmpty()) {
				// create an empty return node
				CodeNodeIdentifier cnId = new CodeNodeIdentifier(
						methodId.getPackageName(), methodId.getClassName(),
						methodId.getMethodName(), n.getEndLine(), n.getEndColumn(),
						n.getEndLine(), n.getEndColumn());
				CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
						cnId, null);
				cfgNode.setReturnNode(true);
				cfg.directAllExitNodesToSpecificNode(cfgNode);
				cfg.addReturnNode(cfgNode);
				cfg.getExitNodeList().clear();
			}
		}else{
			return cfg;
		}
		
		CodeNodeIdentifier cnId = new CodeNodeIdentifier(
				methodId.getPackageName(), methodId.getClassName(),
				methodId.getMethodName(), n.getBeginLine(), n.getBeginColumn(),
				n.getEndLine(), n.getEndColumn());
		
		//if it has multiple entry nodes, then please merge all entry nodes
		if(cfg.getEntryNodeList().size() > 1){
			//create an empty express node at very beginning
			BooleanLiteralExpr boolExpr = new BooleanLiteralExpr(n.getBeginLine(),
					n.getBeginColumn(), n.getEndLine(), n.getEndColumn(), true);
			CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
					cnId, boolExpr);
			for(CFGNode<CodeNodeIdentifier, Expression> entryNode : cfg.getEntryNodeList()){
				cfg.addEdge(cfgNode, entryNode);
			}
			cfg.getEntryNodeList().clear();
			cfg.addEntryNode(cfgNode);
		}
		
		//if the cfg contains only a single node, then please create a empty node and put at very beginning
		//then set this as the entry node, and connect it to the previous single node
		
		if(cfg.getNodeList().size() == 1) {
			//create an empty express node at very beginning
			BooleanLiteralExpr boolExpr = new BooleanLiteralExpr(n.getBeginLine(),
					n.getBeginColumn(), n.getEndLine(), n.getEndColumn(), true);
			CFGNode<CodeNodeIdentifier, Expression> cfgNode = new CFGNode<CodeNodeIdentifier, Expression>(
					cnId, boolExpr);
			cfg.addEdge(cfgNode, cfg.getNodeList().get(0));
			cfg.getEntryNodeList().clear();
			cfg.addEntryNode(cfgNode);
		}
		
		cfg.setCfgIdentifier(cnId);
		return cfg;
	}

}

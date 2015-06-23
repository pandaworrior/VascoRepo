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

import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGGraph;
import org.mpi.vasco.util.debug.Debug;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.stmt.AssertStmt;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.BreakStmt;
import japa.parser.ast.stmt.CatchClause;
import japa.parser.ast.stmt.ContinueStmt;
import japa.parser.ast.stmt.DoStmt;
import japa.parser.ast.stmt.EmptyStmt;
import japa.parser.ast.stmt.ExplicitConstructorInvocationStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.ForeachStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.LabeledStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.SwitchEntryStmt;
import japa.parser.ast.stmt.SwitchStmt;
import japa.parser.ast.stmt.SynchronizedStmt;
import japa.parser.ast.stmt.ThrowStmt;
import japa.parser.ast.stmt.TryStmt;
import japa.parser.ast.stmt.TypeDeclarationStmt;
import japa.parser.ast.stmt.WhileStmt;

// TODO: Auto-generated Javadoc
/**
 * The Class StatementParser. It defines methods to recognize the type of
 * statements, and dispatch them to the corresponding functions to analyze them.
 * 
 * @author chengli
 */
public class StatementParser {

	/**
	 * Dispatch a statement to a proper visitor for parsing.
	 *
	 * @param st the statement
	 * @param methodId the method id
	 * @return the cFG graph
	 */
	public static CFGGraph<CodeNodeIdentifier, Expression> dispatchToStatementVisitor(
			Statement st, MethodIdentifier methodId) {
		if (st instanceof AssertStmt) {
			Debug.println("AssertStmt");
			Debug.println("Warning: not implemented yet!!!!!!!!!!!!");
			return null;
		} else if (st instanceof BlockStmt) {
			Debug.println("BlockStmt");
			return new StatementVisitor().visit((BlockStmt) st, methodId);
		} else if (st instanceof BreakStmt) {
			Debug.println("BreakStmt");
			return new StatementVisitor().visit((BreakStmt) st, methodId);
		} else if (st instanceof ContinueStmt) {
			Debug.println("ContinueStmt");
			return new StatementVisitor().visit((ContinueStmt) st, methodId);
		} else if (st instanceof DoStmt) {
			Debug.println("DoStmt");
			return new StatementVisitor().visit((DoStmt) st, methodId);
		} else if (st instanceof EmptyStmt) {
			Debug.println("EmptyStmt");
			return null;
		} else if (st instanceof ExplicitConstructorInvocationStmt) {
			Debug.println("ExplicitConstructorInvocationStmt");
			Debug.println("Warning: not implemented yet!!!!!!!!!!!!");
			return null;
		} else if (st instanceof ExpressionStmt) {
			Debug.println("ExpressionStmt");
			return new StatementVisitor().visit((ExpressionStmt) st, methodId);
		} else if (st instanceof ForeachStmt) {
			Debug.println("ForeachStmt");
			Debug.println("Warning: not implemented yet!!!!!!!!!!!!");
			return null;
		} else if (st instanceof ForStmt) {
			Debug.println("ForStmt");
			return new StatementVisitor().visit((ForStmt) st, methodId);
		} else if (st instanceof IfStmt) {
			Debug.println("IfStmt");
			return new StatementVisitor().visit((IfStmt) st, methodId);
		} else if (st instanceof LabeledStmt) {
			Debug.println("LabeledStmt");
			Debug.println("Warning: not implemented yet!!!!!!!!!!!!");
			return null;
		} else if (st instanceof ReturnStmt) {
			Debug.println("ReturnStmt");
			return new StatementVisitor().visit((ReturnStmt) st, methodId);
		} else if (st instanceof SwitchEntryStmt) {
			Debug.println("SwitchEntryStmt");
			return new StatementVisitor().visit((SwitchEntryStmt) st, methodId);
		} else if (st instanceof SwitchStmt) {
			Debug.println("SwitchStmt");
			return new StatementVisitor().visit((SwitchStmt) st, methodId);
		} else if (st instanceof SynchronizedStmt) {
			Debug.println("SynchronizedStmt");
			return new StatementVisitor().visit((SynchronizedStmt) st, methodId);
		} else if (st instanceof ThrowStmt) {
			Debug.println("ThrowStmt");
			return new StatementVisitor().visit((ThrowStmt) st, methodId);
		} else if (st instanceof TryStmt) {
			Debug.println("TryStmt");
			return new StatementVisitor().visit((TryStmt) st, methodId);
		} else if (st instanceof TypeDeclarationStmt) {
			Debug.println("TypeDeclarationStmt");
			Debug.println("Warning: not implemented yet!!!!!!!!!!!!");
			return null;
		} else if (st instanceof WhileStmt) {
			Debug.println("WhileStmt");
			return new StatementVisitor().visit((WhileStmt) st, methodId);
		}else {
			System.err.println("not possible");
			return null;
		}
	}

}

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

import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGGraph;
import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGNode;
import org.mpi.vasco.sieve.staticanalysis.datastructures.tree.Tree;
import org.mpi.vasco.sieve.staticanalysis.datastructures.tree.TreeNode;
import org.mpi.vasco.sieve.staticanalysis.datastructures.tree.TreeNodeTypes;
import org.mpi.vasco.util.commonfunc.FileOperations;
import org.mpi.vasco.util.commonfunc.FileTypes;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class ProjectStructureParser.
 * 
 * @author chengli This class is used to obtain the project organization
 */
public class ProjectParser {

	/** The project path. */
	private String projectPath;

	/** The project name. */
	private String projectName;

	/** The project file tree. The key is full path, the data is file type. */
	private Tree<String, FileTypes> projectFileTree;

	/** The package control flow mapping. */
	private HashMap<String, JavaFileParser> packageControlFlowMapping;

	/**
	 * The list of methods that either doesn't have bindings or bindings are
	 * already resolved.
	 */
	private List<MethodDeclaration> methodsNoBindingsOrResolved;

	/**
	 * The map storing methods having bindings and their positions and their
	 * corresponding binding method.
	 */
	private HashMap<MethodDeclaration, HashMap<CFGNode<CodeNodeIdentifier, Expression>, MethodDeclaration>> bindingPositions;

	/** The method cfg mapping. */
	private HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>> methodCfgMapping;
	
	/** The file filter to exclude a few files. */
	private List<String> fileFilter;
	
	/** Enable optimization for inlining functions if it is true. */
	private final boolean optimized = true;
	
	/**
	 * Instantiates a new project structure parser.
	 * 
	 * @param pP
	 *            the project path
	 * @param pN
	 *            the project name
	 */
	public ProjectParser(String pP, String pN) {
		this.setProjectPath(pP);
		this.setProjectName(pN);
		this.projectFileTree = new Tree<String, FileTypes>();
		this.packageControlFlowMapping = new HashMap<String, JavaFileParser>();
		this.methodsNoBindingsOrResolved = new ArrayList<MethodDeclaration>();
		this.bindingPositions = new HashMap<MethodDeclaration, HashMap<CFGNode<CodeNodeIdentifier, Expression>, MethodDeclaration>>();
		this.methodCfgMapping = new HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>();
		this.fileFilter = new ArrayList<String>();
	}

	/**
	 * Sets the project name.
	 * 
	 * @param projectName
	 *            the projectName to set
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/**
	 * Sets the project path.
	 * 
	 * @param projectPath
	 *            the projectPath to set
	 */
	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	/**
	 * Gets the project path.
	 * 
	 * @return the project path
	 */
	public String getProjectPath() {
		return this.projectPath;
	}

	/**
	 * Gets the project name.
	 * 
	 * @return the project name
	 */
	public String getProjectName() {
		return this.projectName;
	}

	/**
	 * Gets the package control flow mapping.
	 * 
	 * @return the packageControlFlowMapping
	 */
	public HashMap<String, JavaFileParser> getPackageControlFlowMapping() {
		return packageControlFlowMapping;
	}

	/**
	 * Sets the package control flow mapping.
	 * 
	 * @param packageControlFlowMapping
	 *            the packageControlFlowMapping to set
	 */
	public void setPackageControlFlowMapping(
			HashMap<String, JavaFileParser> packageControlFlowMapping) {
		this.packageControlFlowMapping = packageControlFlowMapping;
	}
	
	/**
	 * Gets the all package names.
	 * 
	 * @return the all package names
	 */
	private Set<String> getAllPackageNames() {
		return this.packageControlFlowMapping.keySet();
	}

	/**
	 * Adds the resolved method.
	 * 
	 * @param methodDecl
	 *            the method decl
	 */
	public void addResolvedMethod(MethodDeclaration methodDecl) {
		if (!this.methodsNoBindingsOrResolved.contains(methodDecl)) {
			this.methodsNoBindingsOrResolved.add(methodDecl);
		}
	}
	
	/**
	 * Adds the binding positions. Map one method call expr
	 * to the callee method declaration
	 *
	 * @param methodDecl the method decl
	 * @param methodCallMapping the method call mapping
	 */
	public void addBindingPositions(MethodDeclaration methodDecl, HashMap<CFGNode<CodeNodeIdentifier, Expression>, 
			MethodDeclaration> methodCallMapping){
		this.bindingPositions.put(methodDecl, methodCallMapping);
	}
	
	/**
	 * Removes the binding positions.
	 *
	 * @param methodDecl the method decl
	 */
	public void removeBindingPositions(MethodDeclaration methodDecl){
		this.bindingPositions.remove(methodDecl);
	}
	
	/**
	 * Gets the all callee method declarations.
	 *
	 * @param methodDecl the method decl
	 * @return the all callee method declarations
	 */
	private Collection<MethodDeclaration> getAllCalleeMethodDeclarations(MethodDeclaration methodDecl){
		HashMap<CFGNode<CodeNodeIdentifier, Expression>, MethodDeclaration> mapping = this.bindingPositions.get(methodDecl);
		return mapping.values();
	}
	
	/**
	 * Checks if is all callee method declarations resolved.
	 *
	 * @param methodDecl the method decl
	 * @return true, if is all callee method declarations resolved
	 */
	private boolean isAllCalleeMethodDeclarationsResolved(MethodDeclaration methodDecl){
		Collection<MethodDeclaration> calleeMethods = this.getAllCalleeMethodDeclarations(methodDecl);
		Iterator<MethodDeclaration> calleeMethodIt = calleeMethods.iterator();
		while(calleeMethodIt.hasNext()){
			if(!this.methodsNoBindingsOrResolved.contains(calleeMethodIt.next())){
				return false;
			}
		}
		return true;
	}

	/**
	 * Builds the raw dir tree that may contain empty dir.
	 */
	private void buildRawDirTree() {
		// start with the top dir
		ArrayList<String> dirList = new ArrayList<String>();
		// push the top dir into the dirList
		dirList.add(this.projectPath);
		// create a root node
		TreeNode<String, FileTypes> rootNode = new TreeNode<String, FileTypes>(
				this.getProjectPath(), FileTypes.DIRECTORY, null,
				TreeNodeTypes.ROOT);
		this.projectFileTree.setRootTreeNode(rootNode);

		// traverse the entire dir recursively
		while (!dirList.isEmpty()) {
			// get one dir from the dirList and fetch all files
			String currentDir = dirList.remove(0);
			Debug.println("analyze the current dir " + currentDir);
			if (!FileOperations.isExisted(currentDir)) {
				throw new RuntimeException(currentDir + " not existed!");
			}

			List<String> fileList = FileOperations.getAllFilesUnderDir(currentDir);
			Debug.println("fileList " + fileList);

			TreeNode<String, FileTypes> parentNode = this.projectFileTree.getTreeNodeById(currentDir);
			for (String fileName : fileList) {
				Debug.println("filename " + fileName);
				FileTypes fileType = FileOperations.getFileType(fileName);
				switch (fileType) {
				case DIRECTORY:
					Debug.println(fileName + " is a dir");
					// if this file is a dir, create a internal node and put the dir in dirList
					dirList.add(fileName);
					TreeNode<String, FileTypes> treeNode = new TreeNode<String, FileTypes>(
							fileName, FileTypes.DIRECTORY, null);
					// if this dir is empty then it is leaf
					if (FileOperations.isDirEmpty(fileName)) {
						treeNode.setNodeType(TreeNodeTypes.LEAF);
					} else {
						treeNode.setNodeType(TreeNodeTypes.INTERNAL);
					}
					this.projectFileTree.addTreeNode(parentNode, treeNode);
					break;
				case JAVA_FILE:
					Debug.println(fileName + " is a java file");
					if(!this.shouldBeExcluded(fileName)){
						// if this file is a java file, create a leaf node
						TreeNode<String, FileTypes> leafNode = new TreeNode<String, FileTypes>(
								fileName, FileTypes.JAVA_FILE, null,
								TreeNodeTypes.LEAF);
						this.projectFileTree.addTreeNode(parentNode, leafNode);
					}
					break;
				case NON_JAVA_FILE:
					Debug.println(fileName + " is a file but not java");
					break;
				default:
					throw new RuntimeException("Should not reach here");
				}

			}
		}
	}

	/**
	 * Removes the empty dir from dir tree.
	 */
	private void removeEmptyDirFromDirTree() {
		while (true) {
			// get all leaf nodes
			ArrayList<TreeNode<String, FileTypes>> leafNodeList = this.projectFileTree
					.getTreeAllLeafNodes();
			// if all leafs are not dirs
			int dirNum = 0;
			for (TreeNode<String, FileTypes> leafNode : leafNodeList) {
				if (FileOperations.isDirectory(leafNode.getNodeData())) {
					// if this leaf is a dir, then remove it from tree and
					// update its parent to leaf if the parent doesn't have more
					// children
					this.projectFileTree.removeTreeNode(leafNode.getParent(),
							leafNode);
					dirNum++;
				}
			}
			// if no dir leaf, then the tree is formed
			if (dirNum == 0) {
				break;
			}
		}
	}

	/**
	 * Builds the file tree.
	 */
	public void buildFileTree() {
		this.buildRawDirTree();
		//this.printOutFileTree();
		this.removeEmptyDirFromDirTree();
	}

	/**
	 * Obtail all control flow graphs. Iterate all files under this directory
	 * and parse all of them.
	 */
	public void obtailAllControlFlowGraphs() {
		// get node list
		List<TreeNode<String, FileTypes>> treeNodeList = this.projectFileTree
				.getTreeNodeInDFSOrder();
		for (TreeNode<String, FileTypes> treeNode : treeNodeList) {
			if (treeNode.getNodeData() == FileTypes.JAVA_FILE) {
				// parse the file here
				Debug.println("Parse file: " + treeNode.getNodeId());
				JavaFileParser jpParser = new JavaFileParser(
						treeNode.getNodeId());
				jpParser.parseJavaFile();
				this.packageControlFlowMapping.put(
						jpParser.getPackageNamePlainText(), jpParser);
				this.methodCfgMapping.putAll(jpParser.getMethodControlFlowGraphMapping());
			}
		}

	}

	/**
	 * Gets the all java file parsers by package name.
	 *
	 * @param packageName the package name
	 * @return the all java file parsers by package name
	 */
	private List<JavaFileParser> getAllJavaFileParsersByPackageName(String packageName){
		List<JavaFileParser> jfParserList = new ArrayList<JavaFileParser>();
		Iterator<Entry<String, JavaFileParser>> it = this.packageControlFlowMapping.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, JavaFileParser> tuple = it.next();
			Debug.println("package name " + tuple.getKey());
			if(tuple.getKey().equals(packageName)){
				jfParserList.add(tuple.getValue());
			}
		}
		return jfParserList;
	}
	
	/**
	 * Gets the java file parser by package name.
	 *
	 * @param packageName the package name
	 * @param classOrInterfaceName the class or interface name
	 * @return the java file parser by package name
	 */
	private JavaFileParser getJavaFileParserByPackageName(String packageName, String classOrInterfaceName){
		List<JavaFileParser> jfParserList = this.getAllJavaFileParsersByPackageName(packageName);
		for(JavaFileParser jfParser : jfParserList){
			if(jfParser.isClassOrInterfaceDeclarationContained(classOrInterfaceName)){
				return jfParser;
			}
		}
		return null;
	}
	
	/**
	 * Get a list of method declarations pointed by <packageName, classOrInterface, methodName>.
	 *
	 * @param packageName the package name
	 * @param classOrInterfaceName the class or interface name
	 * @param methodName the method name
	 * @return the list
	 */
	public List<MethodDeclaration> searchForMethodDeclarationByDetails(String packageName, String classOrInterfaceName, String methodName){
		JavaFileParser jfParser = this.getJavaFileParserByPackageName(packageName, classOrInterfaceName);
		if(jfParser != null){
			return jfParser.searchForMethodDeclarationByDetails(classOrInterfaceName, methodName);
		}
		return null;
	}
	
	/**
	 * Search for method declaration in import list.
	 *
	 * @param packageNames the package names
	 * @param classOrInterfaceName the class or interface name
	 * @param methodName the method name
	 * @return the list
	 */
	public List<MethodDeclaration> searchForMethodDeclarationInImportList(List<String> packageNames, String classOrInterfaceName, String methodName){
		List<MethodDeclaration> methodDeclList = new ArrayList<MethodDeclaration>();
		for(String packageName : packageNames){
			//packageName doesn't contain *, split it to be packageName + fileName
			if(!packageName.contains("*")){
				int lastDotIndex = packageName.lastIndexOf('.');
				packageName = packageName.substring(0, lastDotIndex);
			}else{
				//packageName doest contain *, remove *
				packageName = packageName.substring(0, packageName.length() - 1);
			}
			Debug.println("real package name: " + packageName);
			List<JavaFileParser> jfParsers = this.getAllJavaFileParsersByPackageName(packageName);
			for(JavaFileParser jfParser : jfParsers){
				methodDeclList.addAll(jfParser.searchForMethodDeclarationByDetails(classOrInterfaceName, methodName));
			}
		}
		
		return methodDeclList;
	}

	/**
	 * Obtain bindings for all methods. Go over all methods, if a method doesn't
	 * have function call or all functions are from unknown library. then it
	 * will be put into the already resolved list. Otherwise, identify all its
	 * required bindings, and put this into binding map.
	 */
	public void obtainBindingsForAllMethods() {
		Set<String> packageNameSet = this.getAllPackageNames();
		Iterator<String> it = packageNameSet.iterator();
		while (it.hasNext()) {
			String packageName = it.next();
			this.obtainBindingsForAllMethodsInPackage(packageName);
		}
	}
	
	/**
	 * Obtain bindings for all methods in package.
	 *
	 * @param packageName the package name
	 */
	private void obtainBindingsForAllMethodsInPackage(String packageName){
		// get all file parser
		Collection<JavaFileParser> jfParsers = this.getAllJavaFileParsersByPackageName(packageName);
		// iterate all java file parser
		Iterator<JavaFileParser> jfParserIt = jfParsers.iterator();
		while (jfParserIt.hasNext()) {
			JavaFileParser jfParser = jfParserIt.next();
			this.obtainBindingsForAllMethodsInFile(jfParser);
		}

	}
	
	/**
	 * Obtain bindings for all methods in file.
	 *
	 * @param jfParser the jf parser
	 */
	private void obtainBindingsForAllMethodsInFile(JavaFileParser jfParser){
		// get all class
		Set<ClassOrInterfaceDeclaration> classOrInterfaceDeclSet = jfParser.getAllClassOrInterfaceDefinitions();
		// for each class, get all methods
		Iterator<ClassOrInterfaceDeclaration> classOrInterfaceDeclIt = classOrInterfaceDeclSet.iterator();
		while (classOrInterfaceDeclIt.hasNext()) {
			ClassOrInterfaceDeclaration classOrInterfaceDecl = classOrInterfaceDeclIt.next();
			this.obtainBindingsForAllMethodsInClass(jfParser, classOrInterfaceDecl);
		}
	}
	
	/**
	 * Obtain bindings for all methods in class.
	 *
	 * @param jfParser the jf parser
	 * @param classOrInterfaceDecl the class or interface decl
	 */
	private void obtainBindingsForAllMethodsInClass(JavaFileParser jfParser, ClassOrInterfaceDeclaration classOrInterfaceDecl){
		// get all methods
		Set<MethodDeclaration> methodDeclSet = jfParser.getAllMethodDeClarationsByClassOrInterfaceName(classOrInterfaceDecl.getName());
		Iterator<MethodDeclaration> methodDeclIt = methodDeclSet.iterator();
		while (methodDeclIt.hasNext()) {
			MethodDeclaration methodDecl = methodDeclIt.next();
			this.obtainBindingsForMethod(jfParser, classOrInterfaceDecl, methodDecl);
		}
	}
	
	/**
	 * check whether this method can be put into
	 * alreadyResolved list a) if there is no function call
	 * b) if the function calls are out of the project scope
	 * if not, return a map <MethodCallExpr,
	 * MethodDeclaration>.
	 *
	 * @param jfParser the jf parser
	 * @param classOrInterfaceDecl the class or interface decl
	 * @param methodDecl the method decl
	 */
	
	private void obtainBindingsForMethod(JavaFileParser jfParser, ClassOrInterfaceDeclaration classOrInterfaceDecl, MethodDeclaration methodDecl){
		Debug.println("find binding information for " + classOrInterfaceDecl.getName() + " " +methodDecl.getName());
		HashMap<CFGNode<CodeNodeIdentifier, Expression>, MethodDeclaration> methodCallMapping = new HashMap<CFGNode<CodeNodeIdentifier, Expression>, MethodDeclaration>();
		List<CFGNode<CodeNodeIdentifier, Expression>> methodCallNodes = MethodInvocationBinder
				.getAllMethodCallNodes(jfParser.getControlFlowGraph(classOrInterfaceDecl.getName(),methodDecl));
		if(methodCallNodes.isEmpty()){
			this.addResolvedMethod(methodDecl);
		}else{
			for(CFGNode<CodeNodeIdentifier, Expression> cfgNode : methodCallNodes){
				MethodCallExpr methodCallExpr = (MethodCallExpr) cfgNode.getNodeData();
				Debug.println("find binding information for a method call " + methodCallExpr.toString());
				MethodDeclaration methodCalleeDef = MethodInvocationBinder.findMethodDeclaration(methodCallExpr, methodDecl, classOrInterfaceDecl.getName(), 
						jfParser, this);
				if(methodCalleeDef != null){
					methodCallMapping.put(cfgNode, methodCalleeDef);
				}
			}
			if(methodCallMapping.isEmpty()){
				Debug.println("I have function calls but you can not find");
				this.addResolvedMethod(methodDecl);
			}else{
				Debug.println("You have to resolve my method call things!!!!");
				this.addBindingPositions(methodDecl, methodCallMapping);
			}
		}
	}
	
	/**
	 * Checks if is binding already resolved.
	 *
	 * @return true, if is binding already resolved
	 */
	private boolean isBindingAlreadyResolved(){
		if(this.bindingPositions.isEmpty()){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if is execute update method call expression.
	 *
	 * @param exp the exp
	 * @return true, if is execute update method call expression
	 */
	private boolean isExecuteUpdateMethodCallExpression(Expression exp){
		if(ExpressionParser.isMethodCallExpression(exp)){
			MethodCallExpr methodCallExpr = (MethodCallExpr) exp;
			if(methodCallExpr.getName().equals("executeUpdate") || 
					methodCallExpr.getName().equals("execute")){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if is sql statement execution contained.
	 *
	 * @param cfg the cfg
	 * @return true, if is sql statement execution contained
	 */
	private boolean isSqlStatementExecutionContained(CFGGraph<CodeNodeIdentifier, Expression> cfg){
		List<CFGNode<CodeNodeIdentifier, Expression>> cfgNodeList = cfg.getNodeList();
		for(CFGNode<CodeNodeIdentifier, Expression> cfgNode : cfgNodeList){
			Expression expr = cfgNode.getNodeData();
			if(this.isExecuteUpdateMethodCallExpression(expr)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Bind control flow graph for method.
	 * Optimization: 
	 * If the callee function doesn't have sql execution statement,
	 * then it should not be inlined.
	 *
	 * @param methodDecl the method decl
	 * @param bindingInfo the binding info
	 */
	private void bindControlFlowGraphForMethodWithOptimization(MethodDeclaration methodDecl, HashMap<CFGNode<CodeNodeIdentifier, Expression>, MethodDeclaration> bindingInfo){
		Debug.println("Resolved the method " + methodDecl.getName());
		Debug.println(methodDecl.toString());
		//get control flow graph for methodDecl
		CFGGraph<CodeNodeIdentifier, Expression> methodCallerCfg = this.methodCfgMapping.get(methodDecl);
		Debug.println("-------> original cfg <-------");
		//methodCallerCfg.printOut();
		//iterate the bindingInfo list
		//get control flow graph for the callee method
		Iterator<Entry<CFGNode<CodeNodeIdentifier, Expression>, MethodDeclaration>> it = bindingInfo.entrySet().iterator();
		while(it.hasNext()){
			Entry<CFGNode<CodeNodeIdentifier, Expression>, MethodDeclaration> tuple = it.next();
			MethodDeclaration methodCallee = tuple.getValue();
			CFGNode<CodeNodeIdentifier, Expression> callerNode = tuple.getKey();
			CFGGraph<CodeNodeIdentifier, Expression> methodCalleeCfg = this.methodCfgMapping.get(methodCallee);
			//if this methodCalleeCfg does have sql execution statement then you can merge
			if(this.isSqlStatementExecutionContained(methodCalleeCfg)){
				//merge two control flow graph on a particular node
				methodCallerCfg.mergeWithControlFlowPrecedingSpecificNode(callerNode, methodCalleeCfg);
				Debug.println("-------> new cfg after one merge <-------");
				//methodCallerCfg.printOut();
			}else{
				Debug.println("One cfg doesn't have sql execution statement");
			}
		}
		Debug.println("--------> new cfg <--------");
		//methodCallerCfg.printOut();
	}
	
	/**
	 * Bind control flow graph for method.
	 *
	 * @param methodDecl the method decl
	 * @param bindingInfo the binding info
	 */
	private void bindControlFlowGraphForMethodNoOptimization(MethodDeclaration methodDecl, HashMap<CFGNode<CodeNodeIdentifier, Expression>, MethodDeclaration> bindingInfo){
		Debug.println("Resolved the method " + methodDecl.getName());
		//Debug.println(methodDecl.toString());
		//get control flow graph for methodDecl
		CFGGraph<CodeNodeIdentifier, Expression> methodCallerCfg = this.methodCfgMapping.get(methodDecl);
		Debug.println("-------> original cfg <-------");
		//methodCallerCfg.printOut();
		//iterate the bindingInfo list
		//get control flow graph for the callee method
		Iterator<Entry<CFGNode<CodeNodeIdentifier, Expression>, MethodDeclaration>> it = bindingInfo.entrySet().iterator();
		while(it.hasNext()){
			Entry<CFGNode<CodeNodeIdentifier, Expression>, MethodDeclaration> tuple = it.next();
			MethodDeclaration methodCallee = tuple.getValue();
			CFGNode<CodeNodeIdentifier, Expression> callerNode = tuple.getKey();
			CFGGraph<CodeNodeIdentifier, Expression> methodCalleeCfg = this.methodCfgMapping.get(methodCallee);
			//merge two control flow graph on a particular node
			methodCallerCfg.mergeWithControlFlowPrecedingSpecificNode(callerNode, methodCalleeCfg);
			Debug.println("-------> new cfg after one merge <-------");
			//methodCallerCfg.printOut();
		}
		Debug.println("--------> new cfg <--------");
		//methodCallerCfg.printOut();
	}

	/**
	 * Bind control flow graph from callee to caller. Iterate all control flow
	 * graph. Identify methocall expr. Find the method declaration. Merge two
	 * control flow graphs.
	 * For every method, its bindings can be resolved if only if
	 * all methods it call are resolved.
	 * 
	 * If all bindings are resolved, then it terminates.
	 * Optimization 1:
	 * If a callee function doesn't have any sql statement execution,
	 * then this function should not be inlined.
	 */
	public void resolveAllBindings() {
		while(!this.isBindingAlreadyResolved()){
			Debug.println("try to resolve");
			Debug.println("how many left " + this.bindingPositions.size());
			//go over all method declaration
			Iterator<Entry<MethodDeclaration, HashMap<CFGNode<CodeNodeIdentifier, Expression>, MethodDeclaration>>> it = this.bindingPositions.entrySet().iterator();
			List<MethodDeclaration> elementsMustBeRemoved = new ArrayList<MethodDeclaration>();
			while(it.hasNext()){
				Entry<MethodDeclaration, HashMap<CFGNode<CodeNodeIdentifier, Expression>, MethodDeclaration>> tuple = it.next();
				MethodDeclaration methodCaller = tuple.getKey();
				if(this.isAllCalleeMethodDeclarationsResolved(methodCaller)){
					//now you can resolve this method
					if(optimized){
						this.bindControlFlowGraphForMethodWithOptimization(methodCaller, tuple.getValue());
					}else{
						this.bindControlFlowGraphForMethodNoOptimization(methodCaller, tuple.getValue());
					}
					//add this item to the already resolved list
					this.addResolvedMethod(methodCaller);
					//this.removeBindingPositions(methodCaller);
					elementsMustBeRemoved.add(methodCaller);
				}else{
					Debug.println("Resolve this next time " + methodCaller.toString());
					continue;
				}
			}
			for(MethodDeclaration methodCaller : elementsMustBeRemoved){
				this.removeBindingPositions(methodCaller);
			}
		}
	}

	/**
	 * Prints the out file tree.
	 */
	public void printOutFileTree() {
		System.out.println("Print out a directory tree for " + this.getProjectName()
				+ " " + this.getProjectPath());
		this.projectFileTree.printOut();
	}
	
	/**
	 * Prints the out all control flow graphs.
	 */
	public void printOutAllControlFlowGraphs(){
		Debug.println("--------->control flow graph<---------");
		Iterator<Entry<String, JavaFileParser>> it = this.packageControlFlowMapping.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, JavaFileParser> tuple = it.next();
			Debug.println("Package " + tuple.getKey());
			tuple.getValue().printOutControlFlowGraphs();
		}
	}

	/**
	 * Gets the file filter.
	 *
	 * @return the fileFilter
	 */
	public List<String> getFileFilter() {
		return fileFilter;
	}

	/**
	 * Sets the file filter.
	 *
	 * @param fileFilter the fileFilter to set
	 */
	public void setFileFilter(List<String> fileFilter) {
		this.fileFilter = fileFilter;
	}
	
	/**
	 * Adds the file to file filter.
	 *
	 * @param fileName the file name
	 */
	public void addFileToFileFilter(String fileName){
		this.fileFilter.add(fileName);
	}
	
	/**
	 * Should be excluded.
	 *
	 * @param fileName the file name
	 * @return true, if successful
	 */
	public boolean shouldBeExcluded(String fileName){
		for(String filterName : this.getFileFilter()){
			if(fileName.contains(filterName))
				return true;
		}
		return false;
	}
	
	/**
	 * Gets the method cfg mapping.
	 *
	 * @return the method cfg mapping
	 */
	public HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>> getMethodCfgMapping(){
		return this.methodCfgMapping;
	}

}

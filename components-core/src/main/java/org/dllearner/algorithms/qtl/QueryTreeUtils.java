/**
 * 
 */
package org.dllearner.algorithms.qtl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.LiteralNodeConversionStrategy;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.LiteralNodeSubsumptionStrategy;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.NodeType;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.util.Entailment;
import org.dllearner.algorithms.qtl.util.VarGenerator;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.OwlApiJenaUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.expr.E_Datatype;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.ExprNode;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author Lorenz Buehmann
 *
 */
public class QueryTreeUtils {
	
	private static final VarGenerator varGen = new VarGenerator("x");
	private static final String TRIPLE_PATTERN_TEMPLATE = "%s %s %s .";
	private static final OWLDataFactory df = new OWLDataFactoryImpl(false, false);
	
	public static String EMPTY_QUERY_TREE_QUERY = "SELECT ?s WHERE {?s ?p ?o.}";
	
	private static Reasoner reasoner = ReasonerRegistry.getRDFSSimpleReasoner();
	
	/**
	 * Returns the path from the given node to the root of the given tree, i.e.
	 * a list of nodes starting from the given node.
	 * @param tree
	 * @param node
	 */
	public static List<RDFResourceTree> getPathToRoot(RDFResourceTree tree, RDFResourceTree node) {
		if(node.isRoot()) {
			return Collections.singletonList(node);
		}
		List<RDFResourceTree> path = new ArrayList<RDFResourceTree>();
		
		// add node itself
		path.add(node);
		
		// add parent node
		RDFResourceTree parent = node.getParent();
		path.add(parent);
		
		// traversal up to root node
		while(!parent.isRoot()) {
			parent = parent.getParent();
			path.add(parent);
		}
		
		return path;
	}
	
	/**
	 * Print the path from the given node to the root of the given tree, i.e.
	 * a list of nodes starting from the given node.
	 * @param tree
	 * @param node
	 */
	public static String printPathToRoot(RDFResourceTree tree, RDFResourceTree node) {
		List<RDFResourceTree> path = getPathToRoot(tree, node);
		
		StringBuilder sb = new StringBuilder();
		Iterator<RDFResourceTree> iterator = path.iterator();
		
		RDFResourceTree child = iterator.next();
		sb.append(child + "(" + child.getID() + ")");
		while (iterator.hasNext()) {
			RDFResourceTree parent = iterator.next();
			sb.append(" <").append(parent.getEdgeToChild(child)).append("> ");
			sb.append(parent + "(" + parent.getID() + ")");
			child = parent;
		}
		return sb.toString();
	}
	
	/**
	 * Returns all nodes in the given query tree, i.e. the closure of 
	 * the children.
	 * @param tree
	 * @return 
	 */
	public static <N> List<QueryTree<N>> getNodes(QueryTree<N> tree) {
		return tree.getChildrenClosure();
	}
	
	/**
	 * Returns all nodes of the given node type in the query tree, i.e. 
	 * the closure of the children.
	 * @param tree
	 * @return 
	 */
	public static <N> List<QueryTree<N>> getNodes(QueryTree<N> tree, NodeType nodeType) {
		// get all nodes
		List<QueryTree<N>> nodes = tree.getChildrenClosure();
		
		// filter by type
		Iterator<QueryTree<N>> iterator = nodes.iterator();
		while (iterator.hasNext()) {
			QueryTree<N> node = (QueryTree<N>) iterator.next();
			if(node.getNodeType() != nodeType) {
				iterator.remove();
			}
			
		}
		return nodes;
	}
	
	/**
	 * Returns the number of nodes in the given query tree, i.e. the number of 
	 * the children closure.
	 * @param tree
	 * @return 
	 */
	public static <N> int getNrOfNodes(QueryTree<N> tree) {
		return tree.getChildrenClosure().size();
	}
	
	/**
	 * Returns the set of edges that occur in the given query tree, i.e. the 
	 * closure of the edges.
	 * @param tree
	 * @return the set of edges in the query tree
	 */
	public static <N> List<QueryTree<N>> getEdges(QueryTree<N> tree) {
		return tree.getChildrenClosure();
	}
	
	/**
	 * Returns the number of edges that occur in the given query tree, which
	 * is obviously n-1 where n is the number of nodes.
	 * @param tree
	 * @return the set of edges in the query tree
	 */
	public static <N> int getNrOfEdges(QueryTree<N> tree) {
		return getNrOfNodes(tree) - 1;
	}
	
	/**
	 * Returns the complexity of the given query tree. 
	 * <div>
	 * Given a query tree T = (V,E) comprising a set V of vertices or nodes 
	 * together with a set E of edges or links. Moreover we have that 
	 * V = U ∪ L ∪ VAR , where U denotes the nodes that are URIs, L denotes
	 * the nodes that are literals and VAR contains the nodes that are variables.
	 * We define the complexity c(T) of query tree T as follows:
	 * </div>
	 * <code>c(T) = 1 + log(|U| * α + |L| * β + |VAR| * γ) </code>
	 * <div>
	 * with <code>α, β, γ</code> being the weight of the particular node types.
	 * </div>
	 * @param tree
	 * @return the set of edges in the query tree
	 */
	public static <N> double getComplexity(RDFResourceTree tree) {
		
		double varNodeWeight = 0.8;
		double resourceNodeWeight = 1.0;
		double literalNodeWeight = 1.0;
		
		double complexity = 0;
		
		List<RDFResourceTree> nodes = getNodes(tree);
		for (RDFResourceTree node : nodes) {
			if(node.isVarNode()) {
				complexity += varNodeWeight;
			} else if(node.isResourceNode()) {
				complexity += resourceNodeWeight;
			} else if(node.isLiteralNode()) {
				complexity += literalNodeWeight;
			}
		}
		
		return 1 + Math.log(complexity);
	}
	
	/**
	 * Determines if tree1 is subsumed by tree2, i.e. whether tree2 is more general than
	 * tree1.
	 * @param tree1
	 * @param tree2
	 * @return
	 */
    public static <N> boolean isSubsumedBy(QueryTree<N> tree1, QueryTree<N> tree2) {
    	// 1.compare the root nodes
    	// if both nodes denote the same resource or literal
    	if(tree1.isVarNode() && !tree2.isVarNode() && tree1.getUserObject().equals(tree2.getUserObject())){
    		return true;
    	}
    	
    	// if node2 is more specific than node1
    	if(tree1.isVarNode() && !tree2.isVarNode()) {
    		return false;
    	}
    	
    	// 2. compare the children
    	Object edge;
    	for(QueryTree<N> child2 : tree2.getChildren()){
    		boolean isSubsumed = false;
    		edge = tree2.getEdge(child2);
    		for(QueryTree<N> child1 : tree1.getChildren(edge)){
    			if(child1.isSubsumedBy(child2)){
    				isSubsumed = true;
    				break;
    			}
    		}
    		if(!isSubsumed){
				return false;
			}
    	}
    	return true;
    }
    
    /**
     * Returns all nodes in the given query tree.
     * @param tree
     * @return
     */
    public static List<RDFResourceTree> getNodes(RDFResourceTree tree) {
		List<RDFResourceTree> nodes = new ArrayList<RDFResourceTree>();
		nodes.add(tree);
		
		for (RDFResourceTree child : tree.getChildren()) {
			nodes.addAll(getNodes(child));
		}
		
		return nodes;
	}
    
    /**
     * Returns all nodes in the given query tree.
     * @param tree
     * @return
     */
    public static List<RDFResourceTree> getLeafs(RDFResourceTree tree) {
		List<RDFResourceTree> leafs = new ArrayList<RDFResourceTree>();
		
		for (RDFResourceTree node : getNodes(tree)) {
			if(node.isLeaf()) {
				leafs.add(node);
			}
		}
		
		return leafs;
	}
    
    /**
     * Returns the depth of the query tree
     * @param tree
     * @return
     */
    public static int getDepth(RDFResourceTree tree) {
		int maxDepth = 0;
		
		for(RDFResourceTree child : tree.getChildren()) {
			int depth;
			if(child.isLeaf()) {
				depth = 1;
			} else {
				depth = 1 + getDepth(child);
			}
			maxDepth = Math.max(maxDepth, depth);
		}
		
		return maxDepth;
	}
    
    /**
	 * Determines if tree1 is subsumed by tree2, i.e. whether tree2 is more general than
	 * tree1.
	 * @param tree1
	 * @param tree2
	 * @return
	 */
    public static boolean isSubsumedBy(RDFResourceTree tree1, RDFResourceTree tree2) {
    	// 1.compare the root nodes
    	// (T_1 != ?) and (T_2 != ?) --> T_1 = T_2
    	if(tree1.isResourceNode() && tree2.isResourceNode()) {
    		return tree1.getData().equals(tree2.getData());
    	} else if(tree1.isLiteralNode() && tree2.isLiteralNode()) {
    		if(tree1.isLiteralValueNode()) {
    			if(tree2.isLiteralValueNode()) {
    				return tree1.getData().equals(tree2.getData());
    			} else {
    				RDFDatatype d1 = tree1.getData().getLiteralDatatype();
    				return tree2.getDatatype().equals(d1);
    			}
    		} else {
    			if(tree2.isLiteralValueNode()) {
    				return false;
    			} else {
    				RDFDatatype d1 = tree1.getDatatype();
    				return tree2.getDatatype().equals(d1);
    			}
    		}
    		
    	}
    	
    	// (T_1 = ?) and (T_2 != ?) --> FALSE
    	if(tree1.isVarNode() && !tree2.isVarNode()) {
    		return false;
    	}
    	
    	// 2. compare the children
    	for(Node edge2 : tree2.getEdges()){ // for each edge in T_2
    		List<RDFResourceTree> children1 = tree1.getChildren(edge2);
      		if(children1 != null) {
	    		for(RDFResourceTree child2 : tree2.getChildren(edge2)) { // and each child in T_2
	    			boolean isSubsumed = false;
	        		for(RDFResourceTree child1 : children1){ // there has to be at least one child in T_1 that is subsumed 
	        			if(QueryTreeUtils.isSubsumedBy(child1, child2)){ 
	        				isSubsumed = true;
	        				break;
	        			}
	        		}
	        		if(!isSubsumed){
	    				return false;
	    			}
	    		}
      		} else {
      			return false;
      		}
    	}
    	return true;
    }
    
    public static boolean isSubsumedBy(RDFResourceTree tree1, RDFResourceTree tree2, LiteralNodeSubsumptionStrategy strategy) {
		return isSubsumedBy(tree1, tree2);
	}
    
    /**
   	 * Determines if tree1 is subsumed by tree2, i.e. whether tree2 is more general than
   	 * tree1.
   	 * @param tree1
   	 * @param tree2
   	 * @param entailment
   	 * @return
   	 */
	public static boolean isSubsumedBy(RDFResourceTree tree1, RDFResourceTree tree2, Entailment entailment) {
		Resource root = ResourceFactory.createResource("http://example.org/root");
		
		Model m1 = toModel(tree1, root);
		Model m2 = toModel(tree2, root);
		
		Model m1closure = ModelFactory.createDefaultModel();
		m1closure.add(ModelFactory.createInfModel(reasoner, m1));
		
		Model m2closure = ModelFactory.createDefaultModel();
		m2closure.add(ModelFactory.createInfModel(reasoner, m2));
		
		boolean sameClosure = m1closure.isIsomorphicWith(m2closure);
		if(sameClosure) {
			return true;
		}

		// check if each statement of m1 is contained in m2
		StmtIterator iterator = m1closure.listStatements();
		while (iterator.hasNext()) {
			Statement st = iterator.next();
			if (!st.getSubject().isAnon() && !st.getObject().isAnon() && !m2closure.contains(st)) {
				return false;
			} 
		}
		return true;
	}
    
    /**
   	 * Determines if tree1 is subsumed by tree2, i.e. whether tree2 is more general than
   	 * tree1.
   	 * @param tree1
   	 * @param tree2
   	 * @return
   	 */
       public static boolean isSubsumedBy(RDFResourceTree tree1, RDFResourceTree tree2, SPARQLReasoner reasoner) {
       	// 1.compare the root nodes
       	
       	// (T_1 != ?) and (T_2 != ?) --> T_1 = T_2
       	if(!tree1.isVarNode() && !tree2.isVarNode()) {
       		if(tree1.isResourceNode() && tree2.isResourceNode()) {
       			
       		}
       		return tree1.getData().equals(tree2.getData());
       	}
       	
       	// (T_1 = ?) and (T_2 != ?) --> FALSE
       	if(tree1.isVarNode() && !tree2.isVarNode()) {
       		return false;
       	}
       	
       	// 2. compare the children
       	for(Node edge2 : tree2.getEdges()){
       		List<RDFResourceTree> children1 = tree1.getChildren(edge2);
      		if(children1 != null) {
	       		for(RDFResourceTree child2 : tree2.getChildren(edge2)) {
	       			boolean isSubsumed = false;
	       			
	           		for(RDFResourceTree child1 : children1){
	           			if(QueryTreeUtils.isSubsumedBy(child1, child2, reasoner, edge2.equals(RDF.type.asNode()))){
	           				isSubsumed = true;
	           				break;
	           			}
	           		}
	           		if(!isSubsumed){
	       				return false;
	       			}
	       		}
      		} else {
      			return false;
      		}
       	}
       	return true;
       }
       
       /**
      	 * Determines if tree1 is subsumed by tree2, i.e. whether tree2 is more general than
      	 * tree1.
      	 * @param tree1
      	 * @param tree2
      	 * @return
      	 */
          public static boolean isSubsumedBy(RDFResourceTree tree1, RDFResourceTree tree2, AbstractReasonerComponent reasoner, boolean typeNode) {
          	// 1.compare the root nodes
          	
          	// (T_1 != ?) and (T_2 != ?) --> T_1 = T_2
          	if(!tree1.isVarNode() && !tree2.isVarNode()) {
          		if(tree1.getData().equals(tree2.getData())) {
          			return true;
          		} else if(typeNode && tree1.isResourceNode() && tree2.isResourceNode()) {
          			return reasoner.isSuperClassOf(
          					new OWLClassImpl(IRI.create(tree2.getData().getURI())), 
          					new OWLClassImpl(IRI.create(tree1.getData().getURI())));
          		}
          		return false;
          	}
          	
          	// (T_1 = ?) and (T_2 != ?) --> FALSE
          	if(tree1.isVarNode() && !tree2.isVarNode()) {
          		return false;
          	}
          	
          	if(typeNode) {
          		return isSubsumedBy(tree1, tree2, Entailment.RDFS);
          	}
          	
          	// 2. compare the children
          	for(Node edge2 : tree2.getEdges()){
          		for(RDFResourceTree child2 : tree2.getChildren(edge2)) {
          			boolean isSubsumed = false;
              		List<RDFResourceTree> children = tree1.getChildren(edge2);
              		if(children != null) {
              			for(RDFResourceTree child1 : children){
                  			if(QueryTreeUtils.isSubsumedBy(child1, child2, reasoner, edge2.equals(RDF.type.asNode()))){
                  				isSubsumed = true;
                  				break;
                  			}
                  		}
              		}
              		if(!isSubsumed){
          				return false;
          			}
          		}
          	}
          	return true;
          }
    
    /**
	 * Determines if the trees are equivalent from a subsumptional point of view.
	 * @param trees
	 * @return
	 */
    public static <N> boolean sameTrees(QueryTree<N>... trees) {
    	for(int i = 0; i < trees.length; i++) {
    		QueryTree<N> tree1 = trees[i];
    		for(int j = i; j < trees.length; j++) {
    			QueryTree<N> tree2 = trees[j];
    			if(!sameTrees(tree1, tree2)) {
    				return false;
    			}
        	}
    	}
    	
    	return true;
    }
    
	/**
	 * Determines if both trees are equivalent from a subsumptional point of
	 * view.
	 * 
	 * @param tree1
	 * @param tree2
	 * @return
	 */
	public static <N> boolean sameTrees(QueryTree<N> tree1, QueryTree<N> tree2) {
		return isSubsumedBy(tree1, tree2) && isSubsumedBy(tree2, tree1);
	}
	
	public static <N> boolean sameTrees(RDFResourceTree tree1, RDFResourceTree tree2) {
		return isSubsumedBy(tree1, tree2) && isSubsumedBy(tree2, tree1);
	}
	
	public static Model toModel(RDFResourceTree tree) {
		Model model = ModelFactory.createDefaultModel();
		buildModel(model, tree, model.asRDFNode(NodeFactory.createAnon()).asResource());
		return model;
	}
	
	public static Model toModel(RDFResourceTree tree, Resource subject) {
		Model model = ModelFactory.createDefaultModel();
		buildModel(model, tree, subject);
		return model;
	}
	
	private static void buildModel(Model model, RDFResourceTree tree, Resource subject) {
		for (Node edge : tree.getEdges()) {
			Property p = model.getProperty(edge.getURI());
			for (RDFResourceTree child : tree.getChildren(edge)) {
				RDFNode object = child.isVarNode() ? model.asRDFNode(NodeFactory.createAnon()).asResource() : model
						.asRDFNode(child.getData());
				model.add(subject, p, object);
				if (child.isVarNode()) {
					buildModel(model, child, object.asResource());
				}
			}
		}
	}
	
	public static OWLClassExpression toOWLClassExpression(RDFResourceTree tree) {
    	return toOWLClassExpression(tree, LiteralNodeConversionStrategy.DATATYPE);
	}
	
	public static OWLClassExpression toOWLClassExpression(RDFResourceTree tree, LiteralNodeConversionStrategy literalConversion) {
    	return buildOWLClassExpression(tree, literalConversion);
	}
	
	private static OWLClassExpression buildOWLClassExpression(RDFResourceTree tree, LiteralNodeConversionStrategy literalConversion) {
		Set<OWLClassExpression> classExpressions = new HashSet<OWLClassExpression>();
		for(Node edge : tree.getEdges()) {
			for (RDFResourceTree child : tree.getChildren(edge)) {
				if(edge.equals(RDF.type.asNode()) || edge.equals(RDFS.subClassOf.asNode())) {
					if(child.isVarNode()) {
						classExpressions.add(buildOWLClassExpression(child, literalConversion));
					} else {
						classExpressions.add(df.getOWLClass(IRI.create(child.getData().getURI())));
					}
				} else {
					// create r some C
					if(child.isLiteralNode()) {
						OWLDataProperty dp = df.getOWLDataProperty(IRI.create(edge.getURI()));
						if(!child.isLiteralValueNode()) {
							classExpressions.add(df.getOWLDataSomeValuesFrom(dp, df.getOWLDatatype(IRI.create(child.getDatatype().getURI()))));
						} else {
							OWLLiteral value = OwlApiJenaUtils.getOWLLiteral(child.getData().getLiteral());
							classExpressions.add(df.getOWLDataHasValue(dp, value));
						}
						
					} else {
						OWLClassExpression filler = null;
						if(child.isVarNode()) {
							filler = buildOWLClassExpression(child, literalConversion);
							classExpressions.add(df.getOWLObjectSomeValuesFrom(
									df.getOWLObjectProperty(IRI.create(edge.getURI())), 
									filler));
						} else if (child.isResourceNode()) {
							classExpressions.add(df.getOWLObjectHasValue(
									df.getOWLObjectProperty(IRI.create(edge.getURI())), 
									df.getOWLNamedIndividual(IRI.create(child.getData().getURI()))));
						}
					}
				}
			}
		}
		if(classExpressions.isEmpty()) {
			return df.getOWLThing();
		} else if(classExpressions.size() == 1){
    		return classExpressions.iterator().next();
    	} else {
    		return df.getOWLObjectIntersectionOf(classExpressions);
    	}
	}
	
	/**
	 * Returns a SPARQL query representing the query tree. Note, for empty trees
	 * it just returns 
	 * <p><code>SELECT ?s WHERE {?s ?p ?o.}</code></p>
	 * @param tree
	 * @return
	 */
	public static Query toSPARQLQuery(RDFResourceTree tree) {
		return QueryFactory.create(toSPARQLQueryString(tree));
	}
	
	public static String toSPARQLQueryString(RDFResourceTree tree) {
    	return toSPARQLQueryString(tree, PrefixMapping.Standard);
    }
	
	public static String toSPARQLQueryString(RDFResourceTree tree, PrefixMapping pm) {
    	return toSPARQLQueryString(tree, null, pm, LiteralNodeConversionStrategy.DATATYPE);
    }
	
	public static String toSPARQLQueryString(RDFResourceTree tree, String baseIRI, PrefixMapping pm) {
    	return toSPARQLQueryString(tree, baseIRI, pm, LiteralNodeConversionStrategy.DATATYPE);
    }
	
	public static String toSPARQLQueryString(RDFResourceTree tree, String baseIRI, PrefixMapping pm, LiteralNodeConversionStrategy literalConversion) {
		if(!tree.hasChildren()){
    		return EMPTY_QUERY_TREE_QUERY;
    	}
    	
    	varGen.reset();
    	
    	SerializationContext context = new SerializationContext(pm);
    	context.setBaseIRI(baseIRI);
    	
    	StringBuilder sb = new StringBuilder();
    	
    	// Add BASE declaration
        if (baseIRI != null) {
            sb.append("BASE ");
            sb.append(FmtUtils.stringForURI(baseIRI, null, null));
            sb.append('\n');
        }

        // Then pre-pend prefixes
        for (String prefix : pm.getNsPrefixMap().keySet()) {
            sb.append("PREFIX ");
            sb.append(prefix);
            sb.append(": ");
            sb.append(FmtUtils.stringForURI(pm.getNsPrefixURI(prefix), null, null));
            sb.append('\n');
        }
        
        List<ExprNode> filters = new ArrayList<>();
        
        // target var
        String targetVar = "?s";
        
        // header
    	sb.append(String.format("SELECT DISTINCT %s WHERE {\n", targetVar));
    	
    	// triple patterns
    	buildSPARQLQueryString(tree, targetVar, sb, filters, context);
        
    	// filters
    	if(!filters.isEmpty()) {
    		Iterator<ExprNode> it = filters.iterator();
    		ExprNode filter = it.next();
    		while(it.hasNext()) {
    			filter = new E_LogicalAnd(filter, it.next());
    		}
    		sb.append("FILTER(").append(filter.toString()).append(")\n");
    	}
    	
        sb.append("}");
    	
    	Query query = QueryFactory.create(sb.toString(), Syntax.syntaxSPARQL_11);
    	query.setPrefixMapping(pm);
    	
    	return query.toString();
	}
    
	private static void buildSPARQLQueryString(RDFResourceTree tree,
			String subjectStr, StringBuilder sb, Collection<ExprNode> filters,
			SerializationContext context) {
		if (!tree.isLeaf()) {
			for (Node edge : tree.getEdges()) {
				// process predicate
				String predicateStr = FmtUtils.stringForNode(edge, context);
				for (RDFResourceTree child : tree.getChildren(edge)) {
					// pre-process object
					Node object = child.getData();
					
					if(child.isVarNode()) {
						// set a fresh var in the SPARQL query
						object = varGen.newVar();
					} else if(child.isLiteralNode() && !child.isLiteralValueNode()) { 
						// set a fresh var in the SPARQL query
						object = varGen.newVar();
						
						// literal node describing a set of literals is rendered depending on the conversion strategy
						if(child.getDatatype() != null) {
							ExprNode filter = new E_Equals(
									new E_Datatype(new ExprVar(object)), 
									NodeValue.makeNode(NodeFactory.createURI(child.getDatatype().getURI())));
							filters.add(filter);
						}
						
					} 
					
					// process object
					String objectStr = FmtUtils.stringForNode(object, context);
					sb.append(String.format(TRIPLE_PATTERN_TEMPLATE, subjectStr, predicateStr, objectStr)).append("\n");
					
					/*
					 * only if child is var node recursively process children if
					 * exist because for URIs it doesn't make sense to add the
					 * triple pattern and for literals there can't exist a child
					 * in the tree
					 */
					if (child.isVarNode()) {
						buildSPARQLQueryString(child, objectStr, sb, filters, context);
					}
				}
			}
		}
    }
	
	/**
	 * Remove trivial statements according to the given entailment semantics:
	 * <h3>RDFS</h3>
	 * <ul>
	 * <li>remove trivial statements like <code>?s a ?x</code>
	 * <li>remove type statements if this is given by domain and range 
	 * of used statements.</li>
	 * 
	 * </ul>
	 * @param tree
	 * @param entailment
	 */
	public static void prune(RDFResourceTree tree, AbstractReasonerComponent reasoner, Entailment entailment) {
		
		// remove trivial statements
		for(Node edge : new TreeSet<Node>(tree.getEdges())) {
			if(edge.equals(RDF.type.asNode())) { // check outgoing rdf:type edges
				List<RDFResourceTree> children = new ArrayList<RDFResourceTree>(tree.getChildren(edge));
				for (Iterator<RDFResourceTree> iterator = children.iterator(); iterator.hasNext();) {
					RDFResourceTree child = iterator.next();
					if(!isNonTrivial(child, entailment)) {
						tree.removeChild(child, edge);
					}
				}
			} else {// recursively apply pruning on all subtrees
				List<RDFResourceTree> children = tree.getChildren(edge);
				
				for (RDFResourceTree child : children) {
					prune(child, reasoner, entailment);
				}
			}
		}
		
		// we have to run the subsumption check one more time to prune the tree
		for (Node edge : tree.getEdges()) {
			Set<RDFResourceTree> children2Remove = new HashSet<RDFResourceTree>();
			List<RDFResourceTree> children = tree.getChildren(edge);
			for(int i = 0; i < children.size(); i++) {
				RDFResourceTree child1 = children.get(i);
				if(!children2Remove.contains(child1)) {
					for(int j = i + 1; j < children.size(); j++) {
						RDFResourceTree child2 = children.get(j);
//						System.out.println(QueryTreeUtils.getPathToRoot(tree, child1));
//						System.out.println(QueryTreeUtils.getPathToRoot(tree, child2));
						if(!children2Remove.contains(child2)) {
							if (QueryTreeUtils.isSubsumedBy(child1, child2)) {
								children2Remove.add(child2);
							} else if (QueryTreeUtils.isSubsumedBy(child2, child1)) {
								children2Remove.add(child1);
							}
						}
					}
				}
				
			}
			
			for (RDFResourceTree child : children2Remove) {
				tree.removeChild(child, edge);
			}
		}
		
//		if(entailment == Entailment.RDFS) {
//			if(reasoner != null) {
//				List<RDFResourceTree> typeChildren = tree.getChildren(RDF.type.asNode());
//				
//				// compute implicit types
//				Set<OWLClassExpression> implicitTypes = new HashSet<OWLClassExpression>();
//				for(Node edge : tree.getEdges()) {
//					if(!edge.equals(RDF.type.asNode())) {
//						// get domain for property
//						implicitTypes.add(reasoner.getDomain(new OWLObjectPropertyImpl(IRI.create(edge.getURI()))));
//					}
//				}
//				if(typeChildren != null) {
//					// remove type children which are already covered implicitely
//					for (RDFResourceTree child : new ArrayList<RDFResourceTree>(tree.getChildren(RDF.type.asNode()))) {
//						if(child.isResourceNode() && implicitTypes.contains(new OWLClassImpl(IRI.create(child.getData().getURI())))) {
//							tree.removeChild(child, RDF.type.asNode());
//							System.out.println("removing " + child.getData().getURI());
//						}
//					}
//				}
//				
//			}
//		}
	}
	
	/**
	 * Recursively removes edges that lead to a leaf node which is a variable.
	 * @param tree
	 * @param entailment
	 */
	public static boolean removeVarLeafs(RDFResourceTree tree) {
		SortedSet<Node> edges = new TreeSet<>(tree.getEdges());
		
		boolean modified = false;
		for (Node edge : edges) {
			List<RDFResourceTree> children = new ArrayList<>(tree.getChildren(edge));
//			
			for (RDFResourceTree child : children) {
				if(child.isLeaf() && child.isVarNode()) {
					tree.removeChild(child, edge);
					modified = true;
				} else {
					modified = removeVarLeafs(child);
					if(modified && child.isLeaf() && child.isVarNode()) {
						tree.removeChild(child, edge);
						modified = true;
					}
				}
			}
		}
		return modified;
	}
	
	public static boolean isNonTrivial(RDFResourceTree tree, Entailment entailment) {
		if(tree.isResourceNode() || tree.isLiteralNode()){
    		return true;
    	} else {
    		for (Node edge : tree.getEdges()) {
    			for (RDFResourceTree child : tree.getChildren(edge)) {
        			if(!edge.equals(RDFS.subClassOf.asNode())){
        				return true;
        			} else if(child.isResourceNode()){
        				return true;
        			} else if(isNonTrivial(child, entailment)){
        				return true;
        			}
        		}
			}
    		
    	}
    	return false;
	}
}
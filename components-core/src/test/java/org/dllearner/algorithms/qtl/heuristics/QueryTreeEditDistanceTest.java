package org.dllearner.algorithms.qtl.heuristics;

import static junit.framework.Assert.assertEquals;

import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.NodeType;
import org.junit.Test;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class QueryTreeEditDistanceTest {
    /*
     *  A     A
     */
    @Test
    public void test01() {
        RDFResourceTree tree1 = new RDFResourceTree(NodeFactory.createURI("A"));
        RDFResourceTree tree2 = new RDFResourceTree(NodeFactory.createURI("A"));

        double expectedDistance = 0;
        assertEquals(expectedDistance, QueryTreeEditDistance.getDistance(tree1, tree2));
    }

    /*
     * A1     A2
     */
    @Test
    public void test02() {
    	RDFResourceTree tree1 = new RDFResourceTree(NodeFactory.createURI("A1"));
    	RDFResourceTree tree2 = new RDFResourceTree(NodeFactory.createURI("A2"));

        double expectedDistance = 1;
        assertEquals(expectedDistance, QueryTreeEditDistance.getDistance(tree1, tree2));
    }

    /*
     *  A     A
     *  |
     *  B
     */
    @Test
    public void test03() {
        // tree 1:
        // A
    	RDFResourceTree tree1 = new RDFResourceTree(NodeFactory.createURI("A"));
        // B
    	RDFResourceTree child1_1 = new RDFResourceTree(NodeFactory.createURI("B"));
        tree1.addChild(child1_1, NodeFactory.createURI("p"));

        // tree 2:
        // A
        RDFResourceTree tree2 = new RDFResourceTree(NodeFactory.createURI("A"));

        double expectedDistance = 1;
        assertEquals(expectedDistance, QueryTreeEditDistance.getDistance(tree1, tree2));
    }

    /*
     *  A     A
     *        |
     *        B
     */
    @Test
    public void test04() {
        // tree 1:
        // A
    	RDFResourceTree tree1 = new RDFResourceTree(NodeFactory.createURI("A"));

        // tree 2:
        // A
    	RDFResourceTree tree2 = new RDFResourceTree(NodeFactory.createURI("A"));
        // B
    	RDFResourceTree child2_1 = new RDFResourceTree(NodeFactory.createURI("B"));
        tree2.addChild(child2_1, NodeFactory.createURI("p"));

        double expectedDistance = 1;
        assertEquals(expectedDistance, QueryTreeEditDistance.getDistance(tree1, tree2));
    }

    /*
     *   A       A
     *  / \      |
     * B   C     C
     *     |
     *     D
     */
    @Test
    public void test05() {
        // tree 1:
        // A
    	RDFResourceTree tree1 = new RDFResourceTree(NodeFactory.createURI("A"));
        // B
    	RDFResourceTree child1_1 = new RDFResourceTree(NodeFactory.createURI("B"));
        // C
    	RDFResourceTree child1_2 = new RDFResourceTree(NodeFactory.createURI("C"));
        // D
    	RDFResourceTree grandChild1_2_1 = new RDFResourceTree(NodeFactory.createURI("D"));

        child1_2.addChild(grandChild1_2_1, NodeFactory.createURI("r"));
        tree1.addChild(child1_1, NodeFactory.createURI("p"));
        tree1.addChild(child1_2, NodeFactory.createURI("q"));

        // tree 2:
        // A
        RDFResourceTree tree2 = new RDFResourceTree(NodeFactory.createURI("A"));
        // C
        RDFResourceTree child2_1 = new RDFResourceTree(NodeFactory.createURI("C"));

        tree2.addChild(child2_1, NodeFactory.createURI("q"));

        double expectedDistance = 2;
        assertEquals(expectedDistance, QueryTreeEditDistance.getDistance(tree1, tree2));
    }

    /*
     *   A       A
     *  / \
     * B   C
     *     |
     *     D
     */
    @Test
    public void test06() {
    	// tree 1:
        // A
    	RDFResourceTree tree1 = new RDFResourceTree(NodeFactory.createURI("A"));
        // B
    	RDFResourceTree child1_1 = new RDFResourceTree(NodeFactory.createURI("B"));
        // C
    	RDFResourceTree child1_2 = new RDFResourceTree(NodeFactory.createURI("C"));
        // D
    	RDFResourceTree grandChild1_2_1 = new RDFResourceTree(NodeFactory.createURI("D"));

        child1_2.addChild(grandChild1_2_1, NodeFactory.createURI("r"));
        tree1.addChild(child1_1, NodeFactory.createURI("p"));
        tree1.addChild(child1_2, NodeFactory.createURI("q"));

        // tree 2:
        // A
        RDFResourceTree tree2 = new RDFResourceTree(NodeFactory.createURI("A"));

        double expectedDistance = 3;
        assertEquals(expectedDistance, QueryTreeEditDistance.getDistance(tree1, tree2));
    }

    /*
     *    A         A       | 0
     *   /|\       /|\
     *  B C D     G C H     | 2
     *  |   |     |   |
     *  E   F     E   F     | 0
     *
     *  --> dist = 2
     */
    @Test
    public void test07() {
        // tree 1:
        // A
    	RDFResourceTree tree1 = new RDFResourceTree(NodeFactory.createURI("A"));
        // B
    	RDFResourceTree child1_1 = new RDFResourceTree(NodeFactory.createURI("B"));
        // C
    	RDFResourceTree child1_2 = new RDFResourceTree(NodeFactory.createURI("C"));
        // D
    	RDFResourceTree child1_3 = new RDFResourceTree(NodeFactory.createURI("D"));
        // E
    	RDFResourceTree child1_1_1 = new RDFResourceTree(NodeFactory.createURI("E"));
        // F
    	RDFResourceTree child1_3_1 = new RDFResourceTree(NodeFactory.createURI("F"));

        child1_1.addChild(child1_1_1, NodeFactory.createURI("s"));
        child1_3.addChild(child1_3_1, NodeFactory.createURI("t"));
        tree1.addChild(child1_1, NodeFactory.createURI("p"));
        tree1.addChild(child1_2, NodeFactory.createURI("q"));
        tree1.addChild(child1_3, NodeFactory.createURI("r"));

        // tree 2:
        // A
        RDFResourceTree tree2 = new RDFResourceTree(NodeFactory.createURI("A"));
        // G
        RDFResourceTree child2_1 = new RDFResourceTree(NodeFactory.createURI("G"));
        // C
        RDFResourceTree child2_2 = new RDFResourceTree(NodeFactory.createURI("C"));
        // H
        RDFResourceTree child2_3 = new RDFResourceTree(NodeFactory.createURI("H"));
        // E
        RDFResourceTree child2_1_1 = new RDFResourceTree(NodeFactory.createURI("E"));
        // F
        RDFResourceTree child2_3_1 = new RDFResourceTree(NodeFactory.createURI("F"));

        child2_1.addChild(child2_1_1, NodeFactory.createURI("s"));
        child2_3.addChild(child2_3_1, NodeFactory.createURI("t"));
        tree2.addChild(child2_1, NodeFactory.createURI("p"));
        tree2.addChild(child2_2, NodeFactory.createURI("q"));
        tree2.addChild(child2_3, NodeFactory.createURI("r"));

        double expectedDistance = 2;
        assertEquals(expectedDistance, QueryTreeEditDistance.getDistance(tree1, tree2));
    }

    @Test
    public void test08() {
    	/*
    	 *  A	--p--> ?
    	 *  			--p--> A1
    	 */
    	RDFResourceTree tree1 = new RDFResourceTree(NodeFactory.createURI("A"));
    	RDFResourceTree subTree1 = new RDFResourceTree();
    	subTree1.addChild(new RDFResourceTree(NodeFactory.createURI("A1")), NodeFactory.createURI("p"));
    	tree1.addChild(subTree1, NodeFactory.createURI("p"));

    	/*
    	 *  B	--p--> B1
    	 *  			 --p--> A1
    	 */
    	RDFResourceTree tree2 = new RDFResourceTree(NodeFactory.createURI("B"));
    	RDFResourceTree subTree2 = new RDFResourceTree(NodeFactory.createURI("B1"));
    	subTree2.addChild(new RDFResourceTree(NodeFactory.createURI("A1")), NodeFactory.createURI("p"));
    	tree2.addChild(subTree2, NodeFactory.createURI("p"));

    	/*
    	 *  C	--p--> ?
    	 *  			--p--> ?
    	 */
    	RDFResourceTree tree3 = new RDFResourceTree(NodeFactory.createURI("C"));
    	RDFResourceTree subTree3 = new RDFResourceTree();
    	subTree3.addChild(new RDFResourceTree(), NodeFactory.createURI("p"));
    	tree3.addChild(subTree3, NodeFactory.createURI("p"));

        System.out.println(tree1.getStringRepresentation());
        System.out.println(tree2.getStringRepresentation());
        System.out.println(tree3.getStringRepresentation());

        double distance1_2 = QueryTreeEditDistance.getDistanceApprox(tree1, tree2);
        double distance1_3 = QueryTreeEditDistance.getDistanceApprox(tree1, tree3);
        double distance2_3 = QueryTreeEditDistance.getDistanceApprox(tree2, tree3);

        System.out.println("d(t1,t2) = " + distance1_2);
        System.out.println("d(t1,t3) = " + distance1_3);
        System.out.println("d(t2,t3) = " + distance2_3);


        boolean moreSimilar = distance1_2 < distance1_3;

        assertEquals(distance1_2 < distance1_3, moreSimilar);
    }
}
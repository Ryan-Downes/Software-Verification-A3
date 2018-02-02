/*** Class to hold Upward Exposed Uses analyis. Extend appropriate class. 

Author: Harman Sran , Ryan Downes, Michael Fang Li
FINAL VERSION

Modified from LiveVariableAnalysis.java:
http://www.cs.toronto.edu/~aamodkore/notes/dfa-tutorial/live-analysis-example.html

***/
package csc410.hw3;

import java.util.*;
import java.io.*;

import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ArraySparseSet;

public class UpwardExposedUses 
	extends BackwardFlowAnalysis<Unit, FlowSet<List<Object>>> 
{
	
	private FlowSet<List<Object>> emptySet;

	public UpwardExposedUses(DirectedGraph g) {
		// First obligation
		super(g);
		
		// Create the emptyset
		emptySet = new ArraySparseSet<List<Object>>();
		
		// Second obligation
		doAnalysis();
        
        /* Parse DirectedGraph g and output EUs to exposed-uses.txt */
        
        // Open file stream (overwrite's exposed-uses.txt if exists)
        PrintWriter f = null;
        try{
            f = new PrintWriter("./exposed-uses.txt", "UTF-8");
        }
        catch (Exception e){
            System.out.println(e);
        }
        
        /* 
            Iterate through Unit nodes of graph,
            for each elem in exit of node n (FlowSet<List<Object>> exitSet = this.getFlowAfter(n))
            println: n (auto toString)          {Unit Node - where Value is UE to}
            println: elem[1] (auto toString)    {Unit Node - where Value was read}
            println: elem[0] (auto toString)    {Value}
        */
        Iterator<Unit> unitIt = g.iterator();

        while (unitIt.hasNext()){
            Unit n = unitIt.next();
            FlowSet<List<Object>> exitSet = this.getFlowAfter(n);

            for (List<Object> elem: exitSet){

                f.println(n.toString().replace("\n", ""));
                f.println(elem.get(1).toString().replace("\n", ""));
                f.println(elem.get(0));
                //f.println(); // TODO REMOVE THIS
            }
        }
        f.close();
	}
	

	// This method performs the joining of successor nodes
	// Since live variables is a may analysis we join by union 
	@Override
	protected void merge(FlowSet<List<Object>> inSet1, 
		FlowSet<List<Object>> inSet2, 
		FlowSet<List<Object>> outSet) 
	{
		inSet1.union(inSet2, outSet);
	}


	@Override
	protected void copy(FlowSet<List<Object>> srcSet, 
		FlowSet<List<Object>> destSet) 
	{
		srcSet.copy(destSet);
	}

	
	// Used to initialize the in and out sets for each node. In
	// our case we build up the sets as we go, so we initialize
	// with the empty set.
	@Override
	protected FlowSet<List<Object>> newInitialFlow() {
		return emptySet.clone();
	}


	// Returns FlowSet representing the initial set of the entry
	// node. In our case the entry node is the last node and it
	// should contain the empty set.
	@Override
	protected FlowSet<List<Object>> entryInitialFlow() {
		return emptySet.clone();
	}

	
	// Sets the outSet with the values that flow through the 
	// node from the inSet based on reads/writes at the node
	// Set the outSet (entry) based on the inSet (exit)
    // outSet is the set at entry of the node
	// inSet is the set at exit of the node
	@Override
	protected void flowThrough(FlowSet<List<Object>> inSet, 
		Unit node, FlowSet<List<Object>> outSet) {
	
        // Make outset a copy of inset (we will modify outset)
        copy(inSet, outSet);
        
        /* Remove any (var, *) from outSet if var is in
           node's def boxes (being written to at node) */
		for (ValueBox def: node.getDefBoxes()) {
			if (def.getValue() instanceof Local) {
			    
                // Go through outSet (contains ArrayLists)
                Iterator setIt = outSet.iterator();
                while (setIt.hasNext()){
                    List<Object> elem = (List<Object>)setIt.next();

                    // If the elem has a variable we're writing to, remove it from outSet (regardless of node)
                    if (elem.get(0).equals((Local) def.getValue())) {
                        outSet.remove(elem);
                    }
                }
			}
		}

        /* Add everything in use boxes (being read at node)
           as ArrayList of form [use.getValue(), node] */
		for (ValueBox use: node.getUseBoxes()) {
			if (use.getValue() instanceof Local) {
                
                List<Object> newElem = new ArrayList <Object>();
                newElem.add((Local) use.getValue());
                newElem.add(node);

				outSet.add(newElem);
			}
		}
	}
}

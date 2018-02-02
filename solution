package csc410.hw3;

import java.util.*;
import java.io.PrintWriter;

import soot.*;
import soot.toolkits.scalar.*;
import soot.toolkits.graph.DirectedGraph;

public class UpwardExposedUses extends BackwardFlowAnalysis<Unit, FlowSet<LocalUnitPair>> {
	
	public UpwardExposedUses(DirectedGraph g) {
		
		super(g);

		doAnalysis();

		try {
			PrintWriter writer = new PrintWriter("exposed-uses.txt");
			
			Iterator<Unit> unitIt = graph.iterator();
			while (unitIt.hasNext()) {
				Unit s = unitIt.next();
				FlowSet<LocalUnitPair> set = this.getFlowAfter(s);
				for (LocalUnitPair eUse: set) {
					writer.println(s+"\n"+eUse.getUnit()+"\n"+eUse.getLocal());
				}
			}
			
			writer.close();
		} catch (Exception ex) {
			System.err.println("Unable to write to file: exposed-uses.txt");
		}
	}

	@Override
	protected void merge(FlowSet<LocalUnitPair> in1,FlowSet<LocalUnitPair> in2, FlowSet<LocalUnitPair> out) {
		in1.union(in2, out);
	}


	@Override
	protected void copy(FlowSet<LocalUnitPair> src, FlowSet<LocalUnitPair> dest) {
		src.copy(dest);
	}

	
	@Override
	protected FlowSet<LocalUnitPair> newInitialFlow() { return new ArraySparseSet<LocalUnitPair>(); }


	@Override
	protected FlowSet<LocalUnitPair> entryInitialFlow() { return new ArraySparseSet<LocalUnitPair>(); }

	
	@Override
	protected void flowThrough(FlowSet<LocalUnitPair> inSet, Unit node, FlowSet<LocalUnitPair> outSet) {
		
		inSet.copy(outSet);
		for (ValueBox def: node.getDefBoxes()) {
			for (LocalUnitPair eUse: inSet) {
				if (eUse.getLocal().equals(def.getValue())) {
					outSet.remove(eUse);
				}
			}
		}

		for (ValueBox use: node.getUseBoxes()) {
			if (use.getValue() instanceof Local) {
				outSet.add(new LocalUnitPair((Local) use.getValue(), node));
			}
		}
	}
}

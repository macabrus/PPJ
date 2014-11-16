import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

/**
 * DKA constructed from e-NKA
 * 
 * @author Ivan Paljak
 */
public class DKA {
	
	public class DKATransition {

		public Integer from;
		public Integer to;

		public String edge;

		public DKATransition(Integer from, Integer to, String edge) {
			this.from = from;
			this.to = to;
			this.edge = edge;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((edge == null) ? 0 : edge.hashCode());
			result = prime * result + ((from == null) ? 0 : from.hashCode());
			result = prime * result + ((to == null) ? 0 : to.hashCode());
			return result;
		}

		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DKATransition other = (DKATransition) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (edge == null) {
				if (other.edge != null)
					return false;
			} else if (!edge.equals(other.edge))
				return false;
			if (from == null) {
				if (other.from != null)
					return false;
			} else if (!from.equals(other.from))
				return false;
			if (to == null) {
				if (other.to != null)
					return false;
			} else if (!to.equals(other.to))
				return false;
			return true;
		}

		private DKA getOuterType() {
			return DKA.this;
		}

	}

	public class Cluster {
		
		public HashSet<EpsNKAState> contents = new HashSet<EpsNKAState>();
		
		public Cluster() {}
		
		public Cluster(HashSet<EpsNKAState> contents) {
			for (EpsNKAState s : contents) {
				EpsNKAState newState = new EpsNKAState();
				newState.copyState(s);
				this.contents.add(s);
			}
		}
		
		public Cluster(EpsNKAState state) {
			this.contents.add(state);
		}
		
		public void fromCluster(Cluster x) {
			for (EpsNKAState s : x.contents) {
				EpsNKAState newState = new EpsNKAState();
				newState.copyState(s);
				this.contents.add(s);
			}
		}
		
		public void addState(EpsNKAState s) {
			contents.add(s);
		}
		
		public void mergeContents(HashSet<EpsNKAState> c) {
			for (EpsNKAState s : c) {
				EpsNKAState _s = new EpsNKAState();
				_s.copyState(s);
				this.contents.add(_s);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((contents == null) ? 0 : contents.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Cluster other = (Cluster) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (contents == null) {
				if (other.contents != null)
					return false;
			} else if (!contents.equals(other.contents))
				return false;
			return true;
		}

		private DKA getOuterType() {
			return DKA.this;
		}
		
		@Override
		public String toString() {
			String ret = "";
			for (EpsNKAState s : this.contents) ret += "bla" + s.toString() + "\n"; 
			return ret;
		}
		
	}
	
	public int clusters;

	private EpsilonNKA eNKA;

	private HashMap<Integer, HashSet<EpsNKAState>> cluster = new HashMap<Integer, HashSet<EpsNKAState>>();
	private HashSet<DKATransition> transitions = new HashSet<DKA.DKATransition>();

	private HashMap<EpsNKAState, ArrayList<Integer>> clusterID = new HashMap<EpsNKAState, ArrayList<Integer>>();

	private HashSet<Cluster> clusterSet = new HashSet<DKA.Cluster>();
	
	private Queue<Cluster> Q = new LinkedList<DKA.Cluster>();
	
	public DKA (EpsilonNKA eNKA) {
		
		this.eNKA = eNKA;
		clusters = 0;
		
		HashSet<EpsNKAState> S42 = new HashSet<EpsNKAState>();
		S42.add(eNKA.getStates().get(0));
		
		Q.add(new Cluster(eNKA.getEpsDistance(S42)));
		
		constructDKA();
	
	}
	
	private void makeClusters() {
<<<<<<< HEAD
		
		int br = 0;
		
		while (!Q.isEmpty() && br < 10) { 
			
			++br;
			
		//	System.out.println(clusterSet.size());
			
			Cluster currentCluster = Q.peek();
			//System.out.println("Govno s kjua: " + Q.size());
			//System.out.println(currentCluster);
			
			Q.remove();
			
			System.out.println("KURCINA ------------------------------------");
			for (Cluster c : clusterSet) System.out.println(c);
			
			
			currentCluster.mergeContents(eNKA.getEpsDistance(currentCluster.contents));
			Cluster copyCluster = new Cluster();
			copyCluster.fromCluster(currentCluster);
			
			clusterSet.add(copyCluster);
			
			for (String s : eNKA.getTerminals()) {
				Cluster nextState = new Cluster(eNKA.makeTransition(currentCluster.contents, s));
				if (nextState.contents.isEmpty()) continue;
				if (!Q.contains(nextState) && !clusterSet.contains(nextState)){
					Q.add(nextState);
					clusterSet.add(nextState);
=======

		boolean change = false;

		do {

			change = false;

			for (EpsilonNKA.Transition t : eNKA.getTransitions()) {

				if (!t.edge.equals("$"))
					continue;

				ArrayList<Integer> fromClustId = clusterID.get(t.from);
				ArrayList<Integer> toClustId = clusterID.get(t.to);

				if (fromClustId == null) {
					fromClustId = new ArrayList<Integer>();
					fromClustId.add(clusters++);
					change = true;
				}

				if (toClustId == null)
					toClustId = new ArrayList<Integer>();

				// for (Integer i : fromClustId) {
				// if (!toClustId.contains(i))
				// change = true;
				// toClustId.add(new Integer(i));
				// }

				for (int i = 0; i < fromClustId.size(); i++) {
					int tmp = fromClustId.get(i);
					if (!toClustId.contains(tmp)) {
						change = true;
						toClustId.add(new Integer(tmp));
					}
>>>>>>> 50d91076f7ae86f6ebf443e9946cf3e415112d5c
				}
			}
			
			for (String s : eNKA.getNonterminals()) {
				Cluster nextState = new Cluster(eNKA.makeTransition(currentCluster.contents, s));
				if (nextState.contents.isEmpty()) continue;
				if (!Q.contains(nextState) && !clusterSet.contains(nextState)){
					Q.add(nextState);
					clusterSet.add(nextState);
					
				}
			}
			
		}
		
		for (Cluster c : clusterSet) {
			cluster.put(clusters++, c.contents);
		}
		
	}

	private void makeTransitions() {
		for (int i = 0; i <= clusters; ++i) {
			HashSet<EpsNKAState> c1 = cluster.get(i);
			for (int j = 0; j <= clusters; ++j) {
				HashSet<EpsNKAState> c2 = cluster.get(j);
				for (EpsilonNKA.Transition t : eNKA.getTransitions()) {
					if (c1.contains(t.from) && c2.contains(t.to)) {
						DKATransition trans = new DKATransition(i, j, t.edge);
						transitions.add(trans);
					}
				}
			}
		}
	}

	private void constructDKA() {
		makeClusters();
		makeTransitions();
	}

	public void outputClusters() {
		for (int i = 0; i < clusters; ++i) {
			HashSet<EpsNKAState> set = cluster.get(i);
			for (EpsNKAState s : set)
				System.err.println(s);
			System.err.println("---------------------------------------");
		}
	}

	public void outputTransitions() {
		for (DKATransition t : transitions) {
			System.err.println(t.from + "->" + t.to + " preko " + t.edge);
		}
		for (DKATransition t1 : transitions) {
			for (DKATransition t2 : transitions) {
				if (t1.equals(t2))
					continue;
				if (t1.from.equals(t2.from) && !t1.to.equals(t2.to) && t1.edge.equals(t2.edge)) {
					System.out.println("GOVNO");
				}
			}
		}
	}

	public HashMap<Integer, HashSet<EpsNKAState>> getCluster() {
		return cluster;
	}

	public HashSet<DKATransition> getTransitions() {
		return transitions;
	}

}

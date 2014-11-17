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

	public class ClusterTransition {

		public HashSet<EpsNKAState> from;
		public HashSet<EpsNKAState> to;

		public String edge;

		public ClusterTransition(HashSet<EpsNKAState> from,
				HashSet<EpsNKAState> to, String edge) {
			super();
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
			ClusterTransition other = (ClusterTransition) obj;
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

		public Cluster() {
		}

		public Cluster(HashSet<EpsNKAState> contents) {
			for (EpsNKAState s : contents) {
				EpsNKAState newState = new EpsNKAState();
				newState.copyState(s);
				this.contents.add(s);
			}
		}

		public Cluster(EpsNKAState state) {
			EpsNKAState novoS = new EpsNKAState();
			novoS.fromState(state);
			this.contents.add(novoS);
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
				if (!this.contents.contains(_s))
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
			for (EpsNKAState s : this.contents)
				ret += "bla" + s.toString() + "\n";
			return ret;
		}

	}

	public int clusters;

	private EpsilonNKA eNKA;

	private HashMap<Integer, HashSet<EpsNKAState>> cluster = new HashMap<Integer, HashSet<EpsNKAState>>();
	private HashMap<HashSet<EpsNKAState>, Integer> clusterID = new HashMap<HashSet<EpsNKAState>, Integer>();

	private HashSet<DKATransition> transitions = new HashSet<DKA.DKATransition>();
	private HashSet<ClusterTransition> clusterTransitions = new HashSet<DKA.ClusterTransition>();

	private HashSet<Cluster> clusterSet = new HashSet<DKA.Cluster>();

	private Queue<Cluster> Q = new LinkedList<DKA.Cluster>();

	public DKA(EpsilonNKA eNKA) {

		this.eNKA = eNKA;
		clusters = 0;

		HashSet<EpsNKAState> S42 = new HashSet<EpsNKAState>();
		S42.add(eNKA.getStates().get(0));

		cluster.put(0, eNKA.getEpsDistance(S42));
		clusterID.put(eNKA.getEpsDistance(S42), 0);
		Q.add(new Cluster(eNKA.getEpsDistance(S42)));

		++clusters;

		constructDKA();

	}

	private void makeClusters() {

		while (!Q.isEmpty()) {

			System.err.println(clusterSet.size());

			Cluster currentCluster = Q.peek();
			Q.remove();

			for (String s : eNKA.getTerminals()) {
				Cluster nextState = new Cluster(eNKA.makeTransition(
						currentCluster.contents, s));
				nextState
						.mergeContents(eNKA.getEpsDistance(nextState.contents));
				if (nextState.contents.isEmpty())
					continue;
				clusterTransitions.add(new ClusterTransition(
						currentCluster.contents, nextState.contents, s));
				if (!Q.contains(nextState) && !clusterSet.contains(nextState)) {
					Q.add(nextState);
					clusterSet.add(nextState);
				}
			}

			for (String s : eNKA.getNonterminals()) {
				Cluster nextState = new Cluster(eNKA.makeTransition(
						currentCluster.contents, s));
				nextState
						.mergeContents(eNKA.getEpsDistance(nextState.contents));
				if (nextState.contents.isEmpty())
					continue;
				clusterTransitions.add(new ClusterTransition(
						currentCluster.contents, nextState.contents, s));
				if (!Q.contains(nextState) && !clusterSet.contains(nextState)) {
					Q.add(nextState);
					clusterSet.add(nextState);
				}
			}
		}

		for (Cluster c : clusterSet) {
			clusterID.put(c.contents, clusters);
			cluster.put(clusters++, c.contents);
		}

	}

	private void makeTransitions() {

		for (ClusterTransition t : clusterTransitions) {
			Integer fromId = clusterID.get(t.from);
			Integer toId = clusterID.get(t.to);
			transitions.add(new DKATransition(fromId, toId, t.edge));
		}
		System.out.println("Number of transitions: " + transitions.size());

		for (DKATransition t : transitions)
			System.out.println(t.from + " " + t.to + " " + t.edge);

//		transitions = new HashSet<DKA.DKATransition>();

//		for (int i = 0; i < clusters; ++i) {
//			HashSet<EpsNKAState> c1 = cluster.get(i);
//			for (int j = 0; j < clusters; ++j) {
//				HashSet<EpsNKAState> c2 = cluster.get(j);
//				for (EpsilonNKA.Transition t : eNKA.getTransitions()) {
//					if (c1.contains(t.from) && c2.contains(t.to)
//							&& !t.edge.equals("$")) {
//						DKATransition trans = new DKATransition(i, j, t.edge);
//						transitions.add(trans);
//					}
//				}
//			}
//		}
//
//		System.out.println("Number of transitions: " + transitions.size());
//
//		for (DKATransition t : transitions)
//			System.out.println(t.from + " " + t.to + " " + t.edge);

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
				if (t1.from.equals(t2.from) && !t1.to.equals(t2.to)
						&& t1.edge.equals(t2.edge)) {
					System.out.println("bla");
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

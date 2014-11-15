import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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

	public int clusters;

	private EpsilonNKA eNKA;

	private HashMap<Integer, HashSet<EpsilonNKA.State>> cluster;
	private HashSet<DKATransition> transitions;

	private HashMap<EpsilonNKA.State, ArrayList<Integer>> clusterID;

	public DKA(EpsilonNKA eNKA) {
		this.eNKA = eNKA;
		clusters = 0;
		cluster = new HashMap<Integer, HashSet<EpsilonNKA.State>>();
		transitions = new HashSet<DKA.DKATransition>();
		clusterID = new HashMap<EpsilonNKA.State, ArrayList<Integer>>();
		constructDKA();
	}

	private void makeClusters() {

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

				for (Integer i : fromClustId) {
					if (!toClustId.contains(i))
						change = true;
					toClustId.add(new Integer(i));
				}

				ArrayList<Integer> _fromClustId = new ArrayList<Integer>();
				ArrayList<Integer> _toClustId = new ArrayList<Integer>();

				for (Integer i : fromClustId)
					_fromClustId.add(i);
				for (Integer i : toClustId)
					_toClustId.add(i);

				clusterID.put(t.from, _fromClustId);
				clusterID.put(t.to, _toClustId);

			}

			for (EpsilonNKA.State s : eNKA.getStates()) {

				ArrayList<Integer> C = clusterID.get(s);
				if (C == null) {
					change = true;
					C = new ArrayList<Integer>();
					C.add(clusters++);
					clusterID.put(s, C);
				}

				for (Integer i : C) {
					HashSet<EpsilonNKA.State> hs = cluster.get(i);
					if (hs == null)
						hs = new HashSet<EpsilonNKA.State>();
					hs.add(s);
					cluster.put(i, hs);
				}
			}
		} while (change);

	}

	private void makeTransitions() {
		for (EpsilonNKA.Transition t : eNKA.getTransitions()) {

			ArrayList<Integer> ClustID1 = clusterID.get(t.from);
			ArrayList<Integer> ClustID2 = clusterID.get(t.to);

			if (t.edge.equals("$"))
				continue;

			for (Integer i : ClustID1) {
				for (Integer j : ClustID2) {
					// if (i.equals(j)) continue;
					DKATransition trans = new DKATransition(i, j, t.edge);
					transitions.add(trans);
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
			HashSet<EpsilonNKA.State> set = cluster.get(i);
			for (EpsilonNKA.State s : set)
				System.err.println(s);
			System.err.println("---------------------------------------");
		}
	}

	public void outputTransitions() {
		for (DKATransition t : transitions) {
			System.err.println(t.from + "->" + t.to + " preko " + t.edge);
		}
	}

	public HashMap<Integer, HashSet<EpsilonNKA.State>> getCluster() {
		return cluster;
	}

	public HashSet<DKATransition> getTransitions() {
		return transitions;
	}

}

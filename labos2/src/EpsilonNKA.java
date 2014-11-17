import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class which models e-NKA.
 * 
 * @author Ivan Paljak
 */
public class EpsilonNKA {

	public EpsilonNKA() {
	}

	public class Transition {

		public EpsNKAState from, to;
		public String edge;

		public Transition(EpsNKAState from, EpsNKAState to, String edge) {
			this.from = from;
			this.to = to;
			this.edge = edge;
		}

		@Override
		public String toString() {
			return from.toString() + " --> ( " + edge + " ) " + to.toString();
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
			Transition other = (Transition) obj;
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

		private EpsilonNKA getOuterType() {
			return EpsilonNKA.this;
		}

	}

	private String startingState;

	private HashMap<String, ArrayList<Production>> grammar = new HashMap<>();
	private BeginsWithTable beginsWith;

	private ArrayList<String> nonterminal;
	private ArrayList<String> terminal;

	private ArrayList<EpsNKAState> states;
	private ArrayList<Transition> transitions;

	public EpsilonNKA(String startingState, HashMap<String, ArrayList<Production>> grammar, BeginsWithTable beginsWith,
			ArrayList<String> nonterminal, ArrayList<String> terminal) {

		this.startingState = startingState;
		this.grammar = grammar;
		this.beginsWith = beginsWith;
		this.nonterminal = nonterminal;
		this.terminal = terminal;

		states = new ArrayList<EpsNKAState>();
		transitions = new ArrayList<EpsilonNKA.Transition>();

		for (Production p : grammar.get(this.startingState)) {
			EpsNKAState s = new EpsNKAState(p, 0);

			HashSet<String> starts = new HashSet<String>();
			starts.add("$");
			s.starts = starts;

			states.add(s);
		}

	}

	/** Getters for states and transitions **/
	public ArrayList<EpsNKAState> getStates() {
		return states;
	}

	public ArrayList<Transition> getTransitions() {
		return transitions;
	}

	public ArrayList<String> getTerminals() {
		return terminal;
	}

	public ArrayList<String> getNonterminals() {
		return nonterminal;
	}

	public HashSet<EpsNKAState> makeTransition(HashSet<EpsNKAState> from, String e) {
		HashSet<EpsNKAState> ret = new HashSet<EpsNKAState>();
		for (Transition t : transitions) {
			if (from.contains(t.from) && t.edge.equals(e))
				ret.add(t.to);
		}
		return ret;
	}

	public HashSet<EpsNKAState> getEpsDistance(HashSet<EpsNKAState> start) {

		HashSet<EpsNKAState> ret = new HashSet<EpsNKAState>();
		for (EpsNKAState s : start) {
			EpsNKAState _s = new EpsNKAState();
			_s.copyState(s);
			ret.add(_s);
		}

		boolean change = false;

		do {

			change = false;

			for (Transition t : transitions) {
				if (!t.edge.equals("$"))
					continue;
				if (ret.contains(t.from) && !ret.contains(t.to)) {
					change = true;
					EpsNKAState _s = new EpsNKAState();
					_s.copyState(t.to);
					ret.add(_s);
				}
			}

		} while (change);

		return ret;

	}

	public void generateEpsilonNKA() {

		int stateIndex = -1;

		while (stateIndex < states.size()) {
			++stateIndex;

			if (stateIndex == states.size())
				break;

			EpsNKAState currentState = states.get(stateIndex);
			if (!currentState.isAlive()) {
				continue;
			}

			// b) prijelaz
			EpsNKAState nextState = new EpsNKAState();

			nextState.fromState(currentState);

			if (!states.contains(nextState)) {
				states.add(nextState);
			}

			transitions.add(new Transition(currentState, nextState, currentState.afterDot()));

			// c) prijelaz
			if (terminal.contains(currentState.afterDot())) {
				continue;
			}

			HashSet<String> starting = new HashSet<String>();

			boolean propagate = false;
			if (currentState.dotIndex + 1 >= currentState.p.right.size())
				propagate = true;

			for (int i = currentState.dotIndex + 1; i < currentState.p.right.size(); ++i) {
				String currString = currentState.p.right.get(i);
				for (String str : beginsWith.getBeginsWith(currString)) {
					if (nonterminal.contains(str))
						continue;
					starting.add(new String(str));
				}
				if (!beginsWith.generatesEpsilon(currString))
					break;
			}

			propagate = true;
			for (int i = currentState.dotIndex + 1; i < currentState.p.right.size(); ++i) {
				String currString = currentState.p.right.get(i);
				if (beginsWith.generatesEpsilon(currString)) {
					propagate = false;
					break;
				}
			}
			if (propagate) {
				for (String str : currentState.starts)
					starting.add(new String(str));
			}

			for (Production p : grammar.get(currentState.afterDot())) {

				nextState = new EpsNKAState(p, 0);
				nextState.starts = starting;

				transitions.add(new Transition(currentState, nextState, "$"));

				if (!states.contains(nextState)) {
					states.add(nextState);
				}
			}

		}

	}

	public void outputEpsTransitions() {
		for (EpsNKAState s : states) {
			System.out.println("Stanje: " + s.toString());
			HashSet<EpsNKAState> _S = new HashSet<EpsNKAState>();
			_S.add(s);
			HashSet<EpsNKAState> S = this.getEpsDistance(_S);
			for (EpsNKAState _s : S)
				System.out.println(_s);
		}
	}

	public void outputStates() {
		for (EpsNKAState s : states)
			System.out.println(s);
	}

	public void outputTransitions() {
		for (Transition t : transitions)
			System.out.println(t);
	}

}
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class GenerateLRParserTable implements Serializable {

	private static final long serialVersionUID = 9012062540849752089L;
	private DKA dka;
	private ArrayList<String> terminals = new ArrayList<>();
	private ArrayList<String> nonterminals = new ArrayList<>();
	public HashMap<Integer, HashMap<String, Action>> actions = new HashMap<>();
	public HashMap<Integer, HashMap<String, Integer>> newState = new HashMap<>();

	public GenerateLRParserTable(EpsilonNKA eNKA, ArrayList<String> terminals, ArrayList<String> nonterminals) {
		this.dka = new DKA(eNKA);
		this.terminals = terminals;
		this.nonterminals = nonterminals;
		fillActions();
		fillNewState();
	}

	private void fillActions() {
		// initialize table for all DKA states and all terminal signs
		for (int i = 0; i < dka.clusters; i++) {
			actions.put(i, new HashMap<String, Action>());

			for (String terminal : terminals) {
				actions.get(i).put(terminal, new Action());
			}

			// initialize end of array character also
			actions.get(i).put("$", new Action());
		}

		// generate shift actions (they are superior to reduce)
		// transition -> Integer1, char, Integer2
		for (DKA.DKATransition transition : dka.getTransitions()) {
			int currentState = transition.from;
			String transChar = transition.edge;
			int newState = transition.to;

			// Nonterminal characters aren't part of this table
			if (!terminals.contains(transChar))
				continue;

			for (EpsilonNKA.State LRState : dka.getCluster().get(currentState)) {
				// if the dot is not located at the end
				if (LRState.dotIndex >= LRState.p.right.size())
					continue;

				if (LRState.p.right.get(LRState.dotIndex).equals(transChar)) {
					// generate Shift action
					actions.get(currentState).get(transChar).shift = newState;
				}
			}
		}

		// generate Reduce and Accept actions
		// for each DKA state, check all LR States
		for (Integer DKAState : dka.getCluster().keySet()) {
			for (EpsilonNKA.State LRState : dka.getCluster().get(DKAState)) {
				// Reduce works only if the dot is the final element
				if (LRState.dotIndex != LRState.p.right.size())
					continue;

				// check if it's the initial state, then generate Accept
				if (LRState.p.left.equals(nonterminals.get(0))) {
					actions.get(DKAState).get("$").accept = true;
					continue;
				}

				// Reduce actions follow
				for (String transChar : LRState.starts) {
					Action action = actions.get(DKAState).get(transChar);

					if (action.shift != -1) {
						// we have a Shift/Reduce collision, use Shift
						System.err.printf("Collision between Shift(%d) and Reduce(%s) for [%d,%s], using Shift.",
								action.shift, LRState.p, DKAState, transChar);
						continue;
					}

					if (action.reduce != null) {
						// we have a Reduce/Reduce collision, using the one
						// stated before in the input file
						if (action.reduce.productionNumber > LRState.p.productionNumber) {
							action.reduce = LRState.p;
							System.err.printf(
									"Collision between Reduce(%s) and Reduce(%s) for [%d,%s], using Reduce(%s).",
									action.reduce, LRState.p, DKAState, transChar, LRState.p);
						} else { // do nothing
							// keep the reduction already stated
							System.err.printf(
									"Collision between Reduce(%s) and Reduce(%s) for [%d,%s], using Reduce(%s).",
									action.reduce, LRState.p, DKAState, transChar, action.reduce);
						}
						continue;
					}

					// else just generate new Reduce
					action.reduce = LRState.p;
				}
			}
		}
	}

	private void fillNewState() {
		// initialize table, -1 is default value
		for (int i = 0; i < dka.clusters; i++) {
			newState.put(i, new HashMap<String, Integer>());
			for (String nonterminal : nonterminals) {
				// ignore artificial starting nonterminal
				if (nonterminal.equals(nonterminals.get(0)))
					continue;
				newState.get(i).put(nonterminal, -1);
			}
		}

		for (DKA.DKATransition transition : dka.getTransitions()) {
			int currState = transition.from;
			int toState = transition.to;
			String transChar = transition.edge;

			// check whether this is a nonterminal character
			if (!nonterminals.contains(transChar))
				continue;

			// ignore artificial starting nonterminal
			if (transChar.equals(nonterminals.get(0)))
				continue;

			newState.get(currState).put(transChar, toState);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < dka.clusters; i++) {
			HashMap<String, Action> tmp = actions.get(i);
			for (String terminal : terminals) {
				sb.append("State " + i + ", " + terminal + " -> " + tmp.get(terminal).toString());
				sb.append("\n");
			}
			sb.append("State " + i + ", " + "$" + " -> " + tmp.get("$").toString());
			sb.append("\n\n");
		}

		sb.append("---------\n");

		for (int i = 0; i < dka.clusters; i++) {
			HashMap<String, Integer> tmp = newState.get(i);
			for (String nonterminal : nonterminals) {
				// ignore artificial starting nonterminal
				if (nonterminal.equals(nonterminals.get(0)))
					continue;
				sb.append("State " + i + ", " + nonterminal + " -> " + tmp.get(nonterminal).toString());
				sb.append("\n");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public HashMap<Integer, HashMap<String, Action>> getActions() {
		return actions;
	}

	public HashMap<Integer, HashMap<String, Integer>> getNewState() {
		return newState;
	}

}

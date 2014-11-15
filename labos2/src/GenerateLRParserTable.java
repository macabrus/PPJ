import java.util.ArrayList;
import java.util.HashMap;

public class GenerateLRParserTable {

	// private DKA dka = new DKA();
	private ArrayList<String> terminals = new ArrayList<>();
	private ArrayList<String> nonterminals = new ArrayList<>();
	public HashMap<Integer, HashMap<String, Action>> actions = new HashMap<>();

//	public void fillActions() {
//		// initialize table for all DKA states and all terminal signs
//		for (int i = 0; i < dka.size(); i++) {
//			actions.put(i, new HashMap<String, Action>());
//
//			for (String terminal : terminals) {
//				actions.get(i).put(terminal, new Action());
//			}
//
//			// initialize end of array character also
//			actions.get(i).put("$", new Action());
//		}
//
//		// generate shift actions (they are superior to reduce)
//		// transition -> Integer1, char, Integer2
//		for (DKATransition transition : dka.getTransitions()) {
//			int currentState = transition.first;
//			String transChar = transition.second;
//			int newState = transition.third;
//
//			// Nonterminal characters aren't part of this table
//			if (!terminals.contains(transChar))
//				continue;
//
//			for (State LRState : dka.getStates().get(currentState)) {
//				// if the dot is not located at the end
//				if (LRState.dotPosition >= LRState.production.right.size())
//					continue;
//
//				if (LRState.production.right.get(LRState.dotPosition).equals(transChar)) {
//					// generate Shift action
//					actions.get(currentState).get(transChar).shift = newState;
//				}
//			}
//		}
//
//		// generate Reduce and Accept actions
//		// for each DKA state, check all LR States
//		for (DKAState DKAState : dka.getStates()) {
//			for (State LRState : DKAState.getLRStates()) {
//				// Reduce works only if the dot is the final element
//				if (LRState.dotPosition != LRState.production.right.size())
//					continue;
//
//				// check if it's the initial state, then generate Accept
//				if (LRState.production.left.equals(nonterminals.get(0))) {
//					actions.get(DKAState.id).get("$").accept = true;
//					continue;
//				}
//
//				// Reduce actions follow
//				for (String transChar : LRState.starts) {
//					Action action = actions.get(DKAState.id).get(transChar);
//
//					if (action.shift != -1) {
//						// we have a Shift/Reduce collision, use Shift
//						System.err.println("Collision between shift and reduce, using shift");
//						continue;
//					}
//
//					if (action.reduce != null) {
//						// we have a Reduce/Reduce collision, using the one
//						// stated before
//						if(action.reduce.productionNumber > LRState.production.productioNumber) {
//							// 
//						}
//					}
//				}
//			}
//		}
//
//	}
}

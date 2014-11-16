import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

/**
 * Class which models an LR parser and builds a generative tree.
 * @author Ivan Paljak
 */
public class LRParser {

	private int pos;
	private ArrayList<LexUnit> input;

	private HashMap<Integer, HashMap<String, Action>> actions;
	private HashMap<Integer, HashMap<String, Integer>> newState;

	private HashSet<String> syncro;

	private TreeNode root;

	private Stack<TreeNode> characters;
	private Stack<Integer> states;

	public LRParser(ArrayList<LexUnit> input, HashMap<Integer, HashMap<String, Action>> actions,
			HashMap<Integer, HashMap<String, Integer>> newState, HashSet<String> syncro) {
		super();

		this.input = input;
		this.actions = actions;
		this.newState = newState;
		this.syncro = syncro;

		root = null;
		pos = 0;

		characters = new Stack<TreeNode>();
		states = new Stack<Integer>();

		parse();
	}

	public TreeNode getRoot() {
		return root;
	}

	private void parse() {

		states.push(0);
		while (true) {

			LexUnit lu;
			Integer state = states.peek();
			Action action;

			if (pos < input.size()) {
				lu = input.get(pos);
			} else {
				lu = new LexUnit("$ $ $");
			}

			action = actions.get(state).get(lu.uniform);

			if (action.isAccept())
				break;

			if (action.isNothing()) {
				System.err.println("Stanje: " + state + " char " + lu.uniform);
				System.err.printf("Error recovery in row %s\n", lu.row);

				// characters that wouldn't cause an error
				System.err.printf("Expected characters that don't cause an error: ");

				for (String s : actions.get(state).keySet()) {
					// get characters that have defined valid actions
					if (!actions.get(state).get(s).isNothing()) {
						System.err.printf(s + " ");
					}
				}
				System.err.println();
				System.err.printf("At character %s\n", lu.uniform);
				boolean found = false;

				for (int i = pos; i < input.size(); i++) {
					// stop at a syncro character
					if (syncro.contains(input.get(i).uniform)) {
						found = true;
					}
					++pos;
				}

				if (found == false) {
					System.err.println("Unrecoverable error!");
					break;
				}

				lu = input.get(pos);
				while (states.size() > 0) {
					// pop states until an action for syncro character is
					// defined
					Action tmp = actions.get(state).get(lu.uniform);
					if (!tmp.isNothing())
						break;

					states.pop();
					characters.pop();
				}

				if (states.size() == 0) {
					System.err.println("Unrecoverable error!");
					break;
				}
			}

			if (action.isShift()) {
				int shift = action.shift;
				states.push(new Integer(shift));
				characters.push(new TreeNode(lu.toString()));
				++pos;
				continue;
			}

			if (action.isReduce()) {
				Production prod = action.reduce;
				TreeNode newNode = new TreeNode(prod.left);

				if (prod.right.size() == 0) {
					newNode.addChild(new TreeNode("$"));
				} else {
					for (int i = 0; i < prod.right.size(); ++i) {
						states.pop();
						newNode.addChild(characters.peek());
						characters.pop();
					}
				}

				characters.add(newNode);

				Integer state1 = states.peek();
				Integer nextState = newState.get(state1).get(prod.left);

				states.push(nextState);
			}

		}
		root = characters.peek();
	}

}

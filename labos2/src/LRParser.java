import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

/**
 * Class which models a LR parser and builds a generative tree.
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
			System.out.println("state " + state + " char " + lu.toString() + " action " + action.toString());

			if (action.isAccept())
				break;

			if (action.isNothing()) {
				System.err.println("Oporavak od pogreske");
				break;
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

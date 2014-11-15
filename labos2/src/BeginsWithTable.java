import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class BeginsWithTable {

	private HashMap<String, HashMap<String, Integer>> beginsWith = new HashMap<>();

	private ArrayList<String> allChars = new ArrayList<>();
	private ArrayList<String> nonterminals = new ArrayList<>();
	private HashSet<String> emptyNonterminals = new HashSet<>();

	// map of all productions, key is leftSide, value is array of all
	// productions of that value
	private HashMap<String, ArrayList<Production>> grammar = new HashMap<>();

	public BeginsWithTable(ArrayList<String> allChars, ArrayList<String> nonterminals,
			HashMap<String, ArrayList<Production>> grammar) {
		super();
		this.allChars = allChars;
		this.nonterminals = nonterminals;
		this.grammar = grammar;
		getEmptyNonterminals();
		createTable();
	}

	/**
	 * Method that finds all nonterminals that have an epsilon production, i.e.
	 * <A> -> $
	 */
	public void getEmptyNonterminals() {
		// add left sides of all epsilon productions
		for (String nonterm : nonterminals) {
			for (Production production : grammar.get(nonterm)) {

				if (production.right.size() == 0) {
					emptyNonterminals.add(production.left);
				}
			}
		}

		// if all characters from right side of production can result in
		// epsilon, add the left side
		boolean changed = true;

		while (changed) {
			changed = false;

			for (String nonterminal : nonterminals) {
				for (Production production : grammar.get(nonterminal)) {
					boolean empty = true;

					// check whether all right side characters are empty
					for (String rightChar : production.right) {
						if (!emptyNonterminals.contains(rightChar)) {
							empty = false;
							break;
						}
					}

					if (!emptyNonterminals.contains(production.left) && empty) {
						changed = true;
						emptyNonterminals.add(production.left);
					}
				}
			}
		}
	}

	private void createTable() {
		for (String c : allChars) {
			beginsWith.put(c, new HashMap<String, Integer>());

			for (String otherC : allChars) {
				// set all to zero (false)
				beginsWith.get(c).put(otherC, 0);
			}
		}

		// 1st pass (beginsDirectlyWith)
		for (String nonterminal : nonterminals) {
			for (Production production : grammar.get(nonterminal)) {
				// iterate over right side of production, add the first right
				// character, but the following only if the one before can end
				// in epsilon
				for (int i = 0; i < production.right.size(); i++) {
					beginsWith.get(production.left).put(production.right.get(i), 1);

					// if the current character doesn't end in epsilon, then
					// it's done for this production. if it can, continue
					if (!emptyNonterminals.contains(production.right.get(i)))
						break;
				}
			}
		}

		// 2nd pass (beginsWith) - BFS
		// beginsWith is a transitive environment of beginsDirectlyWith

		for (String c : beginsWith.keySet()) {
			// mark itself, reflexive operation
			beginsWith.get(c).put(c, 1);

			// create queue and visited
			Queue<String> queue = new LinkedList<String>();
			HashSet<String> visited = new HashSet<>();

			// init - add all marked values from the table into queue
			for (String otherC : beginsWith.get(c).keySet()) {
				if (beginsWith.get(c).get(otherC) == 1) {
					queue.add(otherC);
				}
			}

			// BFS over table
			while (queue.size() > 0) {
				String otherC = queue.peek();
				queue.remove();

				if (visited.contains(otherC))
					continue;

				// visit and mark in table
				visited.add(otherC);
				beginsWith.get(c).put(otherC, 1);

				// add to queue all reachable
				for (String nextC : beginsWith.get(otherC).keySet()) {
					if (beginsWith.get(otherC).get(nextC) == 1)
						queue.add(nextC);
				}
			}

		}
	}

	public HashSet<String> getBeginsWith(String nonterminal) {
		HashSet<String> beginners = new HashSet<>();
		for (String c : beginsWith.get(nonterminal).keySet()) {
			if (beginsWith.get(nonterminal).get(c) == 1) {
				beginners.add(c);
			}
		}

		return beginners;
	}

	public boolean generatesEpsilon(String nonterminal) {
		return emptyNonterminals.contains(nonterminal);
	}

}

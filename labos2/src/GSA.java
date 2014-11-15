import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class GSA {

	// list of all chars, terminals and nonterminals
	private static ArrayList<String> allChars = new ArrayList<>();
	private static ArrayList<String> nonterminals = new ArrayList<>();
	private static ArrayList<String> terminals = new ArrayList<>();
	private static ArrayList<String> synchro = new ArrayList<>();

	private static HashSet<String> emptyNonterminals = new HashSet<>();

	private static HashMap<String, HashMap<String, Integer>> beginsWith = new HashMap<>();

	// map of all productions, key is leftSide, value is array of all
	// productions of that value
	private static HashMap<String, ArrayList<Production>> grammar = new HashMap<>();

	private static void parseInput() throws IOException {
		// BufferedReader br = new BufferedReader(new
		// InputStreamReader(System.in));
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("test.san")));
		String currLine;

		// input nonterminals
		currLine = br.readLine();
		String[] dubrovnik = currLine.split(" ");
		for (int i = 1; i < dubrovnik.length; i++) {
			nonterminals.add(dubrovnik[i]);
			allChars.add(dubrovnik[i]);
			grammar.put(dubrovnik[i], new ArrayList<Production>());
		}

		// input terminals
		currLine = br.readLine();
		String[] dubrovnik2 = currLine.split(" ");
		for (int i = 1; i < dubrovnik2.length; i++) {
			terminals.add(dubrovnik2[i]);
			allChars.add(dubrovnik2[i]);
		}

		// input synchro
		currLine = br.readLine();
		String[] dubrovnik3 = currLine.split(" ");
		for (int i = 1; i < dubrovnik3.length; i++) {
			synchro.add(dubrovnik3[i]);
		}

		String leftSide = "";
		int numberOfProduction = 1;
		// input productions
		while ((currLine = br.readLine()) != null) {
			if (currLine.equals("")) {
				continue;
			}
			if (currLine.startsWith("<")) {
				// left side of production
				leftSide = currLine;
			} else {
				// right side of production, add to grammar
				Production production = new Production(leftSide, currLine.substring(1), numberOfProduction++);
				grammar.get(leftSide).add(production);
			}
		}

		br.close();
	}

	/**
	 * Method that adds a new initial state and a production from that state to
	 * the first state of the input.
	 */
	private static void addNewInit() {
		// get new initial state
		String init = nonterminals.get(0);
		String newInit = init.concat("'");
		// make it the head of nonterminal list
		ArrayList<String> temp = new ArrayList<>();
		temp.add(newInit);
		temp.addAll(nonterminals);
		nonterminals = new ArrayList<>();
		nonterminals.addAll(temp);

		// add its production to grammar
		grammar.put(newInit, new ArrayList<Production>());
		Production production = new Production(newInit, init, 0);
		grammar.get(newInit).add(production);

		// add the char to allChars
		temp = new ArrayList<>();
		temp.add(newInit);
		temp.addAll(allChars);
		allChars = new ArrayList<>();
		allChars.addAll(temp);
	}

	/**
	 * Method that finds all nonterminals that have an epsilon production, i.e.
	 * <A> -> $
	 */
	private static void getEmptyNonterminals() {
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

	private static void getBeginsWith() {
		// create table
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

	public static void main(String[] args) throws IOException {
		parseInput();
		addNewInit();
		getEmptyNonterminals();
		getBeginsWith();

		for (String c : beginsWith.keySet()) {
			System.out.println("char " + c + " begins with:");
			for (String cc : beginsWith.get(c).keySet()) {
				if (beginsWith.get(c).get(cc) == 1)
					System.out.println("\t" + cc);
			}
		}
	}

}
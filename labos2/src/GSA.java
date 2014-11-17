import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class modelling a syntax generator.
 * 
 * @author Paula Gombar, 0036474619
 * 
 */
public class GSA {

	// list of all chars, terminals and nonterminals
	private static ArrayList<String> allChars = new ArrayList<>();
	private static ArrayList<String> nonterminals = new ArrayList<>();
	private static ArrayList<String> terminals = new ArrayList<>();
	private static HashSet<String> synchro = new HashSet<>();
	private static GenerateLRParserTable LRParserTable;

	// map of all productions, key is leftSide, value is array of all
	// productions of that value
	private static HashMap<String, ArrayList<Production>> grammar = new HashMap<>();

	private static void parseInput(String input) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		// BufferedReader br = new BufferedReader(new InputStreamReader(new
		// FileInputStream(input)));
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

	private static void outputCollections() throws IOException {

		FileOutputStream fout = new FileOutputStream("analizator/synchro.ser");
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(synchro);

		fout.close();
		fout = new FileOutputStream("analizator/actions.ser");
		oos = new ObjectOutputStream(fout);
		oos.writeObject(LRParserTable.getActions());

		fout.close();
		fout = new FileOutputStream("analizator/newState.ser");
		oos = new ObjectOutputStream(fout);
		oos.writeObject(LRParserTable.getNewState());

		fout.close();
		oos.close();

	}

	public static void main(String[] args) throws IOException {
		String input = "00aab_2.san";
		parseInput(input);

		addNewInit();
		BeginsWithTable beginsWith = new BeginsWithTable(allChars, nonterminals, grammar);
		System.err.println("Done with beginsWith table.");
		EpsilonNKA eNKA = new EpsilonNKA(nonterminals.get(0), grammar, beginsWith, nonterminals, terminals);
		eNKA.generateEpsilonNKA();
		System.err.println("Done with eNKA.");
		System.out.println("eNKA states: " + eNKA.getStates().size() + " trans: " + eNKA.getTransitions().size());

		// eNKA.outputStates();
		// eNKA.outputTransitions();
		// eNKA.outputEpsTransitions();

		DKA dka = new DKA(eNKA);
		System.err.println("Done with DKA.");

		LRParserTable = new GenerateLRParserTable(dka, terminals, nonterminals);
		System.err.println("Done with parser table.");
		// System.out.println(LRParserTable.toString());

		outputCollections();
	}

}
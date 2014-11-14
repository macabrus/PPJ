

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class GSA {

	private static ArrayList<String> nonterminal = new ArrayList<>();
	private static ArrayList<String> terminal = new ArrayList<>();
	private static ArrayList<String> synchro = new ArrayList<>();

	// map of all productions, key is leftSide, value is array of all
	// productions of that value
	private static HashMap<String, ArrayList<String>> grammar = new HashMap<>();

	private static void parseInput() throws IOException {
		// BufferedReader br = new BufferedReader(new
		// InputStreamReader(System.in));
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("test.san")));
		String currLine;

		// input nonterminals
		while ((currLine = br.readLine()) != null) {
			if (!currLine.startsWith("%V"))
				break;
			String[] dubrovnik = currLine.split(" ");
			for (int i = 1; i < dubrovnik.length; i++) {
				nonterminal.add(dubrovnik[i]);
				grammar.put(dubrovnik[i], new ArrayList<String>());
			}
		}

		// input terminals
		while ((currLine = br.readLine()) != null) {
			if (!currLine.startsWith("%T"))
				break;
			String[] dubrovnik = currLine.split(" ");
			for (int i = 1; i < dubrovnik.length; i++) {
				terminal.add(dubrovnik[i]);
			}
		}

		// input synchro
		while ((currLine = br.readLine()) != null) {
			if (!currLine.startsWith("%Syn"))
				break;
			String[] dubrovnik = currLine.split(" ");
			for (int i = 1; i < dubrovnik.length; i++) {
				synchro.add(dubrovnik[i]);
			}
		}

		String leftSide = "";
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
				grammar.get(leftSide).add(currLine.substring(1));
			}
		}

		br.close();
	}

	private static void addNewInit() {
		// get new initial state
		String newInit = nonterminal.get(0).concat("'");
		// make it the head of nonterminal list
		ArrayList<String> temp = new ArrayList<>();
		temp.add(newInit);
		temp.addAll(nonterminal);
		nonterminal = new ArrayList<>();
		nonterminal.addAll(temp);
	}

	public static void main(String[] args) throws IOException {
		parseInput();
		addNewInit();
	}

}
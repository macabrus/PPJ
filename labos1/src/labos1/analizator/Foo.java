package labos1.analizator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import labos1.Automaton;
import labos1.Rule;

public class Foo {

	// temp strukture
	static ArrayList<String> LAstates = new ArrayList<>();
	static ArrayList<String> LAitems = new ArrayList<>();

	// lista Ruleova, jedan Rule je <stanje,automat,listaAkcija>
	static ArrayList<Rule> rules = new ArrayList<>();

	// mapa <Stanje, listaAutomata> tako da znam kojim sve automatima pristupam
	// u trenutnom stanju
	static Map<String, ArrayList<Integer>> availableAut = new TreeMap<>();

	private static ObjectInput input;
	private static InputStream buffer;
	private static InputStream file;

	@SuppressWarnings("unchecked")
	private static void inputStates() throws IOException, ClassNotFoundException {
		file = new FileInputStream("src/labos1/analizator/states.ser");
		buffer = new BufferedInputStream(file);
		input = new ObjectInputStream(buffer);
		LAstates = (ArrayList<String>) input.readObject();
		// for (String string : LAstates) {
		// System.out.println(string);
		// }
	}

	@SuppressWarnings("unchecked")
	private static void inputItems() throws IOException, ClassNotFoundException {
		file = new FileInputStream("src/labos1/analizator/items.ser");
		buffer = new BufferedInputStream(file);
		input = new ObjectInputStream(buffer);

		LAitems = (ArrayList<String>) input.readObject();
	}

	@SuppressWarnings("unchecked")
	private static void inputRules() throws IOException, ClassNotFoundException {
		file = new FileInputStream("src/labos1/analizator/rules.ser");
		buffer = new BufferedInputStream(file);
		input = new ObjectInputStream(buffer);

		rules = (ArrayList<Rule>) input.readObject();
		// for (Rule string : rules) {
		// System.out.println(string);
		// }
	}

	/**
	 * Metoda koja se poziva na pocetku programa. Za svako stanje napravi listu
	 * indekasa (lol) automata dostupnih iz tog stanja. Indeksi su iz liste
	 * rules i preko njih dolazimo do automata.
	 */
	private static void generateAvailableAutomata() {
		// za svako stanje generiraj listu indeksa automata kojima mogu
		// pristupiti iz njega
		for (String state : LAstates) {
			ArrayList<Integer> autList = new ArrayList<>();
			// trci po svim trojkama i nadji <stanje,automat,listaAkcija>
			for (int i = 0; i < rules.size(); ++i) {
				Rule rule = rules.get(i);
				if (state.equals(rule.getState())) {
					autList.add(i);
				}
			}
			availableAut.put(state, autList);
		}
	}

	private static void inputSource() throws IOException {
//		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		BufferedReader stdin = new BufferedReader(new InputStreamReader(new FileInputStream("test.in")));
		String line = "";
		while (true) {
			line = stdin.readLine();
			if (line == null)
				break;
			source += line + "\n";
//			source += line + System.getProperty("line.separator");
		}
		stdin.close();
	}

	// u pocetnom sam stanju
	static String state = "";
	static String source = "";

	static int pos = 0;
	static int maxPos = 0;
	static int npos = 0;
	static int index = 0;
	static int line = 0;
	static int good = 0;

	/**
	 * Metoda koja isprobava mijenjati stanje svim raspolozivim automatima tako
	 * da im preda trenutni procitani znak.
	 * @param c
	 * @return Vraca true ako postoji automat koji prihvaca znak
	 */
	static boolean feed(char c) {
		boolean sol = false;
		int ngood = 0;
		for (int i = tmpAut.size() - 1; i >= 0; i--) {
			// koji index
			int koji = tmpAut.get(i);
			rules.get(koji).getAutomaton().makeTransitions(c);

			if (rules.get(koji).getAutomaton().isAccepted()) {
				sol = true;
				index = koji;
			}
			if(!rules.get(koji).getAutomaton().getActiveStates().isEmpty()) {
				ngood++;
			}
		}
		good = ngood;
		return sol;
	}

	/**
	 * Metoda koja isprobava najdulji moguci string kao match raspolozivim
	 * automatima i pamti poziciju do koje smo dosli.
	 */
	static void findBestMatch() {
		maxPos = -1;
		// na pocetku su svi dohvatljivi automati dobri
		good = tmpAut.size();
		for (int i = pos; i < source.length(); i++) {
			if(good == 0) break;
			// ako postoji automat koji moze prozvakat sve ovo dosad
			if (feed(source.charAt(i))) {
				maxPos = i;
			}
		}
	}

	/**
	 * Metoda koja inicijalizira sve automate na pocetno stanje.
	 */
	static void initializeAutomata() {
		for(int i = 0; i < tmpAut.size(); i++) {
			rules.get(tmpAut.get(i)).getAutomaton().initialize();
		}
//		for (Rule rule : rules) {
//			rule.getAutomaton().initialize();
//		}
	}

	// lista INDEKSA automata dostupnih iz trenutnog stanja
	static ArrayList<Integer> tmpAut = new ArrayList<>();

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		inputStates();
		inputItems();
		inputRules();

		generateAvailableAutomata();
		state = LAstates.get(0);

		inputSource();
		// pocni!!!
		
		while (pos < source.length()) {

			// dohvati sve automate iz trenutnog stanja
			tmpAut = new ArrayList<>(availableAut.get(state));

			// inicijaliziraj automate dohvatljive iz TRENUTNOG stanja
			initializeAutomata();

			// nadji best match automat
			findBestMatch();

			if (maxPos == -1) {
				// nijedan automat ne prihvaca nista
				// oporavak od pogreske
				pos++;
				continue;
			}
			
			System.out.println(maxPos + " " + rules.get(index).getAutomaton().getRegex());

			npos = maxPos + 1;

			// inace smo nasli automat i varijabla index oznacava koji tocno
			// automat
			ArrayList<String> actions = rules.get(index).getActions();
			// ispuni akcije!
			
			for (int i = actions.size() - 1; i >= 0; --i) {
				String action = actions.get(i);

				if (action.equals("{") || action.equals("}"))
					continue;

				if (LAitems.contains(action)) {
					// leksem je, ispisi ga
					// prvi argument je ili leksem ili -
					System.out.println(action + " " + (line + 1) + " " + source.substring(pos, npos));
				}

				if (action.split(" ")[0].equals("UDJI_U_STANJE")) {
					state = action.split(" ")[1];
				}

				if (action.split(" ")[0].equals("VRATI_SE")) {
					// promijeni poziciju
					npos = pos + Integer.parseInt(action.split(" ")[1]);
				}

				if (action.equals("NOVI_REDAK")) {
					++line;
				}

				if (action.equals("-")) {
					// nesto
				}
			}
//			System.out.println(state);
			pos = npos;
		}

	}

}

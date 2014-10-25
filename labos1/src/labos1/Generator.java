package labos1;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.lang.String;

public class Generator {

	private static ArrayList<RegularDefinition> regDefs = new ArrayList<>();
	private static ArrayList<String> LAStates = new ArrayList<>();
	private static ArrayList<String> lexItems = new ArrayList<>();
	private static ArrayList<Rule> rules  = new ArrayList<>();
	
	private static Map<String, Automaton> mapa = new HashMap<>(); 
	
	public static void parseInput() throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String currLine;
		
		while ((currLine = br.readLine()) != null) {
			if (currLine.startsWith("%X")) break;
			String[] dubrovnik = currLine.split(" ");	
			regDefs.add(new RegularDefinition(dubrovnik[0], dubrovnik[1]));
		}
	
		parseRegularDefinitions();
		
		// parse LA states
		LAStates = new ArrayList<>(Arrays.asList(currLine.split(" ")));
		LAStates.remove(0);
		
		// parse lexical items
		currLine = br.readLine();
		lexItems = new ArrayList<>(Arrays.asList(currLine.split(" ")));
		lexItems.remove(0);
		
		// parse LA rules 
		while ((currLine = br.readLine()) != null) {
			
			String state = currLine.substring(1, currLine.indexOf('>'));
			String regex = parseRegex(currLine.substring(currLine.indexOf('>') + 1));
			ArrayList<String> actions = new ArrayList<>();
			
			while (!(currLine = br.readLine()).equals("}")) {
				if (currLine.equals("}")) continue;
				actions.add(currLine);
			}

			Automaton regAutomaton = null; 
			
			if (!mapa.containsKey(regex)) {
				regAutomaton = new Automaton(regex);
				mapa.put(regex, regAutomaton);
			} else {
				regAutomaton = mapa.get(regex);
			}

			rules.add(new Rule(state, regAutomaton, actions));
			
		}
		
	}
		
	/**
	 * Metoda za debug -- slobodno zanemariti 
	 */
	private static void printDefinitions() {
		for (RegularDefinition regDef : regDefs) 
			System.out.println(regDef.getName() + " " + regDef.getDefinition());
	}

	/**
	 * Ispravno parsisra regularne definicije. Preciznije, reference na prethodno definirane
	 * regularne izraze mijenja samim izrazom. 
	 */
	private static void parseRegularDefinitions() {
		for (int i = 1; i < regDefs.size(); ++i) {
			for (int j = 0; j < i; ++j) {
				regDefs.get(i).replaceNameWithDefinition(
						regDefs.get(j).getName(), regDefs.get(j).getDefinition()
				);
			}
		}
	}
	
	/**
	 * Gleda dal u konkretnom regexu ima nekih referenci...
	 * @param regex
	 */
	private static String parseRegex(String regex) {
		for (int i = 0; i < regDefs.size(); ++i) {
			regex = regex.replaceAll(
					regDefs.get(i).getName().replaceAll("\\{", "\\\\\\{").replaceAll("\\}", "\\\\\\}"), 
					"(" + regDefs.get(i).getDefinition() + ")"
			);	
		}
		return regex;
	}
	
	private static void outputCollections() throws IOException {
		
		
		FileOutputStream fout = new FileOutputStream("src/labos1/analizator/states.ser");
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(LAStates);
		
		fout.close();
		fout = new FileOutputStream("src/labos1/analizator/items.ser");
		oos = new ObjectOutputStream(fout);
		oos.writeObject(lexItems);
		
		fout.close();
		fout = new FileOutputStream("src/labos1/analizator/rules.ser");
		oos = new ObjectOutputStream(fout);
		oos.writeObject(rules);

		fout.close();
		oos.close();
		
	}
	
	public static void main(String[] args) throws IOException {
		
		parseInput();
		outputCollections();
		
		Automaton a1 = new Automaton("ABC");
		a1.printEverything();
		
		a1 = new Automaton("A|B|C");
		a1.printEverything();
			
		
		for (Rule R : rules) {
			R.getAutomaton().printEverything();
			System.out.println("----------------------------");
		}
		
		
	}
	
}

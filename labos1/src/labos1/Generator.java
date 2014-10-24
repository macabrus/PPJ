package labos1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.lang.String;

public class Generator {

	private static ArrayList<RegularDefinition> regDefs = new ArrayList<>();
	
	public static void parseInput() throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String currLine;
		
		while ((currLine = br.readLine()) != null) {
			if (currLine.startsWith("%X")) break;
			String[] dubrovnik = currLine.split(" ");	
			regDefs.add(new RegularDefinition(dubrovnik[0], dubrovnik[1]));
		}
	
		printDefinitions(); System.out.println("");
	
		parseRegularDefinitions();
		printDefinitions();
		
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
	
	public static void main(String[] args) throws IOException {
		
		parseInput();
		
	}
	
}

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class SemantickiAnalizator {

	public static TreeNode rootOfGenerativeTree;
	
	public static void main(String[] args) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		ArrayList<String> inputFileContents = new ArrayList<String>();
		
		String currLine;
		while ((currLine = br.readLine()) != null) inputFileContents.add(currLine);
		
		TreeParser parser = new TreeParser(inputFileContents);
		rootOfGenerativeTree = parser.getRoot();
		
		// Provjereno da radi na svim primjerima 
		// rootOfGenerativeTree.printSubtree(0);
		
		ActualAnalizator analizator = new ActualAnalizator(rootOfGenerativeTree);
		analizator.analyze();
		
		if (!analizator.gotError()) {
			// main
			if (analizator.noMain()) {
				System.out.println("main");
			} else {
				if (analizator.functionError()) System.out.println("funkcija");
				System.out.println("funkcija");
			}
		}
		
	}
	
}

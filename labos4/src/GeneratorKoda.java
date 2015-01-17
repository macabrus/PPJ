import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class GeneratorKoda {

	public static TreeNode rootOfGenerativeTree;

	public static void main(String[] args) throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		ArrayList<String> inputFileContents = new ArrayList<String>();

		String currLine;
		while ((currLine = br.readLine()) != null)
			inputFileContents.add(currLine);

		TreeParser parser = new TreeParser(inputFileContents);
		rootOfGenerativeTree = parser.getRoot();

		// Provjereno da radi na svim primjerima
		rootOfGenerativeTree.printSubtree(0);

		System.out.print("\n\tMOVE 40000, R7\n\tJP CMAIN\n");
//		"\tCALL MAIN\n\tHALT\n\nMAIN\n");
		
		// izraz pridruzivanja:
		System.out.print("\tLOAD R0, (BLA)\n\tPUSH R0\n");
		
		// return:
		System.out.print("\tPOP R6\n\tRET\n");
		
		// call main medjulabela
		
		
		// definiraj labele
		System.out.print("BLA\tDW %D 12\n");
	}
}

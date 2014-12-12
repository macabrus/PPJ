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
		
		rootOfGenerativeTree.printSubtree(0);
		
	}
	
}

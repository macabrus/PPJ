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
		// rootOfGenerativeTree.printSubtree(0);

		ActualAnalizator analizator = new ActualAnalizator(rootOfGenerativeTree);
		analizator.analyze();

		System.out.print("\t`BASE D\n");
		System.out.print("\tMOVE 40000, R7\n");
		String main = "\tCALL MAIN\n\tHALT\n\n";

		ArrayList<LabelTableNode> labels = analizator.getLabelTable();

		boolean notTab = true;
		for (String bla : analizator.getRoot().getKod().split("\n")) {
			if (!bla.startsWith("\t") && notTab) {
				System.out.print(main);
				notTab = false;
			}
			System.out.print(bla + "\n");
		}

		// System.out.println(analizator.getRoot().getKod());
		// for (LabelTableNode ltNode : labels) {
		// if (ltNode.isFunction()) {
		// System.out.print(ltNode.getLabela() + "\n");
		// System.out.print(ltNode.getValue().getKod() + "\n");
		// }
		// }
		//
		for (LabelTableNode ltNode : labels) {
			if (!ltNode.isFunction()) {
				System.out.print(ltNode.getLabela());
				if (ltNode.isEmpty()) {
					//System.out.print("\n");
					System.out.print("\tDW %D 0\n");
					continue;
				}
				System.out.print(ltNode.getBytes());
			}
		}
	}
}

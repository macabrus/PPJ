import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SA {

	private static HashSet<String> synchro = new HashSet<>();
	private static HashMap<Integer, HashMap<String, Action>> actions = new HashMap<>();
	private static HashMap<Integer, HashMap<String, Integer>> newState = new HashMap<>();
	private static ArrayList<LexUnit> lexUnits = new ArrayList<>();

	private static ObjectInput input;
	private static InputStream buffer;
	private static InputStream file;

	private static String source = "";

	@SuppressWarnings("unchecked")
	private static void inputSynchro() throws IOException, ClassNotFoundException {
		file = new FileInputStream("src/analizator/synchro.ser");
		buffer = new BufferedInputStream(file);
		input = new ObjectInputStream(buffer);
		synchro = (HashSet<String>) input.readObject();
	}

	@SuppressWarnings("unchecked")
	private static void inputActions() throws IOException, ClassNotFoundException {
		file = new FileInputStream("src/analizator/actions.ser");
		buffer = new BufferedInputStream(file);
		input = new ObjectInputStream(buffer);
		actions = (HashMap<Integer, HashMap<String, Action>>) input.readObject();
	}

	@SuppressWarnings("unchecked")
	private static void inputNewState() throws IOException, ClassNotFoundException {
		file = new FileInputStream("src/analizator/newState.ser");
		buffer = new BufferedInputStream(file);
		input = new ObjectInputStream(buffer);
		newState = (HashMap<Integer, HashMap<String, Integer>>) input.readObject();
	}

	private static void inputSource() throws IOException {
		// BufferedReader stdin = new BufferedReader(new
		// InputStreamReader(System.in));
		BufferedReader stdin = new BufferedReader(new InputStreamReader(new FileInputStream("test.in")));
		String line = "";
		while (true) {
			line = stdin.readLine();
			if (line == null)
				break;
			LexUnit unit = new LexUnit(line);
			lexUnits.add(unit);
			source += line + "\n";
		}
		stdin.close();
	}

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		inputSynchro();
		inputActions();
		inputNewState();
		inputSource();
		LRParser parser = new LRParser(lexUnits, actions, newState, synchro);
		parser.getRoot().printSubtree(0);
	}

}

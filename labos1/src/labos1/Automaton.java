package labos1;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Klasa koja modelira eps-NKA automat.
 * @author Ivan Paljak
 * 
 */
public class Automaton implements Serializable {

	private static final long serialVersionUID = 1L;

	class Pair implements Serializable {

		private static final long serialVersionUID = 1L;
		public int fst, snd;

		public Pair(int fst, int snd) {
			this.fst = fst;
			this.snd = snd;
		}
		
		@Override
		public String toString() {
			return this.fst + " -> " + this.snd;
		}
		
		@Override
		public boolean equals(Object obj) {
			return ((Pair) obj).fst == this.fst && ((Pair) obj).snd == this.snd;
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}

	}

	class Transition implements Serializable {

		private static final long serialVersionUID = 1L;
		public int fst, snd;
		public char c;

		public Transition(int fst, int snd, char c) {
			this.fst = fst;
			this.snd = snd;
			this.c = c;
		}

		@Override
		public String toString() {
			return c + ": " + this.fst + " -> " + this.snd;
		}
		
		@Override
		public boolean equals(Object obj) {
			return ((Transition) obj).fst == this.fst && ((Transition) obj).snd == this.snd
					&& ((Transition) obj).c == this.c;
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}

	}

	private ArrayList<Integer> activeStates;

	private int states;
	private String regex;
	private Pair P;

	ArrayList<Pair> epsTransitions = new ArrayList<>();
	ArrayList<Transition> transitions = new ArrayList<>();

	/**
	 * Konstruira automat koji prihvaca zadani regularni izraz, nakon stvaranja
	 * objekta automat je inicijaliziran!
	 * @param regex
	 */
	public Automaton(String regex) {
		this.regex = regex;
		P = constructAutomaton(this.regex);
		this.initialize();
	}

	/**
	 * @return pocetnp stanje automata
	 */
	public int getStartingState() {
		return P.fst;
	}

	/**
	 * @return prihvatljivo stanje automata
	 */
	public int getFinalState() {
		return P.snd;
	}

	/**
	 * @return True ako se automat nalazi u prihvatljivom stanju, inace false.
	 */
	public boolean isAccepted() {
		for (Integer i : activeStates) {
			if (i.intValue() == getFinalState())
				return true;
		}
		return false;
	}

	/**
	 * Inicijalizira automat
	 */
	public void initialize() {
		activeStates = new ArrayList<>();
		activeStates.add(this.getStartingState());
		makeEpsilonTransitions();
	}

	/**
	 * Debug metoda, slobodno zanemariti 
	 */
	public void printEverything() {
		
		System.out.println("Regex: " + this.regex);
		
		System.out.println("Pocetno stanje: " + this.getStartingState());
		System.out.println("Zavrsno stanje: " + this.getFinalState());
		
		System.out.println("\nPrijelazi (znak: stanje1 -> stanje2): ");
		for (Transition T : transitions) System.out.println(T);
		
		System.out.println("\nEpsilon prijelazi (stanje1 -> stanje2): " );
		for (Pair p : epsTransitions) System.out.println(p);
		
	}
	
	/**
	 * radi prijelaze s obzirom na ucitani znak. Znak '$' oznacava epsilon.
	 * @param c
	 */
	public void makeTransitions(char c) {

		if (c == '$') {
			makeEpsilonTransitions();
			return;
		}

		ArrayList<Integer> newStates = new ArrayList<>();

		for (Transition t : transitions) {
			if (!activeStates.contains(t.fst) || c != t.c)
				continue;
			newStates.add(t.snd);
		}

		this.activeStates = newStates;
		makeEpsilonTransitions();

	}

	private void makeEpsilonTransitions() {

		boolean addedState;
		do {
			addedState = false;
			for (Pair p : epsTransitions) {
				if (this.activeStates.contains(p.fst) && !this.activeStates.contains(p.snd)) {
					addedState = true;
					activeStates.add(p.snd);
				}
			}

		} while (addedState);

	}

	private int requestNewState() {
		return ++states;
	}

	/**
	 * Funkcija provjerava je li znak na odredjenom indeksu operator ili je
	 * escapean neparnim brojem '\'
	 * @param index indeks operatora
	 * @return true ako je operator, inace false.
	 */
	private boolean isOperator(int index, String regex) {
		int cnt = 0; --index;
		while (index >= 0 && regex.charAt(index) == '\\') {
			++cnt;
			--index;
		}
		return cnt % 2 == 0;
	}

	/**
	 * Dodaje novi epsilon prijelaz u listu epsilon prijelaza :)
	 * @param s1 first state
	 * @param s2 second state
	 */
	private void addEpsilonTransition(int s1, int s2) {
		epsTransitions.add(new Pair(s1, s2));
	}

	/**
	 * Dodaje novi prijelaz
	 * @param s1
	 * @param s2
	 * @param c
	 */
	private void addTransition(int s1, int s2, char c) {
		transitions.add(new Transition(s1, s2, c));
	}

	private int findClosed(String s, int i) {
		int brackets = 1;
		while (i < s.length()) {
			if (s.charAt(i) == '(')
				++brackets;
			if (s.charAt(i) == ')')
				--brackets;
			if (brackets == 0)
				return i;
			++i;
		}
		return -1;
	}

	private Pair constructAutomaton(String regex) {

		boolean foundIt = false;
		int brackets = 0, last = 0;
		ArrayList<String> choices = new ArrayList<>();

		for (int i = 0; i < regex.length(); ++i) {
			if (regex.charAt(i) == '(' && isOperator(i, regex))
				++brackets;
			if (regex.charAt(i) == ')' && isOperator(i, regex))
				--brackets;
			if (regex.charAt(i) == '|' && isOperator(i, regex) && brackets == 0) {
				choices.add(regex.substring(last, i));
				last = i + 1;
				foundIt = true;
			}
		}

		if (foundIt) {
			choices.add(regex.substring(last, regex.length()));
		}

		// gotova podjela s obzirom na '|'

		int leftState = this.requestNewState();
		int rightState = this.requestNewState();

		if (choices.size() != 0) {
			for (String choice : choices) {
				Pair tmp = constructAutomaton(choice);
				addEpsilonTransition(leftState, tmp.fst);
				addEpsilonTransition(tmp.snd, rightState);
			}
			return new Pair(leftState, rightState);
		}

		boolean escaped = false;
		int lastState = leftState;

		for (int i = 0; i < regex.length(); ++i) {

			int l = -1, r = -1;
			if (escaped) {

				escaped = false;
				char transChar = regex.charAt(i);
				if (regex.charAt(i) == 't')
					transChar = '\t';
				if (regex.charAt(i) == 'n')
					transChar = '\n';
				if (regex.charAt(i) == '_')
					transChar = ' ';

				l = requestNewState();
				r = requestNewState();
				addTransition(l, r, transChar);

			} else {

				if (regex.charAt(i) == '\\') {
					escaped = true;
					continue;
				}

				if (regex.charAt(i) != '(') {
					l = requestNewState();
					r = requestNewState();
					if (regex.charAt(i) == '$') {
						addEpsilonTransition(l, r);
					} else {
						addTransition(l, r, regex.charAt(i));
					}
				} else {
					int j = findClosed(regex, i + 1);
					Pair tmp = constructAutomaton(regex.substring(i + 1, j));
					l = tmp.fst;
					r = tmp.snd;
					i = j;
				}

			}

			// operator *
			if (i + 1 < regex.length() && regex.charAt(i + 1) == '*') {

				int _l = l, _r = r;
				l = requestNewState();
				r = requestNewState();

				addEpsilonTransition(l, _l);
				addEpsilonTransition(_r, r);
				addEpsilonTransition(l, r);
				addEpsilonTransition(_r, _l);

				++i;

			}

			addEpsilonTransition(lastState, l);
			lastState = r;

		}

		addEpsilonTransition(lastState, rightState);
		return new Pair(leftState, rightState);

	}

	public String getRegex() {
		return regex;
	}
	
	public void setRegex(String regex) {
		this.regex = regex;
	}

	public ArrayList<Integer> getActiveStates() {
		return activeStates;
	}
	

}

package labos1;

import java.util.ArrayList;

/**
 * Klasa koja modelira eps-NKA automat. 
 * @author Ivan Paljak
 *
 */
public class Automaton {

	class Pair {
		
		public int fst, snd;
		
		public Pair(int fst, int snd) {
			this.fst = fst;
			this.snd = snd;
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
	
	class Transition {
		
		public int fst, snd;
		public char c;
		
		public Transition(int fst, int snd, char c) {
			this.fst = fst;
			this.snd = snd;
			this.c = c;
		}
		
		@Override
		public boolean equals(Object obj) {
			return ((Transition) obj).fst == this.fst && ((Transition) obj).snd == this.snd &&
					((Transition) obj).c == this.c;
		}
		
		@Override
		public int hashCode() {
			return super.hashCode();
		}
		
	}
	
	private int states;
	
	private String regex; 
	
	private Pair P;
	
	ArrayList<Pair> epsTransitions = new ArrayList<>();
	ArrayList<Transition> transitions = new ArrayList<>();
	
	/**
	 * Konstruira automat koji prihvaca zadani regularni izraz
	 * @param regex
	 */
	public Automaton(String regex) {
		this.regex = regex;	
		P = constructAutomaton(this.regex);
	}
	
	private int requestNewState() {
		return ++states;
	}
	
	/**
	 * @return pocetno stanje automata
	 */
	private int getStart() {
		return P.fst;
	}
	
	/*
	 * @return prihvatljivo stanje automaata
	 */
	private int getAccept() {
		return P.snd;
	}
	
	/**
	 * Funkcija provjerava je li znak na odredjenom indeksu operator ili je 
	 * escapean neparnim brojem '\'
	 * @param index indeks operatora
	 * @return true ako je operator, inace false.
	 */
	private boolean isOperator(int index, String regex) {
		int cnt = 0;
		while (index > 0 && regex.charAt(index) == '\\') {++cnt; --index;}
		if (cnt % 2 == 0) return true; else return false;
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
			if (s.charAt(i) == '(') ++brackets;
			if (s.charAt(i) == ')') --brackets;
			if (brackets == 0) return i;
			++i;
		}
		return -1;
	}
	
	private Pair constructAutomaton(String regex) {
		
		boolean foundIt = false;
		int brackets = 0, last = 0;
		ArrayList<String> choices = new ArrayList<>();
		
		for (int i = 0; i < regex.length(); ++i) {
			if (regex.charAt(i) == '(' && isOperator(i, regex)) ++brackets;
			if (regex.charAt(i) == ')' && isOperator(i, regex)) --brackets;
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
				addEpsilonTransition(rightState, tmp.snd);
			}
			return new Pair(leftState, rightState);
		} 
		
		boolean escaped = false;
		int lastState = leftState;
		
		for (int i = 0; i < regex.length(); ++i) {
			
			int l = -1, r = -1;
			if (escaped) {
				
				char transChar = regex.charAt(i);	
				if (regex.charAt(i) == 't') transChar = '\t';
				if (regex.charAt(i) == 'n') transChar = '\n';
				if (regex.charAt(i) == '_') transChar = ' ';
				
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
					int j = findClosed(regex, i);
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
				
				addEpsilonTransition(_l, l);
				addEpsilonTransition(_r, r);
				addEpsilonTransition(l, r);
				addEpsilonTransition(_l, _r);
				
				++i;
				
			}
			
			addEpsilonTransition(lastState, l);
			lastState = r;
			
		}
		
		addEpsilonTransition(lastState, rightState);
		return new Pair(leftState, rightState);
		
	}
	
}

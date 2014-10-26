package analizator;


import java.io.Serializable;
import java.util.ArrayList;

/**
 * Klasa modelira skup leksickih pravila! 
 * 
 * @author Ivan Paljak
 */
public class Rule implements Serializable {


	private static final long serialVersionUID = 1L;
	private String state; 
	private Automaton automaton; 
	
	private ArrayList<String> actions;
	
	public Rule(String state, Automaton automaton, ArrayList<String> actions) {
		this.state = state; 
		this.automaton = automaton;
		this.actions = actions;
	}
	
	/* getteri */
	public String getState() {
		return this.state;
	}
	
	public Automaton getAutomaton() {
		return this.automaton;
	}
	
	public ArrayList<String> getActions() {
		return this.actions;
	}
	
}

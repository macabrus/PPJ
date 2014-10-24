package labos1;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Klasa modelira skup leksickih pravila! 
 * 
 * @author Ivan Paljak
 */
public class Rule implements Comparable<Rule>, Serializable {

	private String state; 
	private String regex; 
	
	private ArrayList<String> actions;
	
	public Rule(String state, String regex, ArrayList<String> actions) {
		this.state = state; 
		this.regex = regex;
		this.actions = actions;
	}
	
	@Override
	public int compareTo(Rule o) {
		return this.state.compareTo(o.state);
	}
	
	
}

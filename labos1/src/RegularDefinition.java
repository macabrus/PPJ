

import java.util.regex.Matcher;

/**
 * 
 * Klasa definira regularnu definiciju kao uredjen par (imeDefinicije, definicija). 
 * 
 * @author Ivan Paljak
 *
 */
public class RegularDefinition {
	
	private String name, definition; 
	
	/**
	 * prazan konstruktor
	 */
	public RegularDefinition() {
		this.name = this.definition = "";
	}
	
	/**
	 * Konstruktor iz fieldova
	 */
	public RegularDefinition(String name, String definition) {
		this.name = name; 
		this.definition = definition;
	}
	
	/* getteri */
	public String getName() {
		return this.name;
	}
	
	public String getDefinition() {
		return this.definition;
	}
	
	/* setteri */
	public void setName(String name) {
		this.name = name; 
	}
	
	public void setDefinition(String definition) {
		this.definition = definition;
	}
	
	/**
	 * Ukoliko se u nasoj definiciji nalazi referenca na neku drugu definiciju, 
	 * zamijenimo referencu s actual definicijom. Ime reference treba biti wrappano
	 * u '{}'. 
	 * 
	 * @param name ime reference
	 * @param definition definicija reference
	 */
	public void replaceNameWithDefinition(String name, String definition) {
		this.definition = this.definition.replaceAll(
				name.replaceAll("\\{", "\\\\\\{").replaceAll("\\}", "\\\\\\}"), 
				"(" + Matcher.quoteReplacement(definition) + ")"
		);		
	}
	
}

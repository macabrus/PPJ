

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Class modelling a single grammar production.
 * 
 * @author Paula Gombar, 0036474619
 * 
 */
public class Production {

	public String left;
	public ArrayList<String> right = new ArrayList<>();
	public int productionNumber;

	public Production() {
	}

	public Production(String left, String right, int number) {
		this.left = left;
		this.productionNumber = number;

		if (!right.equals("$")) {
			// set right side of production
			String[] dubrovnik = right.split(" ");
			for (String string : dubrovnik) {
				this.right.add(string);
			}
		}
	}
	
	public void fromProduction(Production p) {
		this.left = new String(p.left);
		for (String str : p.right) this.right.add(new String(str));
		this.productionNumber = p.productionNumber;
	}
	
}
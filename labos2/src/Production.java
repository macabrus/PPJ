import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class modelling a single grammar production. A grammar production consists of
 * a left side (nonterminal) and right side (array of both terminal and
 * nonterminal characters). We also have to keep track of the index of the
 * production, determined by the input file, in order to solve Reduce/Reduce
 * ambiguities.
 * 
 * @author Paula Gombar, 0036474619
 * 
 */
public class Production implements Serializable {

	private static final long serialVersionUID = -8446612721162372258L;
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
		for (String str : p.right)
			this.right.add(new String(str));
		this.productionNumber = p.productionNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + productionNumber;
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Production other = (Production) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (productionNumber != other.productionNumber)
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return left + "->" + right.toString();
	}

}
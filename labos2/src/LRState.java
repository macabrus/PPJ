import java.util.HashSet;

public class LRState {

	public Production production;
	public HashSet<String> set = new HashSet<>();
	public int dotPosition;

	public LRState(Production production) {
		this.production = production;
		this.dotPosition = 0;
		this.set.add("");
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(production.left);
		sb.append("->");

		for (int i = 0; i <= production.right.size(); i++) {
			if (i == dotPosition)
				sb.append("*");
			if (i != production.right.size())
				sb.append(production.right.get(i));
		}
		sb.append(", {");

		for (String s : set) {
			if (s.equals(""))
				sb.append("$,");
			else
				sb.append(s + ",");
		}
		sb.append("}");
		return sb.toString();
	}

}

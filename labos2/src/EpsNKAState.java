import java.util.HashSet;

public class EpsNKAState {

	public Production p = new Production();
	public int dotIndex = 0;
	public HashSet<String> starts = new HashSet<String>();

	public EpsNKAState() {
	}

	public EpsNKAState(Production p, int dotIndex) {
		this.p = p;
		dotIndex = 0;
	}

	public boolean isAlive() {
		return dotIndex < p.right.size();
	}

	public String afterDot() {
		return p.right.get(dotIndex);
	}
	
	public void copyState(EpsNKAState s) {
		this.p = new Production();
		this.p.fromProduction(s.p);

		this.dotIndex = s.dotIndex;

		this.starts = new HashSet<String>();
		for (String str : s.starts)
			this.starts.add(new String(str));
	}

	public void fromState(EpsNKAState s) {

		this.p = new Production();
		this.p.fromProduction(s.p);

		this.dotIndex = s.dotIndex + 1;

		this.starts = new HashSet<String>();
		for (String str : s.starts)
			this.starts.add(new String(str));

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(p.left);
		sb.append("->");

		for (int i = 0; i <= p.right.size(); i++) {
			if (i == dotIndex)
				sb.append("*");
			if (i != p.right.size())
				sb.append(p.right.get(i));
		}
		sb.append(", {");

		for (String s : starts) {
			if (s.equals(""))
				sb.append("$,");
			else
				sb.append(s + ",");
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dotIndex;
		result = prime * result + ((p == null) ? 0 : p.hashCode());
		result = prime * result + ((starts == null) ? 0 : starts.hashCode());
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
		EpsNKAState other = (EpsNKAState) obj;
		if (dotIndex != other.dotIndex)
			return false;
		if (p == null) {
			if (other.p != null)
				return false;
		} else if (!p.equals(other.p))
			return false;
		if (starts == null) {
			if (other.starts != null)
				return false;
		} else if (!starts.equals(other.starts))
			return false;
		return true;
	}

	
	
}
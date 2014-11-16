public class LexUnit {

	public String uniform;
	public String row;
	public String lexUnits;

	public LexUnit(String s) {
		int first = s.indexOf(" ");
		this.uniform = s.substring(0, first);

		s = s.substring(first + 1);
		int second = s.indexOf(" ");
		this.row = s.substring(0, second);

		s = s.substring(second + 1);
		this.lexUnits = s;
	}

	@Override
	public String toString() {
		return uniform + " " + row + " " + lexUnits;
	}

}

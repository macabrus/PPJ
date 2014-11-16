import java.io.Serializable;

/**
 * Class modelling an action of LR parser. Possible actions are shift(N),
 * reduce(Production) and accept().
 * 
 * @author Paula Gombar, 0036474619
 * 
 */
public class Action implements Serializable {

	private static final long serialVersionUID = -910710423669351320L;
	/**
	 * Index of state which we're shifting to. If -1, then the action is not
	 * shift.
	 */
	public int shift = -1;
	/**
	 * Production specifying reduction. If null, then the action is not reduce.
	 */
	public Production reduce = null;
	/**
	 * Boolean deciding whether the action is accept or not.
	 */
	public boolean accept = false;

	public Action() {
	}

	@Override
	public String toString() {
		if (accept)
			return "A";
		if (shift > -1)
			return "S" + shift;
		if (reduce != null)
			return "R" + reduce.toString();

		return "";
	}
	
	public boolean isNothing() {
		return shift == -1 && reduce == null && accept == false;
	}
	
	public boolean isAccept() {
		return accept;
	}
	
	public boolean isShift() {
		return shift != -1;
	}
	
	public boolean isReduce() {
		return reduce != null;
	}
	
}

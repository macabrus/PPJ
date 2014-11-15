/**
 * Class modelling an action of LR parser. Possible actions are shift(N),
 * reduce(Production) and accept().
 * 
 * @author Paula Gombar, 0036474619
 * 
 */
public class Action {

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

}

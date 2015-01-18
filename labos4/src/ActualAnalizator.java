import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The class that actually analyzes (?)
 * @author Ivan Paljak
 * 
 */
public class ActualAnalizator {

	private TreeNode root;
	private TableNode scope;

	private boolean error = false;
	private boolean hasMain = false;

	private ArrayList<String> declaredFunctions;
	private ArrayList<String> definedFunctions;

	private int labelCounter;
	private int functionLabelCounter;
	private int ifLabelCounter;
	private int povrAdrCounter;

	private ArrayList<LabelTableNode> labelTable = new ArrayList<>();

	public ArrayList<LabelTableNode> getLabelTable() {
		return labelTable;
	}

	/**
	 * Constructs class from root of generative tree
	 * @param root Root of generative tree
	 */
	public ActualAnalizator(TreeNode root) {
		this.root = root;
		scope = new TableNode();
		declaredFunctions = new ArrayList<String>();
		definedFunctions = new ArrayList<String>();
		this.labelCounter = 0;
		this.functionLabelCounter = 0;
		this.ifLabelCounter = 0;
	}

	public void analyze() {
		prijevodnaJedinica(root);
	}

	public TreeNode getRoot() {
		return root;
	}

	// ------------------------------------------------
	// POCETAK IZRAZA
	// ------------------------------------------------

	private void primarniIzraz(TreeNode node) {
		TreeNode init = node.getChildAt(0);

		if (init.getContent().startsWith("IDN")) {
			if (!isDeclaredYet(init.getName())) {
				printErrorMessage(node);
				return;
			}
			node.setType(init.getType(scope));
			node.setTypes(init.getTypes(scope));
			node.setName(init.getName());
			node.setLValue(init.getLValue(scope));

			// staviti na stog iz adrese od [nadji identifikator u
			// scopeu]

			TreeNode idn = getDeclaredYet(init.getName());
			String label = idn.getLabela();

			if (node.isFunction()) {
				node.appendKod("\tCALL " + label + "\n");
				if (node.getTypes(scope).size() == 1 && !node.getTypeAt(0).equals("void"))
					node.appendKod("\tADD R7, " + node.getTypes(scope).size() * 4 + ", R7\n");
				node.appendKod("\tPUSH R6\n");
			} else {
				node.appendKod("\tLOAD R0, (" + label + ")\n");
				node.appendKod("\tPUSH R0\n");
			}

			node.setLabela(label);
		}
		if (init.getContent().startsWith("BROJ")) {
			if (!isInt(node.getChildAt(0).getName())) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			// node.setName(init.getName());
			node.setLValue(false);

			init.setLabela("L" + labelCounter);
			++labelCounter;

			// dodaj labelu i DW int u globalnu tablicu
			labelTable.add(new LabelTableNode(init.getLabela(), init));

			node.appendKod("\tLOAD R0, (" + init.getLabela() + ")\n");
			node.appendKod("\tPUSH R0\n");
			node.setLabela(init.getLabela());
		}
		if (init.getContent().startsWith("ZNAK")) {
			if (!isChar(node.getChildAt(0).getName())) {
				printErrorMessage(node);
				return;
			}
			node.setType("char");
			// node.setName(init.getName());
			node.setLValue(false);

			init.setLabela("L" + labelCounter);
			++labelCounter;
			// dodaj labelu i DW int u globalnu tablicu
			labelTable.add(new LabelTableNode(init.getLabela(), init));

			node.appendKod("\tLOAD R0, (" + init.getLabela() + ")\n");
			node.appendKod("\tPUSH R0\n");
			node.setLabela(init.getLabela());
		}
		if (init.getContent().startsWith("NIZ_ZNAKOVA")) {
			if (!isString(node.getChildAt(0).getName())) {
				printErrorMessage(node);
				return;
			}
			node.setType("nizchar");
			node.setConst();
			// node.setName(init.getName());
			node.setLValue(false);

			// TODO
		}
		if (init.getContent().startsWith("L_ZAGRADA")) {
			izraz(node.getChildAt(1));
			if (error)
				return;
			node.setType(node.getChildAt(1).getType(scope));
			node.setLValue(node.getChildAt(1).getLValue(scope));

			node.appendKod(node.getChildAt(1).getKod());
		}
	}

	private void postfiksIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			primarniIzraz(node.getChildAt(0));
			if (error)
				return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));

			node.appendKod(node.getChildAt(0).getKod());
			node.setLabela(node.getChildAt(0).getLabela());
			return;
		}
		if (node.getChildAt(1).getContent().startsWith("L_UGL_ZAGRADA")) {
			// TODO str 52. pise da nema vise dimenzija?
			postfiksIzraz(node.getChildAt(0));
			if (error)
				return;
			String X;
			boolean isConst;
			if (!node.getChildAt(0).getType(scope).startsWith("niz")) {
				printErrorMessage(node);
				return;
			}
			X = node.getChildAt(0).getType(scope).substring(3);
			isConst = node.getChildAt(0).isConst();
			izraz(node.getChildAt(2));
			if (error)
				return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType(X);
			node.setLValue(!isConst);
			node.setName(node.getChildAt(0).getName());
			node.setTypes(node.getChildAt(0).getTypes(scope));
			
			String labela = getDeclaredYet(node.getChildAt(0).getName()).getLabela();
			node.appendKod(node.getChildAt(2).getKod());

			node.appendKod("\tPOP R0\n");
			node.appendKod("\tSHL R0, %D 2, R0\n");
			
			node.appendKod("\tMOVE " + labela + ", R1\n");
			node.appendKod("\tADD R0, R1, R0\n");
			
			node.appendKod("\tLOAD R0, (R0)\n\tPUSH R0\n");	
			
			
			
			return;
			
		}
		if (node.getChildren().size() == 3) {
			postfiksIzraz(node.getChildAt(0));
			if (error)
				return;
			if (!node.getChildAt(0).isFunction() || !node.getChildAt(0).getTypeAt(0).equals("void")) {
				printErrorMessage(node);
				return;
			}
			node.setType(node.getChildAt(0).getType(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(false);

			node.appendKod(node.getChildAt(0).getKod());
		}
		if (node.getChildren().size() == 4) {
			postfiksIzraz(node.getChildAt(0));
			if (error)
				return;
			listaArgumenata(node.getChildAt(2));
			if (error)
				return;
			if (!node.getChildAt(0).isFunction()) {
				printErrorMessage(node);
				return;
			}
			TreeNode pIzraz = node.getChildAt(0);
			TreeNode listArg = node.getChildAt(2);
			if (pIzraz.getTypes(scope).size() != listArg.getTypes(scope).size()) {
				printErrorMessage(node);
				return;
			}
			for (int i = 0; i < pIzraz.getTypes(scope).size(); ++i) {
				if (!isCastable(listArg.getTypeAt(i), pIzraz.getTypeAt(i))) {
					printErrorMessage(node);
					return;
				}
			}
			node.setType(pIzraz.getType(scope));
			node.setLValue(false);

			node.appendKod(node.getChildAt(2).getKod());
			node.appendKod(node.getChildAt(0).getKod());

		}
		if (node.getChildren().size() == 2) {
			postfiksIzraz(node.getChildAt(0));
			if (error)
				return;
			TreeNode pIzraz = node.getChildAt(0);
			if (!pIzraz.getLValue(scope) || !isCastable(pIzraz.getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		}
	}

	private void listaArgumenata(TreeNode node) {
		if (node.getChildren().size() == 1) {
			izrazPridruzivanja(node.getChildAt(0));
			if (error)
				return;
			node.addType(node.getChildAt(0).getType(scope));

			node.appendKod(node.getChildAt(0).getKod());
		} else {
			listaArgumenata(node.getChildAt(0));
			if (error)
				return;
			izrazPridruzivanja(node.getChildAt(2));
			if (error)
				return;
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.addType(node.getChildAt(2).getType(scope));
		}
	}

	private void unarniIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			postfiksIzraz(node.getChildAt(0));
			if (error)
				return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));

			node.appendKod(node.getChildAt(0).getKod());
			//node.appendKod("\tPOP R0\n\tLOAD R0, (R0)\n\tPUSH R0\n");
			
			node.setLabela(node.getChildAt(0).getLabela());
			
		} else if (node.getChildAt(1).getContent().equals("<unarni_izraz>")) {
			unarniIzraz(node.getChildAt(1));
			if (error)
				return;
			if (!node.getChildAt(1).getLValue(scope) || !isCastable(node.getChildAt(1).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		} else {
			castIzraz(node.getChildAt(1));
			if (error)
				return;
			if (!isCastable(node.getChildAt(1).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);

			// MIJENJAM
			node.appendKod(node.getChildAt(1).getKod());
			node.appendKod("\tPOP R0\n");
			//System.out.println(node.getChildAt(0).getChildAt(0).getName());
			if (node.getChildAt(0).getChildAt(0).getName().equals("-")) {
				node.appendKod("\tXOR R0, -1, R0\n\tADD R0, 1, R0\n");
			}
			if (node.getChildAt(0).getChildAt(0).getName().equals("~")) {
				node.appendKod("\tXOR R0, %D -1, R0\n");
			}
			
			node.appendKod("\tPUSH R0\n");
		}
	}

	private void castIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			unarniIzraz(node.getChildAt(0));
			if (error)
				return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));

			node.appendKod(node.getChildAt(0).getKod());
			node.setLabela(node.getChildAt(0).getLabela());
			
		} else {
			imeTipa(node.getChildAt(1));
			if (error)
				return;
			castIzraz(node.getChildAt(3));
			if (error)
				return;
			if (!isCastable(node.getChildAt(3).getType(scope), node.getChildAt(1).getType(scope))
					&& !(node.getChildAt(3).getType(scope).equals("int") && node.getChildAt(1).getType(scope)
							.equals("char")) || node.getChildAt(3).isFunction() || node.getChildAt(1).isFunction()) {
				printErrorMessage(node);
				return;
			}
			node.setType(node.getChildAt(1).getType(scope));
			node.setLValue(false);
			
			node.appendKod(node.getChildAt(3).getKod());
			node.setLabela(node.getChildAt(3).getLabela());
		}
	}

	private void imeTipa(TreeNode node) {

		// evo ti komentar
		if (node.getChildren().size() == 1) {
			specifikatorTipa(node.getChildAt(0));
			node.setType(node.getChildAt(0).getType(scope));
		} else {
			specifikatorTipa(node.getChildAt(1));
			TreeNode specTip = node.getChildAt(1);
			if (specTip.getType(scope).equals("void"))
				printErrorMessage(node);
			if (error)
				return;
			node.setType(node.getChildAt(1).getType(scope));
			node.setConst();
		}
	}

	private void specifikatorTipa(TreeNode node) {
		if (node.getChildAt(0).getContent().startsWith("KR_VOID"))
			node.setType("void");
		if (node.getChildAt(0).getContent().startsWith("KR_CHAR"))
			node.setType("char");
		if (node.getChildAt(0).getContent().startsWith("KR_INT"))
			node.setType("int");
	}

	private void multiplikativniIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			castIzraz(node.getChildAt(0));
			if (error)
				return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));

			node.appendKod(node.getChildAt(0).getKod());
			node.setLabela(node.getChildAt(0).getLabela());
			
		} else {
			multiplikativniIzraz(node.getChildAt(0));
			if (error)
				return;
			if (!isCastable(node.getChildAt(0).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			castIzraz(node.getChildAt(2));
			if (error)
				return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		}
	}

	private void aditivniIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			multiplikativniIzraz(node.getChildAt(0));
			if (error)
				return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));

			node.appendKod(node.getChildAt(0).getKod());
			node.setLabela(node.getChildAt(0).getLabela());
			
		} else {
			aditivniIzraz(node.getChildAt(0));
			if (error)
				return;
			if (!isCastable(node.getChildAt(0).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			multiplikativniIzraz(node.getChildAt(2));
			if (error)
				return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);

			// MIJENJAM
			node.appendKod(node.getChildAt(0).getKod());
			node.appendKod(node.getChildAt(2).getKod());

			node.appendKod("\tPOP R0\n");
			node.appendKod("\tPOP R1\n");

			if (node.getChildAt(1).getContent().startsWith("PLUS")) {
				node.appendKod("\tADD R0, R1, R0\n");
			} else {
				node.appendKod("\tSUB R1, R0, R0\n");
			}
			node.appendKod("\tPUSH R0\n");
		}
	}

	private void odnosniIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			aditivniIzraz(node.getChildAt(0));
			if (error)
				return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));

			node.appendKod(node.getChildAt(0).getKod());
			node.setLabela(node.getChildAt(0).getLabela());
			
		} else {
			odnosniIzraz(node.getChildAt(0));
			if (error)
				return;
			if (!isCastable(node.getChildAt(0).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			aditivniIzraz(node.getChildAt(2));
			if (error)
				return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);

			node.appendKod(node.getChildAt(0).getKod());
			node.appendKod(node.getChildAt(2).getKod());

			node.appendKod("\tPOP R1\n");
			node.appendKod("\tPOP R0\n");
			node.appendKod("\tCMP R0, R1\n");

			HashMap<String, String> mapa = new HashMap<String, String>();
			mapa.put("<", "SLT");
			mapa.put(">", "SGT");
			mapa.put("<=", "SLE");
			mapa.put(">=", "SGE");

			String labelaTrue = "TRUE" + ifLabelCounter;
			String labelaFalse = "FALSE" + ifLabelCounter;
			String labelaEndIf = "ENDIF" + ifLabelCounter++;

			// dodaj JP TRUE
			node.appendKod("\tJP_" + mapa.get(node.getChildAt(1).getName()) + " " + labelaTrue + "\n");
			// slijedi false kod
			node.appendKod(labelaFalse + "\n");
			node.appendKod("\tMOVE 0, R2\n");
			node.appendKod("\tJP " + labelaEndIf + "\n");

			// slijedi true kod
			node.appendKod(labelaTrue + "\n");
			node.appendKod("\tMOVE 1, R2\n");

			// slijedi endif
			node.appendKod(labelaEndIf + "\n");
			node.appendKod("\tPUSH R2\n");

		}
	}

	private void jednakosniIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			odnosniIzraz(node.getChildAt(0));
			if (error)
				return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));

			node.appendKod(node.getChildAt(0).getKod());
			node.setLabela(node.getChildAt(0).getLabela());
			
		} else {
			jednakosniIzraz(node.getChildAt(0));
			if (error)
				return;
			if (node.getChildAt(0).isFunction() || !isCastable(node.getChildAt(0).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			odnosniIzraz(node.getChildAt(2));
			if (error)
				return;
			if (node.getChildAt(2).isFunction() || !isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		}
	}

	private void binIIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			jednakosniIzraz(node.getChildAt(0));
			if (error)
				return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));

			node.appendKod(node.getChildAt(0).getKod());
			node.setLabela(node.getChildAt(0).getLabela());
			
		} else {
			binIIzraz(node.getChildAt(0));
			if (error)
				return;
			if (!isCastable(node.getChildAt(0).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			jednakosniIzraz(node.getChildAt(2));
			if (error)
				return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);

			node.appendKod(node.getChildAt(0).getKod());
			node.appendKod(node.getChildAt(2).getKod());
			node.appendKod("\tPOP R0\n\tPOP R1\n");
			node.appendKod("\tAND R0, R1, R0\n");
			node.appendKod("\tPUSH R0\n");
		}
	}

	private void binXiliIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			binIIzraz(node.getChildAt(0));
			if (error)
				return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));

			node.appendKod(node.getChildAt(0).getKod());
			node.setLabela(node.getChildAt(0).getLabela());
			
		} else {
			binXiliIzraz(node.getChildAt(0));
			if (error)
				return;
			if (!isCastable(node.getChildAt(0).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			binIIzraz(node.getChildAt(2));
			if (error)
				return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);

			node.appendKod(node.getChildAt(0).getKod());
			node.appendKod(node.getChildAt(2).getKod());
			node.appendKod("\tPOP R0\n\tPOP R1\n");
			node.appendKod("\tXOR R0, R1, R0\n");
			node.appendKod("\tPUSH R0\n");
		}
	}

	private void binIliIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			binXiliIzraz(node.getChildAt(0));
			if (error)
				return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));

			node.appendKod(node.getChildAt(0).getKod());
			node.setLabela(node.getChildAt(0).getLabela());
			
		} else {
			binIliIzraz(node.getChildAt(0));
			if (error)
				return;
			if (!isCastable(node.getChildAt(0).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			binXiliIzraz(node.getChildAt(2));
			if (error)
				return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);

			node.appendKod(node.getChildAt(0).getKod());
			node.appendKod(node.getChildAt(2).getKod());
			node.appendKod("\tPOP R0\n\tPOP R1\n");
			node.appendKod("\tOR R0, R1, R0\n");
			node.appendKod("\tPUSH R0\n");
		}
	}

	private void logIIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			binIliIzraz(node.getChildAt(0));
			if (error)
				return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));

			node.appendKod(node.getChildAt(0).getKod());
			node.setLabela(node.getChildAt(0).getLabela());
			
		} else {
			logIIzraz(node.getChildAt(0));
			if (error)
				return;
			if (!isCastable(node.getChildAt(0).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			binIliIzraz(node.getChildAt(2));
			if (error)
				return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		}
	}

	private void logIliIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			logIIzraz(node.getChildAt(0));
			if (error)
				return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));

			node.appendKod(node.getChildAt(0).getKod());
			node.setLabela(node.getChildAt(0).getLabela());
			
		} else {
			logIliIzraz(node.getChildAt(0));
			if (error)
				return;
			if (!isCastable(node.getChildAt(0).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			logIIzraz(node.getChildAt(2));
			if (error)
				return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		}
	}

	private void izrazPridruzivanja(TreeNode node) {
		if (node.getChildren().size() == 1) {
			logIliIzraz(node.getChildAt(0));
			if (error)
				return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));

			node.appendKod(node.getChildAt(0).getKod());
			node.setLabela(node.getChildAt(0).getLabela());
			
		} else {
			postfiksIzraz(node.getChildAt(0));
			if (error)
				return;
			if (!node.getChildAt(0).getLValue(scope)) {
				printErrorMessage(node);
				return;
			}
			izrazPridruzivanja(node.getChildAt(2));
			if (error)
				return;
			if (!isCastable(node.getChildAt(2).getType(scope), node.getChildAt(0).getType(scope))) {
				printErrorMessage(node);
				return;
			}
			node.setType(node.getChildAt(0).getType(scope));
			node.setLValue(false);
			
			for (int i = 0; i < node.getChildAt(0).getKod().split("\n").length - 2; ++i) {
				node.appendKod((node.getChildAt(0).getKod().split("\n"))[i] + "\n");
			}
			
			node.appendKod("\tPUSH R0\n");
			
			//node.appendKod(node.getChildAt(0).getKod());
			//node.appendKod("\tPOP R0\n");
			node.appendKod(node.getChildAt(2).getKod());
			
			node.appendKod("\tPOP R1\n");
			node.appendKod("\tPOP R0\n");
			
			node.appendKod("\tSTORE R1, (R0)\n");
			
			//System.out.println("mirko" + node.getChildAt(0).getName());
			// System.out.println("mirko" + node.getChildAt(2).getName());
			
			
		}
	}

	private void izraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			izrazPridruzivanja(node.getChildAt(0));
			if (error)
				return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));

			node.appendKod(node.getChildAt(0).getKod());
		} else {
			izraz(node.getChildAt(0));
			if (error)
				return;
			izrazPridruzivanja(node.getChildAt(2));
			if (error)
				return;
			node.setType(node.getChildAt(2).getType(scope));
			node.setLValue(false);
		}
	}

	// ------------------------------------------------
	// KRAJ IZRAZA
	// ------------------------------------------------

	// ------------------------------------------------
	// POCETAK NAREDBENE STRUKTURE PROGRAMA
	// ------------------------------------------------

	private void slozenaNaredba(TreeNode node) {
		TableNode copyOfScope = new TableNode(scope.getParent());
		copyOfScope.setDeclaredStuff(scope.getDeclaredStuff());
		if (scope.isInLoop())
			copyOfScope.setLoop();

		TableNode newScope = new TableNode(copyOfScope);
		if (node.isInLoop() || scope.isInLoop())
			newScope.setLoop();
		scope = newScope;

		if (node.isInLoop())
			scope.setLoop();

		// System.out.println(node.getTypes(scope).size());
		// System.out.println(node.getNames().size());

		for (int i = 0; i < node.getTypes(scope).size(); ++i) {
			TreeNode newNode = new TreeNode(-1, "<" + node.getNames().get(i));
			newNode.setType(node.getTypes(scope).get(i));
			if (node.isInLoop())
				newNode.setLoop();
			newNode.setName(node.getNames().get(i));
			scope.addChild(newNode);
		}

		int offset = 4;
		for (String n : node.getNames()) {
			node.appendKod("\tLOAD R0, (R7+" + offset + ")\n");
			String labela = "L" + labelCounter++;
			node.appendKod("\tSTORE R0, (" + labela + ")\n");
			getDeclaredYet(n).setLabela(labela);
			offset += 4;
			LabelTableNode ltnode = new LabelTableNode(labela, null);
			ltnode.setEmpty(true);
			labelTable.add(ltnode);
		}

		// gadna brija neka

		if (node.getChildren().size() == 3) {
			listaNaredbi(node.getChildAt(1));
			if (error)
				return;

			node.appendKod(node.getChildAt(1).getKod());
		} else {
			if (node.isInLoop())
				node.getChildAt(1).setLoop();
			listaDeklaracija(node.getChildAt(1));
			if (error)
				return;
			listaNaredbi(node.getChildAt(2));
			if (error)
				return;
			node.appendKod(node.getChildAt(1).getKod());
			node.appendKod(node.getChildAt(2).getKod());

		}
		scope = scope.getParent();
	}

	private void listaNaredbi(TreeNode node) {
		if (node.getChildren().size() == 1) {
			naredba(node.getChildAt(0));
			if (error)
				return;
			node.appendKod(node.getChildAt(0).getKod());
		} else {
			listaNaredbi(node.getChildAt(0));
			if (error)
				return;
			naredba(node.getChildAt(1));
			if (error)
				return;

			node.appendKod(node.getChildAt(0).getKod());
			node.appendKod(node.getChildAt(1).getKod());
		}
	}

	private void naredba(TreeNode node) {
		TreeNode rightSide = node.getChildAt(0);
		if (node.isInLoop())
			rightSide.setLoop();
		if (rightSide.getContent().equals("<slozena_naredba>"))
			slozenaNaredba(rightSide);
		if (rightSide.getContent().equals("<izraz_naredba>"))
			izrazNaredba(rightSide);
		if (rightSide.getContent().equals("<naredba_grananja>"))
			naredbaGrananja(rightSide);
		if (rightSide.getContent().equals("<naredba_petlje>"))
			naredbaPetlje(rightSide);
		if (rightSide.getContent().equals("<naredba_skoka>"))
			naredbaSkoka(rightSide);
		if (error)
			return;

		node.appendKod(rightSide.getKod());
	}

	private void izrazNaredba(TreeNode node) {
		if (node.getChildren().size() == 1) {
			node.setType("int");
			node.appendKod(node.getChildAt(0).getKod());
		} else {
			izraz(node.getChildAt(0));
			if (error)
				return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.appendKod(node.getChildAt(0).getKod());	
		}
	}

	private void naredbaGrananja(TreeNode node) {
		izraz(node.getChildAt(2));
		if (error)
			return;
		if (!isCastable(node.getChildAt(2).getType(scope), "int") || node.getChildAt(2).isFunction()) {
			printErrorMessage(node);
			return;
		}
		naredba(node.getChildAt(4));
		if (error)
			return;

		if (node.getChildren().size() > 5) {
			naredba(node.getChildAt(6));
			if (error)
				return;
		}

		String labelaThen = "THEN" + ifLabelCounter;
		String labelaElse = "ELSE" + ifLabelCounter;
		String labelaEndIf = "ENDIF" + ifLabelCounter++;

		node.appendKod(node.getChildAt(2).getKod());
		node.appendKod("\tPOP R0\n");
		node.appendKod("\tCMP R0, 0\n");

		node.appendKod("\tJP_EQ " + labelaElse + "\n");
		node.appendKod(labelaThen + "\n");

		node.appendKod(node.getChildAt(4).getKod());
		node.appendKod("\tJP " + labelaEndIf + "\n");

		node.appendKod("\tJP_NE " + labelaElse + "\n");
		node.appendKod(labelaElse + "\n");

		if (node.getChildren().size() == 7) {
			node.appendKod(node.getChildAt(6).getKod());
		}

		node.appendKod(labelaEndIf + "\n");
	}

	private void naredbaPetlje(TreeNode node) {
		if (node.getChildren().size() == 5) {
			izraz(node.getChildAt(2));
			if (error)
				return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int") || node.getChildAt(2).isFunction()) {
				printErrorMessage(node);
				return;
			}
			node.getChildAt(4).setLoop();
			naredba(node.getChildAt(4));
			if (error)
				return;
		}
		if (node.getChildren().size() == 6) {
			izrazNaredba(node.getChildAt(2));
			if (error)
				return;
			izrazNaredba(node.getChildAt(3));
			if (error)
				return;
			if (!isCastable(node.getChildAt(3).getType(scope), "int") || node.getChildAt(3).isFunction()) {
				printErrorMessage(node);
				return;
			}
			node.getChildAt(5).setLoop();
			naredba(node.getChildAt(5));
			if (error)
				return;
		}
		if (node.getChildren().size() == 7) {
			izrazNaredba(node.getChildAt(2));
			if (error)
				return;
			izrazNaredba(node.getChildAt(3));
			if (error)
				return;
			if (!isCastable(node.getChildAt(3).getType(scope), "int") || node.getChildAt(3).isFunction()) {
				printErrorMessage(node);
				return;
			}
			izraz(node.getChildAt(4));
			if (error)
				return;
			node.getChildAt(6).setLoop();
			naredba(node.getChildAt(6));
			if (error)
				return;
		}
	}

	private void naredbaSkoka(TreeNode node) {
		if (node.getChildren().size() == 3) {
			izraz(node.getChildAt(1));
			if (error)
				return;
			String type = getTypeOfCurrentFunction();
			if (!isCastable(node.getChildAt(1).getType(scope), type) || node.getChildAt(1).isFunction()) {
				printErrorMessage(node);
				return;
			}

			node.appendKod(node.getChildAt(1).getKod());
			node.appendKod("\tPOP R6\n\tRET\n");
		} else {
			if (node.getChildAt(0).getContent().startsWith("KR_RETURN")) {
				// ArrayList<String> parameters = getTypesOfCurrentFunction();
				if (!getTypeOfCurrentFunction().equals("void")) {
					printErrorMessage(node);
					return;
				}
				node.appendKod("\tRET\n");
			} else {
				if (!scope.isInLoop()) {
					printErrorMessage(node);
					return;
				}
				// System.out.println("local scope: ");
				// for (TreeNode decl : scope.getDeclaredStuff())
				// System.out.println(decl.getName());
			}
		}
	}

	private void prijevodnaJedinica(TreeNode node) {
		if (node.getChildren().size() == 1) {
			vanjskaDeklaracija(node.getChildAt(0));
			if (error)
				return;
			node.appendKod(node.getChildAt(0).getKod());
		} else {
			prijevodnaJedinica(node.getChildAt(0));
			if (error)
				return;
			vanjskaDeklaracija(node.getChildAt(1));
			if (error)
				return;
			node.appendKod(node.getChildAt(0).getKod());
			node.appendKod(node.getChildAt(1).getKod());
		}
	}

	private void vanjskaDeklaracija(TreeNode node) {
		if (node.getChildAt(0).getContent().equals("<definicija_funkcije>")) {
			definicijaFunkcije(node.getChildAt(0));
			if (error)
				return;
		} else {
			deklaracija(node.getChildAt(0));
			if (error)
				return;
		}
		node.appendKod(node.getChildAt(0).getKod());
	}

	// ------------------------------------------------
	// KRAJ NAREDBENE STRUKTURE PROGRAMA
	// ------------------------------------------------

	// ------------------------------------------------
	// POCETAK DEKLARACIJA I DEFINICIJA
	// ------------------------------------------------

	private void definicijaFunkcije(TreeNode node) {
		imeTipa(node.getChildAt(0));
		if (node.getChildAt(0).isConst()) {
			printErrorMessage(node);
			return;
		}
		if (existsFunctionBefore(scope, node.getChildAt(1).getName(), node.getType(scope))) {
			printErrorMessage(node);
			return;
		}
		if (node.getChildAt(3).getContent().startsWith("KR_VOID")) {
			if (conflictingDeclaration(scope, node.getChildAt(0).getName(), node.getChildAt(0).getType(scope))) {
				printErrorMessage(node);
				return;
			}
			node.setDefined(true);
			node.setName(node.getChildAt(1).getName());
			node.setType(node.getChildAt(0).getType(scope));
			node.addType("void");
			if (node.getName().equals("main") && node.getType(scope).equals("int"))
				hasMain = true;
			scope.addChild(node);
			definedFunctions.add(node.getName());
			slozenaNaredba(node.getChildAt(5));
			if (error)
				return;

			if (node.getChildAt(1).getName().equals("main")) {
				node.setLabela("MAIN");
			} else {
				node.setLabela("F" + functionLabelCounter);
				++functionLabelCounter;
			}

			node.appendKod(node.getLabela());
			node.appendKod(node.getChildAt(5).getKod());

			LabelTableNode ltnode = new LabelTableNode(node.getLabela(), node);
			ltnode.setFunction(true);
			// dodaj labelu u globalnu tablicu
			labelTable.add(ltnode);
		} else {
			listaParametara(node.getChildAt(3));
			if (error)
				return;
			if (conflictingDeclaration(scope, node.getFunctionName(), node.getType(scope))) {
				printErrorMessage(node);
				return;
			}
			node.setDefined(true);
			node.setType(node.getChildAt(0).getType(scope));
			node.setName(node.getChildAt(1).getName());
			node.getChildAt(5).setTypes(node.getChildAt(3).getTypes(scope));
			node.setTypes(node.getChildAt(3).getTypes(scope));
			node.getChildAt(5).setNames(node.getChildAt(3).getNames());
			node.setNames(node.getChildAt(3).getNames());

			/*
			 * System.out.println("Types: "); for (String type :
			 * node.getChildAt(5).getTypes(scope)) System.out.println(type);
			 * 
			 * System.out.println("Names: "); for (String name :
			 * node.getChildAt(5).getNames()) System.out.println(name);
			 */

			if (node.getChildAt(1).getName().equals("main")) {
				node.setLabela("MAIN");
			} else {
				node.setLabela("F" + functionLabelCounter);
				++functionLabelCounter;
			}

			node.appendKod(node.getLabela());

			definedFunctions.add(node.getName());
			scope.addChild(node);
			slozenaNaredba(node.getChildAt(5));
			if (error)
				return;

			node.appendKod(node.getChildAt(5).getKod());

			LabelTableNode ltnode = new LabelTableNode(node.getLabela(), node);
			ltnode.setFunction(true);
			// dodaj labelu u globalnu tablicu
			labelTable.add(ltnode);
		}

	}

	private void listaParametara(TreeNode node) {
		if (node.getChildren().size() == 1) {
			deklaracijaParametara(node.getChildAt(0));
			if (error)
				return;
			node.addType(node.getChildAt(0).getType(scope));
			node.addName(node.getChildAt(0).getName());
		} else {

			listaParametara(node.getChildAt(0));
			if (error)
				return;
			deklaracijaParametara(node.getChildAt(2));
			if (error)
				return;

			if (node.getChildAt(0).getNames().contains(node.getChildAt(2).getName())) {
				printErrorMessage(node);
				return;
			}

			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.addType(node.getChildAt(2).getType(scope));

			node.setNames(node.getChildAt(0).getNames());
			node.addName(node.getChildAt(2).getName());

		}
	}

	private void deklaracijaParametara(TreeNode node) {
		imeTipa(node.getChildAt(0));
		if (error)
			return;
		if (node.getChildAt(0).getType(scope).equals("void")) {
			printErrorMessage(node);
			return;
		}
		if (node.getChildren().size() == 2) {
			node.setType(node.getChildAt(0).getType(scope));
		} else {
			node.setType("niz" + node.getChildAt(0).getType(scope));
		}
		node.setName(node.getChildAt(1).getName());
		// scope.addChild(node);
	}

	private void listaDeklaracija(TreeNode node) {
		if (node.getChildren().size() == 1) {
			if (node.isInLoop())
				node.getChildAt(0).setLoop();
			deklaracija(node.getChildAt(0));
			node.appendKod(node.getChildAt(0).getKod());
		} else {
			if (node.isInLoop()) {
				node.getChildAt(0).setLoop();
				node.getChildAt(1).setLoop();
			}
			listaDeklaracija(node.getChildAt(0));
			deklaracija(node.getChildAt(1));
			node.appendKod(node.getChildAt(0).getKod());
			node.appendKod(node.getChildAt(1).getKod());
		}
	}

	private void deklaracija(TreeNode node) {
		imeTipa(node.getChildAt(0));
		if (error)
			return;
		node.getChildAt(1).setType(node.getChildAt(0).getType(scope));
		// System.out.println(node.getChildAt(0).isConst());
		if (node.getChildAt(0).isConst())
			node.getChildAt(1).setConst();
		if (node.isInLoop())
			node.getChildAt(1).setLoop();
		listaInitDeklaratora(node.getChildAt(1));

		node.appendKod(node.getChildAt(1).getKod());
	}

	private void listaInitDeklaratora(TreeNode node) {
		if (node.getChildren().size() == 1) {
			node.getChildAt(0).setType(node.getType(scope));
			if (node.isConst())
				node.getChildAt(0).setConst();
			if (node.isInLoop())
				node.getChildAt(0).setLoop();
			initDeklarator(node.getChildAt(0));
			if (error)
				return;

			node.appendKod(node.getChildAt(0).getKod());
		} else {
			node.getChildAt(0).setType(node.getType(scope));
			if (node.isInLoop())
				node.getChildAt(0).setLoop();
			if (node.isConst())
				node.getChildAt(0).setConst();
			listaInitDeklaratora(node.getChildAt(0));
			if (error)
				return;

			node.appendKod(node.getChildAt(0).getKod());
			
			node.getChildAt(2).setType(node.getType(scope));
			if (node.isInLoop())
				node.getChildAt(2).setLoop();
			if (node.isConst())
				node.getChildAt(2).setConst();
			initDeklarator(node.getChildAt(2));
			if (error)
				return;
			
			node.appendKod(node.getChildAt(2).getKod());
			
		}
	}

	private void initDeklarator(TreeNode node) {
		node.getChildAt(0).setType(node.getType(scope));
		if (node.isConst())
			node.getChildAt(0).setConst();
		if (node.isInLoop())
			node.getChildAt(0).setLoop();
		izravniDeklarator(node.getChildAt(0));
		if (error) {
			return;
		}
		if (node.getChildren().size() == 1) {
			if (node.getChildAt(0).isConst()) {
				printErrorMessage(node);
				return;
			}
			node.appendKod(node.getChildAt(0).getKod());
			if (node.getChildAt(0).isArray()) {
				String labela;
				for (int i = 0; i < node.getChildAt(0).getArraySize(); ++i) {
					labela = "L" + labelCounter++;
					if (i == 0) node.setLabela(labela);
					LabelTableNode ltnode = new LabelTableNode(labela, null);
					ltnode.setEmpty(true);
					labelTable.add(ltnode);
//					System.out.println(i);
				}
				scope.getChildAt(scope.getDeclaredStuff().size() - 1).setLabela(node.getLabela());
			}
		} else {
			inicijalizator(node.getChildAt(2));
			if (error) 
				return;
			if (node.getChildAt(0).isArray()) {
				if (node.getChildAt(0).getArraySize() < node.getChildAt(2).getArraySize())
					error = true;
				for (String type1 : node.getChildAt(2).getTypes(scope)) {
					// System.out.println(type1);
					if (!isCastable(type1, node.getChildAt(0).getType(scope).substring(3)))
						error = true;
				}
				if (error) {
					printErrorMessage(node);
					return;
				}
			
				node.setLabela(node.getChildAt(2).getLabela());
				scope.getChildAt(scope.getDeclaredStuff().size() - 1).setLabela(node.getLabela());
				//System.out.println(";" + node.getChildAt(2).getLabela());
				//node.appendKod(node.getChildAt(2).getKod());
				
			} else {
				if (!isCastable(node.getChildAt(2).getType(scope), node.getChildAt(0).getType(scope))
						|| node.getChildAt(2).isFunction()) {
					printErrorMessage(node);
					return;
				}
			
				node.appendKod(node.getChildAt(2).getKod());
				String labela = node.getChildAt(0).getLabela();

				node.appendKod("\tLOAD R0, (R7)\n");
				node.appendKod("\tSTORE R0, (" + labela + ")\n");
				node.appendKod("\tADD R7, 4, R7\n");
				LabelTableNode ltNode = new LabelTableNode(labela, node);
				ltNode.setEmpty(true);
				labelTable.add(ltNode);

				node.setLabela(labela);
			
			}
		}
	}

	private void izravniDeklarator(TreeNode node) {
		if (node.getChildren().size() == 1) {
			if (node.getType(scope).equals("void") || isDeclaredLocally(node.getChildAt(0).getName())) {
				printErrorMessage(node);
				return;
			}
			node.setDefined(false);
			node.setName(node.getChildAt(0).getName());
			String label = "L" + labelCounter++;
			node.setLabela(label);
			scope.addChild(node);
			return;
		}
		if (node.getChildAt(2).getContent().startsWith("BROJ")) {
			if (node.getType(scope).equals("void") || isDeclaredLocally(node.getName())) {
				printErrorMessage(node);
				return;
			}
			if (node.getChildAt(2).getNumberValue() <= 0 || node.getChildAt(2).getNumberValue() > 1024) {
				printErrorMessage(node);
				return;
			}
			node.setType("niz" + node.getType(scope));
			node.setArraySize(node.getChildAt(2).getNumberValue());
			node.setName(node.getChildAt(0).getName());
			scope.addChild(node);
		}
		if (node.getChildAt(2).getContent().startsWith("KR_VOID")) {
			TreeNode localDef = getLocalDeclaration(node.getChildAt(0).getName());
			if (localDef == null) {
				node.addType("void");
				node.setName(node.getChildAt(0).getName());
				scope.addChild(node);
				declaredFunctions.add(node.getName());
			} else {
				if (localDef.getTypes(scope).size() != 1 || !localDef.getTypeAt(0).equals("void")) {
					printErrorMessage(node);
					return;
				}
				node.setName(node.getChildAt(0).getName());
				node.addType("void");
			}
		}
		if (node.getChildAt(2).getContent().equals("<lista_parametara>")) {
			listaParametara(node.getChildAt(2));
			if (error)
				return;
			TreeNode localDec = getLocalDeclaration(node.getChildAt(0).getName());
			if (localDec != null) {
				if (!checkTypes(localDec, node.getChildAt(2))) {
					printErrorMessage(node);
					return;
				}
				node.setTypes(node.getChildAt(2).getTypes(scope));
				node.setName(node.getChildAt(0).getName());
			} else {
				node.setName(node.getChildAt(0).getName());
				node.setTypes(node.getChildAt(2).getTypes(scope));
				declaredFunctions.add(node.getName());
				scope.addChild(node);
			}
		}
	}

	private void inicijalizator(TreeNode node) {
		if (node.getChildren().size() == 1) {
			izrazPridruzivanja(node.getChildAt(0));
			if (error)
				return;
			if (ideUNizZnakova(node.getChildAt(0))) {
				node.setArraySize(izracunajDuljinuZnakova(node));
				for (int i = 0; i < node.getArraySize(); ++i)
					node.addType("char");
			} else {
				node.setType(node.getChildAt(0).getType(scope));
				node.setTypes(node.getChildAt(0).getTypes(scope));
			}
			node.setName(node.getChildAt(0).getName());

			node.appendKod(node.getChildAt(0).getKod());
			node.setLabela(node.getChildAt(0).getLabela());
			
		} else {
			listaIzrazaPridruzivanja(node.getChildAt(1));
			if (error)
				return;
			node.setArraySize(node.getChildAt(1).getArraySize());
			node.setTypes(node.getChildAt(1).getTypes(scope));
			node.setName(node.getChildAt(1).getName());
	
			node.appendKod(node.getChildAt(1).getKod());
			node.setLabela(node.getChildAt(1).getLabela());
			
		}
	}

	private void listaIzrazaPridruzivanja(TreeNode node) {
		if (node.getChildren().size() == 1) {
			izrazPridruzivanja(node.getChildAt(0));
			if (error)
				return;
			node.addType(node.getChildAt(0).getType(scope));
			node.setArraySize(1);
			
			node.appendKod(node.getChildAt(0).getKod());
			node.setLabela(node.getChildAt(0).getLabela());
			
		} else {
			listaIzrazaPridruzivanja(node.getChildAt(0));
			if (error)
				return;
			izrazPridruzivanja(node.getChildAt(2));
			if (error)
				return;
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.addType(node.getChildAt(2).getType(scope));
			node.setArraySize(node.getChildAt(0).getArraySize() + 1);
		
			node.appendKod(node.getChildAt(0).getKod());
			node.appendKod(node.getChildAt(2).getKod());
			node.setLabela(node.getChildAt(0).getLabela());
			
		}
	}

	// ------------------------------------------------
	// KRAJ DEKLARACIJA I DEFINICIJA
	// ------------------------------------------------

	// Helper functions START HERE

	private int izracunajDuljinuZnakova(TreeNode node) {
		while (!node.getChildren().isEmpty()) {
			node = node.getChildAt(0);
		}
		return node.getContent().split(" ")[2].length() - 2;
	}

	private boolean ideUNizZnakova(TreeNode node) {
		while (!node.getChildren().isEmpty()) {
			if (node.getChildren().size() != 1)
				return false;
			node = node.getChildAt(0);
		}
		return node.getContent().startsWith("NIZ_ZNAKOVA");
	}

	private boolean checkTypes(TreeNode n1, TreeNode n2) {
		if (n1.getTypes(scope).size() != n2.getTypes(scope).size())
			return false;
		for (int i = 0; i < n1.getTypes(scope).size(); ++i) {
			if (!n1.getTypeAt(i).equals(n2.getTypeAt(i)))
				return false;
		}
		return true;
	}

	private TreeNode getLocalDeclaration(String name) {
		for (TreeNode declaration : scope.getDeclaredStuff()) {
			if (declaration.getName().equals(name))
				return declaration;
		}
		return null;
	}

	private boolean isDeclaredLocally(String name) {
		if (scope.getDeclaredStuff() == null)
			return false;
		for (TreeNode declaration : scope.getDeclaredStuff()) {
			if (declaration.getName().equals(name))
				return true;
		}
		return false;
	}

	/**
	 * Method that checks whether something of the name <code>name</code> has
	 * been declared in local or whatever scope, up to the global one.
	 * @param name
	 * @return
	 */
	private boolean isDeclaredYet(String name) {
		TableNode node = new TableNode();
		node = scope;
		while (node != null) {
			for (TreeNode declaration : node.getDeclaredStuff()) {
				if (declaration.getName().equals(name))
					return true;
			}
			node = node.getParent();
		}
		return false;
	}

	private TreeNode getDeclaredYet(String name) {
		TableNode node = new TableNode();
		node = scope;
		while (node != null) {
			for (TreeNode declaration : node.getDeclaredStuff()) {
				if (declaration.getName().equals(name))
					return declaration;
			}
			node = node.getParent();
		}
		return new TreeNode(0, "");
	}

	private boolean existsFunctionBefore(TableNode node, String funName, String funType) {

		// TableNode prevNode = node;
		// node = node.getParent();

		while (node != null) {
			for (TreeNode d : node.getDeclaredStuff()) {
				if (d.isFunction() && d.getFunctionName().equals(funName) && d.isFunctionDefined())
					return true;
			}
			// prevNode = node;
			node = node.getParent();
		}

		return false;
	}

	private boolean conflictingDeclaration(TableNode node, String funName, String funType) {

		while (node.getParent() != null)
			node = node.getParent();

		for (TreeNode d : node.getDeclaredStuff()) {
			if (d.isFunction() && d.getFunctionName().equals(funName) && !d.getType(scope).equals(funType))
				return true;
		}

		return false;

	}

	/**
	 * Checks whether type1 is implicitly castable into type2 (~)
	 * 
	 * @param type1 first type
	 * @param type2 second type
	 * @return :)
	 */
	private boolean isCastable(String type1, String type2) {
		return type1.equals(type2) || (type1.equals("char") && type2.equals("int"));
	}

	/**
	 * 
	 * @param node
	 */
	private void printErrorMessage(TreeNode node) {
		System.out.println(node.getContent() + " ::= " + node);
		error = true;
	}

	private boolean isInt(String x) {
		BigInteger i = new BigInteger(x);
		return i.bitCount() < 32;
	}

	private boolean isChar(String x) {
		return x.length() == 3 || (x.charAt(1) == '\\' && "tn0'\"\\".indexOf(x.charAt(2)) != -1);
	}

	private boolean isString(String x) {
		for (int i = 1; i < x.length() - 1; ++i) {
			if (x.charAt(i) == '\\') {
				String s = "'";
				s += x.charAt(i) + x.charAt(i + 1);
				s += "'";
				if (!isChar(s))
					return false;
			}
		}
		return x.charAt(x.length() - 2) != '\\';
	}

	private String getTypeOfCurrentFunction() {
		TableNode node = new TableNode();
		node = scope;
		while (node != null) {
			for (int i = node.getDeclaredStuff().size() - 1; i >= 0; --i) {
				TreeNode declaration = node.getDeclaredStuff().get(i);
				if (declaration.isFunction() && declaration.isFunctionDefined()) {
					return declaration.getType(node);
				}
			}
			node = node.getParent();
		}
		return "";
	}

	// private ArrayList<String> getTypesOfCurrentFunction() {
	// TableNode node = new TableNode();
	// node = scope;
	// while (node != null) {
	// for (int i = node.getDeclaredStuff().size() - 1; i >= 0; --i) {
	// TreeNode declaration = node.getDeclaredStuff().get(i);
	// if (declaration.isFunction() && declaration.isFunctionDefined()) {
	// return declaration.getTypes(scope);
	// }
	// }
	// node = node.getParent();
	// }
	// return new ArrayList<String>();
	// }

	public boolean gotError() {
		return error;
	}

	public boolean noMain() {
		return !hasMain;
	}

	public boolean functionError() {
		// System.out.println("defined: ");
		// for (String def : definedFunctions) System.out.println(def);
		// System.out.println("declared");
		// for (String dec : declaredFunctions) System.out.println(dec);
		return !definedFunctions.containsAll(declaredFunctions) && false;

	}

	// Helper functions END HERE

}

package org.reprap.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;

/**

/**
 * @author adrian
 * 
 * This is a program to automatically generate the Java for dealing with
 * the simplification of CSG expressions.  That is to say that it generates
 * simplified expressions when two operands in a more complicated expression
 * are equal, or are complements.
 *
 */

/**
 * Boolean operators and similar
 * 
 */
enum Bop 
{
	ZERO("zero"),
	ONE("one"),
	LEAF("leaf"),
	NOT("not"),
	LEFT("left"),
	RIGHT("right"),
	AND("and"),
	OR("or"),
	XOR("xor");
	
    private String name;
    
    Bop(String name)
    {
        this.name = name;
    }
    
    public String toString() { return name; }
    
    /**
     * All above NOT are diadic; all including and below monadic
     * @return
     */
    public boolean diadic() { return compareTo(NOT) > 0; }
}

/**
 * A single boolean variable with a name
 * @author ensab
 *
 */
class Variable implements Comparator<Variable>
{
	boolean bv;
	boolean init;
	String n;
	
	public Variable(String s) { init = false; n = s;}
	public boolean value() { if(!init) Debug.e("Variable undefined!"); return bv; }
	public boolean isSet() { return init; }
	public void set(boolean b) { bv = b; init = true;}
	public String name() { return n; }
	public void clean() { init = false; }
	
	public Variable(Variable v)
	{
		if(!v.init) 
			Debug.e("Variable(Variable v): input Variable undefined!");
		bv = v.bv;
		init = v.init;
		n = new String(v.n);
	}
	
	public static boolean same(Variable a, Variable b)
	{
		return(a.compare(a, b) == 0);
	}
		
	/**
	 * Compare means compare the lexical order of the names.
	 */
	public final int compare(Variable a, Variable b)
	{
		return(a.n.compareTo(b.n));
	}
	
}



/**
 * @author adrian
 *
 */
class BooleanExpression
{	
	/**
	 * 
	 */
	private BooleanExpression c1, c2;

	/**
	 * 
	 */
	private Bop leafOp;
	
	/**
	 * 
	 */
	private Variable leaf;
	
	/**
	 * 
	 */
	private Variable[] variables;
	
	/**
	 * 
	 */
	private int leafCount;
	
	
	/**
	 * Make an expression from three or four atomic expressions in an array.
	 * exp decides the expression.
	 * @param variables
	 * @param exp
	 */
	private void makeFromSeveral(BooleanExpression[] be, int exp)
	{
		BooleanExpression t1;
		
		switch(be.length)
		{
		// Bits in exp:  ba
		// Expression = v[0] b (v[1] a v[2])
		// a, b == 0 -> OR
		// a, b == 1 -> AND
		case 3:
			leafCount = -1;
			c1 = be[0];
			if((exp & 1) == 1)
				c2 = new BooleanExpression(be[1], be[2], Bop.AND);
			else
				c2 = new BooleanExpression(be[1], be[2], Bop.OR);
			if((exp & 2) == 2)
				leafOp = Bop.AND;
			else
				leafOp = Bop.OR;
			recordVariables();
			break;
		
		// Bits in exp:  dcba
		// d == 0 -> Expression = v[0] c (v[1] b (v[2] a v[3]))
		// d == 1 -> Expression = (v[0] b v[1]) c (v[2] a v[3])	
		// a, b, c == 0 -> OR
		// a, b, c == 1 -> AND	
		case 4:
			leafCount = -1;

			if((exp & 8) == 8)
			{
				if((exp & 1) == 1)
					c2 = new BooleanExpression(be[2], be[3], Bop.AND);
				else
					c2 = new BooleanExpression(be[2], be[3], Bop.OR);
				if((exp & 2) == 2)
					c1 = new BooleanExpression(be[0], be[1], Bop.AND);
				else
					c1 = new BooleanExpression(be[0], be[1], Bop.OR);
			} else
			{
				c1 = be[0];
				if((exp & 1) == 1)
					t1 = new BooleanExpression(be[2], be[3], Bop.AND);
				else
					t1 = new BooleanExpression(be[2], be[3], Bop.OR);
				if((exp & 2) == 2)
					c2 = new BooleanExpression(be[1], t1, Bop.AND);
				else
					c2 = new BooleanExpression(be[1], t1, Bop.OR);
				
			}
			if((exp & 4) == 4)
				leafOp = Bop.AND;
			else
				leafOp = Bop.OR;
			recordVariables();
			break;			
			
		default:
			Debug.e("BooleanExpression(...): variable number not 3 or 4!");	
		}		
	}
	
	/**
	 * Make an expression from three or four atomic expressions in an array.
	 * exp decides the expression.
	 * @param variables
	 * @param exp
	 */
	public BooleanExpression(BooleanExpression[] be, int exp)
	{
		makeFromSeveral(be, exp);
	}
	
	/**
	 * Make an expression from three or four variables in an array.
	 * exp decides the expression.
	 * @param variables
	 * @param exp
	 */
	public BooleanExpression(Variable[] v, int exp)
	{
		BooleanExpression[] be = new BooleanExpression[v.length];
		for(int i = 0; i < v.length; i++)
			be[i] = new BooleanExpression(v[i]);
		makeFromSeveral(be, exp);
	}

	/**
	 * Operand and two operators
	 * @param a
	 * @param b
	 * @param op
	 */
	public BooleanExpression(BooleanExpression a, BooleanExpression b, Bop op)
	{
		leafCount = -1;		
		if(!op.diadic())
			Debug.e("BooleanExpression(a, b): leaf operator or NOT!");
		
		leafOp = op;
		leaf = null;
		c1 = a;
		c2 = b;
		recordVariables();
	}
	
	/**
	 * Monadic operator
	 * @param a
	 * @param op
	 */
	public BooleanExpression(BooleanExpression a, Bop op)
	{
		leafCount = -1;		
		if(op != Bop.NOT)
			Debug.e("BooleanExpression(..., NOT): op not NOT!");
		
		leafOp = op;
		leaf = null;
		c1 = a;
		c2 = null;
		recordVariables();
	}
	
	/**
	 * Variable leaf
	 */
	public BooleanExpression(Variable v)
	{
		leafCount = -1;
		c1 = null;
		c2 = null;
		leafOp = Bop.LEAF;
		leaf = v;
		recordVariables();
	}
	
	/**
	 * @return
	 */
	public int leafCount()
	{
		if(leafCount < 0)
		{
			if(leafOp == Bop.LEAF) // || leafOp == bop.ZERO || leafOp == bop.ONE)
			{
				leafCount = 1;
			}
			else if(leafOp == Bop.NOT)
			{
				leafCount = c1.leafCount();
			} else
				leafCount = c1.leafCount()+c2.leafCount();
		}

		return leafCount;		
	}
		
	
	private void recordVariables()
	{
		int vc = leafCount();
		variables = new Variable[vc];
		int i = 0;
		int k;
		if(leafOp == Bop.LEAF) // || leafOp == bop.ZERO || leafOp == bop.ONE)
			variables[i++] = leaf;
		else if(leafOp == Bop.NOT)
		{
			for(k = 0; k < c1.variables.length; k++)
				variables[i++] = c1.variables[k];			
		} else
		{
			for(k = 0; k < c1.variables.length; k++)
				variables[i++] = c1.variables[k];
			for(k = 0; k < c2.variables.length; k++)
				variables[i++] = c2.variables[k];
		}		
	}
	
	public void setAll(int i)
	{
		TableRow.setAll(variables, i);
	}
	
	public Variable [] getVariables()
	{
		return variables;
	}
	
	public int getIndex(Variable v)
	{
		for(int i = 0; i < variables.length; i++)
		{
			if(v == variables[i])
				return i;
		}
		Debug.e("getIndex(): variable not found!");
		return -1;
	}
		
	/**
	 * @param v
	 * @return
	 */
	public boolean value()
	{
		
		boolean r;
		
		switch(leafOp)
		{
		case LEAF:
			return leaf.value();
		
		case ZERO:
			return false;
			
		case ONE:
			return true;
			
		case NOT:
			return !c1.value();
			
		case LEFT:
			return c1.value();
			
		case RIGHT:
			return c2.value();
			
		case AND:
			r = c1.value();
			return r & c2.value(); // &&
			
		case OR:
			r = c1.value(); 
			return r | c2.value(); // ||
			
		case XOR:
			r = c1.value(); 
			return r ^ c2.value();
			
		default:
			Debug.e("generateValue_r: dud operator!");
		}
		return false;
	}

	
	private String toJava_r(String r)
	{		
		switch(leafOp)
		{
		case LEAF:
			return r + leaf.name();
		
		case ZERO:
			return r + "RrCSG.nothing()";
			
		case ONE:
			return r + "RrCSG.universe()";
			
		case NOT:
			return c1.toJava_r(r) + ".complement()";
			
		case LEFT:
			return c1.toJava_r(r);
			
		case RIGHT:
			return c2.toJava_r(r);
			
		case AND:
			r += "RrCSG.intersection(";
			r = c1.toJava_r(r) + ", ";
			r = c2.toJava_r(r) + ")";
			return r;
			
		case OR:
			r += "RrCSG.union(";
			r = c1.toJava_r(r) + ", ";
			r = c2.toJava_r(r) + ")";
			return r;
			
		case XOR:
			Debug.e("toJava(): got to an XOR...");
			break;
			
		default:
			Debug.e("toJava(): dud operator");
		}
		
		return r;
	}
	
	public String toJava()
	{
		String r = "r = ";
		return toJava_r(r) + ";";
	}
}





/**
 * A row of variables in a function table, and the table value.
 * Also contains useful functions for variable arrays.
 * @author ensab
 *
 */

class TableRow implements Comparator<TableRow>
{
	private Variable[] vs;
	private boolean b;
	
	public TableRow() { vs = null; }
	
	public TableRow(Variable[] vin, boolean bin)
	{
		vs = sort(vin);
		b = bin;
	}
	
	public int length() { return vs.length; }
	public boolean value() { return b; } 
	public Variable get(int i) { return vs[i]; }
	public Variable[] all() { return vs; }
	
	public String toString()
	{
		String result = "";
		for(int i = 0; i < vs.length; i++)
			result += vs[i].name() + " ";
		return result;
	}
	
	/**
	 * Set all the variables in a list according to the corresponding
	 * bits in an integer.
	 * @param vs
	 * @param v
	 */
	public static void setAll(Variable[] vars, int v)
	{
		int k = 1;
		for(int i = 0; i < vars.length; i++)
		{
			if((v & k) == 0)
				vars[i].set(false);
			else
				vars[i].set(true);
			k *= 2;	
		}
	}
	
	/**
	 * Remove one variable from a list to make a shorter list
	 * @param vars
	 * @param remove
	 * @return
	 */
	public static Variable[] eliminateVariable(Variable[] vars, Variable remove)
	{
		Variable[] result = new Variable[vars.length - 1];
		int k = 0;
		
		for(int i = 0; i < vars.length; i++)
		{
			if(vars[i] != remove)
			{
				result[k] = new Variable(vars[i]);
				k++;
			}
		}
		
		return result;
	}
	
	/**
	 * Take a list of variables and return a copy lexically sorted by name
	 * @param v
	 * @return
	 */
	private static Variable[] sort(Variable[] vins)
	{
		Variable[] result = new Variable[vins.length];
		for(int i = 0; i < vins.length; i++)
			result[i] = new Variable(vins[i]);
		java.util.Arrays.sort(result, new Variable(""));
		return result;
	}
	
	/**
	 * Check if two lists of variables have the same variables in the same order
	 * @param a
	 * @param b
	 */
	public static boolean sameOrder(Variable [] a, Variable [] b)
	{
		if(a.length != b.length)
			return false;

		for(int i = 0; i < a.length; i++)
		{
			if(!Variable.same(a[i], b[i]))
				return false;
		}
		
		return true;
	}
	
	/**
	 * Find the binary number represented by the list
	 * @param a
	 * @return
	 */
	public int number()
	{
		int result = 0;
		
		for(int i = length() - 1; i >= 0; i--)
		{
			if(get(i).value())
				result |= 1;
			result = result << 1;
		}
		
		return result;
	}
	
	/**
	 * Compare the binary numbers represented by two lists
	 * @param a
	 * @param b
	 */
	public final int compare(TableRow a,TableRow b)
	{
		int va = a.number();
		int vb = b.number();
		
		if(va < vb)
			return -1;
		else if(va > vb)
			return 1;
		
		return 0;
	}
}

/**
 * @author adrian
 *
 */
class FunctionTable
{		
	/**
	 * 
	 */
	List<TableRow> rows;
	
	/**
	 * 
	 */
	boolean allFalse, allTrue;
	
	/**
	 *
	 */
	public FunctionTable()
	{
		rows = new ArrayList<TableRow>();
		allFalse = true;
		allTrue = true;
	}
	
	/**
	 * Add a new row to the function table
	 * @param v
	 * @param b
	 */
	public void addRow(Variable[] v, boolean b)
	{
		if(b)
			allFalse = false;
		else
			allTrue = false;

		TableRow newOne = new TableRow(v, b);
		
//		 Check that each has the same variables as the first
		
		if(rows.size() > 0)
		{
			if(!TableRow.sameOrder(newOne.all(), rows.get(0).all()))
				Debug.e("FunctionTable.addRow() - variable lists different!");
		}
		
		rows.add(newOne);
	}
	
	public void tableCheck()
	{
		// Check we have the right number of entries
		
		int vars = rows.get(0).all().length;
		int leng = 1;
		for(int j = 0; j < vars; j++)
			leng *= 2;
		
		if(leng != rows.size())
			Debug.e("FunctionTable.tableCheck() - incorrect entry count: " + rows.size() +
					"(should be " + leng + ")");
		Collections.sort(rows, new TableRow());
		for(int i = 1; i < rows.size(); i++)
			if(rows.get(i-1).number() == rows.get(i).number())
				Debug.e("FunctionTable.tableDone() - identical rows: " + rows.get(i-1).toString() +
						rows.get(i).toString());
	}
		
	/**
	 * @param b
	 */
	public FunctionTable(BooleanExpression b)
	{
		this();
		
		int i;
		int inputs = b.leafCount();
		
		int entries = 1;
		for(i = 0; i < inputs; i++)
			entries *= 2;

		for(i = 0; i < entries; i++)
		{
			b.setAll(i);
			addRow(b.getVariables(), b.value());
		}
		
		tableCheck();
	}	

	/**
	 * @param b
	 * @param a
	 * @param equal_a
	 */
	public FunctionTable(BooleanExpression b, Variable v, Variable equal_v, boolean opposite)
	{
		this();
		
		int i;
		int inputs = b.leafCount() - 1;
		
		int entries = 1;
		for(i = 0; i < inputs; i++)
			entries *= 2;

		for(i = 0; i < entries*2; i++)
		{
			b.setAll(i);
			if(opposite ^ (equal_v.value() == v.value()))
				addRow(TableRow.eliminateVariable(b.getVariables(), equal_v), b.value());
		}
		
		tableCheck();		
	}
	
	public boolean allOnes() { return allTrue;}
	
	public boolean allZeros() { return allFalse;}
	
	public int entries() { return rows.size(); }
	
	/**
	 * @param a
	 * @param b
	 * @return
	 */
	static boolean same(FunctionTable a, FunctionTable b)
	{
		if(!TableRow.sameOrder(a.rows.get(0).all(), b.rows.get(0).all()))
			return false;
		
		if(a.entries() != b.entries())
			return false;
		if(a.allFalse && b.allFalse)
			return true;
		if(a.allTrue && b.allTrue)
			return true;		
		for(int i = 0; i < a.entries(); i++)
			if(a.rows.get(i).value() != b.rows.get(i).value())
				return false;
		return true;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		String result = "\n\t// " + rows.get(0).toString();
		for(int i = 0; i < entries(); i++)
		{
			TableRow tr = rows.get(i);
			Variable[] vs = tr.all();
			result += "\n\t// ";
			for(int j = 0; j < vs.length; j++)
			{
				if(vs[j].value())
					result += "1 ";
				else
					result += "0 ";
			}
			
			result += "| ";
			if(tr.value())
				result += "1 ";
			else
				result += "0 ";
		}
		return result;
	}
}

/**
 * @author adrian
 *
 */
public class CodeGenerator 
{
	static Variable[] eliminate(Variable[] v, int k)
	{
		int len = v.length;
		Variable[] result = new Variable[len - 1];
		int count = 0;
		for(int i = 0; i < len; i++)
			if(i != k)
				result[count++] = v[i];
		return result;
	}
	
	static List<BooleanExpression> generateAllPairs(BooleanExpression[] b2)
	{
		if(b2.length != 2)
			Debug.e("generateAllPairs: array not of length 2: " + b2.length);
		
		List<BooleanExpression> bel2 = new ArrayList<BooleanExpression>();
		
		Bop[] bopValues = Bop.values();
		for(int i = 0; i < bopValues.length; i++)
		{
			if(bopValues[i].diadic())
			{
				BooleanExpression be = new BooleanExpression(b2[0], b2[1], bopValues[i]);
				bel2.add(be);
				BooleanExpression bf = new BooleanExpression(be, Bop.NOT);
				bel2.add(bf);
				BooleanExpression bg = new BooleanExpression(new BooleanExpression(b2[0], Bop.NOT), b2[1], bopValues[i]);
				bel2.add(bg);				
				BooleanExpression bh = new BooleanExpression(b2[0], new BooleanExpression(b2[1], Bop.NOT), bopValues[i]);
				bel2.add(bh);
				BooleanExpression bi = new BooleanExpression(new BooleanExpression(b2[0], Bop.NOT), new BooleanExpression(b2[1], Bop.NOT), bopValues[i]);
				bel2.add(bi);				
			}
		}
		return bel2;
	}	
	
	static List<BooleanExpression> generateAllTripples(BooleanExpression[] b3)
	{
		BooleanExpression[] b2a = new BooleanExpression[2];
		BooleanExpression[] b2b = new BooleanExpression[2];
		List<BooleanExpression> bel3 = new ArrayList<BooleanExpression>();
		List<BooleanExpression> bel2a, bel2b;
		int i, j;
		
		b2b[0] = b3[0];
		b2a[0] = b3[1];
		b2a[1] = b3[2];
		bel2a = generateAllPairs(b2a);
		for(i = 0; i < bel2a.size(); i++)
		{
			b2b[1] = bel2a.get(i);
			bel2b = generateAllPairs(b2b);
			for(j = 0; j < bel2b.size(); j++)
				bel3.add(bel2b.get(i));
		}
		
		b2b[0] = b3[1];
		b2a[0] = b3[0];
		b2a[1] = b3[2];
		bel2a = generateAllPairs(b2a);
		for(i = 0; i < bel2a.size(); i++)
		{
			b2b[1] = bel2a.get(i);
			bel2b = generateAllPairs(b2b);
			for(j = 0; j < bel2b.size(); j++)
				bel3.add(bel2b.get(i));
		}
		
		b2b[0] = b3[2];
		b2a[0] = b3[0];
		b2a[1] = b3[1];
		bel2a = generateAllPairs(b2a);
		for(i = 0; i < bel2a.size(); i++)
		{
			b2b[1] = bel2a.get(i);
			bel2b = generateAllPairs(b2b);
			for(j = 0; j < bel2b.size(); j++)
				bel3.add(bel2b.get(i));
		}
		
		return bel3;
	}
	
	static BooleanExpression findEqualTwo(FunctionTable f, Variable[] v)
	{
		if(v.length != 2)
			Debug.e("findEqualTwo: array not of length 2: " + v.length);
		BooleanExpression[] b2 = new BooleanExpression[2];
		b2[0] = new BooleanExpression(v[0]);
		b2[1] = new BooleanExpression(v[1]);
		List<BooleanExpression> bel = generateAllPairs(b2);
		BooleanExpression be;
		FunctionTable g;
		for(int i = 0; i < bel.size(); i++)
		{
			be = bel.get(i);
			g = new FunctionTable(be);
			if(FunctionTable.same(f, g))
				return be;
		}
		return null;
	}
	
	
	static BooleanExpression findEqualThree(FunctionTable f, Variable[] v)
	{
		if(v.length != 3)
			Debug.e("findEqualThree: array not of length 3: " + v.length);
		BooleanExpression[] b3 = new BooleanExpression[3];
		b3[0] = new BooleanExpression(v[0]);
		b3[1] = new BooleanExpression(v[1]);
		b3[2] = new BooleanExpression(v[2]);
		List<BooleanExpression> bel = generateAllTripples(b3);
		BooleanExpression be;
		FunctionTable g;
		for(int i = 0; i < bel.size(); i++)
		{
			be = bel.get(i);
			g = new FunctionTable(be);
			if(FunctionTable.same(f, g))
				return be;
		}
		return null;
	}
	
//	private static void oneCase3(Variable [] variables, int exp, int j, int k, boolean opposite, boolean fts)
//	{
//		BooleanExpression a = new BooleanExpression(variables, exp);
//		
////		FunctionTable tt = new FunctionTable(a);
////		System.out.println(tt.toString()+"\n\n");
//		
//		FunctionTable f = new FunctionTable(a, variables[j], variables[k], opposite);
//		
//		//BooleanExpression g = findEqualTwo(f, variables[j], variables[3-(j+k)]);
//		BooleanExpression g = findEqualTwo(f, eliminate(variables, k));
//				
//		int caseVal = 0;
//		if(opposite)
//			caseVal |= 1;
//		if(j == 1)
//			caseVal |= 2;
//		if(k == 2)
//			caseVal |= 4;
//		caseVal |= exp << 3;
//
//		System.out.println("\tcase " + caseVal + ": ");
//		if(fts)
//		{
//			System.out.println("\t// " + a.toJava());
//			System.out.print("\t// " + variables[j].name() + " = ");
//			if(opposite)
//				System.out.print("!");	
//			System.out.println(variables[k].name() + " ->");
//			System.out.println(f.toString());
//		}
//
//		if(g != null || f.allOnes() || f.allZeros())
//		{
//			if(f.allOnes())
//				System.out.println("\t\tr = RrCSG.universe();");
//			else if(f.allZeros())
//				System.out.println("\t\tr = RrCSG.nothing();");
//			else
//				System.out.println("\t\t" + g.toJava());
////			if(fts)
////			{
////				FunctionTable h = new FunctionTable(g);
////				System.out.println(h.toString());
////			}
//		} else
//			System.out.println("\t\t// No equivalence." + "\n");
//		System.out.println("\t\tbreak;\n");
//	}
	
	private static void oneCase4(Variable [] variables, int exp, int j, int k, boolean opposite, boolean fts)
	{
		BooleanExpression a = new BooleanExpression(variables, exp);
		
//		FunctionTable tt = new FunctionTable(a);
//		System.out.println(tt.toString()+"\n\n");
		
		FunctionTable f = new FunctionTable(a, variables[j], variables[k], opposite);
		
		BooleanExpression g = findEqualThree(f, eliminate(variables, k));
		
		int caseVal = 0;
		if(opposite)
			caseVal |= 1;
		switch(j)
		{
		case 0:
			if(k == 2)
				caseVal |= 2;
			else if(k == 3)
				caseVal |= 4;
			break;
			
		case 1:
			if(k == 2)
				caseVal |= 6;
			else if(k == 3)
				caseVal |= 8;
			break;
			
		case 2:
			if(k == 3)
				caseVal |= 10;
			break;
			
		default:
			
		}
		

		caseVal |= exp << 4;

		System.out.println("\tcase " + caseVal + ": ");
		if(fts)
		{
			System.out.println("\t// " + a.toJava());
			System.out.print("\t// " + variables[j].name() + " = ");
			if(opposite)
				System.out.print("!");	
			System.out.println(variables[k].name() + " ->");
			System.out.println(f.toString());
		}

		if(g != null || f.allOnes() || f.allZeros())
		{
			if(f.allOnes())
				System.out.println("\t\tr = RrCSG.universe();");
			else if(f.allZeros())
				System.out.println("\t\tr = RrCSG.nothing();");
			else
				System.out.println("\t\t" + g.toJava());
//			if(fts)
//			{
//				FunctionTable h = new FunctionTable(g);
//				System.out.println(h.toString());
//			}
		} else
			System.out.println("\t\t// No equivalence." + "\n");
		System.out.println("\t\tbreak;\n");
	}
	
//	private static void allCases3(Variable [] variables)
//	{	
//		for(int exp = 0; exp < 4; exp++)
//		{
//			for(int j = 0; j < 2; j++)
//				for(int k = j+1; k < 3; k++)
//				{
//					oneCase3(variables, exp, j, k, false, true);
//					oneCase3(variables, exp, j, k, true, true);
//				}
//		}		
//	}
//	
	private static void allCases4(Variable [] variables)
	{	
		for(int exp = 0; exp < 16; exp++)
		{
			for(int j = 0; j < 3; j++)
				for(int k = j+1; k < 4; k++)
				{
					oneCase4(variables, exp, j, k, false, true);
					oneCase4(variables, exp, j, k, true, true);
				}
		}		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		Variable [] variables = new Variable[4];
		variables[0] = new Variable("a");
		variables[1] = new Variable("b");
		variables[2] = new Variable("c");
		variables[3] = new Variable("d");
		
		//oneCase3(variables, 2, 0, 2, false, true);
		//allCases3(variables);
		allCases4(variables);
	}	

}


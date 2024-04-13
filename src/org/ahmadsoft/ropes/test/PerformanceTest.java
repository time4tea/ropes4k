/*
 *  PerformanceTest.java
 *  Copyright (C) 2007 Amin Ahmad. 
 *  
 *  This file is part of Java Ropes.
 *  
 *  Java Ropes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Java Ropes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with Java Ropes.  If not, see <http://www.gnu.org/licenses/>.
 *  	
 *  Amin Ahmad can be contacted at amin.ahmad@gmail.com or on the web at 
 *  www.ahmadsoft.org.
 */
package org.ahmadsoft.ropes.test;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javolution.text.Text;

import org.ahmadsoft.ropes.Rope;
import org.ahmadsoft.ropes.impl.AbstractRope;

/**
 * Performs an extensive performance test comparing Ropes, Strings, and
 * StringBuffers.
 * @author aahmad
 */
public class PerformanceTest {

	private static int seed=342342;
	private static Random random = new Random(PerformanceTest.seed);
	private static int lenCC = 182029;
	private static int lenBF = 467196;
	
	private static final int ITERATION_COUNT = 7;
	private static final int PLAN_LENGTH = 500;
	

	private static String complexString=null; 
	private static StringBuffer complexStringBuffer=null; 
	private static Rope complexRope=null;
	private static Text complexText=null;

	/**
	 * @param args
	 */
	public static void main(final String[] args) throws Exception {
		
		if (args.length == 1) {
			seed = Integer.parseInt(args[0]);
		}
		
		long x,y;

		x=System.nanoTime();
		final char[] aChristmasCarol_RAW = PerformanceTest.readCC();
		final char[] bensAuto_RAW        = PerformanceTest.readBF();
		final String aChristmasCarol = new String(aChristmasCarol_RAW);
		final String bensAuto        = new String(bensAuto_RAW);
		y=System.nanoTime();
		System.out.println("Read " + aChristmasCarol.length() + " bytes in " + PerformanceTest.time(x,y));

		System.out.println();
		System.out.println("**** DELETE PLAN TEST ****");
		System.out.println();

		int newSize = PerformanceTest.lenCC;
		final int[][] deletePlan=new int[PLAN_LENGTH][2];
		for (int j=0;j<deletePlan.length;++j) {
			deletePlan[j][0] = PerformanceTest.random.nextInt(newSize);
			deletePlan[j][1] = PerformanceTest.random.nextInt(Math.min(100, newSize - deletePlan[j][0]));
			newSize -= deletePlan[j][1];
		}

		for (int k=20; k<=deletePlan.length; k+=20) {
		System.out.println("Delete plan length: " + k);
		{
		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT];
		for (int j=0;j<ITERATION_COUNT;++j){
		stats0[j] = PerformanceTest.stringDeleteTest(aChristmasCarol, deletePlan);
		stats1[j] = PerformanceTest.stringBufferDeleteTest(aChristmasCarol, deletePlan);
		stats2[j] = PerformanceTest.ropeDeleteTest(aChristmasCarol, deletePlan);
		}
		stat(System.out, stats0, "ns", "[String]");
		stat(System.out, stats1, "ns", "[StringBuffer]");
		stat(System.out, stats2, "ns", "[Rope]");
		}
		}

		System.out.println();
		System.out.println("**** PREPEND PLAN TEST ****");
		System.out.println();

		final int[][] prependPlan=new int[PLAN_LENGTH][2];
		for (int j=0;j<prependPlan.length;++j) {
			prependPlan[j][0] = PerformanceTest.random.nextInt(PerformanceTest.lenCC);
			prependPlan[j][1] = PerformanceTest.random.nextInt(PerformanceTest.lenCC - prependPlan[j][0]);
		}

		for (int k=20; k<=prependPlan.length; k+=20) {
		System.out.println("Prepend plan length: " + k);
		{
		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT], stats3 = new long[ITERATION_COUNT];
		for (int j=0;j<ITERATION_COUNT;++j){
		stats0[j] = PerformanceTest.stringPrependTest(aChristmasCarol, prependPlan, k);
		stats1[j] = PerformanceTest.stringBufferPrependTest(aChristmasCarol, prependPlan, k);
		stats2[j] = PerformanceTest.ropePrependTest(aChristmasCarol, prependPlan, k);
		stats3[j] = PerformanceTest.textPrependTest(aChristmasCarol, prependPlan, k);
		}
		stat(System.out, stats0, "ns", "[String]");
		stat(System.out, stats1, "ns", "[StringBuffer]");
		stat(System.out, stats2, "ns", "[Rope]");
		stat(System.out, stats3, "ns", "[Text]");
		}
		}

		System.out.println();
		System.out.println("**** APPEND PLAN TEST ****");
		System.out.println();

		final int[][] appendPlan=new int[PLAN_LENGTH][2];
		for (int j=0;j<appendPlan.length;++j) {
			appendPlan[j][0] = PerformanceTest.random.nextInt(PerformanceTest.lenCC);
			appendPlan[j][1] = PerformanceTest.random.nextInt(PerformanceTest.lenCC - appendPlan[j][0]);
		}


		for (int k=20; k<=appendPlan.length; k+=20) {
		System.out.println("Append plan length: " + k);
		{
		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT];
		for (int j=0;j<ITERATION_COUNT;++j){
		stats0[j] = PerformanceTest.stringAppendTest(aChristmasCarol, appendPlan, k);
		stats1[j] = PerformanceTest.stringBufferAppendTest(aChristmasCarol, appendPlan, k);
		stats2[j] = PerformanceTest.ropeAppendTest(aChristmasCarol, appendPlan, k);
		}
		stat(System.out, stats0, "ns", "[String]");
		stat(System.out, stats1, "ns", "[StringBuffer]");
		stat(System.out, stats2, "ns", "[Rope]");
		}
		}


		System.out.println();
		System.out.println("**** INSERT PLAN TEST ****");
		System.out.println("* Insert fragments of A Christmas Carol back into itself.\n");

		final int[][] insertPlan=new int[PLAN_LENGTH][3];
		for (int j=0;j<insertPlan.length;++j) {
			insertPlan[j][0] = PerformanceTest.random.nextInt(PerformanceTest.lenCC);                      //location to insert
			insertPlan[j][1] = PerformanceTest.random.nextInt(PerformanceTest.lenCC);                      //clip from
			insertPlan[j][2] = PerformanceTest.random.nextInt(PerformanceTest.lenCC - insertPlan[j][1]);   //clip length
		}



		for (int k=insertPlan.length; k<=insertPlan.length; k+=20) {
		System.out.println("Insert plan length: " + k);
		{
		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT], stats3 = new long[ITERATION_COUNT];
		for (int j=0;j<1;++j){
		stats0[j] = PerformanceTest.stringInsertTest(aChristmasCarol_RAW, insertPlan, k);
		stats1[j] = PerformanceTest.stringBufferInsertTest(aChristmasCarol_RAW, insertPlan, k);
		stats2[j] = PerformanceTest.ropeInsertTest(aChristmasCarol_RAW, insertPlan, k);
		stats3[j] = PerformanceTest.textInsertTest(aChristmasCarol_RAW, insertPlan, k);
		}
		stat(System.out, stats0, "ns", "[String]");
		stat(System.out, stats1, "ns", "[StringBuffer]");
		stat(System.out, stats2, "ns", "[Rope]");
		stat(System.out, stats3, "ns", "[Text]");
		}
		}

		System.out.println();
		System.out.println("**** INSERT PLAN TEST 2 ****");
		System.out.println("* Insert fragments of Benjamin Franklin's Autobiography into\n" +
				           "* A Christmas Carol.\n");

		final int[][] insertPlan2=new int[PLAN_LENGTH][3];
		for (int j=0;j<insertPlan2.length;++j) {
			insertPlan2[j][0] = PerformanceTest.random.nextInt(PerformanceTest.lenCC);                      //location to insert
			insertPlan2[j][1] = PerformanceTest.random.nextInt(PerformanceTest.lenBF);                      //clip from
			insertPlan2[j][2] = PerformanceTest.random.nextInt(PerformanceTest.lenBF - insertPlan2[j][1]);  //clip length
		}

		{
		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT];
		for (int j=0;j<ITERATION_COUNT;++j){
		stats0[j] = PerformanceTest.stringInsertTest2(aChristmasCarol, bensAuto, insertPlan2);
		stats1[j] = PerformanceTest.stringBufferInsertTest2(aChristmasCarol, bensAuto, insertPlan2);
		stats2[j] = PerformanceTest.ropeInsertTest2(aChristmasCarol, bensAuto, insertPlan2);
		}
		stat(System.out, stats0, "ns", "[String]");
		stat(System.out, stats1, "ns", "[StringBuffer]");
		stat(System.out, stats2, "ns", "[Rope]");
		}

		System.out.println();
		System.out.println("**** TRAVERSAL TEST 1 (SIMPLY-CONSTRUCTED DATASTRUCTURES) ****");
		System.out.println("* A traversal test wherein the datastructures are simply\n" +
				           "* constructed, meaning constructed straight from the data\n" +
				           "* file with no further modifications. In this case, we expect\n" +
				           "* rope performance to be competitive, with the charAt version\n" +
				           "* performing better than the iterator version.");
		System.out.println();

		{
		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT], stats3 = new long[ITERATION_COUNT];
		for (int j=0;j<ITERATION_COUNT;++j){
		stats0[j] = PerformanceTest.stringTraverseTest(aChristmasCarol_RAW);
		stats1[j] = PerformanceTest.stringBufferTraverseTest(aChristmasCarol_RAW);
		stats2[j] = PerformanceTest.ropeTraverseTest_1(aChristmasCarol_RAW);
		stats3[j] = PerformanceTest.ropeTraverseTest_2(aChristmasCarol_RAW);
		}
		stat(System.out, stats0, "ns", "[String]");
		stat(System.out, stats1, "ns", "[StringBuffer]");
		stat(System.out, stats2, "ns", "[Rope/charAt]");
		stat(System.out, stats3, "ns", "[Rope/itr]");
		}

		System.out.println();
		System.out.println("**** TRAVERSAL TEST 2 (COMPLEXLY-CONSTRUCTED DATASTRUCTURES) ****");
		System.out.println("* A traversal test wherein the datastructures are complexly\n" +
				           "* constructed, meaning constructed through hundreds of insertions,\n" +
				           "* substrings, and deletions (deletions not yet implemented). In\n" +
				           "* this case, we expect rope performance to suffer, with the\n" +
				           "* iterator version performing better than the charAt version.");
		System.out.println();

		{
		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT], stats3 = new long[ITERATION_COUNT], stats4 = new long[ITERATION_COUNT];
		for (int j=0;j<3;++j){
		stats0[j] = PerformanceTest.stringTraverseTest2(complexString);
		stats1[j] = PerformanceTest.stringBufferTraverseTest2(complexStringBuffer);
		stats2[j] = PerformanceTest.ropeTraverseTest2_1(complexRope);
		stats3[j] = PerformanceTest.ropeTraverseTest2_2(complexRope);
		stats4[j] = PerformanceTest.textTraverseTest2(complexText);
		}
		stat(System.out, stats0, "ns", "[String]");
		stat(System.out, stats1, "ns", "[StringBuffer]");
		stat(System.out, stats2, "ns", "[Rope/charAt]");
		stat(System.out, stats3, "ns", "[Rope/itr]");
		stat(System.out, stats4, "ns", "[Text/charAt]");
		}

		System.out.println();
		System.out.println("**** REGULAR EXPRESSION TEST (SIMPLY-CONSTRUCTED DATASTRUCTURES) ****");
		System.out.println("* Using a simply-constructed rope and the pattern 'Crachit'.");
		
		Pattern p1 = Pattern.compile("Cratchit");

		{
		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT], stats3 = new long[ITERATION_COUNT], stats4 = new long[ITERATION_COUNT];
		for (int j=0;j<ITERATION_COUNT;++j){
		stats0[j] = PerformanceTest.stringRegexpTest(aChristmasCarol_RAW, p1);
		stats1[j] = PerformanceTest.stringBufferRegexpTest(aChristmasCarol_RAW, p1);
		stats2[j] = PerformanceTest.ropeRegexpTest(aChristmasCarol_RAW, p1);
		stats3[j] = PerformanceTest.ropeMatcherRegexpTest(aChristmasCarol_RAW, p1);
		stats4[j] = PerformanceTest.textRegexpTest(aChristmasCarol_RAW, p1);
		}
		stat(System.out, stats0, "ns", "[String]");
		stat(System.out, stats1, "ns", "[StringBuffer]");
		stat(System.out, stats2, "ns", "[Rope]");
		stat(System.out, stats3, "ns", "[Rope.matcher]");
		stat(System.out, stats4, "ns", "[Text]");
		}

		System.out.println();
		System.out.println("**** REGULAR EXPRESSION TEST (SIMPLY-CONSTRUCTED DATASTRUCTURES) ****");
		System.out.println("* Using a simply-constructed rope and the pattern 'plea.*y'.");

		p1 = Pattern.compile("plea.*y");
		{
		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT], stats3 = new long[ITERATION_COUNT];
		for (int j=0;j<ITERATION_COUNT;++j){
		stats0[j] = PerformanceTest.stringRegexpTest(aChristmasCarol_RAW, p1);
		stats1[j] = PerformanceTest.stringBufferRegexpTest(aChristmasCarol_RAW, p1);
		stats2[j] = PerformanceTest.ropeRegexpTest(aChristmasCarol_RAW, p1);
		stats3[j] = PerformanceTest.ropeMatcherRegexpTest(aChristmasCarol_RAW, p1);
		}
		stat(System.out, stats0, "ns", "[String]");
		stat(System.out, stats1, "ns", "[StringBuffer]");
		stat(System.out, stats2, "ns", "[Rope]");
		stat(System.out, stats3, "ns", "[Rope.matcher]");
		}

		System.out.println();
		System.out.println("**** REGULAR EXPRESSION TEST (COMPLEXLY-CONSTRUCTED DATASTRUCTURES) ****");
		System.out.println("* Using a complexly-constructed rope and the pattern 'Crachit'.");

		p1 = Pattern.compile("Cratchit");
		{
		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT], stats3 = new long[ITERATION_COUNT], stats4 = new long[ITERATION_COUNT], stats5 = new long[ITERATION_COUNT];
		for (int j=0;j<ITERATION_COUNT;++j){
		stats0[j] = PerformanceTest.stringRegexpTest2(complexString, p1);
		stats1[j] = PerformanceTest.stringBufferRegexpTest2(complexStringBuffer, p1);
		stats2[j] = PerformanceTest.ropeRegexpTest2(complexRope, p1);
		stats3[j] = PerformanceTest.ropeRebalancedRegexpTest2(complexRope, p1);
		stats4[j] = PerformanceTest.ropeMatcherRegexpTest2(complexRope, p1);
		stats5[j] = PerformanceTest.textRegexpTest2(complexText, p1);
		}
		stat(System.out, stats0, "ns", "[String]");
		stat(System.out, stats1, "ns", "[StringBuffer]");
		stat(System.out, stats2, "ns", "[Rope]");
		stat(System.out, stats3, "ns", "[Reblncd Rope]");
		stat(System.out, stats4, "ns", "[Rope.matcher]");
		stat(System.out, stats5, "ns", "[Text]");
		}

		System.out.println();
		System.out.println("**** STRING SEARCH TEST ****");
		System.out.println("* Using a simply constructed rope and the pattern 'Bob was very\n" +
						   "* cheerful with them, and spoke pleasantly to'.");

		String toFind = "consumes faster than Labor wears; while the used key is always bright,";
		{
		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT];
		for (int j=0;j<ITERATION_COUNT;++j){
		stats0[j] = PerformanceTest.stringFindTest(bensAuto_RAW, toFind);
		stats1[j] = PerformanceTest.stringBufferFindTest(bensAuto_RAW, toFind);
		stats2[j] = PerformanceTest.ropeFindTest(bensAuto_RAW, toFind);
		}
		stat(System.out, stats0, "ns", "[String]");
		stat(System.out, stats1, "ns", "[StringBuffer]");
		stat(System.out, stats2, "ns", "[Rope]");
		}

		System.out.println();
		System.out.println("**** STRING SEARCH TEST (COMPLEXLY-CONSTRUCTED DATASTRUCTURES)****");
		System.out.println("* Using a complexly constructed rope and the pattern 'consumes faster\n" +
						   "* than Labor wears; while the used key is always bright,'.");

		toFind = "Bob was very cheerful with them, and spoke pleasantly to";
		{
		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT];
		for (int j=0;j<ITERATION_COUNT;++j){
		stats0[j] = PerformanceTest.stringFindTest2(complexString, toFind);
		stats1[j] = PerformanceTest.stringBufferFindTest2(complexStringBuffer, toFind);
		stats2[j] = PerformanceTest.ropeFindTest2(complexRope, toFind);
		}
		stat(System.out, stats0, "ns", "[String]");
		stat(System.out, stats1, "ns", "[StringBuffer]");
		stat(System.out, stats2, "ns", "[Rope]");
		}


		System.out.println();
		System.out.println("**** WRITE TEST ****");
		System.out.println("* Illustrates how to write a Rope to a stream efficiently.");

		{
		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT];
		for (int j=0;j<ITERATION_COUNT;++j){
		stats0[j] = PerformanceTest.ropeWriteBad(complexRope);
		stats1[j] = PerformanceTest.ropeWriteGood(complexRope);
		}
		stat(System.out, stats0, "ns", "[Out.write]");
		stat(System.out, stats1, "ns", "[Rope.write]");
		}
	}

	private static long stringFindTest(char[] aChristmasCarol, String toFind) {
		long x,y;

		String b = new String(aChristmasCarol);
		x = System.nanoTime();
		int loc = b.indexOf(toFind);
		y = System.nanoTime();
		System.out.printf("[String.find]       indexOf needle length %d found at index %d in % ,18d ns.\n", toFind.length(), loc, (y-x));
		return (y-x);
	}

	private static long stringBufferFindTest(char[] aChristmasCarol, String toFind) {
		long x,y;

		StringBuffer b = new StringBuffer(aChristmasCarol.length); b.append(aChristmasCarol);
		x = System.nanoTime();
		int loc = b.indexOf(toFind);
		y = System.nanoTime();
		System.out.printf("[StringBuffer.find] indexOf needle length %d found at index %d in % ,18d ns.\n", toFind.length(), loc, (y-x));
		return (y-x);
	}

	private static long ropeFindTest(char[] aChristmasCarol, String toFind) {
		long x,y;

		Rope b = Rope.BUILDER.build(aChristmasCarol);
		x = System.nanoTime();
		int loc = b.indexOf(toFind);
		y = System.nanoTime();
		System.out.printf("[Rope.find]         indexOf needle length %d found at index %d in % ,18d ns.\n", toFind.length(), loc, (y-x));
		return (y-x);
	}

	private static long stringFindTest2(String aChristmasCarol, String toFind) {
		long x,y;

		x = System.nanoTime();
		int loc = aChristmasCarol.indexOf(toFind);
		y = System.nanoTime();
		System.out.printf("[String.find]       indexOf needle length %d found at index %d in % ,18d ns.\n", toFind.length(), loc, (y-x));
		return (y-x);
	}

	private static long stringBufferFindTest2(StringBuffer aChristmasCarol, String toFind) {
		long x,y;

		x = System.nanoTime();
		int loc = aChristmasCarol.indexOf(toFind);
		y = System.nanoTime();
		System.out.printf("[StringBuffer.find] indexOf needle length %d found at index %d in % ,18d ns.\n", toFind.length(), loc, (y-x));
		return (y-x);
	}

	private static long ropeFindTest2(Rope aChristmasCarol, String toFind) {
		long x,y;

		x = System.nanoTime();
		int loc = aChristmasCarol.indexOf(toFind);
		y = System.nanoTime();
		System.out.printf("[Rope.find]         indexOf needle length %d found at index %d in % ,18d ns.\n", toFind.length(), loc, (y-x));
		return (y-x);
	}

	private static long ropeWriteGood(Rope complexRope) {
		long x,y;

		Writer out = new StringWriter(complexRope.length());
		x = System.nanoTime();
		try {
			complexRope.write(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		y = System.nanoTime();
		System.out.printf("[Rope.write]   Executed write in % ,18d ns.\n", (y-x));
		return (y-x);
	}

	private static long ropeWriteBad(Rope complexRope) {
		long x,y;

		Writer out = new StringWriter(complexRope.length());
		x = System.nanoTime();
		try {
			out.write(complexRope.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		y = System.nanoTime();
		System.out.printf("[Out.write]    Executed write in % ,18d ns.\n", (y-x));
		return (y-x);
	}

	private static char[] readBF() throws Exception {
		final CharArrayWriter out = new CharArrayWriter(467196);
		final BufferedReader in = new BufferedReader(new FileReader("AutobiographyOfBenjaminFranklin_BenjaminFranklin.txt"));

		final char[] c = new char[256];
		int x = -1;
		while (-1 != (x=in.read(c))) {
			out.write(c, 0, x);
		}
		out.close();
		return out.toCharArray();
	}

	private static char[] readCC() throws Exception {
		final CharArrayWriter out = new CharArrayWriter(182029);
		final BufferedReader in = new BufferedReader(new FileReader("AChristmasCarol_CharlesDickens.txt"));

		final char[] c = new char[256];
		int x = -1;
		while (-1 != (x=in.read(c))) {
			out.write(c, 0, x);
		}
		out.close();
		return out.toCharArray();
	}

	private static long ropeAppendTest(final String aChristmasCarol, final int[][] appendPlan, final int planLength) {
		long x,y;

		x = System.nanoTime();
		Rope result=Rope.BUILDER.build(aChristmasCarol);

		for (int j=0; j<planLength; ++j) {
			final int offset = appendPlan[j][0];
			final int length = appendPlan[j][1];
			result = result.append(result.subSequence(offset, offset+length));
		}
		y = System.nanoTime();
		System.out.printf("[Rope]         Executed append plan in % ,18d ns. Result has length: %d. Rope Depth: %d\n", (y-x), result.length(), ((AbstractRope)result).depth());
		return (y-x);
	}

	private static long ropeDeleteTest(final String aChristmasCarol, final int[][] prependPlan) {
		long x,y;

		x = System.nanoTime();
		Rope result=Rope.BUILDER.build(aChristmasCarol);

		for (int j=0; j<prependPlan.length; ++j) {
			final int offset = prependPlan[j][0];
			final int length = prependPlan[j][1];
			result = result.delete(offset, offset + length);
		}
		y = System.nanoTime();
		System.out.printf("[Rope]         Executed delete plan in % ,18d ns. Result has length: %d. Rope Depth: %d\n", (y-x), result.length(), ((AbstractRope)result).depth());
		return (y-x);
	}

	private static long ropeInsertTest(final char[] aChristmasCarol, final int[][] insertPlan, int planLength) {
		long x,y;
		Rope result=Rope.BUILDER.build(aChristmasCarol);

		x = System.nanoTime();

		for (int j=0; j<planLength; ++j) {
			final int into   = insertPlan[j][0];
			final int offset = insertPlan[j][1];
			final int length = insertPlan[j][2];
			result = result.insert(into, result.subSequence(offset, offset+length));
		}
		y = System.nanoTime();
		System.out.printf("[Rope]         Executed insert plan in % ,18d ns. Result has length: %d. Rope Depth: %d\n", (y-x), result.length(), ((AbstractRope)result).depth());
		complexRope = result;
		return (y-x);
	}

	private static long textInsertTest(final char[] aChristmasCarol, final int[][] insertPlan, int planLength) {
		long x,y;
		Text result=new Text(new String(aChristmasCarol));

		x = System.nanoTime();

		for (int j=0; j<planLength; ++j) {
			final int into   = insertPlan[j][0];
			final int offset = insertPlan[j][1];
			final int length = insertPlan[j][2];
			result = result.insert(into, result.subtext(offset, offset+length));
		}
		y = System.nanoTime();
		System.out.printf("[Text]         Executed insert plan in % ,18d ns. Result has length: %d.\n", (y-x), result.length());
		complexText = result;
		return (y-x);
	}

	private static long ropeInsertTest2(final String aChristmasCarol, final String bensAuto, final int[][] insertPlan) {
		long x,y;

		x = System.nanoTime();
		Rope result=Rope.BUILDER.build(aChristmasCarol);

		for (int j=0; j<insertPlan.length; ++j) {
			final int into   = insertPlan[j][0];
			final int offset = insertPlan[j][1];
			final int length = insertPlan[j][2];
			result = result.insert(into, bensAuto.subSequence(offset, offset+length));
		}
		y = System.nanoTime();
		System.out.printf("[Rope]         Executed insert plan in % ,18d ns. Result has length: %d. Rope Depth: %d\n", (y-x), result.length(), ((AbstractRope)result).depth());
		return (y-x);
	}

	private static long ropePrependTest(final String aChristmasCarol, final int[][] prependPlan, int planLength) {
		long x,y;

		x = System.nanoTime();
		Rope result=Rope.BUILDER.build(aChristmasCarol);

		for (int j=0; j<planLength; ++j) {
			final int offset = prependPlan[j][0];
			final int length = prependPlan[j][1];
			result = result.subSequence(offset, offset+length).append(result);
		}
		y = System.nanoTime();
		System.out.printf("[Rope]         Executed prepend plan in % ,18d ns. Result has length: %d. Rope Depth: %d\n", (y-x), result.length(), ((AbstractRope)result).depth());
		return (y-x);
	}

	private static long textPrependTest(final String aChristmasCarol, final int[][] prependPlan, int planLength) {
		long x,y;

		x = System.nanoTime();
		Text result=new Text(aChristmasCarol);

		for (int j=0; j<planLength; ++j) {
			final int offset = prependPlan[j][0];
			final int length = prependPlan[j][1];
			result = result.subtext(offset, offset+length).concat(result);
		}
		y = System.nanoTime();
		System.out.printf("[Text]         Executed prepend plan in % ,18d ns. Result has length: %d.\n", (y-x), result.length());
		return (y-x);
	}

	private static long ropeTraverseTest_1(final char[] aChristmasCarol) {
		long x,y,result=0;
		final Rope r=Rope.BUILDER.build(aChristmasCarol);

		x = System.nanoTime();

		for (int j=0; j<r.length(); ++j) result+=r.charAt(j);

		y = System.nanoTime();
		System.out.printf("[Rope/charAt]  Executed traversal in % ,18d ns. Result checksum: %d\n", (y-x), result);
		return (y-x);
	}

	private static long ropeTraverseTest_2(final char[] aChristmasCarol) {
		long x,y,result=0;
		final Rope r=Rope.BUILDER.build(aChristmasCarol);
		
		x = System.nanoTime();

		for (final char c: r) result+=c;

		y = System.nanoTime();
		System.out.printf("[Rope/itr]     Executed traversal in % ,18d ns. Result checksum: %d\n", (y-x), result);
		return (y-x);
	}

	private static long ropeTraverseTest2_1(Rope aChristmasCarol) {
		long x,y;

		Rope result=aChristmasCarol;
		
		int r=0;
		x = System.nanoTime();
		for (int j=0; j<result.length(); ++j) r+=result.charAt(j);
		y = System.nanoTime();
		System.out.printf("[Rope/charAt]  Executed traversal in % ,18d ns. Result checksum: %d\n", (y-x), r);
		return (y-x);
	}

	private static long textTraverseTest2(Text aChristmasCarol) {
		long x,y;

		Text result=aChristmasCarol;
		
		int r=0;
		x = System.nanoTime();
		for (int j=0; j<result.length(); ++j) r+=result.charAt(j);
		y = System.nanoTime();
		System.out.printf("[Text/charAt]  Executed traversal in % ,18d ns. Result checksum: %d\n", (y-x), r);
		return (y-x);
	}

	private static long ropeTraverseTest2_2(Rope aChristmasCarol) {
		long x,y;

		Rope result=aChristmasCarol;
		
		int r=0;
		x = System.nanoTime();
		for (final char c: result) r+=c;
		y = System.nanoTime();
		System.out.printf("[Rope/itr]     Executed traversal in % ,18d ns. Result checksum: %d\n", (y-x), r);
		return (y-x);
	}

	private static long stringAppendTest(final String aChristmasCarol, final int[][] appendPlan, final int planLength) {
		long x,y;

		x = System.nanoTime();
		String result=aChristmasCarol;

		for (int j=0; j<planLength; ++j) {
			final int offset = appendPlan[j][0];
			final int length = appendPlan[j][1];
			result = result.concat(result.substring(offset, offset + length));
		}
		y = System.nanoTime();
		System.out.printf("[String]       Executed append plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
		return (y-x);
	}

	private static long stringBufferAppendTest(final String aChristmasCarol, final int[][] appendPlan, final int planLength) {
		long x,y;

		x = System.nanoTime();
		final StringBuffer result=new StringBuffer(aChristmasCarol);

		for (int j=0; j<planLength; ++j) {
			final int offset = appendPlan[j][0];
			final int length = appendPlan[j][1];
			result.append(result.subSequence(offset, offset+length));
		}
		y = System.nanoTime();
		System.out.printf("[StringBuffer] Executed append plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
		return (y-x);
	}

	private static long stringBufferDeleteTest(final String aChristmasCarol, final int[][] prependPlan) {
		long x,y;

		x = System.nanoTime();
		final StringBuffer result=new StringBuffer(aChristmasCarol);

		for (int j=0; j<prependPlan.length; ++j) {
			final int offset = prependPlan[j][0];
			final int length = prependPlan[j][1];
			result.delete(offset, offset+length);
		}
		y = System.nanoTime();
		System.out.printf("[StringBuffer] Executed delete plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
		return (y-x);
	}

	private static long stringBufferInsertTest(final char[] aChristmasCarol, final int[][] insertPlan, int planLength) {
		long x,y;
		final StringBuffer result=new StringBuffer(aChristmasCarol.length); result.append(aChristmasCarol);

		x = System.nanoTime();

		for (int j=0; j<planLength; ++j) {
			final int into   = insertPlan[j][0];
			final int offset = insertPlan[j][1];
			final int length = insertPlan[j][2];
			result.insert(into, result.subSequence(offset, offset+length));
		}
		y = System.nanoTime();
		System.out.printf("[StringBuffer] Executed insert plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
		complexStringBuffer = result;
		return (y-x);
	}

	private static long stringBufferInsertTest2(final String aChristmasCarol, final String bensAuto, final int[][] insertPlan) {
		long x,y;

		x = System.nanoTime();
		final StringBuffer result=new StringBuffer(aChristmasCarol);

		for (int j=0; j<insertPlan.length; ++j) {
			final int into   = insertPlan[j][0];
			final int offset = insertPlan[j][1];
			final int length = insertPlan[j][2];
			result.insert(into, bensAuto.subSequence(offset, offset+length));
		}
		y = System.nanoTime();
		System.out.printf("[StringBuffer] Executed insert plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
		return (y-x);
	}

	private static long stringBufferPrependTest(final String aChristmasCarol, final int[][] prependPlan, int planLength) {
		long x,y;

		x = System.nanoTime();
		final StringBuffer result=new StringBuffer(aChristmasCarol);

		for (int j=0; j<planLength; ++j) {
			final int offset = prependPlan[j][0];
			final int length = prependPlan[j][1];
			result.insert(0, result.subSequence(offset, offset+length));
		}
		y = System.nanoTime();
		System.out.printf("[StringBuffer] Executed prepend plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
		return (y-x);
	}

	private static long stringBufferTraverseTest(final char[] aChristmasCarol) {
		long x,y,result=0;
		final StringBuffer b=new StringBuffer(aChristmasCarol.length); b.append(aChristmasCarol);

		x = System.nanoTime();

		for (int j=0; j<b.length(); ++j) result+=b.charAt(j);

		y = System.nanoTime();
		System.out.printf("[StringBuffer] Executed traversal in % ,18d ns. Result checksum: %d\n", (y-x), result);
		return (y-x);

	}

	private static long stringBufferTraverseTest2(final StringBuffer aChristmasCarol) {
		long x,y;

		final StringBuffer result=aChristmasCarol;
		
		int r=0;
		x = System.nanoTime();
		for (int j=0; j<result.length(); ++j) r+=result.charAt(j);
		y = System.nanoTime();
		System.out.printf("[StringBuffer] Executed traversal in % ,18d ns. Result checksum: %d\n", (y-x), r);
		return (y-x);
	}

	private static long stringDeleteTest(final String aChristmasCarol, final int[][] prependPlan) {
		long x,y;

		x = System.nanoTime();
		String result=aChristmasCarol;

		for (int j=0; j<prependPlan.length; ++j) {
			final int offset = prependPlan[j][0];
			final int length = prependPlan[j][1];
			result = result.substring(0, offset).concat(result.substring(offset+length));
		}
		y = System.nanoTime();
		System.out.printf("[String]       Executed delete plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
		return (y-x);
	}

	private static long stringInsertTest(final char[] aChristmasCarol, final int[][] insertPlan, int planLength) {
		long x,y;
		String result=new String(aChristmasCarol);

		x = System.nanoTime();

		for (int j=0; j<planLength; ++j) {
			final int into   = insertPlan[j][0];
			final int offset = insertPlan[j][1];
			final int length = insertPlan[j][2];
			result = result.substring(0, into).concat(result.substring(offset, offset + length)).concat(result.substring(into));
			
		}
		y = System.nanoTime();
		System.out.printf("[String]       Executed insert plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
		complexString = result;
		return (y-x);
	}

	private static long stringInsertTest2(final String aChristmasCarol, final String bensAuto, final int[][] insertPlan) {
		long x,y;

		x = System.nanoTime();
		String result=aChristmasCarol;

		for (int j=0; j<insertPlan.length; ++j) {
			final int into   = insertPlan[j][0];
			final int offset = insertPlan[j][1];
			final int length = insertPlan[j][2];
			result = result.substring(0, into).concat(bensAuto.substring(offset, offset + length)).concat(result.substring(into));
		}
		y = System.nanoTime();
		System.out.printf("[String]       Executed insert plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
		return (y-x);
	}

	private static long stringPrependTest(final String aChristmasCarol, final int[][] prependPlan, int planLength) {
		long x,y;

		x = System.nanoTime();
		String result=aChristmasCarol;

		for (int j=0; j<planLength; ++j) {
			final int offset = prependPlan[j][0];
			final int length = prependPlan[j][1];
			result = result.substring(offset, offset + length).concat(result);
		}
		y = System.nanoTime();
		System.out.printf("[String]       Executed prepend plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
		return (y-x);
	}

	private static long stringTraverseTest(final char[] aChristmasCarol) {
		long x,y,result=0;
		String s = new String(aChristmasCarol);

		x = System.nanoTime();

		for (int j=0; j<s.length(); ++j) result+=s.charAt(j);

		y = System.nanoTime();
		System.out.printf("[String]       Executed traversal in % ,18d ns. Result checksum: %d\n", (y-x), result);
		return (y-x);
	}

	private static long stringTraverseTest2(final String aChristmasCarol) {
		long x,y;

		String result=aChristmasCarol;

		int r=0;
		x = System.nanoTime();
		for (int j=0; j<result.length(); ++j) r+=result.charAt(j);
		y = System.nanoTime();
		System.out.printf("[String]       Executed traversal in % ,18d ns. Result checksum: %d\n", (y-x), r);
		return (y-x);
	}

	private static long stringRegexpTest(final char[] aChristmasCarol, Pattern pattern) {
		long x,y;
		String s = new String(aChristmasCarol);

		x = System.nanoTime();

		int result = 0;
		Matcher m = pattern.matcher(s);
		while (m.find()) ++result;
		
		y = System.nanoTime();
		System.out.printf("[String]       Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
		return (y-x);
	}

	private static long textRegexpTest(final char[] aChristmasCarol, Pattern pattern) {
		long x,y;
		Text s = new Text(new String(aChristmasCarol));

		x = System.nanoTime();

		int result = 0;
		Matcher m = pattern.matcher(s);
		while (m.find()) ++result;
		
		y = System.nanoTime();
		System.out.printf("[Text]         Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
		return (y-x);
	}

	private static long stringBufferRegexpTest(final char[] aChristmasCarol, Pattern pattern) {
		long x,y;
		StringBuffer buffer = new StringBuffer(aChristmasCarol.length); buffer.append(aChristmasCarol);

		x = System.nanoTime();

		int result = 0;
		Matcher m = pattern.matcher(buffer);
		while (m.find()) ++result;
		
		y = System.nanoTime();
		System.out.printf("[StringBuffer] Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
		return (y-x);
	}

	private static long ropeRegexpTest(final char[] aChristmasCarol, Pattern pattern) {
		long x,y;
		Rope rope = Rope.BUILDER.build(aChristmasCarol);

		x = System.nanoTime();

		int result = 0;
		Matcher m = pattern.matcher(rope);
		while (m.find()) ++result;
		
		y = System.nanoTime();
		System.out.printf("[Rope]         Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
		return (y-x);
	}

	private static long ropeMatcherRegexpTest(final char[] aChristmasCarol, Pattern pattern) {
		long x,y;
		Rope rope = Rope.BUILDER.build(aChristmasCarol);

		x = System.nanoTime();

		int result = 0;
		Matcher m = rope.matcher(pattern); 
		while (m.find()) ++result;
		
		y = System.nanoTime();
		System.out.printf("[Rope.matcher] Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
		return (y-x);
	}
	


	private static long stringRegexpTest2(final String aChristmasCarol, Pattern pattern) {
		long x,y;

		x = System.nanoTime();

		int result = 0;
		Matcher m = pattern.matcher(aChristmasCarol);
		while (m.find()) ++result;
		
		y = System.nanoTime();
		System.out.printf("[String]       Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
		return (y-x);
	}
	


	private static long textRegexpTest2(final Text aChristmasCarol, Pattern pattern) {
		long x,y;

		x = System.nanoTime();

		int result = 0;
		Matcher m = pattern.matcher(aChristmasCarol);
		while (m.find()) ++result;
		
		y = System.nanoTime();
		System.out.printf("[Text]         Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
		return (y-x);
	}

	private static long stringBufferRegexpTest2(final StringBuffer aChristmasCarol, Pattern pattern) {
		long x,y;

		x = System.nanoTime();

		int result = 0;
		Matcher m = pattern.matcher(aChristmasCarol);
		while (m.find()) ++result;
		
		y = System.nanoTime();
		System.out.printf("[StringBuffer] Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
		return (y-x);
	}

	private static long ropeRegexpTest2(final Rope aChristmasCarol, Pattern pattern) {
		long x,y;

		x = System.nanoTime();

		int result = 0;
		Matcher m = pattern.matcher(aChristmasCarol);
		while (m.find()) ++result;
		
		y = System.nanoTime();
		System.out.printf("[Rope]         Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
		return (y-x);
	}

	private static long ropeRebalancedRegexpTest2(final Rope aChristmasCarol, Pattern pattern) {
		long x,y;

		x = System.nanoTime();

		CharSequence adaptedRope = aChristmasCarol.rebalance(); //Rope.BUILDER.buildForRegexpSearching(aChristmasCarol);
		int result = 0;
		Matcher m = pattern.matcher(adaptedRope);
		while (m.find()) ++result;
		
		y = System.nanoTime();
		System.out.printf("[Reblncd Rope] Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
		return (y-x);
	}

	private static long ropeMatcherRegexpTest2(final Rope aChristmasCarol, Pattern pattern) {
		long x,y;

		x = System.nanoTime();
		
		int result = 0;
		Matcher m = aChristmasCarol.matcher(pattern);
		while (m.find()) ++result;
		
		y = System.nanoTime();
		System.out.printf("[Rope.matcher] Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
		return (y-x);
	}

	private static String time(final long x, final long y) {
		return (y-x) + "ns";
	}
	
	private static void stat(PrintStream out, long[] stats, String unit, String prefix) {
		if (stats.length < 3) 
			System.err.println("Cannot print stats.");
		Arrays.sort(stats);
		
		double median = ((stats.length & 1) == 1 ? stats[stats.length >> 1]: (stats[stats.length >> 1] + stats[1 + (stats.length >> 1)]) / 2);
		double average = 0;
		for (int j=1;j<stats.length-1;++j) {
			average += stats[j];
		}
		average /= stats.length - 2;
		out.printf("%-14s Average=% ,16.0f %s Median=% ,16.0f%s\n", prefix, average, unit, median, unit);
	}

}

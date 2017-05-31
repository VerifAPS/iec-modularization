package edu.kit.formal.modularization;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Before;
import org.junit.Test;

import edu.kit.formal.modularization.graph.BiCGNode;
import edu.kit.formal.modularization.graph.CallGraph;
import edu.kit.formal.modularization.graph.SimpleCGNode;
import edu.kit.iti.formal.automation.NiceErrorListener;
import edu.kit.iti.formal.automation.parser.IEC61131Lexer;
import edu.kit.iti.formal.automation.parser.IEC61131Parser;
import edu.kit.iti.formal.automation.scope.LocalScope;
import edu.kit.iti.formal.automation.smv.SymbolicExecutioner;
import edu.kit.iti.formal.automation.st.ast.TopLevelElement;
import org.junit.Assert;

/*-
 * #%L
 * plc-modularization
 * %%
 * Copyright (C) 2017 Alexander Weigl
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */



public class TestMain {
	
	SymbolicExecutioner se;
	
	private static final List<TopLevelElement> loadSourceFile(
			final String fileName) throws IOException {
		
		final URL url = TestMain.class.getClassLoader().getResource(fileName);
		
		if(url == null) {
			System.err.println("File '" + fileName + "' not found");
			return null;
		}
		
		final String         absFileName =
			new File(url.getFile()).getAbsolutePath();
		final IEC61131Lexer  lexer       =
			new IEC61131Lexer(new ANTLRFileStream(absFileName));
		final IEC61131Parser parser      =
			new IEC61131Parser(new CommonTokenStream(lexer));
		
		parser.addErrorListener(new NiceErrorListener(absFileName));
		System.out.println();
		
		return parser.start().ast;
	}
	
	@Before
	public void setupExecutioner() {
	    se = new SymbolicExecutioner();
	    LocalScope scope = new LocalScope();
	    scope.builder().identifiers("A", "B", "C").setBaseType("INT").create();
	    scope.getLocalVariables().values().forEach(se::lift);
	}

	@Test
	public final void biCallGaph() throws IOException {
		
		final List<TopLevelElement>       program1 =
			loadSourceFile("bi_call_graph1.st");
		final List<TopLevelElement>       program2 =
			loadSourceFile("bi_call_graph2.st");
		final CallGraph<BiCGNode<Object>> cg       =
			CallGraph.create(program1, program2);
		
		System.out.println(cg);
		
		Assert.assertFalse(cg.is1to1mapping());
	}
	
	@Test
	public final void simpleCallGaph() throws IOException {
		
		final List<TopLevelElement>           program =
			loadSourceFile("simple_call_graph.st");
		final CallGraph<SimpleCGNode<Object>> cg      =
			CallGraph.create(program);
		
		System.out.println(cg);
		
		Assert.assertTrue(cg.is1to1mapping());
	}

	@Test
	public void simpleTest() throws IOException {
	    
		//final StatementList list = loadFunctionBlock("fb_call.st");
		
		//final StatementList fb1 = loadFunctionBlock("fb2.st");
		//System.out.println(fb1);
		//fb1.visit(se);
		//System.out.println("visit complete");
		//System.out.println(se.peek());
		//for(Map.Entry<SVariable, SMVExpr> i : se.peek().entrySet()) {
		//	System.out.println(i.getValue());
		//}
		
		//System.out.println(se.peek().toString());
		//System.out.println(se.peek().toString());
		
	    /*
		StatementList list = IEC61131Facade.statements(
	            "a := 2;" +
	                    "c := 3;" +
	                    "c := a+c;" +
	                    "b := 2*a+c;");
	    list.visit(se);
	    Assert.assertEquals("{a=0sd16_2, b=((0sd16_2*0sd16_2)+(0sd16_2+0sd16_3)), c=(0sd16_2+0sd16_3)}",
	            se.peek().toString()
	    );
	    */
	}
}

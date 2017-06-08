package edu.kit.formal.modularization;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

import edu.kit.formal.modularization.graph.BiCGNode;
import edu.kit.formal.modularization.graph.CallGraph;
import edu.kit.formal.modularization.graph.SimpleCGNode;
import edu.kit.iti.formal.automation.IEC61131Facade;
import edu.kit.iti.formal.automation.SymbExFacade;
import edu.kit.iti.formal.automation.scope.LocalScope;
import edu.kit.iti.formal.automation.smv.SymbolicExecutioner;
import edu.kit.iti.formal.automation.st.StructuredTextPrinter;
import edu.kit.iti.formal.automation.st.ast.FunctionBlockDeclaration;
import edu.kit.iti.formal.automation.st.ast.ProgramDeclaration;
import edu.kit.iti.formal.automation.st.ast.TopLevelElement;
import edu.kit.iti.formal.automation.st.ast.TopLevelElements;
import edu.kit.iti.formal.smv.ast.SMVModule;

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
	
	private final TopLevelElements loadSourceFile1(
			final String fileName) throws IOException {
		
		try(final Scanner scanner =
			new Scanner(getClass().getResourceAsStream(fileName))) {
			
			scanner.useDelimiter("\\A");
			return IEC61131Facade.file(scanner.hasNext() ? scanner.next() : "");
		}
	}
	/*
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
	*/
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
			loadSourceFile1("/bi_call_graph1.st");
		final List<TopLevelElement>       program2 =
			loadSourceFile1("/bi_call_graph2.st");
		final CallGraph<BiCGNode<Object>> cg       =
			CallGraph.create(program1, program2);
		
		System.out.println(cg);
		
		Assert.assertFalse(cg.is1to1mapping());
	}
	
	@Test
	public final void simpleCallGaph() throws IOException {
		
		final List<TopLevelElement>           program =
			loadSourceFile1("/simple_call_graph.st");
		final CallGraph<SimpleCGNode<Object>> cg      =
			CallGraph.create(program);
		
		System.out.println(cg);
		
		Assert.assertTrue(cg.is1to1mapping());
	}

	@Test
	public final void structuralFBEquivalence() throws IOException {
		
		final TopLevelElement program1 =
			loadSourceFile1("/structural_equal1.st").get(0);
		final TopLevelElement program2 =
			loadSourceFile1("/structural_equal2.st").get(0);
		
		Assert.assertTrue(
			ModularProver.areFunctionBlocksEquivalent(program1, program2));
	}
	
	@Test
	public final void proverTest() throws IOException {
		
		final SMVModule[][] modules = ModularProver.generateSmvFiles(
			loadSourceFile1("/program1.st"),
			loadSourceFile1("/program2.st"));
		
		for(SMVModule[] i : modules)
			for(SMVModule j : i) System.out.println(j);
	}
    
    @Test
    public void testSimplify() throws IOException {
    	System.out.println("Embed test:");
    	
        TopLevelElements toplevels = loadSourceFile1("/embed_test.st");
        IEC61131Facade.resolveDataTypes(toplevels);
        
        final TopLevelElements simpleProgram = SymbExFacade.simplify(
        	toplevels, (FunctionBlockDeclaration)toplevels.get(1));
        
        final ProgramDeclaration decl = (ProgramDeclaration)simpleProgram.get(1);
        
        final StructuredTextPrinter stp = new StructuredTextPrinter();
        
        decl.visit(stp);
        System.out.println(stp.getString());
    }
}

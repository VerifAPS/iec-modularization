package edu.kit.formal.modularization;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.kit.formal.modularization.graph.BiCGNode;
import edu.kit.formal.modularization.graph.CallGraph;
import edu.kit.iti.formal.automation.IEC61131Facade;
import edu.kit.iti.formal.automation.SymbExFacade;
import edu.kit.iti.formal.automation.datatypes.Any;
import edu.kit.iti.formal.automation.datatypes.FunctionBlockDataType;
import edu.kit.iti.formal.automation.scope.LocalScope;
import edu.kit.iti.formal.automation.smv.SymbolicExecutioner;
import edu.kit.iti.formal.automation.smv.SymbolicState;
import edu.kit.iti.formal.automation.st.StructuredTextPrinter;
import edu.kit.iti.formal.automation.st.ast.FunctionBlockDeclaration;
import edu.kit.iti.formal.automation.st.ast.FunctionCall;
import edu.kit.iti.formal.automation.st.ast.FunctionCallStatement;
import edu.kit.iti.formal.automation.st.ast.ProgramDeclaration;
import edu.kit.iti.formal.automation.st.ast.Reference;
import edu.kit.iti.formal.automation.st.ast.Statement;
import edu.kit.iti.formal.automation.st.ast.StatementList;
import edu.kit.iti.formal.automation.st.ast.SymbolicReference;
import edu.kit.iti.formal.automation.st.ast.TopLevelElement;
import edu.kit.iti.formal.automation.st.ast.TopLevelElements;
import edu.kit.iti.formal.automation.st.ast.TypeDeclarations;
import edu.kit.iti.formal.automation.st.ast.VariableDeclaration;
import edu.kit.iti.formal.smv.SMVFacade;
import edu.kit.iti.formal.smv.ast.SBinaryExpression;
import edu.kit.iti.formal.smv.ast.SBinaryOperator;
import edu.kit.iti.formal.smv.ast.SMVExpr;
import edu.kit.iti.formal.smv.ast.SMVModule;
import edu.kit.iti.formal.smv.ast.SMVModuleImpl;
import edu.kit.iti.formal.smv.ast.SMVType;
import edu.kit.iti.formal.smv.ast.SVariable;

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



public final class ModularProver {
	
	private static final class VariableRenamer extends FullVisitor {
		
		private final String _instName;
		
		private VariableRenamer(final FunctionCallStatement fcs) {
			_instName = fcs.getFunctionCall().getFunctionName().getIdentifier();
		}
		
		@Override
		public final Object visit(final SymbolicReference symbRef) {
			
			final String    identifier = symbRef.getIdentifier();
			final Reference sub        = symbRef.getSub();
			
			// Check whether this is a reference to an input variable
			if(!identifier.equals(_instName) || sub == null) return null;
			
			// TODO: Doesn't work for deeper subs
			symbRef.setSub(null);
			symbRef.setIdentifier(
				_instName + "$" + ((SymbolicReference)sub).getIdentifier());
			
			return null;
		}
	}
	
	private static final int _VAR_DECL_IN_OUT_MASK =
		VariableDeclaration.INPUT |
		VariableDeclaration.INOUT |
		VariableDeclaration.OUTPUT;
	
	public static final SMVExpr evaluateFunctionBlock(FunctionBlockDeclaration decl,
            SMVExpr... args) {
        return evaluateFunction(decl, Arrays.asList(args));
    
    }
    
	private static void _substituteFBCalls(
			final FunctionBlockDeclaration fbDecl) {
		
		final StatementList statements  = fbDecl.getFunctionBody();
		final LocalScope    localScope  = fbDecl.getLocalScope();
		
		for(int i = 0; i < statements.size(); i++) {
			
			final Statement statement = statements.get(i);
			
			if(!(statement instanceof FunctionCallStatement)) continue;
			
			statements.remove(i--);
			
			final FunctionCallStatement fcs  = (FunctionCallStatement)statement;
			final VariableDeclaration   fbvd =
				localScope.getVariable(fcs.getFunctionCall().getFunctionName());
			
			// The variable declaration may have been removed earlier due to
			// another invocation of this function block
			if(fbvd == null) continue;
			
			final Any dataType = fbvd.getDataType();
			
			if(!(dataType instanceof FunctionBlockDataType)) continue;
			
			final FunctionBlockDeclaration fbd =
				((FunctionBlockDataType)dataType).getFunctionBlock();
			final String prefix = fbvd.getName() + "$";
			
			// Add the input/output variables from the callee to the caller
			for(VariableDeclaration j : fbd.getLocalScope()) {
				
				if((j.getType() & _VAR_DECL_IN_OUT_MASK) == 0) continue;
				
				final VariableDeclaration vd = new VariableDeclaration(j);
				
				// TODO: This can be removed but is currently kept for correct
				// syntax
				if(j.isInput())
					vd.setType(
						(j.getType() & ~_VAR_DECL_IN_OUT_MASK) |
						VariableDeclaration.LOCAL);
				
				if(j.isOutput() || j.isInOut())
					vd.setType(
						(j.getType() & ~_VAR_DECL_IN_OUT_MASK) |
						VariableDeclaration.INPUT);

				vd.setName(prefix + j.getName());
				localScope.add(vd);
			}
			
			statements.visit(new VariableRenamer(fcs));
			localScope.getLocalVariables().remove(fbvd.getName());
		}
		
		//final StructuredTextPrinter stp = new StructuredTextPrinter();
		//
		//fbDecl.visit(stp);
		//System.out.println(stp.getString());
	}
	
    private static SMVExpr evaluateFunction(FunctionBlockDeclaration decl,
            List<SMVExpr> ts) {
        SymbolicExecutioner se = new SymbolicExecutioner();
        SymbolicState state = new SymbolicState();
        // <name>(i1,i2,i2,...)
        FunctionCall fc = new FunctionCall();
        fc.setFunctionName(new SymbolicReference(decl.getFunctionBlockName()));
        int i = 0;
        for (VariableDeclaration vd : decl.getLocalScope()
                .filterByFlags(VariableDeclaration.INPUT)) {
            fc.getParameters()
                    .add(new FunctionCall.Parameter(vd.getName(), false,
                            new SymbolicReference(vd.getName())));
            state.put(se.lift(vd), ts.get(i++));
        }
        se.push(state);
        se.getGlobalScope().registerFunctionBlock(decl);
        return fc.visit(se);
    }

	public static final boolean areFunctionBlocksEquivalent(
			final TopLevelElement fb1,
			final TopLevelElement fb2) {
		
		final StructuredTextPrinter stp = new StructuredTextPrinter();
		
		fb1.visit(stp);
		
		final String fb1String = stp.getString();
		
		stp.clear();
		fb2.visit(stp);
		
		final String fb2String = stp.getString();
		
		if(fb1String.equals(fb2String)) return true;
		
		return false;
	}
	
	public static final SMVModule[][] generateSmvFiles(
			final TopLevelElements program1,
			final TopLevelElements program2) {
		
		IEC61131Facade.resolveDataTypes(program1);
		IEC61131Facade.resolveDataTypes(program2);
		
		final CallGraph<BiCGNode<Object>> cg      =
			CallGraph.create(program1, program2);
		final SMVModule[][]               modules = new SMVModule[cg.size()][];
		
		int curModulePos = 0;
		
		for(BiCGNode<Object> i : cg.bottomUp) {
			
			_substituteFBCalls((FunctionBlockDeclaration)i.host1);
			_substituteFBCalls((FunctionBlockDeclaration)i.host2);
			
			SMVModuleImpl smv1 = null;
			SMVModuleImpl smv2 = null;
			
			if(i.host1 instanceof ProgramDeclaration) {
				smv1 = (SMVModuleImpl)SymbExFacade.evaluateProgram(program1);
				smv2 = (SMVModuleImpl)SymbExFacade.evaluateProgram(program2);
			}
			
			if(i.host1 instanceof FunctionBlockDeclaration) {
				
				final TopLevelElements simple1 = SymbExFacade.simplify(
					program1, (FunctionBlockDeclaration)i.host1);
				final TopLevelElements simple2 = SymbExFacade.simplify(
					program1, (FunctionBlockDeclaration)i.host2);
				
				smv1 = (SMVModuleImpl)SymbExFacade.evaluateProgram(
					(ProgramDeclaration)simple1.get(1),
					(TypeDeclarations)  simple1.get(0));
				smv2 = (SMVModuleImpl)SymbExFacade.evaluateProgram(
					(ProgramDeclaration)simple2.get(1),
					(TypeDeclarations)  simple2.get(0));
			}
			
			smv1.setName(smv1.getName() + "_1");
			smv2.setName(smv2.getName() + "_2");
			
			final SMVModuleImpl mainModule    = new SMVModuleImpl();
			final List<SMVExpr> invarEquations = new LinkedList<>();
			
			mainModule.setName("main");
			mainModule.getInputVars().addAll(smv1.getModuleParameter());
			
			mainModule.getStateVars().add(
				new SVariable(
					"fb1",
					new SMVType.Module(
						smv1.getName(), mainModule.getInputVars())));
			
			mainModule.getStateVars().add(
				new SVariable(
					"fb2",
					new SMVType.Module(
						smv2.getName(), mainModule.getInputVars())));
			
			for(SVariable j : smv1.getStateVars())
				invarEquations.add(new SBinaryExpression(
					new SVariable("fb1." + j.getName(), j.getDatatype()),
					SBinaryOperator.EQUAL,
					new SVariable("fb2." + j.getName(), j.getDatatype())));
			
			mainModule.getInvarSpec().add(
				SMVFacade.combine(SBinaryOperator.AND, invarEquations));
			
			modules[curModulePos++] = new SMVModule[]{mainModule, smv1, smv2};
		}
		
		return modules;
	}
}

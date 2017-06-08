package edu.kit.formal.modularization;

/*-
 * #%L
 * iec-modularization
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


import edu.kit.iti.formal.automation.scope.LocalScope;
import edu.kit.iti.formal.automation.st.ast.AssignmentStatement;
import edu.kit.iti.formal.automation.st.ast.BinaryExpression;
import edu.kit.iti.formal.automation.st.ast.CaseStatement;
import edu.kit.iti.formal.automation.st.ast.ClassDeclaration;
import edu.kit.iti.formal.automation.st.ast.Deref;
import edu.kit.iti.formal.automation.st.ast.ExpressionList;
import edu.kit.iti.formal.automation.st.ast.ForStatement;
import edu.kit.iti.formal.automation.st.ast.FunctionBlockDeclaration;
import edu.kit.iti.formal.automation.st.ast.FunctionCall;
import edu.kit.iti.formal.automation.st.ast.FunctionCallStatement;
import edu.kit.iti.formal.automation.st.ast.FunctionDeclaration;
import edu.kit.iti.formal.automation.st.ast.GuardedStatement;
import edu.kit.iti.formal.automation.st.ast.IfStatement;
import edu.kit.iti.formal.automation.st.ast.MethodDeclaration;
import edu.kit.iti.formal.automation.st.ast.ProgramDeclaration;
import edu.kit.iti.formal.automation.st.ast.RepeatStatement;
import edu.kit.iti.formal.automation.st.ast.StatementList;
import edu.kit.iti.formal.automation.st.ast.StructureInitialization;
import edu.kit.iti.formal.automation.st.ast.SymbolicReference;
import edu.kit.iti.formal.automation.st.ast.TypeDeclarations;
import edu.kit.iti.formal.automation.st.ast.UnaryExpression;
import edu.kit.iti.formal.automation.st.ast.VariableDeclaration;
import edu.kit.iti.formal.automation.st.ast.WhileStatement;
import edu.kit.iti.formal.automation.visitors.DefaultVisitor;

public class FullVisitor extends DefaultVisitor<Object> {
	
    @Override
    public Object visit(final AssignmentStatement assignmentStatement) {
        
    	assignmentStatement.getLocation  ().visit(this);
    	assignmentStatement.getExpression().visit(this);
    	
    	return null;
    }
    
    @Override
    public Object visit(final BinaryExpression binaryExpression) {
        
    	binaryExpression.getLeftExpr ().visit(this);
    	binaryExpression.getRightExpr().visit(this);
    	
    	return null;
    }
    
    @Override
    public Object visit(final RepeatStatement repeatStatement) {
        
    	repeatStatement.getCondition ().visit(this);
    	repeatStatement.getStatements().visit(this);
    	
    	return null;
    }

    @Override
    public Object visit(final WhileStatement whileStatement) {
        
    	whileStatement.getCondition ().visit(this);
    	whileStatement.getStatements().visit(this);
    	
    	return null;
    }
    
    @Override
    public Object visit(final UnaryExpression unaryExpression) {
    	return unaryExpression.getExpression().visit(this);
    }
    
	@SuppressWarnings("unchecked")
	@Override
    public Object visit(final TypeDeclarations typeDeclarations) {
		typeDeclarations.forEach(x -> x.visit(this));
    	return null;
    }
	
    @Override
    public Object visit(final CaseStatement caseStatement) {
    	
    	caseStatement.getExpression().visit(this);
    	caseStatement.getCases     ().forEach(x -> x.visit(this));
    	caseStatement.getElseCase  ().visit(this);
    	
        return null;
    }

    @Override
    public Object visit(final StatementList statements) {
    	statements.forEach(x -> x.visit(this));
        return null;
    }

    @Override
    public Object visit(final ProgramDeclaration programDeclaration) {
    	
    	programDeclaration.getLocalScope ().visit(this);
    	programDeclaration.getProgramBody().visit(this);
    	
    	return null;
    }

    @Override
    public Object visit(final ExpressionList expressions) {
        expressions.forEach(x -> x.visit(this));
    	return null;
    }
    
    @Override
    public Object visit(final FunctionDeclaration functionDeclaration) {
        return functionDeclaration.getStatements().visit(this);
    }

    @Override
    public Object visit(final FunctionCall functionCall) {
    	return functionCall.getFunctionName().visit(this);
    }

    @Override
    public Object visit(final ForStatement forStatement) {
        
    	forStatement.getStart     ().visit(this);
    	forStatement.getStop      ().visit(this);
    	forStatement.getStep      ().visit(this);
    	forStatement.getStatements().visit(this);
    	
    	return null;
    }

    @Override
    public Object visit(final FunctionBlockDeclaration functionBlockDeclaration) {
        
    	functionBlockDeclaration.getLocalScope  ().visit(this);
    	functionBlockDeclaration.getFunctionBody().visit(this);
    	
    	return null;
    }

    @Override
    public Object visit(final IfStatement ifStatement) {
    	ifStatement.getConditionalBranches().forEach(x -> x.visit(this));
    	return ifStatement.getElseBranch().visit(this);
    }
    
    public Object visit(final GuardedStatement guardedStatement) {
    	
    	guardedStatement.getCondition ().visit(this);
    	guardedStatement.getStatements().visit(this);
    	
        return null;
    }

    @Override
    public Object visit(final FunctionCallStatement functionCallStatement) {
    	return functionCallStatement.getFunctionCall().visit(this);
    }

    @Override
    public Object visit(final CaseStatement.Case aCase) {
    	
    	aCase.getConditions().forEach(x -> x.visit(this));
    	aCase.getStatements().visit(this);
        
    	return null;
    }

    @Override
    public Object visit(final LocalScope localScope) {
    	localScope.getLocalVariables().forEach((x, y) -> y.visit(this));
    	return null;
    }

    @Override
    public Object visit(final VariableDeclaration variableDeclaration) {
    	return variableDeclaration.getInit().visit(this);
    }

    @Override
    public Object visit(StructureInitialization structureInitialization) {
        structureInitialization.getInitValues().forEach((x, y) -> y.visit(this));
    	return null;
    }
    
    @Override
    public Object visit(final Deref deref) {
    	return deref.getReference().visit(this);
    }

    @Override
    public Object visit(final SymbolicReference symbolicReference) {
    	return symbolicReference.getSub().visit(this);
    }

    @Override
    public Object visit(final ClassDeclaration clazz) {
    	
    	clazz.getLocalScope().visit(this);
    	clazz.getMethods   ().forEach(x -> x.visit(this));
        
    	return null;
    }

    @Override
    public Object visit(final MethodDeclaration method) {
        
    	method.getLocalScope().visit(this);
    	method.getStatements().visit(this);
    	
    	return null;
    }
}

package edu.kit.formal.modularization.graph;

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


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.iti.formal.automation.datatypes.Any;
import edu.kit.iti.formal.automation.datatypes.FunctionBlockDataType;
import edu.kit.iti.formal.automation.scope.LocalScope;
import edu.kit.iti.formal.automation.st.ast.FunctionBlockDeclaration;
import edu.kit.iti.formal.automation.st.ast.FunctionCall;
import edu.kit.iti.formal.automation.st.ast.FunctionCallStatement;
import edu.kit.iti.formal.automation.st.ast.FunctionDeclaration;
import edu.kit.iti.formal.automation.st.ast.ProgramDeclaration;
import edu.kit.iti.formal.automation.st.ast.Statement;
import edu.kit.iti.formal.automation.st.ast.StatementList;
import edu.kit.iti.formal.automation.st.ast.TopLevelElement;
import edu.kit.iti.formal.automation.st.ast.VariableDeclaration;
import edu.kit.iti.formal.automation.visitors.DefaultVisitor;

public final class CallGraph<N extends CGNode<N, ?>> implements Iterable<N> {

	private final class IteratorNode {
		
		private final Set<IteratorNode> _callers = new HashSet<>();
		private final Set<IteratorNode> _callees = new HashSet<>();
		private final N                 _parent;
		
		private IteratorNode(final N parent) {
			this._parent = parent;
		}
	}
	
	private abstract class DirectionalIterator implements Iterator<N> {
		
		protected final Map<N, IteratorNode> _varNodes = new HashMap<>();
		
		protected DirectionalIterator() {
			
			for(N i : _nodes.values()) _varNodes.put(i, new IteratorNode(i));
			
			for(N i : _nodes.values()) {
				
				final IteratorNode node = _varNodes.get(i);
				
				for(N j : i.callers) node._callers.add(_varNodes.get(j));
				for(N j : i.callees) node._callees.add(_varNodes.get(j));
			}
		}
		
		@Override
		public final boolean hasNext() {
			return !_varNodes.isEmpty();
		}
	}
	
	private final class BottomUpIterator extends DirectionalIterator {
		
		@Override
		public final N next() {
			
			IteratorNode nextNode = null;
			
			for(IteratorNode i : _varNodes.values()) {
				if(i._callees.isEmpty()) {
					nextNode = i;
					break;
				}
			}
			
			for(IteratorNode i : nextNode._callers)
				i._callees.remove(nextNode);
			_varNodes.remove(nextNode._parent);
			
			return nextNode._parent;
		}
		
	}

	private final class TopDownIterator extends DirectionalIterator {
		
		@Override
		public final N next() {
			
			IteratorNode nextNode = null;
			
			for(IteratorNode i : _varNodes.values()) {
				if(i._callers.isEmpty()) {
					nextNode = i;
					break;
				}
			}
			
			for(IteratorNode i : nextNode._callees)
				i._callers.remove(nextNode);
			_varNodes.remove(nextNode._parent);
			
			return nextNode._parent;
		}
	}
	
	private static final class GraphBuilder<N extends CGNode<N, ?>>
		extends DefaultVisitor<Object> {
    	
		private final Map<String, N> _nodes;
		
    	private N          _node       = null;
    	private LocalScope _localScope = null;
    	
    	private GraphBuilder(final Map<String, N> nodes) {
    		_nodes = nodes;
    	}
    	
    	@Override
    	public final Object visit(final FunctionCall fc) {
    		
    		final VariableDeclaration vd       =
    			_localScope.getVariable(fc.getFunctionName());
    		final Any                 dataType = vd.getDataType();
    		
    		final N calledNode = _nodes.getOrDefault(
    			dataType != null ?
    				((FunctionBlockDataType)dataType).getFunctionBlock().
    					getFunctionBlockName() :
    				vd.getDataTypeName(),
    			null);
    		
    		if(calledNode != null) {
    			_node     .callees.add(calledNode);
    			calledNode.callers.add(_node);
    		}
    		
    		return null;
    	}
    
    	@Override
    	public final Object visit(final FunctionCallStatement fcs) {
    		fcs.getFunctionCall().visit(this);
    		return null;
    	}
    
    	@Override
    	public final Object visit(final StatementList sl) {
    
    		for(Statement i : sl) i.visit(this);
    		return null;
    	}
    	
    	@Override
    	public final Object visit(final FunctionBlockDeclaration fbd) {
    		
    		_node       = _nodes.get(fbd.getIdentifier());
    		_localScope = fbd.getLocalScope();
    		fbd.getFunctionBody().visit(this);
    		
    		return null;
    	}
    	
    	@Override
    	public final Object visit(final FunctionDeclaration fd) {
    
    		_node       = _nodes.get(fd.getIdentifier());
    		_localScope = fd.getLocalScope();
    		fd.getStatements().visit(this);
    		
    		return null;
    	}
    	
    	@Override
    	public final Object visit(final ProgramDeclaration pd) {
    
    		_node       = _nodes.get(pd.getIdentifier());
    		_localScope = pd.getLocalScope();
    		pd.getProgramBody().visit(this);
    		
    		return null;
    	}
    }
	
	private final Map<String, N> _nodes       = new HashMap<>();
	private       boolean        _1to1mapping = true;
	
	public final Iterable<N> topDown  = () -> new TopDownIterator();
	public final Iterable<N> bottomUp = () -> new BottomUpIterator();
	
	private CallGraph() {}
	
	private final CallGraph<N> _compute1to1mapping() {
		for(N i : _nodes.values()) _1to1mapping = _1to1mapping && i.complete;
		return this;
	}
	
	public static final <D> CallGraph<SimpleCGNode<D>> create(
			final List<TopLevelElement> program) {
		
		final CallGraph   <SimpleCGNode<D>> cg = new CallGraph<>();
		final GraphBuilder<SimpleCGNode<D>> gb =
			new GraphBuilder<>(cg._nodes);
		
		for(TopLevelElement i : program)
			cg._nodes.put(i.getIdentifier(), new SimpleCGNode<>(i));
		
		for(TopLevelElement i : program) i.visit(gb);
		
		return cg._compute1to1mapping();
	}
	
	public static final <D> CallGraph<BiCGNode<D>> create(
    		final List<TopLevelElement> program1,
    		final List<TopLevelElement> program2) {
		
		final CallGraph   <BiCGNode<D>> cg = new CallGraph<>();
		final GraphBuilder<BiCGNode<D>> gb = new GraphBuilder<>(cg._nodes);
		
		final Map<String, TopLevelElement> elementsByName1 = new HashMap<>();
		
		for(TopLevelElement i : program1)
			elementsByName1.put(i.getIdentifier(), i);
		
		for(TopLevelElement i : program2) {
			
			final String          name    = i.getIdentifier();
			final TopLevelElement pendant =
				elementsByName1.getOrDefault(name, null);
			
			cg._nodes.put(name, new BiCGNode<>(pendant, i));
			
			// Remove the pendant, so only those elements of program 1 without a
			// pendant in program 2 will remain
			if(pendant != null) elementsByName1.remove(name);
		}
		
		for(Map.Entry<String, TopLevelElement> i : elementsByName1.entrySet())
			cg._nodes.put(i.getKey(), new BiCGNode<>(i.getValue(), null));
		
		for(TopLevelElement i : program1) i.visit(gb);
		for(TopLevelElement i : program2) i.visit(gb);
		
		return cg._compute1to1mapping();
	}
	
	public final boolean is1to1mapping() {
		return _1to1mapping;
	}

	@Override
	public Iterator<N> iterator() {
		return _nodes.values().iterator();
	}
	
	public final int size() {
		return _nodes.size();
	}
	
	@Override
	public final String toString() {
		
		final StringBuilder sb = new StringBuilder();
		
		for(N i : topDown) i.toString(sb).append('\n');
		
		// Remove the last line break
		sb.setLength(sb.length() - 1);
		
		return sb.toString();
	}
}

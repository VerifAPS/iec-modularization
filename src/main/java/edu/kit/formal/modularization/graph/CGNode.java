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


import java.util.HashSet;
import java.util.Set;

public abstract class CGNode<This extends CGNode<This, D>, D> {

	public final String    name;
	public final boolean   complete;
	public final Set<This> callers = new HashSet<>();
	public final Set<This> callees = new HashSet<>();
	
	public D data = null;
	
	private final void _apppendNodeNames(
    		final StringBuilder sb,
    		final String        caption,
    		final Set<This>     nodes) {
		
		sb.append("  ").append(caption).append(": ");
		
		if(nodes.isEmpty()) {
			sb.append("-\n");
		} else {
			
			boolean first = true;
			
			for(This i : nodes) {
				if(!first) sb.append("           ");
				sb.append(i.name).append('\n');
				first = false;
			}
		}
	}
	
	protected CGNode(
			final String  name,
			final boolean complete) {
		
		this.name     = name;
		this.complete = complete;
	}
	
	protected abstract void printName(StringBuilder sb);
	
	@Override
	public final String toString() {
		return toString(new StringBuilder()).toString();
	}
	
	public final StringBuilder toString(final StringBuilder sb) {
		
		sb.append("Node: ");
		printName(sb);
		sb.append('\n');
		
		_apppendNodeNames(sb, "callers", callers);
		_apppendNodeNames(sb, "callees", callees);
		
		// Remove the last line break
		sb.setLength(sb.length() - 1);
		
		return sb;
	}
}

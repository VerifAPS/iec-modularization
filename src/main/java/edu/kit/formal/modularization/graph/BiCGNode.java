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


import edu.kit.iti.formal.automation.st.ast.TopLevelElement;

public final class BiCGNode<D> extends CGNode<BiCGNode<D>, D> {
	
	public final TopLevelElement host1;
	public final TopLevelElement host2;
	
	public BiCGNode(
			final TopLevelElement host1,
			final TopLevelElement host2) {
		
		super(
			host1 != null ? host1.getIdentifier() : host2.getIdentifier(),
			host1 != null && host2 != null);
		
		this.host1 = host1;
		this.host2 = host2;
	}

	@Override
	protected final void printName(final StringBuilder sb) {
		sb.append(host1 != null ? name : "- ");
		sb.append('|');
		sb.append(host2 != null ? name : " -");
	}
}

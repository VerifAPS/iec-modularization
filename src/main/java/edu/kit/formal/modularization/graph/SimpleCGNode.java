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

public final class SimpleCGNode<D> extends CGNode<SimpleCGNode<D>, D> {
	
	public final TopLevelElement host;
	
	public SimpleCGNode(final TopLevelElement host) {
		super(host.getIdentifier(), true);
		this.host = host;
	}

	@Override
	protected final void printName(final StringBuilder sb) {
		sb.append(name);
	}
}

package edu.kit.formal.modularization;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.kit.iti.formal.automation.IEC61131Facade;
import edu.kit.iti.formal.automation.scope.LocalScope;
import edu.kit.iti.formal.automation.smv.SymbolicExecutioner;
import edu.kit.iti.formal.automation.st.ast.StatementList;

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

    @Before
    public void setupExecutioner() {
        se = new SymbolicExecutioner();
        LocalScope scope = new LocalScope();
        scope.builder().identifiers("a", "b", "c", "d", "e", "f").setBaseType("INT").create();
        scope.getLocalVariables().values().forEach(se::lift);
    }

    @Test
    public void simpleTest() {
        StatementList list = IEC61131Facade.statements(
                "a := 2;" +
                        "c := 3;" +
                        "c := a+c;" +
                        "b := 2*a+c;");
        list.visit(se);
        Assert.assertEquals("{a=0sd16_2, b=((0sd16_2*0sd16_2)+(0sd16_2+0sd16_3)), c=(0sd16_2+0sd16_3)}",
                se.peek().toString()
        );
    }
}

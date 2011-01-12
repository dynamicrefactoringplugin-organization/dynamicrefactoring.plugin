/*<Dynamic Refactoring Plugin For Eclipse 2.0 - Plugin that allows to perform refactorings 
on Java code within Eclipse, as well as to dynamically create and manage new refactorings>

Copyright (C) 2009  Laura Fuente De La Fuente

This file is part of Foobar

Foobar is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package repository.moon.concreterefactoring;

import java.util.*;

import javamoon.construct.source.SourceLoader;
import javamoon.core.JavaModel;

import moon.core.MoonFactory;
import moon.core.classdef.*;
import moon.core.genericity.BoundS;
import moon.core.genericity.FormalPar;

import static org.junit.Assert.*;
import org.junit.Test;

import refactoring.engine.PreconditionException;

import repository.RefactoringTemplateAbstractTest;
import repository.moon.MOONRefactoring;

/** 
 * Comprueba que funciona correctamente la refactorización que especializa la 
 * acotación F de un parámetro formal
 * 
 * <p>Indirectamente, se comprueba también la corrección de las funciones,
 * acciones y predicados utilizados por la refactorización.</p>
 *
 * @author <A HREF="mailto:sfd0009@alu.ubu.es">Sonia Fuente de la Fuente</A>
 * @author <A HREF="mailto:ehp0001@alu.ubu.es">Enrique Herrero Paredes</A>
 * 
 * @see SpecializeBoundF
 */
public class TestSpecializeBoundF extends RefactoringTemplateAbstractTest {

	/** 
	 * Comprueba que la refactorización funciona correctamente en un caso 
	 * simple.
	 * 
	 * @throws Exception si se produce un error durante la ejecución de la prueba.
	 */ 
	@Test
	public void testSimple() throws Exception{

		SourceLoader sourceLoader = new SourceLoader();
		sourceLoader.loadFromDirectory(formatString(
			"./testdata/repository/moon/concreterefactoring/TestSpecializeBoundF/testSimple"));		 //$NON-NLS-1$
		JavaModel jm = JavaModel.getInstance();
		MoonFactory factory = jm.getMoonFactory();

		ClassDef classDef = jm.getClassDef(factory.createName("paqueteA.AcotaF")); //$NON-NLS-1$

		List<FormalPar> lFormalPar = classDef.getFormalPars();
		FormalPar formalPar = lFormalPar.get(0); 

		ClassType newType = (ClassType)jm.getType(factory.createName("paqueteA.D")); //$NON-NLS-1$

		ClassType oldType = null;
		if (formalPar instanceof BoundS){
			List<Type> bounds = ((BoundS)formalPar).getBounds();
			for(int i = 0; i < bounds.size(); i++){
				if (bounds.get(i).getUniqueName().equals(
						factory.createName(("paqueteA.M<paqueteA.AcotaF@E>")))){ //$NON-NLS-1$
					oldType = (ClassType)bounds.get(i);
					break;
				}
			}
		}

		MOONRefactoring ref = new SpecializeBoundF(formalPar, oldType, newType, jm);			
		ref.run();	

		// Comienzan las comprobaciones
		ClassType bTypeDest = (ClassType)(((BoundS)classDef.getFormalPars().get(0)).getBounds()).get(0);
		assertEquals(newType,bTypeDest);
	}

	/** 
	 * Comprueba que la refactorización funciona correctamente en un caso 
	 * con más de un parámetro formal.
	 *
	 * @throws Exception si se produce un error durante la ejecución de la prueba.
	 */ 
	@Test
	public void testWithMorePar() throws Exception{

		SourceLoader sourceLoader = new SourceLoader();
		sourceLoader.loadFromDirectory(formatString(
			"./testdata/repository/moon/concreterefactoring/TestSpecializeBoundF/testWithMorePar"));		 //$NON-NLS-1$
		JavaModel jm = JavaModel.getInstance();
		MoonFactory factory = jm.getMoonFactory();

		ClassDef classDef = jm.getClassDef(factory.createName("paqueteA.AcotaF")); //$NON-NLS-1$

		List<FormalPar> lFormalPar = classDef.getFormalPars();
		FormalPar formalPar = lFormalPar.get(0); 

		ClassType newType = (ClassType)jm.getType(factory.createName("paqueteA.D")); //$NON-NLS-1$

		ClassType oldType = null;
		if (formalPar instanceof BoundS){
			List<Type> bounds = ((BoundS)formalPar).getBounds();
			for(int i = 0; i < bounds.size(); i++){
				if (bounds.get(i).getUniqueName().equals(
						factory.createName(("paqueteA.M<paqueteA.AcotaF@E>")))){ //$NON-NLS-1$
					oldType = (ClassType)bounds.get(i);
					break;
				}
			}
		}

		MOONRefactoring ref = new SpecializeBoundF(formalPar, oldType, newType, jm);			
		ref.run();	

		// Comienzan las comprobaciones
		ClassType bTypeDest = (ClassType)(((BoundS)classDef.getFormalPars().get(0)).getBounds()).get(0);
		assertEquals(newType,bTypeDest);
	}

	/**
	 * Verifica el funcionamiento de las precondiciones de la refactorización.
	 *
	 * <p>Comprueba que se lanza una excepción cuando se intenta realizar la 
	 * refactorización cuando la clase seleccionada no es BoundF.</p>
	 * 
	 * @throws Exception si se produce un error durante la ejecución de la prueba.
	 */ 
	@Test(expected=PreconditionException.class) 
	public void testCheckIsNotBoundF() throws Exception{

		SourceLoader sourceLoader = new SourceLoader();
		sourceLoader.loadFromDirectory(formatString(
			"./testdata/repository/moon/concreterefactoring/TestSpecializeBoundF/testCheckIsNotBoundF"));		 //$NON-NLS-1$
		JavaModel jm = JavaModel.getInstance();
		MoonFactory factory = jm.getMoonFactory();

		ClassDef classDef = jm.getClassDef(factory.createName("paqueteA.M")); //$NON-NLS-1$

		List<FormalPar> lFormalPar = classDef.getFormalPars();
		FormalPar formalPar = lFormalPar.get(0); 

		ClassType newType = (ClassType)jm.getType(factory.createName("paqueteA.H")); //$NON-NLS-1$

		ClassType oldType = null;
		if (formalPar instanceof BoundS){
			List<Type> bounds = ((BoundS)formalPar).getBounds();
			for(int i = 0; i < bounds.size(); i++){
				if (bounds.get(i).getUniqueName().equals(
						factory.createName(("paqueteA.H")))){ //$NON-NLS-1$
					oldType = (ClassType)bounds.get(i);
					break;
				}
			}
		}

		MOONRefactoring ref = new SpecializeBoundF(formalPar, oldType, newType, jm);			
		ref.run();	
	}

	/**
	 * Verifica el funcionamiento de las precondiciones de la refactorización.
	 *
	 * <p>Comprueba que se lanza una excepción cuando se intenta realizar la refactorización y
	 * no todas las sustituciones al parámetro formal son iguales.</p>
	 * 
	 * @throws Exception si se produce un error durante la ejecución de la prueba.
	 */ 
	@Test(expected=PreconditionException.class)
	public void testCheckIsSubstFormalPar() throws Exception {

		SourceLoader sourceLoader = new SourceLoader();
		sourceLoader.loadFromDirectory(formatString(
			"./testdata/repository/moon/concreterefactoring/TestSpecializeBoundF/testCheckIsSubstFormalPar"));		 //$NON-NLS-1$
		JavaModel jm = JavaModel.getInstance();
		MoonFactory factory = jm.getMoonFactory();

		ClassDef classDef = jm.getClassDef(factory.createName("paqueteA.AcotaF")); //$NON-NLS-1$

		List<FormalPar> lFormalPar = classDef.getFormalPars();
		FormalPar formalPar = lFormalPar.get(0); 

		ClassType newType = (ClassType)jm.getType(factory.createName("paqueteA.D")); //$NON-NLS-1$

		ClassType oldType = null;
		if (formalPar instanceof BoundS){
			List<Type> bounds = ((BoundS)formalPar).getBounds();
			for(int i = 0; i < bounds.size(); i++){
				if (bounds.get(i).getUniqueName().equals(
						factory.createName(("paqueteA.M<paqueteA.AcotaF@E>")))){ //$NON-NLS-1$
					oldType = (ClassType)bounds.get(i);
					break;
				}
			}
		}
		MOONRefactoring ref = new SpecializeBoundF(formalPar, oldType, newType, jm);			
		ref.run();	
	}
}
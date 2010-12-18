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

package repository.moon.concreteaction;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;


import moon.core.Name;
import moon.core.classdef.*;
import moon.core.entity.Entity;
import moon.core.instruction.CodeFragment;

import refactoring.engine.Action;
import repository.RelayListenerRegistry;
import repository.moon.concretefunction.LocalEntitiesAccessed;

/**
 * Permite extraer los argumentos formales que formar�n parte del m�todo
 * que se forma a partir de un fragmento de c�digo determinado.
 *
 * @author <A HREF="mailto:rmartico@ubu.es">Ra�l Marticorena</A>
 */ 
public class AddExtractedFormalArgToMethod extends Action {
	
	/**
	 * Code fragment.
	 */
	private CodeFragment fragment;

	/**
	 * Name.
	 */
	private Name name;
	
	/**
	 * Clase a la que se mover� el m�todo.
	 */
	private ClassDef classDef;
	
	/**
	 * M�todo que se va a mover de una clase a otra.
	 */
	 private MethDec method;
	 
	 /**
	  * Receptor de los mensajes enviados por la acci�n concreta.
	  */
	 private RelayListenerRegistry listenerReg;
	 	
	/**
	 * Constructor.<p>
	 *
	 * Obtiene una nueva instancia de AddExtractedFormalArgToMethod.
	 * @param name nombre del nuevo m�todo a ser creado.
	 * @param fragment fragmento de c�digo a ser tratado.
	 */	
	public AddExtractedFormalArgToMethod(Name name, CodeFragment fragment){
		super();
		this.name = name;
		this.classDef = fragment.getClassDef();
		this.method = fragment.getMethDec();
		this.fragment = fragment;
		
		listenerReg = RelayListenerRegistry.getInstance();
	}
	
	/**
	 * A�ade los par�metros al m�todo.
	 */
	@Override
	public void run() {		
		listenerReg.notify("# run():AddExtractedFormalArgToMethod #"); //$NON-NLS-1$

		listenerReg.notify("\t- Adding formalArgs to  " + method.getUniqueName().toString() //$NON-NLS-1$
			+ " from " + classDef.getName().toString()); //$NON-NLS-1$
		
		
		List<MethDec> listMethDec = this.classDef.getMethDecByName(name);
		MethDec methDec = listMethDec.get(0);
		
		
		Collection cole = this.fragment.getInstructionsInMethod();
		Iterator it = cole.iterator();

		LocalEntitiesAccessed lea = new LocalEntitiesAccessed(this.fragment.getInstructionsInMethod());
		
		
		List<Entity> list = (List<Entity>) lea.getCollection();
		for (Entity entity : list){
			Action action = new AddFormalArg(methDec, entity.getName(), entity.getType());	
			action.run();
		}		
		
		 //$NON-NLS-1$
	}

	/**
	 * Deshace la adicci�n de los par�metro del m�todo.
	 */
	@Override
	public void undo() {		
		listenerReg.notify("# undo():AddExtractedFormalArgToMethod #"); //$NON-NLS-1$
		
		AddExtractedFormalArgToMethod undo = new AddExtractedFormalArgToMethod(name, fragment);

		undo.run();
	}
}
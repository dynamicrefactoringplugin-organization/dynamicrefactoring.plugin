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

import java.util.*;


import moon.core.classdef.*;
import moon.core.instruction.*;
import moon.core.Name;

import refactoring.engine.Action;
import repository.RelayListenerRegistry;
import repository.moon.MOONRefactoring;
import repository.moon.concretefunction.*;

/**
 * Permite incluir un nuevo argumento formal en la signatura de un m�todo.<p>
 *
 * Se ocupa de incluir como par�metro real un valor por defecto en todas las 
 * llamadas al m�todo que existan en las clases del modelo. Si el argumento
 * es de alguno de los tipos primitivos, se asignar� como valor real el valor 
 * habitual por defecto de cada tipo; si no, se le asignar� un valor nulo.
 *
 * @author <A HREF="mailto:ehp0001@alu.ubu.es">Enrique Herrero Paredes</A>
 * @author <A HREF="mailto:alc0022@alu.ubu.es">�ngel L�pez Campo</A>
 * @author <A HREF="mailto:sfd0009@alu.ubu.es">Sonia Fuente de la Fuente</A>
 */ 
public class AddFormalArg extends Action {

	/**
	 * El nuevo par�metro formal que se va a a�adir al m�todo.
	 */
	FormalArgument newParameter;
	
	/**
	 * El m�todo a cuya lista de par�metros se va a a�adir el nuevo argumento.
	 */
	MethDec method;
	
	/**
	 * La clase a la que pertenece el m�todo que se va a modificar.
	 */
	private ClassDef classDef;
		
	/**
	 * El nombre �nico del m�todo modificado antes del cambio de signatura.
	 */
	private String originalUniqueName;
	
	/**
	 * Elemento auxiliar para extender el cambio en el m�todo a otras clases,
	 * en caso de que aparezca en clases superiores o inferiores en la jerarqu�a
	 * de herencia.
	 */
	private Vector<AddFormalArgWithoutHierarchy> addParInOtherClassVec;
	
	/**
	 * Elemento auxiliar para extender el cambio en el m�todo a todas las
	 * instrucciones con llamadas al m�todo.
	 */
	private ArrayList<AddFormalArgIntoInstructions> addIntoInstr;
	
	/**
	 * Receptor de los mensajes enviados por la acci�n concreta.
	 */
	private RelayListenerRegistry listenerReg;
			 
	/**
	 * Constructor.<p>
	 *
	 * Obtiene una nueva instancia de AddFormalArg.
	 *
	 * @param method el m�todo a cuya signatura se va a a�adir un argumento.
	 * @param name el nombre del nuevo par�metro formal.
	 * @param type el tipo del nuevo par�metro formal.
	 */	
	public AddFormalArg(MethDec method, Name name, Type type){
		
		super();
		
		this.method = method;
		this.classDef = method.getClassDef();
		this.originalUniqueName = method.getUniqueName().toString();
		this.addParInOtherClassVec = new Vector<AddFormalArgWithoutHierarchy>(10,1);
		this.addIntoInstr = new ArrayList<AddFormalArgIntoInstructions>();
		
		newParameter = MOONRefactoring.getModel().getMoonFactory().
			createFormalArgument(name, type);
		newParameter.setMethDec(method);
		
		listenerReg = RelayListenerRegistry.getInstance();
	}	
	
	/**
	 * A�ade un par�metro formal a la signatura de un m�todo.
	 */
	public void run() {
		
		listenerReg.notify("# run():AddFormalArg #"); //$NON-NLS-1$
		
		addIntoHierarchy();
		
		method.add(newParameter);
		
		String name = newParameter.getUniqueName().toString(); 
		listenerReg.notify("\t- Adding formal argument " + name); //$NON-NLS-1$
			
		Collection<ClassDef> allClasses = 
			MOONRefactoring.getModel().getClassDefSourceAvailable();
		Collection<MethDec> allMethods = new Vector<MethDec>();
			
		for (ClassDef next : allClasses){
			MethodCollector methColl = new MethodCollector(next);
			Collection<MethDec> classMethods = methColl.getCollection();
			allMethods.addAll(classMethods);
		}
		

		for (MethDec next : allMethods){
			List<Instr> instrList = next.getInstructions();
			AddFormalArgIntoInstructions addParam = new AddFormalArgIntoInstructions(
				instrList, newParameter, method);
			addIntoInstr.add(addParam);
			addParam.run();
		}

	}

	/**
	 * Extiende la adici�n del argumento formal a las clases de la jerarqu�a. 
	 */
	void addIntoHierarchy() {
		Collection<ClassDef> alreadyFoundClasses = new Vector<ClassDef>(10,1);
		
		ClassesAffectedByMethRenameCollector getSuperAndSubclasses =
			new ClassesAffectedByMethRenameCollector(
				classDef, method.getUniqueName().toString(), 
				alreadyFoundClasses,true);
				
		Collection<ClassDef> superAndSubclasses = 
			getSuperAndSubclasses.getCollection();
		superAndSubclasses.remove(classDef);
		
		
		addFormalArgIntoSubAndSuperclasses(getSuperAndSubclasses.getCollection());
	}

	/**
	 * Elimina el nuevo par�metro formal de la signatura del m�todo.
	 */
	public void undo() {
		
		listenerReg.notify("# undo():AddFormalArg #"); //$NON-NLS-1$
		
		if(! addParInOtherClassVec.isEmpty())
			for(int i = 0; i < addParInOtherClassVec.size(); i++)
				addParInOtherClassVec.get(i).undo();
		
		if(! addIntoInstr.isEmpty())
			for(AddFormalArgIntoInstructions next : addIntoInstr)
				next.undo();
		
		listenerReg.notify("\t- Undoing formal argument addition"); //$NON-NLS-1$
		
		if(method.getFormalArgument().size() > 0)
			method.removeFormalArg(method.getFormalArgument().size()-1);
	}
	
	/**
	 * A�ade el par�metro en la signatura del m�todo en las clases inferiores 
	 * y superiores de la jerarqu�a de herencia que, a trav�s de herencia, 
	 * posean el mismo m�todo (clases que hereden de la que posee el m�todo
	 * afectado o superclases de la misma que contengan el mismo m�todo, y a su
	 * vez, recursivamente, subclases o superclases de las mismas).
	 *
	 * @param affectedClasses las clases de la jerarqu�a de herencia que se ven 
	 * afectadas por el cambio de la signatura del m�todo.
	 */
	private void addFormalArgIntoSubAndSuperclasses (
		Collection<ClassDef> affectedClasses){
		
		int i = 0;
		
		Iterator<ClassDef> classesIt = affectedClasses.iterator();
		
		while(classesIt.hasNext()){			
			ClassDef affectedClass = classesIt.next();
			
			int indexOfMethodName = originalUniqueName.lastIndexOf('~');
			String methNameWithoutPath = originalUniqueName.substring(indexOfMethodName);
			String methUniqueName = affectedClass.getUniqueName() + methNameWithoutPath;
			
			List<MethDec> methodsWithName = 
				affectedClass.getMethDecByName(method.getName());
			for (MethDec affectedClassMethod : methodsWithName)
				if (affectedClassMethod.getUniqueName().toString().equals(methUniqueName)){
					addParInOtherClassVec.add(new AddFormalArgWithoutHierarchy(
						affectedClassMethod, newParameter.getName(), 
						newParameter.getType()));
					addParInOtherClassVec.get(i).run();
					i++;
					break;
				}
		}
		
	}
}
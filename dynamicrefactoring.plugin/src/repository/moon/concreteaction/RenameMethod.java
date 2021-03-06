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
import moon.core.Name;

import refactoring.engine.Action;
import repository.RelayListenerRegistry;
import repository.moon.concretefunction.*;

/**
 * Permite renombrar un m�todo de una representaci�n MOON de un modelo Java
 * teniendo incluso en cuenta las consideraciones pertinentes cuando la clase
 * que contiene al m�todo se encuentre dentro de una jerarqu�a de herencia.<p>
 *
 * Comprueba si existen superclases o subclases que puedan verse afectadas por
 * el renombrado del m�todo y, en tal caso, extiende a ellas las modificaciones
 * necesarias para mantener la coherencia del modelo.
 *
 * @author <A HREF="mailto:alc0022@alu.ubu.es">�ngel L�pez Campo</A>
 * @author <A HREF="mailto:ehp0001@alu.ubu.es">Enrique Herrero Paredes</A>
 * @author <A HREF="mailto:sfd0009@alu.ubu.es">Sonia Fuente de la Fuente</A>
 */ 
public class RenameMethod extends Action {
	
	/**
	 * M�todo que se debe renombrar.
	 */
	private MethDec method;
	
	/**
	 * Clase que contiene el m�todo que sufrir� el renombrado.
	 */
	private ClassDef classDef;
	
	/**
	 * Nuevo nombre que se dar� al m�todo.
	 */
	private Name newName;
	
	/**
	 * Nombre del m�todo antes del renombrado.
	 */
	private Name originalName;
	
	/**
	 * Nombre �nico del m�todo antes del renombrado.
	 */
	private String originalUniqueName;
	
	/**
	 * Elemento auxiliar para renombrar el m�todo en caso de que aparezca en
	 * clases superiores o inferiores en la jerarqu�a de herencia.
	 */
	private Vector<RenameMethodWithoutHierarchy> renMethInOtherClassVec;

	/**
	 * Receptor de los mensajes enviados por la acci�n concreta.
	 */
	private RelayListenerRegistry listenerReg;
		
	/**
	 * Constructor.<p>
	 *
	 * Obtiene una nueva instancia de RenameMethod.
	 * @param method el m�todo cuyo nombre se desea cambiar.
	 * @param classDef la clase que contiene el m�todo que se va a renombrar.
	 * @param newName el nuevo nombre que se dar� al m�todo.
	 */
	public RenameMethod (MethDec method, ClassDef classDef, Name newName){
			
		super();
		
		this.classDef = classDef;
		this.method = method;
		this.newName = newName;
		this.originalName = method.getName();
		this.originalUniqueName = method.getUniqueName().toString();
		
		renMethInOtherClassVec = new Vector<RenameMethodWithoutHierarchy>(10,1);
		
		listenerReg = RelayListenerRegistry.getInstance();
	}
	
	/**
	 * Ejecuta el renombrado del m�todo.
	 */
	public void run(){
		
		listenerReg.notify("# run():RenameMethod #"); //$NON-NLS-1$
		
		Collection<ClassDef> alreadyFoundClasses = new Vector<ClassDef>(10,1);
		ClassesAffectedByMethRenameCollector getSuperAndSubclasses =
			new ClassesAffectedByMethRenameCollector(classDef,
				method.getUniqueName().toString(), alreadyFoundClasses,true);
		Collection<ClassDef> superAndSubclasses = 
			getSuperAndSubclasses.getCollection();
		superAndSubclasses.remove(classDef);
		
			
		renameSubAndSuperclasses(getSuperAndSubclasses.getCollection());
		
		listenerReg.notify("\t- Renaming method"); //$NON-NLS-1$
		listenerReg.notify("\t\tFormer name: \"" + method.getName().toString() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		listenerReg.notify("\t\tNew name: \"" + newName.toString()+ "\""); //$NON-NLS-1$ //$NON-NLS-2$
		
		method.setName(newName);
	}
	
	/**
	 * Deshace el renombrado del m�todo.
	 */
	public void undo(){
		
		listenerReg.notify("# undo():RenameMethod #");		 //$NON-NLS-1$
		
		if(! renMethInOtherClassVec.isEmpty())
			for(int i=0; i < renMethInOtherClassVec.size(); i++)
				renMethInOtherClassVec.get(i).undo();
		
		listenerReg.notify("\t- Undoing method renaming"); //$NON-NLS-1$
		listenerReg.notify("\t\tFormer name: \"" + method.getName().toString() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		listenerReg.notify("\t\tNew name: \"" + originalName.toString() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		
		method.setName(originalName);
	}	
	
	/**
	 * Renombra el m�todo, si es necesario, en las clases inferiores y superiores
	 * de la jerarqu�a de herencia (clases que hereden de la que posee el m�todo
	 * renombrado, o superclases de la misma que posean el mismo m�todo).
	 *
	 * @param affectedClasses las clases de la jerarqu�a de herencia que se ven 
	 * afectadas por el cambio en la signatura del m�todo renombrado.
	 */
	private void renameSubAndSuperclasses(Collection<ClassDef> affectedClasses){
		
		int i=0;
		
		for (ClassDef affectedClass : affectedClasses){
			int indexOfMethodName = originalUniqueName.lastIndexOf('~');
			String methNameWithoutPath = originalUniqueName.substring(indexOfMethodName);
			String propUniqueName = 
				affectedClass.getUniqueName().toString() + methNameWithoutPath;
			
			List<MethDec> possibleMethods = 
				affectedClass.getMethDecByName(method.getName());
			MethDec affectedClassMethod = null;
			for(MethDec md : possibleMethods)
				if(md.getUniqueName().toString().equals(propUniqueName)){
					affectedClassMethod = md;
					break;
				}
						
			renMethInOtherClassVec.add(new RenameMethodWithoutHierarchy(
				affectedClassMethod, newName));
			renMethInOtherClassVec.get(i).run();
			i++;
		}
	}
}
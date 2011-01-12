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

package dynamicrefactoring.util.selection;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jdt.core.*;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;

import org.eclipse.jface.text.TextSelection;

import org.eclipse.ui.*;

import dynamicrefactoring.util.io.JavaFileManager;

/**
 * Proporciona funciones capaces de determinar el tipo de elemento que se 
 * encuentra seleccionado en una ventana y otra informaci�n adicional 
 * relativa al mismo.
 * 
 * @author <A HREF="mailto:sfd0009@alu.ubu.es">Sonia Fuente de la Fuente</A>
 * @author <A HREF="mailto:ehp0001@alu.ubu.es">Enrique Herrero Paredes</A>
 */
public abstract class SelectionInfo {
	
	/**
	 * Constante que representa una declaraci�n de argumento formal.
	 * 
	 * @see IJavaElement#LOCAL_VARIABLE
	 */
	public static final int FORMAL_ARGUMENT = -1;
	
	/**
	 * Constante que representa una declaraci�n de variable local.
	 * 
	 * @see IJavaElement#LOCAL_VARIABLE
	 */
	public static final int LOCAL_VARIABLE = -2;
	
	/**
	 * Selecci�n gen�rica cuya informaci�n se desea consultar.
	 */
	private ISelection selection;
	
	/**
	 * Ventana del espacio de trabajo sobre la que se ha hecho la selecci�n.
	 */
	protected IWorkbenchWindow window;
	
	/**
	 * Nombre completamente cualificado del tipo de selecci�n.
	 */
	protected String selectionName;
	
	/**
	 * Nombre completamente cualificado del elemento "Tipo" o "Clase".
	 */
	protected final String typeName = "org.eclipse.jdt.internal.core.SourceType"; //$NON-NLS-1$
	
	/**
	 * Nombre completamente cualificado del elemento "M�todo" o "Funci�n".
	 */
	protected final String methodName = "org.eclipse.jdt.internal.core.SourceMethod"; //$NON-NLS-1$
	
	/**
	 * Nombre completamente cualificado del elemento "Atributo de instancia".
	 */
	private final String fieldName = "org.eclipse.jdt.internal.core.SourceField"; //$NON-NLS-1$
	
	/**
	 * Nombre completamente cualificado del elemento "Par�metro formal".
	 */
	private final String parameterName = "org.eclipse.jdt.internal.core.TypeParameter"; //$NON-NLS-1$
	
	/**
	 * Nombre completamente cualificado del elemento "Argumento formal".
	 */
	private final String formalArgumentName = "dynamicrefactoring.SourceFormalArgument"; //$NON-NLS-1$
	
	
	/**
	 * Constructor.
	 * 
	 * @param selection la selecci�n sobre la que se desea obtener informaci�n.
	 * @param window la ventana sobre la que se ha efectuado la selecci�n.
	 */
	public SelectionInfo (ISelection selection, 
		IWorkbenchWindow window){
		
		this.selection = selection;
		this.selectionName = null;
		this.window = window;
	}
	
	/**
	 * Determina el tipo de elemento seleccionado en una ventana.
	 * Solo tiene en consideraci�n los tipos indicados por {@link #typeName}, 
	 * {@link #methodName}, {@link #fieldName}, {@link #parameterName} y 
	 * {@link #formalArgumentName}.
	 * 
	 * @return el nombre completamente cualificado del tipo de elemento 
	 * seleccionado, o <code>null</code> si no es ninguno de los admitidos. 
	 */
	public abstract String getSelectionType();
		
	/**
	 * Comprueba si el nombre completamente cualificado de tipo de selecci�n se
	 * corresponde con alguno de los tipos de selecci�n v�lidos como entrada para
	 * la herramienta de refactorizaci�n.
	 * 
	 * @return <code>true</code> si es uno de los tipos contemplados como v�lidos;
	 * <code>false</code> en caso contrario.
	 */
	public boolean isValidSelectionType(){
		ArrayList<String> validTypes = new ArrayList<String>();
			
		validTypes.add(typeName);
		validTypes.add(fieldName);
		validTypes.add(methodName);
		validTypes.add(parameterName);
		validTypes.add(formalArgumentName);
		
		return validTypes.contains(getSelectionType()) 
			|| (isTextSelection() && ((TextSelection)selection).getLength()>0);
	}
	
	/**
	 * Comprueba si el nombre completamente cualificado del tipo de la selecci�n 
	 * corresponde a un atributo seleccionado.
	 * 
	 * @return <code>true</code> si se trata de un atributo seleccionado;
	 * <code>false</code> en caso contrario.
	 */
	public boolean isFieldSelection(){
		return (getSelectionType().equals(fieldName));
	}
	
	/**
	 * Comprueba si un nombre completamente cualificado del tipo de la selecci�n 
	 * corresponde a un m�todo seleccionado.
	 * 
	 * @return <code>true</code> si se trata de un m�todo seleccionado;
	 * <code>false</code> en caso contrario.
	 */
	public boolean isMethodSelection(){
		return (getSelectionType().equals(methodName));
	}
	
	/**
	 * Comprueba si un nombre completamente cualificado del tipo de la selecci�n 
	 * corresponde a una clase seleccionada.
	 * 
	 * @return <code>true</code> si se trata de una clase seleccionada;
	 * <code>false</code> en caso contrario.
	 */
	public boolean isClassSelection(){
		return (getSelectionType().equals(typeName));
	}
	
	/**
	 * Comprueba si un nombre completamente cualificado del tipo de la selecci�n
	 * corresponde a un argumento formal de un m�todo seleccionado.
	 * 
	 * @return <code>true</code> si se trata de un argumento formal de un m�todo;
	 * <code>false</code> en caso contrario.
	 */
	public boolean isFormalArgumentSelection(){
		return (getSelectionType().equals(formalArgumentName));
	}
	
	/**
	 * Comprueba si un nombre completamente cualificado del tipo de la selecci�n
	 * corresponde a un par�metro formal.
	 * 
	 * @return <code>true</code> si se trata de un par�metro formal;
	 * <code>false</code> en caso contrario.
	 */
	public boolean isFormalParameterSelection(){
		return (getSelectionType().equals(parameterName));
	}
	
	/**
	 * Comprueba si la selecci�n es parte de una representaci�n en �rbol.
	 * 
	 * @return <code>true</code> si se trata de una selecci�n en un �rbol;
	 * <code>false</code> en caso contrario.
	 */
	public boolean isTreeSelection(){
		return (selection instanceof TreeSelection);
	}
	
	/**
	 * Comprueba si la selecci�n es parte de una representaci�n textual.
	 * 
	 * @return <code>true</code> si se trata de una selecci�n de texto;
	 * <code>false</code> en caso contrario.
	 */
	public boolean isTextSelection(){
		return (selection instanceof TextSelection);
	}
	
	/**
	 * Obtiene el nombre completamente cualificado de los tipos de elemento 
	 * descritos por la interfaz <code>IJavaElement</code>.
	 * 
	 * @param type el valor codificado por IJavaElement para el tipo de elemento Java.
	 * 
	 * @return el nombre completamente cualificado del tipo equivalente.
	 */
	protected String decodeElementType(int type){
		switch(type){
		case IJavaElement.TYPE:
			return typeName;
		case IJavaElement.FIELD:
			return fieldName;
		case IJavaElement.METHOD:
			return methodName;
		case IJavaElement.TYPE_PARAMETER:
			return parameterName;
		case FORMAL_ARGUMENT:
			return formalArgumentName;
		default:
			return "";		 //$NON-NLS-1$
		}
	}

	/**
	 * Obtiene el proyecto al que pertenece el elemento seleccionado.
	 * 
	 * @return el proyecto al que pertenece el elemento seleccionado.
	 */
	public abstract IProject getProjectForSelection();
	
	/**
	 * Obtiene la lista de directorios de fuentes del proyecto al que pertenece
	 * el elemento seleccionado.
	 * 
	 * M�todo plantilla (patr�n de dise�o M�todo Plantilla).
	 * 
	 * @return la lista de directorios de fuentes del proyecto al que pertenece
	 * el elemento seleccionado.
	 */
	public List<String> getSourceDirsForSelection(){
		IJavaProject project = getJavaProjectForSelection();
		
		return JavaFileManager.getSourceDirsForProject(project);
	}
	
	/**
	 * Obtiene el proyecto del espacio de trabajo con el nombre especificado.
	 * 
	 * @param name el nombre del proyecto que se quiere recuperar.
	 * 
	 * @return el proyecto del espacio de trabajo con el nombre especificado por #name.
	 */
	protected IProject getJavaProjectForName(String name) {
		IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();

		IProject myProject = wsroot.getProject(name);
		return myProject;
	}

	/**
	 * Obtiene el proyecto al que pertenece la selecci�n actual como un <code>
	 * IJavaElement</code>, de manera que se pueda manejar aprovechando las 
	 * posibilidades de Eclipse para el manejo de elementos Java.
	 * 
	 * Operaci�n primitiva (patr�n de dise�o M�todo Plantilla).
	 * 
	 * @return el proyecto al que pertenece la selecci�n actual como un <code>
	 * IJavaElement</code>.
	 */
	public abstract IJavaProject getJavaProjectForSelection();
	
	/**
	 * Obtiene la selecci�n asociada al procesador de informaci�n.
	 * 
	 * @return la selecci�n asociada al procesador de informaci�n.
	 */
	public ISelection getSelection(){
		return selection;
	}
}
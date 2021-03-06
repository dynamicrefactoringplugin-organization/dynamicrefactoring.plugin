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

package dynamicrefactoring.interfaz.wizard;

import dynamicrefactoring.RefactoringPlugin;
import dynamicrefactoring.interfaz.ButtonTextProvider;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.swtdesigner.ResourceManager;

/**
 * Proporciona una especializaci�n del di�logo de asistente de Eclipse.
 * 
 * <p>Permite internacionalizar los textos de los botones del di�logo por defecto
 * en el que se abren los asistentes en Eclipse, que de otra forma tendr�an siempre
 * los textos por defecto en ingl�s.</p>
 * 
 * @author <A HREF="mailto:sfd0009@alu.ubu.es">Sonia Fuente de la Fuente</A>
 * @author <A HREF="mailto:ehp0001@alu.ubu.es">Enrique Herrero Paredes</A>
 */
public class CustomWizardDialog extends WizardDialog {

	/**
	 * Constructor.
	 * 
	 * @param shell <i>shell</i> en la que se abrir� el di�logo del asistente.
	 * @param wizard asistente que se debe abrir en el di�logo.
	 */
	public CustomWizardDialog(Shell shell, RefactoringWizard wizard){
		super(shell, wizard);
	}
	
	/**
	 * Crea los botones del di�logo en que se muestra un asistente.
	 * 
	 * @param parent el componente padre que contendr� los botones.
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		
		if (super.getButton(IDialogConstants.FINISH_ID) != null)
			super.getButton(IDialogConstants.FINISH_ID).setText(
				ButtonTextProvider.getFinishText());
		
		if (super.getButton(IDialogConstants.CANCEL_ID) != null)
			super.getButton(IDialogConstants.CANCEL_ID).setText(
				ButtonTextProvider.getCancelText());
		
		if (super.getButton(IDialogConstants.BACK_ID) != null)
			super.getButton(IDialogConstants.BACK_ID).setText(
				ButtonTextProvider.getBackText());
		
		if (super.getButton(IDialogConstants.NEXT_ID) != null)
			super.getButton(IDialogConstants.NEXT_ID).setText(
				ButtonTextProvider.getNextText());
	}
	
	/**
	 * Configura la <i>shell</i> en la que se abrir� el di�logo del asistente.
	 * 
	 * @param newShell <i>shell</i> utilizada por el di�logo del asistente.
	 */
	@Override
	protected void configureShell(Shell newShell){
		super.configureShell(newShell);
		
		newShell.setImage(ResourceManager.getPluginImage(RefactoringPlugin.getDefault(),
			"icons" + System.getProperty("file.separator") + "ref.png")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
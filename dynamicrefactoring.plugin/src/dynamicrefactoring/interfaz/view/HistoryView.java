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

package dynamicrefactoring.interfaz.view;

import dynamicrefactoring.RefactoringPlugin;
import dynamicrefactoring.RefactoringUndoSystem;

import dynamicrefactoring.domain.RefactoringSummary;
import dynamicrefactoring.listener.IRefactoringRunListener;
import dynamicrefactoring.reader.XMLRefactoringReaderException;
import dynamicrefactoring.writer.RefactoringPlanWriter;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IToolBarManager;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.layout.GridData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.widgets.Shell;


import com.swtdesigner.ResourceManager;

/**
 * Proporciona una vista de Eclipse en la que quedan reflejadas todas las 
 * refactorizaciones que ejecuta el <i>plugin</i> de refactorizaciones dinámicas.
 * 
 * <p>Proporciona información de resumen acerca de cada una de las refactorizaciones,
 * así como la posibilidad de ordenarlas por tipo de refactorización y por orden de
 * ejecución real.</p>
 * 
 * @author <A HREF="mailto:lfd0002@alu.ubu.es">Laura Fuente de la Fuente</A>
 * @author <A HREF="mailto:sfd0009@alu.ubu.es">Sonia Fuente de la Fuente</A>
 * @author <A HREF="mailto:ehp0001@alu.ubu.es">Enrique Herrero Paredes</A>
 */
public class HistoryView extends ViewPart {
	
	/**
	 * Elemento de registro de errores y otros eventos de la clase.
	 */
	private static final Logger logger = Logger.getLogger(HistoryView.class);

	/**
	 * Identificador de la vista.
	 */
	public static final String ID = "dynamicrefactoring.views.historyView"; //$NON-NLS-1$
	
	/**
	 * <i>Listener</i> a través del que la vista es notificada acerca de los 
	 * eventos relativos a las refactorizaciones realizadas.
	 */
	private IRefactoringRunListener listener;
	
	/**
	 * Tabla en que se muestran las refactorizaciones completadas.
	 */
	private Table table;
	
	/**
	 * Refactorizaciones cuya finalización ha registrado la vista.
	 * 
	 * <p>Se utiliza como clave la hora de finalización, y como valor, el 
	 * elemento resumen de la refactorización.</p>
	 */
	private HashMap<String, RefactoringSummary> elements;
	
	/**
	 * Descripción de la columna que mostrará el nombre de la refactorización. 
	 */
	private final String NAME_COLUMN = Messages.HistoryView_Refactoring;
	
	/**
	 * Descripción de la columna que mostrará la hora de finalización de la 
	 * refactorización.
	 */
	private final String TIME_COLUMN = Messages.HistoryView_Time;
	
	/**
	 * Descripción de la columna que permitirá seleccionar el punto en el que
	 * se quiere restaurar el estado del modelo.
	 */
	private final String UNDO_COLUMN = Messages.HistoryView_Undo;
	
	/**
	 * Propiedad asociada a los botones que indica en qué fila de la tabla se
	 * encuentran.
	 */
	private final String ROW_PROPERTY = "Row"; //$NON-NLS-1$
	
	/**
	 * Propiedad asociada a las filas de la tabla que indica qué botón tienen 
	 * asociado cada una.
	 */
	private final String BUTTON_PROPERTY = "Button"; //$NON-NLS-1$
	
	/**
	 * Columna en la que se muestran los botones que permiten deshacer.
	 */
	private TableColumn col_undo;
	
	/**
	 * Indica si una operación de deshacer se disparó desde la vista.
	 */
	private boolean viewFired = false;
	
	/**
	 * Crea los controles SWT para este componente del espacio de trabajo.
	 * 
	 * @param parent el control padre.
	 */
	@Override
	public void createPartControl(Composite parent) {
		try {
			this.setPartName(Messages.HistoryView_RefactoringHistory);
			
			elements = new HashMap<String, RefactoringSummary>();
			
			createTable(parent);

			// Se asocia un listener a la vista.
			listener = new HistoryListener();
			RefactoringPlugin.getDefault().addRefactoringListener(listener);
			
		}
		catch (Exception e){
			logger.error(Messages.HistoryView_ErrorOpening + ".\n" + //$NON-NLS-1$
				e.getMessage());
		}
		initializeToolBar();
	}
	
	/**
	 * Obtiene la ventana actual.
	 * @return ventana en la que se encuentra la vista.
	 */
	public Shell getShell(){
		return this.getViewSite().getShell();
	}
	
	/**
	 * Devuelve la vista a su estado inicial, vaciando la tabla y la tabla 
	 * asociativa de refactorizaciones almacenadas.
	 */
	public void cleanView(){
		cleanTable(table.getItemCount() - 1);
		
		elements = new HashMap<String, RefactoringSummary>();
	}
	
	/**
	 * Crea la tabla asociada a la vista.
	 * 
	 * @param parent el control padre.
	 * 
	 * @return la tabla asociada a la vista.
	 */
	private Table createTable(Composite parent){
		table = new Table(parent, SWT.SINGLE | SWT.HIDE_SELECTION | SWT.BORDER);
		table.setSortDirection(SWT.DOWN);
		table.setRedraw(true);
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 2;
		
		table.setLayoutData(gridData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		// Se crean tres columnas.
		TableColumn col_refactoring = new TableColumn(table, SWT.LEFT);
		col_refactoring.setToolTipText(Messages.HistoryView_NameCompleted);
		col_refactoring.setResizable(true);
		col_refactoring.setText(TIME_COLUMN);
		col_refactoring.setWidth(100);
					
		TableColumn col_time = new TableColumn(table, SWT.NONE);
		col_time.setToolTipText(Messages.HistoryView_TimeCompleted);
		col_time.setResizable(true);
		col_time.setText(NAME_COLUMN);
		col_time.setWidth(110);
				
		col_undo = new TableColumn(table, SWT.NONE);
		col_undo.setToolTipText(Messages.HistoryView_SelectPoint);
		col_undo.setResizable(false);
		col_undo.setText(UNDO_COLUMN);
						
		return table;
	}

	/**
	 * Elimina los recursos asociados a la vista antes de ocultarla si estaba
	 * visible.
	 * 
	 * <p>En particular, desactiva el <i>listener</i> asociado a la vista y 
	 * solicta al <i>plugin</i> que lo elimine de la lista de <i>plugins</i>
	 * registrados.</p>
	 */
	@Override
	public void dispose(){
		if (listener != null)
			RefactoringPlugin.getDefault().removeRefactoringListener(listener);
		listener = null;
	}
	
	/**
	 * Solicita a la vista que tome el foco en el espacio de trabajo.
	 */
	@Override
	public void setFocus() {
	}


	/**
	 * Permite responder a los eventos del proceso de refactorización disparados por
	 * el <i>plugin</i>.
	 * 
	 * <p>
	 * Desempeña el papel de Observador Concreto en el patrón de diseño Observador.
	 * </p>
	 * 
	 * <p>
	 * Responde a los eventos de finalización de refactorización registrando los 
	 * datos de la refactorización completada en la vista {@link HistoryView}.
	 * </p>
	 * 
	 * @author <A HREF="mailto:sfd0009@alu.ubu.es">Sonia Fuente de la Fuente</A>
	 * @author <A HREF="mailto:ehp0001@alu.ubu.es">Enrique Herrero Paredes</A>
	 */
	private class HistoryListener implements IRefactoringRunListener {
		
		/**
		 * Indica si la refactorización se ha completado correctamente.
		 */
		private boolean correct = true;
	
		
		/**
		 * Notifica al <i>listener</i> acerca del comienzo de una refactorización.
		 * 
		 * <p>Sin impelementación. Se trata de una vista de tipo histórico, por lo 
		 * que solo procesa refactorizaciones que se hayan completado.</p>
		 * 
		 * @param name nombre de la refactorización que ha comenzado.
		 * 
		 * @see IRefactoringRunListener#refactoringStarted(String)
		 */
		@Override
		public void refactoringStarted(String name){}
		
		/**
		 * Notifica al <i>listener</i> acerca de la realización de un paso concreto
		 * en una refactorización.
		 * 
		 * <p>Sin impelementación. Se trata de una vista de tipo histórico, por lo 
		 * que solo tiene en cuenta la finalización de refactorizaciones.</p>
		 * 
		 * @param message mensaje asociado al paso realizado.
		 * 
		 * @see IRefactoringRunListener#refactoringStepTaken(String)
		 */
		@Override
		public void refactoringStepTaken(String message){}
		
		/**
		 * Notifica al <i>listener</i> acerca de un fallo durante una refactorización.
		 * 
		 * @param name nombre de la refactorización que ha fallado.
		 * @param message mensaje asociado al fallo de la refactorización.
		 * 
		 * @see IRefactoringRunListener#refactoringFailed(String, String)
		 */
		@Override
		public void refactoringFailed(String name, String message){
			correct = false;
		}
	
		/**
		 * Notifica al <i>listener</i> acerca de la finalización de una refactorización.
		 * 	
		 * @param summary resumen informativo de la refactorización que ha finalizado.
		 * 	
		 * @see IRefactoringRunListener#refactoringFinished(RefactoringSummary)
		 */
		public void refactoringFinished(RefactoringSummary summary){
			if (correct){
				
				
				elements.put(summary.getStrCompletionTime(), summary);
				
				TableRunnable t = new TableRunnable();
				t.setSummary(summary);
				//de esta forma podemos llamar a el Runnable t desde otro Thread.
				getShell().getDisplay().asyncExec(t);
				
			}
			else
				correct = true;
		}
		
	
	
	
		/**
		 * Notifica al <i>listener</i> acerca del hecho de que se haya recuperado el
		 * estado anterior a una refactorización.
		 * 
		 * @param id identificador de la refactorización cuyo estado anterior
		 * se ha recuperado.
		 */
		@Override
		public void refactoringUndone(String id){
			if (!viewFired){
				// Se busca la refactorización que se ha deshecho.
				String time = ""; //$NON-NLS-1$
				for (RefactoringSummary summary : elements.values()){
					if (summary.getId().equals(id)){
						time = summary.getStrCompletionTime();
						//Se borra del fichero las refactorizaciones deshechas.
						try{
							RefactoringPlanWriter.getInstance().deleteRefactorings(summary.getName(),summary.getStrCompletionTime());
						}catch( XMLRefactoringReaderException e){
							logger.error(Messages.HistoryView_ErrorUpdating + ".\n" + //$NON-NLS-1$
									e.getMessage());
						}
						break;
					}
				}
				
				// Se busca la posición de la refactorización en la tabla.
				int index = 0;
				TableItem[] items = table.getItems();
				for (int i = 0; i < items.length; i++){
					if (items[i].getText(0).equals(time)){
						index = i;
						break;
					}
				}
				
				// Se limpia la tabla de ahí en adelante.
			cleanTable(index);
			}
		}
	}
	
	/**
	 * Elimina los elementos de la tabla que se encuentren entre la última 
	 * posición y la posición indicada, ésta última incluida.
	 * 
	 * @param toIndex posición de la primera fila de la tabla a partir de la que
	 * debe comenzar la eliminación de elementos. La numeración de las filas 
	 * comienza en 0, y la fila indicada será la primera en ser eliminada.
	 */
	private void cleanTable(int toIndex){
		if (toIndex < table.getItemCount() && toIndex >= 0){
			TableItem[] items = table.getItems();
			for (int i = items.length - 1; i >= toIndex; i--){
				// Primero se recupera el botón asociado a la fila y se elimina.
				Object button = items[i].getData(BUTTON_PROPERTY);
				if (button instanceof Button)
					((Button)button).dispose();
				items[i].dispose();
			}
		}
	}
	
	/**
	 * Permite responder a los eventos de los botones de la tabla que solicitan
	 * que el proyecto sea restaurado a un determinado punto anterior a una
	 * refactorización.
	 * 
	 * @author <A HREF="mailto:sfd0009@alu.ubu.es">Sonia Fuente de la Fuente</A>
	 * @author <A HREF="mailto:ehp0001@alu.ubu.es">Enrique Herrero Paredes</A>
	 */
	private class UndoSelectionListener implements SelectionListener {
		
		/**
		 * @see SelectionListener#widgetDefaultSelected(SelectionEvent)
		 */
		@Override
		public void widgetDefaultSelected(SelectionEvent event){
			widgetSelected(event);
		}
		
		/**
		 * Responde a los eventos de selección sobre uno de los botones de la
		 * tabla, deshaciendo la refactorización asociada al botón y eliminando
		 * de la tabla todos los estados posteriores al del punto recuperado.
		 * 
		 * @param event el evento de selección disparado sobre la ventana.
		 */
		@Override
		public void widgetSelected(SelectionEvent event){
			if (event.getSource() instanceof Button){
				Button button = (Button)event.getSource();
				int row = ((Integer)button.getData(ROW_PROPERTY)).intValue();
				
				TableItem item = table.getItem(row);
				String time = item.getText(0);
				
				// Se recupera el ID de la operación que ejecutó la refactorización.
				RefactoringSummary summary = elements.get(time);
				
				/*****************************undo*********************************/
			    //borramos del xml los datos de las refactorizaciones que se acaban de deshacer.
				try{
					RefactoringPlanWriter.getInstance().deleteRefactorings(summary.getName(),summary.getStrCompletionTime());
				}catch( XMLRefactoringReaderException e){
					logger.error(Messages.HistoryView_ErrorUpdating + ".\n" + //$NON-NLS-1$
							e.getMessage());
				}
				/*****************************************************************/
				
				try {
					cleanTable(row);
					viewFired = true;
					RefactoringUndoSystem.undoRefactoring(summary.getId(), true);
					viewFired = false;
				}
				catch (Exception exception){
					viewFired = false;
					MessageDialog.openError(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						Messages.HistoryView_Error, exception.getMessage());
				}				
			}
		}
	}
	
	/**
	 * Permite actualizar la tabla de refactorizaciones ejecutadas cada vez que se ejecuta
	 * una refactorización.
	 * 
	 * @author <A HREF="mailto:lfd0002@alu.ubu.es">Laura Fuente de la Fuente</A>
	 * @author <A HREF="mailto:sfd0009@alu.ubu.es">Sonia Fuente de la Fuente</A>
	 * @author <A HREF="mailto:ehp0001@alu.ubu.es">Enrique Herrero Paredes</A>
	 *
	 */
	private class TableRunnable implements Runnable{
		/**
		 * Resumen de la refactorización que se está ejecutando.
		 */
		private RefactoringSummary summary;
		
		/**
		 * Establece el resumen de la refactorización.
		 * 
		 * @param summary resumen de la refactorización.
		 */
		public void setSummary(RefactoringSummary summary){
			this.summary=summary;
		}
		
		/**
		 * Permite actualizar la tabla de refactorizaciones ejecutadas.
		 */
		public void run(){
			TableItem item = new TableItem(table, SWT.BORDER);
			item.setText(new String[]{summary.getStrCompletionTime(), 
				summary.getName(), ""}); //$NON-NLS-1$
			item.setForeground(
				Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
			

			final TableEditor editor = new TableEditor(table);
			Button bt_Undo = new Button(table, SWT.NONE);
			bt_Undo.setImage(ResourceManager.getPluginImage(
				RefactoringPlugin.getDefault(), 
				"icons" + System.getProperty("file.separator") + "undo.ico")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			bt_Undo.addSelectionListener(new UndoSelectionListener());
			bt_Undo.setData(ROW_PROPERTY, table.indexOf(item));
			bt_Undo.pack();
			item.setData(BUTTON_PROPERTY, bt_Undo);
			editor.minimumWidth = bt_Undo.getSize().x;
			editor.minimumHeight = bt_Undo.getSize().y;
			editor.horizontalAlignment = SWT.CENTER;
			editor.setEditor(bt_Undo, item, 2);
			if (table.getItemCount() == 1){
				col_undo.setWidth(editor.minimumWidth);
				// Se aumenta la altura de las filas.
				table.addListener(SWT.MeasureItem, new Listener() {
					public void handleEvent(Event event) {
						event.height = editor.minimumHeight;
					}
				});
			}
			table.redraw();
		}
}
	/**
	 * Inicializa la barra de herramientas.
	 */
	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
	}
}
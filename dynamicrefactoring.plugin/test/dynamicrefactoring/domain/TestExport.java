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

package dynamicrefactoring.domain;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import dynamicrefactoring.util.io.FileManager;
import dynamicrefactoring.RefactoringConstants;
import dynamicrefactoring.domain.ExportImportUtilities;
import dynamicrefactoring.reader.JDOMXMLRefactoringReaderImp;
import dynamicrefactoring.reader.XMLRefactoringReaderException;

import static org.junit.Assert.*;

/**
 * Comprueba que funciona correctamente el proceso de exportaci�n de
 * refactorizaciones.
 * 
 * @author <A HREF="mailto:lfd0002@alu.ubu.es">Laura Fuente de la Fuente</A>
 * 
 */
public class TestExport {

	/**
	 * Comprueba que el proceso de exportaci�n de la refactorizaci�n din�mica
	 * Rename Class a un directorio temporal "./temp" se ha realizado
	 * correctamente.
	 * 
	 * @throws XMLRefactoringReaderException
	 *             XMLRefactoringReaderException.
	 * @throws IOException
	 *             IOException.
	 */
	@Test
	public void testExportRefactoring() throws XMLRefactoringReaderException,
			IOException {
		String destination = FilenameUtils.separatorsToSystem(".\\temp");
		FileManager.createDir(destination);

		String definition = FilenameUtils
				.separatorsToSystem(RefactoringConstants.DYNAMIC_REFACTORING_DIR
						+ "\\Rename Class\\Rename Class.xml");

		// Primero exportamos la refactorizaci�n Rename Class a un directorio
		// temporal que luego eliminaremos
		ExportImportUtilities.ExportRefactoring(destination, definition, false);

		JDOMXMLRefactoringReaderImp reader = new JDOMXMLRefactoringReaderImp(
				new File(definition));

		for (String element : reader.readMechanismRefactoring()) {

			String name = FilenameUtils.getName(ExportImportUtilities
					.splitGetLast(element, "."));
			String namefolder = FilenameUtils.getName(new File(definition)
					.getParent());

			File resultFile = new File(destination + File.separatorChar
					+ namefolder + File.separatorChar + name + ".class");
			assertEquals(true, resultFile.exists());
		}

		// Borramos el directorio temporal al final del test
		FileManager.emptyDirectories(destination);
		FileManager.deleteDirectories(destination, true);
	}

	/**
	 * Comprueba que el proceso de exportaci�n de la refactorizaci�n din�mica
	 * Rename Class a un directorio temporal "./temp" teniendo en cuenta que uno
	 * de los ficheros .class requeridos no se encuentra en el repositorio.
	 * 
	 * @throws XMLRefactoringReaderException
	 *             XMLRefactoringReaderException.
	 * @throws IOException
	 *             IOException.
	 */
	@Test(expected = IOException.class)
	public void testExportFileNotExists() throws XMLRefactoringReaderException,
			IOException {
		
		String destination = FilenameUtils.separatorsToSystem(".\\temp");
		String refactoringName = "RenameClass.class";
		
		FileManager.createDir(destination);
		String definition = FilenameUtils.separatorsToSystem(RefactoringConstants.DYNAMIC_REFACTORING_DIR
				+ "\\Rename Class\\Rename Class.xml");

		File definitionFile = new File(definition);
		File definitionFolder = definitionFile.getParentFile();
		String definitionFolderName = definitionFolder.getName();
		String ficheroOrigen = FilenameUtils.separatorsToSystem(
		".\\bin\\repository\\moon\\concreteaction\\" + refactoringName);

		try {
			// Copiamos uno de los ficheros .class que necesita la
			// refactorizaci�n al directorio
			// temporal y luego lo borramos para que posteriormente salte la
			// excepci�n.
			

			FileUtils.copyFileToDirectory(new File(ficheroOrigen),new File(destination));
			FileManager.deleteFile(ficheroOrigen);

			ExportImportUtilities.ExportRefactoring(destination, definition,false);

		} catch (IOException e) {
			// Comprobamos que el directorio en el que se generar�a la
			// refactorizaci�n no existe al no
			// poderse completar la operaci�n.
			assertEquals(false,
					new File(destination + File.separatorChar + definitionFolderName).exists());

			// Reponemos el fichero .class que hab�amos borrado para comprobar
			// que saltaba la
			// excepci�n.
			
			FileManager.copyFile(new File(destination + File.separatorChar + refactoringName),
							new File(ficheroOrigen));

			assertEquals(true,new File(ficheroOrigen).exists());

			// Borramos el directorio temporal al final del test
			FileManager.emptyDirectories(destination);
			FileManager.deleteDirectories(destination, true);
			throw e;
		}
	}

}

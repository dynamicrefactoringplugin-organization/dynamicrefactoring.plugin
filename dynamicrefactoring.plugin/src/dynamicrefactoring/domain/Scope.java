package dynamicrefactoring.domain;

import java.util.Arrays;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import dynamicrefactoring.interfaz.Messages;

import moon.core.ObjectMoon;
import moon.core.classdef.AttDec;
import moon.core.classdef.ClassDef;
import moon.core.classdef.FormalArgument;
import moon.core.classdef.MethDec;
import moon.core.genericity.BoundS;
import moon.core.genericity.FormalPar;
import moon.core.instruction.CodeFragment;

public enum Scope {
	/**
	 * �mbito de clase.
	 */
	SCOPE_CLASS (ClassDef.class, Messages.SelectRefactoringWindow_ClassScope),
	/**
	 * �mbito de m�todo.
	 */
	SCOPE_METHOD (MethDec.class, Messages.SelectRefactoringWindow_MethodScope),
	/**
	 * �mbito de atributo.
	 */
	SCOPE_ATTRIBUTE (AttDec.class, Messages.SelectRefactoringWindow_FieldScope),
	/**
	 * �mbito de argumento formal.
	 */
	SCOPE_FORMAL_ARG (FormalArgument.class, Messages.SelectRefactoringWindow_FormalArgumentScope),
	/**
	 * �mbito de par�metro formal.
	 */
	SCOPE_FORMAL_PAR (FormalPar.class, Messages.SelectRefactoringWindow_FormalParameterScope),
	/**
	 * �mbito de par�metro formal acotado.
	 */
	SCOPE_BOUNDED_PAR (BoundS.class, Messages.SelectRefactoringWindow_BoundedParameterScope),
	/**
	 * �mbito de bloque de texto.
	 */
	SCOPE_CODE_FRAGMENT (CodeFragment.class, Messages.SelectRefactoringWindow_CodeFragmentScope);
	
	
	/**
	 * Clase de JavaMoon que se corresponde con el ambito.
	 */
	private final Class<? extends ObjectMoon> correspondingClass;
	
	/**
	 * Descripci�n del ambito.
	 */
	private final String description;
	
	/**
	 * Obtiene la clase de moon que se corresponde con el 
	 * �mbito declarado.
	 * 
	 * @return the corresponding class
	 */
	private Class<? extends ObjectMoon> getCorrespondingClass() {
		return correspondingClass;
	}

	/**
	 * Descripci�n del ambito.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Constructor de un �mbito.
	 * 
	 * @param correspondinClass clase de moon que se corresponde con el �mbito
	 */
	Scope(final Class<? extends ObjectMoon> correspondinClass, final String description){
		this.correspondingClass = correspondinClass;
		this.description = description;
	}
	
	/**
	 * Dada una clase de Moon obtiene el ambito que se corresponde
	 * con la misma
	 * @param correspondingClass
	 * @return ambito que se corresponde con la clase
	 */
	public static final Scope getScopeForObjectMoonClass(final Class<? extends ObjectMoon> correspondingClass){
		return Collections2.filter(Arrays.asList(Scope.values()), new Predicate<Scope>(){

			@Override
			public boolean apply(Scope arg0) {
				return arg0.correspondingClass.equals(correspondingClass);
			}
			
		}).iterator().next();
	}
	
}
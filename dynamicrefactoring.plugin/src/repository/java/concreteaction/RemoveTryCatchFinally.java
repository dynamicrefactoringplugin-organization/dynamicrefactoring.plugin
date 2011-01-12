package repository.java.concreteaction;

import java.util.ArrayList;
import java.util.List;

import javamoon.core.DefinitionLanguage;
import javamoon.core.classdef.JavaType;
import javamoon.core.entity.JavaFunctionDec;
import javamoon.core.entity.JavaRoutineDec;
import javamoon.core.entity.JavaThrows;
import javamoon.core.instruction.JavaInstrNoMoon;
import moon.core.classdef.ClassType;
import moon.core.classdef.MethDec;
import moon.core.instruction.CompoundInstr;
import moon.core.instruction.Instr;
import refactoring.engine.Action;

/**
 * Removes the try-catch-finally blocks.
 * 
 * @author Ra�l Marticorena
 *
 */
public class RemoveTryCatchFinally  extends Action{

	/**
	 * Method.
	 */
	private MethDec methDec;
	
	/**
	 * Undo list.
	 */
	private List<Instr> undoInstructions;
	
	/**
	 * Constructor.
	 * 
	 * @param methDec method
	 */
	public RemoveTryCatchFinally(MethDec methDec){
		this.methDec = methDec;
	}
	
	/**
	 * Run.
	 */
	@Override
	public void run() {
		List<Instr> list = methDec.getInstructions();
		undoInstructions = methDec.copy();
		/*
		for (Instr instr : list) {			
			if (instr instanceof CompoundInstr) {			
				remove((CompoundInstr) instr);
			}
		}*/
		remove((CompoundInstr) list.get(0));
	}

	/**
	 * Undo.
	 */
	@Override
	public void undo() {
		methDec.setInstructions(undoInstructions);			
	}
	
	/**
	 * Removes the try-catch-finally.
	 * 
	 * @param compoundInstr compound instruction
	 */
	private void remove(CompoundInstr compoundInstr){
		
		List<Instr> listAux = compoundInstr.getInstructions();
		List<Instr> list = new ArrayList<Instr>(listAux);
		for (int i = 0; i<list.size(); i++){
			Instr instr = list.get(i);
			
			if (instr instanceof CompoundInstr){
				remove((CompoundInstr)instr);
			}
			else{		
				// Remove try block
				if (instr.toString().equals(DefinitionLanguage.TRY)){					
					compoundInstr.removeInstruction(instr); // try
					//Instr left = list.get(i+1);					
					//compoundInstr.removeInstruction(left); // {
					//int j = 2;
					//while(!(list.get(i+j).toString().equals(DefinitionLanguage.END))){	
					//	j++;					
					//}
					//Instr right = list.get(i+j);
					//compoundInstr.removeInstruction(right); // }
					this.cleanBraces((CompoundInstr)list.get(i+1));
				}
				else if (instr.toString().equals(DefinitionLanguage.CATCH)){					
					compoundInstr.removeInstruction(instr); // catch
					Instr left = list.get(i+1);					
					compoundInstr.removeInstruction(left); // (
					Instr local = list.get(i+2);					
					compoundInstr.removeInstruction(local); // Exception ex
					Instr right = list.get(i+3);					
					compoundInstr.removeInstruction(right); // )
					//Instr leftP = list.get(i+4);					
					//compoundInstr.removeInstruction(leftP); // {					
					//int j = 5;
					//while(!(list.get(i+j).toString().equals(DefinitionLanguage.END))){						
					//	j++;					
					//}
					//Instr rightP = list.get(i+j);					
					//compoundInstr.removeInstruction(rightP); // }
					this.cleanBraces((CompoundInstr)list.get(i+4));
				}
				// Remove finally block
				else if (instr.toString().equals(DefinitionLanguage.FINALLY)){					
					compoundInstr.removeInstruction(instr); // finally
					//Instr left = list.get(i+1); 					
					//compoundInstr.removeInstruction(left); // {
					//int j = 2;
					//while(!(list.get(i+j).toString().equals(DefinitionLanguage.END))){					
					//	j++;					
					//}
					//Instr right = list.get(i+j);
					//compoundInstr.removeInstruction(right); // }
					this.cleanBraces((CompoundInstr)list.get(i+1));
				}
			}
		}
	}
	
	/**
	 * cleanBraces.
	 * @param instr instr
	 */
	private void cleanBraces(CompoundInstr instr){
		Instr beginBrace = instr.getInstructions().get(0);
		Instr endBrace = instr.getInstructions().get(instr.getInstructions().size()-1);
		instr.removeInstruction(beginBrace);
		instr.removeInstruction(endBrace);		
	}
}

	
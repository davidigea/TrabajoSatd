/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ej2;
import jade.core.Agent;

/**
 *
 * @author goyo
 */
public class Ej2_Envia extends Agent{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected void setup(){
    	int arg = Integer.valueOf((String) getArguments()[0]);
    	
        System.out.println("Hola, soy el agente " + getLocalName());
        
        addBehaviour(new Ej2_Envia_Behaviour(arg));       
    }
}

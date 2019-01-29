/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ej2;
import java.util.Scanner;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author 697589
 */
public class Ej2_Envia_Behaviour_mod extends CyclicBehaviour{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Scanner teclado;
    boolean empezar;
    int veces;
    
    public Ej2_Envia_Behaviour_mod(int n)
    {
    	super();
    	teclado = new Scanner (System.in);
    	empezar = false;
    	veces = n;
    }
    
    //@Override
    public void action() {
    	String name, texto;
    	ACLMessage msg;
    	AID p;
    	
    	//Comprueba el valor del contador para saber si debe morirse o no
    	if (veces >= 0) {
    		//Este bloque "if" solo se ejecuta cuando el agente manda el mensaje por primera vez
	    	if (!empezar) {
		        System.out.println("Nombre del Agente al que va dirigido el mensaje: ");
		        name = teclado.nextLine();
		        System.out.println("Introduce el mensaje: ");
		        texto = teclado.nextLine();
		        
		        //Se adapta el mensaje para incluir el contador seguido, del símbolo ";" y del mensaje tecleado por el usuario
		        texto = String.valueOf(veces) + ";" + texto;
		        
		        //Se especifica el tipo de petición del mensaje
		        msg = new ACLMessage(ACLMessage.REQUEST);
		        
		        //Crea un "objeto" que representa al receptor del mensaje
		        p = new AID(name, AID.ISLOCALNAME);
		        //Añade ese receptor a la lista de destinatarios del mensaje
		        msg.addReceiver(p);
		        //Añade lo tecleado por el usuario como contenido del mensaje
		        msg.setContent(texto);
		        
		        System.out.println("Ronda número " + veces);
		        System.out.println("======================");
		        System.out.println();
		        
		        System.out.println("EMISOR: voy a enviar el mensaje " + msg.getContent() + " a " + name);
		        //Envía el mensaje
		        this.myAgent.send(msg);
		        empezar = true;
	    	}
	    	else {
	    		//El agente espera a que le llegue un mensaje
	    		msg = this.myAgent.blockingReceive();
	    		
	            if (msg != null) {
	            	//Si el mensaje no es nulo, escribe por pantalla el nombre del emisor del mensaje recibido y su contenido
	                System.out.println(" EMISOR >>> Mensaje recibido del Agente " + msg.getSender().getLocalName());
	                System.out.println(" Que dice >>> "+ msg.getContent());
	            }
	            if (veces == 0) {
	        		//El agente emisor escribe su nombre por pantalla y se muere
	        		System.out.println("El agente " + this.myAgent.getLocalName() + " dice: adiosito");
	        		this.myAgent.doDelete();
	    		}
	            else {
		            //Se adapta el mensaje para incluir el contador seguido, del símbolo ";" y del mensaje tecleado por el usuario
		    		texto = msg.getContent();
		    		texto = String.valueOf(veces) + ";" + texto;
		            name = msg.getSender().getLocalName();
		    		//Crea un "objeto" que representa al receptor del mensaje
		            p = new AID(name, AID.ISLOCALNAME);
		            //Añade ese receptor a la lista de destinatarios del mensaje
		            msg = new ACLMessage(ACLMessage.REQUEST);
		            msg.addReceiver(p);
			        //Añade lo tecleado por el usuario como contenido del mensaje
		            msg.setContent(texto);
		            
		            System.out.println();
		            System.out.println("Ronda número " + veces);
			        System.out.println("======================");
			        System.out.println();
		            
		            System.out.println("EMISOR: voy a reenviar el mensaje " + texto + " a " + name);
			        //Envía el mensaje
		            this.myAgent.send(msg);
	            }
	    	}
	    	//Decrementa el contador
            veces--;
	    }
    }
}

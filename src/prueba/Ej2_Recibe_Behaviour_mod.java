/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ej2;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author 697589
 */
public class Ej2_Recibe_Behaviour_mod extends CyclicBehaviour{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int veces;
	
	public Ej2_Recibe_Behaviour_mod() {
		super();
		veces = 0;
	}
	
    public void action() {
        // Se recibe el mensaje del emisor
	    ACLMessage msg;
	    String[] aux;
    	String name;
    	AID p;
    	
    	msg = this.myAgent.blockingReceive();
	    //if (msg != null) {
	    	//Si el mensaje es no nulo, se parsea para obtener el contador actual 
	    	aux = msg.getContent().split(";");
	    	veces = Integer.parseInt(aux[0]);
	    	//System.out.println("EMISOR: VECES = " + veces);
        //}	    
	    //Comprueba el contador
	    if (veces > 0) {    
	    	//if (msg != null) {
	    		//Si el mensaje no es nulo, escribe por pantalla el nombre del emisor del mensaje recibido y su contenido
	            System.out.println(" RECEPTOR >>> Mensaje recibido del Agente " + msg.getSender().getLocalName());
	            System.out.println(" Que dice >>> "+ aux[1]);
	        //}
	        name = msg.getSender().getLocalName();
	        msg = new ACLMessage(ACLMessage.REQUEST);
	        
	    	//Crea un "objeto" que representa al receptor del mensaje        
	        p = new AID(name, AID.ISLOCALNAME);
	        //Añade ese receptor a la lista de destinatarios del mensaje
	        msg.addReceiver(p);
	        //Añade lo tecleado por el usuario como contenido del mensaje
	        msg.setContent(aux[1]);
	        System.out.println("RECEPTOR: voy a reenviar el mensaje " + aux[1] + " a " + name);
	        //Envía el mensaje
	        this.myAgent.send(msg);
    	}
    }
}

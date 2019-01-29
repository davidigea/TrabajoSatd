/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ej2;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author goyo
 */
public class Ej2_Recibe_Behaviour extends CyclicBehaviour{
    public void action() {
        
        ACLMessage msg = this.myAgent.blockingReceive();
        
        if (msg != null)
        {
            System.out.println(" Mensaje recibido del Agente " + msg.getSender().getLocalName());
            System.out.println(" Que dice >>> "+ msg.getContent());
        }
        
    }
}

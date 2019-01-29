/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prueba;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Scanner;
import jade.core.AID;

/**
 *
 * @author goyo
 */
public class Ej2_Envia_Behaviour extends CyclicBehaviour{
    Scanner teclado;
    
    public Ej2_Envia_Behaviour()
    {
      teclado = new Scanner (System.in);
    }
    
    //@Override
    public void action() {
        
        System.out.println("Nombre del Agente al que va dirigido el mensaje: ");
        String name = teclado.nextLine();
        System.out.println("Introduce el mensaje: ");
        String texto = teclado.nextLine();
        
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        
        AID p = new AID(name, AID.ISLOCALNAME);
        msg.addReceiver(p);
        msg.setContent(texto);
        
        this.myAgent.send(msg);
    }
}

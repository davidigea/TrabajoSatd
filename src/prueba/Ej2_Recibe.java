/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prueba;
import jade.core.Agent;
import utils.Tools;

/**
 *
 * @author goyo
 */
public class Ej2_Recibe extends Agent{
    protected void setup(){
        System.out.println("Hola, soy el agente " + getLocalName());
        Tools.registrarServicio(this, "Recibidor");
        addBehaviour(new Ej2_Recibe_Behaviour());
    }
}
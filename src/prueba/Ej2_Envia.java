package prueba;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import jade.core.AID;
import jade.core.Agent;
import utils.Tools;

import java.util.ArrayList;

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
        System.out.println("Hola, soy el agente " + getLocalName());
		ArrayList<AID> agentes = Tools.BuscarAgentes(this,"Recibidor");

		System.out.println(agentes.size());
		System.out.println(agentes.get(0));

        addBehaviour(new Ej2_Envia_Behaviour());
    }
}

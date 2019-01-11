package comportamiento;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import utils.Tools;
import weka.core.Instances;

import java.io.IOException;
import java.util.ArrayList;

public class Particionador_behaviour extends Behaviour {
    int percentage;
    Instances datos;

    public Particionador_behaviour(int percentage){
        this.percentage = percentage;
    }

    @Override
    public void action() {
        ACLMessage respuestaFichero = null;
        Instances datosEntrenamiento = null;
        Instances datosTest = null;
        String nombreReceptor = "";

        //pedir fichero
        ArrayList<AID> candidatos = Tools.BuscarAgentes(this.myAgent, "lector");
        int elegido = (int) Math.random()*((candidatos.size()-1)+1);
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        AID agenteLector = new AID(candidatos.get(elegido).getLocalName(), AID.ISLOCALNAME);
        msg.addReceiver(agenteLector);
        this.myAgent.send(msg);
        System.out.println("Soy el agente " + this.myAgent.getLocalName() + " y acabo de enviar una " +
                            "petición de fichero CSV al agente " + agenteLector.getLocalName());

        //recibir fichero
        while (!nombreReceptor.equals("lector")) {
            respuestaFichero = this.myAgent.blockingReceive();
            nombreReceptor = respuestaFichero.getSender().getLocalName();
        }
        System.out.println("Soy el agente " + this.myAgent.getLocalName() + " y acabo de recibir un" +
                " fichero CSV del agente " + agenteLector.getLocalName());

        try {
            datos = (Instances) respuestaFichero.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }

        //particionar fichero
        //int tamanyoTrozoEntrenamiento = datos.size()*percentage/100;
        //int tamanyoTrozoTest = datos.size() - tamanyoTrozoEntrenamiento;
        //datosEntrenamiento = new Instances(datos, 0, tamanyoTrozoEntrenamiento);
        //datosTest = new Instances(datos, tamanyoTrozoEntrenamiento, tamanyoTrozoTest);

        while (true) {
            //esperar petición de particiones
            ACLMessage peticionParticionar = this.myAgent.blockingReceive();
            System.out.println("Soy el agente " + this.myAgent.getLocalName() + " y acabo de recibir una petición" +
                    " de datos de entrenamiento del agente " + peticionParticionar.getSender());

            //enviar fichero
            ACLMessage mensajeParticiones = new ACLMessage(ACLMessage.REQUEST);
            AID agenteNaiveBayes = new AID(peticionParticionar.getSender().getLocalName(), AID.ISLOCALNAME);
            mensajeParticiones.addReceiver(agenteNaiveBayes);

            try {
                mensajeParticiones.setContentObject(new Object[]{datosEntrenamiento, datosTest});
                this.myAgent.send(mensajeParticiones);

                Thread.sleep(1000000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean done() {
        return false;
    }
}

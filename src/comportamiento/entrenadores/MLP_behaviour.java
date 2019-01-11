package comportamiento.entrenadores;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import utils.Tools;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instances;

import java.io.IOException;
import java.util.ArrayList;

public class MLP_behaviour extends Behaviour {
    @Override
    public void action() {
        Instances datosEntrenamiento = null;
        Instances datosTest = null;
        Object[] contenidoRespuesta = null;
        Classifier clasificadorMLP = new MultilayerPerceptron();
        Evaluation evaluadorMLP = null;

        //pedir particiones
        ArrayList<AID> candidatos = Tools.BuscarAgentes(this.myAgent, "particionador");
        int elegido = (int) Math.random()*((candidatos.size()-1)+1);
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        AID agenteParticionador = new AID(candidatos.get(elegido).getLocalName(), AID.ISLOCALNAME);
        msg.addReceiver(agenteParticionador);
        this.myAgent.send(msg);
        System.out.println("Soy el agente " + this.myAgent.getLocalName() + " y acabo de enviar una petición de particiones");

        //recibir particiones
        ACLMessage respuestaFichero = this.myAgent.blockingReceive();
        try {
            contenidoRespuesta = (Object[]) respuestaFichero.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        datosEntrenamiento = (Instances) contenidoRespuesta[0];
        datosTest = (Instances) contenidoRespuesta[1];

        //crear clasificador y evaluador bayesianos
        try {
            clasificadorMLP.buildClassifier(datosEntrenamiento);
            evaluadorMLP = new Evaluation(datosEntrenamiento);
            evaluadorMLP.evaluateModel(clasificadorMLP, datosTest);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while(true){
            //esperar petición de entrenamiento
            ACLMessage peticionEntrenar = this.myAgent.blockingReceive();

            //enviar entrenamiento
            ACLMessage mensajeEntrenamiento = new ACLMessage(ACLMessage.REQUEST);
            AID agenteCalculadorMedia = new AID(peticionEntrenar.getSender().getLocalName(), AID.ISLOCALNAME);
            mensajeEntrenamiento.addReceiver(agenteCalculadorMedia);
            try {
                mensajeEntrenamiento.setContentObject(new Object[]{evaluadorMLP});
                this.myAgent.send(mensajeEntrenamiento);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean done() {
        return false;
    }
}

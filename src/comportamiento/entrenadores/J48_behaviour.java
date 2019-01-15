package comportamiento.entrenadores;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import utils.Tools;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;

import java.io.IOException;
import java.util.ArrayList;

public class J48_behaviour extends Behaviour {
    @Override
    public void action() {
        Instances datosEntrenamiento = null;
        Instances datosTest = null;
        Object[] contenidoRespuesta = null;
        Classifier clasificadorJ48 = new J48();
        Evaluation evaluadorJ48 = null;
        double[] estadisticas = {};


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
            clasificadorJ48.buildClassifier(datosEntrenamiento);
            evaluadorJ48 = new Evaluation(datosEntrenamiento);
            evaluadorJ48.evaluateModel(clasificadorJ48, datosTest);
            estadisticas[0] = evaluadorJ48.weightedTruePositiveRate();
            estadisticas[1] = evaluadorJ48.weightedFalsePositiveRate();
            estadisticas[2] = evaluadorJ48.weightedTrueNegativeRate();
            estadisticas[3] = evaluadorJ48.weightedFalseNegativeRate();
            estadisticas[4] = evaluadorJ48.weightedRecall();
            estadisticas[5] = evaluadorJ48.weightedPrecision();
            estadisticas[6] = evaluadorJ48.weightedFMeasure();
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
                mensajeEntrenamiento.setContentObject(new Object[]{estadisticas});
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

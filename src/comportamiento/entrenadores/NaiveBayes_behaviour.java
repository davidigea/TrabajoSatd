package comportamiento.entrenadores;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import utils.Tools;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

import java.io.IOException;
import java.util.ArrayList;

public class NaiveBayes_behaviour extends Behaviour {
    @Override
    public void action() {
        ACLMessage respuestaParticion = null;
        Instances datosEntrenamiento;
        Instances datosTest;
        String nombreReceptor = "";
        Object[] contenidoRespuesta = null;
        Classifier clasificadorBayes = new NaiveBayes();
        Evaluation evaluadorBayes = null;
        boolean atendido = false;
        ACLMessage msgRecibido;
        double[] estadisticas = {};

        //pedir particiones
        ArrayList<AID> candidatos = Tools.BuscarAgentes(this.myAgent, "particionador");
        int elegido = (int) Math.random()*((candidatos.size()-1)+1);
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        AID agenteParticionador = new AID(candidatos.get(elegido).getLocalName(), AID.ISLOCALNAME);
        msg.addReceiver(agenteParticionador);
        this.myAgent.send(msg);

        while (!atendido) {
            msgRecibido = this.myAgent.blockingReceive();

            if(msgRecibido.getPerformative() != ACLMessage.REFUSE) {
                atendido = true;
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.myAgent.send(msg);
            }
        }


        System.out.println("Soy el agente " + this.myAgent.getLocalName() + " y acabo de enviar una " +
                            "petición de particiones al agente " + agenteParticionador.getLocalName());

        //recibir particiones
        while (!nombreReceptor.equals("particionador")) {
            respuestaParticion = this.myAgent.blockingReceive();
            nombreReceptor = respuestaParticion.getSender().getLocalName();
        }
        System.out.println("Soy el agente " + this.myAgent.getLocalName() + " y acabo de recibir " +
                            "particiones de datos del agente " + agenteParticionador.getLocalName());
        try {
            contenidoRespuesta = (Object[]) respuestaParticion.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        datosEntrenamiento = (Instances) contenidoRespuesta[0];
        datosTest = (Instances) contenidoRespuesta[1];

        System.out.println("Tamaño de los datos de entrenamiento en bayes:");
        System.out.println(datosEntrenamiento.size());

        //crear clasificador y evaluador bayesianos
        try {
            clasificadorBayes.buildClassifier(datosEntrenamiento);
            evaluadorBayes = new Evaluation(datosEntrenamiento);
            evaluadorBayes.evaluateModel(clasificadorBayes, datosTest);
            estadisticas[0] = evaluadorBayes.weightedTruePositiveRate();
            estadisticas[1] = evaluadorBayes.weightedFalsePositiveRate();
            estadisticas[2] = evaluadorBayes.weightedTrueNegativeRate();
            estadisticas[3] = evaluadorBayes.weightedFalseNegativeRate();
            estadisticas[4] = evaluadorBayes.weightedRecall();
            estadisticas[5] = evaluadorBayes.weightedPrecision();
            estadisticas[6] = evaluadorBayes.weightedFMeasure();
        } catch (Exception e) {
            e.printStackTrace();
        }

        while(true){
            //esperar petición de resultados de entrenamiento
            ACLMessage peticionEntrenar = this.myAgent.blockingReceive();

            //enviar resultados de entrenamiento
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

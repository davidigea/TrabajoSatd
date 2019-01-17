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
import java.util.Random;

public class MLP_behaviour extends Behaviour {
    @Override
    public void action() {
        Instances datosEntrenamiento;
        Instances datosTest;
        Object[] contenidoRespuesta = null;
        Classifier clasificadorMLP = new MultilayerPerceptron();
        Evaluation evaluadorMLP;
        ACLMessage msgRecibido = null;
        double[] estadisticas = new double[7];
        ArrayList<Integer> agentesUsados = new ArrayList<>();
        AID agenteParticionador = null;

        boolean conseguidosDatos = false;
        while(!conseguidosDatos){
            //pedir datos de partición
            ArrayList<AID> candidatos = Tools.BuscarAgentes(this.myAgent, "particionador");
            boolean elegidoNuevo = false;
            while (!elegidoNuevo) {
                Random r = new Random();
                int elegido = r.nextInt(candidatos.size());
                if (!agentesUsados.contains(elegido)) {
                    agenteParticionador = new AID(candidatos.get(elegido).getLocalName(), AID.ISLOCALNAME);
                    elegidoNuevo = true;
                    agentesUsados.add(elegido);
                }
            }

            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(agenteParticionador);
            this.myAgent.send(msg);
            System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                    this.myAgent.getLocalName(),"ENV","Petición Datos", agenteParticionador.getLocalName());

            //recibir datos de partición
            boolean recibeDatos = false;
            boolean caminoEquivocado = false;
            while (!recibeDatos && !caminoEquivocado) {
                msgRecibido = this.myAgent.blockingReceive();
                String nombreReceptor = msgRecibido.getSender().getLocalName();
                if(nombreReceptor.equals(agenteParticionador.getLocalName())){
                    if(msgRecibido.getPerformative() == ACLMessage.REFUSE) {
                        System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                                this.myAgent.getLocalName(),"REC","Rechazo", nombreReceptor);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        this.myAgent.send(msg);
                        System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                                this.myAgent.getLocalName(),"ENV","Petición Datos", nombreReceptor);
                    } else if(msgRecibido.getPerformative() == ACLMessage.CANCEL) {
                        caminoEquivocado = true;
                    } else {
                        recibeDatos = true;
                        System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                                this.myAgent.getLocalName(),"REC","Datos", nombreReceptor);
                    }
                } else {
                    System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                            this.myAgent.getLocalName(),"REC","Petición A Destiempo", nombreReceptor);
                    ACLMessage rechazo = new ACLMessage(ACLMessage.REFUSE);
                    rechazo.addReceiver(new AID(nombreReceptor, AID.ISLOCALNAME));
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    this.myAgent.send(rechazo);
                    System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                            this.myAgent.getLocalName(),"ENV","Rechazo", nombreReceptor);
                }
            }
            conseguidosDatos = recibeDatos;
        }
        try {
            contenidoRespuesta = (Object[]) msgRecibido.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        datosEntrenamiento = (Instances) contenidoRespuesta[0];
        datosTest = (Instances) contenidoRespuesta[1];
        int percentage = (int) contenidoRespuesta[2];

        //crear clasificador y evaluador MLPianos
        try {
            clasificadorMLP.buildClassifier(datosEntrenamiento);
            evaluadorMLP = new Evaluation(datosEntrenamiento);
            evaluadorMLP.evaluateModel(clasificadorMLP, datosTest);
            estadisticas[0] = evaluadorMLP.weightedTruePositiveRate();
            estadisticas[1] = evaluadorMLP.weightedFalsePositiveRate();
            estadisticas[2] = evaluadorMLP.weightedTrueNegativeRate();
            estadisticas[3] = evaluadorMLP.weightedFalseNegativeRate();
            estadisticas[4] = evaluadorMLP.weightedRecall();
            estadisticas[5] = evaluadorMLP.weightedPrecision();
            estadisticas[6] = evaluadorMLP.weightedFMeasure();
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean enviado = false;
        while(!enviado) {
            //esperar petición de resultados de entrenamiento
            ACLMessage peticionEntrenar = this.myAgent.blockingReceive();
            System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                    this.myAgent.getLocalName(), "REC", "Petición MLP Entrenado", peticionEntrenar.getSender().getLocalName());

            String nombreEmisor = peticionEntrenar.getSender().getLocalName();
            if(nombreEmisor.split("_")[2].equals(String.valueOf(percentage))) {
                //enviar resultados de entrenamiento
                ACLMessage mensajeEntrenamiento = new ACLMessage(ACLMessage.REQUEST);
                AID agenteCalculadorMedia = new AID(nombreEmisor, AID.ISLOCALNAME);
                mensajeEntrenamiento.addReceiver(agenteCalculadorMedia);
                try {
                    mensajeEntrenamiento.setContentObject(estadisticas);
                    this.myAgent.send(mensajeEntrenamiento);
                    enviado = true;
                    System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                            this.myAgent.getLocalName(), "ENV", "MLP Entrenado", nombreEmisor);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                //rechazar envío
                ACLMessage rechazo = new ACLMessage(ACLMessage.CANCEL);
                rechazo.addReceiver(new AID(nombreEmisor, AID.ISLOCALNAME));
                this.myAgent.send(rechazo);
                System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                        this.myAgent.getLocalName(),"ENV","Rechazo MLP Entrenado", nombreEmisor);
            }
        }
    }

    @Override
    public boolean done() {
        System.out.printf("Agente %-18s : %s\n",
                this.myAgent.getLocalName(),"DEP");
        return true;
    }
}

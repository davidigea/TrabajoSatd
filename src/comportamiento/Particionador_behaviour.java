package comportamiento;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import utils.Tools;
import weka.core.Instances;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Particionador_behaviour extends Behaviour {
    private int percentage;
    private int numModelos;
    private Instances datos;

    public Particionador_behaviour(int percentage, int numModelos){
        this.percentage = percentage;
        this.numModelos = numModelos;
    }

    @Override
    public void action() {
        ACLMessage respuestaFichero = null;
        Instances datosEntrenamiento;
        Instances datosTest;
        String nombreReceptor;

        //pedir fichero
        ArrayList<AID> candidatos = Tools.BuscarAgentes(this.myAgent, "lector");
        Random r = new Random();
        int elegido = r.nextInt(candidatos.size());
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        AID agenteLector = new AID(candidatos.get(elegido).getLocalName(), AID.ISLOCALNAME);
        msg.addReceiver(agenteLector);
        this.myAgent.send(msg);
        System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                this.myAgent.getLocalName(),"ENV","Petición Fichero", agenteLector.getLocalName());

        //recibir fichero
        boolean recibeDeLector = false;
        while (!recibeDeLector) {
            respuestaFichero = this.myAgent.blockingReceive();
            nombreReceptor = respuestaFichero.getSender().getLocalName();
            recibeDeLector = nombreReceptor.equals("lector");
            if(!recibeDeLector){
                System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                        this.myAgent.getLocalName(),"REC","Petición A Destiempo", nombreReceptor);
                ACLMessage rechazo = new ACLMessage(ACLMessage.REFUSE);
                rechazo.addReceiver(new AID(nombreReceptor, AID.ISLOCALNAME));
                this.myAgent.send(rechazo);
                System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                        this.myAgent.getLocalName(),"ENV","Rechazo", nombreReceptor);
            }
        }
        System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                this.myAgent.getLocalName(),"REC","Fichero", agenteLector.getLocalName());

        try {
            datos = (Instances) respuestaFichero.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }

        //particionar fichero
        int tamanyoTrozoEntrenamiento = datos.size() * percentage / 100;
        int tamanyoTrozoTest = datos.size() - tamanyoTrozoEntrenamiento;
        datosEntrenamiento = new Instances(datos, 0, tamanyoTrozoEntrenamiento);
        datosTest = new Instances(datos, tamanyoTrozoEntrenamiento, tamanyoTrozoTest);

        ArrayList<String> modelosEntregados = new ArrayList<>();
        ArrayList<AID> agentesUsados = new ArrayList<>();
        while (modelosEntregados.size()<numModelos) {
            //esperar petición de particiones
            ACLMessage peticionParticionar = this.myAgent.blockingReceive();
            String nombreEmisor = peticionParticionar.getSender().getLocalName();
            System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                    this.myAgent.getLocalName(),"REC","Petición Datos", nombreEmisor);
            String modeloEmisor = nombreEmisor.split("_")[0];
            if(!modelosEntregados.contains(modeloEmisor)){
                //enviar fichero
                ACLMessage mensajeParticiones = new ACLMessage(ACLMessage.REQUEST);
                AID agenteModelo = new AID(peticionParticionar.getSender().getLocalName(), AID.ISLOCALNAME);
                mensajeParticiones.addReceiver(agenteModelo);
                try {
                    mensajeParticiones.setContentObject(new Object[]{datosEntrenamiento, datosTest, percentage});
                    this.myAgent.send(mensajeParticiones);
                    System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                            this.myAgent.getLocalName(),"ENV","Datos", nombreEmisor);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                modelosEntregados.add(modeloEmisor);
                agentesUsados.add(agenteModelo);
            } else {
                //rechazar envío
                ACLMessage rechazo = new ACLMessage(ACLMessage.CANCEL);
                rechazo.addReceiver(new AID(nombreEmisor, AID.ISLOCALNAME));
                this.myAgent.send(rechazo);
                System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                        this.myAgent.getLocalName(),"ENV","Rechazo Datos", nombreEmisor);
            }
        }

        boolean avisoMuerte = false;
        while(!avisoMuerte){
            ACLMessage peticionParticionar = this.myAgent.blockingReceive();
            String nombreEmisor = peticionParticionar.getSender().getLocalName();
            if(nombreEmisor.equals("lector")){
                System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                        this.myAgent.getLocalName(),"REC","Aviso Finalización", nombreEmisor);
                avisoMuerte = true;
                for(AID agente : agentesUsados){
                    ACLMessage mensajeFichero = new ACLMessage(ACLMessage.REQUEST);
                    mensajeFichero.addReceiver(agente);
                    this.myAgent.send(mensajeFichero);
                    System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                            this.myAgent.getLocalName(), "ENV", "Aviso Finalización", agente.getLocalName());
                }
            } else {
                System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                        this.myAgent.getLocalName(),"REC","Petición Datos", nombreEmisor);

                //rechazar envío
                ACLMessage rechazo = new ACLMessage(ACLMessage.CANCEL);
                rechazo.addReceiver(new AID(nombreEmisor, AID.ISLOCALNAME));
                this.myAgent.send(rechazo);
                System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                        this.myAgent.getLocalName(),"ENV","Rechazo Datos", nombreEmisor);
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

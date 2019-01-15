package comportamiento;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import utils.Tools;

import java.util.ArrayList;

public class Mostrador_behaviour extends Behaviour {

    int numModelos;
    int numPorcentajes;

    public Mostrador_behaviour(int numModelos, int numPorcentajes){
        this.numModelos = numModelos;
        this.numPorcentajes = numPorcentajes;
    }

    @Override
    public void action() {
        ACLMessage respuestaFichero = null;
        String nombreReceptor = "";
        ArrayList<Integer> agentesUsados = new ArrayList<>();
        boolean elegidoNuevo;
        AID agenteModelo = null;
        String[][] tablaDatos = {};
        Object[] contenidoRespuesta = {};

        for(int i=0;i<numModelos*numPorcentajes;i++) {
            ArrayList<AID> candidatos = Tools.BuscarAgentes(this.myAgent, "media");
            elegidoNuevo = false;
            while (!elegidoNuevo) {
                int elegido = (int) Math.random() * ((candidatos.size() - 1) + 1);
                if (!agentesUsados.contains(elegido)) {
                    agenteModelo = new AID(candidatos.get(elegido).getLocalName(), AID.ISLOCALNAME);
                    elegidoNuevo = true;
                    agentesUsados.add(elegido);
                }
            }

            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(agenteModelo);
            this.myAgent.send(msg);
            System.out.println("Soy el agente " + this.myAgent.getLocalName() + " y acabo de enviar una " +
                    "petición de resultados de media al agente " + agenteModelo.getLocalName());

            while (!nombreReceptor.equals("media")) {
                respuestaFichero = this.myAgent.blockingReceive();
                nombreReceptor = respuestaFichero.getSender().getLocalName();
                ACLMessage rechazo = new ACLMessage(ACLMessage.REFUSE);
                AID destinatario = new AID(nombreReceptor, AID.ISLOCALNAME);
                rechazo.addReceiver(destinatario);
            }
            System.out.println("Soy el agente " + this.myAgent.getLocalName() + " y acabo de recibir" +
                    " medias del agente " + agenteModelo.getLocalName());

            try {
                contenidoRespuesta = (Object[]) respuestaFichero.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            double[] arrayAux = (double[]) contenidoRespuesta[0];
            for (int j = 0; j < 7; j++) {
                tablaDatos[i][j] = String.valueOf(arrayAux[j]); //Estadísticas
            }
            tablaDatos[i][7] = (String) contenidoRespuesta[1];  //Porcentaje
            tablaDatos[i][8] = (String) contenidoRespuesta[2];  //Modelo
        }

        //Mostrar los resultados (tablaDatos)
    }

    @Override
    public boolean done() {
        return false;
    }
}

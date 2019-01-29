package comportamiento;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import utils.Tools;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Mostrador_behaviour extends Behaviour {

    private int numModelos;
    private int numPorcentajes;

    public Mostrador_behaviour(int numModelos, int numPorcentajes){
        this.numModelos = numModelos;
        this.numPorcentajes = numPorcentajes;
    }

    @Override
    public void action() {
        long startTime = System.nanoTime();
        ACLMessage msgRecibido = null;
        ArrayList<Integer> agentesUsados = new ArrayList<>();
        boolean elegidoNuevo;
        AID agenteMedia = null;
        String[][] tablaDatos = new String[numModelos*numPorcentajes][9];
        Object[] contenidoRespuesta = {};

        for(int i=0;i<numModelos*numPorcentajes;i++) {
            ArrayList<AID> candidatos = Tools.BuscarAgentes(this.myAgent, "media");
            elegidoNuevo = false;
            while (!elegidoNuevo) {
                Random r = new Random();
                int elegido = r.nextInt(candidatos.size());
                if (!agentesUsados.contains(elegido)) {
                    agenteMedia = new AID(candidatos.get(elegido).getLocalName(), AID.ISLOCALNAME);
                    elegidoNuevo = true;
                    agentesUsados.add(elegido);
                }
            }

            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(agenteMedia);
            this.myAgent.send(msg);
            System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                    this.myAgent.getLocalName(),"ENV","Petición resultados media", agenteMedia.getLocalName());

            boolean recibeMedia = false;
            while(!recibeMedia){
                msgRecibido = this.myAgent.blockingReceive();
                String nombreReceptor = msgRecibido.getSender().getLocalName();
                if(nombreReceptor.equals(agenteMedia.getLocalName())){
                    if(msgRecibido.getPerformative() != ACLMessage.REFUSE) {
                        recibeMedia = true;
                        System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                                this.myAgent.getLocalName(),"REC","Resultados Media", nombreReceptor);
                    } else {
                        System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                                this.myAgent.getLocalName(),"REC","Rechazo", nombreReceptor);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        this.myAgent.send(msg);
                        System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                                this.myAgent.getLocalName(),"ENV","Petición resultados media", agenteMedia.getLocalName());
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

            try {
                contenidoRespuesta = (Object[]) msgRecibido.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            double[] arrayAux = (double[]) contenidoRespuesta[0];
            for (int j = 0; j < 7; j++) {
                tablaDatos[i][j] = String.valueOf(arrayAux[j]); //Estadísticas
            }
            tablaDatos[i][7] = String.valueOf(contenidoRespuesta[1]);  //Porcentaje
            tablaDatos[i][8] = String.valueOf(contenidoRespuesta[2]);  //Modelo
        }

        //Mostrar tabla por pantalla
        System.out.println("\n\nEstos son todos los resultados:");
        String linea = new String(new char[190]).replace('\0', '-');
        System.out.println(linea);
        pintarResultados("Modelo","Porcentaje","Ratio VerPos", "Ratio FalPos",
                "Ratio VerNeg","Ratio FalNeg","Recall","Precisión","F-Valor");
        System.out.println(linea);
        pintarResultadosMatriz(tablaDatos);
        System.out.println(linea);
        System.out.println("\n\n");

        ArrayList<AID> lector = Tools.BuscarAgentes(this.myAgent, "lector");
        Random r = new Random();
        int elegido = r.nextInt(lector.size());
        AID agenteLector = new AID(lector.get(elegido).getLocalName(), AID.ISLOCALNAME);

        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(agenteLector);
        this.myAgent.send(msg);
        System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                this.myAgent.getLocalName(),"ENV","Aviso de Finalización", agenteLector.getLocalName());
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime)/1000000;

        String lineaTiempo = new String(new char[35]).replace('\0', '=');
        System.out.println(lineaTiempo + "\n= Duración del programa: " + durationMs + " ms =\n" + lineaTiempo);
    }

    /* Pinta los resultados en el orden pasado con el formato adecuado */
    private void pintarResultados(String modelo, String porcentaje, String verPos, String falPos,
                                  String verNeg, String falNeg, String recall, String precision, String fValor){
        System.out.printf("|%-20s|%-20s|%-20s|%-20s|%-20s|%-20s|%-20s|%-20s|%-20s|\n",
                modelo,porcentaje,verPos,falPos,verNeg,falNeg,recall,precision,fValor);
    }

    /* Pinta los resultados de un vector con el formato adecuado */
    private void pintarResultadosArray(String[] array){
        pintarResultados(array[8],array[7],array[0],array[1],array[2],array[3],array[4],array[5],array[6]);
    }

    /* Pinta los resultados de una matriz con el formato adecuado */
    private void pintarResultadosMatriz(String[][] matriz){
        for(int i=0;i<numPorcentajes*numModelos;i++){
            pintarResultadosArray(matriz[i]);
        }
    }

    @Override
    public boolean done() {
        System.out.printf("Agente %-18s : %s\n",
                this.myAgent.getLocalName(),"DEP");
        return true;
    }
}

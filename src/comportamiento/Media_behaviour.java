package comportamiento;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import utils.Tools;

import java.io.IOException;
import java.util.ArrayList;

public class Media_behaviour extends Behaviour {

    private int num;
    private int percentage;     //Necesario saber % y modelo para decírselo al mostrador
    private String modelo;
    private double[][] entrada; //Recibe num arrays de estadísticas como resultado de los modelos
    private double[] salida;    //Entrega un array de estadísticas como media de la entrada

    public Media_behaviour(int num, int percentage, String modelo){
        this.num = num;
        this.percentage = percentage;
        this.modelo = modelo;
    }

    @Override
    public void action() {
        ACLMessage respuestaFichero = null;
        String nombreReceptor = "";
        double[] datos = null;
        ArrayList<Integer> agentesUsados = new ArrayList<>();
        boolean elegidoNuevo = false;
        AID agenteModelo = null;

        for(int i=0;i<num;i++){     //Pedir num resultados
            ArrayList<AID> candidatos = Tools.BuscarAgentes(this.myAgent, modelo);              //Elegir uno del mismo modelo
            elegidoNuevo = false;
            while(!elegidoNuevo){
                int elegido = (int) Math.random()*((candidatos.size()-1)+1);
                if(!agentesUsados.contains(elegido)){                                           //Elegir uno no tratado
                    agenteModelo = new AID(candidatos.get(elegido).getLocalName(), AID.ISLOCALNAME);
                    if(agenteModelo.getLocalName().contains(String.valueOf(percentage))){       //Elegir uno del mismo porcentaje
                        elegidoNuevo = true;
                        agentesUsados.add(elegido);
                    }
                }
            }

            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(agenteModelo);
            this.myAgent.send(msg);
            System.out.println("Soy el agente " + this.myAgent.getLocalName() + " y acabo de enviar una " +
                    "petición de resultados del modelo "+ modelo +" al agente " + agenteModelo.getLocalName());

            while (!nombreReceptor.contains(modelo)) {
                respuestaFichero = this.myAgent.blockingReceive();
                nombreReceptor = respuestaFichero.getSender().getLocalName();
                ACLMessage rechazo = new ACLMessage(ACLMessage.REFUSE);
                AID destinatario = new AID(nombreReceptor, AID.ISLOCALNAME);
                rechazo.addReceiver(destinatario);
            }
            System.out.println("Soy el agente " + this.myAgent.getLocalName() + " y acabo de recibir" +
                    " resultados del agente " + agenteModelo.getLocalName());


            try {
                datos = (double[]) respuestaFichero.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            entrada[i] = datos;

            System.out.println(datos);
        }
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Hacer las medias de las estadísticas
        for(int i = 0; i<7;i++){
            for (int j = 0; j<num; j++){
                salida[i] += entrada[j][i];     //salida[i] es la suma de la columna i de entrada
            }
            salida[i] /= num;                   //salida[i] es la media de la columna i de entrada
        }

        while (true) {
            //esperar petición de medias
            ACLMessage peticionParticionar = this.myAgent.blockingReceive();
            System.out.println("Soy el agente " + this.myAgent.getLocalName() + " y acabo de recibir una petición" +
                    " de medias del agente " + peticionParticionar.getSender());

            //enviar fichero
            ACLMessage mensajeParticiones = new ACLMessage(ACLMessage.REQUEST);
            AID agenteMostrador = new AID(peticionParticionar.getSender().getLocalName(), AID.ISLOCALNAME);
            mensajeParticiones.addReceiver(agenteMostrador);

            try {
                mensajeParticiones.setContentObject(new Object[]{salida, percentage, modelo});
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

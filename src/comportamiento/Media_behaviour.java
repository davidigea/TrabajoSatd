package comportamiento;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import utils.Tools;
import weka.core.Instances;

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
        ArrayList<String> nombresAgentes = new ArrayList<>();
        boolean elegidoNuevo = false;
        AID agenteModelo = null;

        for(int i=0;i<num;i++){     //Pedir num resultados
            ArrayList<AID> candidatos = Tools.BuscarAgentes(this.myAgent, modelo);              //Elegir del mismo modelo
            elegidoNuevo = false;
            while(!elegidoNuevo){
                int elegido = (int) Math.random()*((candidatos.size()-1)+1);
                agenteModelo = new AID(candidatos.get(elegido).getLocalName(), AID.ISLOCALNAME);
                if(!nombresAgentes.contains(agenteModelo.getLocalName())                        //Elegir un no tratado
                        && agenteModelo.getLocalName().contains(String.valueOf(percentage))){   //Elegir del mismo %
                    elegidoNuevo = true;
                }
            }

            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(agenteModelo);
            this.myAgent.send(msg);
            System.out.println("Soy el agente " + this.myAgent.getLocalName() + " y acabo de enviar una " +
                    "petición de resultados del modelo "+ modelo +" al agente " + agenteModelo.getLocalName());

            while (!nombreReceptor.equals(modelo)) {
                respuestaFichero = this.myAgent.blockingReceive();
                nombreReceptor = respuestaFichero.getSender().getLocalName();
                ACLMessage rechazo = new ACLMessage(ACLMessage.REFUSE);
                AID destinatario = new AID(nombreReceptor, AID.ISLOCALNAME);
                rechazo.addReceiver(destinatario);
            }
            System.out.println("Soy el agente " + this.myAgent.getLocalName() + " y acabo de recibir" +
                    " resultados del agente " + agenteModelo.getLocalName());
            nombresAgentes.add(agenteModelo.getLocalName());

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
        //Medias

    }

    @Override
    public boolean done() {
        return false;
    }
}

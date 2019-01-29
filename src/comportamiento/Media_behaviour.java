package comportamiento;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import utils.Tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

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
        ACLMessage msgRecibido = null;
        double[] datos = new double[7];
        ArrayList<Integer> agentesUsados = new ArrayList<>();
        boolean elegidoNuevo;
        AID agenteModelo = null;
        entrada = new double[num][7];
        salida = new double[7];

        for(int i=0;i<num;i++){     //Pedir num resultados
            ArrayList<AID> candidatos = Tools.BuscarAgentes(this.myAgent, modelo);              //Elegir uno del mismo modelo
            elegidoNuevo = false;
            while(candidatos.size()<=0){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                candidatos = Tools.BuscarAgentes(this.myAgent, modelo);
            }
            while(!elegidoNuevo){
                Random r = new Random();
                int elegido = r.nextInt(candidatos.size());
                if(!agentesUsados.contains(elegido)){                                           //Elegir uno no tratado
                    agenteModelo = new AID(candidatos.get(elegido).getLocalName(), AID.ISLOCALNAME);
                        elegidoNuevo = true;
                        agentesUsados.add(elegido);
                }
            }
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(agenteModelo);
            this.myAgent.send(msg);
            System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                    this.myAgent.getLocalName(),"ENV","Petición "+modelo+" entrenado", agenteModelo.getLocalName());
            boolean recibeModelo = false;
            boolean caminoEquivocado = false;
            while(!recibeModelo && !caminoEquivocado){
                msgRecibido = this.myAgent.blockingReceive();
                String nombreReceptor = msgRecibido.getSender().getLocalName();
                if(nombreReceptor.equals(agenteModelo.getLocalName())){
                    if(msgRecibido.getPerformative() == ACLMessage.REFUSE){
                        System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                                this.myAgent.getLocalName(),"REC","Rechazo", nombreReceptor);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        this.myAgent.send(msg);
                        System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                                this.myAgent.getLocalName(),"ENV","Petición "+modelo+" entrenado", agenteModelo.getLocalName());
                    } else if(msgRecibido.getPerformative() == ACLMessage.CANCEL){
                        caminoEquivocado = true;
                    } else {
                        recibeModelo = true;
                        System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                                this.myAgent.getLocalName(),"REC",modelo+" entrenado", nombreReceptor);
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
            if(recibeModelo){
                try {
                    datos = (double[]) msgRecibido.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                entrada[i] =  datos;
            } else i--; //Esta iteración no cuenta
        }

        //Hacer las medias de las estadísticas
        for(int i = 0; i<7;i++){
            for (int j = 0; j<num; j++){
                salida[i] += entrada[j][i];     //salida[i] es la suma de la columna i de entrada
            }
            salida[i] /= num;                   //salida[i] es la media de la columna i de entrada
        }

        //esperar petición de medias
        ACLMessage peticionParticionar = this.myAgent.blockingReceive();
        System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                this.myAgent.getLocalName(),"REC","Petición Medias", peticionParticionar.getSender().getLocalName());

        //enviar fichero
        ACLMessage mensajeParticiones = new ACLMessage(ACLMessage.REQUEST);
        AID agenteMostrador = new AID(peticionParticionar.getSender().getLocalName(), AID.ISLOCALNAME);
        mensajeParticiones.addReceiver(agenteMostrador);

        try {
            mensajeParticiones.setContentObject(new Object[]{salida, percentage, modelo});
            this.myAgent.send(mensajeParticiones);
            System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                    this.myAgent.getLocalName(),"ENV","Medias", peticionParticionar.getSender().getLocalName());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean done() {
        System.out.printf("Agente %-18s : %s\n",
                this.myAgent.getLocalName(),"DEP");
        return true;
    }
}

package comportamiento;

import comportamiento.entrenadores.J48_behaviour;
import comportamiento.entrenadores.MLP_behaviour;
import comportamiento.entrenadores.NaiveBayes_behaviour;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

public class StartDist_Behaviour extends Behaviour {

    private ArrayList<String> modelos;
    private ArrayList<String> porcentajes;
    private String fileName;
    private int numIteraciones;
    private int tipo;

    public StartDist_Behaviour(int tipo){
        this.tipo = tipo;
    }

    @Override
    public void action() {
        long beforeUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        ContainerController cc = myAgent.getContainerController();
        modelos = new ArrayList<>();
        porcentajes = new ArrayList<>();

        //Para versión final
        try {datos(); } catch (IOException e) { e.printStackTrace(); }

/*         //Para pruebas:
        modelos.add("bayes");
        modelos.add("j48");
        modelos.add("mlp");
        porcentajes.add("80");
        porcentajes.add("50");
        porcentajes.add("20");
        fileName = "yellow-small.arff";
        numIteraciones = 2;
*/
        try {
            Thread.sleep(3000);
            //Se añaden al array de agentes todos los agentes que se vayan a lanzar
            ArrayList<AgentController> agents = new ArrayList<>();
            Object[] argumentos;

            if(tipo == 1){
                //Los agentes lector y mostrador se añaden de forma estática
                argumentos = new Object[]{"lector", new LectorCSV_behaviour("data/" + fileName)};
                agents.add(cc.createNewAgent("lector", "agentes.AgenteBase", argumentos));

                argumentos = new Object[]{"mostrador", new Mostrador_behaviour(modelos.size(), porcentajes.size())};
                agents.add(cc.createNewAgent("mostrador", "agentes.AgenteBase", argumentos));
            }

            for (int j = 0; j < porcentajes.size(); j++) {
                String por = porcentajes.get(j);
                for (int i = 0; i < numIteraciones; i++) {
                    if(tipo == 1){
                        argumentos = new Object[]{"particionador", new Particionador_behaviour(Integer.parseInt(por),modelos.size())};
                        agents.add(cc.createNewAgent("particionador_" + por + "_" + (i + 1),
                                "agentes.AgenteBase", argumentos));
                    }else {
                        for (String modelo : modelos) {
                            Behaviour beh = null;
                            switch (modelo) {
                                case "bayes":
                                    beh = new NaiveBayes_behaviour();
                                    break;
                                case "j48":
                                    beh = new J48_behaviour();
                                    break;
                                case "mlp":
                                    beh = new MLP_behaviour();
                                    break;
                            }
                            argumentos = new Object[]{modelo, beh};
                            agents.add(cc.createNewAgent(modelo + "_" + (10 * j + i + 1),
                                    "agentes.AgenteBase", argumentos));
                        }
                    }
                }
                if(tipo == 1){
                    for (String mod : modelos) {
                        argumentos = new Object[]{"media", new Media_behaviour(numIteraciones, (Integer.parseInt(por)), mod)};
                        agents.add(cc.createNewAgent("media_" + mod + "_" + por, "agentes.AgenteBase", argumentos));
                    }
                }
            }

            //Se recorre el array de agentes y se ponen en marcha
            for (AgentController agent : agents) {
                System.out.println("Arrancando agente " + agent.getName() + "...");
                agent.start();
            }

            long afterUsedMem = 0, actualMemUsed = 0;
            long slaveMem = 0, totalMem = 0;

            int n = 1;
            if(tipo == 1) n = 2;
            while(n>0) {
                ACLMessage aviso = this.myAgent.blockingReceive();
                if (aviso.getSender().getLocalName().contains("lector")) {
                    afterUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    actualMemUsed = afterUsedMem - beforeUsedMem;
                    if(tipo == 2){
                        AID start = new AID("AgenteStartMaestro", AID.ISLOCALNAME);;
                        ACLMessage mensajeMemoria = new ACLMessage(ACLMessage.REQUEST);
                        mensajeMemoria.addReceiver(start);
                        mensajeMemoria.setContent(String.valueOf(actualMemUsed));
                        this.myAgent.send(mensajeMemoria);
                    }
                } else if (tipo == 1) {
                    slaveMem =  Long.valueOf(aviso.getContent());
                }
                n--;
            }

            if(tipo == 1){
                totalMem = slaveMem + actualMemUsed;
                String linea = new String(new char[50]).replace('\0', '=');
                System.out.println(linea + "\n= Memoria ocupada por los agentes del equipo maestro: " + actualMemUsed + " bytes =\n" + linea);
                System.out.println(linea + "\n= Memoria ocupada por los agentes del equipo esclavo: " + slaveMem + " bytes =\n" + linea);
                System.out.println(linea + "\n= Memoria ocupada por los agentes en total: " + totalMem + " bytes =\n" + linea);
            }

        } catch (StaleProxyException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* Pide los datos del programa por consola */
    private void datos() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Inserte un nombre de fichero ('-' para fichero por defecto):");
        fileName = br.readLine();
        if (fileName.equals("-")) fileName = "yellow-small.arff";
        System.out.println("Inserte los modelos, separados por comas, en minúsculas:");
        String[] aux = br.readLine().split(",");
        Collections.addAll(modelos, aux);
        System.out.println("Inserte los porcentajes, separados por comas (numéricamente):");
        aux = br.readLine().split(",");
        Collections.addAll(porcentajes, aux);
        System.out.println("Inserte el número de iteraciones (numéricamente):");
        numIteraciones = Integer.parseInt(br.readLine());
    }

    @Override
    public boolean done() {
        return true;
    }
}

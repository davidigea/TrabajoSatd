package comportamiento;

import comportamiento.entrenadores.J48_behaviour;
import comportamiento.entrenadores.MLP_behaviour;
import comportamiento.entrenadores.NaiveBayes_behaviour;
import jade.core.behaviours.Behaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;

public class Start_Behaviour extends Behaviour {

    @Override
    public void action() {
        ContainerController cc = myAgent.getContainerController();

        try {
            Thread.sleep(3000);
            //Se añaden al array de agentes todos los agentes que se vayan a lanzar
            ArrayList<AgentController> agents = new ArrayList<>();

            Object[] argumentos = {"lector", new LectorCSV_behaviour("data/yellow-small.arff")};
            agents.add(cc.createNewAgent("lector", "agentes.AgenteBase",argumentos));

            argumentos = new Object[]{"particionador", new Particionador_behaviour(80)};
            agents.add(cc.createNewAgent("particionador", "agentes.AgenteBase", argumentos));

            argumentos = new Object[]{"bayes", new NaiveBayes_behaviour()};
            agents.add(cc.createNewAgent("bayes", "agentes.AgenteBase", argumentos));

//            argumentos = new Object[]{"j48", new J48_behaviour()};
//            agents.add(cc.createNewAgent("j48", "agentes.AgenteBase", argumentos));
//
//            argumentos = new Object[]{"mlp", new MLP_behaviour()};
//            agents.add(cc.createNewAgent("mlp", "agentes.AgenteBase", argumentos));
//
            argumentos = new Object[]{"media", new Media_behaviour(1,80,"bayes")};
            agents.add(cc.createNewAgent("media", "agentes.AgenteBase", argumentos));
//
//            argumentos = new Object[]{"mostrador", new Mostrador_behaviour(3,3)};
//            agents.add(cc.createNewAgent("mostrador", "agentes.AgenteBase", argumentos));

            //Se recorre el array de agentes y se ponen en marcha
            for (int i=0; i<agents.size(); i++) {
                System.out.println("Arrancando agente " + agents.get(i).getName() + "...");
                agents.get(i).start();
            }
        } catch (StaleProxyException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean done() {
        return true;
    }
}

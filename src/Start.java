import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;

public class Start {
    public static void main(String[] args) {
        Runtime rt = Runtime.instance();
        ProfileImpl p = new ProfileImpl(false);
        ContainerController container = rt.createMainContainer(p);
        ArrayList<AgentController> agents = new ArrayList<>();

        try {
            //Se añaden al array de agentes todos los agentes que se vayan a lanzar
            agents.add(container.createNewAgent("Agent1", "jade.Agent1", null));

            //Se recorre el array de agentes y se ponen en marcha
            for (int i=0; i<agents.size(); i++) {
                agents.get(i).start();
            }
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}

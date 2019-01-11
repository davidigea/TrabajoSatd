package agentes;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import utils.Tools;

public class AgenteBase extends Agent {
    public void setup(){
        Object[] argumentos = getArguments();
        Tools.registrarServicio(this, (String) argumentos[0]);

        this.addBehaviour((Behaviour) argumentos[1]);
    }
}

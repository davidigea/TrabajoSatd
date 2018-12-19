package agentes;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import utils.Tools;

public class AgenteBase extends Agent {
    public void setup(String servicio, Behaviour b){
        Tools.registrarServicio(this, servicio);
        this.addBehaviour(b);
    }
}

package agentes;

import comportamiento.Start_Behaviour;
import jade.core.Agent;
public class Start extends Agent {
    public void setup() {
        this.addBehaviour(new Start_Behaviour());
    }
}

package agentes;

import comportamiento.StartDist_Behaviour;
import comportamiento.Start_Behaviour;
import jade.core.Agent;

import java.util.Scanner;

public class StartEsclavo extends Agent {
    public void setup() {
        this.addBehaviour(new StartDist_Behaviour(2));
    }
}

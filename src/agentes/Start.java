package agentes;

import comportamiento.StartDist_Behaviour;
import comportamiento.Start_Behaviour;
import jade.core.Agent;

import java.util.Scanner;

public class Start extends Agent {
    public void setup() {
        Scanner teclado = new Scanner(System.in);
        String opcion = teclado.nextLine();

        if (opcion.contains("l")) {
            this.addBehaviour(new Start_Behaviour());
        }
        else if (opcion.contains("d")){
            this.addBehaviour(new StartDist_Behaviour(1));
        }
        else {
            System.out.println("No se han podido lanzar los agentes.");
        }
    }
}

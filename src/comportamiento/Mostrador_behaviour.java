package comportamiento;

import jade.core.behaviours.Behaviour;

public class Mostrador_behaviour extends Behaviour {

    int[] veinte;           // ¿O hacerlo menos acoplado?
    int[] cincuenta;
    int[] ochenta;

    @Override
    public void action() {

    }

    @Override
    public boolean done() {
        return false;
    }
}

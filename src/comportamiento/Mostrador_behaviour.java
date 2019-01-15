package comportamiento;

import jade.core.behaviours.Behaviour;

public class Mostrador_behaviour extends Behaviour {

    double[] veinte;           // ¿O hacerlo menos acoplado?
    double[] cincuenta;
    double[] ochenta;

    @Override
    public void action() {

    }

    @Override
    public boolean done() {
        return false;
    }
}

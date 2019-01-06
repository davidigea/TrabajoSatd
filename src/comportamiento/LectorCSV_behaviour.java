package comportamiento;

import jade.core.behaviours.Behaviour;
import au.com.bytecode.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class LectorCSV_behaviour  extends Behaviour {
    String path;
    CSVReader reader;

    public LectorCSV_behaviour(String path){
        this.path = path;
    }

    @Override
    public void action() {
        String[] fila;
        ArrayList<String[]> resultado = new ArrayList<>();

        try {
            reader = new CSVReader(new FileReader(path));

            while ((fila = reader.readNext()) != null){
                resultado.add(fila);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        while (true) {
            //Esperar a una petición de fichero
            //Envía un array con el contenido del fichero
        }
    }

    @Override
    public boolean done() {
        return false;
    }
}

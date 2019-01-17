package comportamiento;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import au.com.bytecode.opencsv.CSVReader;
import jade.lang.acl.ACLMessage;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import javax.sound.midi.SysexMessage;
import java.io.File;
import java.io.IOException;

public class LectorCSV_behaviour extends Behaviour {
    String path;
    CSVReader reader;

    public LectorCSV_behaviour(String path){
        this.path = path;
    }

    /**
     * This method is to load the data set.
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    private static Instances getDataSet(String fileName) throws Exception {
        /**
         * we can set the file i.e., loader.setFile("finename") to load the data
         */
        StringToWordVector filter = new StringToWordVector();
        int classIdx = 1;
        /** the arffloader to load the arff file */
        ArffLoader loader = new ArffLoader();
        /** load the traing data */
        /**
         * we can also set the file like loader3.setFile(new
         * File("test-confused.arff"));
         */
        loader.setFile(new File(fileName));
        Instances dataSet = loader.getDataSet();
        /** set the index based on the data given in the arff files */
        dataSet.setClassIndex(classIdx);
        filter.setInputFormat(dataSet);
        dataSet = Filter.useFilter(dataSet, filter);
        return dataSet;
    }

    @Override
    public void action() {
        String[] fila;
        Instances fichero = null;

        try {
            fichero = getDataSet(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ACLMessage peticionFichero;
        while (true) {
            //Esperar a una petición de fichero
            peticionFichero = this.myAgent.blockingReceive();
            System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                    this.myAgent.getLocalName(),"REC","Petición Fichero", peticionFichero.getSender().getLocalName());
            ACLMessage mensajeFichero = new ACLMessage(ACLMessage.REQUEST);
            AID agenteParticionador = new AID(peticionFichero.getSender().getLocalName(), AID.ISLOCALNAME);
            mensajeFichero.addReceiver(agenteParticionador);
            try {
                mensajeFichero.setContentObject(fichero);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Envía un array con el contenido del fichero
            this.myAgent.send(mensajeFichero);
            System.out.printf("Agente %-18s : %s : %-35s : Agente %-18s\n",
                        this.myAgent.getLocalName(),"ENV","Fichero", peticionFichero.getSender().getLocalName());
        }
    }

    @Override
    public boolean done() {
        return false;
    }
}

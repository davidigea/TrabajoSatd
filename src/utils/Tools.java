package utils;

import excepciones.ServicioNoValido;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.util.leap.Iterator;

import java.util.ArrayList;

public class Tools {
    /*
    *   Registra en el agente de páginas blancas que el agente "a" ofrece el servicio "servicio"
    *
    */
    public static void registrarServicio(Agent a, String servicio) {
        DFAgentDescription dfd = new DFAgentDescription();
        // Se le asigna al servicio el ID del Agente que lo ofrece
        dfd.setName(a.getAID());

        // Se especifica el tipo y nombre del servicio
        ServiceDescription sd = new ServiceDescription();
        sd.setType(servicio);
        sd.setName(servicio);

        dfd.addServices(sd);
        try {
            if (servicio == null || servicio == ""){
                throw new ServicioNoValido("");
            }

            DFService.register(a, dfd);
        }
        catch (FIPAException ex) {
            System.err.println("el Agente :" + a.getLocalName() + "No ha podido registar el servicio : " + ex.getMessage());
            a.doDelete();
        } catch (ServicioNoValido servicioNoValido) {
            servicioNoValido.printStackTrace();
        }
    }

    /*
    *   Recupera del agente de páginas blancas una lista con todos los agentes que ofrecen el servicio "servicio"
    *
    */
    public static ArrayList<AID> BuscarAgentes(Agent a, String servicio) {
        ArrayList<AID> creadores = new ArrayList<>();
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setName(servicio);
        dfd.addServices(sd);

        try {
            if (servicio == null || servicio == ""){
                throw new ServicioNoValido("");
            }

            DFAgentDescription[] result = DFService.search(a, dfd);

            //System.out.println("Total encontrados " + result.length);
            for (int i = 0; i < result.length; i++) {
                creadores.add(result[i].getName());
            }
        } catch (FIPAException fe) {
            System.err.println(a.getLocalName() + " busqueda con DF fallida "
                    + fe.getMessage());
            a.doDelete();
        } catch (ServicioNoValido servicioNoValido) {
            servicioNoValido.printStackTrace();
        }

        return creadores;
    }
}

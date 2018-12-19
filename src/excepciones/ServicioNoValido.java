package excepciones;

public class ServicioNoValido extends Exception {
    public ServicioNoValido(String mensaje){
        super(mensaje);
    }
}

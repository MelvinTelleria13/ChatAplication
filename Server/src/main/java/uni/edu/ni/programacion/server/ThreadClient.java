package uni.edu.ni.programacion.server;

/**
 *
 * @author Melvin Telleria
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

public class ThreadClient {
    private final Socket S_ocket;        
    private ObjectOutputStream ObjectOutput_Stream;
    private ObjectInputStream ObjectInput_Stream;                    
    private final Server S_erver;
    private String Identificator;
    private boolean escuchando;
    
    public ThreadClient(Socket S_ocket,Server S_erver) {
        this.S_erver = S_erver;
        this.S_ocket = S_ocket;
        try {
            ObjectOutput_Stream = new ObjectOutputStream(S_ocket.getOutputStream());
            ObjectInput_Stream = new ObjectInputStream(S_ocket.getInputStream());
        } catch (IOException ex) {
            System.err.println("Error en la inicialización del ObjectOutputStream y el ObjectInputStream");
        }
    }
   
    public void desconnectar() {
        try {
            S_ocket.close();
            escuchando=false;
        } catch (IOException ex) {
            System.err.println("Error al cerrar el socket de comunicación con el cliente.");
        }
    }
       
    public void run() {
        try{
            escuchar();
        } catch (Exception ex) {
            System.err.println("Error al llamar al método readLine del hilo del cliente.");
        }
        desconnectar();
    }
               
    public void escuchar(){        
        escuchando=true;
        while(escuchando){
            try {
                Object aux = ObjectInput_Stream.readObject();
                if(aux instanceof LinkedList){
                    ejecutar((LinkedList<String>)aux);
                }
            } catch (Exception e) {                    
                System.err.println("Error al leer lo enviado por el cliente.");
            }
        }
    }
        
    public void ejecutar(LinkedList<String> lista){
        String tipo=lista.get(0);
        switch (tipo) {
            case "SOLICITUD_CONEXION":
                confirmarConexion(lista.get(1));
                break;
            case "SOLICITUD_DESCONEXION":
                confirmarDesConexion();
                break;                
            case "MENSAJE":
                String destinatario=lista.get(2);
                S_erver.Clients 
                        .stream()
                        .filter(h -> (destinatario.equals(h.getIdentificador())))
                        .forEach((h) -> h.enviarMensaje(lista));
                break;
            default:
                break;
        }
    }  
    
    private void enviarMensaje(LinkedList<String> lista){
        try {
            ObjectOutput_Stream.writeObject(lista);            
        } catch (Exception e) {
            System.err.println("Error al enviar el objeto al cliente.");
        }
    }    

    private void confirmarConexion(String identificador) {
        Server.Correlativo++;
        this.Identificator = Server.Correlativo+" - "+identificador;
        LinkedList<String> lista=new LinkedList<>();
        lista.add("CONEXION_ACEPTADA");
        lista.add(this.Identificator);
        lista.addAll(S_erver.getUsuariosConectados());
        enviarMensaje(lista);
        S_erver.agregarLog("\nNuevo cliente: "+this.Identificator);
        LinkedList<String> auxLista=new LinkedList<>();
        auxLista.add("NUEVO_USUARIO_CONECTADO");
        auxLista.add(this.Identificator);
        S_erver.Clients.stream().forEach(cliente -> cliente.enviarMensaje(auxLista));
        S_erver.Clients.add(this);
    }

    public String getIdentificador() {
        return Identificator;
    }

    private void confirmarDesConexion() {
        LinkedList<String> auxLista=new LinkedList<>();
        auxLista.add("USUARIO_DESCONECTADO");
        auxLista.add(this.Identificator);
        S_erver.agregarLog("\nEl cliente \""+this.Identificator+"\" se ha desconectado.");
        this.desconnectar();
        for(int i=0;i<S_erver.Clients.size();i++){
            if(S_erver.Clients.get(i).equals(this)){
                S_erver.Clients.remove(i);
                break;
            }
        }
        S_erver.Clients.stream().forEach(h -> h.enviarMensaje(auxLista));        
    }
}

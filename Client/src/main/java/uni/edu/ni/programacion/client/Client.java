package uni.edu.ni.programacion.client;

/**
 *
 * @author Melvin Telleria
 */

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import javax.swing.JOptionPane;

public class Client {
    private Socket S_ocket;
    private ObjectOutputStream ObjectOutput_Stream;
    private ObjectInputStream ObjectInput_Stream;
    private final WinClient Win;
    private String Identificator;
    private boolean Escuchando;
    private final String Huesped;
    private final int Puerto;
    
    Client(WinClient Win, String Huesped, Integer Puerto, String Nombre){
        this.Win = Win;
        this.Huesped = Huesped;
        this.Puerto = Puerto;
        this.Identificator = Nombre;
        Escuchando = true; 
    }
    
    public void run(){
        try {
            S_ocket=new Socket(Huesped, Puerto);
            ObjectOutput_Stream = new ObjectOutputStream(S_ocket.getOutputStream());
            ObjectInput_Stream = new ObjectInputStream(S_ocket.getInputStream());
            System.out.println("¡La conexion se a establecido!");
            this.enviarSolicitudConexion(Identificator);
            this.Escuchar();
        } catch (UnknownHostException ex) {
            JOptionPane.showMessageDialog(Win, "Conexión rehusada, es posible que haya ingresado una IP incorrecto o que el servidor no este en funcionamiento.\n");
            JOptionPane.showMessageDialog(Win, "Esta aplicación se cerrará.");
            System.exit(0);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(Win, "Conexión rehusada, es posible que haya ingresado una IP o un puerto incorrecto, o que el servidor no este en funcionamiento\n");
            JOptionPane.showMessageDialog(Win, "Esta aplicación se cerrará.");
            System.exit(0);
        }

    }
    
    public void desconectar(){
        try {
            ObjectOutput_Stream.close();
            ObjectInput_Stream.close();
            S_ocket.close();  
            Escuchando=false;
        } catch (Exception e) {
            System.err.println("Error al cerrar los elementos de comunicación del cliente.");
        }
    }
    
    public void enviarMensaje(String cliente_receptor, String mensaje){
        LinkedList<String> lista=new LinkedList<>();
        lista.add("MENSAJE");
        lista.add(Identificator);
        lista.add(cliente_receptor);
        lista.add(mensaje);
        try {
            ObjectOutput_Stream.writeObject(lista);
        } catch (IOException ex) {
            System.out.println("Error de lectura y escritura al enviar mensaje al servidor.");
        }
    }
    
    public void Escuchar() {
        try {
            while (Escuchando) {
                Object aux = ObjectInput_Stream.readObject();
                if (aux != null) {
                    if (aux instanceof LinkedList) {
                        //Si se recibe una LinkedList entonces se procesa
                        ejecutar((LinkedList<String>)aux);
                    } else {
                        System.err.println("Se recibió un Objeto desconocido a través del socket");
                    }
                } else {
                    System.err.println("Se recibió un null a través del socket");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(Win, "La comunicación con el servidor se ha perdido, este chat tendrá que finalizar.");
            JOptionPane.showMessageDialog(Win, "Esta aplicación se cerrará.");
            System.exit(0);
        }
    }
    
    public void ejecutar(LinkedList<String> lista){
        String tipo=lista.get(0);
        switch (tipo) {
            case "CONEXION_ACEPTADA":
            String I = lista.get(1);
                Win.sesionIniciada(Identificator);
                for(int i=2;i<lista.size();i++){
                    Win.addContacto(lista.get(i));
                }
                break;

            case "NUEVO_USUARIO_CONECTADO":
                Win.addContacto(lista.get(1));
                break;
            case "USUARIO_DESCONECTADO":
                Win.eliminarContacto(lista.get(1));
                break;                
            case "MENSAJE":
                Win.addMensaje(lista.get(1), lista.get(3));
                break;
            default:
                break;
        }
    }
    
    private void enviarSolicitudConexion(String identificador) {
        LinkedList<String> lista=new LinkedList<>();
        lista.add("SOLICITUD_CONEXION");
        lista.add(identificador);
        try {
            ObjectOutput_Stream.writeObject(lista);
        } catch (IOException ex) {
            System.out.println("Error de lectura y escritura al enviar mensaje al servidor.");
        }
    }
    
    void confirmarDesconexion() {
        LinkedList<String> lista=new LinkedList<>();
        lista.add("SOLICITUD_DESCONEXION");
        lista.add(Identificator);
        try {
            ObjectOutput_Stream.writeObject(lista);
        } catch (IOException ex) {
            System.out.println("Error de lectura y escritura al enviar mensaje al servidor.");
        }
    }
    
    String getIdentificador() {
        return Identificator;
    }
    
}

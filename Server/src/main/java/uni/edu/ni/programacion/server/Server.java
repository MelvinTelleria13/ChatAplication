package uni.edu.ni.programacion.server;

/**
 *
 * @author Melvin Telleria
 */

import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import javax.swing.JOptionPane;

public class Server {
   
        private ServerSocket serverSocket;
    LinkedList<ThreadClient> Clients;
    private final WinServer Win;
    private final String Puerto;
    static int Correlativo;
    public Server(String Puerto, WinServer Win) {
        Correlativo = 0;
        this.Puerto = Puerto;
        this.Win = Win;
        Clients =new LinkedList<>();
 
    }
    
    public void run() {
        try {
            serverSocket = new ServerSocket(Integer.valueOf(Puerto));
            Win.addServidorIniciado();
            while (true) {
                ThreadClient h;
                Socket S_ocket;
                S_ocket = serverSocket.accept();
                System.out.println("Nueva conexion entrante: " + S_ocket);
                h = new ThreadClient(S_ocket, this);               
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(Win, "La inicializacion de sevidor ha fallado,por favor cierre y vuelva a intentarlo.");
            JOptionPane.showMessageDialog(Win, "Esta aplicación se cerrará");
            
            System.exit(0);
        }                
    }        

    LinkedList<String> getUsuariosConectados() {
        LinkedList<String>usuariosConectados=new LinkedList<>();
        Clients.stream().forEach(c -> usuariosConectados.add(c.getIdentificador()));
        return usuariosConectados;
    }

    void agregarLog(String texto) {
        Win.agregarLog(texto);
    }
    
}

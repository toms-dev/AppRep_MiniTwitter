package client;

import org.apache.activemq.ActiveMQConnectionFactory;
import server.AccountInformation;

import javax.jms.JMSException;
import javax.jms.Message;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by Fabien on 07/05/15.
 */
public class User implements javax.jms.MessageListener{

    private String pseudo;
    private String password;
    private boolean isConnected;

    //JMS declarations
    private javax.jms.Session receiveSession = null;
    private javax.jms.Queue queue = null;
    private javax.jms.Connection connect = null;



    /**
     * Un utilisateur est identifié par un pseudo et un mot de passe
     * @param pseudo pseudo de l'utilisateur
     * @param password mot de passe de l'utilisateur
     */
    public User(String pseudo, String password){
        this.pseudo = pseudo;
        this.password = password;
        this.isConnected = false;

        // Create a connection.
        javax.jms.ConnectionFactory factory;
        factory = new ActiveMQConnectionFactory("user", "user", "tcp://localhost:61616");
        try {
            connect = factory.createConnection ("user", "user");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * Méthode de connexion de l'utilisateur.
     * Interogation auprès du serveur rmi sur la classe AccountInformationImpl et la methode connect
     * @param port port du serveur rmi (2002 par exemple)
     * @return true ou false indiquant si la connexion a réussie ou non
     */
    public boolean connect(int port){
        if(isConnected){
            return true;
        }
        try {
            //System.out.println("getregistry "+port);
            Registry r = LocateRegistry.getRegistry(port);
            //LocateRegistry.getRegistry();
            //System.out.println("lookup sur server.AccountServer");
            AccountInformation req = (AccountInformation) r.lookup("Server");
            //AccountInformation req = (AccountInformation) Naming.lookup("rmi://localhost:2020/AccountInformation");
            if(req.connect(pseudo, password)){
                //configurer jms server puis start ?
                this.configurerConsommateur();
                this.setIsConnected(true);
                return true;
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println(e.fillInStackTrace());
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return false;
    }


    /*  GETTERS AND SETTERS */
    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String Password) {
        this.password = password;
    }
    public boolean isConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }
    private void configurerConsommateur() throws JMSException {
        // Pour consommer, il faudra simplement ouvrir une session
        receiveSession = connect.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
        javax.jms.MessageConsumer qReceiver = receiveSession.createConsumer(queue);
        qReceiver.setMessageListener(this);
        // Now that 'receive' setup is complete, start the Connection

    }
    @Override
    public void onMessage(Message message) {
        System.out.println("Reception message: "+message.toString());

    }
}

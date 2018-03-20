package client;

import conn.Conn;
import conn.ConnHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Scanner;

public class ConnClient
{
    static final String PREFIX_FOR_CMD = "/";
    static final String CREATE_SERVER = PREFIX_FOR_CMD + "create ";
    static final String LIST_AVAILABLE_SERVER = PREFIX_FOR_CMD + "list";
    static final String CMD_JOIN   = PREFIX_FOR_CMD + "join ";
    static final String CMD_LEAVE  = PREFIX_FOR_CMD + "leave";
    static final String CMD_NAME   = PREFIX_FOR_CMD + "name ";
    static final String CMD_QUIT   = PREFIX_FOR_CMD + "quit";

    static Conn connImpl;
    static String token;

    public static class Input implements Runnable {

        public void run() {
            Scanner in = new Scanner(System.in); //ecoute l'entrée standart
            while (true) {
                String s = in.nextLine();//quand on press "ENTER" on récupère la chaîne écrite
                parse(s);//apelle la fonction parse avec comme paramètre la chaîne écrite
            }
        }

        /**
         * 
         * @param str
         */
        void parse(String str) {
            if (str.startsWith(CREATE_SERVER)) {
                String name = str.substring(CREATE_SERVER.length());
                connImpl.createChatRoom(token, name);
            } else if (str.startsWith(LIST_AVAILABLE_SERVER)) {
                connImpl.listChatRooms(token);
            } else if (str.startsWith(CMD_JOIN)) {
                String name = str.substring(CMD_JOIN.length());
                connImpl.joinChatRoom(token, name);
            } else if (str.startsWith(CMD_LEAVE)) {
                connImpl.leaveChatRoom(token);
            } else if (str.startsWith(CMD_NAME)) {
                String name = str.substring(CMD_NAME.length());
                connImpl.changeName(token, name);
            } else if (str.startsWith(CMD_QUIT)) {
                System.exit(0);
            } else {
                connImpl.sendMessage(token, str);
            }
        }
    }

    public static class Output implements Runnable {

        public void run() {
            while(true) {
                String message = connImpl.receiveMessage(token);
                if (!message.isEmpty()) {
                    System.out.println(message);
                } else {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String args[])
    {
        try{
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get the root naming context
            org.omg.CORBA.Object objRef =
                    orb.resolve_initial_references("NameService");

            // Use NamingContextExt instead of NamingContext. This is
            // part of the Interoperable naming Service.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // resolve the Object Reference in Naming
            String name = "Conn";
            connImpl = ConnHelper.narrow(ncRef.resolve_str(name));

            System.out.println("Obtained a handle on server object: " + connImpl);
            token = connImpl.connect();

            new Thread(new Input()).start();
            new Thread(new Output()).start();

        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
        }
    }

}

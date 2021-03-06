import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
   static ArrayList<ClientHandler> ar = new ArrayList<>();

   public static void main (String[] args) throws IOException {
      ServerSocket serverSocket = new ServerSocket(9563);

      InetAddress ip = null;
      try {
         ip = InetAddress.getLocalHost();
      }
      catch (Exception e) {
         e.printStackTrace();
      }

      System.out.println("Server started. IP Address: " + ip.getHostAddress());

      Socket clientSocket = null;

      while (true) {
         try {
            System.out.println("Waiting for a client");
            clientSocket = serverSocket.accept();
            System.out.println("Client accepted");

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out= new PrintWriter(clientSocket.getOutputStream(), true);

            ClientHandler c = new ClientHandler(clientSocket, in, out);
            Thread t = new Thread(c);
            ar.add(c);
            t.start();
         }
         catch (Exception e) {
            clientSocket.close();
            e.printStackTrace();
         }
      }
   }
}

class ClientHandler implements Runnable {
   private String name;
   final BufferedReader in;
   final PrintWriter out;
   final Socket s;
   boolean isLoggedIn = true;
	
   public ClientHandler (Socket s, BufferedReader in, PrintWriter out) {
      this.s = s;
      this.in = in;
      this.out = out;
   }

   public void setName(String name) {
      this.name = name;
   }

   @Override
   public void run() {
      String received;

      out.println("Hello. Enter your name: ");
      try {
         this.setName(in.readLine());
      }
      catch (Exception e) {
         e.printStackTrace();
      }
      out.println("Great! Your name is " + this.name);

      while (true) {
         try {
            received = in.readLine();
            if (received == null) {
               this.isLoggedIn = false;
               break;
            }
            else if (received.equals("#logout")) {
               this.isLoggedIn = false;
               this.s.close();
               break;
            }
            else if (received.equals("#active")) {
              int i = 1;
               for (ClientHandler mc : Server.ar) {
                  if (mc.isLoggedIn == true) {
                     out.println(i + ": " + mc.name);
                     i++;
                  }
               }
            }
            else {
               StringTokenizer st = new StringTokenizer(received, "@");
               String toSend = st.nextToken();
               String recipient = st.nextToken();

               for (ClientHandler mc : Server.ar) {
                  if (mc.name.equals(recipient) && mc.isLoggedIn == true) {
                     mc.out.println(this.name + ": " + toSend);
                     break;
                  }
               }
            }
         }
         catch (Exception e) {
            e.printStackTrace();
         }
      }
      try { 
		   this.in.close();
		   this.out.close();
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }
}


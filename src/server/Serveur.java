package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import org.json.JSONObject;


public class Serveur {

	public static String getJson() {
		String json = "{\"block\" : \"1\", \"auteur\" : \"SERVEUR\", \"id\" : 2 }";
		return json;
	}

	public static void main(String[] test) throws IOException {

		final ServerSocket serveurSocket  ;
		final Socket clientSocket ;
		final BufferedReader in;
		final PrintWriter out;
		final Scanner sc=new Scanner(System.in);

		try {
			serveurSocket = new ServerSocket(5000);
			
			System.out.println("attend une connexion");
			clientSocket = serveurSocket.accept();
			System.out.println("client connecte");
			
			//flux sortant et entrant
			out = new PrintWriter(clientSocket.getOutputStream());
			in = new BufferedReader (new InputStreamReader (clientSocket.getInputStream()));

			Thread envoi= new Thread(new Runnable() {
				String msg;
				@Override
				public void run() {
					while(true){
						try {
							Thread.sleep(5000);
							msg = getJson();
							System.out.println();
							out.println(msg);
							out.flush();
						} catch (InterruptedException e) {
							//e.printStackTrace();
							System.out.println("Serveur interrompu");
							break;
						}
					}
				}
			});
			envoi.start();

			Thread recevoir= new Thread(new Runnable() {
				String msg ;
				@Override
				public void run() {
					try {


						while(true) {
							msg = in.readLine();
							if(msg != null) {
								
								//Parsing du Json
								JSONObject myResponse = new JSONObject(msg);
								System.out.println("block- "+myResponse.getString("block"));
								System.out.println("id- "+myResponse.getInt("id"));
								System.out.println("auteur- "+myResponse.getString("auteur"));
							}
						}

					} catch (IOException e) {
						//e.printStackTrace();
						System.out.println("le client s'est deconnecte");
						try {
							clientSocket.close();
							serveurSocket.close();
							out.close();
							envoi.interrupt();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			});
			recevoir.start();

		}catch (IOException e) {
			e.printStackTrace();
		}
	}
}
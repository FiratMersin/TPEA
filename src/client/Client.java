package client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import org.json.JSONObject;

public class Client {

	public static String getJson() {
		String json = "{\"block\" : \"0\", \"auteur\" : \"CLIENT\", \"id\" : 1 }";
		return json;
	}
	public static void main(String[] args) {

		final Socket clientSocket;
		final BufferedReader in;
		final PrintWriter out;

		try {
			//Adresse :	127.0.0.1 = localhost
			//Port 	  : 5000
			clientSocket = new Socket("127.0.0.1",5000);

			//flux sortant et entrant
			out = new PrintWriter(clientSocket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			Thread envoyer = new Thread(new Runnable() {
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
							System.out.println("communication interrompue");
							break;
						}
					}
				}
			});
			envoyer.start();

			Thread recevoir = new Thread(new Runnable() {
				String msg;
				@Override
				public void run() {
					try {
						while(true) {
							msg = in.readLine();
							if(msg != null) {
								JSONObject myResponse = new JSONObject(msg);
								System.out.println("block- "+myResponse.getString("block"));
								System.out.println("id- "+myResponse.getInt("id"));
								System.out.println("auteur- "+myResponse.getString("auteur"));
							}
						}

					} catch (IOException e) {
						System.out.println("Connexion perdue avec le serveur :'(");
						envoyer.interrupt();
					}
				}
			});
			recevoir.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
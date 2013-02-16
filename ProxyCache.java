/**
 * ProxyCache.java - Simple caching proxy
 *
 * $Id: ProxyCache.java,v 1.3 2004/02/16 15:22:00 kangasha Exp $
 *
 */

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

public class ProxyCache {
    /** Port for the proxy */
    private static int port;
    /** Socket for client connections */
    private static ServerSocket socket;
    /** Create the ProxyCache object and the socket */
    private static Map<String, String> cache = new Hashtable<String, String>();



    public synchronized static void caching(HttpRequest pedido, HttpResponse resposta) throws IOException{
    	File ficheiro;
    	DataOutputStream paraficheiro;


    	ficheiro = new File("cache/","cached_"+System.currentTimeMillis());
    	paraficheiro = new DataOutputStream( new FileOutputStream(ficheiro));
    	paraficheiro.writeBytes(resposta.toString()); /* Escreve headers */
    	paraficheiro.write(resposta.body); /* Escreve body */
    	paraficheiro.close();
    	cache.put(pedido.URI, ficheiro.getAbsolutePath());
    	System.out.println("Caching from: "+pedido.URI+" para "+ficheiro.getAbsolutePath());
    }


  	public synchronized static byte[] uncaching(String uripedido) throws IOException{
  		File ficheirocached;
  		FileInputStream deficheiro;
  		String hashfile;
  		byte[] bytescached;

  		if((hashfile = cache.get(uripedido))!=null){
  			ficheirocached = new File(hashfile);
  			deficheiro = new FileInputStream(ficheirocached);
  			bytescached = new byte[(int)ficheirocached.length()];
  			deficheiro.read(bytescached);
  			System.out.println("Caching: Hit on "+uripedido+" returning cache to user");
  			return bytescached;
  		}
  		else {
  			System.out.println("Caching: No hit on "+uripedido);
  			return bytescached = new byte[0];
  		}

  	}


  	public static void init(int p) {
	port = p;
		try {
	    	socket = new ServerSocket(port);
		} catch (IOException e) {
	    		System.out.println("Error creating socket: " + e);
	    		System.exit(-1);
			}
  	}



    /** Read command line arguments and start proxy */
    public static void main(String args[]) {
	int myPort = 0;
	File cachedir = new File("cache/");
	if (!cachedir.exists()){cachedir.mkdir();}
    
	try {
	    myPort = Integer.parseInt(args[0]);
	} catch (ArrayIndexOutOfBoundsException e) {
	    System.out.println("Need port number as argument");
	    System.exit(-1);
	} catch (NumberFormatException e) {
	    System.out.println("Please give port number as integer.");
	    System.exit(-1);
	}
	
	init(myPort);

	/** Main loop. Listen for incoming connections and spawn a new
	 * thread for handling them */
	Socket client = null;
	
	while (true) {
	    try {
		client = socket.accept(); /* Aceita novos clientes */
		(new Thread(new Threads(client))).start(); /* Criar threads para cada novo cliente */
	    } catch (IOException e) {
		System.out.println("Error reading request from client: " + e);
		/* Definitely cannot continue processing this request,
		 * so skip to next iteration of while loop. */
		continue;
	    }
	}

    }
}

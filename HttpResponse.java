/**
 * HttpResponse - Handle HTTP replies
 *
 * $Id: HttpResponse.java,v 1.2 2003/11/26 18:12:42 kangasha Exp $
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class HttpResponse {
    final static String CRLF = "\r\n";
    /** How big is the buffer used for reading the object */
    final static int BUF_SIZE = 8192;
    /** Maximum size of objects that this proxy can handle. For the
     * moment set to 100 KB. You can adjust this as needed. */
    final static int MAX_OBJECT_SIZE = 100000;
    /** Reply status and headers */
    String version;
    int status;
    String statusLine = "";
    String headers = "";
    /* Body of reply */
    byte[] body = new byte[MAX_OBJECT_SIZE];

    /** Read response from server. */
    public HttpResponse(DataInputStream fromServer) {
	/* Length of the object */
	int length = -1;
	boolean gotStatusLine = false;

	/* First read status line and response headers */
	try {
	    String line =fromServer.readLine(); /* Ler inputstream do servidor */
	    while (line.length() != 0) {
		if (!gotStatusLine) {
		    statusLine = line;
		    gotStatusLine = true;
		} else {
		    headers += line + CRLF;
		}

		/* Get length of content as indicated by
		 * Content-Length header. Unfortunately this is not
		 * present in every response. Some servers return the
		 * header "Content-Length", others return
		 * "Content-length". You need to check for both
		 * here. */
		if (line.startsWith("Content-Length:") ||
		    line.startsWith("Content-length:")) {
		    String[] tmp = line.split(" ");
		    length = Integer.parseInt(tmp[1]);
		}
		line = fromServer.readLine();
	    }
	} catch (IOException e) {
	}

	try {
	    int bytesRead = 0;
	    byte buf[] = new byte[BUF_SIZE];
	    boolean loop = false;

	    /* If we didn't get Content-Length header, just loop until
	     * the connection is closed. */
	    if (length == -1) {
		loop = true;
	    }
	    
	    /* Read the body in chunks of BUF_SIZE and copy the chunk
	     * into body. Usually replies come back in smaller chunks
	     * than BUF_SIZE. The while-loop ends when either we have
	     * read Content-Length bytes or when the connection is
	     * closed (when there is no Connection-Length in the
	     * response. */
	    while (bytesRead < length || loop) {
		/* Read it in as binary data */
		int res = fromServer.read(buf, 0, BUF_SIZE); /* Lê binary data até BUFF_SIZE para buf (array de bytes) */
		if (res == -1) {
		    break;
		}
		/* Copy the bytes into body. Make sure we don't exceed
		 * the maximum object size. */
		for (int i = 0; 
		     i < res && (i + bytesRead) < MAX_OBJECT_SIZE; 
		     i++) {
		    body[bytesRead + i] = buf[i]; /* copiar bytes lidos para buf para body */
		}
		bytesRead += res;
	    }
 	} catch (IOException e) {
 	    System.out.println("Error reading response body: " + e);
 	    return;
 	}


    }

    /**
     * Convert response into a string for easy re-sending. Only
     * converts the response headers, body is not converted to a
     * string.
     */
    public String toString() {
	String res = "";

	res = statusLine + CRLF;
	res += headers;
	res += CRLF;
	
	return res;
    }
}
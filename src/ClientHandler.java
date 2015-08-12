import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class ClientHandler extends Thread {
    Socket serverSocket;
    ArrayList<ClientHandler> clients;
    
    DataInputStream dis = null;
    DataOutputStream dos = null;
    
    boolean connect;
    String clientName = null;
    String clientMessage = null;
    
    public ClientHandler(Socket serverSocket, ArrayList<ClientHandler> clients)
    {
        this.serverSocket = serverSocket;
        this.clients = clients;
        
        System.out.println("New client connected" + serverSocket.getRemoteSocketAddress().toString());
        
        OutputStream os = null;
        InputStream in = null;
        try {
            in = serverSocket.getInputStream();
            os = serverSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connect = true;
        
        // the raw input stream only deals with bytes so lets wrap it in a data input stream
        
        dis = new DataInputStream(in);            
        dos = new DataOutputStream(os);

    }

    public void run()
    {
        System.out.println("Client connected");
        
        
        while(connect)
        {
            int message;
            try {
                message = dis.read();
                //TODO block and wait for the client to send an appropriate message
            
                // decode the message based on message id
                if(message == ServerConstants.CHAT_MESSAGE)
                {
                	clientMessage = dis.readUTF();
                	System.out.println(clientName + ": " + clientMessage);
                	for (int i=0; i<clients.size(); i++){
                		clients.get(i).dos.write(ServerConstants.BROADCAST_MESSAGE);
                		clients.get(i).dos.writeUTF(clientName + ": " + clientMessage);
                		
                	}                	               	
                	//TODO process chat messages from our chat client
                }
                else if(message == ServerConstants.SETUP_MESSAGE)
                {
                	connect = true;
                    clientName = dis.readUTF();
                    System.out.println("Client Name:"+clientName);
                    
                    // ack that we have recieved the client's name
    
                    dos.write(ServerConstants.SETUP_ACK);
                }
                else if(message == ServerConstants.CLOSE_CONNECTION)
                {
                	connect = false;
                	clients.remove(this);
                	dis.close();
                	dos.flush();
                	dos.close();
                	serverSocket.close();
                	
                }
                else if(message == ServerConstants.UPDATE_USERLIST)
                {
                	String user = "";
                	for (int i=0; i<clients.size(); i++){
                		String name = clients.get(i).clientName;
                		String ip = clients.get(i).serverSocket.getLocalAddress().toString();
                		user += name + " @" + ip + "\n";
                	}
                	dos.write(ServerConstants.UPDATE_ACK);
                	dos.writeUTF(user);              		            	
                }
                else if(message == ServerConstants.CHECK_PASSWORD)
                {
                	String username = dis.readUTF();
                	String password = dis.readUTF();
                	System.out.println(username);
                	boolean correct_password = checkpassword(username, password);  
                	dos.writeUTF(String.valueOf(correct_password));
                	if(!correct_password){
                		connect = false;
                    	clients.remove(this);
                    	dis.close();
                    	dos.flush();
                    	dos.close();
                    	serverSocket.close();
                	}
                	//dos.writeUTF("true");
                }
                if(message == ServerConstants.PRIVATE_CHAT)
                {
                	String name = dis.readUTF();
                	String privateMessage = dis.readUTF();
                	
                	for (int i=0; i<clients.size(); i++){
                		if(clients.get(i).clientName.equals(name)){
                			if(clients.get(i).connect){
                				clients.get(i).dos.write(ServerConstants.BROADCAST_MESSAGE);
                				clients.get(i).dos.writeUTF(clientName + ": " + privateMessage);
                			}
                		}	
                		
                	}                	               	
                	//TODO process chat messages from our chat client
                }
            
            
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
    }
    }

	private boolean checkpassword(String username, String userpassword) {
		boolean correct_password = false;
		File file = new File("src/password.txt");
    	if(file.isFile() && file.exists()){
    		try{
	            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
	            String userDetail = "";
	            String userName = "";
	            String password = "";
	            while((userDetail = br.readLine()) != null){
	            	if(userDetail.contains(username)){
	            		userName = userDetail.substring(0, userDetail.indexOf(" "));
	            		password = userDetail.substring(userDetail.indexOf(" ")+1, userDetail.length());
	            	}
	            	
	            }
	            br.close();
	            
	            
	            if(userName.equals(username) 
	            		&& password.equals(userpassword)){
			            	System.out.println("Login successful");
			            	correct_password = true;  
	            }
	            
    		} catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
    	}else{
    		System.out.println("File Not Found!");
    	}
		return correct_password;
		
	}
}

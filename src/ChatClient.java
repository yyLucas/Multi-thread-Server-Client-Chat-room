import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class ChatClient extends JFrame{
	
	private JFrame frame;
	private JButton sendButton;
	private JLabel userNameLabel;
	private JLabel passwordLable;
	private JTextArea chatTextArea;
	private JTextField usernameTextFiled;	
	private JPasswordField passwordTextFiled;
	private JTextField inputTextField;
	private JButton connectButton;
	private JButton disconnectButton;
	private JButton refresh;
	
	private JList<String> userList; 
	
	private DefaultListModel<String> listModel;
	private JPanel northPane;
	private JSplitPane centrePane;
	private JPanel southPane;
	private JScrollPane rightScroll;
	private JScrollPane leftScroll;
	
	private boolean isConnected;
	private DataOutputStream dos;
	private DataInputStream dis;
	private String username;
	private Thread serverHandler;
	private Socket client;
	

    
	public void creatFrame() {
		isConnected = false;
		chatTextArea = new JTextArea();  
		chatTextArea.setEditable(false);
		chatTextArea.setLineWrap(true);
		chatTextArea.setFont(new Font("Courier", Font.PLAIN, 16));
		chatTextArea.setForeground(Color.blue);  
        inputTextField = new JTextField(); 
        inputTextField.setFont(new Font("Courier", Font.PLAIN, 16));
        userNameLabel = new JLabel("UserName: ");
        userNameLabel.setHorizontalAlignment(JLabel.CENTER);
        passwordLable = new JLabel("Password: ");
        passwordLable.setHorizontalAlignment(JLabel.CENTER);
        usernameTextFiled = new JTextField("username"); 
        passwordTextFiled = new JPasswordField("password"); 
        connectButton = new JButton("Connect"); 
        disconnectButton = new JButton("Disconnect"); 
        sendButton = new JButton("Send");
        
        listModel = new DefaultListModel<String>();  
        userList = new JList<String>(listModel); 
        refresh = new JButton("refresh");
        userList.setFont(new Font("Courier", Font.PLAIN, 16));
  
        northPane = new JPanel();  
        northPane.setLayout(new GridLayout(1,6));   
        northPane.add(userNameLabel);    
        northPane.add(usernameTextFiled);  
        northPane.add(passwordLable);
        northPane.add(passwordTextFiled);
        northPane.add(connectButton);
        northPane.add(disconnectButton);
        northPane.setBorder(new TitledBorder("User Login"));  
  
        rightScroll = new JScrollPane(chatTextArea);  
        rightScroll.setBorder(new TitledBorder("Messages"));  
        leftScroll = new JScrollPane(userList);  
        leftScroll.setBorder(new TitledBorder("Online Users")); 
        northPane.add(refresh);
       
        
        southPane = new JPanel(new BorderLayout());  
        southPane.setPreferredSize(new Dimension(800,80));
        southPane.add(inputTextField, "Center");  
        southPane.add(sendButton, "East");  
        southPane.setBorder(new TitledBorder("Write your messages"));  
  
        centrePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);  
        centrePane.setDividerLocation(250);  
  
        frame = new JFrame("chat room");  
        
        frame.setLayout(new BorderLayout());  
        frame.add(northPane, "North");  
        frame.add(centrePane, "Center");  
        frame.add(southPane, "South");  
        frame.setSize(800, 600);
        
        frame.setVisible(true);
        
        //Stackoverflow Retrieve from : http://stackoverflow.com/questions/9093448/
        //do-something-when-the-close-button-is-clicked-on-a-jframe
        
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            	if (JOptionPane.showConfirmDialog(frame, 
                        "Are you sure to close this window?", "Really Closing?", 
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
            			disconnect();
                        System.exit(0);
                    }
            }
        });
        
        refresh.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent arg0) {
            	checkOnlineUser();
            }  
        });
        
        inputTextField.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent arg0) {  
                sendMessage();  
            }  
        }); 
        
        sendButton.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
            	sendMessage(); 
            }  
        }); 
        
        connectButton.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
            	if(isConnected){
            		JOptionPane.showMessageDialog(frame, "Already connected!", "Error",  
	                        JOptionPane.ERROR_MESSAGE);  
	                return;
            	}else{
            		checkPassword(); 
            	}
            	
            }  
        });
        
        disconnectButton.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
                if (!isConnected) {  
                    JOptionPane.showMessageDialog(frame, "Please connect to the server first",  
                            "Error", JOptionPane.ERROR_MESSAGE);  
                    return;  
                }  
                disconnect();
                
            }  
        }); 
        
        userList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
            	
            }
        });
        
        userList.addMouseListener( new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                if ( SwingUtilities.isRightMouseButton(e) )
                {
                    userList.clearSelection();                  
                }
            }

        });
        
	}
	
	public void checkOnlineUser(){
		if(isConnected){
        	try {
        		userList.clearSelection();	
				dos.write(ServerConstants.UPDATE_USERLIST);			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}else{
    		JOptionPane.showMessageDialog(frame, "Please connect to the server first", "Error",  
    				JOptionPane.ERROR_MESSAGE);  
    		return;

    	}
	}
	
	public void disconnect(){
		if(isConnected){
			try {  
	        	isConnected = false; 
	        	dos.write(ServerConstants.CLOSE_CONNECTION);
	            System.out.println("Disconnect!");  
	        } catch (Exception exc) {  
	            JOptionPane.showMessageDialog(frame, exc.getMessage(),  
	                    "Error", JOptionPane.ERROR_MESSAGE);  
	        } 
		}
	}
	
	public void checkPassword() { 
		
		try {
			client = new Socket("localhost",5000);
			dos = new DataOutputStream(client.getOutputStream());
	        dis = new DataInputStream(client.getInputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}        
        
		String username = usernameTextFiled.getText();
		char[] pass = passwordTextFiled.getPassword();
        String passString = new String(pass);
        String connect = " ";
        
        try {
        	dos.write(ServerConstants.CHECK_PASSWORD);
        	dos.writeUTF(username);
        	dos.writeUTF(passString);
			connect = dis.readUTF();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        boolean right_password = Boolean.valueOf(connect);
        if(right_password){
        	System.out.println("Login successful");
        	isConnected = true;
        	connect(); 
        }else{
	        JOptionPane.showMessageDialog(frame, "Wrong username or password", "Error",  
	        JOptionPane.ERROR_MESSAGE);  
	        return; 
        }
	}
	
	public void sendMessage() {  
        if (!isConnected) {  
            JOptionPane.showMessageDialog(frame, "Please login first!", "Error",  
                    JOptionPane.ERROR_MESSAGE);  
            return;  
        }  
        String message = inputTextField.getText();  
        if (message == null || message.equals("")) {  
            JOptionPane.showMessageDialog(frame, "Can't send blank message!", "Error",  
                    JOptionPane.ERROR_MESSAGE);  
            return;  
        } 
        if(userList.getSelectedValue() == null){
	        try {
	        	dos.write(ServerConstants.CHAT_MESSAGE);
				dos.writeUTF(message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        if (userList.getSelectedValue() != null){
        	try {
        		//System.out.println(userList.getSelectedValue());
		        	dos.write(ServerConstants.PRIVATE_CHAT);
		        	String name = userList.getSelectedValue().substring(0, userList.getSelectedValue().indexOf(" "));
		        	dos.writeUTF(name);
					dos.writeUTF(message);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        inputTextField.setText(null); 
        checkOnlineUser();
    } 
	
	public void updateUser(){

		String user = " ";
		listModel.removeAllElements();
		try {

			user = dis.readUTF();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		String[] list = user.split("\n");
		//System.out.println(user);
		for(int i=0; i<list.length; i++){
			
			listModel.addElement(list[i]);
		}
	            
    	
	}
	
	public void connect(){
		try {
        	// setup BufferedReader for console / keyboard input
                             
            username = usernameTextFiled.getText();
            chatTextArea.append("["+ username +"]" + "enter the room\n" );
            
            dos.write(ServerConstants.SETUP_MESSAGE);
            dos.writeUTF(username);
            
            System.out.println("Simple chat client.");
            
            // setup another thread to handle network I-O from ClientHandler 
            serverHandler = new Thread()
            {
                public void run()
                {
                    while(isConnected)
                    {
                        int message;
                        
                        try {
                            message = dis.read();
                            if ((message != -1) && (message != 0)){
                            // once we have setup the connection wait for a response from the server
	                            if(message == ServerConstants.SETUP_ACK || message == ServerConstants.CHAT_ACK)
	                            {
	                            	checkOnlineUser();
	                            }
	                            else if(message == ServerConstants.BROADCAST_MESSAGE)
	                            {
	                            	String broadcastMessage = dis.readUTF();
	                            	chatTextArea.append(broadcastMessage + "\n" );
	                            	checkOnlineUser();
	                            	//TODO process broadcast message from other chat client
	                            }
	                            else if(message == ServerConstants.UPDATE_ACK)
	                            {
	                            	updateUser();
	                            }
	                            else
	                            {
	                            	// throw an exception if we are sent some strange message
	                                throw new UnsupportedOperationException("Unknown message type");
	                            }
                            }
    
                        }
                        catch (IOException e)
                        {
                        	System.err.println(e);
                            break;
                        }
                    }
                }
            };
            
            serverHandler.start();
            
            
	        } catch (UnknownHostException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}
	
    public static void main(String[] args)
    {
    	//TODO the code below needs to be re-structured to work
    	// within the constraints of GUI framework i.e. the ChatClient
    	// will need to extend a JFrame and the network messages below
    	// will need to be modified so that they are sent based on GUI
    	// events. The messages received should be displayed in a JTextArea 
    	ChatClient chatClient = new ChatClient();
    	chatClient.creatFrame();
    	       
        
    }
}

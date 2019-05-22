// Java implementation of Server side
// contains two classes : Server and client handler
import java.lang.*;
import java.io.*;
import java.text.*;
import java.net.*;
import java.util.logging.*;
import java.util.Calendar;
public class newServer
{
	public static void main(String[] args) throws IOException
	{
		// Server is listening on port 8080
		ServerSocket serverSocket = new ServerSocket(8080);
		
		//Running infinite loop for getting client request
		while (true)
		{
			Socket socket = null;
			Logger logger = Logger.getLogger("RPCLog");
			FileHandler fh = null;
			try
			{
				// socket object to receive incoming client requests
				socket = serverSocket.accept();
				
				SimpleDateFormat format = new SimpleDateFormat("M-d-HH-mm-ss");
				fh = new FileHandler("MyLogFile_" + format.format(Calendar.getInstance().getTime())+".log");
				fh.setFormatter(new Formatter() {
					@Override
					public String format(LogRecord record)
					{
						SimpleDateFormat logTime = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
						Calendar cal = Calendar.getInstance();
						cal.setTimeInMillis(record.getMillis());
						return record.getLevel()
								+ logTime.format(cal.getTime())
								+ " || "
								+ record.getSourceClassName().substring(
									record.getSourceClassName().lastIndexOf(".")+1,
									record.getSourceClassName().length())
								+ "."
								+ record.getSourceMethodName()
								+ "() : "
								+ record.getMessage() + "\n";
					}
				});
				logger.addHandler(fh);
				logger.setUseParentHandlers(false);
				System.out.println("Client connected: "+ socket);
				logger.info("Client Connected: "+ socket + "\n");
				System.out.println("Assigning new thread for this client");
				logger.info("Assigning new thread for this client\n");
				// Create a new thread object
				Thread t = new ClientHandler(socket,logger);
				
				// Invoking the start method
				t.start();
			}
			
			catch (Exception e)
			{
				logger.severe("Exception occured:" + e.getLocalizedMessage() + "\n");
				socket.close();
				fh.close();
				e.printStackTrace();
			}
		}
	}
}

class ClientHandler extends Thread
{
	final Socket socket;
	final Logger logger;
	
	// Constructor
	public ClientHandler(Socket socket, Logger logger)
	{
		this.socket = socket;
		this.logger = logger;
	}
	
	@Override
	public void run()
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
		
			boolean next=true;
			while(next)
			{
				String result="Start\n";
				String received;
				String st;
				
				try
				{
					// ask client what he wants
					writer.write("what do you want me to do?\n");
					// receive info from client
					logger.info("Waiting for input from client\n");
					writer.newLine();
					writer.flush();
					received = reader.readLine();
					/*	
					if(received.equals("EXIT"))
					{
						logger.info("Entered EXIT..So Client disconnecting\n");
						System.out.println("client "+ this.socket + " sends exit...");
						System.out.println("closing the connection.");
						this.socket.close();
						System.out.println("Connection closed");
						break;
					}
					*/
					
					String[] elements = received.trim().replaceAll(" +", " ").split(" ");
					switch(elements[0])
					{
						case "EXIT":
						case "exit":
							logger.info("Entered EXIT..So Client disconnecting\n");
							System.out.println("client "+ this.socket + " sends exit...");
							System.out.println("closing the connection.");
							this.socket.close();
							System.out.println("Connection closed");
							next = false;
							break;
						case "READ":
						case "read":
							logger.info("Inside Read Case\n");
							if(elements.length != 2)
							{
								result = "INVALID_COMMAND";
								writer.write(""+result);
								writer.newLine();
								writer.flush();
								break;
							}
							File file = new File(elements[1]);
							if(!file.exists())
							{
								result = "INVALID_FILE";
								writer.write(""+result);
								writer.newLine();
								writer.flush();
								break;
							}
							BufferedReader br = new BufferedReader(new FileReader(file));
							while((st=br.readLine()) != null)
							{
								result += st +"\n";
								//System.out.println(st);
							}
							writer.write(""+result);
							writer.newLine();
							writer.flush();
							break;
					
						case "EXEC":
						
						case "exec":
							logger.info("Inside Exec Case\n");
							String command="cmd.exe /c ";
							if(elements.length < 2)
							{
								result = "INVALID_COMMAND";
								writer.write(""+result);
								writer.newLine();
								writer.flush();
								break;
							}
							for(int i=1; i<elements.length;i++)
							{
								command += elements[i] + " ";
							}	
							logger.info("Executing "+command+" command\n");
							Process p = Runtime.getRuntime().exec(command);
			
							BufferedReader cmdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
							BufferedReader cmderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			
							//read the output from the command
							System.out.println();
							while((st=cmdout.readLine())!=null)
							{
								result += st +"\n";
								//System.out.println(st);
							}
							result += "\n";
							while((st = cmderr.readLine()) != null)
							{
								result += st + "\n";
								System.out.println(st);
							}
							int exitValue = p.waitFor();
							System.out.println("Process exitValue: " + exitValue);
							if(exitValue == 1)
							{
								result = "INVALID_PROGRAM";
								writer.write(""+ result);
								writer.newLine();
								writer.flush();
								break;
							}
							writer.write(""+ result + "END.");
							writer.newLine();
							writer.flush();
							break;
					
						default:
							logger.info("Inside default\n");
							result = "Invalid arguments .. Try Again\n";
							writer.write(""+ result + "END.");
							writer.newLine();
							writer.flush();
					}
				}
				catch (IOException e)
				{
					logger.severe("IOException: " + e.getLocalizedMessage() + "\n");
					e.printStackTrace();
				}
				catch (Exception e)
				{
					logger.severe("Exception: " + e.getLocalizedMessage() + "\n");
					e.printStackTrace();
				}
			}
			try
			{
				logger.info("Trying to close Writer and Reader\n");
				reader.close();
				writer.close();
			}
			catch(IOException e)
			{
				logger.severe("IOException: " + e.getLocalizedMessage() + "\n");
				e.printStackTrace();
			}
		
		}
		catch(IOException e)
		{
			logger.severe("IOException: "+e.getLocalizedMessage() + "\n");
			e.printStackTrace();
		}
		
	}
}

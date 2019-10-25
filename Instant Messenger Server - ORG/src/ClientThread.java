import javafx.application.Platform;

import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread
{
	private String clientName = null;
	private BufferedReader input;
	private PrintStream output;
	private Socket socket;
	private final ClientThread[] threads;
	private String toLast = "";

	public ClientThread(Socket socket, ClientThread[] threads)
	{
		this.socket = socket;
		this.threads = threads;

	}
	public void run()
	{
		ClientThread[] threads = this.threads;
		try
		{
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new PrintStream(socket.getOutputStream());
			String list = "";
			for (int i = 0; i < threads.length; i++)
			{
				if (threads[i] != null && threads[i] != this && threads[i].clientName != null && !threads[i].clientName.equals(null) && !threads[i].clientName.equals(""))
				{
					list += "active|" + threads[i].clientName + "|message" + "\n";
				}
			}
			list += "|messagelast";
			sendToClient(list);
			String text = "";
			while ((text = input.readLine()) != null)
			{
				ServerGUI.textArea.appendText("From " + clientName + ":" + text + "\n");
				if (clientName == null)//get name
				{
						clientName = text.substring(5, text.length() - 8);
						for (int i = 0; i < threads.length; i++)
						{
							if (threads[i] != null && threads[i] != this)
							{
								threads[i].sendToClient("enter|" + clientName + "|||message");
							}
						}
				}
				else//got name
				{
					if (text.startsWith("message"))
					{
						text = text.substring(8);
						toLast = text.substring(0, text.indexOf("|"));
						if (text.startsWith("|"))
						{
							toLast = "|";
						}
						for (int i = 0; i < threads.length; i++)
						{
							if (threads[i] != null && threads[i] != this)
							{
								if (text.startsWith("|"))
								{
									threads[i].sendToClient("message|" + clientName + "|" + toLast + "|" + text.substring(2));
								}
								else if (toLast.equals(threads[i].clientName))
								{
									threads[i].sendToClient("message|" + clientName + "|" + toLast + "|" + text.substring(text.indexOf("|") + 1));
								}
							}
						}
					}
					else if (text.startsWith("leave"))//wants to leave
					{
						close();
					}
					else //append
					{
						for (int i = 0; i < threads.length; i++)
						{
							if (threads[i] != null && threads[i] != this)
							{
								threads[i].sendToClient("appendL|" + clientName + "|" + toLast + "|" + text);
							}
						}
					}
				}

			}
		}
		catch (Exception e)
		{
		}
		close();

	}
	private void sendToClient(String text)
	{
		ServerGUI.textArea.appendText("From " + clientName + ":" + text + "\n");
		output.println(text);
		output.flush();
	}
	public void close()
	{
		try
		{
			for (int i = 0; i < threads.length; i++)
			{
				if (threads[i] != null && threads[i] != this)
				{
					threads[i].sendToClient("leave|" + clientName + "|||message");
				}
			}
		}
		catch (Exception e)
		{
		}output.close();
		try
		{
			input.close();
			socket.close();
		}
		catch (Exception e)
		{
		}

		for (int i = 0; i < threads.length; i++)
		{
		if (threads[i] == this)
		{
			threads[i] = null;
			break;
		}
		}
	}
}

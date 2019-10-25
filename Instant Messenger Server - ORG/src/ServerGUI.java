import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerGUI extends Application
{
	public static TextArea textArea;
	public static void main(String[] args)
	{
		launch(args);
	}
	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	private static final int maxClientsCount = 10;
	private static final int serverPort = 1234;
	private static final ClientThread[] threads = new ClientThread[maxClientsCount];

	private void startServer()
	{
		try
		{
			serverSocket = new ServerSocket(serverPort);
		}
		catch (Exception e)
		{
		}
		Runnable myRunnable = new Runnable()
		{
			public void run()
			{
				while (true)
				{
					try
					{
						clientSocket = serverSocket.accept();
						int i = 0;
						while (i < maxClientsCount)
						{
							if (threads[i] == null)
							{
								(threads[i] = new ClientThread(clientSocket, threads)).start();
								break;
							}
							i++;
						}
						if (i == maxClientsCount)
						{
							PrintStream os = new PrintStream(clientSocket.getOutputStream());
							os.close();
							clientSocket.close();
						}
					}
					catch (Exception e)
					{
					}
				}
			}
		};
		Thread thread = new Thread(myRunnable);
		thread.start();
	}
	@Override
	public void start(Stage primaryStage)
	{
		primaryStage.setTitle("Instant Messenger Server");
		primaryStage.setWidth(500);
		primaryStage.setResizable(false);
		primaryStage.setHeight(500);
		BorderPane layout = new BorderPane();
		Scene scene = new Scene(layout);
		textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setText("Server Running\n");
		layout.setCenter(textArea);
		primaryStage.setScene(scene);
		primaryStage.show();
		startServer();
	}
	@Override
	public void stop()
	{
		for (int i = 0; i < maxClientsCount; i++)
		{
			if (threads[i] != null)
			{
				threads[i].close();
			}
		}
		Platform.exit();
		System.exit(0);
	}
}

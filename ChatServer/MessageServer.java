/*
-* This project is our own work. We have not recieved assistance beyond what is
-* normal, and we have cited any sources from which we have borrowed. We have
-* not given a copy of our work, or a part of our work, to anyone. We are aware
-* that copying or giving a copy may have serious consequences.
-*
-* @author Ian Duffy, 11356066
-* @author Richard Kavanagh, 11482928
-* @author Darren Brogan, 11424362
-*/

import java.util.*;

/**
 * MessageServer is responsible for recieving and sending all of the clients
 * messages.
 *
 * MessageServer holds:
 * - A buffer of all messages.
 * - A list of all connections.
 */
class MessageServer extends Thread {
  // Message buffer.
  private ArrayList<String> buffer;

  // Connection list.
  private ArrayList<Connection> connections;

  // Maximum about of connections.
  private final int MAXCONNECTIONS;

  MessageServer(int MAXCONNECTIONS) {
    this.MAXCONNECTIONS = MAXCONNECTIONS;
    this.buffer = new ArrayList<String>();
    this.connections = new ArrayList<Connection>();
  }

  /// Adds a new connection to the connections list.
  public synchronized void addConnection(Connection connection) {
    // Adds the connection.
    connections.add(connection);
    // Inform the server operator of a change in the amount of clients.
    System.out.println("Clients: " + connections.size());
  }

  /// Deletes a connection from the connections list.
  public synchronized void deleteConnection(Connection connection) {
    // Find the connection in the list.
    int connectionIndex = connections.indexOf(connection);

    // If it was found remove it.
    if(connectionIndex != -1) {
      connections.remove(connectionIndex);

      // Inform the server operator of a change in the amount of clients.
      System.out.println("Clients: " + connections.size());

      // Inform all other connections.
      addMessage(connection.nickname + " has left the chatroom...");
    }
  }

  public synchronized Connection getConnection(String recipient) {
    Connection result = null;
    for(Connection connection : connections) {
      if(connection.nickname.equals(recipient)) {
        result = connection;
        break;
      }
    }
    return result;
  }

  /// Adds a message to the buffer.
  public synchronized void addMessage(String message) {
    buffer.add(message);

    // Notify getNextMessage() that the message list is no longer empty.
    notify();
  }

  /// Waits until the buffer is not empty and then returns the first message.
  public synchronized String getNextMessage() throws InterruptedException {
    while(buffer.size() == 0) {
      wait();
    }

    // Get the message from the buffer.
    String message = buffer.get(0);

    // Remove the message from the bufffer.
    buffer.remove(0);

    return message;
  }

  /// Passes the message onto all the connections.
  private synchronized void sendMessageToAll(String message) {
    for(Connection connection : connections) {
      connection.sender.addMessage(message);
    }
  }

  /// Reports whether or not the server is at maximum connections.
  public synchronized boolean isFull() {
    return (connections.size() >= MAXCONNECTIONS);
  }

  /// Send messages from the buffer to clients.
  public void run() {
    while(true) {
      try {
        // Sleep for 100 milliseconds, don't want to hog the CPU.
        sleep(100);

        // Get message from buffer.
        String message = getNextMessage();

        // Send to Client.
        sendMessageToAll(message);
      } catch(Exception e) { }
    }
  }
}
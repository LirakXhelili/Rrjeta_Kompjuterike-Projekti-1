# Rrjeta_Kompjuterike-Projekti-1
# README

## Chat Server and Client

This repository contains Java code for a simple chat server and client application. The server handles multiple clients, allowing them to exchange messages and execute certain commands.

### Server

#### Usage

1. Compile the Server.java file.
   ```bash
   javac Server.java
   ```

2. Run the compiled server file.
   ```bash
   java Server
   ```

3. The server will be listening for client connections on `localhost:2911`.

#### Features

- **User Authentication**: Clients can authenticate themselves using a password.
- **Command Handling**: Server processes various commands, including reading, writing, executing, listing clients, and sending private messages.
- **File Operations**: Supports reading, writing, and executing files on the server.

#### Commands

- `/lexo <filename>`: Read the content of a file.
- `/shkruaj <filename> <text>`: Write text to a file.
- `/ekzekuto <filename>`: Execute a file.
- `/dalje`: Disconnect from the server.
- `/lista`: List connected clients.
- `/password <password> <username>`: Authenticate with a password.
- `/listofile`: List files in the server's current directory.
- `/msg <username> <message>`: Send a private message to a specific user.

### Client

#### Usage

1. Compile the Client.java file.
   ```bash
   javac Client.java
   ```

2. Run the compiled client file.
   ```bash
   java Client
   ```

3. Follow the prompts to enter your username and password.

#### Features

- **User Authentication**: Clients authenticate themselves with a username and password.
- **Chat Interaction**: Clients can send messages and private messages to other users.
- **Graceful Exit**: Clients can exit the chat by typing `/dalje`.

### Notes

- Ensure both server and client are running on the same machine.
- Customize the server port, password, and other settings as needed.
- This is a basic implementation and might need enhancements for production use.

Feel free to explore, modify, and extend the functionality as per your requirements.
# Java TFTP Server & Client

This project implements an extended version of the TFTP (Trivial File Transfer Protocol) in Java using TCP. It was developed for a systems programming course and includes both the server and client components.

The server follows the **Thread-per-Client (TPC)** design pattern, while the client supports interactive file upload/download commands. The communication protocol uses binary-encoded messages based on the extended TFTP spec.

## 🧠 Project Features

### Server

- Thread-per-client architecture
- Binary encoder/decoder for packet types
- Full TFTP protocol support:
  - Login (`LOGRQ`)
  - File upload (`WRQ`)
  - File download (`RRQ`)
  - File deletion (`DELRQ`)
  - Directory listing (`DIRQ`)
  - Disconnect (`DISC`)
- Error handling with TFTP-specific error codes
- Broadcast notifications (`BCAST`) to all clients on file add/delete

### Client

- Two-thread architecture: keyboard input + server listener
- Supports command-line interaction (RRQ, WRQ, DIRQ, LOGRQ, etc.)
- Local validation (e.g., file existence)
- Handles DATA, ACK, BCAST, and ERROR packets

## ⚙️ Technologies Used

- Java 11+
- Maven
- TCP sockets
- Java NIO
- UTF-8 encoding

## 🗂️ Project Structure


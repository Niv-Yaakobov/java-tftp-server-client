# Java TFTP Server & Client (Extended Protocol)

This project implements an extended version of the Trivial File Transfer Protocol (TFTP) using Java TCP sockets. It supports file upload, download, deletion, directory listing, and client management over a binary protocol. Built as a major assignment for the **Systems Programming** course.

## 🎯 Objective

- Build a custom server based on the **Thread-per-Client (TPC)** pattern using generics and interfaces.
- Develop a functional command-line **client**.
- Design a **binary message encoder/decoder** and a complete **TFTP-based protocol** with client-server communication.

## 🧱 Key Components

### ✅ Server

- Accepts multiple clients using a `TPCServer` loop.
- Each connection spawns a handler implementing `ConnectionHandler<T>`.
- Uses `ConnectionsImpl<T>` to broadcast messages or target specific clients.
- Supports `BidiMessagingProtocol<T>` for bi-directional messaging.

### ✅ Client

- Two threads:
  - `KeyboardThread` — reads and encodes commands from user input.
  - `ListeningThread` — listens for server responses and handles protocol decoding.
- Interacts with server using binary-encoded messages (UTF-8, big-endian format).

## 🧪 Supported TFTP Commands

| Command | Description |
|---------|-------------|
| `LOGRQ <username>` | Logs into the server |
| `WRQ <filename>` | Uploads file to server |
| `RRQ <filename>` | Downloads file from server |
| `DELRQ <filename>` | Deletes file from server |
| `DIRQ` | Lists all server files |
| `DISC` | Disconnects from server |

## 🧠 Packet Format

Each message starts with a **2-byte opcode** followed by fields specific to the command.

### Example: `LOGRQ` Packet
```
[0, 7] + <username bytes> + [0]
```

### Other Opcodes
| Opcode | Command |
|--------|---------|
| 1 | RRQ (Read Request) |
| 2 | WRQ (Write Request) |
| 3 | DATA |
| 4 | ACK |
| 5 | ERROR |
| 6 | DIRQ |
| 7 | LOGRQ |
| 8 | DELRQ |
| 9 | BCAST |
| 10 | DISC |

## ⚙️ How to Run

### 🖥️ Run the Server

```bash
cd server
mvn compile
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.tftp.TftpServer" -Dexec.args="7777"
```

### 💻 Run the Client

```bash
cd client
mvn compile
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.tftp.TftpClient" -Dexec.args="127.0.0.1 7777"
```

## 🧪 Testing Tips

- Test file uploads/downloads with `.mp3`, `.txt`, or any other valid files.
- Try error cases (e.g., sending `RRQ` without logging in).
- Use UTF-8 filenames with spaces — they are supported.
- You can simulate a client using a **Rust test client**:  
  [https://github.com/bguspl/TFTP-rust-client](https://github.com/bguspl/TFTP-rust-client)

## 🔧 Developer Notes

- Use `TftpEncoderDecoder` to encode/decode binary protocol messages.
- Protocol implementation is in `TftpProtocol`.
- All input/output is handled through `ConnectionHandler<T>` and `Connections<T>`.
- You **must** use **Big-endian** byte order for all numbers.

## ✍️ Author

- [Niv Yaakobobv](https://github.com/Niv-Yaakobov)

> Project developed as part of **SPL241 – Systems Programming**  
> Ben-Gurion University, Spring 2024

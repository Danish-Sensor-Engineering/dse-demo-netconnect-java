# DSE Net Connect - Client Example  

This repository serves as example of how to connect to _DSE Net Connect_ and receive measurements over the network.

DSE Net Connect is an additional device that manages the basic serial communication and enables simple setup through a web interface. Multiple applications can connect through the network (TCP/IP) to access measurement results. Additionally, the Net Connect device functions as a power supply with voltage monitoring capabilities.

The typical flow:

- Open a TCP socket to DSE Net Connect (default port 2730) 
- Send control commands to start, stop, etc. in plaintext
- In a loop, read the byte input-stream and parse the data
  - Use the measurement result in your business logic 
- Send the 'exit' commands to disconnect 


## Network Datagram

The data received always consist of a _header_ and some _data_ of varied length.

| HEADER   | DATA                               |
|----------|------------------------------------| 
| 22 bytes | 4 bytes or more, depending on type |


### Header

The header is always 22 bytes and tells us about the type of data (distance measurement, 2d profile sweep, or error) and other information.

| Bytes | Java Type | Header Name | Description                                                          |
|-------|-----------|-------------|----------------------------------------------------------------------|
| 2     | short     | ID          | Unique fingerprint for this kind of binary payload                   |
| 2     | short     | VERSION     | Datagram version to accommodate for future changes                   |
| 2     | short     | TYPE        | 1=Error, 11=Distance or 21=Profile                                   |
| 4     | int       | LENGTH      | Size of datagram including header of 22 bytes                        |
| 4     | int       | SEQUENCE    | Counter that wraps at Integer.MAX and starts over          |
| 8     | long      | TIMESTAMP   | Timestamp in milliseconds (Unix Epoch) when measurement was received |



### Data

Following the 22 bytes header, we get 4 bytes or more data depending on the type.

| Type (from header)   | Description & Size                                           |
|----------------------|--------------------------------------------------------------| 
| 1 = Error            | 4 Bytes error code                                           |
| 11 = Distance        | 4 Bytes distance                                             |
| 21 = 2D Profile      | 16 Bytes (for X/Y) x scan of x points (determined by length) |
| 22 = 2D Profile 400  | 16 Bytes (for X/Y) x scan of 400 points = 6400 bytes         |
| 23 = 2D Profile 800  | 16 Bytes (for X/Y) x scan of 800 points = 12800 bytes        |
| 24 = 2D Profile 1600 | 16 Bytes (for X/Y) x scan of 1600 points = 25600 bytes       |



 ## Control Commands

The session can be controlled with the following commands, which should be sent in plaintext following a newline.

| Command | Description                         |
|---------|-------------------------------------| 
| ping    | Should respond 'pong' as a test     |
| start   | Start flow of measurement data      |
| stop    | Stop flow of measurement data       |
| ascii   | Send ASCII data (only measurement)  |
| binary  | Send BINARY data (default)          |
| exit    | Exit and disconnect                 |                           |
| quit    | Exit and disconnect                 |


## Error Codes

If the datagram header is of type=1 (Error), the 4-bytes data (integer) can be used to tell us about the error.

| Code | Description                                            |
|------|--------------------------------------------------------| 
| 99   | An unknown error occurred                              |
| 6    | Too little light returned or there is no target at all |
| 5    | Too much light returned/blinding or false light        |
| 4    | False light or an undefined spot recorded              |
| 2    | A target is observed but outside the measuring range   |
| 1    | A target is observed but outside the measuring range   |
| 0    | A target is observed but outside the measuring range   |

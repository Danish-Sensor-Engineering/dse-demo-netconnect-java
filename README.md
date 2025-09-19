# DSE Net Connect - Client Demo  

This repository serves as example of how to connect to DSE Net Connect and receive measurements over the network.

- Connect to DSE Net Connect (default port 2730)
- Send control commands to start, stop, etc.
- In a loop, read the byte input-stream and parse
- Send the 'exit' commands to disconnect 


## Network Datagram

| HEADER   | DATA                               |
|----------|------------------------------------| 
| 22 bytes | 4 bytes or more, depending on type |


### Header

The network datagram always consists of 22 bytes header data 

| Bytes | Java Type | Header Name | Description                                                                      |
|-------|-----------|-------------|----------------------------------------------------------------------------------|
| 2     | short     | ID          | Unique fingerprint for this kind of binary payload                               |
| 2     | short     | VERSION     | Datagram version to accommodate for future changes                               |
| 2     | short     | TYPE        | 1=Error, 11=Distance or Profile ( 21=400, 22=800, 24=1600 )                      |
| 4     | int       | LENGTH      | Size of datagram including header of 22 bytes                                    |
| 4     | int       | SEQUENCE    | Counter that wraps at MAX (for int) and starts over                              |
| 8     | long      | TIMESTAMP   | Timestamp in milliseconds (Unix Epoch) when measurement was received from device |



### Data

The header is followed by data depending of type.

| Type (from header) | Description & Size                              |
|--------------------|-------------------------------------------------| 
| Error              | 4 Bytes error code                              |
| Distance           | 4 Bytes distance                                |
| Profile            | 16 Bytes (for X + Y) x Sweep (400, 800 or 1600) |



 ## Control Commands

The session dan be controlled with the following commands, which should be send in plaintext following a newline.

| Command | Description                         |
|---------|-------------------------------------| 
| ping    | Should respond 'pong' as a test     |
| start   | Start flow of measurement data      |
| stop    | Stop flow of measurement data       |
| ascii   | Send ASCII data (only measurements) |
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

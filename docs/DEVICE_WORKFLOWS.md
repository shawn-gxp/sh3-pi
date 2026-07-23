# Device Connection Workflows

This document visualizes the connection lifecycles for the three primary device classes handled by the SH3-PI BLE Hub.

## 1. WINDOWED Class (e.g., Nipro NT-100B Thermometer)
*These devices only broadcast for a few seconds immediately after a measurement is taken. The Hub must capture the advertisement quickly.*

```mermaid
flowchart TD
    %% Device Action
    Start((User Takes Measurement)) -.-> |Broadcasts for 6s| Ad[BLE Advertisement]
    
    %% Hub Detection
    Ad -.-> Detect{HubDaemon Detects MAC?}
    Detect -- No --> Ignore[Ignore / Timeout]
    Detect -- Yes --> Lock[Lock Radio]
    
    %% Session
    Lock --> Connect[BLE Connect]
    Connect --> Enable[Enable 0x1524 Notifications]
    Enable --> Wait[Wait for 8-byte TICD Frame]
    
    %% Processing
    Wait -.-> |Data Received| Parse[Pure Parser Decodes Bytes]
    Parse --> DB[(Insert to SQLite and Publish MQTT)]
    Parse --> Disconnect[Disconnect and Release Radio]
    
    style Start fill:#f9f,stroke:#333,stroke-width:2px
    style DB fill:#9cf,stroke:#333,stroke-width:2px
```

## 2. STREAM Class (e.g., Masimo MightySat Rx)
*These devices stream data continuously while worn. The Hub uses a duty cycle to capture a chunk of data, then disconnects to free the radio for other devices.*

```mermaid
flowchart TD
    %% Device Action
    Start((User Wears Device)) -.-> Ad[BLE Advertisement]
    
    %% Hub Detection
    Ad -.-> Detect{HubDaemon Detects MAC?}
    Detect -- Yes --> Lock[Lock Radio]
    
    %% Session
    Lock --> Connect[BLE Connect]
    Connect --> Enable[Enable Masimo Indications]
    Enable --> StreamLoop((Duty Cycle Loop))
    
    %% Loop Logic
    StreamLoop -.-> |Stream Chunk| Decode[Decode Frame]
    Decode --> CRC{CRC Valid?}
    CRC -- No --> Drop[Drop Frame] -.-> StreamLoop
    CRC -- Yes --> Buffer[Buffer Valid SpO2 Reading]
    
    %% Completion
    Buffer -.-> |5 Valid Frames Captured| Avg[Average Buffered Vitals]
    Avg --> DB[(Insert to SQLite and Publish MQTT)]
    Avg --> Disconnect[Disconnect and Release Radio]
    
    style Start fill:#f9f,stroke:#333,stroke-width:2px
    style DB fill:#9cf,stroke:#333,stroke-width:2px
```

## 3. ALWAYS Class (e.g., Omron BP)
*These devices can be connected to at almost any time (if not asleep). The Hub relies on a periodic timer to poll them for historical records.*

```mermaid
flowchart TD
    %% Hub Trigger
    Timer((Periodic Poll e.g. 2 hours)) -.-> Lock[Lock Radio and Scan MAC]
    
    %% Session
    Lock --> Connect[BLE Connect]
    Connect --> Enable[Enable FE4A Notifications]
    Enable --> Unlock[Write Unlock Token 0x00]
    
    %% Verification
    Unlock -.-> |Notify Accepted| Request[Request EEPROM History Data]
    
    %% Transfer
    Request -.-> |Binary Record Stream| Parse[Parse Binary Slots]
    Parse --> Dedup[Deduplicate Records]
    
    %% Completion
    Dedup --> DB[(Insert to SQLite and Publish MQTT)]
    Dedup --> Disconnect[Disconnect and Release Radio]
    
    style Timer fill:#f9d0c4,stroke:#333,stroke-width:2px
    style DB fill:#9cf,stroke:#333,stroke-width:2px
```

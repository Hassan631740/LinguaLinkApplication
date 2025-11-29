# WebRTC Signaling Infrastructure

This document describes the foundational WebRTC signaling infrastructure implemented for peer-to-peer connections.

## Overview

The WebRTC signaling infrastructure enables real-time peer-to-peer communication using WebSocket for signaling. The system manages signaling rooms, tracks participants, and facilitates the exchange of SDP offers/answers and ICE candidates.

## Architecture

### Components

1. **WebRTC DTOs** (`com.lingualink.dto.request` & `response`)
   - `WebRtcOfferRequest` - SDP offer messages
   - `WebRtcAnswerRequest` - SDP answer messages
   - `WebRtcIceCandidateRequest` - ICE candidate messages
   - `WebRtcJoinRoomRequest` - Join signaling room
   - `WebRtcLeaveRoomRequest` - Leave signaling room
   - `WebRtcSignalResponse` - Signaling response messages
   - `WebRtcRoomStatusResponse` - Room status and participant list

2. **WebRtcSignalingService** (`com.lingualink.service`)
   - Manages signaling rooms and participant tracking
   - Handles room join/leave operations
   - Tracks participant statuses
   - Notifies participants of status changes

3. **WebSocketController** (`com.lingualink.controller`)
   - WebRTC message handlers for WebSocket STOMP protocol
   - Handles SDP offers, answers, and ICE candidates
   - Manages room join/leave operations

4. **WebSocketEventListener** (`com.lingualink.config`)
   - Listens to WebSocket connection/disconnection events
   - Automatically cleans up participants on disconnect

## WebSocket Message Endpoints

All WebRTC signaling messages use the STOMP protocol over WebSocket.

### Base Configuration
- **Connection**: `ws://localhost:8080/ws?token=JWT_TOKEN` (or use Authorization header)
- **Application Prefix**: `/app` (messages sent to this prefix are routed to handlers)
- **User Queue Prefix**: `/user/{userId}/queue` (user-specific messages)
- **Topic Prefix**: `/topic` (broadcast messages)

### Message Handlers

#### 1. Join Signaling Room
- **Endpoint**: `/app/webrtc/join`
- **Payload**:
  ```json
  {
    "roomId": "room-123"
  }
  ```
- **Response**: Room status sent to `/user/{userId}/queue/webrtc-room-status`
- **Side Effects**: 
  - User is added to the room
  - Other participants are notified via `/user/{userId}/queue/webrtc-signal` with type `"participant-joined"`

#### 2. Leave Signaling Room
- **Endpoint**: `/app/webrtc/leave`
- **Payload**:
  ```json
  {
    "roomId": "room-123"
  }
  ```
- **Side Effects**: 
  - User is removed from the room
  - Other participants are notified via `/user/{userId}/queue/webrtc-signal` with type `"participant-left"`

#### 3. Send SDP Offer
- **Endpoint**: `/app/webrtc/offer`
- **Payload**:
  ```json
  {
    "roomId": "room-123",
    "sdp": "v=0\r\no=-...",
    "type": "offer",
    "targetUserId": "456"  // Optional: if provided, sent to specific user; otherwise broadcast to all in room
  }
  ```
- **Response**: Sent to target user(s) via `/user/{userId}/queue/webrtc-signal` with type `"offer"`

#### 4. Send SDP Answer
- **Endpoint**: `/app/webrtc/answer`
- **Payload**:
  ```json
  {
    "roomId": "room-123",
    "sdp": "v=0\r\no=-...",
    "type": "answer",
    "targetUserId": "123"  // Required: ID of user who sent the offer
  }
  ```
- **Response**: Sent to target user via `/user/{userId}/queue/webrtc-signal` with type `"answer"`

#### 5. Send ICE Candidate
- **Endpoint**: `/app/webrtc/ice-candidate`
- **Payload**:
  ```json
  {
    "roomId": "room-123",
    "candidate": "candidate:...",
    "sdpMid": "0",
    "sdpMLineIndex": 0,
    "targetUserId": "456"  // Optional: if provided, sent to specific user; otherwise broadcast to all in room
  }
  ```
- **Response**: Sent to target user(s) via `/user/{userId}/queue/webrtc-signal` with type `"ice-candidate"`

#### 6. Get Room Status
- **Endpoint**: `/app/webrtc/room-status`
- **Payload**:
  ```json
  {
    "roomId": "room-123"
  }
  ```
- **Response**: Sent to `/user/{userId}/queue/webrtc-room-status` with participant list and room information

## Client Subscription Destinations

Clients should subscribe to these destinations to receive signaling messages:

1. **Signaling Messages**: `/user/{userId}/queue/webrtc-signal`
   - Receives: offers, answers, ICE candidates, participant-joined, participant-left events

2. **Room Status**: `/user/{userId}/queue/webrtc-room-status`
   - Receives: room status updates with participant list

## Signaling Flow

### Basic Peer-to-Peer Connection

1. **Both users join the room**:
   ```
   User A → /app/webrtc/join {"roomId": "call-123"}
   User B → /app/webrtc/join {"roomId": "call-123"}
   ```

2. **User A creates offer and sends it**:
   ```
   User A → /app/webrtc/offer {
     "roomId": "call-123",
     "sdp": "<SDP offer>",
     "type": "offer",
     "targetUserId": "<User B ID>"
   }
   ```
   User B receives offer via `/user/{userId}/queue/webrtc-signal`

3. **User B creates answer and sends it**:
   ```
   User B → /app/webrtc/answer {
     "roomId": "call-123",
     "sdp": "<SDP answer>",
     "type": "answer",
     "targetUserId": "<User A ID>"
   }
   ```
   User A receives answer via `/user/{userId}/queue/webrtc-signal`

4. **Both users exchange ICE candidates**:
   ```
   User A → /app/webrtc/ice-candidate {
     "roomId": "call-123",
     "candidate": "<ICE candidate>",
     "sdpMid": "0",
     "sdpMLineIndex": 0,
     "targetUserId": "<User B ID>"
   }
   ```
   (Repeat as needed for each ICE candidate)

5. **Users leave the room**:
   ```
   User A → /app/webrtc/leave {"roomId": "call-123"}
   User B → /app/webrtc/leave {"roomId": "call-123"}
   ```

### Automatic Cleanup

- When a user disconnects from WebSocket (session closed), the `WebSocketEventListener` automatically:
  - Removes the user from all signaling rooms
  - Notifies remaining participants in those rooms

## Security

- All WebSocket connections require JWT authentication (handled by `JwtWebSocketHandshakeInterceptor`)
- Only authenticated users can join rooms and send signaling messages
- User authentication is validated on connection and for each message

## Implementation Details

### Room Management
- Rooms are stored in-memory using `ConcurrentHashMap`
- Each room tracks a set of participant user IDs
- Each user can be in multiple rooms simultaneously
- Rooms are automatically cleaned up when empty

### Participant Tracking
- Participant information includes: userId, userName, connection status, join timestamp
- Participant status is maintained per user across all rooms
- Status updates are sent to relevant participants when changes occur

### Error Handling
- Invalid room IDs are logged and ignored
- Users attempting to send signals without being in a room are rejected
- Missing required fields (like targetUserId for answers) are logged and ignored

## Future Enhancements

Potential improvements for production:
- Persistent room storage (database)
- Room expiration/TTL
- Maximum participant limits per room
- STUN/TURN server configuration endpoint
- Call quality metrics tracking
- Recording capabilities
- Screen sharing support

## Example Client Implementation (JavaScript)

```javascript
// Connect to WebSocket
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

// Connect with JWT token
stompClient.connect({
  'Authorization': 'Bearer ' + jwtToken
}, function(frame) {
  console.log('Connected: ' + frame);
  
  // Subscribe to signaling messages
  stompClient.subscribe('/user/' + userId + '/queue/webrtc-signal', function(message) {
    const signal = JSON.parse(message.body);
    handleWebRtcSignal(signal);
  });
  
  // Subscribe to room status
  stompClient.subscribe('/user/' + userId + '/queue/webrtc-room-status', function(message) {
    const status = JSON.parse(message.body);
    console.log('Room status:', status);
  });
  
  // Join a room
  stompClient.send('/app/webrtc/join', {}, JSON.stringify({
    roomId: 'call-123'
  }));
});

// Send SDP offer
function sendOffer(roomId, sdp, targetUserId) {
  stompClient.send('/app/webrtc/offer', {}, JSON.stringify({
    roomId: roomId,
    sdp: sdp,
    type: 'offer',
    targetUserId: targetUserId
  }));
}

// Send SDP answer
function sendAnswer(roomId, sdp, targetUserId) {
  stompClient.send('/app/webrtc/answer', {}, JSON.stringify({
    roomId: roomId,
    sdp: sdp,
    type: 'answer',
    targetUserId: targetUserId
  }));
}

// Send ICE candidate
function sendIceCandidate(roomId, candidate, sdpMid, sdpMLineIndex, targetUserId) {
  stompClient.send('/app/webrtc/ice-candidate', {}, JSON.stringify({
    roomId: roomId,
    candidate: candidate.candidate,
    sdpMid: candidate.sdpMid,
    sdpMLineIndex: candidate.sdpMLineIndex,
    targetUserId: targetUserId
  }));
}
```


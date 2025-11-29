# WebSocket JWT Authentication Setup

This document explains how to use WebSocket with JWT authentication in the LinguaLink application.

## Overview

WebSocket connections are secured using JWT tokens during the handshake phase. The JWT token is validated before the WebSocket connection is established, ensuring only authenticated users can connect.

## Architecture

- **JwtWebSocketHandshakeInterceptor**: Validates JWT tokens during the WebSocket handshake
- **WebSocketConfig**: Configures STOMP messaging endpoints
- **WebSocketSecurityConfig**: Defines security rules for WebSocket messages
- **WebSocketController**: Example controller showing how to handle WebSocket messages

## Connection Methods

### 1. Query Parameter Method
Connect to the WebSocket endpoint with the JWT token as a query parameter:

```javascript
const token = 'your-jwt-token-here';
const socket = new SockJS(`http://localhost:8080/ws?token=${token}`);
```

### 2. Authorization Header Method
Some WebSocket clients support headers during connection:

```javascript
// Note: Not all WebSocket clients support headers during handshake
// Query parameter method is more reliable
```

## Client Example (JavaScript/React)

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class WebSocketService {
  constructor(token) {
    this.token = token;
    this.stompClient = null;
  }

  connect() {
    // Connect using query parameter
    const socket = new SockJS(`http://localhost:8080/ws?token=${this.token}`);
    
    this.stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('WebSocket connected');
        this.subscribeToTopics();
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      }
    });

    this.stompClient.activate();
  }

  subscribeToTopics() {
    // Subscribe to broadcast messages
    this.stompClient.subscribe('/topic/messages', (message) => {
      const data = JSON.parse(message.body);
      console.log('Received broadcast:', data);
    });

    // Subscribe to private messages for current user
    this.stompClient.subscribe('/user/queue/messages', (message) => {
      const data = JSON.parse(message.body);
      console.log('Received private message:', data);
    });

    // Subscribe to echo responses
    this.stompClient.subscribe('/user/queue/echo', (message) => {
      const data = JSON.parse(message.body);
      console.log('Echo response:', data);
    });
  }

  sendBroadcastMessage(text) {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.publish({
        destination: '/app/message',
        body: JSON.stringify({ text })
      });
    }
  }

  sendPrivateMessage(to, text) {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.publish({
        destination: '/app/private',
        body: JSON.stringify({ to, text })
      });
    }
  }

  sendEcho(text) {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.publish({
        destination: '/app/echo',
        body: JSON.stringify({ text })
      });
    }
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }
}

// Usage
const token = localStorage.getItem('jwt_token');
const wsService = new WebSocketService(token);
wsService.connect();
```

## Server-Side Message Handling

### Available Endpoints

1. **Broadcast Message**: `/app/message`
   - Sends message to all subscribers of `/topic/messages`
   - Payload: `{ "text": "Hello everyone" }`

2. **Private Message**: `/app/private`
   - Sends message to specific user at `/user/{username}/queue/messages`
   - Payload: `{ "to": "user@example.com", "text": "Hello" }`

3. **Echo**: `/app/echo`
   - Echoes message back to sender at `/user/{username}/queue/echo`
   - Payload: `{ "text": "Echo test" }`

### Accessing Authentication in Controllers

```java
@MessageMapping("/message")
@SendTo("/topic/messages")
public MessageResponse handleMessage(@Payload MessageRequest request, Principal principal) {
    String username = principal.getName(); // Gets username from JWT
    // Access authorities if needed
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return response;
}
```

## Security Features

1. **JWT Validation**: Token is validated during handshake
2. **Authentication Required**: All message destinations require authentication
3. **User-Specific Messages**: Supports user-specific message queues
4. **CORS Support**: Configured for cross-origin connections

## Error Handling

If the JWT token is invalid or missing:
- HTTP 401 (Unauthorized) status is returned
- Connection is rejected
- Client should handle reconnection with valid token

## Production Considerations

1. **Allowed Origins**: Update `setAllowedOriginPatterns("*")` in `WebSocketConfig` to restrict origins
2. **Token Refresh**: Implement token refresh mechanism for long-lived connections
3. **Rate Limiting**: Consider adding rate limiting for WebSocket connections
4. **Message Broker**: For production, use a proper message broker (RabbitMQ, ActiveMQ) instead of in-memory broker

## Testing

### Using Postman/WebSocket Client

1. Get JWT token from `/api/auth/login`
2. Connect to: `ws://localhost:8080/ws?token=YOUR_JWT_TOKEN`
3. Send STOMP frames:
   ```
   CONNECT
   
   ^@
   
   SEND
   destination:/app/message
   content-type:application/json
   
   {"text":"Hello"}
   ^@
   ```

## Troubleshooting

1. **Connection Rejected**: Check JWT token is valid and not expired
2. **401 Unauthorized**: Verify token is being sent correctly in query parameter
3. **CORS Issues**: Check allowed origins in WebSocketConfig
4. **Messages Not Received**: Verify subscription destinations match exactly


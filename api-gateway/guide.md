TTL = 30
   ↓
Key exists + expires in 30 seconds





TTL = -1
   ↓
Key exists + NO expiry





TTL = -2
   ↓
Key does NOT 






#CORS: Cross Origin Resource Sharing
It is a browser security mechanism that controls:
"Which frontend origins are allowed to call my backend"

CORS protects the browser boundary, JWT establishes identity, rate limiting controls traffic, the gateway centralizes these cross-cutting concerns, services own business domains, Kafka propagates asynchronous events, and smoke testing validates that the critical path across all of those boundaries works end-to-end.
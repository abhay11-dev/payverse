JWT: Header: Metadata: HMAC SHA-256
     Payload: Claims: Username, expiry and all data
     Signature: Created from Header + payload + secret key
     

JWTs are signed but not encrypted by default. Anyone possessing the token can decode its payload, so only non-sensitive claims like the username, user ID, or roles should be stored. Passwords and other secrets must never be included.






We don't use a permanent JWT because JWT is stateless and difficult to revoke. If compromised, a long-lived JWT gives an attacker access until expiry. 
We also don't use refresh tokens for every API call because refresh tokens are high-value credentials that require server-side validation and increase latency.

Instead, we use short-lived JWT access tokens for fast stateless authorization and long-lived refresh tokens for session management. 
Refresh tokens are stored server-side, rotated, and revoked on logout. 
The exact expiry depends on the business risk: fintech applications prioritize security with shorter access token lifetimes, while lower-risk applications may choose longer sessions for better UX.




WHY Redis?

- Very fast lookup: 
- In built expiry: Time To Live (TTL)
- Logout becomes simple: Simply delete that key


Redis is an in-memory key-value store.
Think of it like a Java HashMap: O(1)
 key
 |
 |-- hash function
 |
 ▼
memory location
 |
 ▼
value


Why Redis is better for this than MySQL?
Because refresh tokens are temporary session data.

Create
 |
 |
Read frequently
 |
 |
Delete/replace
 |
 |
Expire automatically

This matches Redis exactly.




"Redis is in-memory. If Redis shuts down, won't my refresh tokens disappear?"

Redis stopped
       |
       ▼
RAM cleared
       |
       ▼
Refresh tokens lost


Production enables persistence:
1.RDB Snapshot: Redis Database Backup: Redis periodically saves memory state to disk.Snapshots.
dump.rdb file -> Redis restart -> Load dump.rdb -> Resotre tokens

RDB disadvantage:
Possible data loss.

Example:

Snapshot:
10:00 AM

Redis crash:
10:05 AM

Data created between:
10:00 - 10:05

may be lost.






2.AOF (Append Only File):
Redis writes every change.
After restart,Reads appendonly.aof -> Redis replays commands.









                 Application Services

                         |
                         |
                 Redis Cluster

        +----------------+----------------+
        |                |                |
     Node 1           Node 2           Node 3
    Master           Master           Master

        |                |                |

    Replica          Replica          Replica








Do NOT store like: 
refresh_token:userId = token

because one user can have multiple devices.

Better:
refresh_token:{userId}:{deviceId}

Example:
refresh_token:101:mobile_device_abc

Value:
{
 tokenHash: "abc123...",
 createdAt: "...",
 expiresAt: "..."
}

TTL:
30 days

Example:
User logs in:
Phone
Laptop
Tablet

Redis:
refresh_token:101:phone
refresh_token:101:laptop
refresh_token:101:tablet

Now user can logout from one device only.






Stateless authentication means the server does not maintain session state for every user request. With JWT, the token itself carries user information and the server only verifies the signature. However, large fintech systems often use a hybrid model where access tokens remain stateless while refresh tokens are stored in Redis for revocation, logout, and security control.







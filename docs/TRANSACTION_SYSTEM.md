# Transaction System Documentation

This document describes the transaction system implementation for LinguaLink, including client balance management, booking payments, and interpreter withdrawals.

## Overview

The transaction system provides:
- **Client Balance Top-up**: Clients can add funds to their wallet
- **Balance Deduction**: Automatic deduction when bookings are completed
- **Interpreter Earnings**: Earnings are recorded when bookings complete
- **Withdrawal Requests**: Interpreters can request withdrawals of their earnings

## Database Schema

### User Balance
- Added `balance` field to `users` table (DECIMAL(10,2), default 0.00)
- For clients: represents wallet balance
- For interpreters: represents accumulated earnings

### Transactions Table
- Tracks all financial movements
- Transaction types: TOP_UP, DEDUCTION, EARNING, WITHDRAWAL_REQUEST, WITHDRAWAL_COMPLETED, WITHDRAWAL_REJECTED, REFUND
- Transaction statuses: PENDING, COMPLETED, FAILED, CANCELLED
- Stores balance before/after each transaction
- Links to users and bookings

## API Endpoints

### Client Balance Top-up

**POST** `/api/transactions/top-up`
- **Role**: CLIENT
- **Request Body**:
  ```json
  {
    "amount": 100.00,
    "currency": "USD",
    "paymentMethod": "CREDIT_CARD",
    "paymentReference": "external-tx-id-123",
    "notes": "Monthly top-up"
  }
  ```
- **Response**: Transaction details with updated balance
- **Status Codes**: 201 (Created), 400 (Bad Request), 402 (Payment Required), 403 (Forbidden)

### Get Balance

**GET** `/api/transactions/balance`
- **Role**: Any authenticated user
- **Response**:
  ```json
  {
    "userId": 1,
    "balance": 250.50
  }
  ```

**GET** `/api/transactions/balance/{userId}`
- **Role**: ADMINISTRATOR
- **Response**: Balance for specified user

### Request Withdrawal (Interpreter)

**POST** `/api/transactions/withdrawals`
- **Role**: INTERPRETER
- **Request Body**:
  ```json
  {
    "amount": 500.00,
    "currency": "USD",
    "accountDetails": "Bank: ABC Bank, Account: 123456789, Routing: 987654321",
    "notes": "Monthly withdrawal"
  }
  ```
- **Response**: Withdrawal request transaction (status: PENDING)
- **Status Codes**: 201 (Created), 400 (Bad Request), 402 (Insufficient Balance), 403 (Forbidden)

### Get Transactions

**GET** `/api/transactions`
- **Role**: Any authenticated user
- **Query Parameters**: `page`, `size`, `sortBy`, `sortDir`
- **Response**: Paginated list of user's transactions

**GET** `/api/transactions/user/{userId}`
- **Role**: ADMINISTRATOR
- **Response**: Paginated transactions for specified user

**GET** `/api/transactions/{id}`
- **Role**: Any authenticated user (own transactions) or ADMINISTRATOR
- **Response**: Single transaction details

### Manage Withdrawals (Admin)

**GET** `/api/transactions/withdrawals/pending`
- **Role**: ADMINISTRATOR
- **Response**: Paginated list of pending withdrawal requests

**POST** `/api/transactions/withdrawals/{transactionId}/approve`
- **Role**: ADMINISTRATOR
- **Response**: Withdrawal completion transaction

**POST** `/api/transactions/withdrawals/{transactionId}/reject`
- **Role**: ADMINISTRATOR
- **Request Body**:
  ```json
  {
    "reason": "Invalid account details"
  }
  ```
- **Response**: Rejection transaction (amount refunded to interpreter)

### Interpreter Earnings Summary

**GET** `/api/transactions/earnings/total`
- **Role**: INTERPRETER
- **Response**:
  ```json
  {
    "userId": 5,
    "totalEarnings": 2500.00,
    "totalWithdrawals": 1500.00,
    "availableBalance": 1000.00
  }
  ```

### Complete Booking (Triggers Payment)

**POST** `/api/bookings/{id}/complete`
- **Role**: CLIENT, INTERPRETER, or ADMINISTRATOR
- **Description**: Completes a booking after call ends. Automatically:
  1. Deducts booking price from client's balance
  2. Records 80% of booking price as interpreter earning
  3. Updates booking status to "COMPLETED"
- **Status Codes**: 200 (OK), 400 (Bad Request), 402 (Insufficient Balance), 403 (Forbidden), 404 (Not Found)

## Transaction Flow

### Client Top-up Flow
1. Client sends top-up request with payment method
2. System validates request
3. Adds amount to client's balance
4. Creates TOP_UP transaction (status: COMPLETED)
5. Returns transaction details

### Booking Completion Flow
1. Call ends
2. Client/Interpreter/Admin calls `/api/bookings/{id}/complete`
3. System validates booking and client balance
4. Deducts booking price from client balance
5. Creates DEDUCTION transaction
6. Records interpreter earning (80% of price)
7. Creates EARNING transaction
8. Updates booking status to "COMPLETED"
9. Returns updated booking

### Withdrawal Flow
1. Interpreter requests withdrawal with account details
2. System validates sufficient balance
3. Deducts requested amount from interpreter's balance (reserved)
4. Creates WITHDRAWAL_REQUEST transaction (status: PENDING)
5. Admin reviews request
6. Admin approves → Creates WITHDRAWAL_COMPLETED transaction
7. Admin rejects → Creates WITHDRAWAL_REJECTED transaction, refunds amount

## Transaction Types

### TOP_UP
- Increases client balance
- Status: COMPLETED
- Requires payment method and external reference

### DEDUCTION
- Decreases client balance for booking payment
- Status: COMPLETED
- Linked to booking

### EARNING
- Increases interpreter balance from completed booking
- Status: COMPLETED
- Linked to booking
- Amount typically 80% of booking price

### WITHDRAWAL_REQUEST
- Decreases interpreter balance (reserved)
- Status: PENDING (initially)
- Requires account details

### WITHDRAWAL_COMPLETED
- Marks withdrawal as processed
- Status: COMPLETED
- Balance already deducted in WITHDRAWAL_REQUEST

### WITHDRAWAL_REJECTED
- Refunds amount to interpreter
- Status: COMPLETED
- Includes rejection reason

## Error Handling

### Insufficient Balance Exception (402)
- Thrown when:
  - Client tries to complete booking without sufficient balance
  - Interpreter requests withdrawal exceeding available balance
- Response includes required amount and available balance

### Custom Exception Handler
- `InsufficientBalanceException` is handled globally
- Returns HTTP 402 (Payment Required) status
- Includes detailed error message

## Security

- All endpoints require JWT authentication
- Role-based access control:
  - Clients: Top-up, view own transactions
  - Interpreters: Request withdrawals, view earnings
  - Administrators: Manage withdrawals, view all transactions
- Users can only view their own transactions (unless admin)
- Balance operations are transactional and atomic

## Business Rules

1. **Booking Payment Split**:
   - Client pays: 100% of booking price
   - Interpreter earns: 80% of booking price
   - Platform fee: 20% (implicit)

2. **Withdrawal Process**:
   - Withdrawal requests reserve the amount immediately
   - Only one pending withdrawal per interpreter at a time (enforced by business logic)
   - Rejected withdrawals automatically refund the amount

3. **Balance Validation**:
   - All balance operations check sufficient funds
   - Negative balances are prevented
   - Transactions are atomic (all-or-nothing)

## Future Enhancements

Potential improvements:
- Multiple currency support with exchange rates
- Partial withdrawals
- Scheduled/automatic withdrawals
- Refund processing for cancelled bookings
- Transaction fee configuration
- Payment gateway integration (Stripe, PayPal)
- Escrow system for bookings
- Minimum withdrawal amounts
- Withdrawal approval workflows

## Example Usage

### Client Top-up
```bash
POST /api/transactions/top-up
Authorization: Bearer {client_jwt_token}
{
  "amount": 100.00,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "paymentReference": "stripe_pi_1234567890"
}
```

### Complete Booking
```bash
POST /api/bookings/123/complete
Authorization: Bearer {jwt_token}
```

### Request Withdrawal
```bash
POST /api/transactions/withdrawals
Authorization: Bearer {interpreter_jwt_token}
{
  "amount": 500.00,
  "currency": "USD",
  "accountDetails": "Bank: XYZ Bank\nAccount: 987654321\nRouting: 123456789"
}
```

### Approve Withdrawal (Admin)
```bash
POST /api/transactions/withdrawals/456/approve
Authorization: Bearer {admin_jwt_token}
```


# LinguaLink Entities Summary

## Total Entities: 7
## Total MySQL Tables Required: 7

---

## 1. User Entity
**Table Name:** `users`
**File:** `src/main/java/com/lingualink/entity/User.java`

### Fields:
- `id` (Long) - Primary Key, Auto Increment
- `name` (String)
- `email` (String) - Unique, Not Null
- `password` (String) - Not Null
- `role` (String)
- `createdAt` (LocalDateTime) - Auto-generated timestamp

### Relationships:
- One-to-One with `Interpreter`
- One-to-Many with `Event` (as organizer)
- One-to-Many with `Message` (as sender/receiver)
- One-to-Many with `Review` (as reviewer)

---

## 2. Interpreter Entity
**Table Name:** `interpreters`
**File:** `src/main/java/com/lingualink/entity/Interpreter.java`

### Fields:
- `id` (Long) - Primary Key, Auto Increment
- `user` (User) - One-to-One relationship, Foreign Key: `user_id`
- `languages` (String) - JSON format
- `ratePerHour` (BigDecimal)
- `experienceYears` (Integer)
- `bio` (String) - TEXT

### Relationships:
- One-to-One with `User`
- One-to-Many with `Booking`

---

## 3. Event Entity
**Table Name:** `events`
**File:** `src/main/java/com/lingualink/entity/Event.java`

### Fields:
- `id` (Long) - Primary Key, Auto Increment
- `organizer` (User) - Many-to-One relationship, Foreign Key: `organizer_id` (Not Null)
- `title` (String)
- `description` (String) - TEXT
- `startDatetime` (LocalDateTime)
- `endDatetime` (LocalDateTime)
- `location` (String)
- `language` (String)

### Relationships:
- Many-to-One with `User` (organizer)
- One-to-Many with `Booking`

---

## 4. Booking Entity
**Table Name:** `bookings`
**File:** `src/main/java/com/lingualink/entity/Booking.java`

### Fields:
- `id` (Long) - Primary Key, Auto Increment
- `event` (Event) - Many-to-One relationship, Foreign Key: `event_id` (Not Null)
- `interpreter` (Interpreter) - Many-to-One relationship, Foreign Key: `interpreter_id`
- `status` (String)
- `requestedAt` (LocalDateTime) - Auto-generated timestamp
- `confirmedAt` (LocalDateTime)
- `price` (BigDecimal)
- `paymentId` (Long)

### Relationships:
- Many-to-One with `Event`
- Many-to-One with `Interpreter`
- One-to-Many with `Payment`
- One-to-Many with `Message`
- One-to-Many with `Review`

---

## 5. Payment Entity
**Table Name:** `payments`
**File:** `src/main/java/com/lingualink/entity/Payment.java`

### Fields:
- `id` (Long) - Primary Key, Auto Increment
- `booking` (Booking) - Many-to-One relationship, Foreign Key: `booking_id`
- `amount` (BigDecimal)
- `currency` (String)
- `method` (String)
- `status` (String)
- `paidAt` (LocalDateTime)

### Relationships:
- Many-to-One with `Booking`

---

## 6. Message Entity
**Table Name:** `messages`
**File:** `src/main/java/com/lingualink/entity/Message.java`

### Fields:
- `id` (Long) - Primary Key, Auto Increment
- `booking` (Booking) - Many-to-One relationship, Foreign Key: `booking_id`
- `sender` (User) - Many-to-One relationship, Foreign Key: `sender_id`
- `receiver` (User) - Many-to-One relationship, Foreign Key: `receiver_id`
- `content` (String) - TEXT
- `sentAt` (LocalDateTime) - Auto-generated timestamp

### Relationships:
- Many-to-One with `Booking`
- Many-to-One with `User` (sender)
- Many-to-One with `User` (receiver)

---

## 7. Review Entity
**Table Name:** `reviews`
**File:** `src/main/java/com/lingualink/entity/Review.java`

### Fields:
- `id` (Long) - Primary Key, Auto Increment
- `booking` (Booking) - Many-to-One relationship, Foreign Key: `booking_id`
- `reviewer` (User) - Many-to-One relationship, Foreign Key: `reviewer_id`
- `rating` (Integer)
- `comment` (String) - TEXT
- `createdAt` (LocalDateTime) - Auto-generated timestamp

### Relationships:
- Many-to-One with `Booking`
- Many-to-One with `User` (reviewer)

---

## MySQL Tables Required in Workbench

You need to create **7 tables** in MySQL Workbench:

1. ✅ **users**
2. ✅ **interpreters**
3. ✅ **events**
4. ✅ **bookings**
5. ✅ **payments**
6. ✅ **messages**
7. ✅ **reviews**

### Database Schema Location
The complete SQL schema is available in:
`src/main/resources/db/migration/V1__init_schema.sql`

### Note
If you're using Flyway (which is configured in this project), the tables will be automatically created when the application starts. However, if you want to create them manually in Workbench, you can use the SQL from the migration file.

---

## Entity Relationship Diagram

```
User (1) ──────── (1) Interpreter
  │
  │ (1)
  │
  └─── (Many) Event
       │
       │ (1)
       │
       └─── (Many) Booking
            │
            ├─── (Many) Payment
            ├─── (Many) Message
            └─── (Many) Review
```

---

## All Entity Files Location

All entities are located in: `src/main/java/com/lingualink/entity/`

1. `User.java`
2. `Interpreter.java`
3. `Event.java`
4. `Booking.java`
5. `Payment.java`
6. `Message.java`
7. `Review.java`


# TicketDesk - Full-Stack Ticketing System

A comprehensive IT support and customer service ticketing system built with Spring Boot and Next.js.

## 🚀 Features

### ✅ Must-Have Features (Implemented)
- **Authentication & Authorization**: JWT-based login/logout with role-based access control
- **User Dashboard**: Create tickets, view status, add comments, track history
- **Ticket Management**: Complete lifecycle (Open → In Progress → Resolved → Closed)
- **Admin Panel**: User management, ticket oversight, system monitoring
- **Access Control**: Role-based permissions (User, Support Agent, Admin)

### 🌟 Good-to-Have Features (Implemented)
- **Email Notifications**: Automated emails for ticket events
- **Search & Filter**: Advanced ticket filtering and search
- **Ticket Prioritization**: Priority levels (Low, Medium, High, Urgent)
- **Rating System**: Rate ticket resolutions with feedback

## 🧰 Tech Stack

### Backend
- **Java 17** with **Spring Boot 3.1.5**
- **PostgreSQL** database
- **Spring Security** with JWT authentication
- **Spring Data JPA** for data persistence
- **Spring Mail** for email notifications

### Frontend
- **Next.js 14** (React-based framework)
- **TypeScript** for type safety
- **Tailwind CSS** for styling
- **React Query** for state management
- **Headless UI** for accessible components

## 📋 Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher

## 🛠️ Setup Instructions

### 1. Database Setup

```sql
-- Create database
CREATE DATABASE ticketing_db;

-- Create user (optional)
CREATE USER ticketing_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE ticketing_db TO ticketing_user;
```

### 2. Backend Setup

```bash
# Navigate to backend directory
cd backend

# Update application.yml with your database credentials
# Edit src/main/resources/application.yml:
# - Update PostgreSQL connection details
# - Configure email settings (optional)

# Install dependencies and run
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### 3. Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

The frontend will start on `http://localhost:3000`

## 👥 Default Users

The system creates default users on first startup:

| Role | Username | Password | Email |
|------|----------|----------|-------|
| Admin | `admin` | `password123` | admin@ticketdesk.com |
| Support Agent | `support` | `password123` | support@ticketdesk.com |
| User | `user` | `password123` | user@ticketdesk.com |

## 🔧 Configuration

### Email Configuration (Optional)

Update `backend/src/main/resources/application.yml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:your-email@gmail.com}
    password: ${MAIL_PASSWORD:your-app-password}
```

Set environment variables:
```bash
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
```

### Database Configuration

Update connection details in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ticketing_db
    username: postgres
    password: your_password
```

## 🎯 Usage Guide

### For Regular Users
1. **Login** with your credentials
2. **Create Tickets** describing your issues
3. **Track Progress** through the dashboard
4. **Add Comments** to provide additional information
5. **Rate Resolution** when tickets are resolved

### For Support Agents
1. **View Assigned Tickets** in your dashboard
2. **Update Ticket Status** as you work on issues
3. **Add Comments** to communicate with users
4. **Reassign Tickets** if needed

### For Admins
1. **Monitor System** through the admin dashboard
2. **Manage Users** - create, edit, delete accounts
3. **Oversee All Tickets** - force assign, update status
4. **View Statistics** and system health

## 🏗️ Project Structure

```
ticketing-system/
├── backend/
│   ├── src/main/java/com/ticketing/
│   │   ├── config/          # Security & configuration
│   │   ├── controller/      # REST API endpoints
│   │   ├── dto/            # Data transfer objects
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Data access layer
│   │   ├── security/       # JWT & authentication
│   │   └── service/        # Business logic
│   └── src/main/resources/
│       └── application.yml  # Configuration
├── frontend/
│   ├── app/                # Next.js app directory
│   ├── components/         # Reusable components
│   ├── contexts/          # React contexts
│   └── services/          # API services
└── README.md
```

## 🔐 Security Features

- **JWT Authentication** with secure token handling
- **Role-Based Access Control** (RBAC)
- **Password Encryption** using BCrypt
- **CORS Configuration** for cross-origin requests
- **Input Validation** and sanitization

## 📊 API Endpoints

### Authentication
- `POST /api/auth/signin` - User login
- `POST /api/auth/signup` - User registration

### Tickets
- `GET /api/tickets` - Get user tickets
- `POST /api/tickets` - Create new ticket
- `GET /api/tickets/{id}` - Get ticket details
- `PUT /api/tickets/{id}/status` - Update ticket status
- `POST /api/tickets/{id}/comments` - Add comment

### Admin
- `GET /api/admin/users` - Get all users
- `POST /api/admin/users` - Create user
- `GET /api/admin/tickets` - Get all tickets
- `GET /api/admin/stats` - Get system statistics

## 🚀 Deployment

### Backend Deployment
```bash
# Build JAR file
mvn clean package

# Run with production profile
java -jar target/ticketing-system-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Frontend Deployment
```bash
# Build for production
npm run build

# Start production server
npm start
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation

---

**TicketDesk** - Streamlining IT support and customer service operations.

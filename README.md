# TicketDesk - IT Support & Customer Service Portal

A modern, full-stack ticketing system designed for efficient IT support and customer service management. Built with Spring Boot backend and Next.js frontend for optimal performance and user experience.

## ✨ Key Features

### 🔐 Authentication & Security
- JWT-based authentication with secure token handling
- Role-based access control (User, Support Agent, Admin)
- BCrypt password encryption
- Session management and automatic logout

### 🎫 Ticket Management
- Complete ticket lifecycle: Open → In Progress → Resolved → Closed
- Priority levels: Low, Medium, High, Urgent
- Real-time status updates with color-coded indicators
- Comment system for communication
- Ticket assignment and reassignment
- Advanced search and filtering capabilities

### 👥 User Management
- Multi-role user system (Admin, Support Agent, User)
- User dashboard with personalized ticket views
- Admin panel for user management and system oversight
- Profile management and settings

### 📧 Communication & Notifications
- Email notifications for ticket updates
- In-app comment system
- Rating and feedback system for resolved tickets
- Real-time status updates

### 🎨 Modern UI/UX
- Responsive design for all devices
- Clean, intuitive interface
- Dark/light theme support
- Accessible components with proper ARIA labels

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
| Admin | `admin` | `admin@4352` | admin@ticketdesk.com |
| Support Agent | `support` | `support@4352` | support@ticketdesk.com |
| User | `user` | `password123` | user@ticketdesk.com |

## 🔧 Configuration

### Environment Variables

Create a `.env` file in the backend directory:

```bash
# Database Configuration
DB_PASSWORD=your_db_password
DB_USERNAME=your_db_username
DB_URL=your_db_url

# JWT Configuration
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=set_your_jwt_expiration

# Email Configuration (Optional)
MAIL_HOST=set_your_mail_host
MAIL_PORT=set_your_mail_port
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Server Configuration
SERVER_PORT=set_your_server_port

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

### Production Deployment

#### Backend Deployment
```bash
# Build JAR file
mvn clean package

# Set production environment variables
export DB_PASSWORD=your_secure_password
export JWT_SECRET=your_jwt_secret_key
export MAIL_USERNAME=your_email@gmail.com
export MAIL_PASSWORD=your_app_password

# Run application
java -jar target/ticketing-system-0.0.1-SNAPSHOT.jar
```

#### Frontend Deployment
```bash
# Build for production
npm run build

# Start production server
npm start
```

## 🐛 Troubleshooting

### Common Issues

**Backend won't start:**
- Check PostgreSQL is running
- Verify database credentials in `.env`
- Ensure Java 17+ is installed

**Frontend connection issues:**
- Verify backend is running on port 8080
- Check CORS configuration
- Ensure API endpoints are accessible

**Email notifications not working:**
- Verify SMTP credentials
- Check firewall settings
- Enable "Less secure app access" for Gmail

**TicketDesk** - Modern IT support and customer service management made simple.

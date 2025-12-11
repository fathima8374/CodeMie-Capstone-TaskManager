📌 Project: To-Do List Application (REST API)

This is a simple To-Do List REST API built using Java and Spring Boot.
The project demonstrates how to build CRUD operations using RESTful web services without any database (in-memory storage).
It is ideal for beginners learning Spring Boot, API development, and Postman testing.

🚀 Features
Create a new To-Do task
Get all tasks
Get a task by ID
Update an existing task
Delete a task
In-memory storage using Java Collections
Simple and clean REST API design
Includes Postman testing examples

🛠️ Tech Stack
Java 8+ / Java 17
Spring Boot
Spring Web (REST)
Maven
Postman (for API Testing)
📁 Project Structure
src/
 └── main/
     ├── java/com/todo/
     │     ├── controller/   → API Endpoints
     │     ├── model/        → Todo model class
     │     ├── service/      → Business logic
     │     └── TodoApiApplication.java
     └── resources/
           └── application.properties

🧪 API Endpoints
▶ 1. Add Task
POST /todo
Body:
{
  "title": "Learn Java",
  "completed": false
}

▶ 2. Get All Tasks
GET /todo/readall

▶ 3. Get Task by ID
GET /todo/read/{id}

▶ 4. Update Task
PUT /todo/update/{id}
Body:
{
  "title": "Updated Task",
  "completed": true
}

▶ 5. Delete Task
DELETE /todo/delete/{id}

🧰 How to Run the Project

Clone the repository:
git clone https://github.com/your-username/todo-api.git
Open the project in STS / IntelliJ / Eclipse
Build using Maven:
mvn clean install

Run Spring Boot Application:
mvn spring-boot:run

Test the APIs using Postman at:
http://localhost:8080/todo

📌 Future Enhancements
Integrate MySQL/PostgreSQL database
Add JPA Hibernate
Add validation (title required, length restrictions)

Add authentication (JWT/Spring Security)

Create a front-end UI using React / Angular

🤝 Contributing

Pull requests are welcome.
Feel free to open issues for improvements or bugs.

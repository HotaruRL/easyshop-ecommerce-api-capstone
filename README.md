# 🛍️ EasyShop E-commerce Backend

Welcome to the EasyShop E-commerce Backend! This project provides a complete and robust RESTful API for an online store, built with Java and the powerful Spring Boot framework. It's a fantastic starting point for developers looking to understand or build modern e-commerce platforms, featuring everything from user authentication to order processing.

This backend is designed to handle core e-commerce functionalities, including a dynamic product catalog, a persistent shopping cart for each user, secure profile management, and an order creation system that integrates with external services for shipping calculations.

## 📋 Project Requirements

To get this project up and running on your local machine, you'll need the following software installed:

*   **Java JDK 11** or newer
*   **Apache Maven**
*   A running **MySQL** server instance

## 🧱 Dependencies

This project is built upon a solid foundation of industry-standard libraries and frameworks to ensure performance, security, and maintainability.

*   **Spring Boot:** Serves as the core application framework.
*   **Spring Security:** Manages all authentication and authorization concerns.
*   **JSON Web Tokens (JWT):** Implements stateless, secure API authentication.
*   **Spring WebFlux (`WebClient`):** Enables non-blocking, reactive calls to external APIs, like our shipping service.
*   **Apache Commons DBCP2:** Provides efficient and robust database connection pooling.
*   **MySQL JDBC Driver:** Handles all connectivity to the MySQL database.

## 🚀 Getting Started

After cloning the repository, a few configuration steps are needed to get the application ready to launch.

### Database Setup

First, you'll need to set up the database. This application is configured to connect to a MySQL database.

1.  Connect to your local or remote MySQL server.
2.  Create a new database. The default name used in the configuration is `easyshop`. You can create it with the following SQL command:
    ```sql
    CREATE DATABASE easyshop;
    ```
3.  You will need to create the table schema. A schema file (e.g., `database/schema.sql`) should be run against your new database to create all the necessary tables.

### Application Configuration

Next, you need to tell the application how to connect to your database. All configuration is located in the `application.properties` file.

1.  Navigate to `src/main/resources/`.
2.  Open the `application.properties` file.
3.  Update the `datasource` properties to match your MySQL server credentials.

Here's an example of what the datasource configuration looks like:

```properties
# src/main/resources/application.properties

# Update these values with your MySQL server details
datasource.url=jdbc:mysql://localhost:3306/easyshop
datasource.username=your_mysql_username
datasource.password=your_mysql_password
```

The application uses a secret key to sign JWTs for security. A default key is provided, but you can generate and replace the value of `jwt.secret` for production environments.

## 🏃 How to run the application

With the configuration in place, you can start the application server. Open a terminal in the root directory of the project and run the following Maven command:

```bash
mvn spring-boot:run
```

Once the application has started successfully, the API will be available at `http://localhost:8080`. You can now make requests to the various endpoints!

## 💻 Relevant Code Examples

The API is structured around REST principles. Here are some examples of the most common endpoints you'll interact with.

### 🔑 Authentication

The application uses JWT for security. You must first register and log in to receive a token, which is then used to access protected endpoints.

To register a new user, send a `POST` request with the user's details.

```http
POST /register
Content-Type: application/json

{
    "username": "newUser",
    "password": "password123",
    "confirmPassword": "password123",
    "role": "USER"
}
```

After registering, you can log in to obtain an authentication token.

```http
POST /login
Content-Type: application/json

{
    "username": "newUser",
    "password": "password123"
}
```

The server will respond with a JWT and user information. This token must be included in the `Authorization` header for all subsequent requests to protected routes.

```json
{
    "token": "eyJhbGciOiJI...",
    "user": {
        "id": 1,
        "username": "newUser",
        "authorities": [
            {
                "name": "ROLE_USER"
            }
        ]
    }
}
```

### 📦 Products & Categories

You can browse products and categories without authentication. The products endpoint supports filtering by category, price, and color.

For example, to find all products in category `1` that cost less than `$50`, you would make the following request:

```http
GET /products?cat=1&maxPrice=50.00
```

### 🛒 Shopping Cart (Requires Authentication)

The shopping cart endpoints are protected and require a valid JWT. Remember to include the `Authorization` header.

```http
Authorization: Bearer <your-jwt-token>
```

To add a product to your cart, you make a `POST` request to the cart endpoint with the product's ID.

```http
POST /cart/products/15
```

You can also update the quantity of an item in the cart or clear the cart entirely.

```http
PUT /cart/products/15
Content-Type: application/json

{
    "quantity": 3
}
```

### 🧑‍💼 User Profile (Requires Authentication)

Users can view and update their personal information, such as their shipping address. The update endpoint cleverly handles partial updates—you only need to send the fields you want to change.

```http
PUT /profile
Authorization: Bearer <your-jwt-token>
Content-Type: application/json

{
    "firstName": "John",
    "lastName": "Doe",
    "address": "123 Main St"
}
```

## 🎉 Conclusion

This project serves as a powerful and educational example of a modern, secure e-commerce backend. It demonstrates best practices in API design, database interaction, and security with Spring Boot. We encourage you to explore the code, open issues if you find any bugs, or submit pull requests with improvements.

Happy coding
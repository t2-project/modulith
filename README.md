# T2-Modulith

The T2-Modulith is an implementation of the [T2-Project](https://t2-documentation.readthedocs.io/) as a monolith.
It has still a modular structure enforced by [Spring Modulith](https://spring.io/projects/spring-modulith).

**Migration from the microservices implementation to the monolith implementation:**

|                        Microservice                        |     Monolith      | Comment                                                                                                                                                             |
|:----------------------------------------------------------:|:-----------------:|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|         [Cart](https://github.com/t2-project/cart)         |    Cart Module    |                                                                                                                                                                     |
|    [Inventory](https://github.com/t2-project/inventory)    | Inventory Module  |                                                                                                                                                                     |
|        [Order](https://github.com/t2-project/order)        |   Order Module    |                                                                                                                                                                     |
|      [Payment](https://github.com/t2-project/payment)      |  Payment Module   |                                                                                                                                                                     |
|           [UI](https://github.com/t2-project/ui)           |     UI Module     | JSP 😕                                                                                                                                                              |
|   [UI Backend](https://github.com/t2-project/uibackend)    | UI Backend Module | Still an API gateway for the UI but in a different way:<br/> UI interacts with UI Backend via inter-process communication instead of HTTP requests over the network |
| [Orchestrator](https://github.com/t2-project/orchestrator) |         ❌         | Saga Pattern not needed anymore<br/> → Simple transaction is used to finalize an order, part of the order module                                                    |

The [common](https://github.com/t2-project/common) package that is used as a jar file by all microservices for inter-service communication is not used anymore. Some parts are moved to the respective domain-specific module. There is now a `config` package that includes the configuration that is relevant for multiple modules, however, without any class definitions required used for communication.

## Quick start

You can run T2-Modulith with all dependencies without building anything by using the existing Docker images from [DockerHub](https://hub.docker.com/r/t2project/modulith) and Docker Compose:

```sh
docker compose up
```

## Usage

You can use the application either by accessing the UI or by using the HTTP endpoints.

- UI: http://localhost:8081/ui/
- API: http://localhost:8081/swagger-ui/index.html

On how to use the UI see the usage section of the [T2-Project UI repository](https://github.com/t2-project/ui#usage).
How to use the API is described below in section [HTTP Endpoints](#http-endpoints).

## Build & Run

There are different ways on how to build and run the application. 

### Build with Maven and run with Docker

Application gets build by Maven first, then packaged into a Docker image and finally executed.

```sh
./mvnw clean install -DskipTests
docker build -t t2project/modulith:main .
docker compose up
```

### Build and run with Docker

A multi-stage Dockerfile is used to build the application and place it into a smaller Docker image used for running it.

```sh
docker build -t t2project/modulith:main -f Dockerfile.full-build .
docker compose up
```

### Run in development mode

Development mode means that you run the Modulith application on your own, e.g. in debugging mode using your IDE, and only run the dependencies (databases and fake credit institute) with Docker.

Important: To run the application in development mode, set the Spring profile to `dev`.

Run dependencies:

```sh
docker compose -f docker-compose-dev.yml up
```

If you want to run the application directly from your command line, you can use one of the following commands:

- Spring Boot Maven Plugin (every shell except Powershell):
    ```sh
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
    ```
- Spring Boot Maven Plugin (Powershell):
    ```powershell
    ./mvnw spring-boot:run -D"spring-boot.run.profiles=dev"
    ```
- Java:
    ```sh
    java -jar target/t2-modulith.war --spring.profiles.active=dev
    ```

---

## Architecture

![Components Diagram created by Spring Modulith](./component-diagram.svg)

### UI Module

The UI module provides a simple website. It is based on the UI of the original [TeaStore](https://github.com/DescartesResearch/TeaStore) and implemented with JSP.
The UI module communicates with the UI backend module directly by inter-process communication.

### UI Backend Module

The UI backend module acts as an API gateway and interacts with the cart, inventory and order module.
It provides REST endpoints, but they are not used by the UI.

### Order Module

The order module is responsible for saving orders to the database and handling the completion of orders (trigger payment, committing reservations, deleting cart).

### Inventory Module

The inventory module is the inventory of the T2-Project.
It manages the products and reservations.

The products are the teas that the store sells and reservations exist to express that a user plans to buy some units of a product.

### Payment Module

The payment module is responsible for contacting the payment provider.

In a more real situation, a payment service would contact different payment providers, e.g. paypal or a certain credit institute based on which payment method a user chose.
However, here the payment module knows only one payment provider and always contact that one.

The default payment provider is the fake [Credit Institute Service](https://github.com/t2-project/creditinstitute).

### Cart Module

The cart module manages the shopping carts of the T2-Project.
The content of a user's shopping cart is a map of Strings to Integers.
Within the context of the T2-Project it contains which products (identified by their id) and how many units there of a users wants to buy.

---

## HTTP Endpoints

The modules `cart` and `inventory` provide REST endpoints for their specific domain. The `UI backend` module provides REST endpoints that represent the actual functionality of the whole application.
The JSP-specific http endpoints provided by the `ui` module are not listed here.

**UI Backend:**

* `/products` GET list of all products in the inventory
* `/cart/{id}` GET list of all products in cart of a specific session
* `/cart/{id}` POST list of products to add/update/delete in/from cart of a specific session
* `/confirm` POST to place an order

**Cart:**

* `/cart/{id}` GET, PUT, POST or DELETE the cart with sessionId `id` (does only manipulate the cart, no reservations are made in the inventory)

**Inventory:**

* `/inventory/{id}` : GET, PUT, POST or DELETE the product with productId `id`
* `/generate` : GET to generate new products
* `/restock` : GET to restock all items

### API Usage Examples

Base URL: `http://localhost:8081/`

In the examples we are using `<productId>` and `{sessionId}` as placeholders. You have to replace them with real values. The session id can be arbitrary string, the product id has to match a product that is actually in your inventory.

#### Access the products / inventory

Endpoint to get all products is `GET /products`:

```sh
curl http://localhost:8081/products
```

Response:

```json5
[
    {"id":"3a2b20be-2582-47c6-9524-41b5922dbb2c","name":"Earl Grey (loose)","description":"very nice Earl Grey (loose) tea","units":529,"price":2.088258409676226},
// [...]
    {"id":"89854456-15e0-4ab1-aa0c-aa41915a988c","name":"Sencha (25 bags)","description":"very nice Sencha (25 bags) tea","units":101,"price":0.6923181656954707}
]
```

There are more REST endpoints automatically provided by Spring Data to access inventory items...

An explanatory request to get a specific inventory item with id `<productId>`:

```sh
curl http://localhost:8081/inventory/<productId>
```

Response:

```json
{
  "name" : "Darjeeling (loose)",
  "description" : "very nice Darjeeling (loose) tea",
  "units" : 264,
  "price" : 1.6977123432245298,
  "reservations" : [ ],
  "_links" : {
    "self" : {
      "href" : "http://localhost:8081/inventory/<productId>"
    },
    "inventory" : {
      "href" : "http://localhost:8081/inventory/<productId>"
    }
  }
}
```

#### Restocking the Inventory

If all items are sold out, this is how you restock all of them.

```sh
curl http://localhost:8081/restock
```

If there are no products in the inventory (not as in '0 units of a product' but as in 'there is no product at all'), do this to generate new products.

```sh
curl http://localhost:8081/generate
```

#### Get the products in your cart

The cart is linked to a session id. 
Request to get the cart for the session id `{sessionId}`:

```sh
curl http://localhost:8081/cart/{sessionId}
```

Response if cart is empty:

```json
[]
```

Response if cart includes one product with 3 units:

```json
[{"id":"<productId>","name":"Darjeeling (loose)","description":"very nice Darjeeling (loose) tea","units":3,"price":1.6977123432245298}]
```

#### Update the cart

Add product with id `<productId>` with 3 units to cart of session with id `{sessionId}`:

```sh
curl -i -X POST -H "Content-Type:application/json" -d '{"content":{"<productId>":3}}' http://localhost:8081/cart/{sessionId}
```

Response (successfully added items):

```json
[{"id":"<productId>","name":"Darjeeling (loose)","description":"very nice Darjeeling (loose) tea","units":3,"price":1.6977123432245298}]
```

Remove product with id `<productId>` from cart of session with id `{sessionId}`:

```sh
curl -i -X POST -H "Content-Type:application/json" -d '{"content":{"<productId>":-3}}'  http://localhost:8081/cart/{sessionId}
```

Response:

```json
[]
```

The response is empty, because it only includes added items, not removed items.

#### Confirm Order

With this, you place an order for the session `{sessionId}`, with the given payment details.

```sh
curl -i -X POST -H "Content-Type:application/json" -d '{"cardNumber":"num","cardOwner":"own","checksum":"sum", "sessionId":"{sessionId}"}' http://localhost:8081/confirm
```

---

## Application Properties

Property files: `./src/main/resources/application-*.yaml`

**T2 configuration:**

| property                      | read from env var             | description                                                                                     |
|-------------------------------|-------------------------------|-------------------------------------------------------------------------------------------------|
| t2.cart.TTL		                 | T2_CART_TTL                   | time to live of items in cart (in seconds)                                                      |
| t2.cart.taskRate	             | T2_CART_TASKRATE              | rate at which the cart checks for items that exceeded their TTL (in milliseconds)               |
| t2.inventory.size             | INVENTORY_SIZE                | number of items to be generated into the inventory repository on start up                       |
| t2.inventory.TTL              | T2_INVENTORY_TTL              | time to live of reservations (in seconds)                                                       |
| t2.inventory.taskRate         | T2_INVENTORY_TASKRATE         | rate at which the inventory checks for reservations that exceeded their TTL (in milliseconds).  |
| t2.payment.provider.dummy.url | T2_PAYMENT_PROVIDER_DUMMY_URL | url of the payment provider.                                                                    |
| t2.payment.provider.timeout   | T2_PAYMENT_PROVIDER_TIMEOUT   | timeout in seconds. the payment service waits this long for an reply from the payment provider. |

Setting either `TTL` or `taskrate` to a value less or equal to zero disables the collection of expired entries (cart module and inventory module).

**Postgres database:**

| property                            | read from env var                   | description                                      |
|-------------------------------------|-------------------------------------|--------------------------------------------------|
| spring.datasource.url               | SPRING_DATASOURCE_URL               |                                                  |
| spring.datasource.username          | SPRING_DATASOURCE_USERNAME          |                                                  |
| spring.datasource.password          | SPRING_DATASOURCE_PASSWORD          |                                                  |
| spring.datasource.driver-class-name | SPRING_DATASOURCE_DRIVER_CLASS_NAME | Should be usually set to `org.postgresql.Driver` |

**MongoDB database:**

| property                | read from env var | description          |
|-------------------------|-------------------|----------------------|
| spring.data.mongodb.uri | MONGO_HOST        | host of the mongo db |

---

## Technical Notes

- The application gets packaged as a war and not as jar, because of the limitations of JSP used for the UI (see Spring Docs about [JSP Limitations](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.servlet.embedded-container.jsp-limitations))
- Default port is set to 8081 to have the same base URL than the [T2-Project UI Backend Service](https://github.com/t2-project/uibackend)

# T2-Modulith

The T2-Modulith is an implementation of the [T2-Project](https://t2-documentation.readthedocs.io/) as a monolith.
It has still a modular structure enforced by [Spring Modulith](https://spring.io/projects/spring-modulith).

## Build and Run

Refer to the [Documentation](https://t2-documentation.readthedocs.io/en/latest/monolith/deploy.html) on how to build, run or deploy and use the T2-Modulith application.

## Application Properties

Property files: `./src/main/resources/application-*.yaml`

**T2 configuration:**

| property                      | read from env var             | description                                                                                     |
|-------------------------------|-------------------------------|-------------------------------------------------------------------------------------------------|
| t2.cart.TTL	                  | T2_CART_TTL                   | time to live of items in cart (in seconds)                                                      |
| t2.cart.taskRate	             | T2_CART_TASKRATE              | rate at which the cart checks for items that exceeded their TTL (in milliseconds)               |
| t2.inventory.size             | INVENTORY_SIZE                | number of items to be generated into the inventory repository on start up                       |
| t2.inventory.TTL              | T2_INVENTORY_TTL              | time to live of reservations (in seconds)                                                       |
| t2.inventory.taskRate         | T2_INVENTORY_TASKRATE         | rate at which the inventory checks for reservations that exceeded their TTL (in milliseconds).  |
| t2.payment.provider.enabled   | T2_PAYMENT_PROVIDER_ENABLED   | boolean value, defaults to true. if false, no connection to payment provider is made.           |
| t2.payment.provider.timeout   | T2_PAYMENT_PROVIDER_TIMEOUT   | timeout in seconds. the payment service waits this long for an reply from the payment provider. |
| t2.payment.provider.dummy.url | T2_PAYMENT_PROVIDER_DUMMY_URL | url of the payment provider.                                                                    |

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

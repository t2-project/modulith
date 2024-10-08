# T2-Modulith

The T2-Modulith is an implementation of the [T2-Project](https://t2-documentation.readthedocs.io/) as a monolith.
It has still a modular structure enforced by [Spring Modulith](https://spring.io/projects/spring-modulith). Therefore, we call it *T2-Modulith*.

You can find more information about the migration from the microservices implementation to a monolithic implementation in the [Architecture documentation](https://t2-documentation.readthedocs.io/en/latest/modulith/arch.html).

## Build and Run

Refer to the [Deployment documentation](https://t2-documentation.readthedocs.io/en/latest/modulith/deploy.html) on how to build, run or deploy the T2-Modulith application.

## Usage

Refer to the [Usage documentation](https://t2-documentation.readthedocs.io/en/latest/modulith/use.html) on how to use the T2-Modulith application. There are two ways: via the UI or via the HTTP API endpoints.

## Application Properties

You can configure many properties used at start and during runtime of the T2-Modulith application.

There are different property files located at `./src/main/resources/application-*.yaml`.

Depending on your active Spring profiles, different property files are used. See e.g. documentation section [Run in development mode](https://t2-documentation.readthedocs.io/en/latest/modulith/deploy.html#run-in-development-mode) on how to use the profile `dev`.

**T2 configuration:**

| property                                | read from env var                        | description                                                                                                                                                   |
|-----------------------------------------|------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| t2.cart.TTL                             | T2_CART_TTL                              | time to live of items in cart (in seconds)                                                                                                                    |
| t2.cart.taskRate                        | T2_CART_TASKRATE                         | rate at which the cart checks for items that exceeded their TTL (in milliseconds)                                                                             |
| t2.inventory.size                       | T2_INVENTORY_SIZE                        | number of items to be generated into the inventory repository on start up                                                                                     |
| t2.inventory.TTL                        | T2_INVENTORY_TTL                         | time to live of reservations (in seconds)                                                                                                                     |
| t2.inventory.taskRate                   | T2_INVENTORY_TASKRATE                    | rate at which the inventory checks for reservations that exceeded their TTL (in milliseconds).                                                                |
| t2.inventory.setUnitsToMax              | T2_INVENTORY_SET_UNITS_TO_MAX            | boolean value. if true all units will be set to max integer. helpful for load testing scenarios                                                               |
| t2.payment.provider.enabled             | T2_PAYMENT_PROVIDER_ENABLED              | boolean value, defaults to true. if false, no connection to payment provider is made.                                                                         |
| t2.payment.provider.timeout             | T2_PAYMENT_PROVIDER_TIMEOUT              | timeout in seconds. the payment service waits this long for an reply from the payment provider.                                                               |
| t2.payment.provider.dummy.url           | T2_PAYMENT_PROVIDER_DUMMY_URL            | url of the payment provider.                                                                                                                                  |
| t2.computation-simulator.enabled        | T2_COMPUTATION_SIMULATOR_ENABLED         | boolean value, defaults to false. if true, the service computation-simulator gets called when an order is confirmed to simulate a compute intensive scenario. |
| t2.computation-simulator.pi.totalPoints | T2_COMPUTATION_SIMULATOR_PI_TOTAL_POINTS | total points that should be calculated of PI. 100000000 requires around 5 sec, 1000000000 around ~1 min                                                       |

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

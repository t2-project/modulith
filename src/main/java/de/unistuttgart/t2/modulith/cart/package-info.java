@org.springframework.lang.NonNullApi
// TODO Remove dependency inventory from the cart module
@org.springframework.modulith.ApplicationModule(
    allowedDependencies = {"inventory"}
)
package de.unistuttgart.t2.modulith.cart;
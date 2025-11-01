package com.radwa.store.carts;


public class CartEmptyException extends RuntimeException {

    public CartEmptyException() {
        super("Cart is empty");
    }
}

package com.example.fooddelivery.ui.cart;

public class CheckoutSummary {
    public static final long DEFAULT_DELIVERY_FEE = 15000;
    public static final long DEFAULT_SERVICE_FEE = 0;
    public static final long DEFAULT_VOUCHER_DISCOUNT = 0;

    private final long subtotal;
    private final long deliveryFee;
    private final long serviceFee;
    private final long voucherDiscount;

    public CheckoutSummary(long subtotal, long deliveryFee, long serviceFee, long voucherDiscount) {
        this.subtotal = Math.max(0, subtotal);
        this.deliveryFee = Math.max(0, deliveryFee);
        this.serviceFee = Math.max(0, serviceFee);
        this.voucherDiscount = Math.max(0, voucherDiscount);
    }

    public long getSubtotal() {
        return subtotal;
    }

    public long getDeliveryFee() {
        return deliveryFee;
    }

    public long getServiceFee() {
        return serviceFee;
    }

    public long getVoucherDiscount() {
        return voucherDiscount;
    }

    public long getTotal() {
        return Math.max(0, subtotal + deliveryFee + serviceFee - voucherDiscount);
    }

    public static boolean canPlaceOrder(boolean cartEmpty,
                                        boolean hasDeliveryAddress,
                                        boolean hasPaymentMethod,
                                        boolean loading) {
        return !cartEmpty && hasDeliveryAddress && hasPaymentMethod && !loading;
    }
}

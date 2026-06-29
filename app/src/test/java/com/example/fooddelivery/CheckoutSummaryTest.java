package com.example.fooddelivery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.fooddelivery.ui.cart.CheckoutSummary;

import org.junit.Test;

public class CheckoutSummaryTest {

    @Test
    public void totalIncludesDeliveryFeeAndDiscount() {
        CheckoutSummary summary = new CheckoutSummary(105000, 15000, 0, 0);

        assertEquals(105000, summary.getSubtotal());
        assertEquals(15000, summary.getDeliveryFee());
        assertEquals(0, summary.getServiceFee());
        assertEquals(0, summary.getVoucherDiscount());
        assertEquals(120000, summary.getTotal());
    }

    @Test
    public void totalNeverDropsBelowZero() {
        CheckoutSummary summary = new CheckoutSummary(20000, 15000, 0, 50000);

        assertEquals(0, summary.getTotal());
    }

    @Test
    public void placeOrderRequiresCartAddressPaymentAndNoLoading() {
        assertTrue(CheckoutSummary.canPlaceOrder(false, true, true, false));
        assertFalse(CheckoutSummary.canPlaceOrder(true, true, true, false));
        assertFalse(CheckoutSummary.canPlaceOrder(false, false, true, false));
        assertFalse(CheckoutSummary.canPlaceOrder(false, true, false, false));
        assertFalse(CheckoutSummary.canPlaceOrder(false, true, true, true));
    }
}

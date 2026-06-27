package com.example.fooddelivery.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.fooddelivery.R;
import com.example.fooddelivery.ui.cart.Checkout;

public class OrderDetailFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.order_fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Back button
        view.findViewById(R.id.ivBack).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.popBackStack();
        });

        // Review button
        view.findViewById(R.id.btnReviewOrder).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            // We use the action defined in nav graph to go to review
            navController.navigate(R.id.action_orderDetailFragment_to_orderReviewFragment);
        });

        // Reorder button -> Checkout
        view.findViewById(R.id.btnReorderBottom).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), Checkout.class);
            startActivity(intent);
        });
    }
}

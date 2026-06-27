package com.example.fooddelivery.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fooddelivery.R;

public class OrderManagementFragment extends Fragment {

    private static final String EXTRA_ORDERS_TAB = "orders_tab";

    private final TextView[] tabs = new TextView[3];
    private final View[] indicators = new View[3];
    private View tabProcessingContainer;
    private View tabCompletedContainer;
    private View tabCancelledContainer;
    private ViewPager2 viewPager;

    private static final int COLOR_ACTIVE = 0xFFFF6B35;
    private static final int COLOR_INACTIVE = 0xFF999999;
    private static final int COLOR_TRANSPARENT = 0x00000000;
    private static final int COLOR_INDICATOR = 0xFFFF6B35;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.order_fragment_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setVisibility(View.GONE);

        initViews(view);
        setupViewPager();
        setupTabClicks();
        openRequestedTabIfAny();
    }

    private void initViews(View view) {
        tabs[0] = view.findViewById(R.id.tabPending);
        tabs[1] = view.findViewById(R.id.tabCompleted);
        tabs[2] = view.findViewById(R.id.tabCancelled);

        indicators[0] = view.findViewById(R.id.indicatorPending);
        indicators[1] = view.findViewById(R.id.indicatorCompleted);
        indicators[2] = view.findViewById(R.id.indicatorCancelled);

        tabProcessingContainer = view.findViewById(R.id.tabPendingContainer);
        tabCompletedContainer = view.findViewById(R.id.tabCompletedContainer);
        tabCancelledContainer = view.findViewById(R.id.tabCancelledContainer);

        viewPager = view.findViewById(R.id.viewPager);
    }

    private void setupViewPager() {
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 1: return OrderListFragment.newInstance("completed");
                    case 2: return OrderListFragment.newInstance("cancelled");
                    default: return OrderListFragment.newInstance("processing");
                }
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                selectTab(position);
            }
        });
    }

    private void setupTabClicks() {
        tabProcessingContainer.setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        tabCompletedContainer.setOnClickListener(v -> viewPager.setCurrentItem(1, true));
        tabCancelledContainer.setOnClickListener(v -> viewPager.setCurrentItem(2, true));
    }

    private void openRequestedTabIfAny() {
        int initialTab = 0;
        Intent intent = requireActivity().getIntent();
        if (intent != null) {
            String requestedTab = intent.getStringExtra(EXTRA_ORDERS_TAB);
            if ("completed".equals(requestedTab)) {
                initialTab = 1;
            } else if ("cancelled".equals(requestedTab)) {
                initialTab = 2;
            }
            intent.removeExtra(EXTRA_ORDERS_TAB);
        }
        viewPager.setCurrentItem(initialTab, false);
        selectTab(initialTab);
    }

    private void selectTab(int index) {
        for (int i = 0; i < tabs.length; i++) {
            tabs[i].setTextColor(i == index ? COLOR_ACTIVE : COLOR_INACTIVE);
            tabs[i].setTypeface(null, i == index
                    ? android.graphics.Typeface.BOLD
                    : android.graphics.Typeface.NORMAL);
            indicators[i].setBackgroundColor(i == index ? COLOR_INDICATOR : COLOR_TRANSPARENT);
        }
    }
}

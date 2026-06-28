package com.example.fooddelivery.ui.order;

import com.example.fooddelivery.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

/**
 * Thay tháº¿ OrdersFragment (rá»—ng) vÃ  OrderManagementActivity.
 * ÄÃ¢y lÃ  Fragment Ä‘Æ°á»£c nav_ordes.xml trá» tá»›i, chá»©a toÃ n bá»™
 * logic tab Äang chá» / ÄÃ£ hoÃ n thÃ nh / ÄÃ£ huá»· + ViewPager2.
 *
 * XoÃ¡ OrdersFragment.java vÃ  OrderManagementActivity.java sau khi dÃ¹ng file nÃ y.
 */
public class OrderManagementFragment extends Fragment {

    private TextView tabPending, tabCompleted, tabCancelled;
    private View indicatorPending, indicatorCompleted, indicatorCancelled;
    private ViewPager2 viewPager;

    private static final int COLOR_ACTIVE      = 0xFFFF6B35;
    private static final int COLOR_INACTIVE    = 0xFF999999;
    private static final int COLOR_TRANSPARENT = 0x00000000;
    private static final int COLOR_INDICATOR   = 0xFFFF6B35;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // DÃ¹ng láº¡i layout fragment_order_management.xml
        return inflater.inflate(R.layout.order_fragment_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // áº¨n nÃºt Back vÃ¬ Ä‘Ã¢y lÃ  Fragment trong bottom nav (khÃ´ng cáº§n back)
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setVisibility(View.GONE);

        initViews(view);
        setupViewPager();
        setupTabClicks();
        selectTab(0);
    }

    private void initViews(View view) {
        tabPending   = view.findViewById(R.id.tabPending);
        tabCompleted = view.findViewById(R.id.tabCompleted);
        tabCancelled = view.findViewById(R.id.tabCancelled);

        indicatorPending   = view.findViewById(R.id.indicatorPending);
        indicatorCompleted = view.findViewById(R.id.indicatorCompleted);
        indicatorCancelled = view.findViewById(R.id.indicatorCancelled);

        viewPager = view.findViewById(R.id.viewPager);
    }

    private void setupViewPager() {
        FragmentActivity activity = requireActivity();
        viewPager.setAdapter(new FragmentStateAdapter(activity) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 1:  return OrderListFragment.newInstance("completed");
                    case 2:  return OrderListFragment.newInstance("cancelled");
                    default: return OrderListFragment.newInstance("pending");
                }
            }
            @Override
            public int getItemCount() { return 3; }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                selectTab(position);
            }
        });
    }

    private void setupTabClicks() {
        tabPending.setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        tabCompleted.setOnClickListener(v -> viewPager.setCurrentItem(1, true));
        tabCancelled.setOnClickListener(v -> viewPager.setCurrentItem(2, true));
    }

    private void selectTab(int index) {
        // Reset táº¥t cáº£
        tabPending.setTextColor(COLOR_INACTIVE);
        tabCompleted.setTextColor(COLOR_INACTIVE);
        tabCancelled.setTextColor(COLOR_INACTIVE);
        tabPending.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabCompleted.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabCancelled.setTypeface(null, android.graphics.Typeface.NORMAL);

        indicatorPending.setBackgroundColor(COLOR_TRANSPARENT);
        indicatorCompleted.setBackgroundColor(COLOR_TRANSPARENT);
        indicatorCancelled.setBackgroundColor(COLOR_TRANSPARENT);

        // KÃ­ch hoáº¡t tab Ä‘Æ°á»£c chá»n
        switch (index) {
            case 0:
                tabPending.setTextColor(COLOR_ACTIVE);
                tabPending.setTypeface(null, android.graphics.Typeface.BOLD);
                indicatorPending.setBackgroundColor(COLOR_INDICATOR);
                break;
            case 1:
                tabCompleted.setTextColor(COLOR_ACTIVE);
                tabCompleted.setTypeface(null, android.graphics.Typeface.BOLD);
                indicatorCompleted.setBackgroundColor(COLOR_INDICATOR);
                break;
            case 2:
                tabCancelled.setTextColor(COLOR_ACTIVE);
                tabCancelled.setTypeface(null, android.graphics.Typeface.BOLD);
                indicatorCancelled.setBackgroundColor(COLOR_INDICATOR);
                break;
        }
    }
}


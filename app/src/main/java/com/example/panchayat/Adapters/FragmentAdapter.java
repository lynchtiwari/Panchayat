package com.example.panchayat.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.panchayat.Fragments.CallsFragment;
import com.example.panchayat.Fragments.ChatsFragment;
import com.example.panchayat.Fragments.StatusFragment;

public class FragmentAdapter extends FragmentPagerAdapter {

    public FragmentAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if(position==0)
            return new ChatsFragment();

        else if (position==1)
                  return new StatusFragment();

        else if (position==2)
            return new CallsFragment();

        else
            return new ChatsFragment();
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        String title="";

        if(position==0)
            title="CHATS";

        else if (position==1)
                 title="STATUS";

        else if (position==2)
                 title="CALLS";

      return title;

    }
}

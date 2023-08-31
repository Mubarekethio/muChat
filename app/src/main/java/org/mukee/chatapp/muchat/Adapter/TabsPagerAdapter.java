package org.mukee.chatapp.muchat.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.mukee.chatapp.muchat.Fragments.ChatsFragment;
import org.mukee.chatapp.muchat.Fragments.RequestsFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter {


    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new ChatsFragment();
            case 1:
                return new RequestsFragment();
            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return 2; // 2 is total fragment number (e.x- Chats, Requests)
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "CHATS"; // ChatsFragment
            case 1:
                return "REQUESTS"; // ttttRequestsFragment
            default:
                return null;
        }
        //return super.getPageTitle(position);
    }
}

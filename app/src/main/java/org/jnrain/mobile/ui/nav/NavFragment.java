/*
 * Copyright 2013 JNRain
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jnrain.mobile.ui.nav;

import name.xen0n.cytosol.app.CytosolFragment;
import name.xen0n.cytosol.ui.widget.NavItem;
import name.xen0n.cytosol.ui.widget.NavItemView;
import name.xen0n.cytosol.ui.widget.NavMenuAdapter;

import org.jnrain.mobile.R;
import org.jnrain.mobile.accounts.kbs.KBSLogoutRequest;
import org.jnrain.mobile.accounts.kbs.KBSLogoutRequestListener;
import org.jnrain.mobile.ui.AboutActivity;
import org.jnrain.mobile.ui.kbs.GlobalHotPostsListFragment;
import org.jnrain.mobile.ui.kbs.SectionListFragment;
import org.jnrain.mobile.ui.preferences.SettingsActivity;
import org.jnrain.mobile.util.AccountStateListener;
import org.jnrain.mobile.util.GlobalState;

import roboguice.inject.InjectView;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


@SuppressWarnings("rawtypes")
public class NavFragment extends CytosolFragment
        implements AccountStateListener {
    @InjectView(R.id.textUserID)
    TextView textUserID;
    @InjectView(R.id.listNavMenu)
    ListView listNavMenu;

    NavMenuAdapter _menuAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        _menuAdapter = new JNRainNavMenuAdapter(getActivity());

        // register self
        NavManager.setNavFragment(this);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_nav, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setUserName(GlobalState.getUserName());
        GlobalState.registerAccountStateListener(this);

        // nav menu
        _menuAdapter.addItem(new NavItem(
                R.string.title_kbs_global_hot_posts_list,
                R.drawable.ic_nav_hotpost,
                true,
                true) {
            @Override
            public void onNavItemActivated(Context context) {
                NavFragment.this.clearBackStack();
                NavFragment.this.switchMainContentTo(
                        new GlobalHotPostsListFragment(),
                        false);
            }
        });

        _menuAdapter.addItem(new NavItem(
                R.string.title_kbs_section_list,
                R.drawable.ic_nav_sections,
                true) {
            @Override
            public void onNavItemActivated(Context context) {
                NavFragment.this.clearBackStack();
                NavFragment.this.switchMainContentTo(
                        new SectionListFragment(),
                        false);
            }
        });

        _menuAdapter.addItem(new NavItem(
                R.string.title_preference,
                R.drawable.ic_nav_settings,
                false) {
            @Override
            public void onNavItemActivated(Context context) {
                SettingsActivity.show(context);
            }
        });

        _menuAdapter.addItem(new NavItem(
                R.string.title_about,
                R.drawable.ic_nav_about,
                false) {
            @Override
            public void onNavItemActivated(Context context) {
                AboutActivity.show(context);
            }
        });

        _menuAdapter.addItem(new NavItem(
                R.string.nav_remove_account,
                R.drawable.ic_nav_logout,
                false) {
            @SuppressWarnings("unchecked")
            @Override
            public void onNavItemActivated(Context context) {
                // change UID display
                setUserName(R.string.uid_logging_out);

                // make request
                _listener.makeSpiceRequest(
                        new KBSLogoutRequest(),
                        new KBSLogoutRequestListener(getActivity(), true));
            }
        });

        listNavMenu.setAdapter(_menuAdapter);
        listNavMenu
            .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(
                        AdapterView<?> parent,
                        View view,
                        int position,
                        long id) {
                    NavItem item = _menuAdapter.getItem(position);

                    // update active status of items
                    if (item.canActivate()) {
                        clearMenuItemActiveStatus();
                        ((NavItemView) view).setActive(true);
                    }

                    // notify activation
                    item.onNavItemActivated(getActivity());
                }
            });
    }

    public void clearMenuItemActiveStatus() {
        int viewCount = listNavMenu.getChildCount();

        for (int i = 0; i < viewCount; i++) {
            NavItemView view = (NavItemView) listNavMenu.getChildAt(i);

            view.setActive(false);
        }
    }

    public void setUserName(String uid) {
        textUserID.setText(uid);
    }

    public void setUserName(int resId) {
        textUserID.setText(resId);
    }

    @Override
    public void onAccountLoggedIn(String uid) {
        setUserName(uid);
    }

    @Override
    public void onAccountLoggedOut() {
        // set user name
        setUserName(R.string.uid_logged_out);
    }
}

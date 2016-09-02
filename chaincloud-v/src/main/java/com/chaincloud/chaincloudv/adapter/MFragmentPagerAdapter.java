/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chaincloud.chaincloudv.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class MFragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

	@SuppressWarnings("rawtypes")
	private Class fragments[];


	public MFragmentPagerAdapter(FragmentManager fm, Class[] fragments) {
		super(fm);

		this.fragments = fragments;
	}

	@Override
	public Fragment getItem(int arg0) {
		try {
			return (Fragment) fragments[arg0].newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getCount() {
		return fragments.length;
	}

}

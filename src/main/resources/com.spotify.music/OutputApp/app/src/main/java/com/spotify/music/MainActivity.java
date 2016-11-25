package com.spotify.music;

import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.DATA_BUNDLE_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.INTENT_SUFFIX;
import static edu.stonybrook.cs.netsys.uiwearlib.viewProtocol.ViewUtil.renderView;
import static edu.stonybrook.cs.netsys.uiwearlib.viewProtocol.ViewUtil.setViewListener;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WearableListView;
import android.view.View;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataBundle;
import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataNode;
import edu.stonybrook.cs.netsys.uiwearlib.viewProtocol.UIWearFragment;
import edu.stonybrook.cs.netsys.uiwearlib.viewProtocol.WearableListAdapter;

public class MainActivity extends Activity {
    // resource receiver from wear proxy
    private ResReceiver mResReceiver;
    private GridViewPager mPager;

    private String[] mPreferenceIdArray;
    private int[] mWearViewIdIndexArray;
    private int[] mPhoneViewIdIndexArray;

    private int[] mWearItemLayouts;
    private String[] mPhoneItemViewIds;
    private int[] mWearItemViewIdIndexArray;
    private int[] mPhoneItemViewIdIndexArray;

    public ArrayList<DataNode[]> mListDataNodes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferenceIdArray = getResources().getStringArray(R.array.prefs);
        int[] mLayouts = getResourceId(R.array.layouts);

        mWearViewIdIndexArray = getResourceId(R.array.wear_view_id_array);
        mPhoneViewIdIndexArray = getResourceId(R.array.phone_view_id_array);
        mPager = (GridViewPager) findViewById(R.id.gridViewPager);
        mPager.setAdapter(new LayoutAdapter(getFragmentManager(), mLayouts));

        mWearItemLayouts = getResourceId(R.array.wear_item_layouts);
        mPhoneItemViewIds = getResources().getStringArray(R.array.phone_item_view_ids);
        mWearItemViewIdIndexArray = getResourceId(R.array.wear_item_view_id_array);
        mPhoneItemViewIdIndexArray = getResourceId(R.array.phone_item_view_id_array);

    }

    private class LayoutAdapter extends FragmentGridPagerAdapter {
        private int[] mLayouts;

        LayoutAdapter(FragmentManager fm, int[] layouts) {
            super(fm);
            mLayouts = layouts;

        }

        @Override
        public Fragment getFragment(int row, int col) {
            int layoutId = mLayouts[row];
            return UIWearFragment.create(layoutId);
        }

        @Override
        public int getRowCount() {
            return mLayouts.length;
        }

        @Override
        public int getColumnCount(int i) {
            // currently set to only 2 column,
            // first column for wear app content
            // TODO: 11/15/16 second column for open app on phone
            return 2;
        }

        @Override
        public Drawable getBackgroundForPage(int row, int column) {
            return super.getBackgroundForPage(row, column);
        }


    }

    /**
     * retrieve all ids in the given layout
     *
     * @param arrayResourceId the given layout resource id
     * @return an array of ids for the given layout
     */
    int[] getResourceId(int arrayResourceId) {
        TypedArray typedArray =
                getResources().obtainTypedArray(arrayResourceId);
        int[] result = new int[typedArray.length()];

        for (int i = 0; i < typedArray.length(); i++) {
            result[i] = typedArray.getResourceId(i, -1);
        }
        typedArray.recycle();
        return result;
    }

    @Override
    protected void onStart() {
        mResReceiver = new ResReceiver();
        IntentFilter intentFilter = new IntentFilter();

        Logger.v("filter : " + getPackageName() + INTENT_SUFFIX);
        intentFilter.addAction(getPackageName() + INTENT_SUFFIX);
        registerReceiver(mResReceiver, intentFilter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mResReceiver);
        super.onStop();
    }

    private class ResReceiver extends BroadcastReceiver {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                Bundle bundle = intent.getBundleExtra(DATA_BUNDLE_KEY);
                DataBundle dataBundle = bundle.getParcelable(DATA_BUNDLE_KEY);
//                DataBundle dataBundle = intent.getParcelableExtra(DATA_BUNDLE_KEY);
                if (dataBundle == null) {
                    Logger.w("cannot get data bundle");
                    return;
                }

                // prefId : e.g., play_song_pref, recent_list_pref
                String prefId = dataBundle.getPreferenceId();
                Logger.i("prefId: " + prefId);
                int pageIndex = Arrays.asList(mPreferenceIdArray).indexOf(prefId);
                Logger.i("prefId index : " + pageIndex);
                if (pageIndex == -1) {
                    // no suitable preference id for rendering layout
                    Logger.w("no preference layout found!");
                    return;
                }

                mPager.setCurrentItem(pageIndex, 0, false);
                // each node contains clickId, phoneViewId, text, image bytes
                ArrayList<DataNode> dataNodes = dataBundle.getDataNodes();
                for (DataNode node : dataNodes) {
                    Logger.d("new node normal: " + node);
                }

                int wearIdIndex = mWearViewIdIndexArray[pageIndex];
                // get wear view id array of current page
                int[] wearViewIds = getResourceId(wearIdIndex);

                int phoneIdIndex = mPhoneViewIdIndexArray[pageIndex];
                String[] phoneViewIds = getResources().getStringArray(phoneIdIndex);

                // list view data need to be parsed first

                ArrayList<DataNode[]> listNodes = dataBundle.getListNodes();
                for (DataNode[] list : listNodes) {
                    for (DataNode node : list) {
                        Logger.d("new node list: " + node);
                    }
                }
                if (listNodes.size() > 0) {
                    mListDataNodes = listNodes;
                }

                // normal view
                for (DataNode node : dataNodes) {
                    Logger.d("new normal node: " + node);
                    parseData(context, wearViewIds, phoneViewIds, node);
                }
            }
        }
    }

    private void parseData(final Context context, int[] wearViewIds, String[] phoneViewIds,
            DataNode node) {
        String phoneViewId = node.getViewId();
        Logger.i("phoneViewId: " + phoneViewId);

        int index = Arrays.asList(phoneViewIds).indexOf(phoneViewId);
        if (index == -1) {
            Logger.w("cannot find wear id index for phoneViewId: " + phoneViewId);
            return;
        }
        View nodeView = mPager.findViewById(wearViewIds[index]);

        if (nodeView instanceof WearableListView) {
            Logger.d("list view");
            int itemIndex = Arrays.asList(mPhoneItemViewIds).indexOf(phoneViewId);
            final int wearItemLayoutResId = mWearItemLayouts[itemIndex];

            int wearItemIdIndex = mWearItemViewIdIndexArray[itemIndex];
            final int[] wearItemIds = getResourceId(wearItemIdIndex);

            int phoneItemIdIndex = mPhoneItemViewIdIndexArray[itemIndex];
            String[] phoneItemViewIds = getResources().getStringArray(phoneItemIdIndex);
            final List<String> phoneItemViewIdList = Arrays.asList(phoneItemViewIds);

            WearableListView listView = (WearableListView) nodeView;
            listView.setGreedyTouchMode(true);
//            listView.setAdapter(new BaseQuickAdapter<DataNode[], BaseViewHolder>(
//                    wearItemLayoutResId, mListDataNodes) {
//                @Override
//                protected void convert(BaseViewHolder baseViewHolder,
//                        DataNode[] dataNodes) {
//                    for (DataNode node : dataNodes) {
//
//                        String phoneItemNodeViewId = node.getViewId();
//                        int index = phoneItemViewIdList.indexOf(phoneItemNodeViewId);
//
//                        View itemNodeView = baseViewHolder.getView(wearItemIds[index]);
//                        setViewListener(node, itemNodeView);
//                        renderView(context, node, itemNodeView);
//
//                    }
//                }
//            });

            WearableListAdapter adapter = new WearableListAdapter(context, wearItemLayoutResId,
                    phoneItemViewIdList, wearItemIds, mListDataNodes);
            listView.setAdapter(adapter);
            listView.invalidate();

        } else {
            // for normal view nodes
            setViewListener(node, nodeView);
            renderView(context, node, nodeView);
        }
    }


}

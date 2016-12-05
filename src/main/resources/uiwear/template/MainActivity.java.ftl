package ${app.appPkgName};

import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.CACHE_STATUS_KEY;
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
import edu.stonybrook.cs.netsys.uiwearlib.viewProtocol.ViewUtil;
import edu.stonybrook.cs.netsys.uiwearlib.viewProtocol.WearableListAdapter;

public class MainActivity extends Activity {
    private ResReceiver mResReceiver;
    private GridViewPager mPager;

    private String[] mPreferenceIdArray;
    private int[] mWearViewIdIndexArray;
    private int[] mPhoneViewIdIndexArray;

    private int[] mWearItemLayouts;
    private String[] mPhoneItemViewIds;
    private int[] mWearItemViewIdIndexArray;
    private int[] mPhoneItemViewIdIndexArray;

    public ArrayList<ArrayList<DataNode>> mListDataNodes = new ArrayList<>();

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
            return 1;
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
                ViewUtil.isCachedEnable = intent.getBooleanExtra(CACHE_STATUS_KEY, true);
                Bundle bundle = intent.getBundleExtra(DATA_BUNDLE_KEY);
                DataBundle dataBundle = bundle.getParcelable(DATA_BUNDLE_KEY);
                if (dataBundle == null) {
                    Logger.w("cannot get data bundle");
                    return;
                }

                String prefId = dataBundle.getPreferenceId();
                Logger.i("prefId: " + prefId);
                int pageIndex = Arrays.asList(mPreferenceIdArray).indexOf(prefId);
                Logger.i("prefId index : " + pageIndex);
                if (pageIndex == -1) {
                    Logger.w("no preference layout found!");
                    return;
                }

                mPager.setCurrentItem(pageIndex, 0, false);
                ArrayList<DataNode> dataNodes = dataBundle.getDataNodes();
                for (DataNode node : dataNodes) {
                    Logger.d("new node normal: " + node);
                }

                int wearIdIndex = mWearViewIdIndexArray[pageIndex];
                int[] wearViewIds = getResourceId(wearIdIndex);

                int phoneIdIndex = mPhoneViewIdIndexArray[pageIndex];
                String[] phoneViewIds = getResources().getStringArray(phoneIdIndex);


                ArrayList<ArrayList<DataNode>> listNodes = dataBundle.getListNodes();
                for (ArrayList<DataNode> list : listNodes) {
                    for (DataNode node : list) {
                        Logger.d("new node list: " + node);
                    }
                }
                if (listNodes.size() > 0) {
                    mListDataNodes = listNodes;
                }

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

        int index = -1;
        List<String> phoneViewIdList = Arrays.asList(phoneViewIds);
        for (int i = 0; i < phoneViewIdList.size(); i++) {
            String viewId = phoneViewIdList.get(i);
            if (phoneViewId.contains(viewId)) {
                index = i;
                break;
            }
        }
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

            WearableListAdapter adapter = new WearableListAdapter(context, wearItemLayoutResId,
                    phoneItemViewIdList, wearItemIds, mListDataNodes);
            listView.setAdapter(adapter);
            listView.invalidate();

        } else {
            setViewListener(node, nodeView);
            renderView(context, node, nodeView);
        }
    }


}

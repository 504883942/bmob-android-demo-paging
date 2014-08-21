package com.bmob.pagingdemo;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Toast;

public class MainActivity extends Activity {

	PullToRefreshListView mPullToRefreshView;
	private ILoadingLayout loadingLayout;
	ListView mMsgListView;
	List<TestData> bankCards = new ArrayList<TestData>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Bmob.initialize(this, "");
		showToast("��ǵý����AppId��д��MainActivity��BmobSDK��ʼ��������");
		
		initListView();
		findViewById(R.id.btn_insertTestData).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				createTestData();
			}
		});
		queryData(0, STATE_REFRESH);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void createTestData(){
		for (int i = 0; i < 20; i++) {
			TestData td = new TestData();
			td.setName("��������  "+i);
			td.save(this);
		}
	}

	private void initListView() {
		mPullToRefreshView = (PullToRefreshListView) findViewById(R.id.list);
		loadingLayout = mPullToRefreshView.getLoadingLayoutProxy();
		loadingLayout.setLastUpdatedLabel("");
		loadingLayout
				.setPullLabel(getString(R.string.pull_to_refresh_bottom_pull));
		loadingLayout
				.setRefreshingLabel(getString(R.string.pull_to_refresh_bottom_refreshing));
		loadingLayout
				.setReleaseLabel(getString(R.string.pull_to_refresh_bottom_release));
		// //��������
		mPullToRefreshView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				if (firstVisibleItem == 0) {
					loadingLayout.setLastUpdatedLabel("");
					loadingLayout
							.setPullLabel(getString(R.string.pull_to_refresh_top_pull));
					loadingLayout
							.setRefreshingLabel(getString(R.string.pull_to_refresh_top_refreshing));
					loadingLayout
							.setReleaseLabel(getString(R.string.pull_to_refresh_top_release));
				} else if (firstVisibleItem + visibleItemCount + 1 == totalItemCount) {
					loadingLayout.setLastUpdatedLabel("");
					loadingLayout
							.setPullLabel(getString(R.string.pull_to_refresh_bottom_pull));
					loadingLayout
							.setRefreshingLabel(getString(R.string.pull_to_refresh_bottom_refreshing));
					loadingLayout
							.setReleaseLabel(getString(R.string.pull_to_refresh_bottom_release));
				}
			}
		});

		// ����ˢ�¼���
		mPullToRefreshView
				.setOnRefreshListener(new OnRefreshListener2<ListView>() {

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						// ����ˢ��(�ӵ�һҳ��ʼװ������)
						queryData(0, STATE_REFRESH);
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						// �������ظ���(������һҳ����)
						queryData(curPage, STATE_MORE);
					}
				});

		mMsgListView = mPullToRefreshView.getRefreshableView();
		// ������adapter
		mMsgListView.setAdapter(new DeviceListAdapter(this));
	}
	
	private static final int STATE_REFRESH = 0;// ����ˢ��
	private static final int STATE_MORE = 1;// ���ظ���
	
	private int limit = 10;		// ÿҳ��������10��
	private int curPage = 0;		// ��ǰҳ�ı�ţ���0��ʼ
	
	/**
	 * ��ҳ��ȡ����
	 * @param page	ҳ��
	 * @param actionType	ListView�Ĳ������ͣ�����ˢ�¡��������ظ��ࣩ
	 */
	private void queryData(final int page, final int actionType){
		Log.i("bmob", "pageN:"+page+" limit:"+limit+" actionType:"+actionType);
		
		BmobQuery<TestData> query = new BmobQuery<TestData>();
		query.setLimit(limit);			// ����ÿҳ����������
		query.setSkip(page*limit);		// �ӵڼ������ݿ�ʼ��
		query.findObjects(this, new FindListener<TestData>() {
			
			@Override
			public void onSuccess(List<TestData> arg0) {
				// TODO Auto-generated method stub
				
				if(arg0.size()>0){
					if(actionType == STATE_REFRESH){
						// ��������ˢ�²���ʱ������ǰҳ�ı������Ϊ0������bankCards��գ��������
						curPage = 0;
						bankCards.clear();
					}
					
					// �����β�ѯ��������ӵ�bankCards��
					for (TestData td : arg0) {
						bankCards.add(td);
					}
					
					// ������ÿ�μ��������ݺ󣬽���ǰҳ��+1������������ˢ�µ�onPullUpToRefresh�����оͲ���Ҫ����curPage��
					curPage++;
					showToast("��"+(page+1)+"ҳ���ݼ������");
				}else if(actionType == STATE_MORE){
					showToast("û�и���������");
				}else if(actionType == STATE_REFRESH){
					showToast("û������");
				}
				mPullToRefreshView.onRefreshComplete();
			}
			
			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				showToast("��ѯʧ��:"+arg1);
				mPullToRefreshView.onRefreshComplete();
			}
		});
	}
	
	
	private class DeviceListAdapter extends BaseAdapter  {
		
		Context context;
		
		public DeviceListAdapter(Context context){
			this.context = context;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				
				convertView = LayoutInflater.from(context)
						.inflate(R.layout.list_item_bankcard, null);
				holder = new ViewHolder();
				holder.tv_cardNumber = (TextView) convertView
						.findViewById(R.id.tv_cardNumber);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			TestData td = (TestData) getItem(position);

			holder.tv_cardNumber.setText(td.getName());
			return convertView;
		}

		class ViewHolder {
			TextView tv_cardNumber;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return bankCards.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return bankCards.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

	}
	
	private void showToast(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

}

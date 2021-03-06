package com.intelligencencu.Fragment.Schoollife;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.intelligencencu.activity.LoginActivity;
import com.intelligencencu.activity.LostFoundActivity;
import com.intelligencencu.adapter.LostAdapter;
import com.intelligencencu.db.Lost;
import com.intelligencencu.intelligencencu.R;
import com.intelligencencu.utils.IsLogin;
import com.intelligencencu.utils.ToastUntil;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static android.app.Activity.RESULT_OK;

/**
 * Created by liangzhan on 17-3-28.
 * 失物招领Fragment
 */

public class LostFoundFragment extends Fragment {

    private FloatingActionButton float_lostfound;
    private SwipeRefreshLayout swip_lostfound;
    private RecyclerView rlv_lostfound;
    private List<Lost> mLostlist;

    private static final int STATE_REFRESH = 0;// 下拉刷新
    private static final int STATE_MORE = 1;// 加载更多

    private static final int LOSTREQ = 0;// 请求码

    private boolean isFirstLastPosition = true;
    private int count = 20;        // 每页的数据是20条
    private int curPage = 0;        // 当前页的编号，从0开始
    private LostAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lostfound, container, false);
        initView(view);
        initEvent();
        getList(0, STATE_REFRESH);
        return view;
    }

    private void initDate() {
        if (mLostlist != null) {
            if (adapter == null) {
                adapter = new LostAdapter(mLostlist);
                rlv_lostfound.setAdapter(adapter);
                Log.d("null", mLostlist.size() + "");
            } else {
                Log.d("notifyDataSetChanged", mLostlist.size() + "");
                adapter.notifyDataSetChanged();
            }
        } else {
            ToastUntil.showShortToast(getActivity(), "数据为空！");
        }
        //使用notifyDataSetChanged方法更新列表数据时，一定要保证数据为同个对象
        //lostAdapter.notifyDataSetChanged();
        swip_lostfound.setRefreshing(false);
    }

    //查询数据的方法
    private void getList(int page, final int actionType) {
        BmobQuery<Lost> query = new BmobQuery<>();
        //按照时间降序
        query.order("-createdAt");
        // 如果是加载更多
        if (actionType == STATE_MORE) {
            // 跳过之前页数并去掉重复数据
            query.setSkip(page * count);
        } else {
            page = 0;
            query.setSkip(page);
        }
        query.setLimit(count);
        query.findObjects(new FindListener<Lost>() {
            @Override
            public void done(List<Lost> list, BmobException e) {
                if (e == null) {
                    if (list.size() > 0) {
                        Log.d("list.size()", "" + list.size());
                        if (actionType == STATE_REFRESH) {
                            // 当是下拉刷新操作时，将当前页的编号重置为0，并把bankCards清空，重新添加
                            curPage = 0;
                            if (mLostlist != null)
                                mLostlist.clear();
                        }
                        for (Lost lost : list) {
                            mLostlist.add(lost);
                        }
                        //mLostlist = list;
                        // 这里在每次加载完数据后，将当前页码+1，这样在上拉刷新的onPullUpToRefresh方法中就不需要操作curPage了
                        curPage++;
                        isFirstLastPosition=true;
                    } else if (actionType == STATE_MORE) {
                        ToastUntil.showShortToast(getActivity(), "没有更多数据了");
                    } else if (actionType == STATE_REFRESH) {
                        ToastUntil.showShortToast(getActivity(), "没有数据");
                    }
                    initDate();
                } else {
                    ToastUntil.showShortToast(getActivity(), "" + e);
                    Log.d("query", "" + e);
                }
            }
        });
    }

    private void initEvent() {
        //悬浮按钮
        float_lostfound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoLostFound();
            }
        });
        //刷新监听
        swip_lostfound.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("onRefresh", "到了这里！");
                //先清空缓存
                // 下拉刷新(从第一页开始装载数据)
                getList(0, STATE_REFRESH);
            }
        });

        //RecyclerView位置监听
        rlv_lostfound.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                //判断是当前layoutManager是否为LinearLayoutManager
                //只有LinearLayoutManager才有查找第一个和最后一个可见view位置的方法
                if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                    //获取最后一个可见view的位置
                    int lastItemPosition = linearManager.findLastVisibleItemPosition();
                    if (lastItemPosition == count * curPage - 1) {
                        if (isFirstLastPosition){
                            isFirstLastPosition=false;
                            ToastUntil.showShortToast(getActivity(), "正在加载更多...");
                            getList(curPage, STATE_MORE);
                        }
                    }
                    Log.d("sItemPosition", "" + "" + lastItemPosition);
                }
            }
        });
    }

    private void gotoLostFound() {
        Intent intent = new IsLogin().isLogin() ? new Intent(getActivity(), LostFoundActivity.class) : new Intent(getActivity(), LoginActivity.class);
        //startActivity(intent);
        startActivityForResult(intent, LOSTREQ);
    }

    private void initView(View view) {
        float_lostfound = (FloatingActionButton) view.findViewById(R.id.float_lostfound);
        swip_lostfound = (SwipeRefreshLayout) view.findViewById(R.id.swip_lostfound);
        swip_lostfound.setColorSchemeResources(R.color.colorPrimary);
        swip_lostfound.setRefreshing(true);
        rlv_lostfound = (RecyclerView) view.findViewById(R.id.rlv_lostfound);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rlv_lostfound.setLayoutManager(layoutManager);
        mLostlist = new ArrayList<>();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_OK:
                getList(0, STATE_REFRESH);
                break;
        }
    }
}

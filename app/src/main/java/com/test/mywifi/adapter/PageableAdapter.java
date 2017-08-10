package com.test.mywifi.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by ZXW23 on 2017/8/3.
 */

public class PageableAdapter extends BaseAdapter {
    public static final int DATA_FROM_CURSOR = 1;
    public static final int DATA_FROM_LIST = 2;

    private static final int DEF_PAGESIZE = 5;

    private int totalCount;    //内容总条数
    private int pageSize;    //一页显示的行数
    private int pageIndex;    //当前显示的页码
    private int pageCount;    //总页数
    private int srcType;    //数据来源

    private boolean showLineNum;    //显示行标
//    private boolean hasNext, hasPrev;    //标志是否有上一页/下一页

    private Context context;
    private LayoutInflater mInflater;    //布局文件解析器
    private int layout;        //布局文件资源ID

        private Cursor cursor;    //数据库查询游标
    private List<? extends HashMap<String, ?>> list;    //List数据来源

    private String[] from;    //数据来源标志
    private int[] to;    //数据去向 （显示控件ID）


    /**
     * 构造器
     * 适用于数据库数据显示
     * @param context 上下文
     * @param layout ListItem 布局文件
     * @param c 数据库查询游标
     * @param from 数据库列标
     * @param to 对应于列标 显示的容器
     */
    public PageableAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to)
    {
        super();

        this.context = context;
        this.layout = layout;
        this.cursor = cursor;
        this.from = from;
        this.to = to;
        //获取系统布局文件解析器
        this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //数据初始化
        srcType = DATA_FROM_CURSOR;
        totalCount = cursor.getCount();
        pageSize = DEF_PAGESIZE;
        showLineNum = true;
        pageIndex = 1;
        countPage();
    }

    /**
     * 适配器
     * 数据来源 List 继承自HashMap 采取键值对形式传入参数
     */
    public PageableAdapter(Context context, List<? extends HashMap<String, ?>> list, String[] from, int[] to) {
        super();
        this.context = context;
        this.list = list;
        this.from = from;
        this.to = to;

        //数据初始化
        srcType = DATA_FROM_LIST;
        totalCount = list.size();
        pageSize = DEF_PAGESIZE;
        showLineNum = true;
        pageIndex = 1;
        countPage();
    }

    /**
     * 内部方法，计算总页数
     */
    private void countPage() {
        pageCount = totalCount / pageSize;    //
        if (totalCount % pageSize > 0)    //最后一页不足pagesize个
            pageCount++;
    }

    /**
     * ListView通过此方法获知要显示多少行内容
     * 我们即在此方法下手，每次设置一页需要显示的行数
     * 返回值：ListView 要显示的行数
     */
    public int getCount() {
        if (totalCount < pageSize) {
            //如果总行数小于一页显示的行数，返回总行数
            return totalCount;
        } else if (totalCount < pageIndex * pageSize) {
            //即最后一页不足5行（页面行数）
            return (totalCount - (pageIndex - 1) * pageSize);
        } else { //其他情况返回页面尺寸
            return pageSize;
        }
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * 获取每一项item
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(layout, null);
        int index = position + pageSize * (pageIndex - 1);    //position对应到数据集中的正确位置

        //将各个要显示的值部署到显示控件
        for (int i = 0; i < Math.min(to.length, from.length); i++) {
            //控件ID
            TextView tv = (TextView) convertView.findViewById(to[i]);

            //要显示的内容
            String text = null;
            switch (srcType) {
                case DATA_FROM_CURSOR: {//cursor获取数据
                    cursor.moveToPosition(index);
                    text = cursor.getString(cursor.getColumnIndex(from[i]));
                    break;
                }
                case DATA_FROM_LIST: {//list获取数据
                    HashMap<String, ?> map;
                    map = list.get(index);
                    text = (String) map.get(from[i]).toString();
                    break;
                }
            }
            tv.setText(text);    //设置textview显示文本
        }
        return convertView;
    }

    /**
     * 返回列表是否有下一页
     */
    public boolean hasNextPg() {
        return pageIndex * pageSize < totalCount;
    }

    /**
     * 返回是否有上一页
     */
    public boolean hasPrevPg() {
        return pageIndex > 1;
    }

    /**
     * 下翻页，如果有下一页则返回成功
     */
    public boolean pgDown() {
        if (!hasNextPg())
            return false;
        pageIndex++;
        this.notifyDataSetChanged();

        return true;
    }

    /**
     * 上翻页
     */
    public boolean pgUp() {
        if (!hasPrevPg())
            return false;

        pageIndex--;
        this.notifyDataSetChanged();

        return true;
    }

    /**
     * 跳转到某个页码
     */
    public void gotoPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
        this.notifyDataSetChanged();
    }

    /**
     * 跳到第一页
     */
    public void gotoFirstPg() {
        if (pageIndex != 1) {
            pageIndex = 1;
            this.notifyDataSetChanged();
        }
    }

    /**
     * 跳到最后一页
     */
    public void gotoLastPg() {
        this.pageIndex = 1;
        this.notifyDataSetChanged();
    }

    /**
     * 获取一页行数
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 设置一页行数
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 获取总行数
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * 获取当前显示的页码
     */
    public int getPageIndex() {
        return pageIndex;
    }

    /**
     * 显示/影藏行号
     * ！未实现
     */
    public void showLineNum(boolean show) {
        this.showLineNum = show;
        this.notifyDataSetChanged();
    }
}

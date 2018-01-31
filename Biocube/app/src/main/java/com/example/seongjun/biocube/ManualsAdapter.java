package com.example.seongjun.biocube;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Eunmi on 2017-11-23.
 */

public class ManualsAdapter extends PagerAdapter {
    LayoutInflater inflater;
    int manualNum;
    String[] plantNameArray;
    Bitmap[] manualInitImgArray;
    ArrayList<ArrayList<String>> manualList;
    Context context;
    int position;


    public ManualsAdapter(LayoutInflater inflater, Context context) {
        this.inflater = inflater;
        this.context = context;
    }

    public void setManualNum(int manualNum) {
        this.manualNum = manualNum;
        plantNameArray = new String[manualNum];
        manualInitImgArray = new Bitmap[manualNum];
    }

    public void setPlantNameArray(String[] plantNameArray) {
        this.plantNameArray = plantNameArray;
    }

    public void setManualInitImg(Bitmap[] manualInitImg) {
        this.manualInitImgArray = manualInitImg;
    }

    public void setManualList(ArrayList<ArrayList<String>> manualList) {
        this.manualList = manualList;
    }

    /* PagerAdapter가 가지고 있는 View의 갯수를 return
    * 보통 보여줘야 하는 이미지 배열 데이터의 길이를 return
    * */
    @Override
    public int getCount() {
        return manualNum;
    }

    /* ViewPager가 현재 보여질 Item(View 객체)을 생성할 필요가 있는 때 자동으로 호출
    * 스크롤을 통해 현재 보여져야 하는 View를 만들어냄
    * 첫 번째 파라미터: ViewPager
    * 두 번째 파라미터: ViewPager가 보여줄 View의 위치(0, 1, 2...)
    * */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = null;

        //새로운 View 객체를 Layoutinflater를 이용해서 생성
        view = inflater.inflate(R.layout.viewpager_manuals, null);

        //만들어진 View 안에 있는 ImageView 객체 참조
        ImageView img = (ImageView) view.findViewById(R.id.img_manuals);

        //ImageView에 현재 position 번째에 해당하는 이미지를 보여주기 위한 작업
        //현재 position에 해당하는 이미지를 setting
        //img.setImageResource(R.drawable.plant_0+position);
        img.setImageBitmap(manualInitImgArray[position]);
        img.setOnClickListener(showManualClickListener);

        container.addView(view);

        return view;
    }

    /* 화면에 보이지 않은 View는 파괴를 해서 메모리를 관리함.
    * 첫 번째 파라미터: ViewPager
    * 두 번째 파라미터: 파괴될 View의 인덱스(0, 1, 2...)
    * 세 번째 파라미터: 파괴될 객체(더 이상 보이지 않은 View 객체)
    * */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    //instantiateItem() 메소드에서 리턴된 Object가 View가 맞는지 확인하는 메소드
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view==object);
    }

    View.OnClickListener showManualClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, PopManualActivity.class);
            intent.putExtra("plantName", plantNameArray[position]);
            intent.putExtra("manual", manualList.get(position));
            context.startActivity(intent);
        }
    };

    public void setCurrentPosition(int position) {
        this.position = position;
    }
}

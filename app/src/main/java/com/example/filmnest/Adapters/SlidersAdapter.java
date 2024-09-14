package com.example.filmnest.Adapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.filmnest.Domains.SliderItems;
import com.example.filmnest.R;

import java.util.List;

public class SlidersAdapter extends RecyclerView.Adapter<SlidersAdapter.SliderViewholder> {

    private List<SliderItems> sliderItems;
    private ViewPager2 viewPager2;
    private Context context;

    // Handler và Runnable để tự động chuyển slide sau mỗi 3 giây
    private Handler sliderHandler = new Handler();
    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            // Di chuyển đến slide tiếp theo
            viewPager2.setCurrentItem(viewPager2.getCurrentItem() + 1);
        }
    };

    public SlidersAdapter(List<SliderItems> sliderItems, ViewPager2 viewPager2) {
        this.sliderItems = sliderItems;
        this.viewPager2 = viewPager2;
    }

    @NonNull
    @Override
    public SlidersAdapter.SliderViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new SliderViewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_viewholder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SlidersAdapter.SliderViewholder holder, int position) {
        holder.setImage(sliderItems.get(position));

        // Kiểm tra nếu vị trí hiện tại là cuối danh sách
        if (position == sliderItems.size() - 2) {
            viewPager2.post(new Runnable() {
                @Override
                public void run() {
                    sliderItems.addAll(sliderItems);
                    notifyDataSetChanged();
                }
            });
        }

        // Đặt lại Handler khi trang thay đổi
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Đặt lại thời gian chờ cho Handler
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000); // 3 giây
            }
        });
    }

    @Override
    public int getItemCount() {
        return sliderItems.size();
    }

    public class SliderViewholder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView nameTxt, genreTxt, ageTxt, yearTxt, timeTxt;

        public SliderViewholder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSlide);
            nameTxt = itemView.findViewById(R.id.nameTxt);
            genreTxt = itemView.findViewById(R.id.genreTxt);
            ageTxt = itemView.findViewById(R.id.ageTxt);
            yearTxt = itemView.findViewById(R.id.yearTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
        }

        void setImage(SliderItems sliderItems) {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(60));
            Glide.with(context)
                    .load(sliderItems.getImage())
                    .apply(requestOptions)
                    .into(imageView);

            nameTxt.setText(sliderItems.getName());
            genreTxt.setText(sliderItems.getGenre());
            ageTxt.setText(sliderItems.getAge());
            yearTxt.setText("" + sliderItems.getYear());
            timeTxt.setText(sliderItems.getTime());
        }
    }

    // Phương thức này để dừng tự động chuyển slide
    public void stopSlider() {
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    // Phương thức này để khởi động lại tự động chuyển slide
    public void startSlider() {
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }
}

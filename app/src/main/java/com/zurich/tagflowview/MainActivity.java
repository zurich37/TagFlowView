package com.zurich.tagflowview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zurich.tagflow.TagFlowView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private int layoutPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnAdd = (Button) findViewById(R.id.button_add);
        Button btnStyle = (Button) findViewById(R.id.button_style);

        final TagFlowView tagFlowView = (TagFlowView) findViewById(R.id.flowView);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = new TextView(v.getContext());
                textView.setTextColor(Color.RED);
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) textView.getLayoutParams();
                if (layoutParams == null) {
                    layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                layoutParams.setMargins(dp2px(5), dp2px(5), dp2px(5), dp2px(5));
                textView.setBackgroundColor(getResources().getColor(R.color.gray));
                textView.setPadding(dp2px(3), dp2px(3), dp2px(3), dp2px(3));
                textView.setLayoutParams(layoutParams);
                textView.setText("随机" + new Random().nextInt());
                tagFlowView.addView(textView);
            }
        });
        layoutPosition = tagFlowView.getLayoutPosition();
        btnStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutPosition++;
                tagFlowView.setStyle(layoutPosition % 3);
            }
        });
        tagFlowView.setOnItemClickListener(new TagFlowView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(view.getContext(), ((TextView) view).getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int dp2px(int dpValue) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (density * dpValue);
    }
}

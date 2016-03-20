package com.example.cfy.mycourse;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by cfy on 2015/2/11.
 */
    /**
     * 修改记事本
     * 输入：note,当前的记事本信息
     */
public class CustomDialog extends Dialog {

    // 利用interface来构造一个回调函数
    public interface ICustomDialogEventListener {
        public void customDialogEvent(String valueYouWantToSendBackToTheActivity);
    }

    private ICustomDialogEventListener onCustomDialogEventListener;
    private Context mContext;
    private String mStr="Test";
    private String mNote;

    // 在构造函数中，设置回调函数
    //设置参数note，传递原来的文本
    public CustomDialog(Context context,String note,
                        ICustomDialogEventListener onCustomDialogEventListener) {
        //全屏模式
        super(context,R.style.Dialog_Fullscreen);

        //参数保存在本地变量中
        this.onCustomDialogEventListener = onCustomDialogEventListener;
        mNote=note;
    }

    //当你想把值传回去的时候，调用回调函数将值设置进去
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note);
        final EditText note_text = (EditText)findViewById(R.id.note_text);
        //文本内容赋值为传递来的参数
        note_text.setText(mNote);

        //确定按钮
        Button btnOk = (Button) findViewById(R.id.note_commit);
        btnOk.setOnClickListener( new Button.OnClickListener()
        {
            public void onClick(View v) {
                final EditText note_text = (EditText) findViewById(R.id.note_text);
                String note = note_text.getText().toString();
                //通过回调函数把note中的数据传回
                onCustomDialogEventListener.customDialogEvent(note);
                dismiss();
            }
        });

        //取消按钮
        Button buttonCancel = (Button) findViewById(R.id.note_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
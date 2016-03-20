package com.example.cfy.mycourse;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;
import android.graphics.drawable.BitmapDrawable;
import android.view.WindowManager;
import android.provider.MediaStore;
import java.io.File;

import com.example.cfy.clip.ClipActivity;
import com.example.cfy.util.FileUtils;
import com.example.cfy.util.ImageTools;

public class MainActivity extends AppCompatActivity {
    private GridView courseGrid; //课程表对应的GRID
    private GridView sideGrid; //左侧的序号对应的GRID
    private TextView textView; //记事本对应的文本框
    private ImageView headPhoto; //头像对应的view
    private PopupWindow popWindow;  //对应下面的滑出窗口
    private LayoutInflater layoutInflater;

    private ScheduleDbAdapter mDbAdapter;  //数据库
    private ScheduleSimpleCursorAdapter mCursorAdapter; //控制光标

    public static final int PHOTOSELECT = 0; // 相册
    public static final int PHOTOTAKE = 1; // 拍照
    public static final int IMAGE_CROP = 2; // 裁剪

    private String ClipCacheDir;//中间文件的保存目录
    private String photoSaveName;//拍照存储在该文件
    private String headFileName;//头像保存的文件名称，全路径


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //使用activity_main布局
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 设置Title
        toolbar.setTitle("我的快乐课程表");
        setSupportActionBar(toolbar);

        //设置上方工具条的点击响应
        toolbar.setOnMenuItemClickListener(onMenuItemClick);

        //显示头像,并设置点击响应
        handlePhoto();

        //显示左边数字序号应的GRID
        handleSideGrid();

        //显示课程表GRID，并设置点击响应
        handleCourseGrid();

        //显示记事本，并设置点击响应
        handleNote();
    }

    //显示Toolbar的菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //Toolbar菜单的点击响应
    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            String msg = "";
            switch (menuItem.getItemId()) {
                case R.id.action_edit:
                    msg += "Click edit";
                    break;
                case R.id.action_share:
                    msg += "Click share";
                    break;
                case R.id.action_settings:
                    msg += "Click setting";
                    break;
            }

            if (!msg.equals("")) {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    };

    /**
     *显示头像，并处理点击等操作响应
     */
    private void handlePhoto(){
        //初始化目录，保存拍照文件和中间文件
        File file = new File(Environment.getExternalStorageDirectory(), "MyCourse/cache");
        if (!file.exists())
            file.mkdirs();
        ClipCacheDir = Environment.getExternalStorageDirectory() + "/MyCourse/cache/";

        //拍照存储在该文件
        //photoSaveName = System.currentTimeMillis() + ".png";
        //头像保存的文件名称，全路径
        headFileName = Environment.getExternalStorageDirectory() + "/MyCourse/myphoto.png";

        //显示头像
        headPhoto = (ImageView) findViewById(R.id.myself);
        showMyPhoto(headFileName);

        //设置头像的点击动作
        headPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupWindow(headPhoto);
            }
        });
    }


    public void showMyPhoto(String pathName) {
        File f = new File(pathName);
        //如果存在头像文件，显示该头像
        if (f.exists()) {
            headPhoto.setImageURI(Uri.fromFile(f));
        } else {
            //否则使用灰色头像
            headPhoto.setImageResource(R.mipmap.biz_forum_author_icon);
        }
    }

    //在点击头像后底端弹出滑动窗口：选择拍照，相册
    @SuppressWarnings("deprecation")
    private void showPopupWindow(View parent) {
        if (popWindow == null) {
            View view = layoutInflater.inflate(R.layout.pop_select_photo, null);
            popWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
            initPop(view);
        }
        popWindow.setAnimationStyle(android.R.style.Animation_InputMethod);
        popWindow.setFocusable(true);
        popWindow.setOutsideTouchable(true);
        popWindow.setBackgroundDrawable(new BitmapDrawable());
        popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
    }

    //设置滑动窗口中各个按钮的动作
    public void initPop(View view) {

        TextView photograph = (TextView) view.findViewById(R.id.photograph);//拍照
        //设置点击“拍照”的动作响应
        photograph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //首先关闭窗口
                popWindow.dismiss();

                //由系统时间生成文件名，拍照的照片将保存在imageUri中
                photoSaveName = String.valueOf(System.currentTimeMillis()) + ".png";
                Uri imageUri = Uri.fromFile(new File(ClipCacheDir, photoSaveName));

                //然后调用拍照ACTIVITY
                //Intent指定要调用的activity
                Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //参数imageUri传递给Intent，告诉拍照应用将照片保存在该文件名中
                openCameraIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
                openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                //启动Activity, 并在 onActivityResult()中处理返回的数据
                // PHOTOTAKE将在 onActivityResult() 中返回，用来识别是哪个activity返回的数据.
                startActivityForResult(openCameraIntent, PHOTOTAKE);
            }
        });

        TextView albums = (TextView) view.findViewById(R.id.albums);//相册
        //设置点击“从相册选择”的动作响应
        albums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //首先关闭窗口
                popWindow.dismiss();

                //然后调用选择相片ACTIVITY
                //Intent指定要调用的activity
                Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                //传递的参数，指定要选择的文件类型为image
                openAlbumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

                //启动Activity, 并在 onActivityResult()中处理返回的数据
                // PHOTOSELECT将在 onActivityResult() 中返回，用来识别是哪个activity返回的数据.
                startActivityForResult(openAlbumIntent, PHOTOSELECT);
            }
        });

        LinearLayout cancel = (LinearLayout) view.findViewById(R.id.cancel);//取消
        //设置点击“取消”的动作响应
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                popWindow.dismiss();

            }
        });
    }

    /**
     * onActivityResult， 调用其他activity后结束时的回调函数，处理图片选择、拍照、裁剪结果
     * 其中resultCode标示来自哪个activity返回的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case PHOTOTAKE://拍照activity返回的数据
                //调用裁剪activity，并把要被裁剪的图像文件名称作为参数传递到intent
                //设置activity的名称：裁剪activity
                Intent intent2 = new Intent(MainActivity.this, ClipActivity.class);
                //调用裁剪功能时，要被裁剪的图像文件名称作为参数传递到intent
                //ClipCacheDir+photoSaveName 为拍照的存储文件
                intent2.putExtra("path", ClipCacheDir + photoSaveName);
                //启动裁剪图片的activity
                startActivityForResult(intent2, IMAGE_CROP);
                break;

            case PHOTOSELECT: //相册acivity返回的数据
                if (data == null) {
                    return;
                }
                //所选照片的uri在data中返回
                Uri uri = data.getData();
                //转换为真实文件名称
                String path = ImageTools.getRealFilePath(this, uri);
                //设置activity的名称：裁剪activity
                Intent intent3 = new Intent(MainActivity.this, ClipActivity.class);
                //参数path传递给Intent
                intent3.putExtra("path", path);
                //启动裁剪图片的activity
                startActivityForResult(intent3, IMAGE_CROP);
                break;

            case IMAGE_CROP: //裁剪activity返回的数据
                //byte方式返回
                byte[] byteArray = data.getByteArrayExtra("bitmap");
                Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                //更新头像
                headPhoto.setImageBitmap(bmp);
                //保存头像到指定文件
                ImageTools.savePhotoToSDCard(bmp, headFileName);
                //删除存储拍照的文件
                if (photoSaveName!=null){
                    FileUtils.deleteFile(new File(ClipCacheDir, photoSaveName));
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 处理左侧的序号Grid
     */
    private void handleSideGrid() {
        sideGrid = (GridView) findViewById(R.id.side_GridView);
        sideGrid.setAdapter(new SideGridViewAdapter(this));
        //设置grid不滚动
        setGridNoScroll(sideGrid);
    }

    //左侧的序号Grid对应的Adapter，显示为1...8
    private class SideGridViewAdapter extends BaseAdapter {
        private Context context;

        public SideGridViewAdapter(Context context) {
            this.context = context;
        }

        int count = 8;

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView result = new TextView(context);
            int temp = position + 1;
            result.setText("" + temp);
            result.setBackgroundColor(Color.WHITE); //设置背景颜色
            result.setTextColor(Color.BLACK);
            result.setTextSize(20);
            result.setHeight(100);
            result.setGravity(Gravity.CENTER);
            return result;
        }
    }

    private void setGridNoScroll(GridView grid){
        //设置grid不滚动
        grid.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //如果是ACTION_MOVE，返回true, 返回true表示不继续分发
                return MotionEvent.ACTION_MOVE == event.getAction() ? true
                        : false;
            }
        });
    }


    /**
     * 显示并处理课程表的响应
     *
     */
    private void handleCourseGrid() {
        //连接并打开数据库
        mDbAdapter = new ScheduleDbAdapter(this);
        mDbAdapter.open();
        //取出数据库中的数据
        Cursor cursor = mDbAdapter.fetchAllSchedule();
        //如果数据库为空，使用初始化数据
        if (cursor.getCount() == 0) insertSomeSchedule();
        //重新取出数据
        cursor = mDbAdapter.fetchAllSchedule();

        //指定使用数据库中的哪些列
        String[] from = new String[]{
                ScheduleDbAdapter.COL_CONTENT};
        //指定界面中的控件
        int[] to = new int[]{R.id.row_text};

        //创建ScheduleSimpleCursorAdapter对象mCursorAdapter，并指定控制的数据库数据和界面控件
        mCursorAdapter = new ScheduleSimpleCursorAdapter(
                //context
                MainActivity.this,
                //the layout of the row
                R.layout.schedule_item,
                //cursor
                cursor,
                //from columns defined in the db
                from,
                //to the ids of views in the layout
                to,
                //flag - not used
                0);

        //课程表数据对应的grid
        courseGrid = (GridView) findViewById(R.id.main_GridView);
        //设置cursorAdapter 作为controller用db中的数据控制courseGrid的更新
        courseGrid.setAdapter(mCursorAdapter);

        //设置grid不滚动
        setGridNoScroll(courseGrid);

        //设置grid点击动作,弹出修改课程名称的的对话框
        courseGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {

                //获取GRID中被点击Item的序号
                int nId = getIdFromPosition(position);
                Schedule schedule = mDbAdapter.fetchScheduleById(nId);
                //显示修改课程名称的对话框
                fireCustomDialog(schedule);
            }
        });

    }

    //获取GRID中被点击Item的序号
    private int getIdFromPosition(int nC) {
        return (int) mCursorAdapter.getItemId(nC);
    }

    //如果数据库中没有数据，往数据库中插入初始数据
    private void insertSomeSchedule() {
        String[] Course = {"语文", "数学", "英语", "物理", "化学",
                "生物", "语文", "数学", "物理", "化学",
                "英语", "语文", "数学", "物理", "生物",
                "化学", "数学", "英语", "数学", "物理",
                "语文", "语文", "语文", "语文", "语文",
                "英语", "语文", "数学", "语文", "物理",
                "物理", "物理", "化学", "化学", "数学",
                "数学", "生物", "英语", "物理", "化学"};
        int count = 40;
        for (int i = 0; i < count; i++) {
            mDbAdapter.createSchedule(Course[i]);
        }
    }


    /**
     * 修改课程名称的对话框
     * 输入：schedule 该格子中的课程ID,名称等信息
     */
    private void fireCustomDialog(final Schedule schedule) {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.edit_dialog);

        final EditText editCustom = (EditText) dialog.findViewById(R.id.custom_edit_reminder);
        //显示课程名称
        editCustom.setText(schedule.getContent());

        //确认对话框
        Button commitButton = (Button) dialog.findViewById(R.id.custom_button_commit);
        //设置确认对话框的动作
        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取课程名称
                String scheduleText = editCustom.getText().toString();
                Schedule scheduleEdited = new Schedule(schedule.getId(), scheduleText);
                //修改数据库中对应的课程名称
                mDbAdapter.updateSchedule(scheduleEdited);
                //更新mCursorAdapter
                mCursorAdapter.changeCursor(mDbAdapter.fetchAllSchedule());
                dialog.dismiss();
            }
        });

        //取消对话框
        Button buttonCancel = (Button) dialog.findViewById(R.id.custom_button_cancel);
        //设置取消对话框的动作
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    /**
     * 显示并处理记事本
     */
    private void handleNote() {
        //从preference读取note数据
        //并把从sharedpreference获得的数据显示在textView
        readNote();

        //设置点击动作
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String note;
                note = textView.getText().toString();
                fireNoteDialog(note);
            }
        });
    }

    /**
     * 启动修改记事本的对话框
     * 输入：note,当前的记录信息
     */
    private void fireNoteDialog(final String note) {

        //note参数，回调函数的名称 在这里传递，
        final CustomDialog dialog = new CustomDialog(this, note, new CustomDialog.ICustomDialogEventListener() {
            public void customDialogEvent(String value) {
                //在这里就获取到了从对话框传回来的值，value为传回的数据
                //将传回的value更新到note文本框
                final TextView note_text = (TextView) findViewById(R.id.noteView);
                note_text.setText(value);
                //并永久保存
                writeNote(value);
            }
        });
        dialog.show();
    }

    //将note文本框中的数据永久保存
    private void writeNote(String value) {
        //write to sharedpreference
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("my_note", value);
        editor.commit();
    }

    //读取存储的数据并显示在note中
    private void readNote() {
        //从preference读取note数据
        String scheduleText;
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        scheduleText = sharedPref.getString("my_note", "");
        //把从sharedpreference获得的数据显示在noteView
        textView = (TextView) findViewById(R.id.noteView);
        if (scheduleText != null) textView.setText(scheduleText);
    }

}


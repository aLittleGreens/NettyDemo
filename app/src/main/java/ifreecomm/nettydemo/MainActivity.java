package ifreecomm.nettydemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ifreecomm.nettydemo.adapter.LogAdapter;
import ifreecomm.nettydemo.bean.LogBean;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NettyListener {

    private static final String TAG = "MainActivity";
    private Button mClearLog;
    private Button mSendBtn;
    private Button mConnect;
    private EditText mSendET;
    private RecyclerView mSendList;
    private RecyclerView mReceList;

    private LogAdapter mSendLogAdapter = new LogAdapter();
    private LogAdapter mReceLogAdapter = new LogAdapter();
    private NettyClient mNettyClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        initData();
        initlistener();
        mNettyClient = new NettyClient(Const.HOST, Const.TCP_PORT);
    }

    private void initlistener() {


    }

    private void initData() {
        LinearLayoutManager manager1 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mSendList.setLayoutManager(manager1);
        mSendList.setAdapter(mSendLogAdapter);

        LinearLayoutManager manager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mReceList.setLayoutManager(manager2);
        mReceList.setAdapter(mReceLogAdapter);

    }

    private void findViews() {
        mSendList = findViewById(R.id.send_list);
        mReceList = findViewById(R.id.rece_list);
        mSendET = findViewById(R.id.send_et);
        mConnect = findViewById(R.id.connect);
        mSendBtn = findViewById(R.id.send_btn);
        mClearLog = findViewById(R.id.clear_log);

        mConnect.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);
        mClearLog.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.connect:
                connect();
                break;

            case R.id.send_btn:
                if (!mNettyClient.getConnectStatus()) {
                    Toast.makeText(getApplicationContext(), "未连接,请先连接", LENGTH_SHORT).show();
                } else {
                    final String msg = mSendET.getText().toString();
                    if (TextUtils.isEmpty(msg.trim())) {
                        return;
                    }
                    mNettyClient.sendMsgToServer(msg, new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            if (channelFuture.isSuccess()) {                //4
                                Log.d(TAG, "Write auth successful");
                                logSend(msg);
                            } else {
                                Log.d(TAG, "Write auth error");
                            }
                        }
                    });
                    mSendET.setText("");
                }

                break;

            case R.id.clear_log:
                mReceLogAdapter.getDataList().clear();
                mSendLogAdapter.getDataList().clear();
                mReceLogAdapter.notifyDataSetChanged();
                mSendLogAdapter.notifyDataSetChanged();
                break;
        }
    }

    private void connect() {
        Log.d(TAG, "connect");
        if (!mNettyClient.getConnectStatus()) {
            mNettyClient.setListener(MainActivity.this);
            mNettyClient.connect();//连接服务器
        } else {
            mNettyClient.disconnect();
        }
    }

    @Override
    public void onMessageResponse(Object msg) {
        Log.e(TAG, "onMessageResponse:" + msg);
        logRece((String) msg);
//        byte[] bytes = byteBuf.array();
//
//        String hexFun3 = bytesToHexFun3(bytes, byteBuf.writerIndex());
//        logRece(hexFun3);
    }

    @Override
    public void onServiceStatusConnectChanged(final int statusCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (statusCode == NettyListener.STATUS_CONNECT_SUCCESS) {
                    Log.e(TAG, "STATUS_CONNECT_SUCCESS:");
                    mConnect.setText("DisConnect");
                } else {
                    Log.e(TAG, "onServiceStatusConnectChanged:" + statusCode);
                    mConnect.setText("Connect");
                }
            }
        });

    }

    private void logSend(String log) {
        LogBean logBean = new LogBean(System.currentTimeMillis(), log);
        mSendLogAdapter.getDataList().add(0, logBean);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSendLogAdapter.notifyDataSetChanged();
            }
        });

    }

    private void logRece(String log) {
        LogBean logBean = new LogBean(System.currentTimeMillis(), log);
        mReceLogAdapter.getDataList().add(0, logBean);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mReceLogAdapter.notifyDataSetChanged();
            }
        });

    }

    /**
     * 方法三：
     * byte[] to hex string
     *
     * @param bytes
     * @return
     */
    public static String bytesToHexFun3(byte[] bytes, int length) {
        StringBuilder buf = new StringBuilder(length * 2);
        for (int i = 0; i < length; i++) {// 使用String的format方法进行转换

            buf.append(String.format("%02x", new Integer(bytes[i] & 0xFF)));
        }
        return buf.toString();
    }

    public void disconnect(View view) {
        mNettyClient.disconnect();
    }
}

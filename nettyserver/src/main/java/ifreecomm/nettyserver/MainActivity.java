package ifreecomm.nettyserver;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ifreecomm.nettyserver.adapter.LogAdapter;
import ifreecomm.nettyserver.bean.LogBean;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NettyListener {

    private static final String TAG = "MainActivity";
    private Button mClearLog;
    private Button mSendBtn;
    private Button startServer;
    private EditText mSendET;
    private RecyclerView mSendList;
    private RecyclerView mReceList;

    private LogAdapter mSendLogAdapter = new LogAdapter();
    private LogAdapter mReceLogAdapter = new LogAdapter();
    private TextView receiveTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        initData();
        initlistener();
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
        mSendBtn = findViewById(R.id.send_btn);
        mClearLog = findViewById(R.id.clear_log);
        startServer = findViewById(R.id.startServer);
        receiveTv = findViewById(R.id.receiveTv);

        startServer.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);
        mClearLog.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.startServer:

                startServer();

                break;

            case R.id.send_btn:
                if (!EchoServer.getInstance().getConnectStatus()) {
                    Toast.makeText(getApplicationContext(), "未连接,请先连接", LENGTH_SHORT).show();
                } else {
                    final String msg = mSendET.getText().toString();
                    if (TextUtils.isEmpty(msg.trim())) {
                        return;
                    }
                    EchoServer.getInstance().sendMsgToServer(msg, new ChannelFutureListener() {
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

    private void startServer() {

        if(!EchoServer.getInstance().isServerStart()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    EchoServer.getInstance().setListener(MainActivity.this);
                    EchoServer.getInstance().start();
                }
            }).start();

        }else{
            EchoServer.getInstance().disconnect();
        }
    }

    @Override
    public void onMessageResponse(Object msg) {
        // TODO Auto-generated method stub

        // ByteBuf in = (ByteBuf) msg;
        System.out.println("Server received: " + msg); // 2
        logRece((String) msg);
    }
    @Override
    public void onChannel(final Channel channel) {
        EchoServer.getInstance().setChannel(channel);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                receiveTv.setText("接收("+channel.toString()+")");
            }
        });

    }

    @Override
    public void onStartServer() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startServer.setText("stopServer");
            }
        });
    }

    @Override
    public void onStopServer() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startServer.setText("startServer");
            }
        });

    }

    @Override
    public void onServiceStatusConnectChanged(final int statusCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (statusCode == NettyListener.STATUS_CONNECT_SUCCESS) {
                    Log.e(TAG, "STATUS_CONNECT_SUCCESS:");
                } else {
                    Log.e(TAG, "onServiceStatusConnectChanged:" + statusCode);
                    receiveTv.setText("接收");
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

}

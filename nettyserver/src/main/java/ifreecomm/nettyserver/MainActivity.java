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
import ifreecomm.nettyserver.netty.NettyServerListener;
import ifreecomm.nettyserver.netty.NettyTcpServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NettyServerListener<String> {

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
                if (!NettyTcpServer.getInstance().getConnectStatus()) {
                    Toast.makeText(getApplicationContext(), "未连接,请先连接", LENGTH_SHORT).show();
                } else {
                    final String msg = mSendET.getText().toString();
                    if (TextUtils.isEmpty(msg.trim())) {
                        return;
                    }
                    NettyTcpServer.getInstance().sendMsgToServer(msg, new ChannelFutureListener() {
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

        if (!NettyTcpServer.getInstance().isServerStart()) {
            NettyTcpServer.getInstance().setListener(MainActivity.this);
            NettyTcpServer.getInstance().start();
        } else {
            NettyTcpServer.getInstance().disconnect();
        }
    }

    @Override
    public void onMessageResponseServer(String msg,String uniqueId) {
//        Log.e(TAG,"onMessageResponseServer:ChannelId:"+uniqueId);
        logRece(msg);
    }

    @Override
    public void onChannelConnect(final Channel channel) {
//        Log.e(TAG,"asLongText:"+channel.id().asLongText());
//        Log.e(TAG,"asShortText:"+channel.id().asShortText());
//        Log.e(TAG,"localAddress:"+channel.localAddress());
//        Log.e(TAG,"remoteAddress:"+channel.remoteAddress());
        NettyTcpServer.getInstance().setChannel(channel);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                receiveTv.setText("接收(" + channel.toString() + ")");
            }
        });

    }

    @Override
    public void onStartServer() {
        Log.e(TAG,"onStartServer");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startServer.setText("stopServer");
            }
        });
    }

    @Override
    public void onStopServer() {
        Log.e(TAG,"onStopServer");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startServer.setText("startServer");
            }
        });
    }

    @Override
    public void onChannelDisConnect(Channel channel) {
        Log.e(TAG,"onChannelDisConnect:ChannelId"+channel.id().asShortText());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                receiveTv.setText("接收");
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

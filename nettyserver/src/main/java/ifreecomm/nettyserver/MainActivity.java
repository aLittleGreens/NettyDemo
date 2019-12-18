package ifreecomm.nettyserver;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ifreecomm.nettyserver.adapter.CustomSpinnerAdapter;
import ifreecomm.nettyserver.adapter.LogAdapter;
import ifreecomm.nettyserver.bean.ClientChanel;
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

    List<ClientChanel> clientChanelArray = new ArrayList<>(); //储存客户端通道信息
    private Spinner mSpinner;
    private CustomSpinnerAdapter spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        initData();
        initlistener();
    }

    private void initlistener() {

        spinnerAdapter = new CustomSpinnerAdapter(this, clientChanelArray);

        mSpinner.setAdapter(spinnerAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ClientChanel clientChanel = spinnerAdapter.getItem(position);
                Toast.makeText(MainActivity.this, "onItemSelected:" + clientChanel.getClientIp(), Toast.LENGTH_LONG).show();
                NettyTcpServer.getInstance().selectorChannel(clientChanel.getChannel());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                NettyTcpServer.getInstance().selectorChannel(null);
                Toast.makeText(MainActivity.this, "onNothingSelected", Toast.LENGTH_LONG).show();
            }
        });
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
        mSpinner = findViewById(R.id.spinner);

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
                if (!NettyTcpServer.getInstance().isServerStart()) {
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
        NettyTcpServer nettyTcpServer = NettyTcpServer.getInstance();
//        nettyTcpServer.setPacketSeparator("#");
        if (!nettyTcpServer.isServerStart()) {
            nettyTcpServer.setListener(MainActivity.this);

            nettyTcpServer.start();
        } else {
            NettyTcpServer.getInstance().disconnect();
        }
    }

    @Override
    public void onMessageResponseServer(String msg, String uniqueId) {
//        Log.e(TAG,"onMessageResponseServer:ChannelId:"+uniqueId);
        logRece(msg);
    }

    @Override
    public void onChannelConnect(final Channel channel) {

        String socketStr = channel.remoteAddress().toString();
        final ClientChanel clientChanel = new ClientChanel(socketStr, channel, channel.id().asShortText());

        synchronized (clientChanelArray) {
            clientChanelArray.add(clientChanel);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, clientChanel.getClientIp() + " 建立连接", Toast.LENGTH_LONG).show();
                    spinnerAdapter.notifyDataSetChanged();
                }
            });
        }

    }

    @Override
    public void onChannelDisConnect(Channel channel) {
        Log.e(TAG, "onChannelDisConnect:ChannelId" + channel.id().asShortText());

        for (int i = 0; i < clientChanelArray.size(); i++) {
            final ClientChanel clientChanel = clientChanelArray.get(i);
            if (clientChanel.getShortId().equals(channel.id().asShortText())) {

                /**
                 * 当Spinner里第一个item被remove，不会触发onItemSelected，（因为 mSelectedPosition != mOldSelectedPosition）
                 */
                if (i == 0) {
                    try {
                        Field field = AdapterView.class.getDeclaredField("mOldSelectedPosition");
                        field.setAccessible(true);  //设置mOldSelectedPosition可访问
                        field.setInt(mSpinner, AdapterView.INVALID_POSITION); //设置mOldSelectedPosition的值
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                synchronized (clientChanelArray) {
                    clientChanelArray.remove(clientChanel);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "disconncect " + clientChanel.getClientIp());
                            Toast.makeText(MainActivity.this, clientChanel.getClientIp() + " 断开连接", Toast.LENGTH_LONG).show();
                            spinnerAdapter.notifyDataSetChanged();
                        }
                    });
                }

                return;
            }
        }

    }

    @Override
    public void onStartServer() {
        Log.e(TAG, "onStartServer");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startServer.setText("stopServer");
            }
        });
    }

    @Override
    public void onStopServer() {
        Log.e(TAG, "onStopServer");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startServer.setText("startServer");
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

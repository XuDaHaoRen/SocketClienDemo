package activity.xbl.com.socketcliendemo;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button start_btn;
    private Button write_btn;
    private ConnectThread connectThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        start_btn = (Button) findViewById(R.id.start_btn);
        write_btn = (Button) findViewById(R.id.write_btn);
        start_btn.setOnClickListener(this);
        write_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //启动客户端
            case R.id.start_btn:
                new ClientSocketThread().start();
                break;
            //向服务端写数据
            case R.id.write_btn:
                String data = "hello_server";
                connectThread.write(data.getBytes());
                break;
        }

    }

    //数据连接发送信息线程的实例化
    private void connected(Socket socket) {
        if (connectThread != null) {
            connectThread.cancle();
            connectThread = null;
        }
        connectThread = new ConnectThread(socket);
        connectThread.start();
        Log.d("TAG", "connect success");
    }

    class ClientSocketThread extends Thread {
        @Override
        public void run() {
            //如果是跑在同一个手机上就可以是随意的IP地址，如果是不同手机上的，上面要是同一WIFI下的IP地址
            try {
                Socket socket = new Socket("192.168.1.104", 1234);//ip地址要是对方的地址
                //启动监听数据消息的线程
                connected(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //写数据和读取数据的线程
    class ConnectThread extends Thread {
        Socket socket;
        InputStream in = null;
        OutputStream out = null;

        public ConnectThread(Socket socket) {
            this.socket = socket;
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes = 0;
            //等待另一端发送的数据
            while (true) {
                try {
                    bytes = in.read(buffer);
                    Log.d("TAG", "client_read:" + new String(buffer, 0, bytes));
                    Log.d("TAG", "----------------------------------");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //向另一端写数据的操作
        public void write(byte[] buffer) {
            try {
                out.write(buffer);
                Log.d("TAG", "client_write:" + new String(buffer));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //关闭的方法
        public void cancle() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

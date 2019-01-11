import DTO.SocketAsyncEventArgs;
import Interface.*;
import com.sun.deploy.net.protocol.ProtocolType;
import javafx.event.EventHandler;

import java.io.Console;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.Semaphore;

public class ServerStart
{
    public LengthDecode LD;
    public LengthEncode LE;
    public Encode encode;
    private ThreadPool threadPool;//�̳߳�
    public Decode decode;
    public AbsHandlerCenter center;
    UserTokenPool pool;
    ServerSocket serverSocket;
    private final int POOL_SIZE=4;//����Cpuʱ�̳߳��й�������Ŀ
    int maxClient;//������������
    Semaphore acceptClients;//����ź������Ա����ͻ


    public ServerStart(int max) throws IOException
    {
//        server = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
        serverSocket = new ServerSocket();
        threadPool=new ThreadPool(Runtime.getRuntime().availableProcessors()*POOL_SIZE);
        this.maxClient = max;

    }
    public void start(int port)
    {
        while (true) {
            Socket socket=null;
            try {
                socket= serverSocket.accept();
                threadPool.execute(new Handler(socket));//����ͻ�ͨѶ�����񽻸��߳�
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }


        }

        pool = new UserTokenPool(maxClient);
        //�����ź���
        acceptClients = new Semaphore(maxClient);
        for(int i = 0; i < maxClient; i++)
        {
            UserToken token = new UserToken();
            token.receiverSAEA.Completed += new EventHandler<SocketAsyncEventArgs>(IO_Completed);
            token.sendSAEA.Completed += new EventHandler<SocketAsyncEventArgs>(IO_Completed);
            token.LD = this.LD;
            token.LE = this.LE;
            token.encode = this.encode;
            token.decode = this.decode;
            token.sendProcess = ProcessSend;
            token.closeProcess = ClientClose;
            token.center = center;
            pool.push(token);
        }
        try
        {
            server.bind(new InetSocketAddress(port));
//            server.Listen(10);
            StartAccept(null);
        }catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
    public void StartAccept(SocketAsyncEventArgs e)
    {
        if (e == null)
        {
            e = new SocketAsyncEventArgs();
            e.Completed += new EventHandler<SocketAsyncEventArgs>(Accept_Completed);
        }
        else
        {
            e.AcceptSocket = null;
        }
        //�ź���
        acceptClients.WaitOne();
        bool result = server.AcceptAsync(e);
        //�ж��첽�¼��Ƿ���ս��û�һ�˵������ִ����ɡ�ֱ�Ӵ����¼���������ɺ󴥷�
        if (!result)
        {
            ProcessAccept(e);
        }
    }
    public void Accept_Completed(object sender, SocketAsyncEventArgs e)
    {
        ProcessAccept(e);
    }
    public void ProcessAccept(SocketAsyncEventArgs e)
    {
        //���������¼�
        //�������Ӷ��󣬹��û�ʹ��
        UserToken token = pool.pop();
        center.ClientConnect(token);
        token.conn = e.AcceptSocket;
        StartReceive(token);
        StartAccept(e);
    }
    public void StartReceive(UserToken token)
    {
        try
        {
            bool result = token.conn.ReceiveAsync(token.receiverSAEA);
            if (!result)
            {
                ProcessReceive(token.receiverSAEA);
            }
        }catch(Exception e)
        {
            Console.WriteLine(e.Message);
        }
    }
    public void  IO_Completed(object sender,SocketAsyncEventArgs e)
    {
        if (e.LastOperation == SocketAsyncOperation.Receive)
        {
            ProcessReceive(e);
        }
        else
        {
            ProcessSend(e);
        }
    }


    public void ProcessReceive(SocketAsyncEventArgs e)
    {
        UserToken token = e.UserToken as UserToken;
        if (token.receiverSAEA.BytesTransferred > 0 && token.receiverSAEA.SocketError == SocketError.Success)
        {
            byte[] message = new byte[token.receiverSAEA.BytesTransferred];
            Buffer.BlockCopy(token.receiverSAEA.Buffer, 0, message,0, token.receiverSAEA.BytesTransferred);
            token.receive(message);
            StartReceive(token);
        }
        else
        {
            if (token.receiverSAEA.SocketError != SocketError.Success)
            {
                ClientClose(token, token.receiverSAEA.SocketError.ToString());
            }
            else
            {
                //��ʱ�ͻ����쳣�Ͽ�
                ClientClose(token, "�ͻ��������Ͽ�����");
            }
        }
    }
    public void ProcessSend(SocketAsyncEventArgs e)
    {
        UserToken token = e.UserToken as UserToken;
        if (e.SocketError != SocketError.Success)
        {
            ClientClose(token, e.SocketError.ToString());
        }
        {
            //��Ϣ���ͳɹ����ص�
            token.writed();
        }
    }
    public void ClientClose(UserToken token,string error)
    {
        if (token.conn != null)
        {
            lock (token)
            {
                center.ClientClose(token, error);
                token.Close();
                pool.push(token);
                acceptClients.Release();
            }
        }
    }

}
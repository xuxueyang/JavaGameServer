

import DTO.SocketAsyncEventArgs;
import Interface.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

interface SendProcess{
    public  void SendProcess(SocketAsyncEventArgs e);
}
interface CloseProcess{
    public  void CloseProcess(UserToken token, String error);
}
/**
 * �û����Ӷ���
 */
public class UserToken extends SocketImpl {
    /// <summary>
    /// �û�����
    /// </summary>
    public Socket conn;
    /// <summary>
    /// �첽������������
    /// </summary>
    public SocketAsyncEventArgs receiverSAEA;
    /// <summary>
    /// �û��첽������������
    /// </summary>
    public SocketAsyncEventArgs sendSAEA;

    public LengthDecode LD ;

    public LengthEncode LE ;
    public Encode encode;
    public Decode decode;

    public SendProcess sendProcess;
    public CloseProcess closeProcess;

    public AbsHandlerCenter center;
    List<Byte> cache = new ArrayList<Byte>();

    private Boolean isReading = false;
    private Boolean isWriting = false;
    Queue<byte[]> writeQueue = new LinkedBlockingQueue<byte[]>();

    public UserToken()
    {
        receiverSAEA = new SocketAsyncEventArgs();
        sendSAEA = new SocketAsyncEventArgs();
        receiverSAEA.userToken = this;//????���ԣ�Ϊʲô���Խӿڷ�ʽʵ���أ�
        sendSAEA.userToken = this;
        //���ý��ܶ���Ļ�������С
        receiverSAEA.SetBuffer(new byte[1024], 0, 1024);

    }
    /// <summary>
    /// ������Ϣ�ﵽ���첽
    /// </summary>
    /// <param name="buff"></param>
    public void receive(byte[] buff)
    {
        cache.AddRange(buff);
        if (!isReading)
        {
            isReading = true;
            onData();
        }
    }
    /// <summary>
    /// ��������
    /// </summary>
    private void onData()
    {
        //������Ϣ�洢����
        //��������Ƕ����ƣ���Ҫ����ͱ���
        byte[] buff = null;
        if (LD != null)
        {
            buff = LD(ref cache);
            //��Ϣ����δ����ȫ���˳����ȴ��´���Ϣ����
            if (buff == null)
            {
                isReading = false;
                return;
            }
        }
        else
        {
            //�����൱��û����ֱ��ȡ�����е㲻�ã���һʲôʲô�أ����ԣ�������ѡ���׳�����
            isReading = false;
            throw new Exception("length decode is null");
                /*
                if (cache.Count == 0)
                {
                    isReading = false;
                    return;
                }
                buff = cache.ToArray();
                cache.Clear();
                */
        }
        //��������
        if (decode == null)
        {
            throw new Exception("message decode is null");
        }
        //��Ϣ�����л�
        object message = decode(buff);
        //֪ͨӦ�ò�����Ϣ����
        center.MessageReceive(this, message);
        //�����ڴ����У�������Ϣ����
        onData();
    }

    public void write(byte[] value)
    {
        //������Ϣ���
        if (conn == null)
        {
            closeProcess(this, "�����Ѿ��Ͽ�������");
        }
        writeQueue.Enqueue(value);
        if (!isWriting)
        {
            isWriting = true;
            onWrite();
        }
    }
    public void onWrite()
    {
        if (writeQueue.Count == 0)
        {
            isWriting = false;return;
        }
        byte[] buff = writeQueue.Dequeue();
        sendSAEA.SetBuffer(buff, 0, buff.Length);
        bool result = conn.SendAsync(sendSAEA);
        if (!result)
        {
            sendProcess(sendSAEA);
        }
    }
    public void writed()
    {
        onWrite();
    }
    public void Close()
    {
        try
        {
            writeQueue.Clear();
            cache.Clear();
            isWriting = false;
            isReading = false;
            conn.Shutdown(SocketShutdown.Both);
            conn.Close();
            conn = null;
        }catch(Exception e)
        {
            Console.WriteLine(e.Message);
        }
    }

    @Override
    protected void create(boolean stream) throws IOException {

    }

    @Override
    protected void connect(String host, int port) throws IOException {

    }

    @Override
    protected void connect(InetAddress address, int port) throws IOException {

    }

    @Override
    protected void connect(SocketAddress address, int timeout) throws IOException {

    }

    @Override
    protected void bind(InetAddress host, int port) throws IOException {

    }

    @Override
    protected void listen(int backlog) throws IOException {

    }

    @Override
    protected void accept(SocketImpl s) throws IOException {

    }

    @Override
    protected InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    protected int available() throws IOException {
        return 0;
    }

    @Override
    protected void close() throws IOException {

    }

    @Override
    protected void sendUrgentData(int data) throws IOException {

    }

    @Override
    public void setOption(int optID, Object value) throws SocketException {

    }

    @Override
    public Object getOption(int optID) throws SocketException {
        return null;
    }
}

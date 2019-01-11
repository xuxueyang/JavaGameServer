import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public  class Handler implements Runnable{
    private Socket socket;
    public Handler(Socket socket){
        this.socket=socket;
    }

    private PrintWriter getWriter(Socket socket)throws IOException {
        OutputStream socketOut=socket.getOutputStream();
        return new PrintWriter(socketOut, true);//参数为true表示每写一行，PrintWriter缓存就自动溢出，把数据写到目的
    }

    private BufferedReader getReader(Socket socket)throws IOException {
        InputStream socketIn=socket.getInputStream();
        return new BufferedReader(new InputStreamReader(socketIn));

    }
    public String echo(String msg){
        return "echo:"+msg;

    }
    public void run(){
        try {
            //得到客户端的地址和端口号
            System.out.println("New connection accepted"+socket.getInetAddress()+":"+socket.getPort());
            BufferedReader br=getReader(socket);
            PrintWriter pw=getWriter(socket);

            String msg=null;
            while ((msg=br.readLine())!=null) {
                System.out.println(msg);
                pw.println(echo(msg));
                if(msg.endsWith("bye")){
                    break;
                }
            }
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }finally{
            try {
                if(socket!=null){
                    socket.close();
                }

            } catch (IOException  e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
    }
}

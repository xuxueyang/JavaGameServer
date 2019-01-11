package Interface;


public interface AbsHandlerCenter {
    public abstract void ClientConnect(UserToken token);
    public abstract void MessageReceive(UserToken token, Object message);
    public abstract void ClientClose(UserToken token, String error);
}
